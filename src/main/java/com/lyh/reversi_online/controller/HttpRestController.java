package com.lyh.reversi_online.controller;

import com.lyh.reversi_online.pojo.GameData;
import com.lyh.reversi_online.pojo.LobbyData;
import com.lyh.reversi_online.pojo.PlayerData;
import com.lyh.reversi_online.service.game.IGameService;
import com.lyh.reversi_online.service.player.IPlayerManagement;
import com.lyh.reversi_online.service.watch.WatchService;
import com.lyh.reversi_online.util.ResponseHTML;
import com.lyh.reversi_online.websocket.IWebSocketHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HttpRestController {
    private final ResponseHTML responseHTML;
    private final IPlayerManagement playerManagement;
    private final IGameService gameService;
    private final IWebSocketHandler webSocketHandler;
    private final WatchService watchService;

    @Autowired
    public HttpRestController(ResponseHTML responseHTML, IPlayerManagement playerManagement, IGameService gameService, IWebSocketHandler webSocketHandler, WatchService watchService) {
        this.responseHTML = responseHTML;
        this.playerManagement = playerManagement;
        this.gameService = gameService;
        this.webSocketHandler = webSocketHandler;
        this.watchService = watchService;
    }

    // 處理玩家連線請求
    @GetMapping("/connectionRequest")
    @ResponseBody
    public ResponseEntity<byte[]> connectionRequest(@CookieValue(value = "player_uuid", defaultValue = "null") String player_uuid,
                                                    HttpServletRequest req,
                                                    HttpServletResponse resp) throws IOException {
        playerManagement.checkUUID(player_uuid, req, resp);
        return responseHTML.getHTML("index", HttpStatus.OK);
    }

    // 處理玩家排隊請求
    @GetMapping("/queue")
    @ResponseBody
    public String queueRequest(@CookieValue(value = "player_uuid") String player_uuid) {
        // System.out.println("收到排隊請求。");
        gameService.joinQueue(player_uuid);
        return "正在排隊";
    }

    // 處理玩家取消排隊請求
    @GetMapping("/cancelQueue")
    @ResponseBody
    public String cancelQueue(@CookieValue(value = "player_uuid") String player_uuid) {
        // System.out.println("收到取消排隊請求。");
        gameService.cancleQueue(player_uuid);
        return "取消排隊";
    }

    // 獲取「待機室」所需數據
    @GetMapping("/lobbyData")
    @ResponseStatus(HttpStatus.OK)
    public void getLobbyData(@CookieValue(value = "player_uuid") String player_uuid) throws IOException {
        // System.out.println("收到請求「待機室」所需數據請求。");

        // 1.獲取「玩家數據」。
        PlayerData playerData = playerManagement.getPlayerData(player_uuid);

        // 2.獲取「待機室」所需數據。
        LobbyData lobbyData = gameService.enterLobby(playerData);

        // 3.調用 WebSocket 返回「待機室」數據進行頁面渲染。
        // 獲取位於「待機室」的所有玩家。
        List<PlayerData> playerFromGameStatus = playerManagement.getPlayerDataFromGameStatus("lobby");

        // 廣播新的「待機室」數據給位於待機室的其它玩家，通知有玩家上線了。
        // 只需要玩家 id。
        ArrayList<String> playerUUIDList = new ArrayList<>();
        for (int i = 0; i < playerFromGameStatus.size(); ++i) {
            playerUUIDList.add(playerFromGameStatus.get(i).getPlayer_uuid());
        }
        webSocketHandler.broadcast(playerUUIDList, "render_data:LobbyData", lobbyData);
    }

    // 獲取「對局」所需數據
    @GetMapping("/gameData/{req_reconnect}")
    @ResponseStatus(HttpStatus.OK)
    public void getGameData(@CookieValue(value = "player_uuid") String player_uuid, @PathVariable(value = "req_reconnect") String req_reconnect) throws IOException {
        // System.out.println("收到請求「對局」所需數據請求。");
        //System.out.println("req_reconnect:" + req_reconnect);

        // 判斷是否啟動「斷線重連」。
        if (req_reconnect.equals("true")) {
            gameService.reconnect(player_uuid);
        } else {
            // 1.獲取「玩家數據」。
            PlayerData playerData = playerManagement.getPlayerData(player_uuid);

            // 2.獲取「對局」所需數據。
            String game_id = playerData.getGame_status().split(":")[1];
            GameData gameData = gameService.getGameData(game_id);

            // 調用 WebSocket 返回「對局」數據進行頁面渲染。
            webSocketHandler.broadcast(playerData.getPlayer_uuid(), "render_data:GameData", gameData);
        }
    }

    // 處理返回待機室請求
    @GetMapping("/backToLooby")
    @ResponseBody
    public ResponseEntity<byte[]> backToLooby(@CookieValue(value = "player_uuid") String player_uuid) throws IOException {
        gameService.surrender(player_uuid);
        return responseHTML.getHTML("lobbyPage", HttpStatus.OK);
    }

    // 處理觀戰請求
    @GetMapping("/watch/{req_gameId}")
    @ResponseStatus(HttpStatus.OK)
    public void watch(@CookieValue(value = "player_uuid") String player_uuid, @PathVariable(value = "req_gameId") String req_gameId) throws IOException {
        // System.out.println("要觀戰的對局 id:" + req_gameId);
        watchService.joinWatch(player_uuid, req_gameId);
    }
}