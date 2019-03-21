package edu.sutd.organice;

import android.util.Log;

import org.drinkless.td.libcore.telegram.TdApi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class ActionRequest {

    private static final String LOG_TAG = "ActionRequest";

    public static ActionRequest parseMessage(TdApi.Message message) throws ParseException {
        String text = ((TdApi.MessageText) message.content).text.text;
        Log.d("MyParser", "Called parser.");
        String title = null;
        Date ds = null;// date start
        Date de = null;// date end
        String venue = null;
        String note = null;

        String[] lines = text.split("\n");
        Log.d("MyParser", "n_ines: " + Integer.toString(lines.length));
        int lineIndex = 0;
        for (;lineIndex < lines.length && !lines[lineIndex].equals("# organice new"); lineIndex++) {
        }
        lineIndex++;
        Log.d("MyParser", "Start line: " + Integer.toString(lineIndex));
        while(lineIndex < lines.length && !lines[lineIndex].equals("# end organice")) {
            String line = lines[lineIndex];
            int colonIndex = line.indexOf(":");
            if (colonIndex < 0) {
                throw new ParseException("", 0);
            }
            Log.d("MyParser", "Found colon.");
            String field = line.substring(0, colonIndex);
            String s = line.substring(colonIndex+1);
            Log.d("MyParser", "Field: " + field);
            Log.d("MyParser", "Data: " + s);
            if (field.equals("Title")) {
                title = s;
            } else if (field.equals("Start")) {
                ds = new SimpleDateFormat("dd/MM/yyyy").parse(s);
            } else if (field.equals("End")) {
                de = new SimpleDateFormat("dd/MM/yyyy").parse(s);
            } else if (field.equals("Venue")) {
                venue = s;
            } else if (field.equals("Note")) {
                note = s;
            } else {
                throw new ParseException("", 0);
            }
            Log.d("MyParser", "Completed iteration.");
            lineIndex++;
        }
        Log.d("MyParser", "Completed loop.");

        EventData eventData = new EventData(title, ds, de, venue, note);
        Log.d("MyParser", eventData.toString());
        return new NewEventRequest(message.chatId, eventData);
    }

    public static void execute(CalendarHelper calendarHelper, ActionRequest request) {
        if (request instanceof NewEventRequest) {
            calendarHelper.addEvent((NewEventRequest) request);
        } else {
            Log.wtf(LOG_TAG, "unexpected request type: " + request.getClass().toString());
        }
    }

    public static void execute(CalendarHelper calendarHelper, TdApi.Message message) throws ParseException {
        execute(calendarHelper, parseMessage(message));
    }
}
