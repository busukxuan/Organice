package edu.sutd.organice;

import android.content.SharedPreferences;

import java.util.Date;

/**
 * This class represents a request for the app to add a new event to its calendar,
 * parsed from input in the form of Telegram messages.
 */
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

    /**
     * Add a new event as specified by this object.
     * @param preferences User preferences to inform the execution of this request.
     * @param calendarHelper Helper object for accessing the phone calendar.
     * @param tdHelper Helper object for sending messages, used by {@link ListEventsRequest} objects.
     * @throws IllegalStateException The {@link EventData} object provided is missing a title, start time or end time.
     */
    @Override
    public void execute(SharedPreferences preferences, CalendarHelper calendarHelper, TDHelper tdHelper) throws IllegalStateException {
        // check event data validity before adding event
        if (eventData.title == null) {
            throw new IllegalStateException("eventData.title for new event must not be null");
        } else if (eventData.dateStart == null) {
            throw new IllegalStateException("eventData.dateStart for new event must not be null");
        } else if (eventData.dateEnd == null) {
            throw new IllegalStateException("eventData.dateStart for new event must not be null");
        }
        // execute according to user preference
        switch (preferences.getString("new_event_message_action", "execute")) {
            case "execute":
                calendarHelper.addEvent(this);
                break;
            case "prompt":
                // do nothing for now
                break;
            case "ignore":
                break;
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