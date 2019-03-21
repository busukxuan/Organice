package edu.sutd.organice;

import java.util.Date;

public class NewEventRequest extends ActionRequest {
    final long chatId;
    final EventData eventData;

    NewEventRequest(long chatId, EventData eventData) {
        this.chatId = chatId;

        // check eventData fields for validity before setting eventData
        if (eventData.title == null) {
            throw new IllegalArgumentException("eventData.title for new event must not be null");
        } else if (eventData.dateStart == null) {
            throw new IllegalArgumentException("eventData.dateStart for new event must not be null");
        } else if (eventData.dateEnd == null) {
            throw new IllegalArgumentException("eventData.dateStart for new event must not be null");
        } else {
            this.eventData = eventData;
        }
    }

    public long getChatID() {
        return chatId;
    }

    public String getTitle() {
        return eventData.title;
    }

    public Date getDateStart() {
        return eventData.dateStart;
    }

    public Date getDateEnd() {
        return eventData.dateEnd;
    }

    public String getVenue() {
        return eventData.venue;
    }

    public String getNote() {
        return eventData.note;
    }

    @Override
    public void execute(CalendarHelper calendarHelper, TDHelper tdHelper) {
        calendarHelper.addEvent(this);
    }

    @Override
    public String toString() {
        return "NewEventRequest(" +
                eventData.toString() +
                ")";
    }

}