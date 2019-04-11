package edu.sutd.organice;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import androidx.appcompat.app.AppCompatActivity;

public class EventSearchActivity extends AppCompatActivity {

    private EditText titleEditText;
    private EditText startEditText;
    private EditText endEditText;
    private EditText venueEditText;
    private EditText noteEditText;
    private Button searchButton;

    public static final int SUCCESS_RESULT_CODE = 0;
    public static final int NO_RESULT_RESULT_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_search);

        titleEditText = findViewById(R.id.titleEditText);
        startEditText = findViewById(R.id.startEditText);
        endEditText = findViewById(R.id.endEditText);
        venueEditText = findViewById(R.id.venueEditText);
        noteEditText = findViewById(R.id.noteEditText);

        searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

                EventData eventData = null;
                try {
                    eventData = new EventData(
                            titleEditText.getText().toString(),
                            format.parse(startEditText.getText().toString()),
                            format.parse(endEditText.getText().toString()),
                            venueEditText.getText().toString(),
                            noteEditText.getText().toString()
                    );
                } catch (ParseException e) {
                    Snackbar.make(searchButton, "invalid date format", Snackbar.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent(EventSearchActivity.this, EventSelectionActivity.class);
                intent.putExtra("mode", EventSelectionActivity.SEARCH_EVENTS_MODE);
                intent.putExtra("eventTitle", eventData.title);
                intent.putExtra("eventStart", eventData.dateStart.getTime());
                intent.putExtra("eventEnd", eventData.dateEnd.getTime());
                intent.putExtra("eventVenue", eventData.venue);
                intent.putExtra("eventNote", eventData.note);
                startActivityForResult(intent, 0);
            }
        });

        setResult(NO_RESULT_RESULT_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case EventSelectionActivity.SUCCESS_RESULT_CODE:
                setResult(SUCCESS_RESULT_CODE, data);
                finish();
                break;
            case EventSelectionActivity.NO_RESULT_RESULT_CODE:
                break;
        }
    }

}
