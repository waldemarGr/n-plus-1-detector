package com.additionaltools.hashcodevalidator;

import com.additionaltools.common.EntityFinderService;
import jakarta.annotation.PostConstruct;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

/**
 * The {@code HashCodeAnalysis} class is responsible for analyzing the {@code hashCode} implementation
 * in entity classes within a specified package. It uses the {@link EntityFinderService} to locate
 * the entity classes and then examines their {@code hashCode} methods to ensure they are implemented correctly.
 */
public class HashCodeAnalysis {
    private final EntityFinderService entityFinderService;
    private final String basePath;
    private static final Logger log = LoggerFactory.getLogger(HashCodeAnalysis.class);

    public HashCodeAnalysis(EntityFinderService entityFinderService, String basePath) {
        this.entityFinderService = entityFinderService;
        this.basePath = basePath;
    }

    @PostConstruct
    public void printStatistics() throws IOException {
        Set<Class<?>> entitiesInPackage = entityFinderService.findEntitiesInPackage(basePath);
        for (Class<?> entity : entitiesInPackage) {
            ClassReader classReader = new ClassReader(entity.getName());
            HashCodeFieldCollector collector = new HashCodeFieldCollector();
            classReader.accept(collector, 0);
            Set<String> fields = collector.getFieldsUsedInHashCode();
            Set<String> externalsId = Set.of("uuid", "naturalId");// TODO: In the future, we'll allow users to configure this (e.g., adding additional identifiers).

            /**
             * @return true if:
             *         - The set of fields contains more than one element; or
             *         - The set of fields contains exactly one element, and this element does not contain
             *           any of the strings in the first element of the external IDs set (in lowercase).
             */
            boolean isProbablyIncorectImplementHascCode = fields.size() > 1 ||
                                                          (fields.size() == 1 && fields.stream().findAny().stream()
                                                                  .noneMatch(s -> externalsId.iterator().next().contains(s.toLowerCase())));
            if (fields.isEmpty()) {
                log.warn("No hashCode implementation found for @Entity {}. It is recommended to implement hashCode," +
                         " preferably based on stable fields like a UUID generated at the application level.",
                        entity.getName());

            } else if (isProbablyIncorectImplementHascCode) {
                log.warn("The hashCode for {}} is calculated from {} fields. For @Entity classes, it is recommended" +
                         " that the hashCode be based on fields that remain stable throughout the lifecycle of the @Entity." +
                         " Ideally, use a single field such as a UUID generated at the application level.",
                        entity.getName(), fields.size());
            }
        }
    }
}
