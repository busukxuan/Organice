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

/**
 * An {@link android.app.Activity Activity} showing a search form to search for events.
 * <p>
 *     This {@link android.app.Activity Activity} starts an {@link EventSelectionActivity}.
 *     If that activity yields result, this activity yields the same result. If that activity yields
 *     no result, this activity continues for users to modify / refine their search criteria. If
 *     this activity is exited before a result is yielded by the {@link EventSelectionActivity},
 *     then this activity returns no result.
 * </p>
 */
public class EventSearchActivity extends AppCompatActivity {

    // views
    private EditText titleEditText;
    private EditText startEditText;
    private EditText endEditText;
    private EditText venueEditText;
    private EditText noteEditText;
    private Button searchButton;

    // result codes
    public static final int SUCCESS_RESULT_CODE = 0;    // this activity has successfully yielded a result
    public static final int NO_RESULT_RESULT_CODE = 1;  // no result was yielded

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
                // start EventSelectionActivity when search button is pressed
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

                String startString = startEditText.getText().toString();
                String endString = endEditText.getText().toString();

                EventData eventData = null;
                try {
                    eventData = new EventData(
                            titleEditText.getText().toString(),
                            startString.trim().isEmpty()? null : format.parse(startString),
                            endString.trim().isEmpty()? null : format.parse(endString),
                            venueEditText.getText().toString(),
                            noteEditText.getText().toString()
                    );
                } catch (ParseException e) {
                    Snackbar.make(searchButton, "Invalid date format", Snackbar.LENGTH_LONG).show();
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

        // set default result code
        setResult(NO_RESULT_RESULT_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case EventSelectionActivity.SUCCESS_RESULT_CODE:
                // EventSelectionActivity yielded a result, finish and pass it back to calling activity
                setResult(SUCCESS_RESULT_CODE, data);
                finish();
                break;
            case EventSelectionActivity.NO_RESULT_RESULT_CODE:
                // EventSelectionActivity yielded no result, do nothing
                break;
        }
    }

}
