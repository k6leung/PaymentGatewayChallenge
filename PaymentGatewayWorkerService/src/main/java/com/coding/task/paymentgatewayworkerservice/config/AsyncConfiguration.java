package com.coding.task.paymentgatewayworkerservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Profile("!test")
@EnableAsync
@Configuration
public class AsyncConfiguration {

    @Value("${threadPool.size}")
    private int threadPoolSize;

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(this.threadPoolSize);
        taskExecutor.initialize();

        return taskExecutor;
    }

}
