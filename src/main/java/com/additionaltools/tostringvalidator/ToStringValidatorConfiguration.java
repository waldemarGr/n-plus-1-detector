package com.additionaltools.tostringvalidator;

import com.additionaltools.common.AnnotationScannerService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to set up HashCodeAnalysis based on @EnableHashCodeAnalysis annotation.
 */
@Configuration
public class ToStringValidatorConfiguration {

    @Bean
    public ToStringAnalysis toStringAnalysis(AnnotationScannerService annotationScannerService) {
        String basePath = annotationScannerService.getPackageNameForAnnotatedClass(EnableToStringValidator.class);
        return new ToStringAnalysis(annotationScannerService, basePath);
    }

    @Bean
    @ConditionalOnMissingBean(AnnotationScannerService.class)
    public AnnotationScannerService annotationScannerService() {
        return new AnnotationScannerService();
    }
}