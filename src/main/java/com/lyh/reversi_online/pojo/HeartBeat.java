package com.lyh.reversi_online.pojo;

public class HeartBeat {
    private String type;
    private Long timestamp;

    public HeartBeat() {
    }

    public HeartBeat(String type, Long timestamp) {
        this.type = type;
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "HeartBeat{" +
                "type='" + type + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}