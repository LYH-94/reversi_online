package com.lyh.reversi_online.config;

import com.lyh.reversi_online.websocket.CustomHandshakeInterceptor;
import com.lyh.reversi_online.websocket.impl.WebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final WebSocketHandler webSocketHandler;
    private final CustomHandshakeInterceptor customHandshakeInterceptor;

    @Autowired
    public WebSocketConfig(WebSocketHandler webSocketHandler, CustomHandshakeInterceptor customHandshakeInterceptor) {
        this.webSocketHandler = webSocketHandler;
        this.customHandshakeInterceptor = customHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/ws")
                .addInterceptors(customHandshakeInterceptor)
                .setAllowedOrigins("*"); // 允許跨來源連線 (若需限制安全性可修改)
    }
}