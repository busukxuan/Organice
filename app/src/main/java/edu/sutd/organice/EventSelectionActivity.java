package edu.sutd.organice;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.util.Date;

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
                EventData template = new EventData(
                        callingIntent.getStringExtra("eventTitle"),
                        (Date) callingIntent.getParcelableExtra("eventDate"),
                        (Date) callingIntent.getParcelableExtra("eventEnd"),
                        callingIntent.getStringExtra("eventVenue"),
                        callingIntent.getStringExtra("eventNote")
                );
                try {
                    events = (EventData[]) (new CalendarHelper(this)).getEvents(template).toArray();
                } catch (Exception e) {
                    Log.v(LOG_TAG, "exeption here...");
                    events = new EventData[0];
                }
                break;
        }
        eventDataRecyclerAdapter = new EventDataRecyclerAdapter(this, events);
        eventDataRecyclerView.setAdapter(eventDataRecyclerAdapter);
    }

    void onItemClick(EventData eventData) {
/*        String message = "# organice new\n" + eventData.toMessageFormat() + "\n# end organice";

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);
        shareIntent.setType("text/plain");
        shareIntent.setPackage("org.telegram.messenger");
        startActivity(shareIntent);*/

        Intent returnIntent = new Intent();
        returnIntent.putExtra("eventTitle", eventData.title);
        returnIntent.putExtra("eventStart", eventData.dateStart);
        returnIntent.putExtra("eventEnd", eventData.dateEnd);
        returnIntent.putExtra("eventVenue", eventData.venue);
        returnIntent.putExtra("eventNote", eventData.note);
        setResult(SUCCESS_RESULT_CODE, returnIntent);
        finish();

    }
}
