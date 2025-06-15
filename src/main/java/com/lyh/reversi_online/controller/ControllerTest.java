package com.lyh.reversi_online.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // 上面兩個註解的合體。
public class ControllerTest {
    @GetMapping("/hello")
    public String hello() {
        return "Hello,Spring Boot 3!";
    }
}
