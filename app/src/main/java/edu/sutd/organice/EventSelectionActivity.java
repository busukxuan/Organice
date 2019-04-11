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

public class EventSelectionActivity extends AppCompatActivity {

    private static final String LOG_TAG = "EventSelectionActivity";

    public static final int SHOW_UPCOMING_EVENTS_MODE = 0;
    public static final int SEARCH_EVENTS_MODE = 1;

    public static final int SUCCESS_RESULT_CODE = 0;
    public static final int NO_RESULT_RESULT_CODE = 1;

    private RecyclerView eventDataRecyclerView;
    private RecyclerView.Adapter eventDataRecyclerAdapter;
    private RecyclerView.LayoutManager eventDataRecyclerLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_selection);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent callingIntent = getIntent();

        FloatingActionButton fab = findViewById(R.id.fab);
        switch (callingIntent.getIntExtra("mode", 0)) {
            case SHOW_UPCOMING_EVENTS_MODE:
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(EventSelectionActivity.this, EventSearchActivity.class);
                        startActivityForResult(intent, 0);
                    }
                });
                break;
            case SEARCH_EVENTS_MODE:
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setResult(NO_RESULT_RESULT_CODE);
                        finish();
                    }
                });
                break;
        }

        eventDataRecyclerView = findViewById(R.id.event_data_recycler_view);
        eventDataRecyclerLayoutManager = new LinearLayoutManager(this);
        eventDataRecyclerView.setLayoutManager(eventDataRecyclerLayoutManager);
        EventData[] events = null;
        switch (callingIntent.getIntExtra("mode", 0)) {
            case SHOW_UPCOMING_EVENTS_MODE:
                events = (new CalendarHelper(this)).getNextEvents();
                break;
            case SEARCH_EVENTS_MODE:
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

        setResult(NO_RESULT_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == EventSearchActivity.SUCCESS_RESULT_CODE) {
            setResult(SUCCESS_RESULT_CODE, data);
            finish();
        }
    }

    void onItemClick(EventData eventData) {
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
