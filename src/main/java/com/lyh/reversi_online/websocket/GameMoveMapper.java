package com.lyh.reversi_online.websocket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lyh.reversi_online.pojo.GameMoveData;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GameMoveMapper {
    private GameMoveData message;

    public GameMoveMapper() {
    }

    public GameMoveMapper(GameMoveData message) {
        this.message = message;
    }

    public GameMoveData getMessage() {
        return message;
    }

    public void setMessage(GameMoveData message) {
        this.message = message;
    }
}