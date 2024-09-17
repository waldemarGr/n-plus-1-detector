package com.additionaltools.tostringvalidator;

import com.additionaltools.common.AnnotationScannerService;
import com.additionaltools.common.EmptyLoggingConfiguration;
import com.additionaltools.logging.LoggingService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Configuration class to set up HashCodeAnalysis based on @EnableHashCodeAnalysis annotation.
 */

@Import(EmptyLoggingConfiguration.class)
@Configuration
public class ToStringValidatorConfiguration {

    @Bean
    public ToStringAnalysis toStringAnalysis(AnnotationScannerService annotationScannerService, LoggingService loggingService) {
        String basePath = annotationScannerService.getPackageNameForAnnotatedClass(EnableToStringValidator.class);
        return new ToStringAnalysis(annotationScannerService, basePath, loggingService);
    }

    @Bean
    @ConditionalOnMissingBean(AnnotationScannerService.class)
    public AnnotationScannerService annotationScannerService() {
        return new AnnotationScannerService();
    }
}