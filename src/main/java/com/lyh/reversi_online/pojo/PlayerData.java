package com.lyh.reversi_online.pojo;

public class PlayerData {
    private Integer id;
    private String player_uuid;
    private String nick_name;
    private String game_status;
    private String connection_status;

    public PlayerData() {
    }

    public PlayerData(Integer id, String player_uuid, String nick_name, String game_status, String connection_status) {
        this.id = id;
        this.player_uuid = player_uuid;
        this.nick_name = nick_name;
        this.game_status = game_status;
        this.connection_status = connection_status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPlayer_uuid() {
        return player_uuid;
    }

    public void setPlayer_uuid(String player_uuid) {
        this.player_uuid = player_uuid;
    }

    public String getNick_name() {
        return nick_name;
    }

    public void setNick_name(String nick_name) {
        this.nick_name = nick_name;
    }

    public String getGame_status() {
        return game_status;
    }

    public void setGame_status(String game_status) {
        this.game_status = game_status;
    }

    public String getConnection_status() {
        return connection_status;
    }

    public void setConnection_status(String connection_status) {
        this.connection_status = connection_status;
    }

    @Override
    public String toString() {
        return "PlayerData{" +
                "id=" + id +
                ", player_uuid='" + player_uuid + '\'' +
                ", nick_name='" + nick_name + '\'' +
                ", game_status='" + game_status + '\'' +
                ", connection_status='" + connection_status + '\'' +
                '}';
    }
}