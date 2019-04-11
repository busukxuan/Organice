package edu.sutd.organice;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    public String toMessageFormat() {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/YYYY HH:mm");

        List<String> lines = new ArrayList<>(5);
        if (title != null && !title.isEmpty()) {
            lines.add("Title: " + title);
        }
        if (dateStart != null) {
            lines.add("Start: " + format.format(dateStart));
        }
        if (dateEnd != null) {
            lines.add("End: " + format.format(dateEnd));
        }
        if (venue != null && !venue.isEmpty()) {
            lines.add("Venue: " + venue);
        }
        if (note != null && !venue.isEmpty()) {
            lines.add("Note: " + note);
        }

        return String.join("\n", lines);
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

    @Override
    public boolean equals(Object other) {
        if (other instanceof EventData) {
            EventData castedOther = (EventData) other;
            boolean eq = true;
            if (title != null) {
                eq = eq && title.equals(castedOther.title);
            } else {
                eq = eq && (castedOther.title == null);
            }
            if (dateStart != null) {
                eq = eq && dateStart.equals(castedOther.dateStart);
            } else {
                eq = eq && (castedOther.dateStart == null);
            }
            if (dateEnd != null) {
                eq = eq && dateEnd.equals(castedOther.dateEnd);
            } else {
                eq = eq && (castedOther.dateEnd == null);
            }
            if (venue != null) {
                eq = eq && venue.equals(castedOther.venue);
            } else {
                eq = eq && (castedOther.venue == null);
            }
            if (note != null) {
                eq = eq && note.equals(castedOther.note);
            } else {
                eq = eq && (castedOther.note == null);
            }
            return eq;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int sum = 0;
        if (title != null) {
            sum += title.hashCode();
        }
        if (dateStart != null) {
            sum += dateStart.hashCode();
        }
        if (dateEnd != null) {
            sum += dateEnd.hashCode();
        }
        if (venue != null) {
            sum += venue.hashCode();
        }
        if (note != null) {
            sum += note.hashCode();
        }
        return sum;
    }
}
