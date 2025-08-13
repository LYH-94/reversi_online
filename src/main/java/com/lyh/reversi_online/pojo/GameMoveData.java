package com.lyh.reversi_online.pojo;

public class GameMoveData {
    private String game_id;
    private String position;

    public GameMoveData() {
    }

    public GameMoveData(String game_id, String position) {
        this.game_id = game_id;
        this.position = position;
    }

    public String getGame_id() {
        return game_id;
    }

    public void setGame_id(String game_id) {
        this.game_id = game_id;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}