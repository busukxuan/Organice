package edu.sutd.organice;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a request for the app to send a message to the requesting chat,
 * listing all upcoming events created by that chat. This request is parsed from input in the form
 * of a Telegram message.
 */
public class ListEventsRequest extends ActionRequest {

    public final long chatId;

    /**
     * Construct the {@link ListEventsRequest} with the given chat ID.
     * @param chatID The chat ID of the chat from which the request originated.
     */
    ListEventsRequest(Long chatID) {
        this.chatId = chatID;
    }

    /**
     * Send the requested message listing all upcoming events created by that chat.
     * @param preferences User preferences to inform the execution of this request.
     * @param calendarHelper Helper object for accessing the phone calendar.
     * @param tdHelper Helper object for sending messages, used by {@link ListEventsRequest} objects.
     */
    @Override
    public void execute(SharedPreferences preferences, CalendarHelper calendarHelper, TDHelper tdHelper) {
        List<EventData> eventData = calendarHelper.getNextEvents(true);
        List<String> eventLines = new ArrayList<>(5);
        for (int i = 0; i < eventData.size(); i++) {
            eventLines.add(Integer.toString(i+1) + ". " + eventData.get(i).toTextLine());
        }
        if (eventLines.isEmpty()) {
            eventLines.add("No upcoming events in Organice's calendar.");
        }
        tdHelper.sendMessage(chatId, String.join("\n", eventLines));
    }

    @Override
    public String toString(){
        return "NewEventRequest(" + Long.toString(chatId) + ")";
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ListEventsRequest) {
            return chatId == ((ListEventsRequest) other).chatId;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (int) chatId;
    }
}
