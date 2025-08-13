package com.lyh.reversi_online.service.game.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lyh.reversi_online.dao.IDataAccess;
import com.lyh.reversi_online.pojo.GameData;
import com.lyh.reversi_online.pojo.LobbyData;
import com.lyh.reversi_online.pojo.PlayerData;
import com.lyh.reversi_online.service.game.IGameService;
import com.lyh.reversi_online.service.game.IGameThreadManager;
import com.lyh.reversi_online.service.player.IPlayerManagement;
import com.lyh.reversi_online.websocket.IWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GameService implements IGameService {
    private final IPlayerManagement playerManagement;
    private final IGameThreadManager gameThreadManager;
    private final IDataAccess redisAccess;
    private final IWebSocketHandler webSocketHandler;
    private final ArrayList queueList = new ArrayList();

    @Autowired
    public GameService(IPlayerManagement playerManagement, @Lazy IGameThreadManager gameThreadManager, @Lazy IDataAccess redisAccess, IWebSocketHandler webSocketHandler) {
        this.playerManagement = playerManagement;
        this.gameThreadManager = gameThreadManager;
        this.redisAccess = redisAccess;
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public LobbyData enterLobby(PlayerData playerData) {
        // 1.先更新「玩家數據」中的遊戲狀態。
        playerManagement.updateGameStatus(playerData, "lobby");

        // 2.獲取「待機室」所需數據。
        return getLobbyData();
    }

    @Override
    public LobbyData getLobbyData() {
        return redisAccess.getLobbyData();
    }

    @Override
    public boolean gameIfExist(String game_id) {
        return gameThreadManager.gameIfExist(game_id);
    }

    @Override
    public void joinQueue(String player_uuid) {
        queueList.add(player_uuid);

        if (queueList.size() >= 2) {
            String playerUUID_One = (String) queueList.remove(0);
            String playerUUID_Two = (String) queueList.remove(0);
            gameThreadManager.createGameThread(playerUUID_One, playerUUID_Two);
        }
    }

    @Override
    public void cancleQueue(String player_uuid) {
        queueList.remove(player_uuid);
    }

    @Override
    public void setGameData(GameData gameData) {
        redisAccess.setGameData(gameData);
    }

    @Override
    public GameData getGameData(String game_id) {
        return redisAccess.getGameData(game_id);
    }

    @Override
    public void deleteGameData(String game_id) {
        redisAccess.deleteGameData(game_id);
    }

    @Override
    public void surrender(String player_uuid) throws JsonProcessingException {
        PlayerData playerData = playerManagement.getPlayerData(player_uuid);

        // 判斷該玩家所在的對局是否還存在，若存在則中斷對局。
        if (gameThreadManager.gameIfExist(playerData.getGame_status().split(":")[1])) {
            // System.out.println("存在");

            // 判斷是否為對局中的棋手，而非觀戰者。
            GameData gameData = getGameData(playerData.getGame_status().split(":")[1]);
            if (gameData.getBlack_uuid().equals(player_uuid) || gameData.getWhite_uuid().equals(player_uuid)) {
                gameThreadManager.surrender(playerData);
            }
        }
    }

    @Override
    public void reconnect(String player_uuid) throws IOException {
        // 1.獲取「玩家數據」。
        PlayerData playerData = playerManagement.getPlayerData(player_uuid);

        // 2.獲取「對局」所需數據。
        String game_id = playerData.getGame_status().split(":")[1];
        GameData gameData = getGameData(game_id);

        // 3.更改「對局」連線狀態並更新 Redis。
        if (gameData.getBlack_uuid().equals(player_uuid)) {
            // 黑方
            gameData.setBlack(gameData.getBlack().split(":")[0]);
        } else if (gameData.getWhite_uuid().equals(player_uuid)) {
            // 白方
            gameData.setWhite(gameData.getWhite().split(":")[0]);
        }
        setGameData(gameData);

        // 4.返回新的「對局」數據給該對局中的所有人。
        // 獲取所有位於該對局且在線的玩家列表。
        List<PlayerData> playerDataList = redisAccess.getPlayerDataFromGame_id(gameData.getGame_id());

        // 廣播新的「對局」數據給位於該對局的其它玩家。
        // 只需要玩家 id。
        ArrayList<String> playerUUIDList = new ArrayList<>();
        for (int i = 0; i < playerDataList.size(); ++i) {
            playerUUIDList.add(playerDataList.get(i).getPlayer_uuid());
        }
        webSocketHandler.broadcast(playerUUIDList, "render_data:GameData", gameData);

        // 重啟或停止該對局中的計時器。
        gameThreadManager.resetOrStopTimer(game_id, player_uuid);
    }
}