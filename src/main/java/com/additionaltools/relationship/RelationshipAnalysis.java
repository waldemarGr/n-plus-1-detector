package com.additionaltools.relationship;

import com.additionaltools.common.EntityFinderService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;


public class RelationshipAnalysis {
    private final EntityFinderService entityFinderService;
    private final String basePath;
    private static final Logger log = LoggerFactory.getLogger(RelationshipAnalysis.class);

    public RelationshipAnalysis(EntityFinderService entityFinderService, String basePath) {
        this.entityFinderService = entityFinderService;
        this.basePath = basePath;
    }

    @PostConstruct
    public void printStatistics() {

        try {
            Optional.ofNullable(collectEntityOptimizationData()).stream()
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream)
                    .forEach(info ->
                            log.warn("Entity '{}' contains a field '{}' of type '{}' with a '{}' relationship annotation. "
                                     + "Consider using a Set for improved performance and more efficient SQL queries.",
                                    info.entityName(), info.fieldName(), info.currentFieldType(), info.relationshipType())
                    );
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
        Set<Class<?>> entitiesInPackage = entityFinderService.findEntitiesInPackage(basePath);
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
