package com.additionaltools.hashcodevalidator;

import com.additionaltools.common.AnnotationScannerService;
import jakarta.annotation.PostConstruct;
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
    private static final Logger log = LoggerFactory.getLogger(HashCodeAnalysis.class);

    HashCodeAnalysis(AnnotationScannerService annotationScannerService, String basePath) {
        this.annotationScannerService = annotationScannerService;
        this.basePath = basePath;
    }

    @PostConstruct
    public void printStatistics() throws IOException {
        try {
            Set<Class<?>> entitiesInPackage = annotationScannerService.findEntitiesInPackage(basePath);
            for (Class<?> entity : entitiesInPackage) {
                InputStream entityStream = entity.getResourceAsStream(entity.getSimpleName() + ".class");
                ClassReader classReader = new ClassReader(entityStream);
                HashCodeFieldCollector collector = new HashCodeFieldCollector();
                classReader.accept(collector, 0);
                Set<String> fieldsUsedInHashCode = collector.getFieldsUsedInHashCode();
                Set<String> methodsUsedInHashCode = collector.getMethodsUsedInHashCode();
                Set<String> externalsId = Set.of("uuid", "naturalId");/* TODO: In the future, we'll allow users to configure this (e.g., adding additional identifiers).*/

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
                    log.warn("No hashCode implementation found for @Entity {}. It is recommended to implement hashCode," +
                             " preferably based on stable fields like a UUID generated at the application level.",
                            entity.getName());
                } else if (isProbablyIncorectImplementHashCodeFields) {
                    log.warn("The hashCode for {}} is calculated from {} fields. For @Entity classes, it is recommended" +
                             " that the hashCode be based on fields that remain stable throughout the lifecycle of the @Entity." +
                             " Ideally, use a single field such as a UUID generated at the application level.",
                            entity.getName(), fieldsUsedInHashCode.size());
                } else if (isProbablyIncorectImplementHashCodeByMethods) {
                    log.warn("The hashCode for {}} is calculated from {} fields. For @Entity classes, it is recommended" +
                             " that the hashCode be based on fields that remain stable throughout the lifecycle of the @Entity." +
                             " Ideally, use a single field such as a UUID generated at the application level.",
                            entity.getName(), methodsUsedInHashCode.size());
                }
            }
        } catch (Exception e) {
            log.error("Problem with HashCodeAnalysis", e);
        }
    }
}
