package com.lyh.reversi_online.controller;


import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Test_RedisController {
    private final RedisTemplate<String, Object> redisTemplate;

    public Test_RedisController(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/ping")
    public void ping() {
        String ping = redisTemplate.getConnectionFactory().getConnection().ping();
        System.out.println(ping);
    }
}