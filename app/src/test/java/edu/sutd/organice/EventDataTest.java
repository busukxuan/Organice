package edu.sutd.organice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class EventDataTest {

    private String title;
    private int[] start;
    private int[] end;
    private String venue;
    private String note;
    private static Class expectedException;

    public EventDataTest(String title, int[] start, int[] end, String venue, String note, Class exception) {
        this.title = title;
        this.start = start;
        this.end = end;
        this.venue = venue;
        this.note = note;
        this.expectedException = exception;
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
                        "this one has no venue!!!",
                        null
                },

                new Object[] {
                        "",
                        new int[] {1997, 0, 7},
                        new int[] {1990, 2, 31},
                        "a random place\n_!#*+?/",
                        "event is past-due; wrong dateEnd; dateEnd is before dateStart",
                        null
                },

                new Object[] {
                        null,
                        new int[] {1997, 1, 7},
                        new int[] {1999, 12, 31},
                        "a mysterious place",
                        "this event has no title",
                        null
                },

                new Object[] {
                        null,
                        new int[] {1997, 1, 7},
                        null,
                        null,
                        "missing one Date",
                        NullPointerException.class
                },

                new Object[] {
                        "a title",
                        new int[] {1997, 1},
                        new int[] {1999, 12, 31},
                        "venue",
                        "",
                        ArrayIndexOutOfBoundsException.class
                },


        };
        return Arrays.asList(parameters);
    }
/*
    @Test
    public void parseMessage() {
        try {
        EventData e1 = new EventData(title, makeDate(start[0], start[1], start[2]), makeDate(end[0], end[1], end[2]), venue, note);
        EventData e2 = new EventData(title, makeDate(start[0], start[1], start[2]), makeDate(end[0], end[1], end[2]), venue, note);
        assertEquals(e1, e2);
        }

        catch(NullPointerException e) {
            fail("Date cannot be empty");
        }

        catch (ArrayIndexOutOfBoundsException e2) {
            fail("missing input(s) for Date");
        }
        

    }*/

    @Test
    public void toTextLine() {
        try {
            EventData e1 = new EventData (title, makeDate(start[0], start[1], start[2]), makeDate(end[0], end[1], end[2]), venue, note);
            EventData e2 = new EventData(title, makeDate(start[0], start[1], start[2]), makeDate(end[0], end[1], end[2]), venue, note);
            String s1 = e1.toTextLine();
            String s2 = e2.toTextLine();
            assertEquals(s1,s2);
            if(expectedException != null){
                fail("expected exception " + expectedException.toString());
            }
        }

        catch(Exception exception){
            assertEquals(expectedException, exception.getClass());
        }


    }

}
