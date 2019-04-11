package edu.sutd.organice;

import android.content.SharedPreferences;
import android.util.Log;

import org.drinkless.td.libcore.telegram.TdApi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class ActionRequest {

    private static final String LOG_TAG = "ActionRequest";

    public static ActionRequest parseMessage(TdApi.Message message) throws ParseException {

        Log.d(LOG_TAG, "parsing message");
        String text = ((TdApi.MessageText) message.content).text.text;

        // check each line for command tag
        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i++) {

            if (lines[i].equals("# organice new")) {
                EventData eventData = parseEventData(lines, i + 1);
                return new NewEventRequest(message.chatId, eventData);

            } else if (lines[i].equals("# organice delete")) {
                EventData eventData = parseEventData(lines, i + 1);
                return new DeleteEventRequest(message.chatId, eventData);

            } else if (lines[i].equals("# organice list")) {
                return new ListEventsRequest(message.chatId);
            }
        }
        return null;
    }

    private static EventData parseEventData(String[] lines, int startLine) throws ParseException {
        // `EventData` fields are null by default
        String title = "";
        Date start = null;
        Date end = null;
        String venue = "";
        String note = "";

        // read data fields line by line, until closing tag "# end organice"
        int lineIndex = startLine;
        while(lineIndex < lines.length && !lines[lineIndex].equals("# end organice")) {
            String line = lines[lineIndex];
            // everything before the first colon is the field name
            int colonIndex = line.indexOf(":");
            if (colonIndex < 0) {
                throw new ParseException("no colon in line", -1);
            }
            String field = line.substring(0, colonIndex).trim();
            String s = line.substring(colonIndex+1).trim();
            switch (field) {
                case "Title":
                    title = s;
                    break;
                case "Start":
                    start = new SimpleDateFormat("dd/MM/yyyy").parse(s);
                    break;
                case "End":
                    end = new SimpleDateFormat("dd/MM/yyyy").parse(s);
                    break;
                case "Venue":
                    venue = s;
                    break;
                case "Note":
                    note = s;
                    break;
                default:
                    // invalid line, throw exception
                    throw new ParseException("unexpected field name: \"" + field + "\"", -1);
            }
            lineIndex++;
        }
        return new EventData(title, start, end, venue, note);
    }

    public abstract void execute(SharedPreferences preferences, CalendarHelper calendarHelper, TDHelper tdHelper);

    public static void execute(SharedPreferences preferences, CalendarHelper calendarHelper, TDHelper tdHelper, ActionRequest request) {
        Log.d(LOG_TAG, "executing request");
        request.execute(preferences, calendarHelper, tdHelper);
    }

    public static void execute(SharedPreferences preferences, CalendarHelper calendarHelper, TDHelper tdHelper, TdApi.Message message) throws ParseException {
        ActionRequest request = parseMessage(message);
        if (request != null) {
            execute(preferences, calendarHelper, tdHelper, request);
        }
    }

    @Override
    public abstract boolean equals(Object other);

    public abstract int hashCode();
}
