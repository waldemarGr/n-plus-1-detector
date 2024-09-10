package com.additionaltools.additionalselect;

import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AdditionalSelectConfiguration {

    @Bean
    public JpaSaveMonitorAspect jpaSaveMonitorAspect(SessionFactory sessionFactory) {
        return new JpaSaveMonitorAspect(sessionFactory);
    }
}