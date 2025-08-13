package com.lyh.reversi_online.dao.impl;

import com.lyh.reversi_online.dao.IDataAccess;
import com.lyh.reversi_online.pojo.GameData;
import com.lyh.reversi_online.pojo.GameList;
import com.lyh.reversi_online.pojo.LobbyData;
import com.lyh.reversi_online.pojo.PlayerData;
import com.lyh.reversi_online.service.game.IGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
public class RedisAccess implements IDataAccess {
    private final RedisTemplate<String, Object> redisTemplate;
    private final IGameService gameService;

    @Autowired
    public RedisAccess(RedisTemplate<String, Object> redisTemplate, IGameService gameService) {
        this.redisTemplate = redisTemplate;
        this.gameService = gameService;
    }

    @Override
    public PlayerData getPlayerData(String player_uuid) {
        String key = "player_data:" + player_uuid;
        return getO(key, PlayerData.class);
    }

    @Override
    public void setPlayerData(String player_uuid, PlayerData playerData) {
        String key = "player_data:" + player_uuid;
        redisTemplate.opsForValue().set(key, playerData);
    }

    @Override
    public LobbyData getLobbyData() {
        // 獲取「待機室﹞所需的數據後，在組合成 LobbyData 物件。
        // 先獲取所有的「玩家數據」，此作法耗資源，但先這樣。
        // 1.用 SCAN 取得所有符合的 palyer_data: 前綴的 key。
        Set<String> keys = new HashSet<>();

        ScanOptions options = ScanOptions.scanOptions().match("player_data:*").count(100).build();
        try (Cursor<byte[]> cursor = redisTemplate.getConnectionFactory().getConnection().scan(options)) {
            while (cursor.hasNext()) {
                keys.add(new String(cursor.next()));
            }
        }

        // 2.用 multiGet() 查出 keys 的所有 value。
        List<Object> values = redisTemplate.opsForValue().multiGet(keys);
        List<PlayerData> playerDataList = new ArrayList<>();
        for (int i = 0; i < values.size(); ++i) {
            if (PlayerData.class.isInstance(values.get(i))) {
                playerDataList.add(PlayerData.class.cast(values.get(i)));
            }
        }

        Integer online_players = 0;
        ArrayList<String> lobby_players_list = new ArrayList<>();
        ArrayList<GameList> game_list = new ArrayList<>();
        for (int i = 0; i < playerDataList.size(); ++i) {
            if (playerDataList.get(i).getConnection_status().equals("online")) {
                // 3.online_players
                ++online_players;
                // 4.lobby_players_list
                lobby_players_list.add(playerDataList.get(i).getNick_name());
                // 5.GameList 需要：game_id、黑白兩方的暱稱
                if (playerDataList.get(i).getGame_status().split(":")[0].equals("ingame")) {
                    // 找出目前在線玩家數據中的 game_id 並判斷該對局是否還存在。
                    boolean gameIfExist = gameService.gameIfExist(playerDataList.get(i).getGame_status().split(":")[1]);
                    if (gameIfExist) {
                        boolean b = true;
                        for (int j = 0; j < game_list.size(); ++j) {
                            if (game_list.get(j).getGame_id().equals(playerDataList.get(i).getGame_status().split(":")[1])) {
                                b = false;
                            }
                        }
                        if (b) {
                            // 根據 game_id 獲取 GameData。
                            GameData gameData = gameService.getGameData(playerDataList.get(i).getGame_status().split(":")[1]);

                            // 創建 GameList
                            GameList gameList = new GameList(gameData.getGame_id(), gameData.getBlack(), gameData.getWhite());

                            // 添加到 game_list 列表。
                            game_list.add(gameList);
                        }
                    }
                }
            }
        }

        // 6.組合 LobbyData 物件。
        return new LobbyData(online_players, lobby_players_list, game_list);
    }

    @Override
    public void setGameData(GameData gameData) {
        String key = "game_data:" + gameData.getGame_id();
        redisTemplate.opsForValue().set(key, gameData);
    }

    @Override
    public GameData getGameData(String game_id) {
        String key = "game_data:" + game_id;
        return getO(key, GameData.class);
    }

    @Override
    public void deleteGameData(String game_id) {
        String key = "game_data:" + game_id;
        redisTemplate.delete(key);
    }

    @Override
    public List<PlayerData> getPlayerDataFromGame_id(String game_id) {
        // 先獲取所有的「玩家數據」，此作法耗資源，但先這樣。
        // 1.用 SCAN 取得所有符合的 palyer_data: 前綴的 key。
        Set<String> keys = getKeyForPrefix("player_data:*");

        // 2.用 multiGet() 查出 keys 的所有 value。
        List<PlayerData> playerDataList = getValueForKeys(keys, PlayerData.class);

        // 3.將不屬於該對局且不在線上的玩家移除。
        List<PlayerData> new_playerDataList = new ArrayList<>();
        for (int i = 0; i < playerDataList.size(); ++i) {
            if (playerDataList.get(i).getConnection_status().equals("online") &&
                    playerDataList.get(i).getGame_status().split(":").length == 2 &&
                    playerDataList.get(i).getGame_status().split(":")[1].equals(game_id)) {
                new_playerDataList.add(playerDataList.get(i));
            }
        }

        return new_playerDataList;
    }

    @Override
    public List<PlayerData> getPlayerDataFromGameStatus(String gameStatus) {
        // 先獲取所有的「玩家數據」，此作法耗資源，但先這樣。
        // 1.用 SCAN 取得所有符合的 palyer_data: 前綴的 key。
        Set<String> keys = getKeyForPrefix("player_data:*");

        // 2.用 multiGet() 查出 keys 的所有 value。
        List<PlayerData> playerDataList = getValueForKeys(keys, PlayerData.class);

        // 3.根據對局狀態、在線狀態塞選玩家。
        List<PlayerData> new_playerDataList = new ArrayList<>();
        for (int i = 0; i < playerDataList.size(); ++i) {
            if (playerDataList.get(i).getGame_status().split(":")[0].equals(gameStatus) &&
                    playerDataList.get(i).getConnection_status().equals("online")) {
                new_playerDataList.add(playerDataList.get(i));
            }
        }

        return new_playerDataList;
    }

    @Override
    public void setPlayerConnectionStatus(PlayerData playerData, String connectionStatus) {
        playerData.setConnection_status("offline:" + connectionStatus);
        setPlayerData(playerData.getPlayer_uuid(), playerData);
    }

    private <T> T getO(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        return clazz.isInstance(value) ? clazz.cast(value) : null;
    }

    private Set<String> getKeyForPrefix(String Prefix) {
        // 1.用 SCAN 取得所有符合的 palyer_data: 前綴的 key。
        Set<String> keys = new HashSet<>();

        ScanOptions options = ScanOptions.scanOptions().match(Prefix).count(100).build();
        try (Cursor<byte[]> cursor = redisTemplate.getConnectionFactory().getConnection().scan(options)) {
            while (cursor.hasNext()) {
                keys.add(new String(cursor.next()));
            }
        }

        return keys;
    }

    private <T> List<T> getValueForKeys(Set<String> keys, Class<T> clazz) {
        // 2.用 multiGet() 查出 keys 的所有 value。
        List<Object> values = redisTemplate.opsForValue().multiGet(keys);
        List<T> playerDataList = new ArrayList<>();
        for (int i = 0; i < values.size(); ++i) {
            if (clazz.isInstance(values.get(i))) {
                playerDataList.add(clazz.cast(values.get(i)));
            }
        }

        return playerDataList;
    }
}