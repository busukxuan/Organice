package edu.sutd.organice;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class EventSelectionActivity extends AppCompatActivity {

    private static final String LOG_TAG = "EventSelectionActivity";

    private RecyclerView eventDataRecyclerView;
    private RecyclerView.Adapter eventDataRecyclerAdapter;
    private RecyclerView.LayoutManager eventDataRecyclerLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_selection);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        eventDataRecyclerView = findViewById(R.id.event_data_recycler_view);
        eventDataRecyclerLayoutManager = new LinearLayoutManager(this);
        eventDataRecyclerView.setLayoutManager(eventDataRecyclerLayoutManager);
        eventDataRecyclerAdapter = new EventDataRecyclerAdapter(this, (new CalendarHelper(this)).getNextEvents());
        eventDataRecyclerView.setAdapter(eventDataRecyclerAdapter);
    }

    void onItemClick(EventData eventData) {
        String message = "# organice new\n" + eventData.toMessageFormat() + "\n# end organice";

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);
        shareIntent.setType("text/plain");
        shareIntent.setPackage("org.telegram.messenger");
        startActivity(shareIntent);
    }
}
