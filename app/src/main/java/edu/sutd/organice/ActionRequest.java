package edu.sutd.organice;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class ActionRequest {

    private static final String LOG_TAG = "ActionRequest";

    public static ActionRequest parseMessage(long chatId, String message) throws ParseException {
        Log.d("MyParser", "Called parser.");
        String title = null;
        Date ds = null;// date start
        Date de = null;// date end
        String venue = null;
        String note = null;

        String[] lines = message.split("\n");
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

        NewEventRequest r = new NewEventRequest(chatId, title, ds, de, venue, note);
        Log.d("MyParser", r.toString());
        return r;
    }

    public static void execute(CalendarHelper calendarHelper, ActionRequest request) {
        if (request instanceof NewEventRequest) {
            calendarHelper.addEvent((NewEventRequest) request);
        } else {
            Log.wtf(LOG_TAG, "unexpected request type: " + request.getClass().toString());
        }
    }

    public static void execute(CalendarHelper calendarHelper, long chatId, String message) throws ParseException {
        execute(calendarHelper, parseMessage(chatId, message));
    }

}
