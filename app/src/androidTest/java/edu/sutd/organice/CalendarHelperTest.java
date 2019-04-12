package edu.sutd.organice;

import android.net.Uri;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.test.platform.app.InstrumentationRegistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CalendarHelperTest {

    private static final int RANDOM_SEED = 64735;
    private static final long TEST_CHAT_ID = 64735;
    private static final String TEST_EVENT_TITLE = "Test Event Title";
    private static final String TEST_EVENT_VENUE = "Test Event Venue";
    private static final String TEST_EVENT_NOTE = "Test Event Note";

    private long startTime;
    private long endTime;

    private CalendarHelper calendarHelper;

    public CalendarHelperTest() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date today = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date tmr = calendar.getTime();
        this.startTime = today.getTime();
        this.endTime = tmr.getTime();
        this.calendarHelper = new CalendarHelper(
                InstrumentationRegistry.getInstrumentation().getTargetContext()
        );
    }

    @Test
    public void test0AddEvent() {
        Uri eventUri = calendarHelper.addEvent(new NewEventRequest(
                TEST_CHAT_ID,
                new EventData(
                    TEST_EVENT_TITLE,
                    new Date(startTime),
                    new Date(endTime),
                    TEST_EVENT_VENUE,
                    TEST_EVENT_NOTE
                )
        ));
        assertNotEquals(null, eventUri);
        List<EventData> eventData = calendarHelper.getEvents(
                TEST_EVENT_TITLE,
                new Date(startTime),
                new Date(endTime),
                TEST_EVENT_VENUE,
                TEST_EVENT_NOTE,
                true
        );
        assertEquals(1, eventData.size());
    }

    @Test
    public void test1DeleteEvent() {
        calendarHelper.deleteEvent(new DeleteEventRequest(
                TEST_CHAT_ID,
                new EventData(
                        TEST_EVENT_TITLE,
                        new Date(startTime),
                        new Date(endTime),
                        TEST_EVENT_VENUE,
                        TEST_EVENT_NOTE
                )
        ));
        List<EventData> eventData = calendarHelper.getEvents(
                TEST_EVENT_TITLE,
                new Date(startTime),
                new Date(endTime),
                TEST_EVENT_VENUE,
                TEST_EVENT_NOTE,
                true
        );
        assertEquals(0, eventData.size());
    }

}
