package com.zorcee.smartdocserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 企业级自定义异步线程池配置（遵循阿里 Java 开发规范，禁止使用 Executors 默认线程池）
 */
@Configuration
public class ThreadPoolConfig {

    @Bean("asyncLogExecutor")
    public Executor asyncLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 1. 核心线程数（常驻线程）
        executor.setCorePoolSize(4);
        // 2. 最大线程数（队列满后扩容上限）
        executor.setMaxPoolSize(8);
        // 3. 有界等待队列容量（防止无界队列引发 OOM）
        executor.setQueueCapacity(200);
        // 4. 非核心线程空闲存活时间（秒）
        executor.setKeepAliveSeconds(60);
        // 5. 线程名前缀（方便线上排查堆栈）
        executor.setThreadNamePrefix("smartdoc-log-");
        // 6. 拒绝策略：队列和线程池都满时，由调用者线程兜底执行（不丢日志）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}