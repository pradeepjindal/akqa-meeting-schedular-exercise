package com.prajin.ie.akqa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component("meetingScheduler")
public class MeetingScheduler {
    private static final Logger logger = LoggerFactory.getLogger(MeetingScheduler.class);

    @Value("${meeting.office.hours.pattern}")
    private String MEETING_OFFICE_HOURS_PATTERN;

    @Value("${meeting.request.by.pattern}")
    private String MEETING_REQUEST_BY_PATTERN;

    @Value("${meeting.request.for.pattern}")
    private String MEETING_REQUEST_FOR_PATTERN;

    @Value("${meeting.beyond.office.hours.allowed}")
    private boolean IS_MEETING_BEYOND_OFFICE_HOURS_ALLOWED;

    @Value("${meeting.interval}")
    private Integer MEETING_INTERVEL;

    @Value("${meeting.request.file.full.path}")
    private String DEFAULT_MEETING_REQUEST_FILE_FULL_PATH;


    public void schedule(String meetingRequestFileFullPath) {
        List<MeetingRequest> meetingRequests = new ArrayList<>();
        Map.Entry<String, Integer> lineNumber = new AbstractMap.SimpleEntry("lineNumber", 0);
        Map.Entry<String, MeetingRequest> meetingRequestEntry = new AbstractMap.SimpleEntry("meetingRequest", null);
        Map.Entry<String, LocalTime> officeOpenTimeEntry = new AbstractMap.SimpleEntry("officeOpenTime", null);
        Map.Entry<String, LocalTime> officeCloseTimeEntry = new AbstractMap.SimpleEntry("officeCloseTime", null);

        try {
            Stream<String> lines;
            if(meetingRequestFileFullPath == null && DEFAULT_MEETING_REQUEST_FILE_FULL_PATH.isEmpty()) {
                lines = Files.lines(Paths.get(getClass().getClassLoader().getResource("data-in.txt").toURI()));
            } else {
                lines = Files.lines(Paths.get(meetingRequestFileFullPath == null ? DEFAULT_MEETING_REQUEST_FILE_FULL_PATH : meetingRequestFileFullPath));
            }

            lines.forEach( line -> {
                lineNumber.setValue(lineNumber.getValue()+1);
                logger.info("reading line.{} [{}]", lineNumber.getValue(), line);

                boolean officeTimeFound = line.matches(MEETING_OFFICE_HOURS_PATTERN);
                boolean requestByFound = line.matches(MEETING_REQUEST_BY_PATTERN);
                boolean requestForFound = line.matches(MEETING_REQUEST_FOR_PATTERN);

                if(requestByFound) {
                    if(meetingRequestEntry.getValue() == null) {
                        String dateString = line.substring(0, 10);
                        String timeString = line.substring(11, 19);
                        LocalDate localDate = LocalDate.parse(dateString);
                        LocalTime localTime = LocalTime.parse(timeString);
                        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
                        //
                        MeetingRequest meetingRequest = new MeetingRequest();
                        meetingRequest.setScheduleDateTime(localDateTime);
                        meetingRequest.setEmployeeCode(line.substring(20, 26));
                        meetingRequestEntry.setValue(meetingRequest);
                    } else {
                        logger.info("incomplete request");
                    }
                }
                if(requestForFound) {
                    if(meetingRequestEntry.getValue() == null) {
                        logger.info("incomplete request");
                    } else {
                        String dateString = line.substring(0, 10);
                        String timeString = line.substring(11, 16);
                        LocalDate localDate = LocalDate.parse(dateString);
                        LocalTime localTime = LocalTime.parse(timeString);
                        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
                        //
                        MeetingRequest meetingRequest = meetingRequestEntry.getValue();
                        meetingRequestEntry.setValue(null);
                        meetingRequest.setMeetingDateTime(localDateTime);
                        int meetingHours = Integer.parseInt(line.substring(17));
                        meetingRequest.setMeetingHours(meetingHours);
                        meetingRequests.add(meetingRequest);
                    }
                }

                if(officeTimeFound && officeOpenTimeEntry.getValue() == null & officeCloseTimeEntry.getValue() == null) {
                    String officeOpenTimeString = line.substring(0, 2) + ":" + line.substring(2, 4);
                    officeOpenTimeEntry.setValue(LocalTime.parse(officeOpenTimeString));
                    String officeCloseTimeString = line.substring(5,7) + ":" + line.substring(7, 9);
                    officeCloseTimeEntry.setValue(LocalTime.parse(officeCloseTimeString));
                }
                if(!requestByFound && !requestForFound && !officeTimeFound) {
                    logger.info("mallformatted request");
                }
            });
            lines.close();
        } catch (IOException e) {
            logger.error("{}", e);
            throw new MeetingSchedulerRtException("file read error", e);
        } catch (URISyntaxException e) {
            logger.error("{}", e);
            throw new MeetingSchedulerRtException("path or filename is incorrect", e);
        }

        scheduleIt(meetingRequests, officeOpenTimeEntry.getValue(), officeCloseTimeEntry.getValue());
    }

    public void scheduleIt(List<MeetingRequest> meetingRequests, LocalTime officeOpenTime, LocalTime officeCloseTime) {
        Collections.sort(meetingRequests, new MeetingRequestComparator());
        Map<LocalDate, Map<LocalTime, MeetingRequest>> fullMeetingMap = new TreeMap<>();
        meetingRequests.forEach( meetingRequest -> {
            LocalTime meetingTime = meetingRequest.getMeetingDateTime().toLocalTime();
            int meetingLength = meetingRequest.getMeetingHours() * 60;
            LocalTime meetingEndTime = meetingRequest.getMeetingDateTime().toLocalTime().plusMinutes(meetingLength);
            if(IS_MEETING_BEYOND_OFFICE_HOURS_ALLOWED == true ||
                    (meetingEndTime.isBefore(officeCloseTime) || meetingEndTime.equals(officeCloseTime)))
            {
                if(fullMeetingMap.containsKey(meetingRequest.getMeetingDateTime().toLocalDate())) {
                    Map<LocalTime, MeetingRequest> dailyMeetingMap = fullMeetingMap.get(meetingRequest.getMeetingDateTime().toLocalDate());
                    boolean meetingSlotsAreAvailable = true;
                    do {
                        meetingSlotsAreAvailable = dailyMeetingMap.get(meetingTime) == null ? true: false;
                        meetingTime = meetingTime.plusMinutes(MEETING_INTERVEL);
                    } while (meetingTime.isBefore(meetingEndTime) && meetingSlotsAreAvailable);
                    if(!meetingSlotsAreAvailable) {
                        logger.info("meeting request ({} - {}) on {} by {} rejected: clashing with already scheduled meeting",
                                meetingRequest.getMeetingDateTime().toLocalTime(), meetingEndTime,
                                meetingRequest.getMeetingDateTime().toLocalDate(), meetingRequest.getEmployeeCode(),
                                officeOpenTime, officeCloseTime);
                        String schedule = dailyMeetingMap.entrySet().stream()
                                .filter( map -> map.getValue() != null )
                                .map( map -> map.getKey() + " | " + map.getValue().getEmployeeCode()).sorted()
                                .collect(Collectors.joining(", "));
                        logger.info("clashing with schedule: {}", schedule);
                    }
                    meetingTime = meetingRequest.getMeetingDateTime().toLocalTime();
                    while (meetingSlotsAreAvailable && meetingTime.isBefore(meetingEndTime)) {
                        dailyMeetingMap.put(meetingTime, meetingRequest);
                        meetingTime = meetingTime.plusMinutes(MEETING_INTERVEL);
                    };
                } else {
                    Map<LocalTime, MeetingRequest> dailyMeetingMap = createDailyMeetingMap(officeOpenTime, officeCloseTime);
                    do {
                        dailyMeetingMap.put(meetingTime, meetingRequest);
                        meetingTime = meetingTime.plusMinutes(MEETING_INTERVEL);
                    } while (meetingTime.isBefore(meetingEndTime));
                    fullMeetingMap.put(meetingRequest.getMeetingDateTime().toLocalDate(), dailyMeetingMap);
                }
            } else {
                logger.info("meeting request ({} - {}) on {} by {} rejected: falling outside office hours ({} - {})",
                        meetingRequest.getMeetingDateTime().toLocalTime(), meetingEndTime,
                        meetingRequest.getMeetingDateTime().toLocalDate(), meetingRequest.getEmployeeCode(),
                        officeOpenTime, officeCloseTime);
            }
        });
        logger.info("Full Meeting Schedule as on {}:", LocalDateTime.now());
        fullMeetingMap.forEach( (key, dailyMeetingMap) -> {
            String schedule = dailyMeetingMap.entrySet().stream()
                    .filter( map -> map.getValue() != null )
                    .map( map -> map.getKey() + " | " + map.getValue().getEmployeeCode()).sorted()
                    .collect(Collectors.joining(", "));
            logger.info("schedule for ({}): [{}]", key, schedule);
            //since it is a file based application,
            //same result should also be written to file here to be consumable by another program.
        });
        return;
    }

    public Map<LocalTime, MeetingRequest> createDailyMeetingMap(LocalTime officeOpenTime, LocalTime officeCloseTime) {
        Map<LocalTime, MeetingRequest> dailyMeetingMap = new HashMap<>();
        do {
            dailyMeetingMap.put(officeOpenTime, null);
            officeOpenTime = officeOpenTime.plusMinutes(MEETING_INTERVEL);
        } while (officeOpenTime.isBefore(officeCloseTime));
        return dailyMeetingMap;
    }

}
