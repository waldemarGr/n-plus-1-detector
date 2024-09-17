package com.additionaltools.tostringvalidator;

import com.additionaltools.common.AnnotationScannerService;
import com.additionaltools.logging.LoggingService;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * The {@code ToStringAnalysis} class is responsible for analyzing the {@code toSting} implementation
 * in entity classes within a specified package. It uses the {@link AnnotationScannerService} to locate
 * the entity classes and then examines their {@code toSting} methods to ensure they are implemented correctly.
 */
public class ToStringAnalysis {
    private final AnnotationScannerService annotationScannerService;
    private final String basePath;
    private static final Logger log = LoggerFactory.getLogger(ToStringAnalysis.class);
    private final LoggingService loggingService;

    ToStringAnalysis(AnnotationScannerService annotationScannerService, String basePath, LoggingService loggingService) {
        this.annotationScannerService = annotationScannerService;
        this.basePath = basePath;
        this.loggingService = loggingService;
    }

    @PostConstruct
    public void printStatistics() {
        try {
            Set<Class<?>> entitiesInPackage = annotationScannerService.findInPackage(basePath, Entity.class);
            for (Class<?> entity : entitiesInPackage) {
                InputStream entityStream = entity.getResourceAsStream(entity.getSimpleName() + ".class");
                ClassReader classReader = new ClassReader(entityStream);
                ToStringFieldCollector collector = new ToStringFieldCollector();
                classReader.accept(collector, 0);
                Set<ToStringData> fieldsUsedInToString = collector.getFieldsUsedInHashCode();
                Set<ToStringData> methodsUsedInToString = collector.getMethodsUsedInHashCode();

                if (!methodsUsedInToString.isEmpty()) {
                    String message = """
                            TO_STRING_CONTAINS_ASSOCIATIONS: The .toString() for %s contains entity methods that are potentially problematic. \
                            These fields might trigger additional lazy loading or other unintended consequences. \
                            Fields causing potential issues: %s \
                            To prevent performance hits and unexpected side effects, consider excluding these methods from the .toString() method.
                            """.formatted(entity.getName(), methodsUsedInToString);
                    loggingService.addLog(message);
                    log.warn(message);
                } else if (!fieldsUsedInToString.isEmpty()) {
                    String message = """
                            TO_STRING_CONTAINS_ASSOCIATIONS: The .toString() for %s contains entity fields that are potentially problematic. \
                            These fields might trigger additional lazy loading or other unintended consequences. \
                            Fields causing potential issues: %s \
                            To prevent performance hits and unexpected side effects, consider excluding these fields from the .toString() method.
                            """.formatted(entity.getName(), fieldsUsedInToString);
                    loggingService.addLog(message);
                    log.warn(message);
                }
            }
        } catch (Exception e) {
            log.error("An error occurred during toString analysis", e);
        }
    }
}
