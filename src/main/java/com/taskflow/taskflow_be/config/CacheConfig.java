package com.taskflow.taskflow_be.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    public static final String PROJECT_REPORT_SUMMARY_CACHE = "project-report-summary";
    public static final String PROJECT_REPORT_WORKLOAD_CACHE = "project-report-workload";
    public static final String PROJECT_REPORT_SPRINT_PROGRESS_CACHE = "project-report-sprint-progress";

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(
                PROJECT_REPORT_SUMMARY_CACHE,
                PROJECT_REPORT_WORKLOAD_CACHE,
                PROJECT_REPORT_SPRINT_PROGRESS_CACHE
        );
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }
}
