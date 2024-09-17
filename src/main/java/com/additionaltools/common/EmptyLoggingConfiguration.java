package com.additionaltools.common;

import com.additionaltools.logging.FileLoggingService;
import com.additionaltools.logging.LoggingService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmptyLoggingConfiguration {

    @Bean
    @ConditionalOnMissingBean(FileLoggingService.class)
    public LoggingService emptyLoggingService() {
        return new EmptyLoggingService();
    }
}
