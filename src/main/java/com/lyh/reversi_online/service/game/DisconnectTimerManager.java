package com.lyh.reversi_online.service.game;

import java.util.concurrent.*;

public class DisconnectTimerManager {
    ThreadFactory factory = r -> {
        Thread t = new Thread(r);
        t.setName("DisconnectTimerManager-Thread-" + t.getId());
        return t;
    };
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(factory);
    private ScheduledFuture<?> disconnectTask;

    // 啟動倒數計時
    public void startDisconnectTimer(Runnable onTimeout, long delay, TimeUnit unit) {
        stopDisconnectTimer(); // 確保之前的任務被取消
        disconnectTask = scheduler.schedule(onTimeout, delay, unit);
    }

    // 取消倒數（例如玩家重連）
    public void stopDisconnectTimer() {
        if (disconnectTask != null && !disconnectTask.isDone()) {
            disconnectTask.cancel(true);
        }
    }

    // 關閉 scheduler（應在遊戲結束時呼叫）
    public void shutdown() {
        scheduler.shutdownNow();
    }
}