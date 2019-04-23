package edu.sutd.organice;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.Locale;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TDHelper {
    private static final String LOG_TAG = "TDHelper";

    private static TDHelper instance = null;

    public static final int PHONE_NUMBER_REQUEST_MESSAGE_CODE = 0;
    public static final int LOGIN_CODE_REQUEST_MESSAGE_CODE = 1;
    public static final int UPDATE_MESSAGE_CODE = 2;
    public static final int RESULT_MESSAGE_CODE = 3;
    public static final int ERROR_MESSAGE_CODE = 4;

    private final Context context;
    private Handler updateServiceHandler;
    private Handler activityHandler;
    private Client client;
    private final Client.ResultHandler defaultHandler = new TDHelper.DefaultHandler();
    // authorization variables
    private final Lock authorizationLock = new ReentrantLock();
    private final Condition gotAuthorization = authorizationLock.newCondition();
    public volatile TdApi.AuthorizationState authorizationState = new TdApi.AuthorizationStateWaitTdlibParameters();

    // TDLib client parameters
    private static final boolean USE_MESSAGE_DATABASE = true;
    private static final boolean USE_SECRET_CHATS = false;
    private static final int API_ID = 611772;
    private static final String API_HASH = "6c33afaaf22568fb526bd770cfa31645";
    private static final boolean ENABLE_STORAGE_OPTIMIZER = true;

    static {
        System.loadLibrary("tdjni");
    }

    private TDHelper(Context context) {
        this.context = context;

        // create client
        client = Client.create(new TDHelper.UpdatesHandler(), null, null);
    }

    public static TDHelper getInstance(Context context) {
        if (instance == null) {
            instance = new TDHelper(context.getApplicationContext());
        }
        return instance;
    }

    public void setUpdateServiceHandler(Handler handler) {
        updateServiceHandler = handler;
    }

    public void setActivityHandler(Handler handler) {
        activityHandler = handler;
    }

    private class DefaultHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            Message message = new Message();
            message.what = RESULT_MESSAGE_CODE;
            message.obj = object;
            updateServiceHandler.sendMessage(message);
        }
    }

    private class UpdatesHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {

                case TdApi.UpdateAuthorizationState.CONSTRUCTOR:
                    onAuthorizationStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
                    break;

                default:
                    Message message = new Message();
                    message.what = UPDATE_MESSAGE_CODE;
                    message.obj = object;
                    updateServiceHandler.sendMessage(message);

            }
        }
    }

    private class AuthorizationRequestHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.Ok.CONSTRUCTOR:
                    // everything normal, nothing to do
                    break;
                case TdApi.Error.CONSTRUCTOR:
                    // TDLib error
                    TdApi.Error error = (TdApi.Error) object;
                    // tell UI about the error
                    Log.w(LOG_TAG, "TDLib error " + error.toString() + "; retrying...");
                    Message message = new Message();
                    message.what = ERROR_MESSAGE_CODE;
                    message.obj = "Error " + Integer.toString(error.code) + " - " + error.message;
                    activityHandler.sendMessage(message);
                    // retry last authorization action
                    onAuthorizationStateUpdated(authorizationState);
                    break;
                default:
                    Log.e(LOG_TAG, "unexpected response from TDLib: " + object.toString());
            }
        }
    }

    private void onAuthorizationStateUpdated(TdApi.AuthorizationState authorizationState) {
        if (authorizationState != null) {
            this.authorizationState = authorizationState;
        }
        switch (authorizationState.getConstructor()) {
            case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:
                // set TDLib parameters
                TdApi.TdlibParameters parameters = new TdApi.TdlibParameters();
                parameters.databaseDirectory = context.getFilesDir().getAbsolutePath();
                parameters.useMessageDatabase = USE_MESSAGE_DATABASE;
                parameters.useSecretChats = USE_SECRET_CHATS;
                parameters.apiId = API_ID;
                parameters.apiHash = API_HASH;
                parameters.systemLanguageCode = Locale.getDefault().getLanguage();
                parameters.deviceModel = Build.MANUFACTURER + ' ' + Build.MODEL;
                parameters.systemVersion = Build.VERSION.RELEASE;
                try {
                    parameters.applicationVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    parameters.applicationVersion = "Unknown Version";
                }
                parameters.enableStorageOptimizer = ENABLE_STORAGE_OPTIMIZER;
                client.send(new TdApi.SetTdlibParameters(parameters), new AuthorizationRequestHandler());
                Log.d(LOG_TAG, "set TDLib parameters");
                break;
            case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR:
                // check encryption key
                client.send(new TdApi.CheckDatabaseEncryptionKey(), new AuthorizationRequestHandler());
                Log.d(LOG_TAG, "checking encryption key");
                break;
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR: {
                // send phone number
                Log.d(LOG_TAG, "prompting UI thread for phone number");
                Message message = new Message();
                message.what = PHONE_NUMBER_REQUEST_MESSAGE_CODE;
                activityHandler.sendMessage(message);
                break;
            }
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR: {
                // send login code
                Log.d(LOG_TAG, "prompting UI thread for login code");
                Message message = new Message();
                message.what = LOGIN_CODE_REQUEST_MESSAGE_CODE;
                activityHandler.sendMessage(message);
                break;
            }
            case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR: {
                // send password (not implemented)
                client.send(new TdApi.Close(), new AuthorizationRequestHandler());
                Log.e(LOG_TAG, "password-based authorization not implemented");
                Toast.makeText(context, "Password-based authorization not supported.", Toast.LENGTH_LONG).show();
                break;
            }
            case TdApi.AuthorizationStateReady.CONSTRUCTOR:
                // authorized
                Log.i(LOG_TAG, "authorization ready");
                authorizationLock.lock();
                try {
                    gotAuthorization.signal();
                } finally {
                    authorizationLock.unlock();
                }
                break;
            case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR:
                // logged out
                Log.i(LOG_TAG, "logging out");
                break;
            case TdApi.AuthorizationStateClosing.CONSTRUCTOR:
                // closing database
                Log.d(LOG_TAG, "closing database");
                break;
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR:
                // database closed
                Log.i(LOG_TAG, "database closed");
                client = Client.create(new TDHelper.UpdatesHandler(), null, null);
                break;
            default:
                Log.e(LOG_TAG, "unexpected authorization state: " + authorizationState.toString());
        }
    }

    public void sendPhoneNumber(String phoneNumber) {
        client.send(new TdApi.SetAuthenticationPhoneNumber(phoneNumber, false, false), new AuthorizationRequestHandler());
        Log.i(LOG_TAG, "sent phone number: " + phoneNumber);
    }

    public void sendLoginCode(String code) {
        client.send(new TdApi.CheckAuthenticationCode(code, "", ""), new AuthorizationRequestHandler());
        Log.i(LOG_TAG, "sent login code: " + code);
    }

    public void logout() {
        client.send(new TdApi.LogOut(), new AuthorizationRequestHandler());
    }

    public void close() {
        client.close();
        instance = null;
    }

    public void sendMessage(long chatId, String s) {
        TdApi.FormattedText text = new TdApi.FormattedText(s, null);
        TdApi.InputMessageContent content = new TdApi.InputMessageText(text, false, false);
        client.send(new TdApi.SendMessage(chatId, 0, false, false, null, content), defaultHandler);
    }

}
