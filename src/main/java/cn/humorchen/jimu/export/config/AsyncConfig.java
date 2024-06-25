package cn.humorchen.jimu.export.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * @author humorchen
 * date: 2024/1/16
 * description: 异步配置
 **/
@Slf4j
@Configuration
public class AsyncConfig {
    public static final String ASYNC_THREAD_POOL = "jimuExportDataExtensionAsyncThreadPool";

    /**
     * 异步线程池
     */
    @Bean(name = ASYNC_THREAD_POOL)
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(64);
        executor.setQueueCapacity(128);
        executor.setThreadNamePrefix(ASYNC_THREAD_POOL + "-");
        executor.initialize();
        return executor;
    }
}
