package edu.sutd.organice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class SyncService extends Service {
    private static final String LOG_TAG = "SyncService";

    private static SyncAdapter syncAdapter = null;

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "started sync service");
        synchronized (SyncService.class) {
            if (syncAdapter == null) {
                syncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }
}
