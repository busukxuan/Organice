package edu.sutd.organice;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PhoneNumberActivity extends AppCompatActivity {

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
                Example.sendPhoneNumber(phoneNumberEditText.getText().toString());
                finish();
            }
        });
    }
}
