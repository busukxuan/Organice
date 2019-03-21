package edu.sutd.organice;

import java.util.Date;

public class ListEventRequest extends ActionRequest{
    private long chatId;
    ListEventRequest(Long chatID){

    }
    public long getChatId() {
        return chatId;
    }
    @Override
    public String toString(){
        return "NewEventRequest(" +Long.toString(chatId)+")";
    }
}
