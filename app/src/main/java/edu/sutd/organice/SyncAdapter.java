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

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String LOG_TAG = "SyncAdapter";

    ContentResolver contentResolver;
    private Resources resources;

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

        Uri uri = CalendarContract.Calendars.CONTENT_URI.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, resources.getString(R.string.app_name))
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, resources.getString(R.string.account_type))
                .build();
        String selection = "((" + CalendarContract.Calendars.NAME + " = ?) AND (" + CalendarContract.Calendars.VISIBLE + " = ?))";
        String[] selectionArgs = new String[]{resources.getString(R.string.app_name), "1"};

        Cursor cur = null;
        try {
            cur = provider.query(uri, CALENDAR_PROJECTION, selection, selectionArgs, null);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "remote exception in content provider client when querying calendars");
            e.printStackTrace();
            return;
        }

        if (cur == null) {
            Log.e(LOG_TAG, "unexpected null cursor");
            return;
        }

        switch (cur.getCount()) {
            case 0:
                // calendar not created, create one
                ContentValues values = new ContentValues();
                values.put(CalendarContract.Calendars.NAME, resources.getString(R.string.app_name));
                values.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, resources.getString(R.string.app_name));
                values.put(CalendarContract.Calendars.ACCOUNT_NAME, resources.getString(R.string.app_name));
                values.put(CalendarContract.Calendars.ACCOUNT_TYPE, resources.getString(R.string.account_type));
                values.put(CalendarContract.Calendars.VISIBLE, 1);
                try {
                    Uri organiceCalendarUri = provider.insert(uri, values);
                } catch(RemoteException e) {
                    Log.e(LOG_TAG, "remote exception in content provider client when insering calendar");
                    return;
                }
                Log.i(LOG_TAG, "creating Organice calendar");
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

    public static void syncNow(Context context) {
        Resources resources = context.getResources();
        final String AUTHORITY = CalendarContract.AUTHORITY;

        AccountManager accountManager = AccountManager.get(context);
        Account account = accountManager.getAccountsByType(resources.getString(R.string.account_type))[0];

        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(account, AUTHORITY, settingsBundle);
    }
}
