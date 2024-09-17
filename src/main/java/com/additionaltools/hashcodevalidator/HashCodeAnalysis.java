package com.additionaltools.hashcodevalidator;

import com.additionaltools.common.AnnotationScannerService;
import com.additionaltools.logging.LoggingService;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.Entity;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * The {@code HashCodeAnalysis} class is responsible for analyzing the {@code hashCode} implementation
 * in entity classes within a specified package. It uses the {@link AnnotationScannerService} to locate
 * the entity classes and then examines their {@code hashCode} methods to ensure they are implemented correctly.
 */
public class HashCodeAnalysis {
    private final AnnotationScannerService annotationScannerService;
    private final String basePath;
    private final LoggingService loggingService;
    private static final Logger log = LoggerFactory.getLogger(HashCodeAnalysis.class);

    HashCodeAnalysis(AnnotationScannerService annotationScannerService, String basePath, LoggingService loggingService) {
        this.annotationScannerService = annotationScannerService;
        this.basePath = basePath;
        this.loggingService = loggingService;
    }

    @PostConstruct
    public void printStatistics() throws IOException {
        try {
            Set<Class<?>> entitiesInPackage = annotationScannerService.findInPackage(basePath, Entity.class);
            for (Class<?> entity : entitiesInPackage) {
                InputStream entityStream = entity.getResourceAsStream(entity.getSimpleName() + ".class");
                ClassReader classReader = new ClassReader(entityStream);
                HashCodeFieldCollector collector = new HashCodeFieldCollector();
                classReader.accept(collector, 0);
                Set<String> fieldsUsedInHashCode = collector.getFieldsUsedInHashCode();
                Set<String> methodsUsedInHashCode = collector.getMethodsUsedInHashCode();
                Set<String> externalsId = Set.of("uuid", "naturalId");
                /**
                 * @return true if:
                 *         - The set of fields contains more than one element; or
                 *         - The set of fields contains exactly one element, and this element does not contain
                 *           any of the strings in the first element of the external IDs set (in lowercase).
                 */
                boolean isProbablyIncorectImplementHashCodeFields = fieldsUsedInHashCode.size() > 1 ||
                                                                    (fieldsUsedInHashCode.size() == 1 && fieldsUsedInHashCode.stream().findAny().stream()
                                                                            .noneMatch(s -> externalsId.iterator().next().contains(s.toLowerCase())));

                boolean isProbablyIncorectImplementHashCodeByMethods = methodsUsedInHashCode.size() > 1 ||
                                                                       (methodsUsedInHashCode.size() == 1 && methodsUsedInHashCode.stream().findAny().stream()
                                                                               .noneMatch(s -> externalsId.iterator().next().contains(s.toLowerCase())));

                if (fieldsUsedInHashCode.isEmpty() && methodsUsedInHashCode.isEmpty()) {
                    String message = """
                            MISSING_HASHCODE: No hashCode implementation found for @Entity %s. It is recommended to implement hashCode, \
                            preferably based on stable fields like a UUID or naturalId generated at the application level."""
                            .formatted(entity.getName());
                    loggingService.addLog(message);
                    log.warn(message);
                } else if (isProbablyIncorectImplementHashCodeFields) {
                    String message = """
                            HASHCODE_INCORRECT_FIELDS: The hashCode for %s is calculated from %s fields. For @Entity classes, it is recommended\
                             that the hashCode be based on fields that remain stable throughout the lifecycle of the @Entity.\
                             Ideally, use a single field such as a UUID or naturalId generated at the application level."""
                            .formatted(entity.getName(), fieldsUsedInHashCode.size());
                    loggingService.addLog(message);
                    log.warn(message);
                } else if (isProbablyIncorectImplementHashCodeByMethods) {
                    String message = """
                            HASHCODE_INCORRECT_METHODS: The hashCode for %s is calculated from %s fields. For @Entity classes, it is recommended\
                             that the hashCode be based on fields that remain stable throughout the lifecycle of the @Entity.\
                             Ideally, use a single field such as a UUID or naturalId  generated at the application level."""
                            .formatted(entity.getName(), methodsUsedInHashCode.size());
                    log.warn(message);
                    loggingService.addLog(message);
                }
            }
        } catch (Exception e) {
            log.error("Problem with HashCodeAnalysis", e);
        }
    }
}
