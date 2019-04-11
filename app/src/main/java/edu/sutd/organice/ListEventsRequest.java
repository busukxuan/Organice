package edu.sutd.organice;

import android.content.SharedPreferences;

public class ListEventsRequest extends ActionRequest {

    public final long chatId;

    ListEventsRequest(Long chatID) {
        this.chatId = chatID;
    }

    @Override
    public void execute(SharedPreferences sharedPreferences, CalendarHelper calendarHelper, TDHelper tdHelper) {
        EventData[] eventData = calendarHelper.getNextEvents();
        String[] eventLines = new String[5];
        for (int i = 0; i < 5; i++) {
            eventLines[i] = Integer.toString(i+1) + ". " + eventData[i].toTextLine();
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
