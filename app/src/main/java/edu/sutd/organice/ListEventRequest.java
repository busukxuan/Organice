package edu.sutd.organice;

public class ListEventRequest extends ActionRequest{
    public final long chatId;

    ListEventRequest(Long chatID){
        this.chatId = chatID;
    }

    @Override
    public String toString(){
        return "NewEventRequest(" +Long.toString(chatId)+")";
    }
}
