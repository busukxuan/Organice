package edu.sutd.organice;

public class NewEventRequest extends ActionRequest {
    final long chatId;
    final EventData eventData;

    NewEventRequest(long chatId, EventData eventData) {
        this.chatId = chatId;

        // check eventData fields for validity before setting eventData
        if (eventData.title == null) {
            throw new IllegalArgumentException("eventData.title for new event must not be null");
        } else if (eventData.dateStart == null) {
            throw new IllegalArgumentException("eventData.dateStart for new event must not be null");
        } else if (eventData.dateEnd == null) {
            throw new IllegalArgumentException("eventData.dateStart for new event must not be null");
        } else {
            this.eventData = eventData;
        }
    }

    @Override
    public String toString() {
        return "NewEventRequest(" +
                eventData.toString() +
                "\")";
    }
}