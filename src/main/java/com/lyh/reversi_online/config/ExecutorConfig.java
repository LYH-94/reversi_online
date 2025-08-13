package com.lyh.reversi_online.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class ExecutorConfig {
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(5); // 核心執行緒數
        executor.setMaxPoolSize(10); // 最大執行緒數
        executor.setQueueCapacity(100); // 任務佇列容量
        executor.setThreadNamePrefix("MyTaskExecutor-"); // 執行緒名稱前綴
        executor.initialize();

        return executor;
    }

    @Bean(name = "broadcastExecutor")
    public Executor broadcastExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(5); // 核心執行緒數
        executor.setMaxPoolSize(10); // 最大執行緒數
        executor.setQueueCapacity(100); // 任務佇列容量
        executor.setThreadNamePrefix("MyBroadcastExecutor-"); // 執行緒名稱前綴
        executor.initialize();

        return executor;
    }
}