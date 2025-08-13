package com.lyh.reversi_online.dao;

import com.lyh.reversi_online.pojo.GameData;
import com.lyh.reversi_online.pojo.LobbyData;
import com.lyh.reversi_online.pojo.PlayerData;

import java.util.List;

public interface IDataAccess {
    // 獲取「玩家數據」。
    public PlayerData getPlayerData(String player_uuid);

    // 儲存/更新「玩家數據」。
    public void setPlayerData(String player_uuid, PlayerData playerData);

    // 獲取「待機室」所需數據。
    public LobbyData getLobbyData();

    // 儲存「對局數據」。
    public void setGameData(GameData gameData);

    // 獲取「對局數據」。
    public GameData getGameData(String game_id);

    // 刪除「對局數據」。
    public void deleteGameData(String game_id);

    // 獲取「對局中的玩家數據」。
    public List<PlayerData> getPlayerDataFromGame_id(String game_id);

    // 根據玩家對局狀態獲取玩家列表。
    public List<PlayerData> getPlayerDataFromGameStatus(String gameStatus);

    // 更新玩家連線狀態
    public void setPlayerConnectionStatus(PlayerData playerData, String connectionStatus);
}
