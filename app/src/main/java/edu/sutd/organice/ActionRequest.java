package edu.sutd.organice;

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
        Log.d(LOG_TAG, "n_ines: " + Integer.toString(lines.length));
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
        String title = null;
        Date start = null;
        Date end = null;
        String venue = null;
        String note = null;

        // read data fields line by line, until closing tag "# end organice"
        int lineIndex = startLine;
        while(lineIndex < lines.length && !lines[lineIndex].equals("# end organice")) {
            String line = lines[lineIndex];
            // everything before the first colon is the field name
            int colonIndex = line.indexOf(":");
            if (colonIndex < 0) {
                throw new ParseException("", 0);
            }
            String field = line.substring(0, colonIndex);
            String s = line.substring(colonIndex+1);
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

    public static void execute(CalendarHelper calendarHelper, ActionRequest request) {
        if (request instanceof NewEventRequest) {
            calendarHelper.addEvent((NewEventRequest) request);
        } else if (request instanceof DeleteEventRequest) {
            calendarHelper.deleteEvent((DeleteEventRequest) request);
        } else if (request instanceof ListEventsRequest) {
            // do nothing for now
        } else {
            Log.wtf(LOG_TAG, "unexpected request type: " + request.getClass().toString());
        }
    }

    public static void execute(CalendarHelper calendarHelper, TdApi.Message message) throws ParseException {
        execute(calendarHelper, parseMessage(message));
    }
}
