package com.lyh.reversi_online.pojo;

public class GameList {
    private String game_id;
    private String black;
    private String white;

    public GameList() {
    }

    public GameList(String game_id, String black, String white) {
        this.game_id = game_id;
        this.black = black;
        this.white = white;
    }

    public String getGame_id() {
        return game_id;
    }

    public void setGame_id(String game_id) {
        this.game_id = game_id;
    }

    public String getBlack() {
        return black;
    }

    public void setBlack(String black) {
        this.black = black;
    }

    public String getWhite() {
        return white;
    }

    public void setWhite(String white) {
        this.white = white;
    }

    @Override
    public String toString() {
        return "GameList{" +
                "game_id='" + game_id + '\'' +
                ", black='" + black + '\'' +
                ", white='" + white + '\'' +
                '}';
    }
}