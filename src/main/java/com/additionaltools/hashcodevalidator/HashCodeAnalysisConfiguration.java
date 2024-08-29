package com.additionaltools.hashcodevalidator;

import com.additionaltools.common.EntityFinderService;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * Configuration class to set up HashCodeAnalysis based on @EnableHashCodeAnalysis annotation.
 */
@Configuration
public class HashCodeAnalysisConfiguration {

    @Bean
    public HashCodeAnalysis hashCodeAnalysis(EntityFinderService entityFinderService) {
        String basePath = getBasePathFromAnnotation();
        return new HashCodeAnalysis(entityFinderService, basePath);
    }

    @Bean
    @ConditionalOnMissingBean(EntityFinderService.class)
    public EntityFinderService entityFinderService() {
        return new EntityFinderService();
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
        Set<Class<?>> entities = reflections.getTypesAnnotatedWith(EnableHashCodeAnalysis.class);
        Class<?> baseClass = entities.stream().findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No class annotated with @EnableHashCodeAnalysis was found in the scanned packages. " +
                        "Please ensure that there is at least one class with this annotation and that it is properly included in " +
                        "the package scanning configuration. If you are using a custom configuration, verify that the scanned packages " +
                        "are correctly specified."
                ));

        return baseClass.getPackageName();
    }

}