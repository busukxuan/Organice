package edu.sutd.organice;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.drinkless.td.libcore.telegram.TdApi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    CardView shareEventCard;
    CardView preferencesCard;
    CardView logoutCard;

    private Snackbar loginSnackbar;

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
                    Toast.makeText(MainActivity.this, (String) msg.obj, Toast.LENGTH_LONG).show();
                    break;

                default:
                    Log.wtf(LOG_TAG, "unexpected message code " + Integer.toString(msg.what));
            }
            return true;
        }
    });

    private static final String LOG_TAG = "MainActivity";

    private static final int CALENDAR_PERMISSIONS_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        logoutCard = findViewById(R.id.logoutCard);
        logoutCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "logout card clicked");
                tdHelper.logout();
            }
        });

        getCalendarPermissions();

        Authenticator.addAccount(this, "Organice");
        SyncAdapter.syncNow(this);

        TDUpdateJobService.scheduleImmediateJob(this);

        tdHelper = TDHelper.getInstance(this);
        tdHelper.setActivityHandler(tdHandler);

        switch (tdHelper.authorizationState.getConstructor()) {
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR:
                loginSnackbar.show();
                break;
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR:
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
        if (
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_CALENDAR
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_CALENDAR
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            // calendar permissions not granted
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