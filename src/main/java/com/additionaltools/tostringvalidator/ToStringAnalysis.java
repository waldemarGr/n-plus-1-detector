package com.additionaltools.tostringvalidator;

import com.additionaltools.common.AnnotationScannerService;
import jakarta.annotation.PostConstruct;
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

    ToStringAnalysis(AnnotationScannerService annotationScannerService, String basePath) {
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
                ToStringFieldCollector collector = new ToStringFieldCollector();
                classReader.accept(collector, 0);
                Set<ToStringData> fieldsUsedInToString = collector.getFieldsUsedInHashCode();
                Set<ToStringData> methodsUsedInToString = collector.getMethodsUsedInHashCode();


                if (!methodsUsedInToString.isEmpty()) {
                    log.warn("""
                            The toString for {} contains entity methods that are potentially problematic. \
                            These fields might trigger additional lazy loading or other unintended consequences. \
                            Fields causing potential issues: {}
                            """, entity.getName(), methodsUsedInToString);
                } else if (!fieldsUsedInToString.isEmpty()) {
                    log.warn("""
                            The toString for {} contains entity fields that are potentially problematic. \
                            These fields might trigger additional lazy loading or other unintended consequences. \
                            Fields causing potential issues: {}
                            """, entity.getName(), fieldsUsedInToString);
                }
            }
        } catch (Exception e) {
            log.error("An error occurred during toString analysis", e);
        }
    }
}
