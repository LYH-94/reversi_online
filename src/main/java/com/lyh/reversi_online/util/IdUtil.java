package com.lyh.reversi_online.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class IdUtil {

    // 生成玩家 UUID + 時間戳，避免出現重複的問題。
    public String generatePlayerUUID() {
        return UUID.randomUUID().toString() + "_" + System.currentTimeMillis();
    }

    // 生成對局 ID + 時間戳，避免出現重複的問題。跟 generatePlayerUUID 一樣就可以了。
    public String generateGameId() {
        return generatePlayerUUID();
    }
}