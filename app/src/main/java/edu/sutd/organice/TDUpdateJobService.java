package edu.sutd.organice;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import org.drinkless.td.libcore.telegram.TdApi;

import java.text.ParseException;


public class TDUpdateJobService extends JobService {
    private static final String LOG_TAG = "TDUpdateJobService";

    private static final int IMMEDIATE_JOB_ID = 0;
    private static final int PERIODIC_JOB_ID = 1;

    private static final int IMMEDIATE_JOB_DEADLINE = 500;

    CalendarHelper calendarHelper;
    TDHelper tdHelper;

    private SharedPreferences preferences;
    public static final String HONOR_STOP_JOB_PREF = "honor_stop_job";

    private Handler tdHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Intent intent;
            switch (msg.what) {

                case TDHelper.PHONE_NUMBER_REQUEST_MESSAGE_CODE:
                    // do nothing for now
                    break;

                case TDHelper.LOGIN_CODE_REQUEST_MESSAGE_CODE:
                    // do nothing
                    break;

                case TDHelper.UPDATE_MESSAGE_CODE:
                    if (((TdApi.Object) msg.obj).getConstructor() == TdApi.UpdateNewMessage.CONSTRUCTOR) {
                        Log.i(LOG_TAG, "received new message");
                        TdApi.UpdateNewMessage updateNewMessage = (TdApi.UpdateNewMessage) msg.obj;
                        TdApi.MessageContent content = updateNewMessage.message.content;
                        if (content instanceof TdApi.MessageText) {
                            try {
                                ActionRequest.execute(preferences, calendarHelper, tdHelper, updateNewMessage.message);
                            } catch (ParseException e) {
                                Log.e(LOG_TAG, "parse error");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;

                case TDHelper.RESULT_MESSAGE_CODE:
                    break;

                default:
                    Log.wtf(LOG_TAG, "unexpected message code " + Integer.toString(msg.what));
            }
            return true;
        }
    });

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(LOG_TAG, "started TDLib update job");
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        calendarHelper = new CalendarHelper(this);
        tdHelper = TDHelper.getInstance(this);
        tdHelper.setUpdateServiceHandler(tdHandler);
        return true;    // return true to continue running
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (preferences.getBoolean(HONOR_STOP_JOB_PREF, false)) {
            tdHelper.close();
        }
        Log.i(LOG_TAG, "stopped TDLib update job");
        return false;   // false means no retry is requested
    }

    public static void scheduleImmediateJob(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler == null) {
            Log.wtf(LOG_TAG, "failed to obtain job scheduler when scheduling immediate TDLib update job");
            return;
        }
        JobInfo.Builder builder = new JobInfo.Builder(
                IMMEDIATE_JOB_ID,
                new ComponentName(context.getApplicationContext(), TDUpdateJobService.class)
        )
                .setOverrideDeadline(IMMEDIATE_JOB_DEADLINE);
        JobInfo job = builder.build();
        jobScheduler.schedule(job);
    }

}
