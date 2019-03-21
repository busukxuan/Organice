package edu.sutd.organice;

public class ListEventsRequest extends ActionRequest {
    public final long chatId;

    ListEventsRequest(Long chatID) {
        this.chatId = chatID;
    }

    @Override
    public String toString(){
        return "NewEventRequest(" +Long.toString(chatId)+")";
    }
}
