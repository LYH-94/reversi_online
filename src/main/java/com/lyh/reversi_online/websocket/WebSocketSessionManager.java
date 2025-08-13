package com.lyh.reversi_online.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionManager {
    private final ConcurrentHashMap<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<WebSocketSession, String> sessionMapForUUID = new ConcurrentHashMap<>();

    // addSession 多形 - 改變參數順序選擇不同函式。
    public void addSession(String player_uuid, WebSocketSession session) {
        sessionMap.put(player_uuid, session);
    }

    public void addSession(WebSocketSession session, String player_uuid) {
        sessionMapForUUID.put(session, player_uuid);
    }

    // getSession 多形 - 改變參數類型選擇不同函式。
    public WebSocketSession getSession(String player_uuid) {
        return sessionMap.get(player_uuid);
    }

    public String getSession(WebSocketSession session) {
        return sessionMapForUUID.get(session);
    }

    public void removeSession(String player_uuid) {
        // 兩邊 Map 都要刪除。
        sessionMapForUUID.remove(getSession(player_uuid));
        sessionMap.remove(player_uuid);
    }

    @Override
    public String toString() {
        return "WebSocketSessionManager{" +
                "sessionMap=" + sessionMap +
                ", sessionMapForUUID=" + sessionMapForUUID +
                '}';
    }
}