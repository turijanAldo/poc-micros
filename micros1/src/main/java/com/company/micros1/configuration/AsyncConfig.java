package com.company.micros1.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
public class AsyncConfig {

    @Bean(name = "procesamientoExecutor")
    public Executor procesamientoExecutor() {
        return new ThreadPoolExecutor(
                50, // core threads
                200, // max threads
                60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
