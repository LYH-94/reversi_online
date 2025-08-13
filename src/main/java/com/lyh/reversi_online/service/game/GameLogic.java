package com.lyh.reversi_online.service.game;

import com.lyh.reversi_online.pojo.GameData;

import java.util.ArrayList;

import static java.lang.Integer.parseInt;

public class GameLogic {
    // 黑白子的數量。
    int blackNumber = 0;
    int whileNumber = 0;

    public boolean validationMove(String position, ArrayList<String> allowPosition) {
        return allowPosition.contains(position);
    }

    public GameData Reversi(String position, GameData gameData) {
        // 先獲取當前回合是黑方還是白方。
        String current_move = gameData.getCurrent_move();
        int current_move_int = 0;
        int current_move_opponent = 0;

        // 將 position 轉換成 int x,y。
        int x = parseInt(position.split(",")[0]);
        int y = parseInt(position.split(",")[1]);

        // 落子
        int[][] board_status = gameData.getBoard_status();
        if (current_move.equals("black")) {
            board_status[x][y] = 2;
            current_move_int = 2;
            current_move_opponent = 1;
        } else if (current_move.equals("white")) {
            board_status[x][y] = 1;
            current_move_int = 1;
            current_move_opponent = 2;
        }

        // 翻轉對手棋子
        // 找出落子位置的九宮格範圍內的對手棋子
        int x_temp = 0;
        int y_temp = 0;
        int x_displacement = 0;
        int y_displacement = 0;
        for (int xx = -1; xx < 2; ++xx) {
            if ((x + xx >= 0) && (x + xx < 8)) {
                x_temp = x + xx;
            } else {
                continue;
            }
            for (int yy = -1; yy < 2; ++yy) {
                if ((y + yy >= 0) && (y + yy < 8)) {
                    y_temp = y + yy;
                } else {
                    continue;
                }
                //System.out.println("落子九宮格範圍: " + board_status[x_temp][y_temp]); // 九宮格範圍。

                // 計算陣列要位移的方向。
                x_displacement = x_temp - x;
                y_displacement = y_temp - y;
                while (board_status[x_temp][y_temp] == current_move_opponent) { // 找出落子位置九宮格範圍中，對手顏色棋子的位置。
                    // 判斷是否超出棋盤
                    if (!((x_temp + x_displacement >= 0) && (x_temp + x_displacement < 8) &&
                            (y_temp + y_displacement >= 0) && (y_temp + y_displacement < 8))) {
                        break;
                    }
                    // 判斷是否為對手顏色棋子，若是則繼續移動。
                    else if (board_status[x_temp + x_displacement][y_temp + y_displacement] == current_move_opponent) {
                        if (x_displacement > 0) {
                            ++x_displacement;
                        } else if (x_displacement < 0) {
                            --x_displacement;
                        }
                        if (y_displacement > 0) {
                            ++y_displacement;
                        } else if (y_displacement < 0) {
                            --y_displacement;
                        }
                    }
                    // 判斷是否為自己顏色的棋子。
                    else if (board_status[x_temp + x_displacement][y_temp + y_displacement] == current_move_int) {
                        while (true) {
                            if (x_displacement > 0) {
                                --x_displacement;
                            } else if (x_displacement < 0) {
                                ++x_displacement;
                            }
                            if (y_displacement > 0) {
                                --y_displacement;
                            } else if (y_displacement < 0) {
                                ++y_displacement;
                            }

                            board_status[x_temp + x_displacement][y_temp + y_displacement] = current_move_int;

                            if ((x_temp + x_displacement) == x_temp && (y_temp + y_displacement) == y_temp) {
                                board_status[x_temp][y_temp] = current_move_int;
                                break;
                            }
                        }
                    }
                    // 以上都不成立則跳出，正常情況下是空格。
                    else {
                        break;
                    }
                }
            }
        }

        // 更新 gameData 落子位置
        gameData.setBoard_status(board_status);

        return gameData;
    }

    public ArrayList<String> calculateAllowedPositions(GameData gameData) {
        // 先創建一個新的允許位置陣列。
        ArrayList<String> allowPosition = new ArrayList<>();

        // 先獲取當前回合是黑方還是白方。
        String current_move = gameData.getCurrent_move();
        int current_move_int = 0;
        int current_move_opponent = 0;
        int current_move_int_allow = 0;
        int current_move_opponent_allow = 0;

        // 判斷對手棋子顏色。
        if (current_move.equals("black")) {
            current_move_int = 2;
            current_move_opponent = 1;
            current_move_int_allow = 3;
            current_move_opponent_allow = 4;
        } else if (current_move.equals("white")) {
            current_move_int = 1;
            current_move_opponent = 2;
            current_move_int_allow = 4;
            current_move_opponent_allow = 3;
        }

        // 找出棋盤上對手棋子的位置。
        int[][] board_status = gameData.getBoard_status();
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                // 先將當前回合允許位置清空。
                if (board_status[i][j] == current_move_int_allow) {
                    board_status[i][j] = ' ';
                }

                // 找出當前回合對手的棋子。
                if (board_status[i][j] == current_move_opponent) {
                    //System.out.println("board_status[i][j]: " + board_status[i][j]);

                    // 找出該位置的九宮格範圍內，對手顏色的棋子。
                    int x_temp = 0;
                    int y_temp = 0;
                    int x_displacement = 0;
                    int y_displacement = 0;
                    for (int xx = -1; xx < 2; ++xx) {
                        if ((i + xx >= 0) && (i + xx < 8)) {
                            x_temp = i + xx;
                        } else {
                            continue;
                        }
                        for (int yy = -1; yy < 2; ++yy) {
                            if ((j + yy >= 0) && (j + yy < 8)) {
                                y_temp = j + yy;
                            } else {
                                continue;
                            }
                            //System.out.println("九宮格範圍: " + board_status[x_temp][y_temp]); // 九宮格範圍。

                            // 計算陣列要位移的方向。
                            x_displacement = x_temp - i;
                            y_displacement = y_temp - j;
                            while (board_status[x_temp][y_temp] == current_move_int) {
                                // 判斷是否超出棋盤
                                if (!((x_temp + x_displacement >= 0) && (x_temp + x_displacement < 8) &&
                                        (y_temp + y_displacement >= 0) && (y_temp + y_displacement < 8))) {
                                    break;
                                }
                                // 判斷是否為當前回合顏色棋子，若是則繼續移動。
                                else if (board_status[x_temp + x_displacement][y_temp + y_displacement] == current_move_int) {
                                    if (x_displacement > 0) {
                                        ++x_displacement;
                                    } else if (x_displacement < 0) {
                                        --x_displacement;
                                    }
                                    if (y_displacement > 0) {
                                        ++y_displacement;
                                    } else if (y_displacement < 0) {
                                        --y_displacement;
                                    }
                                }
                                // 判斷是否為當前回合對手顏色棋子，若是則不動作。
                                else if (board_status[x_temp + x_displacement][y_temp + y_displacement] == current_move_opponent) {
                                    break;
                                }
                                // 以上都不成立則時，正常情況下是空格，設為允許落子位置
                                else {
                                    if (!allowPosition.contains((x_temp + x_displacement) + "," + (y_temp + y_displacement))) {
                                        allowPosition.add((x_temp + x_displacement) + "," + (y_temp + y_displacement));
                                    }
                                    board_status[x_temp + x_displacement][y_temp + y_displacement] = current_move_opponent_allow;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return allowPosition;
    }

    public GameData switchRound(GameData gameData) {
        if (gameData.getCurrent_move().equals("black")) {
            gameData.setCurrent_move("white");
        } else if (gameData.getCurrent_move().equals("white")) {
            gameData.setCurrent_move("black");
        }
        return gameData;
    }

    public boolean winOrLose(boolean NSCMAM, GameData gameData) {
        /* 對局結束的三個條件，任意一個成立則結束：
           1.一方的棋子已經被對方翻光。
           2.棋盤填滿了棋子。
           3.雙方都無合法棋步可下。
        */

        // Total number of chess pieces: TNOCPs
        int TNOCPs = 0;
        blackNumber = 0;
        whileNumber = 0;

        int[][] board_status = gameData.getBoard_status();
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                if (board_status[i][j] == 1) {
                    ++TNOCPs;
                    ++whileNumber;
                } else if (board_status[i][j] == 2) {
                    ++TNOCPs;
                    ++blackNumber;
                }
            }
        }

        // 在有棋子的情況下，一方顏色棋子為 0 時，表示棋盤上一方的棋子已經被對方翻光。即結束對局。
        if (TNOCPs != 0 && (blackNumber == 0 || whileNumber == 0)) {
            return true;
        }

        // 棋盤填滿了棋子。
        if (TNOCPs == 64) {
            return true;
        }

        // 雙方都無合法棋步可下。
        if (NSCMAM) {
            return true;
        }

        // 如果以上都不成立，則對局繼續。
        return false;
    }

    public GameData settlement(GameData gameData) {
        if (blackNumber > whileNumber) { // 黑方勝
            gameData.setBlack(gameData.getBlack() + ":勝 - " + blackNumber);
            gameData.setWhite(gameData.getWhite() + ":敗 - " + whileNumber);
        } else if (blackNumber < whileNumber) { // 白方勝
            gameData.setBlack(gameData.getBlack() + ":敗 - " + blackNumber);
            gameData.setWhite(gameData.getWhite() + ":勝 - " + whileNumber);
        } else { // 雙方平手
            gameData.setBlack(gameData.getBlack() + ":平 - " + blackNumber);
            gameData.setWhite(gameData.getWhite() + ":平 - " + whileNumber);
        }

        return gameData;
    }
}