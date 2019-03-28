package edu.sutd.organice;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.drinkless.td.libcore.telegram.TdApi;

import java.text.ParseException;


public class TDUpdateJobService extends JobService {
    private static final String LOG_TAG = "TDUpdateJobService";

    CalendarHelper calendarHelper;
    TDHelper tdHelper;

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
                                ActionRequest.execute(calendarHelper, tdHelper, updateNewMessage.message);
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
        Log.i(LOG_TAG, "started TD update job");
        calendarHelper = new CalendarHelper(this);
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
