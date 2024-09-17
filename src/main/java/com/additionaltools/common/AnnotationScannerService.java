package com.additionaltools.common;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.stereotype.Service;

import java.lang.annotation.Annotation;
import java.util.Set;


@Service
public class AnnotationScannerService {

    public Set<Class<?>> findInPackage(String basePackage, Class<? extends Annotation> annotation) {
        Reflections reflections = new Reflections(basePackage);
        return reflections.getTypesAnnotatedWith(annotation);
    }

    /**
     * Retrieves the package name of the first class annotated with the specified annotation.
     *
     * <p>This method scans all packages in the application context to find a class annotated
     * with the given annotation. It then returns the name of the package where the class is located.
     * If no class with the specified annotation is found, an {@link IllegalStateException} is thrown.</p>
     *
     * @param annotation the annotation class to search for. The method will look for classes
     *                   that are annotated with this annotation.
     * @return the package name of the first class found that is annotated with the specified annotation.
     * @throws IllegalStateException if no class annotated with the specified annotation is found
     *                               in the scanned packages.
     */
    public String getPackageNameForAnnotatedClass(Class<? extends Annotation> annotation) {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder()
                .forPackage("") // Search all packages
                .setScanners(Scanners.SubTypes, Scanners.TypesAnnotated);

        Reflections reflections = new Reflections(configurationBuilder);
        Set<Class<?>> startupClass = reflections.getTypesAnnotatedWith(annotation);
        Class<?> baseClass = startupClass.stream().findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No class annotated with @EnableHashCodeAnalysis was found in the scanned packages. " +
                        "Please ensure that there is at least one class with this annotation and that it is properly included in " +
                        "the package scanning configuration. If you are using a custom configuration, verify that the scanned packages " +
                        "are correctly specified."
                ));

        return baseClass.getPackageName();
    }
}

