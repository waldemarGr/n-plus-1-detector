package com.additionaltools.relationship;

import com.additionaltools.common.AnnotationScannerService;
import com.additionaltools.common.EmptyLoggingConfiguration;
import com.additionaltools.logging.LoggingService;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Set;

@Import(EmptyLoggingConfiguration.class)
@Configuration
public class RelationshipAnalysisConfiguration {

    @Bean
    public RelationshipAnalysis relationshipAnalysis(AnnotationScannerService annotationScannerService, LoggingService loggingService) {
        String basePath = getBasePathFromAnnotation();
        return new RelationshipAnalysis(annotationScannerService, basePath, loggingService);
    }

    @Bean
    @ConditionalOnMissingBean(AnnotationScannerService.class)
    public AnnotationScannerService entityFinderService() {
        return new AnnotationScannerService();
    }

    /**
     * Retrieves the basePath value from the @EnableHashCodeAnalysis annotation on the application class.
     *
     * @return the basePath value
     */
    private String getBasePathFromAnnotation() {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder()
                .forPackage("") // Search all packages
                .setScanners(Scanners.SubTypes, Scanners.TypesAnnotated);

        Reflections reflections = new Reflections(configurationBuilder);
        Set<Class<?>> entities = reflections.getTypesAnnotatedWith(EnableRelationshipAnalysis.class);
        Class<?> baseClass = entities.stream().findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No class annotated with @EnableRelationshipAnalysis was found in the scanned packages. " +
                        "Please ensure that there is at least one class with this annotation and that it is properly included in " +
                        "the package scanning configuration. If you are using a custom configuration, verify that the scanned packages " +
                        "are correctly specified."
                ));

        return baseClass.getPackageName();
    }

}