package edu.sutd.organice;


import android.content.Intent;
import android.os.Bundle;

import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;

/**
 * An {@link android.app.Activity Activity} for users to share an event to Telegram.
 * <p>
 *     This {@link android.app.Activity Activity} itself has no UI element, but it starts a
 *     {@link EventSelectionActivity} for users to select an event to share, then sends an
 *     {@link Intent Intent} to the <code>org.telegram.messenger</code> package which opens the
 *     Telegram app's Share screen, where users can choose which chat to share to.</p>
 * <p>
 *     Also, this {@link android.app.Activity Activity} has a launcher shortcut (registered in
 *     <code>shortcuts.xml</code>) so that users can access it without going through
 *     {@link MainActivity}.
 * </p>
 */
public class ShareEventActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // start `EventSelectionActivity` for result
        Intent intent = new Intent(this, EventSelectionActivity.class);
        intent.putExtra("mode", EventSelectionActivity.SHOW_UPCOMING_EVENTS_MODE);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == EventSelectionActivity.SUCCESS_RESULT_CODE) {
            // share the event to the Telegram app as text
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
