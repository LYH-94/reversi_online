package com.lyh.reversi_online.service.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lyh.reversi_online.pojo.ChatData;
import com.lyh.reversi_online.pojo.PlayerData;
import com.lyh.reversi_online.service.player.IPlayerManagement;
import com.lyh.reversi_online.websocket.IWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatSystem {
    private final IPlayerManagement playerManagement;
    private final IWebSocketHandler webSocketHandler;

    @Autowired
    public ChatSystem(IPlayerManagement playerManagement, IWebSocketHandler webSocketHandler) {
        this.playerManagement = playerManagement;
        this.webSocketHandler = webSocketHandler;
    }

    public void broadcastMessage(ChatData chatData) throws JsonProcessingException {
        // System.out.println("player:" + chatData.getPlayer());
        // System.out.println("content:" + chatData.getContent());

        // 獲取位於待機室且在線的玩家。
        List<PlayerData> playerFromGameStatus = playerManagement.getPlayerDataFromGameStatus("lobby");

        // 廣播「聊天訊息」給位於待機室的所有玩家。
        // 只需要玩家 id。
        ArrayList<String> playerUUIDList = new ArrayList<>();
        for (int i = 0; i < playerFromGameStatus.size(); ++i) {
            playerUUIDList.add(playerFromGameStatus.get(i).getPlayer_uuid());
        }
        webSocketHandler.broadcast(playerUUIDList, "render_data:ChatData", chatData);
    }
}