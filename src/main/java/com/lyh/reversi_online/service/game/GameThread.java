package com.lyh.reversi_online.service.game;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lyh.reversi_online.pojo.GameData;
import com.lyh.reversi_online.pojo.PlayerData;
import com.lyh.reversi_online.service.player.IPlayerManagement;
import com.lyh.reversi_online.websocket.IWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class GameThread implements Runnable {
    private final String gameId;
    private final PlayerData playerData_One;
    private final PlayerData playerData_Two;
    private final int[][] board_status = new int[8][8];
    private final IGameService gameService;
    private final IWebSocketHandler webSocketHandler;
    private final IPlayerManagement playerManagement;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final Runnable onGameEnd; // 回呼邏輯 1
    private final Consumer<GameData> onBroadcastGameData; // 回呼邏輯 2
    private final DisconnectTimerManager timer = new DisconnectTimerManager();
    private final ArrayList<String> disconnectPlayerArray = new ArrayList<>(); // 用於暫存斷線的玩家。
    private GameData gameData = null;
    private ArrayList<String> allowPosition = new ArrayList<>(); // 用於儲存被允許落子的位置。

    // Neither side can make a move：NSCMAM
    private boolean winOrLose_condition_NSCMAM = false;
    private final GameLogic gameLogic = new GameLogic();

    public GameThread(String gameId, PlayerData playerData_One, PlayerData playerData_Two, IGameService gameService, IWebSocketHandler webSocketHandler, IPlayerManagement playerManagement, Runnable onGameEnd, Consumer<GameData> onBroadcastGameData) {
        this.gameId = gameId;
        this.playerData_One = playerData_One;
        this.playerData_Two = playerData_Two;
        this.gameService = gameService;
        this.webSocketHandler = webSocketHandler;
        this.playerManagement = playerManagement;
        this.onGameEnd = onGameEnd;
        this.onBroadcastGameData = onBroadcastGameData;
        gameService.setGameData(createGameData(gameId, playerData_One, playerData_Two));

        // 初始化黑子允許落子的位置。
        allowPosition.add("2,3");
        allowPosition.add("3,2");
        allowPosition.add("4,5");
        allowPosition.add("5,4");
    }

    @Override
    public void run() {
        // System.out.println("對局開始：" + gameId);
        try {
            // 通知兩位玩家跳轉「對局」頁面。
            webSocketHandler.broadcast(playerData_One.getPlayer_uuid(), "redirect_page:gamePage", "{\"page_name\":\"gamePage\", \"reconnect\":\"false\"}");
            webSocketHandler.broadcast(playerData_Two.getPlayer_uuid(), "redirect_page:gamePage", "{\"page_name\":\"gamePage\", \"reconnect\":\"false\"}");
            playerManagement.getLobbyDataAndBroadcast();
            while (running.get()) {
                Thread.sleep(5000); // 模擬等待事件。

                // 跳出迴圈時，該對局執行緒將結束。
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // System.out.println("對局結束：" + gameId);
    }

    public String getGameId() {
        return gameId;
    }

    private GameData createGameData(String game_id, PlayerData playerData_One, PlayerData playerData_Two) {
        // 初始化棋盤，預設全部為空格 ' '
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board_status[i][j] = ' ';
            }
        }

        // 設定黑白棋的起始位置 (1=白、2=黑、3=允許的位置)
        board_status[3][3] = 1; // 白棋
        board_status[3][4] = 2; // 黑棋
        board_status[4][3] = 2; // 黑棋
        board_status[4][4] = 1; // 白棋
        board_status[2][3] = 3; // 黑子允許位置
        board_status[3][2] = 3; // 黑子允許位置
        board_status[4][5] = 3; // 黑子允許位置
        board_status[5][4] = 3; // 黑子允許位置

        // 創建 GameData。
        gameData = new GameData(
                0,
                gameId,
                playerData_One.getNick_name(),
                playerData_One.getPlayer_uuid(),
                playerData_Two.getNick_name(),
                playerData_Two.getPlayer_uuid(),
                "black",
                board_status);

        return gameData;
    }

    public void gameEnd(String game_id) {
        // 刪除 GameData
        gameService.deleteGameData(game_id);

        // 關閉斷線計時器
        timer.shutdown();

        // 終止執行緒
        running.set(false);

        // 執行回呼：通知 GameManager 移除
        if (onGameEnd != null) {
            onGameEnd.run();
        }
    }

    public void surrender(PlayerData playerData) throws JsonProcessingException {
        // 將 gameData 物件中，該玩家設置為投降。
        if (gameData.getBlack_uuid().equals(playerData.getPlayer_uuid())) {
            // System.out.println("黑色投降");
            gameData.setBlack(playerData.getNick_name() + ":投降");
        } else {
            // System.out.println("白色投降");
            gameData.setWhite(playerData.getNick_name() + ":投降");
        }

        // 獲取所有處於該對局玩家列表。
        List<PlayerData> playerDataFromGame_id = playerManagement.getPlayerDataFromGame_id(gameData.getGame_id());

        // 移除投降玩家。
        for (int i = 0; i < playerDataFromGame_id.size(); ++i) {
            if (playerDataFromGame_id.get(i).getPlayer_uuid().equals(playerData.getPlayer_uuid())) {
                playerDataFromGame_id.remove(i);
            }
        }

        // 只需要玩家 id。
        ArrayList<String> playerUUIDList = new ArrayList<>();
        for (int i = 0; i < playerDataFromGame_id.size(); ++i) {
            playerUUIDList.add(playerDataFromGame_id.get(i).getPlayer_uuid());
        }

        // 廣播新的「對局」數據給該對局中的其它玩家。
        webSocketHandler.broadcast(playerUUIDList, "render_data:GameData", gameData);

        // 結束對局。
        gameEnd(gameData.getGame_id());
    }

    public void recordDisconnectedPlayer(String player_uuid) {
        // System.out.println("玩家斷線了");

        // 紀錄斷線的玩家
        disconnectPlayerArray.add(player_uuid);

        // 啟動計時器
        timer.startDisconnectTimer(() -> {
            // System.out.println("玩家逾時未連線，自動判負！");

            // 更新 GameData
            // 斷線者判輸、未斷線者判贏，皆斷線判平手。
            if (disconnectPlayerArray.size() == 1) {
                if (gameData.getBlack_uuid().equals(player_uuid)) {
                    gameData.setWhite(gameData.getWhite() + ":勝");
                    gameData.setBlack(gameData.getBlack() + ":敗");
                    gameService.setGameData(gameData);
                } else if (gameData.getWhite_uuid().equals(player_uuid)) {
                    gameData.setBlack(gameData.getBlack() + ":勝");
                    gameData.setWhite(gameData.getWhite() + ":敗");
                    gameService.setGameData(gameData);
                }
            } else if (disconnectPlayerArray.size() == 2) {
                gameData.setBlack(gameData.getBlack() + ":平手");
                gameData.setWhite(gameData.getWhite() + ":平手");
                gameService.setGameData(gameData);
            }

            // 廣播新的「對局」數據給該對局的其它玩家。
            // 執行回呼：廣播 GameData。
            if (onBroadcastGameData != null) {
                onBroadcastGameData.accept(gameData);
            }

            // 結束對局。
            gameEnd(gameData.getGame_id());
        }, 60, TimeUnit.SECONDS);
    }

    // 重置/停止該對局中的計時器。大概還會有一個函式調用 timer.stopDisconnectTimer()，例如玩家重新連線時。
    public void resetOrStopTimer(String player_uuid) {
        // 將重新連線的玩家從 disconnectPlayerArray 中移除。
        disconnectPlayerArray.remove(player_uuid);

        // 判斷該對局中，玩家斷線的情況。
        if (disconnectPlayerArray.size() == 0) {
            // 停止
            timer.stopDisconnectTimer();
        } else if (disconnectPlayerArray.size() == 1) {
            // 重新啟動計時器
            timer.startDisconnectTimer(() -> {
                // System.out.println("玩家逾時未連線，自動判負！");

                // 更新 GameData
                // 斷線者判輸、未斷線者判贏，皆斷線判平手。
                if (disconnectPlayerArray.size() == 1) {
                    if (gameData.getBlack_uuid().equals(player_uuid)) {
                        gameData.setWhite(gameData.getWhite() + ":勝");
                        gameData.setBlack(gameData.getBlack() + ":敗");
                        gameService.setGameData(gameData);
                    } else if (gameData.getWhite_uuid().equals(player_uuid)) {
                        gameData.setBlack(gameData.getBlack() + ":勝");
                        gameData.setWhite(gameData.getWhite() + ":敗");
                        gameService.setGameData(gameData);
                    }
                } else if (disconnectPlayerArray.size() == 2) {
                    gameData.setBlack(gameData.getBlack() + ":平手");
                    gameData.setWhite(gameData.getWhite() + ":平手");
                    gameService.setGameData(gameData);
                }

                // 廣播新的「對局」數據給該對局的其它玩家。
                // 執行回呼：廣播 GameData。
                if (onBroadcastGameData != null) {
                    onBroadcastGameData.accept(gameData);
                }

                // 結束對局。
                gameEnd(gameData.getGame_id());
            }, 60, TimeUnit.SECONDS);
        }
    }

    public GameData playerMove(String player_uuid, String position) {
        boolean validationMove = false;
        boolean winOrLose = false;

        // 判斷是否是當前回合的玩家。
        //黑白UUID、
        if (gameData.getCurrent_move().equals("black") && gameData.getBlack_uuid().equals(player_uuid)) {
            // 判斷落子是否符合規則。
            validationMove = gameLogic.validationMove(position, allowPosition);
            //System.out.println("validationMove: " + validationMove);
        } else if (gameData.getCurrent_move().equals("white") && gameData.getWhite_uuid().equals(player_uuid)) {
            // 判斷落子是否符合規則。
            validationMove = gameLogic.validationMove(position, allowPosition);
            //System.out.println("validationMove: " + validationMove);
        }

        if (validationMove) {
            validationMove = false;

            // 落子並翻轉對手棋子。
            gameData = gameLogic.Reversi(position, gameData);

            // 計算對手可落子位置。
            allowPosition = gameLogic.calculateAllowedPositions(gameData);

            // 切換回合。
            // 判斷對手是否有可落子的位置，若沒有則不交換落子方，由當前落子玩家繼續落子。
            // 若由當前落子玩家繼續落子時，需要再次計算允許落子位置。
            if (allowPosition.size() == 0) {
                // 先切換當前落子顏色為對手，計算完允許位置後再切換回來。
                gameData = gameLogic.switchRound(gameData);

                // 再次計算允許位置。
                allowPosition = gameLogic.calculateAllowedPositions(gameData);

                // 如果還是沒有允許落子位置的話，表示雙方皆沒有合法落子位置，因此對局結束。
                if (allowPosition.size() == 0) {
                    winOrLose_condition_NSCMAM = true;
                }

                // 切換回來。
                gameData = gameLogic.switchRound(gameData);
            } else {
                // 切換回合。
                gameData = gameLogic.switchRound(gameData);
            }

            // 判斷是否有勝負。
            winOrLose = gameLogic.winOrLose(winOrLose_condition_NSCMAM, gameData);

            // 若對局結束，進行結算並更新 gameData 勝負平手並追加棋盤上黑白子的數量。
            if (winOrLose) {
                gameData = gameLogic.settlement(gameData);
            }

            // 於 Redis 中更新「對局數據」 GameData。
            gameService.setGameData(gameData);

            // 廣播新的「對局」數據給該對局的其它玩家。
            // 執行回呼：廣播 GameData。
            if (onBroadcastGameData != null) {
                onBroadcastGameData.accept(gameData);
            }

            // 根據 winOrLose 決定是否結束對局。
            if (winOrLose) {
                gameEnd(gameData.getGame_id());
            }
        }
        return gameData;
    }

    public PlayerData getPlayerData_One() {
        return playerData_One;
    }

    public PlayerData getPlayerData_Two() {
        return playerData_Two;
    }
}