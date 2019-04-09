package edu.sutd.organice;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {


    TextView textView;
    Button logoutButton;

    private TDHelper tdHelper;
    private Handler tdHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Intent intent;
            switch (msg.what) {

                case TDHelper.PHONE_NUMBER_REQUEST_MESSAGE_CODE:
                    intent = new Intent(MainActivity.this, PhoneNumberActivity.class);
                    startActivityForResult(intent, PHONE_NUMBER_REQUEST_CODE);
                    break;

                case TDHelper.LOGIN_CODE_REQUEST_MESSAGE_CODE:
                    intent = new Intent(MainActivity.this, LoginCodeActivity.class);
                    startActivityForResult(intent, LOGIN_CODE_REQUEST_CODE);
                    break;

                case TDHelper.UPDATE_MESSAGE_CODE:
                    // do nothing
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

    private static final String LOG_TAG = "MainActivity";

    public static final int PHONE_NUMBER_REQUEST_CODE = 0;
    public static final int LOGIN_CODE_REQUEST_CODE = 1;

    private static final int CALENDAR_PERMISSIONS_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);

        logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tdHelper.logout();
            }
        });



        getCalendarPermissions();

        Authenticator.addAccount(this, "Organice");
        SyncAdapter.syncNow(this);

        TDUpdateJobService.scheduleImmediateJob(this);

        tdHelper = TDHelper.getInstance(this);
        tdHelper.setActivityHandler(tdHandler);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PHONE_NUMBER_REQUEST_CODE:
                String phoneNumber = data.getStringExtra("phoneNumber");
                tdHelper.sendPhoneNumber(phoneNumber);
                break;
            case LOGIN_CODE_REQUEST_CODE:
                String loginCode = data.getStringExtra("loginCode");
                if(loginCode=="CANCELLED") break;
                else tdHelper.sendLoginCode(loginCode);
                break;
            default:
                Log.wtf(LOG_TAG, "unexpected request code " + Integer.toString(requestCode));
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
                    CALENDAR_PERMISSIONS_REQUEST_CODE);
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