package com.lyh.reversi_online.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.util.ArrayList;

public interface IWebSocketHandler {
    // 多人廣播
    public void broadcast(ArrayList<String> target, String type, Object message) throws JsonProcessingException;

    // 單人通知
    public void broadcast(String player_uuid, String type, Object message) throws IOException;
}