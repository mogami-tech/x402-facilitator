package tech.mogami.facilitator.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Configuration class for setting up an executor for asynchronous tasks.
 * This configuration uses virtual threads to handle concurrent tasks efficiently.
 */
@Configuration
@EnableAsync
public class ExecutorConfiguration {

    /**
     * Configures a virtual thread executor for asynchronous tasks.
     * This allows the application to handle many concurrent tasks efficiently
     * using virtual threads introduced in Java 19.
     *
     * @return an Executor that uses virtual threads for task execution
     */
    @Bean
    public Executor taskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

}
