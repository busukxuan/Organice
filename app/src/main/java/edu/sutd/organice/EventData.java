package edu.sutd.organice;

import java.util.Date;

public class EventData {
    public final String title;
    public final Date dateStart, dateEnd;
    public final String venue;
    public final String note;

    EventData(String title, Date dateStart, Date dateEnd, String venue, String note) {
        this.title = title;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.venue = venue;
        this.note = note;
    }

    @Override
    public String toString() {
        return "EventData(" +
                title +
                "\", " +
                dateStart.toString() +
                ", " +
                dateEnd.toString() +
                ", \"" +
                venue +
                "\", \"" +
                note +
                "\")";
    }
}
