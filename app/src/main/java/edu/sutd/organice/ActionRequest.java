package edu.sutd.organice;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class ActionRequest {

    public static ActionRequest parseMessage(String message) throws ParseException {
        Log.d("MyParser", "Called parser.");
        String title = null;
        Date ds = null;
        Date de = null;
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

        NewEventRequest r = new NewEventRequest(title, ds, de, venue, note);
        Log.d("MyParser", r.toString());
        return r;
    }

    public static void main(String[] args) throws ParseException {
        System.out.println(parseMessage("bla #organice new Title:Meeting Start:27/02/2019 End:28/02/2019 Venue:ISH Note:meet #end organice bye").toString());
    }
}
