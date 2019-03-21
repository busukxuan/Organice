package edu.sutd.organice;

public class ListEventsRequest extends ActionRequest {

    public final long chatId;

    ListEventsRequest(Long chatID) {
        this.chatId = chatID;
    }

    @Override
    public void execute(CalendarHelper calendarHelper, TDHelper tdHelper) {
        EventData[] eventData = calendarHelper.getNextEvents();
        String[] eventLines = new String[5];
        for (int i = 0; i < 5; i++) {
            eventLines[i] = Integer.toString(i+1) + ". " + eventData[i].toTextLine();
        }
        tdHelper.sendMessage(chatId, String.join("\n", eventLines));
    }

    @Override
    public String toString(){
        return "NewEventRequest(" + Long.toString(chatId) + ")";
    }
}
