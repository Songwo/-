package com.bmgf.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "vulnTaskExecutor")
    public ThreadPoolTaskExecutor vulnTaskExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(4);
        exec.setMaxPoolSize(8);
        exec.setQueueCapacity(100);
        exec.setKeepAliveSeconds(120);
        exec.setThreadNamePrefix("VulnEnv-");
        exec.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());


        exec.initialize();
        return exec;
    }
}
