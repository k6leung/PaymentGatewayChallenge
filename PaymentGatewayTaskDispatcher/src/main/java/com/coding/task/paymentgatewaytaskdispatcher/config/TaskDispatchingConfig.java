package com.coding.task.paymentgatewaytaskdispatcher.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Profile("!test")
@Configuration
@EnableAsync
@EnableScheduling
public class TaskDispatchingConfig {

    @Value("${payment.threadPool.size}")
    private int paymentThreadPoolSize;

    @Value("${notification.threadPool.size}")
    private int notificationThreadPoolSize;

    @Bean
    public ThreadPoolTaskExecutor paymentTaskExecutor() {
        ThreadPoolTaskExecutor paymentTaskExecutor = new ThreadPoolTaskExecutor();
        paymentTaskExecutor.setMaxPoolSize(this.paymentThreadPoolSize);
        paymentTaskExecutor.initialize();

        return paymentTaskExecutor;
    }

    @Bean
    public ThreadPoolTaskExecutor notificationTaskExecutor() {
        ThreadPoolTaskExecutor notificationTaskExecutor = new ThreadPoolTaskExecutor();
        notificationTaskExecutor.setMaxPoolSize(this.notificationThreadPoolSize);
        notificationTaskExecutor.initialize();

        return notificationTaskExecutor;
    }
}
