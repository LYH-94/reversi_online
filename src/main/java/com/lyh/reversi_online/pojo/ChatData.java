package com.lyh.reversi_online.pojo;

public class ChatData {
    private String player;
    private String content;

    public ChatData() {
    }

    public ChatData(String player, String content) {
        this.player = player;
        this.content = content;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}