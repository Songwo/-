package com.bmgf.Config;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
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
        exec.setCorePoolSize(2);
        exec.setMaxPoolSize(4);
        exec.setQueueCapacity(50);
        exec.setKeepAliveSeconds(60);
        exec.setThreadNamePrefix("VulnEnv-");
        exec.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        exec.initialize();
        return exec;
    }

//    @Bean
//    public DockerClient dockerClient() {
//        // 1. 构建基础配置，自动读取 DOCKER_HOST、证书等环境
//        DefaultDockerClientConfig config = DefaultDockerClientConfig
//                .createDefaultConfigBuilder()
//                .build();
//        // 2. 直接使用 DockerClientImpl 获取默认 transport 实现
//        return DockerClientImpl.getInstance(config);
//    }
}



