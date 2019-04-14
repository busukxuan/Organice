package edu.sutd.organice;

import android.content.SharedPreferences;

/**
 * This class represents a request for the app to delete an event from its calendar,
 * parsed from input in the form of Telegram messages.
 */
public class DeleteEventRequest extends ActionRequest {

    public final long chatId;
    public final EventData eventData;

    /**
     * Construct the {@link DeleteEventRequest} object with the given chat ID and event data.
     * @param chatId Chat ID of the chat from which the request was received.
     * @param eventData Information that represent the event to be deleted.
     */
    DeleteEventRequest(long chatId, EventData eventData){
        this.chatId = chatId;
        this.eventData = eventData;
    }

    /**
     * Delete the event specified by this object.
     * @param preferences User preferences to inform the execution of this request.
     * @param calendarHelper Helper object for accessing the phone calendar.
     * @param tdHelper Helper object for sending messages, used by {@link ListEventsRequest} objects.
     * @throws IllegalStateException The {@link EventData} object provided does not contain a title.
     */
    @Override
    public void execute(SharedPreferences preferences, CalendarHelper calendarHelper, TDHelper tdHelper) throws IllegalStateException {
        // check eventData fields for validity before setting eventData
        if (eventData.title == null) {
            throw new IllegalStateException("eventData.title for event deletion must not be null");
        }
        // execute according to user preference
        switch (preferences.getString("delete_event_message_action", "execute")) {
            case "execute":
                calendarHelper.deleteEvent(this);
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
        return "DeleteEventRequest(" +
                eventData.toString() +
                ")";
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof DeleteEventRequest) {
            DeleteEventRequest castedOther = (DeleteEventRequest) other;
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
