package com.lyh.reversi_online.pojo;

import java.util.Arrays;

public class GameData {
    private Integer id;
    private String game_id;
    private String white;
    private String white_uuid;
    private String black;
    private String black_uuid;
    private String current_move;
    private int[][] board_status;

    public GameData() {
    }

    public GameData(Integer id, String game_id, String white, String white_uuid, String black, String black_uuid, String current_move, int[][] board_status) {
        this.id = id;
        this.game_id = game_id;
        this.white = white;
        this.white_uuid = white_uuid;
        this.black = black;
        this.black_uuid = black_uuid;
        this.current_move = current_move;
        this.board_status = board_status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getGame_id() {
        return game_id;
    }

    public void setGame_id(String game_id) {
        this.game_id = game_id;
    }

    public String getWhite() {
        return white;
    }

    public void setWhite(String white) {
        this.white = white;
    }

    public String getWhite_uuid() {
        return white_uuid;
    }

    public void setWhite_uuid(String white_uuid) {
        this.white_uuid = white_uuid;
    }

    public String getBlack() {
        return black;
    }

    public void setBlack(String black) {
        this.black = black;
    }

    public String getBlack_uuid() {
        return black_uuid;
    }

    public void setBlack_uuid(String black_uuid) {
        this.black_uuid = black_uuid;
    }

    public String getCurrent_move() {
        return current_move;
    }

    public void setCurrent_move(String current_move) {
        this.current_move = current_move;
    }

    public int[][] getBoard_status() {
        return board_status;
    }

    public void setBoard_status(int[][] board_status) {
        this.board_status = board_status;
    }

    @Override
    public String toString() {
        return "GameData{" +
                "id=" + id +
                ", game_id='" + game_id + '\'' +
                ", white='" + white + '\'' +
                ", white_uuid='" + white_uuid + '\'' +
                ", black='" + black + '\'' +
                ", black_uuid='" + black_uuid + '\'' +
                ", current_move='" + current_move + '\'' +
                ", board_status=" + Arrays.toString(board_status) +
                '}';
    }
}