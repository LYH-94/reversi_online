package com.lyh.reversi_online.service.game;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameManager {
    private final Map<String, GameThread> gameMap = new ConcurrentHashMap<>();

    public void addGame(String game_id, GameThread thread) {
        gameMap.put(game_id, thread);
    }

    public GameThread getGame(String game_id) {
        return gameMap.get(game_id);
    }

    public void removeGame(String game_id) {
        gameMap.remove(game_id);
    }

    public boolean gameIfExist(String game_id) {
        return gameMap.containsKey(game_id);
    }

    @Override
    public String toString() {
        return "GameManager{" +
                "gameMap=" + gameMap +
                '}';
    }
}