package com.lyh.reversi_online.websocket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lyh.reversi_online.pojo.ChatData;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMapper {
    private ChatData message;

    public ChatMapper() {
    }

    public ChatMapper(ChatData message) {
        this.message = message;
    }

    public ChatData getMessage() {
        return message;
    }

    public void setMessage(ChatData message) {
        this.message = message;
    }
}