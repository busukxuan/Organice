package edu.sutd.organice;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    public int newMessages = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);

        try {
            Example.main(null, this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static class phoneNumber {
        public static String value = null;
    }

}