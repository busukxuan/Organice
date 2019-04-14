package edu.sutd.organice;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * An {@link android.app.Activity Activity} showing a {@link RecyclerView} where users can select
 * an event.
 * <p>
 *     This {@link android.app.Activity Activity} contains a floating search button.
 *     When in "show upcoming events" mode, the search button starts an {@link EventSearchActivity}
 *     to let users search for events beyond the next 5. When in "search events" mode, the button
 *     simply finishes the current activity to return to the {@link EventSearchActivity} that called
 *     the current activity with no result, to allow users to change or refine their search criteria.
 * </p>
 */
public class EventSelectionActivity extends AppCompatActivity {

    private static final String LOG_TAG = "EventSelectionActivity";

    // operation modes for this activity
    public static final int SHOW_UPCOMING_EVENTS_MODE = 0;  // show the next 5 events
    public static final int SEARCH_EVENTS_MODE = 1;         // search for events matching some criteria

    // result codes
    public static final int SUCCESS_RESULT_CODE = 0;    // this activity has successfully yielded a result
    public static final int NO_RESULT_RESULT_CODE = 1;  // no result was yielded

    // RecyclerView objects
    private RecyclerView eventDataRecyclerView;
    private RecyclerView.Adapter eventDataRecyclerAdapter;
    private RecyclerView.LayoutManager eventDataRecyclerLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_selection);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // get the intent used to start this activity
        Intent callingIntent = getIntent();

        FloatingActionButton fab = findViewById(R.id.fab);
        switch (callingIntent.getIntExtra("mode", 0)) {
            case SHOW_UPCOMING_EVENTS_MODE:
                // "show upcoming events" mode, search button should start an EventSelectionActivity
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(EventSelectionActivity.this, EventSearchActivity.class);
                        startActivityForResult(intent, 0);
                    }
                });
                break;
            case SEARCH_EVENTS_MODE:
                // "search events" mode, search button should return to calling EventSelectionActivity
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setResult(NO_RESULT_RESULT_CODE);
                        finish();
                    }
                });
                break;
        }

        // initialize the RecyclerView showing the events
        eventDataRecyclerView = findViewById(R.id.event_data_recycler_view);
        eventDataRecyclerLayoutManager = new LinearLayoutManager(this);
        eventDataRecyclerView.setLayoutManager(eventDataRecyclerLayoutManager);
        EventData[] events = null;
        switch (callingIntent.getIntExtra("mode", 0)) {
            case SHOW_UPCOMING_EVENTS_MODE:
                // "show upcoming events" mode, get the next 5 events
                events = (new CalendarHelper(this)).getNextEvents();
                break;
            case SEARCH_EVENTS_MODE:
                // "search events" mode, search for events with the given criteria
                try {
                    events = (EventData[]) (new CalendarHelper(this)).getEvents(
                            callingIntent.getStringExtra("eventTitle"),
                            new Date(callingIntent.getLongExtra("eventStart", 0)),
                            new Date(callingIntent.getLongExtra("eventEnd", 0)),
                            callingIntent.getStringExtra("eventVenue"),
                            callingIntent.getStringExtra("eventNote"),
                            false
                    ).toArray(new EventData[0]);
                } catch (Exception e) {
                    Log.v(LOG_TAG, e.toString());
                    events = new EventData[0];
                }
                Log.v(LOG_TAG, "found " + Integer.toString(events.length) + " events");
                break;
        }
        eventDataRecyclerAdapter = new EventDataRecyclerAdapter(this, events);
        eventDataRecyclerView.setAdapter(eventDataRecyclerAdapter);

        // set default result code
        setResult(NO_RESULT_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == EventSearchActivity.SUCCESS_RESULT_CODE) {
            // EventSearchActivity has yielded a result, finish and pass that result back to the calling activity
            setResult(SUCCESS_RESULT_CODE, data);
            finish();
        }
    }

    /**
     * Actions to be taken when an item in the RecyclerView showing the given event data is clicked.
     * @param eventData The event data shown by the RecyclerView item that was clicked
     */
    void onItemClick(EventData eventData) {
        // return the event data to the calling activity
        Intent returnData = new Intent();
        Log.v(LOG_TAG, "putting extra eventTitle " + eventData.title);
        returnData.putExtra("eventTitle", eventData.title);
        returnData.putExtra("eventStart", eventData.dateStart.getTime());
        returnData.putExtra("eventEnd", eventData.dateEnd.getTime());
        returnData.putExtra("eventVenue", eventData.venue);
        returnData.putExtra("eventNote", eventData.note);
        setResult(SUCCESS_RESULT_CODE, returnData);
        finish();
    }

}
