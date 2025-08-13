package com.lyh.reversi_online.websocket.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyh.reversi_online.pojo.PlayerData;
import com.lyh.reversi_online.service.chat.ChatSystem;
import com.lyh.reversi_online.service.game.impl.GameThreadManager;
import com.lyh.reversi_online.service.player.IPlayerManagement;
import com.lyh.reversi_online.websocket.*;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

@Component
public class WebSocketHandler extends TextWebSocketHandler implements IWebSocketHandler {
    private final WebSocketSessionManager webSocketSessionManager;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final IPlayerManagement playerManagement;
    private final HeartbeatManager heartbeatManager;
    private final ChatSystem chatSystem;
    private final GameThreadManager gameThreadManager;
    @Resource(name = "broadcastExecutor")
    private ThreadPoolTaskExecutor broadcastExecutor;

    @Autowired
    public WebSocketHandler(WebSocketSessionManager webSocketSessionManager, @Lazy IPlayerManagement playerManagement, @Lazy HeartbeatManager heartbeatManager, @Lazy ChatSystem chatSystem, @Lazy GameThreadManager gameThreadManager) {
        this.webSocketSessionManager = webSocketSessionManager;
        this.playerManagement = playerManagement;
        this.heartbeatManager = heartbeatManager;
        this.chatSystem = chatSystem;
        this.gameThreadManager = gameThreadManager;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println(" O - WsConnected: " + session.getId());

        // 1.將 player_uuid、nick_name 和 WebSocket 的 Session 用 Map 儲存，以便後續使用。
        Map<String, Object> attributes = session.getAttributes();
        String cookie_player_uuid = (String) attributes.get("player_uuid");
        webSocketSessionManager.addSession(cookie_player_uuid, session);
        webSocketSessionManager.addSession(session, cookie_player_uuid);

        // 2.獲取「玩家數據」。
        PlayerData playerData = playerManagement.getPlayerData(cookie_player_uuid);

        // 3.更新「玩家數據」中的連線狀態為 online。
        String cookie_nick_name = (String) attributes.get("nick_name");
        playerData = playerManagement.setPlayerData(cookie_player_uuid, cookie_nick_name, playerData);

        // 4.檢查「玩家數據」中的遊戲狀態，用於判斷該玩家要帶入待機室或啟動斷線重連。
        playerManagement.checkGameStatus(playerData);

        // 將該玩家 WebSocket Session 註冊心跳。
        heartbeatManager.registerSession(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        // System.out.println("WsReceived: " + payload);

        ObjectMapper mapper = new ObjectMapper();
        WsMapper wsMapper = mapper.readValue(payload, WsMapper.class);

        switch (wsMapper.getType()) {
            case "ping":
                heartbeatManager.updateHeartbeat(session);
                // System.out.println("心跳更新 - ws_id: " + session.getId());
                break;
            case "chat_message":
                ChatMapper chatMapper = mapper.readValue(payload, ChatMapper.class);
                chatSystem.broadcastMessage(chatMapper.getMessage());
                // System.out.println("收到聊天訊息 - ws_id: " + session.getId());
                break;
            case "game_move":
                GameMoveMapper gameMoveMapper = mapper.readValue(payload, GameMoveMapper.class);

                // 獲取 player_uuid。
                Map<String, Object> attributes = session.getAttributes();
                String cookie_player_uuid = (String) attributes.get("player_uuid");
                gameThreadManager.playerMove(gameMoveMapper.getMessage().getGame_id(), cookie_player_uuid, gameMoveMapper.getMessage().getPosition());
                break;
            default:
                // System.out.println("switch 無效");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws JsonProcessingException {
        System.out.println(" X - WsDisconnected: " + session.getId());

        // 關閉連線時，移除心跳。
        heartbeatManager.removeSession(session);

        // 當 ws 中斷時。
        // 處理玩家斷線。
        Map<String, Object> attributes = session.getAttributes();
        String cookie_player_uuid = (String) attributes.get("player_uuid");
        playerManagement.playerDisconnected(cookie_player_uuid);
    }

    @Override
    public void broadcast(ArrayList<String> target, String type, Object message) throws JsonProcessingException {
        String message_json = objectMapper.writeValueAsString(new SendObj(type, message));
        TextMessage message_text = new TextMessage(message_json);

        for (int i = 0; i < target.size(); ++i) {
            WebSocketSession session = webSocketSessionManager.getSession(target.get(i));
            if (session.isOpen()) {
                // 利用子執行緒來非同步廣播。
                broadcastExecutor.submit(() -> {
                    try {
                        session.sendMessage(message_text);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    @Override
    public void broadcast(String player_uuid, String type, Object message) throws IOException {
        String message_json = objectMapper.writeValueAsString(new SendObj(type, message));
        TextMessage message_text = new TextMessage(message_json);

        WebSocketSession session = webSocketSessionManager.getSession(player_uuid);
        if (session.isOpen()) {
            session.sendMessage(message_text);
        }
    }
}