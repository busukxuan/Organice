package edu.sutd.organice;

import java.util.Date;

public class NewEventRequest extends ActionRequest {
    private String title;
    private Date dateStart, dateEnd;
    private String venue;
    private String note;

    NewEventRequest(String title, Date dateStart, Date dateEnd, String venue, String note) {
        this.title = title;
        this.dateStart = dateStart;
        if (dateEnd != null) {
            this.dateEnd = dateEnd;
        }
        if (venue != null) {
            this.venue = venue;
        }
        if (note != null) {
            this.note = note;
        }
    }

    public String getTitle() {
        return title;
    }

    public Date getDateStart() {
        return dateStart;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public String getVenue() {
        return venue;
    }

    public String getNote() {
        return note;
    }

    @Override
    public String toString() {
        return "NewEventRequest(\"" + title + "\", " + dateStart.toString() + ", " + dateEnd.toString() + ", \"" + venue + "\", \"" + note + "\")";
    }
}