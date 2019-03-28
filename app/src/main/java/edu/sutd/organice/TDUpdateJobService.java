package edu.sutd.organice;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


public class TDUpdateJobService extends JobService {
    private static final String LOG_TAG = "TDUpdateJobService";

    TDHelper tdHelper;

    private Handler tdHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            return true;
        }
    });

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(LOG_TAG, "started TD update job");
        tdHelper = new TDHelper(this, tdHandler);
        return true;    // return true to continue running
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        tdHelper.close();
        Log.i(LOG_TAG, "stopped TD update job");
        return false;   // false means no retry requested
    }

}
