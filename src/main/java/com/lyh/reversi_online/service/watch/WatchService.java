package com.lyh.reversi_online.service.watch;

import com.lyh.reversi_online.pojo.GameData;
import com.lyh.reversi_online.pojo.PlayerData;
import com.lyh.reversi_online.service.game.IGameService;
import com.lyh.reversi_online.service.player.IPlayerManagement;
import com.lyh.reversi_online.websocket.IWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class WatchService {
    private final IPlayerManagement playerManagement;
    private final IGameService gameService;
    private final IWebSocketHandler webSocketHandler;

    @Autowired
    public WatchService(IPlayerManagement playerManagement, IGameService gameService, IWebSocketHandler webSocketHandler) {
        this.playerManagement = playerManagement;
        this.gameService = gameService;
        this.webSocketHandler = webSocketHandler;
    }

    public void joinWatch(String player_uuid, String game_id) throws IOException {
        // 1.獲取「玩家數據」
        PlayerData playerData = playerManagement.getPlayerData(player_uuid);

        // 2.更改遊戲狀態並更新 Redis 中的「玩家數據」
        playerManagement.updateGameStatus(playerData, "watching:" + game_id);

        // 3.獲取對局數據。
        GameData gameData = gameService.getGameData(game_id);

        // 4.廣播對局數據給該觀戰玩家。
        webSocketHandler.broadcast(player_uuid, "render_data:GameData", gameData);
    }
}