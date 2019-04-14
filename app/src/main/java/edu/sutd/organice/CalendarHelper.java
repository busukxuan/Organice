package edu.sutd.organice;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * This is a helper class for accessing Android's calendar provider.
 */
public class CalendarHelper {

    private static String LOG_TAG = "CalendarHelper";

    private Context context;
    private long calendarID;
    private Resources resources;


    private static String[] EVENT_PROJECTION = new String[]{
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events.DESCRIPTION
    };
    private static final int PROJECTION_EVENT_ID_INDEX = 0;
    private static final int PROJECTION_EVENT_TITLE_INDEX = 1;
    private static final int PROJECTION_EVENT_DTSTART_INDEX = 2;
    private static final int PROJECTION_EVENT_DTEND_INDEX = 3;
    private static final int PROJECTION_EVENT_LOCATION_INDEX = 4;
    private static final int PROJECTION_EVENT_DESCRIPTION_INDEX = 5;

    /**
     * Construct a @{link CalendarHelper} object using the given context.
     * @param c The context used to construct the object.
     */
    CalendarHelper(Context c) {
        context = c;
        resources = c.getResources();
        initializeCalendarID();
    }

    /**
     * Set the @{link calendarID} field to the ID of the app's calendar in the calendar table of
     * Android's calendar provider. This is called as part of the constructor.
     */
    private void initializeCalendarID() {
        Log.d(LOG_TAG, "getting calendar ID");
        Cursor cur;
        ContentResolver cr = context.getContentResolver();
        // construct query
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String selection = "((" + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + CalendarContract.Calendars.ACCOUNT_NAME + " = ?))";
        String[] selectionArgs = new String[]{
                resources.getString(R.string.account_type),
                resources.getString(R.string.app_name)
        };
        String[] calendarProjection = new String[]{
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.NAME,
                CalendarContract.Calendars.ACCOUNT_TYPE
        };
        final int PROJECTION_CALENDAR_ID_INDEX = 0;
        final int PROJECTION_CALENDAR_NAME_INDEX = 1;
        final int PROJECTION_CALENDAR_ACCOUNT_TYPE_INDEX = 2;
        // query calendar provider
        try {
            cur = cr.query(uri, calendarProjection, selection, selectionArgs, null);
        } catch (SecurityException e) {
            Log.e(LOG_TAG, "unable to obtain calendar ID: permission denied");
            return;
        }

        if (cur == null) {
            Log.e(LOG_TAG, "unable to obtain calendar ID: query failed");
            return;
        }
        // read cursor and obtain calendar ID
        int count = cur.getCount();
        if (count == 0) {
            Log.e(LOG_TAG, "unable to obtain calendar ID: no local calendar found");
        } else {
            cur.moveToFirst();
            String name = cur.getString(PROJECTION_CALENDAR_NAME_INDEX);
            if (count > 1) {
                Log.w(LOG_TAG, "found multiple calendars, arbitrarily using \"" + name + "\"");
            } else {
                Log.d(LOG_TAG, "found calendar \"" + name + "\"");
            }
            calendarID = cur.getLong(PROJECTION_CALENDAR_ID_INDEX);
        }
        cur.close();
    }

    /**
     * Add a new event to the app's calendar.
     * @param request A {@link NewEventRequest} object representing the information of the event to be added.
     * @return The content URI that identifies the event that is added to the calendar, or null if operation failed.
     */
    public Uri addEvent(NewEventRequest request) {
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

        // insert value into the event table
        Uri uri = null;
        try {
            uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
            if (uri != null) {
                Log.i(LOG_TAG, "successfully added new event");
            } else {
                Log.e(LOG_TAG, "failed to create new event");
            }
        } catch (SecurityException e) {
            Log.e(LOG_TAG, "failed to create new event: permission denied");
        } catch (Exception e) {
            throw e;
        }
        return uri;
    }

    /**
     * Delete an event from the app's calendar.
     * @param request A {@link DeleteEventRequest} object representing the information of the event to be deleted.
     */
    public void deleteEvent(DeleteEventRequest request) {
        Log.d(LOG_TAG, "deleting event");

        EventData eventData = request.eventData;

        ContentResolver cr = context.getContentResolver();

        Uri uri = CalendarContract.Events.CONTENT_URI;

        // create selection based on available information from request
        List<String> selectionComponents = new ArrayList<>(6);
        List<String> selectionArgs = new ArrayList<>(6);
        selectionComponents.add("(" + CalendarContract.Events.CALENDAR_ID + " = ?)");
        selectionArgs.add(Long.toString(calendarID));
        if (eventData.title != null) {
            selectionComponents.add("(" + CalendarContract.Events.TITLE + " = ?)");
            selectionArgs.add(eventData.title);
        }
        if (eventData.dateStart != null) {
            selectionComponents.add("(" + CalendarContract.Events.DTSTART + " = ?)");
            selectionArgs.add(
                    Long.toString(eventData.dateStart.getTime())
            );
        }
        if (eventData.dateEnd != null) {
            selectionComponents.add("(" + CalendarContract.Events.DTEND + " = ?)");
            selectionArgs.add(
                    Long.toString(eventData.dateEnd.getTime())
            );
        }
        if (eventData.venue != null) {
            selectionComponents.add("(" + CalendarContract.Events.EVENT_LOCATION + " = ?)");
            selectionArgs.add(eventData.venue);
        }
        if (eventData.note != null) {
            selectionComponents.add("(" + CalendarContract.Events.DESCRIPTION + " = ?)");
            selectionArgs.add(eventData.note);
        }

        String selection = "(" + String.join(" AND ", selectionComponents) + ")";
        String selectionArgsPrint = String.join(", ", selectionArgs);

        Log.v(LOG_TAG, selection + " " + selectionArgsPrint);
        // get number of matching events
        Cursor cur = cr.query(
                uri,
                new String[]{},
                selection,
                selectionArgs.toArray(new String[0]),
                null
        );
        int matches = cur.getCount();
        cur.close();
        switch (matches) {
            case 1:
                // one matching event, delete
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

    /**
     * Get a list of events matching the given criteria.
     * @param titleSubstr Titles of events returned must contain this substring.
     * @param from Events returned must end at or after this time.
     * @param to Events returned must start at or before this time.
     * @param venueSubstr Venues of events returned must contain this substring.
     * @param noteSubstr Notes of events returned must contain this substring.
     * @param organiceOnly Whether only events from this app's calendar should be returned.
     * @return A {@link List} of {@link EventData} objects representing all matching events found.
     */
    public List<EventData> getEvents(
            String titleSubstr,
            Date from,
            Date to,
            String venueSubstr,
            String noteSubstr,
            boolean organiceOnly
    ) {
        Log.d(LOG_TAG, "retrieving events from calendar");

        ContentResolver cr = context.getContentResolver();

        Uri uri = CalendarContract.Events.CONTENT_URI;

        // create selection based on available information from request
        List<String> selectionComponents = new ArrayList<>(6);
        List<String> selectionArgs = new ArrayList<>(6);
        if (organiceOnly) {
            selectionComponents.add("(" + CalendarContract.Events.CALENDAR_ID + " = ?)");
            selectionArgs.add(Long.toString(calendarID));
        }
        if (titleSubstr != null && !titleSubstr.isEmpty()) {
            selectionComponents.add("(" + CalendarContract.Events.TITLE + " LIKE ?)");
            selectionArgs.add("%" + titleSubstr + "%");
        }
        if (from != null) {
            selectionComponents.add("(" + CalendarContract.Events.DTEND + " >= ?)");
            selectionArgs.add(
                    Long.toString(from.getTime())
            );
        }
        if (to != null) {
            selectionComponents.add("(" + CalendarContract.Events.DTSTART + " <= ?)");
            selectionArgs.add(
                    Long.toString(to.getTime())
            );
        }
        if (venueSubstr != null && !venueSubstr.isEmpty()) {
            selectionComponents.add("(" + CalendarContract.Events.EVENT_LOCATION + " LIKE ?)");
            selectionArgs.add("%" + venueSubstr + "%");
        }
        if (noteSubstr != null && !noteSubstr.isEmpty()) {
            selectionComponents.add("(" + CalendarContract.Events.DESCRIPTION + " LIKE ?)");
            selectionArgs.add("%" + noteSubstr + "%");
        }

        // query
        Cursor cur = cr.query(
                uri,
                EVENT_PROJECTION,
                "(" + String.join(" AND ", selectionComponents) + ")",
                selectionArgs.toArray(new String[0]),
                null
        );

        // get all the rows as EventData
        List<EventData> eventData = new ArrayList<>();
        if (cur == null) {
            Log.d(LOG_TAG, "got null cursor");
            return eventData;
        }
        if (cur.getCount() > 0) {
            for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                eventData.add(new EventData(
                        cur.getString(PROJECTION_EVENT_TITLE_INDEX),
                        new Date(cur.getLong(PROJECTION_EVENT_DTSTART_INDEX)),
                        new Date(cur.getLong(PROJECTION_EVENT_DTEND_INDEX)),
                        cur.getString(PROJECTION_EVENT_LOCATION_INDEX),
                        cur.getString(PROJECTION_EVENT_DESCRIPTION_INDEX)
                ));
            }
        }
        cur.close();
        return eventData;
    }

    /**
     * Get the next 5 events (sorted by start time) from the phone calendar.
     * @return An array of {@link EventData} representing the next 5 events.
     */
    public EventData[] getNextEvents() {
        ContentResolver cr = context.getContentResolver();

        Uri uri = CalendarContract.Events.CONTENT_URI;

        String selection = "((" + CalendarContract.Events.DTEND + " > ?))";
        String[] selectionArgs = new String[] { Long.toString(Calendar.getInstance().getTime().getTime()) };

        // get number of matching events
        Cursor cur = cr.query(
                uri,
                EVENT_PROJECTION,
                selection,
                selectionArgs,
                CalendarContract.Events.DTSTART + " ASC"
        );

        // get the 5 events
        cur.moveToFirst();

        EventData[] eventData = new EventData[5];

        for (int i = 0; i < 5; i++) {
            String title = cur.getString(PROJECTION_EVENT_TITLE_INDEX);
            Date start = new Date(cur.getLong(PROJECTION_EVENT_DTSTART_INDEX));
            Date end = new Date(cur.getLong(PROJECTION_EVENT_DTEND_INDEX));
            String venue = cur.getString(PROJECTION_EVENT_LOCATION_INDEX);
            String note = cur.getString(PROJECTION_EVENT_DESCRIPTION_INDEX);
            eventData[i] = new EventData(title, start, end, venue, note);
            cur.moveToNext();
        }

        return eventData;
    }
}
