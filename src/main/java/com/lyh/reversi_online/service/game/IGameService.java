package com.lyh.reversi_online.service.game;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lyh.reversi_online.pojo.GameData;
import com.lyh.reversi_online.pojo.LobbyData;
import com.lyh.reversi_online.pojo.PlayerData;

import java.io.IOException;

public interface IGameService {
    // 獲取「待機室數據」
    public LobbyData enterLobby(PlayerData playerData);

    // 查詢對局是否存在。
    public boolean gameIfExist(String game_id);

    // 加入排隊。
    public void joinQueue(String player_uuid);

    // 取消排隊
    public void cancleQueue(String player_uuid);

    // 儲存「對局數據」
    public void setGameData(GameData gameData);

    // 獲取「對局數據」
    public GameData getGameData(String game_id);

    // 獲取「待機室」所需數據
    public LobbyData getLobbyData();

    // 刪除「對局數據」
    public void deleteGameData(String game_id);

    // 投降
    public void surrender(String player_uuid) throws JsonProcessingException;

    // 斷線重連
    public void reconnect(String player_uuid) throws IOException;
}