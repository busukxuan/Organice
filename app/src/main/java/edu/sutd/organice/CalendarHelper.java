package edu.sutd.organice;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class CalendarHelper {

    private static String LOG_TAG = "CalendarHelper";

    private Context context;
    private long calendarID;


    private static String[] EVENT_PROJECTION = new String[]{
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events.DESCRIPTION
    };
    private static final int PROJECTION_EVENT_ID_INDEX = 0;
    private static final int PROJECTION_EVENT_TITLE_INDEX = 0;
    private static final int PROJECTION_EVENT_DTSTART_INDEX = 0;
    private static final int PROJECTION_EVENT_DTEND_INDEX = 0;
    private static final int PROJECTION_EVENT_LOCATION_INDEX = 0;
    private static final int PROJECTION_EVENT_DESCRIPTION_INDEX = 0;

    CalendarHelper(Context c) {
        context = c;
        initializeCalendarID();
    }

    private void initializeCalendarID() {
        // get calendar ID
        Log.d(LOG_TAG, "getting calendar ID");
        Cursor cur;
        ContentResolver cr = context.getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String selection = "((" + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?))";
        String[] selectionArgs = new String[]{
//                CalendarContract.ACCOUNT_TYPE_LOCAL
                "com.google"
        };
        String[] calendarProjection = new String[]{
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.ACCOUNT_TYPE
        };
        try {
            cur = cr.query(uri, calendarProjection, selection, selectionArgs, null);
        } catch (SecurityException e) {
            Log.e(LOG_TAG, "unable to obtain calendar ID: permission denied");
            return;
        }

        if (cur == null) {
            Log.e(LOG_TAG, "unable to obtain calendar ID: no local calendar found (null)");
            return;
        }
        int count = cur.getCount();
        if (count < 1) {
            Log.e(LOG_TAG, "unable to obtain calendar ID: no local calendar found");
        } else {
            if (count > 1) {
                Log.i(LOG_TAG, "found multiple calendars, arbitrarily using one");
            }
            cur.moveToNext();
            calendarID = cur.getLong(0);
        }
        cur.close();
    }

    public void addEvent(NewEventRequest request) {
        Log.d(LOG_TAG, "adding new event");
        ContentResolver cr = context.getContentResolver();

        // create event value
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.CALENDAR_ID, calendarID);
        values.put(CalendarContract.Events.TITLE, request.eventData.title);
        values.put(CalendarContract.Events.DTSTART, request.eventData.dateStart.getTime());
        values.put(CalendarContract.Events.DTEND, request.eventData.dateEnd.getTime());
        values.put(CalendarContract.Events.EVENT_LOCATION, request.eventData.venue);
        values.put(CalendarContract.Events.DESCRIPTION, request.eventData.note);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

        try {
            cr.insert(CalendarContract.Events.CONTENT_URI, values);
            Log.i(LOG_TAG, "successfully added new event");
        } catch (SecurityException e) {
            Log.e(LOG_TAG, "failed to create new event: permission denied");
        } catch (Exception e) {
            throw e;
        }
    }

    public void deleteEvent(DeleteEventRequest request) {
        Log.d(LOG_TAG, "deleting event");

        EventData eventData = request.eventData;

        ContentResolver cr = context.getContentResolver();

        Uri uri = CalendarContract.Events.CONTENT_URI;

        // create selection based on available information from request
        List<String> selectionComponents = new ArrayList<String>(5);
        List<String> selectionArgs = new ArrayList<>(5);
        if (eventData.title != null) {
            selectionComponents.add("(" + CalendarContract.Events.TITLE + "= ?)");
            selectionArgs.add(eventData.title);
        }
        if (eventData.dateStart != null) {
            selectionComponents.add("(" + CalendarContract.Events.DTSTART + "= ?)");
            selectionArgs.add(
                    Long.toString(eventData.dateStart.getTime())
            );
        }
        if (eventData.dateEnd != null) {
            selectionComponents.add("(" + CalendarContract.Events.DTEND + "= ?)");
            selectionArgs.add(
                    Long.toString(eventData.dateEnd.getTime())
            );
        }
        if (eventData.venue != null) {
            selectionComponents.add("(" + CalendarContract.Events.EVENT_LOCATION + "= ?)");
            selectionArgs.add(eventData.venue);
        }
        if (eventData.note != null) {
            selectionComponents.add("(" + CalendarContract.Events.DESCRIPTION + "= ?)");
            selectionArgs.add(eventData.note);
        }

        // get number of matching events
        Cursor cur = cr.query(
                uri,
                new String[]{},
                "(" + String.join(" AND ", selectionComponents) + ")",
                selectionArgs.toArray(new String[0]),
                null
        );
        int matches = cur.getCount();
        cur.close();
        switch (matches) {
            case 1:
                // one matching event, delete
                cur.close();
                cr.delete(
                        uri,
                        "(" + String.join(" AND ", selectionComponents) + ")",
                        selectionArgs.toArray(new String[0])
                );
                Log.i(LOG_TAG, "successfully deleted event");
                break;
            case 0:
                // no matching event, don't delete
                Log.i(LOG_TAG, "no event matches delete request");
                break;
            default:
                // multiple matching events, don't delete
                Log.i(LOG_TAG, "multiple events match delete request, deletion aborted");
        }
    }
}
