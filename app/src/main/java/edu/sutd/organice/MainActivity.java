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
            int requestCode = msg.arg1;
            Log.d(LOG_TAG, "received message from TDHelper with requestCode " + Integer.toString(requestCode));
            Intent intent;
            switch (requestCode) {
                case PHONE_NUMBER_REQUEST_CODE:
                    intent = new Intent(MainActivity.this, PhoneNumberActivity.class);
                    startActivityForResult(intent, PHONE_NUMBER_REQUEST_CODE);
                    break;
                case LOGIN_CODE_REQUEST_CODE:
                    intent = new Intent(MainActivity.this, LoginCodeActivity.class);
                    startActivityForResult(intent, LOGIN_CODE_REQUEST_CODE);
                    break;
                default:
                    Log.wtf(LOG_TAG, "unexpected request code " + Integer.toString(requestCode));
                    break;
            }
            return true;
        }
    });

    private static final String LOG_TAG = "MainActivity";

    public static final int PHONE_NUMBER_REQUEST_CODE = 0;
    public static final int LOGIN_CODE_REQUEST_CODE = 1;

    private static final int CALENDAR_READ_PERMISSION_REQUEST_CODE = 0;
    private static final int CALENDAR_WRITE_PERMISSION_REQUEST_CODE = 0;

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


        tdHelper = new TDHelper(this, tdHandler);

        getCalendarPermissions();
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
        tdHelper.close();
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
                    CALENDAR_READ_PERMISSION_REQUEST_CODE);
        }
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResult) {
        Log.d(LOG_TAG, "received permission request response");
        switch (requestCode) {
            case CALENDAR_READ_PERMISSION_REQUEST_CODE:
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