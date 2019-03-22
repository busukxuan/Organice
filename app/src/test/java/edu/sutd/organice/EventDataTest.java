package edu.sutd.organice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class EventDataTest {

    private String title;
    private int[] start;
    private int[] end;
    private String venue;
    private String note;

    public EventDataTest(String title, int[] start, int[] end, String venue, String note) {
        this.title = title;
        this.start = start;
        this.end = end;
        this.venue = venue;
        this.note = note;
    }

    private static Date makeDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(year, month-1, day, 0, 0, 0);
        return cal.getTime();
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        Object[][] parameters = new Object[][] {
                new Object[] {
                        "just some example title",
                        new int[] {1997, 1, 7},
                        new int[] {1999, 12, 31},
                        null,
                        "this one has no venue!!!"
                }
        };
        return Arrays.asList(parameters);
    }

    @Test
    public void parseMessage() {
        EventData e1 = new EventData(title, makeDate(start[0], start[1], start[2]), makeDate(end[0], end[1], end[2]), venue, note);
        EventData e2 = new EventData(title, makeDate(start[0], start[1], start[2]), makeDate(end[0], end[1], end[2]), venue, note);
        assertEquals(e1, e2);
    }


    @Test
    public void toTextLine() {
        EventData e1 = new EventData(title, makeDate(start[0], start[1], start[2]), makeDate(end[0], end[1], end[2]), venue, note);
        EventData e2 = new EventData(title, makeDate(start[0], start[1], start[2]), makeDate(end[0], end[1], end[2]), venue, note);
        String s1 = e1.toTextLine();
        String s2 = e2.toTextLine();
        assertEquals(s1,s2);
    }

}
