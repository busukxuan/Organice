package edu.sutd.organice;

public class DeleteEventRequest extends ActionRequest {

    public final long chatId;
    public final EventData eventData;

    DeleteEventRequest(long chatId, EventData eventData){
        this.chatId = chatId;
        this.eventData = eventData;
    }

    @Override
    public void execute(CalendarHelper calendarHelper, TDHelper tdHelper) throws IllegalStateException {
        // check eventData fields for validity before setting eventData
        if (eventData.title == null) {
            throw new IllegalStateException("eventData.title for event deletion must not be null");
        }
        calendarHelper.deleteEvent(this);
    }

    @Override
    public String toString() {
        return "DeleteEventRequest(" +
                eventData.toString() +
                ")";
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof DeleteEventRequest) {
            DeleteEventRequest castedOther = (DeleteEventRequest) other;
            return chatId == castedOther.chatId &&
                    eventData.equals(castedOther.eventData);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (int) chatId + eventData.hashCode();
    }
}
