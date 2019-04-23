package edu.sutd.organice;

import org.drinkless.td.libcore.telegram.TdApi;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class ActionRequestTest {
    private long chatId;
    private TdApi.Message message;
    private ActionRequest expectedRequest;
    private Class expectedException;

    public ActionRequestTest(long chatId, String message, ActionRequest expectedRequest, Class exception) {
        this.chatId = chatId;
        this.message = makeTextMessage(message, chatId);
        this.expectedRequest = expectedRequest;
        this.expectedException = exception;
    }

    private TdApi.Message makeTextMessage(String s, long chatId) {
        TdApi.Message message = new TdApi.Message();
        message.content = new TdApi.MessageText();
        message.isOutgoing = true;
        ((TdApi.MessageText) message.content).text = new TdApi.FormattedText();
        ((TdApi.MessageText) message.content).text.text = s;
        message.chatId = chatId;
        return message;
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
                new Object[] {  //new 0
                        3142,
                        "Hello there this is just a sample text, \n" +
                                "we are using this text for unit testing...\n" +
                                "# organice new\n" +
                                "Title: just some example title\n" +
                                "Note: this one has no venue!!!\n" +
                                "Start: 07/01/1997 00:00\n" +
                                "End: 31/12/1999 00:00\n" +
                                "# end organice\n" +
                                "more sample text here~~~",
                        new NewEventRequest(
                                3142,
                                new EventData(
                                        "just some example title",
                                        makeDate(1997, 1, 7),
                                        makeDate(1999, 12, 31),
                                        "",
                                        "this one has no venue!!!"
                                )
                        ),
                        null
                },
                new Object[]{   //new 1
                        123,
                        "Hello there this is just a sample text, \n" +
                                "we are using this text for unit testing...\n" +
                                "# organice new\n" +
                                "Title: test two title\n" +
                                "Note: no #end statement\n" +
                                "Start: 07/01/1997 00:00\n" +
                                "End: 31/12/1999 00:00\n" +
                                "# end organice\n",
                        new NewEventRequest(
                                123,
                                new EventData(
                                        "test two title",
                                        makeDate(1997, 1, 7),
                                        makeDate(1999, 12, 31),
                                        "",
                                        "no #end statement"
                                )
                        ),
                        null
                },
                new Object[]{   //new 2
                        69,
                        "Hello there this is just a sample text, \n" +
                                "we are using this text for unit testing...\n" +
                                "# organice new\n" +
                                "Note: no title\n" +
                                "Start: 07/01/1997 00:00\n" +
                                "End: 31/12/1999 00:00\n" +
                                "# end organice\n" +
                                "more sample text here~~~",
                        new NewEventRequest(
                                69,
                                new EventData(
                                        "", //test three
                                        makeDate(1997, 1, 7),
                                        makeDate(1999, 12, 31),
                                        "",
                                        "no title"
                                )
                        ),
                        null
                },
                new Object[] {  //new 3
                        12345,
                        "# organice new\n" +
                                "Title: just some example title\n" +
                                "Note: purely the creation of new event\n" +
                                "Start: 07/01/1997 00:00\n" +
                                "End: 31/12/1999 00:00\n" +
                                "# end organice\n",
                        new NewEventRequest(
                                12345,
                                new EventData(
                                        "just some example title",
                                        makeDate(1997, 1, 7),
                                        makeDate(1999, 12, 31),
                                        "",
                                        "purely the creation of new event"
                                )
                        ),
                        null

                },
                new Object[] { //new 4
                        3142,
                        "Hello there this is just a sample text, \n" +
                                "we are using this text for unit testing...\n" +
                                "# organice new\n" +
                                "Title: just some example title\n" +
                                "this one has no venue!!!\n" +
                                "Start: 07/01/1997 00:00\n" +
                                "End: 31/12/1999 00:00\n" +
                                "# end organice\n" +
                                "more sample text here~~~",
                        new NewEventRequest(
                                3142,
                                new EventData(
                                        "just some example title",
                                        makeDate(1997, 1, 7),
                                        makeDate(1999, 12, 31),
                                        "",
                                        "this one has no note!!!"
                                )
                        ),
                        ParseException.class
                },
                new Object[] {  //new5
                        3142,
                        "Hello there this is just a sample text, \n" +
                                "we are using this text for unit testing...\n" +
                                "# organice new\n" +
                                "Title: just some example title\n" +
                                "Extra: this one has no venue!!!\n" +
                                "Start: 07/01/1997 00:00\n" +
                                "End: 31/12/1999 00:00\n" +
                                "# end organice\n" +
                                "more sample text here~~~",
                        new NewEventRequest(
                                3142,
                                new EventData(
                                        "just some example title",
                                        makeDate(1997, 1, 7),
                                        makeDate(1999, 12, 31),
                                        "",
                                        "this one has note called extra!!!"
                                )
                        ),
                        ParseException.class
                },
                new Object[] { //new 6
                        3142,
                        "Hello there this is just a sample text, \n" +
                                "we are using this text for unit testing...\n" +
                                "# organice new\n" +
                                "Title: just some example title\n" +
                                "Note: this one has no start!!!\n" +
                                "07/01/1997 00:00\n" +
                                "End: 31/12/1999 00:00\n" +
                                "# end organice\n" +
                                "more sample text here~~~",
                        new NewEventRequest(
                                3142,
                                new EventData(
                                        "just some example title",
                                        makeDate(1997, 1, 7),
                                        makeDate(1999, 12, 31),
                                        "",
                                        "this one has no start!!!"
                                )
                        ),
                        ParseException.class
                },
                new Object[] { //delete 1
                        3142,
                        "Hello there this is just a sample text, \n" +
                                "we are using this text for unit testing...\n" +
                                "# organice delete\n" +
                                "Title: just some example title\n" +
                                "Note: this one has no venue!!!\n" +
                                "Start: 07/01/1997 00:00\n" +
                                "End: 31/12/1999 00:00\n" +
                                "# end organice\n" +
                                "more sample text here~~~",
                        new DeleteEventRequest(
                                3142,
                                new EventData(
                                        "just some example title",
                                        makeDate(1997, 1, 7),
                                        makeDate(1999, 12, 31),
                                        "",
                                        "this one has no venue!!!"
                                )
                        ),
                        null

                },
                new Object[] {  //delete 2
                        3142,
                        "Hello there this is just a sample text, \n" +
                                "we are using this text for unit testing...\n" +
                                "# organice delete\n" +
                                "Note: this one has no venue!!!\n" +
                                "Start: 07/01/1997 00:00\n" +
                                "End: 31/12/1999 00:00\n" +
                                "# end organice\n" +
                                "more sample text here~~~",
                        new DeleteEventRequest(
                                3142,
                                new EventData(
                                        "",
                                        makeDate(1997, 1, 7),
                                        makeDate(1999, 12, 31),
                                        "",
                                        "this one has no venue!!!"
                                )
                        ),
                        null

                },
                new Object[] { //delete 3
                        3142,
                        "Hello there this is just a sample text, \n" +
                                "we are using this text for unit testing...\n" +
                                "# organice delete\n" +
                                "Title: just some example title\n" +
                                "this one has no venue!!!\n" +
                                "Start: 07/01/1997 00:00\n" +
                                "End: 31/12/1999 00:00\n" +
                                "# end organice\n" +
                                "more sample text here~~~",
                        new DeleteEventRequest(
                                3142,
                                new EventData(
                                        "just some example title",
                                        makeDate(1997, 1, 7),
                                        makeDate(1999, 12, 31),
                                        "",
                                        "this one has no note!!!"
                                )
                        ),
                        ParseException.class

                },
                new Object[] { //delete 1
                        3142,
                        "Hello there this is just a sample text, \n" +
                                "we are using this text for unit testing...\n" +
                                "# organice delete\n" +
                                "Title: just some example title\n" +
                                "Extra: this one has no venue!!!\n" +
                                "Start: 07/01/1997 00:00\n" +
                                "End: 31/12/1999 00:00\n" +
                                "# end organice\n" +
                                "more sample text here~~~",
                        new DeleteEventRequest(
                                3142,
                                new EventData(
                                        "just some example title",
                                        makeDate(1997, 1, 7),
                                        makeDate(1999, 12, 31),
                                        "",
                                        "this one has no note as extra!!!"
                                )
                        ),
                        ParseException.class

                },
                new Object[] {
                        12345,
                        "# organice list\n",    //test function list
                        new ListEventsRequest((long) 12345),
                        null
                },
                new Object[] {
                        12345,
                        "# organice test\n",    //test for non-existing function
                        null,
                        null
                },
        };
        return Arrays.asList(parameters);
    }

    @Test
    public void parseMessage() {
        try {
            ActionRequest request = ActionRequest.parseMessage(message);
            assertEquals(expectedRequest, request);
            if (expectedException != null) {
                fail("expected exception " + expectedException.toString());
            }
        } catch (Exception e) {
            assertEquals(e.getClass(), expectedException);
        }
    }
}
