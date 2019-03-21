package edu.sutd.organice;

import java.text.SimpleDateFormat;
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

    public String toTextLine() {
        SimpleDateFormat format = new SimpleDateFormat("d/M H:mm");

        StringBuilder builder = new StringBuilder()
                .append('"')
                .append(title)
                .append("\" ")
                .append(format.format(dateStart))
                .append(" - ")
                .append(format.format(dateEnd));
        if (venue != null && !venue.isEmpty()) {
            builder.append(" at ");
            builder.append(venue);
        }
        return builder.toString();
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
