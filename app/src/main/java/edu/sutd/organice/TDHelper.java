package edu.sutd.organice;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
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
    private static TDHelper instance;

    private static final String LOG_TAG = "TDHelper";
    private static final Class phoneNumberActivityClass = PhoneNumberActivity.class;
    private static final Class loginCodeActivityClass = LoginCodeActivity.class;

    private final MainActivity context;
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

    private TDHelper(MainActivity context) {
        this.context = context;

        // create client
        client = Client.create(new TDHelper.UpdatesHandler(), null, null);
    }

    public static TDHelper getInstance(MainActivity mainActivity) {
        if (instance == null) {
            instance = new TDHelper(mainActivity);
            return instance;
        } else {
            throw new IllegalStateException("another instance already exists");
        }
    }

    public static TDHelper getInstance() {
        if (instance != null) {
            return instance;
        } else {
            throw new IllegalStateException("instance does not exist; use getInstance(MainActivity) to create instance");
        }
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
                Intent phoneNumberActivityIntent = new Intent(context, phoneNumberActivityClass);
                context.startActivity(phoneNumberActivityIntent);
                break;
            }
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR: {
                // send login code
                Intent loginCodeActivityIntent = new Intent(context, loginCodeActivityClass);
                context.startActivity(loginCodeActivityIntent);
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
                haveAuthorization = false;
                Log.i(LOG_TAG, "logging out");
                break;
            case TdApi.AuthorizationStateClosing.CONSTRUCTOR:
                // closing database
                haveAuthorization = false;
                Log.d(LOG_TAG, "closing database");
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
            TdApi.FormattedText text = ((TdApi.MessageText) content).text;
            try {
                context.setText(ActionRequest.parseMessage(text.text).toString());
                Toast.makeText(context, ActionRequest.parseMessage(text.text).toString(), Toast.LENGTH_LONG).show();
            } catch (ParseException e) {
                throw new RuntimeException();
            }
        }
    }

    public void sendPhoneNumber(String phoneNumber) {
        client.send(new TdApi.SetAuthenticationPhoneNumber(phoneNumber, false, false), new AuthorizationRequestHandler());
        Log.i(LOG_TAG, "sent phone number");
    }

    public void sendLoginCode(String code) {
        client.send(new TdApi.CheckAuthenticationCode(code, "", ""), new AuthorizationRequestHandler());
        Log.i(LOG_TAG, "sent login code");
    }

    public void logout() {
        client.send(new TdApi.LogOut(), new AuthorizationRequestHandler());
    }

    public void close() {
        client.send(new TdApi.Close(), new AuthorizationRequestHandler());
    }

}
