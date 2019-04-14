package edu.sutd.organice;

import android.content.SharedPreferences;
import android.util.Log;

import org.drinkless.td.libcore.telegram.TdApi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class represents a request for the app to perform some actions,
 * parsed from input in the form of Telegram messages.
 * <p>
 *     This is an abstract class, with each subclass representing a particular kind of action.
 *     Subclasses include:
 *     <ul>
 *         <li>{@link NewEventRequest}</li>
 *         <li>{@link DeleteEventRequest}</li>
 *         <li>{@link ListEventsRequest}</li>
 *     </ul>
 *
 *     This class also provides a method for parsing Telegram messages into ActionRequest objects.
 * </p>
 */
public abstract class ActionRequest {

    private static final String LOG_TAG = "ActionRequest";

    /**
     * Parse a Telegram message into an {@link ActionRequest} object, or null if no action requested.
     * @param message The Telegram message to be parsed.
     * @return The {@link ActionRequest} object obtained from the given message, or null.
     * @throws ParseException Malformed event data text, when event data is expected.
     */
    public static ActionRequest parseMessage(TdApi.Message message) throws ParseException {

        Log.d(LOG_TAG, "parsing message");
        String text = ((TdApi.MessageText) message.content).text.text;

        // check each line for command tag
        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i++) {

            if (lines[i].equals("# organice new")) {
                EventData eventData = parseEventData(lines, i + 1);
                return new NewEventRequest(message.chatId, eventData);

            } else if (lines[i].equals("# organice delete")) {
                EventData eventData = parseEventData(lines, i + 1);
                return new DeleteEventRequest(message.chatId, eventData);

            } else if (lines[i].equals("# organice list")) {
                return new ListEventsRequest(message.chatId);
            }
        }
        return null;
    }

    /**
     * Parse event data formatted text in a message into an {@link EventData} object.
     * @param lines The lines containing the event data text.
     * @param startLine The index of the line at which event data format begins.
     * @return An {@link EventData} object obtained from the formatted text.
     * @throws ParseException Malformed event data text.
     */
    private static EventData parseEventData(String[] lines, int startLine) throws ParseException {
        // `EventData` fields are empty by default
        String title = "";
        Date start = null;
        Date end = null;
        String venue = "";
        String note = "";

        // read data fields line by line, until closing tag "# end organice" is reached
        for(
                int lineIndex = startLine;
                lineIndex < lines.length && !lines[lineIndex].equals("# end organice");
                lineIndex++
        ) {
            String line = lines[lineIndex];
            // get the event data field name, i.e. everything before the first colon
            int colonIndex = line.indexOf(":");
            if (colonIndex < 0) {
                throw new ParseException("no colon in line", -1);
            }
            String field = line.substring(0, colonIndex).trim();
            String data = line.substring(colonIndex+1).trim();
            // assign corresponding data for each field
            switch (field) {
                case "Title":
                    title = data;
                    break;
                case "Start":
                    start = new SimpleDateFormat("dd/MM/yyyy").parse(data);
                    break;
                case "End":
                    end = new SimpleDateFormat("dd/MM/yyyy").parse(data);
                    break;
                case "Venue":
                    venue = data;
                    break;
                case "Note":
                    note = data;
                    break;
                default:
                    throw new ParseException("unexpected field name: \"" + field + "\"", -1);
            }
        }
        return new EventData(title, start, end, venue, note);
    }

    /**
     * Execute this action request.
     * @param preferences User preferences to inform the execution of this request.
     * @param calendarHelper Helper object for accessing the phone calendar.
     * @param tdHelper Helper object for sending messages, used by {@link ListEventsRequest} objects.
     */
    public abstract void execute(SharedPreferences preferences, CalendarHelper calendarHelper, TDHelper tdHelper);

    /**
     * Execute the given action request.
     * @param preferences User preferences to inform the execution of this request.
     * @param calendarHelper Helper object for accessing the phone calendar.
     * @param tdHelper Helper object for sending messages, used by {@link ListEventsRequest} objects.
     * @param request The action request to be executed.
     */
    public static void execute(SharedPreferences preferences, CalendarHelper calendarHelper, TDHelper tdHelper, ActionRequest request) {
        Log.d(LOG_TAG, "executing request");
        request.execute(preferences, calendarHelper, tdHelper);
    }

    /**
     * Execute the action requested by a given Telegram message.
     * @param preferences User preferences to inform the execution of this request.
     * @param calendarHelper Helper object for accessing the phone calendar.
     * @param tdHelper Helper object for sending messages, used by {@link ListEventsRequest} objects.
     * @param message The message from which an action request is to be obtained and executed.
     * @throws ParseException Malformed event data text, when event data is expected.
     */
    public static void execute(SharedPreferences preferences, CalendarHelper calendarHelper, TDHelper tdHelper, TdApi.Message message) throws ParseException {
        ActionRequest request = parseMessage(message);
        if (request != null) {
            execute(preferences, calendarHelper, tdHelper, request);
        }
    }

    @Override
    public abstract boolean equals(Object other);

    @Override
    public abstract int hashCode();
}
