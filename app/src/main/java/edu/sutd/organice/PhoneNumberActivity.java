package edu.sutd.organice;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * An {@link android.app.Activity Activity} in which users can enter their phone number as part of
 * the login procedure of TDLib.
 */
public class PhoneNumberActivity extends AppCompatActivity {

    private static final String LOG_TAG = "PhoneNumberActivity";

    // permission request codes
    private static final int READ_PHONE_STATE_PERMISSION_REQUEST_CODE = 0;

    // views
    EditText phoneNumberEditText;
    Button phoneNumberButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number);

        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        phoneNumberButton = findViewById(R.id.phoneNumberButton);
        phoneNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send phone number and finish activity on click
                TDHelper.getInstance(PhoneNumberActivity.this).sendPhoneNumber(
                        phoneNumberEditText.getText().toString()
                );
                finish();
        }});
        autofillPhoneNumber();
        Log.i(LOG_TAG, "waiting for user to enter phone number");
    }

    private void autofillPhoneNumber() {
        Log.d(LOG_TAG, "attempting to autofill phone number");
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {
                // do nothing for now
            } else {
                Log.d(LOG_TAG, "requesting for permission to read phone state");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE}, READ_PHONE_STATE_PERMISSION_REQUEST_CODE);
            }
        } else {
            TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            phoneNumberEditText.setText(tm.getLine1Number());
            Log.d(LOG_TAG, "autofilled phone number " + tm.getLine1Number());
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permission, int[] grantResult) {
        if (requestCode == READ_PHONE_STATE_PERMISSION_REQUEST_CODE) {
            if (grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                if (phoneNumberEditText.getText().toString().isEmpty()) {
                    TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
                    phoneNumberEditText.setText(tm.getLine1Number(), TextView.BufferType.EDITABLE);
                    Log.d(LOG_TAG, "autofilled phone number " + tm.getLine1Number());
                } else {
                    Log.wtf(LOG_TAG, "phone number already entered by user, not autofilling");
                }
            } else {
                Log.wtf(LOG_TAG, "permission request denied");
            }
        } else {
            Log.wtf(LOG_TAG, "unexpected permission request code");
        }
    }
}
