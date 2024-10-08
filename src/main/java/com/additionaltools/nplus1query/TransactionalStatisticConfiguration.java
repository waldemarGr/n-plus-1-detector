package com.additionaltools.nplus1query;

import com.additionaltools.common.EmptyLoggingConfiguration;
import com.additionaltools.logging.LoggingService;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Configuration class for automatic bean registration.
 *
 * <p>This configuration class defines beans for {@link SQLStatisticsService} and {@link TransactionAspect}.
 * It provides the necessary setup to integrate these components into the Spring application context.</p>
 */
@Import(EmptyLoggingConfiguration.class)
@Configuration
public class TransactionalStatisticConfiguration {
    /**
     * Creates and configures a {@link SQLStatisticsService} bean.
     *
     * <p>This bean requires an {@link EntityManagerFactory} to be injected,
     * which is used to initialize the service for managing and analyzing SQL statistics.</p>
     *
     * @param entityManagerFactory the {@link EntityManagerFactory} used to create the SQLStatisticsService
     * @return a configured instance of {@link SQLStatisticsService}
     */
    @Bean
    public SQLStatisticsService sqlStatisticsService(EntityManagerFactory entityManagerFactory, LoggingService loggingService) {
        return new SQLStatisticsService(entityManagerFactory, loggingService);
    }

    /**
     * Creates and configures a {@link TransactionAspect} bean.
     *
     * <p>This bean requires a {@link SQLStatisticsService} to be injected,
     * which is used to monitor and collect statistics during transactional operations.</p>
     *
     * @param sqlStatisticsService the {@link SQLStatisticsService} used by the {@link TransactionAspect}
     * @return a configured instance of {@link TransactionAspect}
     */
    @Bean
    public TransactionAspect transactionAspect(SQLStatisticsService sqlStatisticsService) {
        return new TransactionAspect(sqlStatisticsService);
    }
}