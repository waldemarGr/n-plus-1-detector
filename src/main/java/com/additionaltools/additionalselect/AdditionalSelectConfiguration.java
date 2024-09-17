package com.additionaltools.additionalselect;

import com.additionaltools.common.EmptyLoggingConfiguration;
import com.additionaltools.logging.LoggingService;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


@Configuration
@Import(EmptyLoggingConfiguration.class)
public class AdditionalSelectConfiguration {

    @Bean
    public JpaSaveMonitorAspect jpaSaveMonitorAspect(SessionFactory sessionFactory, LoggingService loggingService) {
        return new JpaSaveMonitorAspect(sessionFactory, loggingService);
    }
}