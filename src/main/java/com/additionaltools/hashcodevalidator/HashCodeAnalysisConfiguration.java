package com.additionaltools.hashcodevalidator;

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
public class HashCodeAnalysisConfiguration {

    @Bean
    public HashCodeAnalysis hashCodeAnalysis(AnnotationScannerService annotationScannerService, LoggingService loggingService) {
        String basePath = annotationScannerService.getPackageNameForAnnotatedClass(EnableHashCodeAnalysis.class);
        return new HashCodeAnalysis(annotationScannerService, basePath, loggingService);
    }

    @Bean
    @ConditionalOnMissingBean(AnnotationScannerService.class)
    public AnnotationScannerService annotationScannerService() {
        return new AnnotationScannerService();
    }
}