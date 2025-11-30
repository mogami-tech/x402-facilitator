package tech.mogami.facilitator.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Asynchronous processing configuration.
 */
@Configuration
@EnableAsync
public class AsyncConfiguration {

    /** Default pool size. */
    private static final int DEFAULT_POOL_SIZE = 4;

    /** Maximum pool size. */
    private static final int MAX_POOL_SIZE = 16;

    /** Queue capacity. */
    private static final int QUEUE_CAPACITY = 1000;

    /**
     * Configure the payment log executor.
     *
     * @return payment log executor
     */
    @Bean(name = "paymentLogExecutor")
    public Executor paymentLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(DEFAULT_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix("x402-facilitator-batch-log-");

        // Never loose tasks: if the queue is full, run the task in the caller's thread
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

}
