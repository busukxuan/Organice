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

import java.text.ParseException;
import java.util.Locale;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TDHelper {
    private static final String LOG_TAG = "TDHelper";

    private final Context context;
    private final Handler uiHandler;
    private final CalendarHelper calendarHelper;
    private Client client;
    private final Client.ResultHandler defaultHandler = new TDHelper.DefaultHandler();
    // authorization variables
    private final Lock authorizationLock = new ReentrantLock();
    private final Condition gotAuthorization = authorizationLock.newCondition();
    private volatile boolean haveAuthorization = false;
    private TdApi.AuthorizationState authorizationState = null;

    // TDLib client parameters
    private static final boolean USE_MESSAGE_DATABASE = true;
    private static final boolean USE_SECRET_CHATS = false;
    private static final int API_ID = 611772;
    private static final String API_HASH = "6c33afaaf22568fb526bd770cfa31645";
    private static final boolean ENABLE_STORAGE_OPTIMIZER = true;

    static {
        System.loadLibrary("tdjni");
    }

    public TDHelper(Context context, Handler handler) {
        this.context = context;
        this.uiHandler = handler;
        this.calendarHelper = new CalendarHelper(context);

        // create client
        client = Client.create(new TDHelper.UpdatesHandler(), null, null);
    }

    private class DefaultHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
        }
    }

    private class UpdatesHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {

                case TdApi.UpdateAuthorizationState.CONSTRUCTOR:
                    onAuthorizationStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
                    break;

                case TdApi.UpdateNewMessage.CONSTRUCTOR:
                    onNewMessage((TdApi.UpdateNewMessage) object);
                    break;

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
                    Log.w(LOG_TAG, "TDLib error: " + object.toString() + "; retrying...");
                    onAuthorizationStateUpdated(authorizationState); // repeat last action
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
                message.arg1 = MainActivity.PHONE_NUMBER_REQUEST_CODE;
                uiHandler.sendMessage(message);
                break;
            }
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR: {
                // send login code
                Log.d(LOG_TAG, "prompting UI thread for login code");
                Message message = new Message();
                message.arg1 = MainActivity.LOGIN_CODE_REQUEST_CODE;
                uiHandler.sendMessage(message);
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
                haveAuthorization = true;
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
                haveAuthorization = false;
                break;
            case TdApi.AuthorizationStateClosing.CONSTRUCTOR:
                // closing database
                Log.d(LOG_TAG, "closing database");
                haveAuthorization = false;
                break;
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR:
                // database closed
                Log.i(LOG_TAG, "database closed");
                break;
            default:
                Log.e(LOG_TAG, "unexpected authorization state: " + authorizationState.toString());
        }
    }

    private void onNewMessage(TdApi.UpdateNewMessage updateNewMessage) {
        Log.i(LOG_TAG, "received new message");
        TdApi.MessageContent content = updateNewMessage.message.content;
        if (content instanceof TdApi.MessageText) {
            try {
                ActionRequest.execute(calendarHelper, updateNewMessage.message);
            } catch (ParseException e) {
                Log.e(LOG_TAG, "parse error");
            } catch (Exception e) {
                e.printStackTrace();
            }
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
    }

}
