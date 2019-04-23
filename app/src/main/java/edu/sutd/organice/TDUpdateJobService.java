package edu.sutd.organice;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import org.drinkless.td.libcore.telegram.TdApi;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * A {@link JobService JobService} for keeping the app's process alive when it is not in the
 * foreground, so that new messages can be received as soon as they are available.
 * <p>
 *     This {@link JobService JobService} seems to be able to run for up to 10 minutes at a time
 *     before Android stops it. A new instance would be started after another 10 minutes.
 * </p>
 * <p>
 *     This class also provides helper methods for starting the {@link JobService JobService}.
 * </p>
 */
public class TDUpdateJobService extends JobService {

    private static final String LOG_TAG = "TDUpdateJobService";

    // job IDs
    private static final int IMMEDIATE_JOB_ID = 0;
    private static final int PERIODIC_JOB_ID = 1;

    // maximum time before which an immediate job must be started by the system
    private static final int IMMEDIATE_JOB_DEADLINE = 500;

    private CalendarHelper calendarHelper;
    private TDHelper tdHelper;

    // preference-related variables
    private SharedPreferences preferences;
    private static final String HONOR_STOP_JOB_PREF = "honor_stop_job";

    // TDHelper handler for use with background updates
    private Handler tdHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {

                case TDHelper.PHONE_NUMBER_REQUEST_MESSAGE_CODE:
                    // do nothing for now
                    break;

                case TDHelper.LOGIN_CODE_REQUEST_MESSAGE_CODE:
                    // do nothing
                    break;

                case TDHelper.UPDATE_MESSAGE_CODE:
                    // got an update, check if it's a new message
                    if (((TdApi.Object) msg.obj).getConstructor() == TdApi.UpdateNewMessage.CONSTRUCTOR) {
                        // got a new message
                        Log.i(LOG_TAG, "received new message");
                        TdApi.UpdateNewMessage updateNewMessage = (TdApi.UpdateNewMessage) msg.obj;
                        TdApi.MessageContent content = updateNewMessage.message.content;
                        // try to execute the request in that message, if any
                        if (content instanceof TdApi.MessageText) {
                            try {
                                ActionRequest.execute(preferences, calendarHelper, tdHelper, updateNewMessage.message);
                            } catch (Exception e) {
                                if (updateNewMessage.message.isOutgoing) {
                                    tdHelper.sendMessage(
                                            updateNewMessage.message.chatId,
                                            "Organice error - " + e.getMessage()
                                    );
                                } else {
                                    StringWriter sw = new StringWriter();
                                    e.printStackTrace(new PrintWriter(sw));
                                    Log.d(LOG_TAG, sw.toString());
                                }
                            }
                        }
                    }
                    break;

                case TDHelper.RESULT_MESSAGE_CODE:
                    // do nothing
                    break;

                default:
                    Log.wtf(LOG_TAG, "unexpected message code " + Integer.toString(msg.what));
            }
            return true;
        }
    });

    @Override
    public boolean onStartJob(JobParameters params) {
        // initialize the job
        Log.i(LOG_TAG, "started TDLib update job");
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        calendarHelper = new CalendarHelper(this);
        tdHelper = TDHelper.getInstance(this);
        tdHelper.setUpdateServiceHandler(tdHandler);
        return true;    // return true to continue running
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        // conditionally close `tdHelper` according to user preference
        if (preferences.getBoolean(HONOR_STOP_JOB_PREF, false)) {
            tdHelper.close();
        }
        Log.i(LOG_TAG, "stopped TDLib update job");
        return false;   // false means no retry is requested
    }

    /**
     * Schedule a job that starts immediately (after a short deadline).
     * @param context A {@link Context Context} with which a {@link JobScheduler JobScheduler}
     *                object can be obtained.
     */
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
