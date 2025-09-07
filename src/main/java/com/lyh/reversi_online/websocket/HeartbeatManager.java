package com.lyh.reversi_online.websocket;

import com.lyh.reversi_online.service.player.IPlayerManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

@Component
public class HeartbeatManager {
    private final ConcurrentMap<WebSocketSession, Long> lastHeartbeatMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final IPlayerManagement playerManagement;
    private final WebSocketSessionManager webSocketSessionManager;

    @Autowired
    public HeartbeatManager(IPlayerManagement playerManagement, WebSocketSessionManager webSocketSessionManager) {
        this.playerManagement = playerManagement;
        this.webSocketSessionManager = webSocketSessionManager;
        scheduler.scheduleAtFixedRate(this::checkHeartbeats, 0, 60, TimeUnit.SECONDS);
    }

    public void updateHeartbeat(WebSocketSession session) {
        lastHeartbeatMap.put(session, System.currentTimeMillis());
    }

    public void removeSession(WebSocketSession session) {
        lastHeartbeatMap.remove(session);
    }

    public void registerSession(WebSocketSession session) {
        updateHeartbeat(session);
    }

    private void checkHeartbeats() {
        long now = System.currentTimeMillis();
        for (Map.Entry<WebSocketSession, Long> entry : lastHeartbeatMap.entrySet()) {
            if (now - entry.getValue() > 60_000) {
                try {
                    // 關閉該玩家的 WebSocket 連線。
                    /*
                        afterConnectionClosed() 是伺服器端 WebSocket 的生命週期事件，
                        所以當調用 session.close() 函式時，也勢必會觸發 afterConnectionClosed()，
                        因此，可以統一在 afterConnectionClosed() 中處理關閉連線邏輯。
                    */
                    entry.getKey().close(new CloseStatus(1001, "Heartbeat timeout"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}