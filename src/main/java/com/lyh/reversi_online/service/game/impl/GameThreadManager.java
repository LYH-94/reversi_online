package com.lyh.reversi_online.service.game.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lyh.reversi_online.dao.IDataAccess;
import com.lyh.reversi_online.pojo.GameData;
import com.lyh.reversi_online.pojo.PlayerData;
import com.lyh.reversi_online.service.game.GameManager;
import com.lyh.reversi_online.service.game.GameThread;
import com.lyh.reversi_online.service.game.IGameService;
import com.lyh.reversi_online.service.game.IGameThreadManager;
import com.lyh.reversi_online.service.player.IPlayerManagement;
import com.lyh.reversi_online.util.IdUtil;
import com.lyh.reversi_online.websocket.IWebSocketHandler;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Service
public class GameThreadManager implements IGameThreadManager {
    private final IdUtil idUtil;
    private final GameManager gameManager;
    private final IPlayerManagement playerManagement;
    private final IGameService gameService;
    private final IWebSocketHandler webSocketHandler;
    private final IDataAccess redisAccess;

    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    public GameThreadManager(IdUtil idUtil, GameManager gameManager, IPlayerManagement playerManagement, IGameService gameService, IWebSocketHandler webSocketHandler, IDataAccess redisAccess) {
        this.idUtil = idUtil;
        this.gameManager = gameManager;
        this.playerManagement = playerManagement;
        this.gameService = gameService;
        this.webSocketHandler = webSocketHandler;
        this.redisAccess = redisAccess;
    }

    @Override
    public boolean gameIfExist(String game_id) {
        return gameManager.gameIfExist(game_id);
    }

    @Override
    public void createGameThread(String playerUUID_One, String playerUUID_Two) {
        String gameId = idUtil.generateGameId();

        // 1.先獲取該兩位玩家的「玩家數據」，更新遊戲狀態後於 Redis 更新。
        PlayerData playerData_One = playerManagement.getPlayerData(playerUUID_One);
        PlayerData playerData_Two = playerManagement.getPlayerData(playerUUID_Two);
        playerManagement.updateGameStatus(playerData_One, "ingame:" + gameId);
        playerManagement.updateGameStatus(playerData_Two, "ingame:" + gameId);

        // 2.啟動對局執行緒並儲存起來便於外部管理。
        // 建立回呼邏輯 1：當遊戲結束時，從 GameManager 移除
        Runnable onGameEnd = () -> {
            gameManager.removeGame(gameId);
        };
        // 建立回呼邏輯 2：廣播「對局」數據給該對局中的其它玩家
        Consumer<GameData> onBroadcastGameData = (GameData gameData) -> {
            try {
                // 獲取所有位於該對局且在線的玩家列表。
                List<PlayerData> playerDataList = redisAccess.getPlayerDataFromGame_id(gameData.getGame_id());

                // 廣播新的「對局」數據給位於該對局的其它玩家。
                // 只需要玩家 id。
                ArrayList<String> playerUUIDList = new ArrayList<>();
                for (int i = 0; i < playerDataList.size(); ++i) {
                    playerUUIDList.add(playerDataList.get(i).getPlayer_uuid());
                }
                webSocketHandler.broadcast(playerUUIDList, "render_data:GameData", gameData);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        };
        GameThread gameThread = new GameThread(gameId, playerData_One, playerData_Two, gameService, webSocketHandler, playerManagement, onGameEnd, onBroadcastGameData);
        gameManager.addGame(gameId, gameThread);
        taskExecutor.execute(gameThread);
    }

    @Override
    public void surrender(PlayerData playerData) throws JsonProcessingException {
        // 獲取 gameThread 執行緒物件。
        GameThread gameThread = gameManager.getGame(playerData.getGame_status().split(":")[1]);

        // 外部中斷該對局。
        gameThread.surrender(playerData);
    }

    @Override
    public void recordDisconnectedPlayer(String game_id, String player_uuid) {
        // 根據 game_id 找出對局執行緒物件 GameThread。
        GameThread gameThread = gameManager.getGame(game_id);

        // 標記斷線的玩家並啟動計時器。
        gameThread.recordDisconnectedPlayer(player_uuid);
    }

    @Override
    public void resetOrStopTimer(String game_id, String player_uuid) {
        // 根據 game_id 找出對局執行緒物件 GameThread。
        GameThread gameThread = gameManager.getGame(game_id);

        // 重置或停止計時器。
        gameThread.resetOrStopTimer(player_uuid);
    }

    @Override
    public void playerMove(String gameId, String player_uuid, String position) {
        // 獲取該對局執行緒 GameThread
        GameThread gameThread = gameManager.getGame(gameId);

        // 判斷該玩家是否是該對局中的玩家，而不是觀眾。
        if (gameThread.getPlayerData_One().getPlayer_uuid().equals(player_uuid) ||
                gameThread.getPlayerData_Two().getPlayer_uuid().equals(player_uuid)) {
            // 調用該對局的 playerMove
            GameData gameData = gameThread.playerMove(player_uuid, position);
        }
    }
}