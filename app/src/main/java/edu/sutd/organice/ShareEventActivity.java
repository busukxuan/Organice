package edu.sutd.organice;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Date;

public class ShareEventActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, EventSelectionActivity.class);
        intent.putExtra("mode", EventSelectionActivity.SHOW_UPCOMING_EVENTS_MODE);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == EventSelectionActivity.SUCCESS_RESULT_CODE) {
            EventData eventData = new EventData(
                    data.getStringExtra("eventTitle"),
                    new Date(data.getLongExtra("eventStart", 0)),
                    new Date(data.getLongExtra("eventEnd", 0)),
                    data.getStringExtra("eventVenue"),
                    data.getStringExtra("eventNote")
            );

            String message = "# organice new\n" + eventData.toMessageFormat() + "\n# end organice";

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, message);
            shareIntent.setType("text/plain");
            shareIntent.setPackage("org.telegram.messenger");
            startActivity(shareIntent);
        }
        finish();
    }
}
