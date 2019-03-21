package edu.sutd.organice;

import java.util.Date;

public class DeleteEventRequest extends ActionRequest {
    final long chatId;
    final EventData eventData;
    DeleteEventRequest(long chatId, EventData eventData){
        this.chatId=chatId;
        // check eventData fields for validity before setting eventData
        if (eventData.title == null) {
            throw new IllegalArgumentException("eventData.title for new event must not be null");
        }
        this.eventData = eventData;
    }
    @Override
    public String toString() {
        return "DeleteEventRequest(" +
                eventData.toString() +
                "\")";
    }

}
