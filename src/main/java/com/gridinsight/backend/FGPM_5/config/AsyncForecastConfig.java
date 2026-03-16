package com.gridinsight.backend.FGPM_5.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configures a dedicated thread pool for asynchronous forecast jobs.
 * Referenced by @Async("forecastExecutor") in ForecastService.
 */
@Configuration
public class AsyncForecastConfig {

    @Bean(name = "forecastExecutor")
    public Executor forecastExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("forecast-");
        executor.initialize();
        return executor;
    }
}

