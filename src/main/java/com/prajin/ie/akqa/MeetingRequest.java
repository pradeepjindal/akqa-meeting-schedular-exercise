package com.prajin.ie.akqa;

import java.time.LocalDateTime;

public class MeetingRequest {

    private String employeeCode;
    private LocalDateTime scheduleDateTime;
    private LocalDateTime meetingDateTime;
    private int meetingHours;

    public MeetingRequest() {

    }

    @Override
    public String toString() {
        return "MeetingRequest{" +
                "employeeCode='" + employeeCode + '\'' +
                ", scheduleDateTime=" + scheduleDateTime +
                ", meetingDateTime=" + meetingDateTime +
                ", meetingHours=" + meetingHours +
                '}';
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public LocalDateTime getScheduleDateTime() {
        return scheduleDateTime;
    }

    public void setScheduleDateTime(LocalDateTime scheduleDateTime) {
        this.scheduleDateTime = scheduleDateTime;
    }

    public LocalDateTime getMeetingDateTime() {
        return meetingDateTime;
    }

    public void setMeetingDateTime(LocalDateTime meetingDateTime) {
        this.meetingDateTime = meetingDateTime;
    }

    public int getMeetingHours() {
        return meetingHours;
    }

    public void setMeetingHours(int meetingHours) {
        this.meetingHours = meetingHours;
    }

}
