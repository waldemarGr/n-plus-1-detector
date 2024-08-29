package com.additionaltools.relationship;

import com.additionaltools.common.EntityFinderService;
import jakarta.annotation.PostConstruct;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * The {@code HashCodeAnalysis} class is responsible for analyzing the {@code hashCode} implementation
 * in entity classes within a specified package. It uses the {@link EntityFinderService} to locate
 * the entity classes and then examines their {@code hashCode} methods to ensure they are implemented correctly.
 */
public class RelationshipAnalysis {
    private final EntityFinderService entityFinderService;
    private final String basePath;
    private static final Logger log = LoggerFactory.getLogger(RelationshipAnalysis.class);

    public RelationshipAnalysis(EntityFinderService entityFinderService, String basePath) {
        this.entityFinderService = entityFinderService;
        this.basePath = basePath;
    }

    @PostConstruct
    public void printStatistics() throws IOException {
        Set<Class<?>> entitiesInPackage = entityFinderService.findEntitiesInPackage(basePath);
        for (Class<?> entity : entitiesInPackage) {
            ClassReader classReader = new ClassReader(entity.getName());
            RelationshipCollector collector = new RelationshipCollector(entity.getName());
            classReader.accept(collector, 0);
            Optional.ofNullable(collector.getEntityFieldWithListInfos()).stream()
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream)
                    .forEach(info ->
                            log.warn("Entity '{}' contains a field '{}' of type List with a '{}' relationship annotation. "
                                     + "Consider using a Set for improved performance and more efficient SQL queries.",
                                    info.entityName(), info.fieldName(), info.relationshipType())
                    );

        }
    }
}
