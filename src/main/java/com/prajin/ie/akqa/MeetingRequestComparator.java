package com.prajin.ie.akqa;

import java.util.Comparator;

public class MeetingRequestComparator implements Comparator<MeetingRequest> {

    @Override
    public int compare(MeetingRequest o1, MeetingRequest o2) {
        if(o1.getScheduleDateTime().isBefore(o2.getScheduleDateTime())) {
            return -1;
        } else if(o1.getScheduleDateTime().isAfter(o2.getScheduleDateTime())) {
            return 1;
        } else {
            return 0;
        }

    }
}
