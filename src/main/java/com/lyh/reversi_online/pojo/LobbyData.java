package com.lyh.reversi_online.pojo;

import java.util.ArrayList;

public class LobbyData {
    private Integer online_players;
    private ArrayList<String> lobby_players_list;
    private ArrayList<GameList> game_list;

    public LobbyData() {
    }

    public LobbyData(Integer online_players, ArrayList<String> lobby_players_list, ArrayList<GameList> game_list) {
        this.online_players = online_players;
        this.lobby_players_list = lobby_players_list;
        this.game_list = game_list;
    }

    public Integer getOnline_players() {
        return online_players;
    }

    public void setOnline_players(Integer online_players) {
        this.online_players = online_players;
    }

    public ArrayList<String> getLobby_players_list() {
        return lobby_players_list;
    }

    public void setLobby_players_list(ArrayList<String> lobby_players_list) {
        this.lobby_players_list = lobby_players_list;
    }

    public ArrayList<GameList> getGame_list() {
        return game_list;
    }

    public void setGame_list(ArrayList<GameList> game_list) {
        this.game_list = game_list;
    }

    @Override
    public String toString() {
        return "LobbyData{" +
                "online_players=" + online_players +
                ", lobby_players_list=" + lobby_players_list +
                ", game_list=" + game_list +
                '}';
    }
}