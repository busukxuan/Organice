package edu.sutd.organice;

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
                TDHelper.getInstance().sendLoginCode(loginCodeEditText.getText().toString());
                finish();
            }
        });
        Log.i(LOG_TAG, "waiting for user to enter login code");
    }
}
