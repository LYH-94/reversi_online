package com.lyh.reversi_online.service.player.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lyh.reversi_online.dao.IDataAccess;
import com.lyh.reversi_online.pojo.GameData;
import com.lyh.reversi_online.pojo.LobbyData;
import com.lyh.reversi_online.pojo.PlayerData;
import com.lyh.reversi_online.service.game.IGameService;
import com.lyh.reversi_online.service.game.IGameThreadManager;
import com.lyh.reversi_online.service.player.IPlayerManagement;
import com.lyh.reversi_online.util.IdUtil;
import com.lyh.reversi_online.websocket.IWebSocketHandler;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlayerManagement implements IPlayerManagement {
    private final IdUtil idUtil;
    private final IDataAccess redisAccess;
    private final IGameService gameService;
    private final IWebSocketHandler webSocketHandler;
    private final IGameThreadManager gameThreadManager;

    @Autowired
    public PlayerManagement(IdUtil idUtil, @Lazy IDataAccess redisAccess, @Lazy IGameService gameService, IWebSocketHandler webSocketHandler, @Lazy IGameThreadManager gameThreadManager) {
        this.idUtil = idUtil;
        this.redisAccess = redisAccess;
        this.gameService = gameService;
        this.webSocketHandler = webSocketHandler;
        this.gameThreadManager = gameThreadManager;
    }

    @Override
    public void checkUUID(String player_uuid, HttpServletRequest req, HttpServletResponse resp) {
        if (player_uuid.equals("null")) {
            String new_player_uuid = idUtil.generatePlayerUUID();

            Cookie cookie = new Cookie("player_uuid", new_player_uuid);
            cookie.setHttpOnly(false); // 如果你要從 JS 傳送，不能設成 true
            cookie.setSecure(false); // 如果你是 ws:// 而非 wss://，不能設成 true
            resp.addCookie(cookie);

            String[] split_player_uuid = new_player_uuid.split("_");
            Cookie cookie_2 = new Cookie("nick_name", split_player_uuid[split_player_uuid.length - 1]);
            cookie_2.setHttpOnly(false); // 如果你要從 JS 傳送，不能設成 true
            cookie_2.setSecure(false); // 如果你是 ws:// 而非 wss://，不能設成 true
            resp.addCookie(cookie_2);
        }
    }

    @Override
    public PlayerData getPlayerData(String player_uuid) {
        PlayerData playerData = redisAccess.getPlayerData(player_uuid);

        return playerData;
    }

    @Override
    public PlayerData setPlayerData(String player_uuid, String nick_name, PlayerData playerData) {
        // 1.判斷玩家數據是否存在。
        if (playerData == null) {
            // 創建新的「玩家數據」。
            PlayerData new_playerData = new PlayerData(0, player_uuid, nick_name, "lobby", "online");
            // 儲存於 Redis。
            redisAccess.setPlayerData(player_uuid, new_playerData);

            return new_playerData;
        } else {
            // 更新「玩家數據」。
            playerData.setConnection_status("online");

            // 更新於 Redis。
            redisAccess.setPlayerData(player_uuid, playerData);

            return playerData;
        }
    }

    @Override
    public void checkGameStatus(PlayerData playerData) throws IOException {
        // 判斷該玩家的遊戲狀態，並返回「待機室」數據或啟動斷線重連。
        if ((playerData.getGame_status().equals("lobby"))
                || ((playerData.getGame_status().split(":")[0]).equals("watching"))) {
            // 通知跳轉至 lobbyPage。
            webSocketHandler.broadcast(playerData.getPlayer_uuid(), "redirect_page:lobbyPage", "{\"page_name\":\"lobbyPage\"}");
        } else if (((playerData.getGame_status().split(":")[0]).equals("ingame"))) {
            // 判斷該對局是否存在。
            boolean gameIfExist = gameService.gameIfExist(playerData.getGame_status().split(":")[1]);

            // 不存在則返回「待機室」數據; 存在則通知玩家跳轉「對局」頁面，並啟動「斷線重連」。
            if (gameIfExist) {
                // 通知跳轉「對局」頁面，並通知瀏覽器端發送「斷線重連」的啟動訊息 true。
                webSocketHandler.broadcast(playerData.getPlayer_uuid(), "redirect_page:gamePage", "{\"page_name\":\"gamePage\", \"reconnect\":\"true\"}");

                // 獲取「待機室」所需數據。
                LobbyData lobbyData = gameService.getLobbyData();

                // 獲取位於「待機室」的所有玩家。
                List<PlayerData> playerFromGameStatus = getPlayerDataFromGameStatus("lobby");

                // 廣播新的「待機室」數據給位於待機室的其它玩家，通知有玩家上線了。
                // 只需要玩家 id。
                ArrayList<String> playerUUIDList = new ArrayList<>();
                for (int i = 0; i < playerFromGameStatus.size(); ++i) {
                    playerUUIDList.add(playerFromGameStatus.get(i).getPlayer_uuid());
                }
                webSocketHandler.broadcast(playerUUIDList, "render_data:LobbyData", lobbyData);
            } else {
                // 這段跟上面 if 中是一樣的。
                // 通知跳轉至 lobbyPage。
                webSocketHandler.broadcast(playerData.getPlayer_uuid(), "redirect_page:lobbyPage", "{\"page_name\":\"lobbyPage\"}");
            }
        }
    }

    @Override
    public PlayerData updateGameStatus(PlayerData playerData, String game_status) {
        playerData.setGame_status(game_status);
        redisAccess.setPlayerData(playerData.getPlayer_uuid(), playerData);
        return playerData;
    }

    @Override
    public List<PlayerData> getPlayerDataFromGame_id(String game_id) {
        return redisAccess.getPlayerDataFromGame_id(game_id);
    }

    @Override
    public List<PlayerData> getPlayerDataFromGameStatus(String gameStatus) {
        return redisAccess.getPlayerDataFromGameStatus(gameStatus);
    }

    @Override
    public void playerDisconnected(String player_uuid) throws JsonProcessingException {
        // 從系統取得目前的日期
        LocalDate currentDate = LocalDate.now();

        // 自訂日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 格式化為字串
        String formattedDate = currentDate.format(formatter);

        // 更新玩家連線狀態
        PlayerData playerData = redisAccess.getPlayerData(player_uuid);
        redisAccess.setPlayerConnectionStatus(playerData, formattedDate);

        // 判斷「玩家數據」中的遊戲狀態
        String game_status = playerData.getGame_status();
        if (game_status.equals("lobby") || game_status.split(":")[0].equals("watching")) {
            // System.out.println("於待機室或觀戰時斷線。");

            // 獲取新的「待機室」數據並廣播給位於待機室的玩家。
            getLobbyDataAndBroadcast();
        } else if (game_status.split(":")[0].equals("ingame")) {
            // System.out.println("於對局中斷線。");

            // 1.獲取 GameData
            GameData gameData = gameService.getGameData(game_status.split(":")[1]);
            // 2.標記 GameData 中該玩家為斷線 (標記在暱稱上)
            // 找出該玩家是黑方還是白方
            if (gameData != null && gameData.getBlack_uuid().equals(player_uuid)) {
                // 黑方
                // 在暱稱上標記「斷線」
                gameData.setBlack(gameData.getBlack() + ":斷線");
                gameService.setGameData(gameData);

                // 於對局子執行緒中標記該玩家斷線，並啟動計時器。
                gameThreadManager.recordDisconnectedPlayer(game_status.split(":")[1], player_uuid);

                // 獲取所有位於該對局且在線的玩家列表。
                List<PlayerData> playerDataList = redisAccess.getPlayerDataFromGame_id(gameData.getGame_id());

                // 廣播新的「對局」數據給位於該對局的其它玩家。
                // 只需要玩家 id。
                ArrayList<String> playerUUIDList = new ArrayList<>();
                for (int i = 0; i < playerDataList.size(); ++i) {
                    playerUUIDList.add(playerDataList.get(i).getPlayer_uuid());
                }
                webSocketHandler.broadcast(playerUUIDList, "render_data:GameData", gameData);

                // 獲取新的「待機室」數據並廣播給位於待機室的玩家。
                getLobbyDataAndBroadcast();
            } else if (gameData != null && gameData.getWhite_uuid().equals(player_uuid)) {
                // 白方
                // 在暱稱上標記「斷線」
                gameData.setWhite(gameData.getWhite() + ":斷線");
                gameService.setGameData(gameData);

                // 於對局子執行緒中標記該玩家斷線，並啟動計時器。
                gameThreadManager.recordDisconnectedPlayer(game_status.split(":")[1], player_uuid);

                // 獲取所有位於該對局且在線的玩家列表。
                List<PlayerData> playerDataList = redisAccess.getPlayerDataFromGame_id(gameData.getGame_id());

                // 廣播新的「對局」數據給位於該對局的其它玩家。
                // 只需要玩家 id。
                ArrayList<String> playerUUIDList = new ArrayList<>();
                for (int i = 0; i < playerDataList.size(); ++i) {
                    playerUUIDList.add(playerDataList.get(i).getPlayer_uuid());
                }
                webSocketHandler.broadcast(playerUUIDList, "render_data:GameData", gameData);

                // 獲取新的「待機室」數據並廣播給位於待機室的玩家。
                getLobbyDataAndBroadcast();
            }
        }
    }

    @Override
    public void getLobbyDataAndBroadcast() throws JsonProcessingException {
        // 獲取「待機室」所需數據。
        LobbyData lobbyData = gameService.getLobbyData();

        // 獲取位於「待機室」的所有玩家。
        List<PlayerData> playerFromGameStatus = getPlayerDataFromGameStatus("lobby");

        // 廣播新的「待機室」數據給位於待機室的其它玩家。
        // 只需要玩家 id。
        ArrayList<String> playerUUIDList = new ArrayList<>();
        for (int i = 0; i < playerFromGameStatus.size(); ++i) {
            playerUUIDList.add(playerFromGameStatus.get(i).getPlayer_uuid());
        }
        webSocketHandler.broadcast(playerUUIDList, "render_data:LobbyData", lobbyData);
    }
}