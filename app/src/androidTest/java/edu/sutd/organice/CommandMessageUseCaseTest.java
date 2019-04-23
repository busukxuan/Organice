package edu.sutd.organice;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import org.drinkless.td.libcore.telegram.TdApi;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CommandMessageUseCaseTest {

    private static final long WAIT_DURATION = 3000;

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    private Handler tdHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.i("TestWeirder", "omg, called!");
            switch (msg.what) {
                case TDHelper.UPDATE_MESSAGE_CODE:
                    // got an update, check if it's a new message
                    if (((TdApi.Object) msg.obj).getConstructor() == TdApi.UpdateNewMessage.CONSTRUCTOR) {
                        // got a new message
                        TdApi.UpdateNewMessage updateNewMessage = (TdApi.UpdateNewMessage) msg.obj;
                        TdApi.MessageContent content = updateNewMessage.message.content;
                        // try to execute the request in that message, if any
                        if (content instanceof TdApi.MessageText) {
                            receivedMessageString = ((TdApi.MessageText) content).text.text;
                            try {
                                ActionRequest.execute(preferences, calendarHelper, tdHelper, updateNewMessage.message);
                            } catch (Exception e) {
                                if (updateNewMessage.message.isOutgoing) {
                                    tdHelper.sendMessage(
                                            updateNewMessage.message.chatId,
                                            "Organice error - " + e.getMessage()
                                    );
                                } else {
                                    e.printStackTrace();
                                }
                            }
                        }

                    }
            }
            return true;
        }
    });

    private static final long telegramChatId = 777000;
    private static final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private static final SimpleDateFormat shortFormat = new SimpleDateFormat("d/M H:mm");

    private static final String eventTitle = "Test event";
    private static final String eventVenue = "Nowhere";
    private static final String eventNote = "Test node.";

    private static String eventDataString;
    private static Calendar calendar;
    private static Date start;
    private static Date end;

    private volatile String receivedMessageString;

    private TDHelper tdHelper;
    private CalendarHelper calendarHelper;
    private SharedPreferences preferences;

    public CommandMessageUseCaseTest() {
        calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Context context = ApplicationProvider.getApplicationContext();
        tdHelper = TDHelper.getInstance(context);
        tdHelper.testHandler = tdHandler;
        calendarHelper = new CalendarHelper(context);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Test
    public void test0NewEventMessage() {
        start = calendar.getTime();
        calendar.add(Calendar.HOUR, 1);
        end = calendar.getTime();

        eventDataString =
                "Title: " + eventTitle + "\n" +
                        "Start: " + format.format(start) + "\n" +
                        "End: " + format.format(end) + "\n" +
                        "Venue: " + eventVenue + "\n" +
                        "Note: " + eventNote;

        tdHelper.sendMessage(telegramChatId, "# organice new\n" + eventDataString);

        try {
            Thread.sleep(WAIT_DURATION);
        } catch (InterruptedException e) {}

        List<EventData> eventData = calendarHelper.getEvents(eventTitle, start, end, eventVenue, eventNote, true);
        assertEquals(1, eventData.size());
    }

    @Test
    public void test1ListEventMessage() {
        String eventDataLine =
                ". \"" + eventTitle + "\" " +
                        shortFormat.format(start) + " - " + shortFormat.format(end) +
                        " at " + eventVenue;

        tdHelper.testHandler = tdHandler;
        tdHelper.sendMessage(telegramChatId, "# organice list");
        try {
            Thread.sleep(WAIT_DURATION);
        } catch (InterruptedException e) {}
        assertNotEquals(null, receivedMessageString);
        assertTrue(
                "expected substring: \"" + eventDataLine + '"' + ", actual: \"" + receivedMessageString + '"',
                receivedMessageString.contains(eventDataLine)
        );
    }

    @Test
    public void test2DeleteEventMessage() {
        tdHelper.sendMessage(telegramChatId, "# organice delete\n" + eventDataString);

        try {
            Thread.sleep(WAIT_DURATION);
        } catch (InterruptedException e) {}

        List<EventData> eventData = calendarHelper.getEvents(eventTitle, start, end, eventVenue, eventNote, true);
        assertEquals(0, eventData.size());
    }
}