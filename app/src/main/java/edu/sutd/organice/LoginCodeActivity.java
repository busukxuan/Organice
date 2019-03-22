package edu.sutd.organice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginCodeActivity extends AppCompatActivity {

    private static final String LOG_TAG = "LoginCodeActivity";

    EditText loginCodeEditText;
    Button loginCodeButton;
    Button CancelcodeButton;
    TextView tv;
    String st;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_code);
        tv=findViewById(R.id.HP_num);
        st=getIntent().getExtras().getString("phoneNumber1");
        tv.setText(st);

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

        CancelcodeButton = findViewById(R.id.CancelCodeButton);
        CancelcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("loginCode", "CANCELLED");
                setResult(0, intent);
                finish();
            }
        });
        Log.i(LOG_TAG, "waiting for user to enter login code");
    }

}
