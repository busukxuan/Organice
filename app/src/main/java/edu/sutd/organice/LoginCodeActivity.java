package edu.sutd.organice;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class LoginCodeActivity extends AppCompatActivity {

    private static final String LOG_TAG = "LoginCodeActivity";

    EditText loginCodeEditText;
    Button loginCodeButton;
    Button cancelCodeButton;

    private boolean submit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_code);

        loginCodeEditText = findViewById(R.id.loginCodeEditText);

        loginCodeButton = findViewById(R.id.loginCodeButton);
        loginCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit = true;
                finish();
            }
        });

        cancelCodeButton = findViewById(R.id.CancelCodeButton);
        cancelCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        Log.i(LOG_TAG, "waiting for user to enter login code");
    }

    @Override
    protected void onDestroy() {

        if (submit) {
            Log.i(LOG_TAG, "login code received");
            TDHelper.getInstance(this).sendLoginCode(
                    loginCodeEditText.getText().toString()
            );
        } else {
            Log.i(LOG_TAG, "login cancelled");
            TDHelper.getInstance(this).logout();
        }

        super.onDestroy();
    }

}
