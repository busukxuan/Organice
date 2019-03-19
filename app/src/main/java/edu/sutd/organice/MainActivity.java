package edu.sutd.organice;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
                tdHelper.sendLoginCode(loginCode);
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

    public static class phoneNumber {
        public static String value = null;
    }

    public void setText(String text) {
        textView.setText(text);
    }

}