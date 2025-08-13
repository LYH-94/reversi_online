package com.lyh.reversi_online.service.player;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lyh.reversi_online.pojo.PlayerData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public interface IPlayerManagement {
    // 檢查用戶的 cookie 中是否已經攜帶了 player_uuid，若沒有則生成新的並添加到 cookie 中。
    public void checkUUID(String player_uuid, HttpServletRequest req, HttpServletResponse resp);

    // 獲取「玩家數據」。
    public PlayerData getPlayerData(String player_uuid);

    // 儲存/更新「玩家數據」。
    public PlayerData setPlayerData(String player_uuid, String nick_name, PlayerData playerData);

    // 檢查「玩家數據」中的遊戲狀態。
    public void checkGameStatus(PlayerData playerData) throws IOException;

    // 更新「玩家數據」中的遊戲狀態。
    public PlayerData updateGameStatus(PlayerData playerData, String game_status);

    // 獲取所有處於該對局且在線的玩家列表。
    public List<PlayerData> getPlayerDataFromGame_id(String game_id);

    // 處理玩家斷線。
    public void playerDisconnected(String player_uuid) throws JsonProcessingException;

    // 根據玩家對局狀態獲取玩家列表。
    public List<PlayerData> getPlayerDataFromGameStatus(String gameStatus);

    public void getLobbyDataAndBroadcast() throws JsonProcessingException;
}