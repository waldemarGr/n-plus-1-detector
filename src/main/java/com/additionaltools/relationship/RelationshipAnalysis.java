package com.additionaltools.relationship;

import com.additionaltools.common.AnnotationScannerService;
import com.additionaltools.logging.LoggingService;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;


public class RelationshipAnalysis {
    private final AnnotationScannerService annotationScannerService;
    private final String basePath;
    private static final Logger log = LoggerFactory.getLogger(RelationshipAnalysis.class);
    private final LoggingService loggingService;

    RelationshipAnalysis(AnnotationScannerService annotationScannerService, String basePath, LoggingService loggingService) {
        this.annotationScannerService = annotationScannerService;
        this.basePath = basePath;
        this.loggingService = loggingService;
    }

    @PostConstruct
    public void printStatistics() {

        try {
            Optional.ofNullable(collectEntityOptimizationData()).stream()
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream)
                    .forEach(info -> {
                        String message = """
                                INEFFICIENT_COLLECTION_TYPE: Entity %s contains a field %s of type %s with a %s relationship annotation. \
                                Consider using a Set for improved performance and more efficient SQL queries."""
                                .formatted(info.entityName(), info.fieldName(), info.currentFieldType(), info.relationshipType());
                        loggingService.addLog(message);
                        log.warn(message);
                    });
        } catch (Exception e) {
            log.error("Problem with RelationshipAnalysis", e);
        }
    }

    /**
     * Should be a set type
     * Prepares the analysis data for entity field optimization.
     *
     * <p>This method scans for entities in the specified package, analyzes their relationships,
     * and collects information about fields that should be optimized. It returns a set of
     * {@link EntityFieldOptimizationInfo} containing the collected data.
     *
     * @return a set of {@link EntityFieldOptimizationInfo} with details about entity fields
     * that require optimization.
     * @throws IOException if an I/O error occurs during class reading.
     */
    public Set<EntityFieldOptimizationInfo> collectEntityOptimizationData() throws IOException {
        Set<EntityFieldOptimizationInfo> entityFieldWithListInfos = new HashSet<>();
        Set<Class<?>> entitiesInPackage = annotationScannerService.findInPackage(basePath, Entity.class);
        for (Class<?> entity : entitiesInPackage) {
            InputStream resourceAsStream = entity.getResourceAsStream(entity.getSimpleName() + ".class");
            ClassReader classReader = new ClassReader(resourceAsStream);
            RelationshipCollector collector = new RelationshipCollector(entity.getName());
            classReader.accept(collector, 0);
            entityFieldWithListInfos.addAll(collector.getEntityFieldWithListInfos());
        }
        return entityFieldWithListInfos;
    }
}
