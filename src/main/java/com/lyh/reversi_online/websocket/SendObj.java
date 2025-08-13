package com.lyh.reversi_online.websocket;

public class SendObj {
    private String type;
    private Object message;

    public SendObj() {
    }

    public SendObj(String type, Object message) {
        this.type = type;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "SendObj{" +
                "type='" + type + '\'' +
                ", message=" + message +
                '}';
    }
}