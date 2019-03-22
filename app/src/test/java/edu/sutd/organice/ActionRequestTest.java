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

@RunWith(Parameterized.class)
public class ActionRequestTest {

    private long chatId;
    private TdApi.Message message;
    private ActionRequest expectedRequest;

    public ActionRequestTest(long chatId, String message, ActionRequest expectedRequest) {
        this.chatId = chatId;
        this.message = makeTextMessage(message, chatId);
        this.expectedRequest = expectedRequest;
    }

    public TdApi.Message makeTextMessage(String s, long chatId) {
        TdApi.Message message = new TdApi.Message();
        message.content = new TdApi.MessageText();
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
                new Object[] {
                            3142,
                            "Hello there this is just a sample text, \n" +
                                    "we are using this text for unit testing...\n" +
                                    "# organice new\n" +
                                    "Title: just some example title\n" +
                                    "Note: this one has no venue!!!\n" +
                                    "Start: 07/01/1997\n" +
                                    "End: 31/12/1999\n" +
                                    "# end organice\n" +
                                    "more sample text here~~~",
                            new NewEventRequest(
                                    3142,
                                    new EventData(
                                            "just some example title",
                                            makeDate(1997, 1, 7),
                                            makeDate(1999, 12, 31),
                                            null,
                                            "this one has no venue!!!"
                                    )
                            )

                },
                new Object[]{
                        123,
                        "Hello there this is just a sample text, \n" +
                                "we are using this text for unit testing...\n" +
                                "# organice new\n" +
                                "Title: test two title\n" +
                                "Note: no #end statement\n" +
                                "Start: 07/01/1997\n" +
                                "End: 31/12/1999\n" +
                                "# end organice\n",
                        new NewEventRequest(
                                123,
                                new EventData(
                                        "test two title",
                                        makeDate(1997, 1, 7),
                                        makeDate(1999, 12, 31),
                                        null,
                                        "no #end statement"
                                )
                        )
                },
                new Object[]{
                        69,
                        "Hello there this is just a sample text, \n" +
                                "we are using this text for unit testing...\n" +
                                "# organice new\n" +
                                "Title: just some example title\n" +
                                "Note: no title\n" +
                                "Start: 07/01/1997\n" +
                                "End: 31/12/1999\n" +
                                "# end organice\n" +
                                "more sample text here~~~",
                                new NewEventRequest(
                                        69,
                                        new EventData(
                                                "just some example title", //test three
                                                makeDate(1997, 1, 7),
                                                makeDate(1999, 12, 31),
                                                null,
                                                "no title"
                                        )
                                )
                },
                new Object[] {
                        12345,
                                "# organice new\n" +
                                "Title: just some example title\n" +
                                "Note: purely the creation of new event\n" +
                                "Start: 07/01/1997\n" +
                                "End: 31/12/1999\n" +
                                "# end organice\n",
                        new NewEventRequest(
                                12345,
                                new EventData(
                                        "just some example title",
                                        makeDate(1997, 1, 7),
                                        makeDate(1999, 12, 31),
                                        null,
                                        "purely the creation of new event"
                                )
                        )

                },
        };
        return Arrays.asList(parameters);
    }

    @Test
    public void parseMessage() throws ParseException {
        ActionRequest request = ActionRequest.parseMessage(message);
        assertEquals(request,expectedRequest);
    }
}
