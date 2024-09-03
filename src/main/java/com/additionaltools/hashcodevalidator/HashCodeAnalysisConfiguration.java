package com.additionaltools.hashcodevalidator;

import com.additionaltools.common.AnnotationScannerService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to set up HashCodeAnalysis based on @EnableHashCodeAnalysis annotation.
 */
@Configuration
public class HashCodeAnalysisConfiguration {

    @Bean
    public HashCodeAnalysis hashCodeAnalysis(AnnotationScannerService annotationScannerService) {
        String basePath = annotationScannerService.getPackageNameForAnnotatedClass(EnableHashCodeAnalysis.class);
        return new HashCodeAnalysis(annotationScannerService, basePath);
    }

    @Bean
    @ConditionalOnMissingBean(AnnotationScannerService.class)
    public AnnotationScannerService annotationScannerService() {
        return new AnnotationScannerService();
    }
}