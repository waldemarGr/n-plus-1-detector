package com.additionaltools.logging;

import com.additionaltools.nplus1query.SQLStatisticsService;
import com.additionaltools.nplus1query.TransactionAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration class for automatic bean registration.
 *
 * <p>This configuration class defines beans for {@link SQLStatisticsService} and {@link TransactionAspect}.
 * It provides the necessary setup to integrate these components into the Spring application context.</p>
 */
@EnableScheduling
@Configuration
public class LoggingConfiguration {

    @Bean
    @Primary
    public LoggingService fileLoggingService() {
        return new FileLoggingService();
    }
}
