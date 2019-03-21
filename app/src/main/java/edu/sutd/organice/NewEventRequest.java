package edu.sutd.organice;

public class NewEventRequest extends ActionRequest {
    final long chatId;
    final EventData eventData;

    NewEventRequest(long chatId, EventData eventData) {
        this.chatId = chatId;
        this.eventData = eventData;
    }

    @Override
    public String toString() {
        return "NewEventRequest(" +
                eventData.toString() +
                "\")";
    }
}