package edu.sutd.organice;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.drinkless.td.libcore.telegram.TdApi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * The main {@link android.app.Activity Activity} / entry point to the app. Presents a few actions
 * that the user can perform with the app, and a {@link Snackbar Snackbar} when the user is not logged in.
 */
public class MainActivity extends AppCompatActivity {

    // views
    CardView shareEventCard;
    CardView preferencesCard;
    CardView logoutCard;

    // snackbar
    private Snackbar loginSnackbar;

    // TDHelper instance
    private TDHelper tdHelper;
    private Handler tdHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {

                case TDHelper.PHONE_NUMBER_REQUEST_MESSAGE_CODE:
                    loginSnackbar.show();
                    break;

                case TDHelper.LOGIN_CODE_REQUEST_MESSAGE_CODE:
                    Intent intent = new Intent(MainActivity.this, LoginCodeActivity.class);
                    startActivity(intent);
                    break;

                case TDHelper.UPDATE_MESSAGE_CODE:
                    // do nothing
                    break;

                case TDHelper.RESULT_MESSAGE_CODE:
                    // do nothing
                    break;

                case TDHelper.ERROR_MESSAGE_CODE:
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    if (preferences.getBoolean(getResources().getString(R.string.enable_error_toast), true)) {
                        Toast.makeText(MainActivity.this, (String) msg.obj, Toast.LENGTH_LONG).show();
                    }
                    break;

                default:
                    Log.wtf(LOG_TAG, "unexpected message code " + Integer.toString(msg.what));
            }
            return true;
        }
    });

    private static final String LOG_TAG = "MainActivity";

    // permission request code
    private static final int CALENDAR_PERMISSIONS_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize snackbar
        loginSnackbar = Snackbar.make(
                        findViewById(R.id.mainActivityLayout),
                        "You are not logged in. Events received through Telegram will not be added.",
                        Snackbar.LENGTH_INDEFINITE
                );
        loginSnackbar.setAction("Log In", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, PhoneNumberActivity.class);
                    startActivity(intent);
                }
        });

        // initialize share event card
        shareEventCard = findViewById(R.id.shareEventCard);
        shareEventCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "share event card clicked");
                Intent intent = new Intent(MainActivity.this, ShareEventActivity.class);
                intent.putExtra("mode", EventSelectionActivity.SHOW_UPCOMING_EVENTS_MODE);
                startActivity(intent);
            }
        });

        // initialize preferences card
        preferencesCard = findViewById(R.id.preferencesCard);
        preferencesCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // do nothing for now
                Log.d(LOG_TAG, "preferences card clicked");
                Intent intent = new Intent(MainActivity.this, PreferencesActivity.class);
                startActivity(intent);
            }
        });

        // initialize login / logout card
        logoutCard = findViewById(R.id.logoutCard);
        logoutCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "logout card clicked");
                tdHelper.logout();
            }
        });

        // ensure the app has calendar permissions
        getCalendarPermissions();

        // ensure a calendar for the app exists
        Authenticator.addAccount(this, "Organice");
        SyncAdapter.syncNow(this);

        // start a background job service to keep TDLib running
        TDUpdateJobService.scheduleImmediateJob(this);

        // get TDHelper instance
        tdHelper = TDHelper.getInstance(this);
        tdHelper.setActivityHandler(tdHandler);

        // initialize the activity according to TDHelper's current authorization state
        switch (tdHelper.authorizationState.getConstructor()) {
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR:
                // TDLib ready to receive login phone number, show login snackbar
                loginSnackbar.show();
                break;
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR:
                // TDLib waiting for login code, start `LoginCodeActivity`
                Intent intent = new Intent(MainActivity.this, LoginCodeActivity.class);
                startActivity(intent);
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void getCalendarPermissions() {
        Log.d(LOG_TAG, "checking for calendar permissions");
        int readPermissionState = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR);
        int writePermissionState = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR);
        if (
                readPermissionState != PackageManager.PERMISSION_GRANTED ||
                writePermissionState != PackageManager.PERMISSION_GRANTED
        ) {
            // calendar permissions not granted, request now
            Log.d(LOG_TAG, "requesting calendar permissions");
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR},
                    CALENDAR_PERMISSIONS_REQUEST_CODE
            );
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResult) {
        Log.d(LOG_TAG, "received permission request response");
        switch (requestCode) {
            case CALENDAR_PERMISSIONS_REQUEST_CODE:
                if (
                        grantResult[0] != PackageManager.PERMISSION_GRANTED ||
                        grantResult[1] != PackageManager.PERMISSION_GRANTED
                ) {
                    // do nothing for now
                }
                break;
        }
    }

}