package edu.sutd.organice;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.CalendarContract;
import android.util.Log;

/**
 * A SyncAdapter which sole purpose is to ensure that a calendar
 * exists for use by this app.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String LOG_TAG = "SyncAdapter";

    ContentResolver contentResolver;
    private Resources resources;

    // projection for using the calendar table in Android's calendar provider
    private static final String[] CALENDAR_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.NAME,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.OWNER_ACCOUNT
    };
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int PROJECTION_CALENDAR_NAME_INDEX = 2;
    private static final int PROJECTION_CALENDAR_DISPLAY_NAME_INDEX = 3;
    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 4;

    public SyncAdapter(Context context, boolean autoInitialize){
        super(context, autoInitialize);
        contentResolver = context.getContentResolver();
        resources = context.getResources();
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs){
        super(context, autoInitialize, allowParallelSyncs);
        contentResolver = context.getContentResolver();
        resources = context.getResources();
    }

    public void onPerformSync(
            Account acc,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult result
    ) {
        result.clear();

        // construct query
        Uri uri = CalendarContract.Calendars.CONTENT_URI.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, resources.getString(R.string.app_name))
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, resources.getString(R.string.account_type))
                .build();
        String selection = "((" + CalendarContract.Calendars.NAME + " = ?) AND ("
                + CalendarContract.Calendars.VISIBLE + " = ?) AND ("
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?))";
        String[] selectionArgs = new String[]{resources.getString(R.string.app_name), "1", resources.getString(R.string.account_type)};

        // query the calendar table
        Cursor cur = null;
        try {
            cur = provider.query(uri, CALENDAR_PROJECTION, selection, selectionArgs, null);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "remote exception in content provider client when querying calendar table");
            e.printStackTrace();
            return;
        }

        if (cur == null) {
            Log.e(LOG_TAG, "calendar table query failed");
            return;
        }

        switch (cur.getCount()) {
            case 0:
                // calendar not created, create one
                ContentValues values = new ContentValues();
                values.put(CalendarContract.Calendars.ACCOUNT_NAME, resources.getString(R.string.app_name));
                values.put(CalendarContract.Calendars.ACCOUNT_TYPE, resources.getString(R.string.account_type));
                values.put(CalendarContract.Calendars.NAME, resources.getString(R.string.app_name));
                values.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, resources.getString(R.string.app_name));
                values.put(CalendarContract.Calendars.CALENDAR_COLOR, getArbitraryColor());
                values.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
                values.put(CalendarContract.Calendars.OWNER_ACCOUNT, resources.getString(R.string.app_name));
                values.put(CalendarContract.Calendars.SYNC_EVENTS, 0);
                values.put(CalendarContract.Calendars.VISIBLE, 1);
                try {
                    Uri organiceCalendarUri = provider.insert(uri, values);
                } catch(RemoteException e) {
                    Log.e(LOG_TAG, "remote exception in content provider client when inserting calendar");
                    return;
                }
                Log.i(LOG_TAG, "created Organice calendar");
                break;
            case 1:
                // calendar already created, do nothing
                Log.d(LOG_TAG, "found one Organice calendar");
                break;
            default:
                // multiple calendars found, warn
                Log.w(LOG_TAG, "found multiple calendars named 'organice'");
        }
        provider.close();
    }

    private int getArbitraryColor() {
        Uri uri = CalendarContract.Colors.CONTENT_URI;
        Cursor cur = contentResolver.query(uri, new String[]{CalendarContract.Colors._ID}, null, null, null);
        cur.moveToFirst();
        int color = cur.getInt(0);
        cur.close();
        return color;
    }

    /**
     * Request an immediate sync using the given context.
     * @param context A {@link Context Context} with which the account to be synced can be obtained.
     */
    public static void syncNow(Context context) {
        Resources resources = context.getResources();

        AccountManager accountManager = AccountManager.get(context);
        Account account = accountManager.getAccountsByType(resources.getString(R.string.account_type))[0];

        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(account, CalendarContract.AUTHORITY, settingsBundle);
    }
}
