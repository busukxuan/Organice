package edu.sutd.organice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginCodeActivity extends AppCompatActivity {

    private static final String LOG_TAG = "LoginCodeActivity";

    EditText loginCodeEditText;
    Button loginCodeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_code);

        loginCodeEditText = findViewById(R.id.loginCodeEditText);

        loginCodeButton = findViewById(R.id.loginCodeButton);
        loginCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("loginCode", loginCodeEditText.getText().toString());
                setResult(0, intent);
                finish();
            }
        });
        Log.i(LOG_TAG, "waiting for user to enter login code");
    }
}
