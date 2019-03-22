package edu.sutd.organice;

import java.util.Date;

public class NewEventRequest extends ActionRequest {
    final long chatId;
    final EventData eventData;

    NewEventRequest(long chatId, EventData eventData) {
        this.chatId = chatId;
        this.eventData = eventData;
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
    public void execute(CalendarHelper calendarHelper, TDHelper tdHelper) throws IllegalStateException {
        // check event data validity before adding event
        if (eventData.title == null) {
            throw new IllegalStateException("eventData.title for new event must not be null");
        } else if (eventData.dateStart == null) {
            throw new IllegalStateException("eventData.dateStart for new event must not be null");
        } else if (eventData.dateEnd == null) {
            throw new IllegalStateException("eventData.dateStart for new event must not be null");
        } else {
            calendarHelper.addEvent(this);
        }
    }

    @Override
    public String toString() {
        return "NewEventRequest(" +
                eventData.toString() +
                ")";
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof NewEventRequest) {
            NewEventRequest castedOther = (NewEventRequest) other;
            return chatId == castedOther.chatId &&
                    eventData.equals(castedOther.eventData);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (int) chatId + eventData.hashCode();
    }
}