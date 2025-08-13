package com.lyh.reversi_online.service.game;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lyh.reversi_online.pojo.PlayerData;

public interface IGameThreadManager {
    // 查詢 gameThreadManager 中用於管理對局的 GameManager 中是否存在該對局。
    public boolean gameIfExist(String game_id);

    // 建立對局執行緒。
    public void createGameThread(String playerUUID_One, String playerUUID_Two);

    // 中斷對局。
    public void surrender(PlayerData playerData) throws JsonProcessingException;

    // 標記玩家斷線。
    public void recordDisconnectedPlayer(String game_id, String player_uuid);

    // 重置或停止該對局的計時器。
    public void resetOrStopTimer(String game_id, String player_uuid);

    // 玩家落子。
    public void playerMove(String gameId, String player_uuid, String position);
}