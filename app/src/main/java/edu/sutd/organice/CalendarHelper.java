package edu.sutd.organice;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import java.util.TimeZone;

public class CalendarHelper {

    private static String LOG_TAG = "CalendarHelper";

    private Context context;
    private long calendarID;

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
}
