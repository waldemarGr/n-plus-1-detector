package com.additionaltools.relationship;


import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.FieldVisitor;
import org.springframework.asm.Opcodes;

import java.util.HashSet;
import java.util.Set;

public class RelationshipCollector extends ClassVisitor {

    private final String entityName;
    private final Set<EntityFieldOptimizationInfo> entityFieldWithListInfos = new HashSet<>();

    RelationshipCollector(String entityName) {
        super(Opcodes.ASM9);
        this.entityName = entityName;
    }

    @Override
    public FieldVisitor visitField(int access, String fieldName, String descriptor, String signature, Object value) {
        return new FieldVisitor(Opcodes.ASM9) {
            private String relationshipType = null;

            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                if ("Ljakarta/persistence/OneToMany;".equals(descriptor)) {
                    relationshipType = "@OneToMany";
                } else if ("Ljakarta/persistence/ManyToMany;".equals(descriptor)) {
                    relationshipType = "@ManyToMany";
                } else if ("Ljakarta/persistence/CollectionTable;".equals(descriptor)) {
                    relationshipType = "@CollectionTable";
                }
                return super.visitAnnotation(descriptor, visible);
            }

            @Override
            public void visitEnd() {
                if (relationshipType != null && (descriptor.contains("Ljava/util/List;") || descriptor.contains("Ljava/util/Collection;") || descriptor.contains("["))) {
                    String type = null;
                    if (descriptor.contains("Ljava/util/List;")) {
                        type = "List";
                    } else if (descriptor.contains("Ljava/util/Collection;")) {
                        type = "Collection";
                    } else if (descriptor.contains("[")) {
                        type = "Array";
                    }
                    EntityFieldOptimizationInfo entityFieldOptimizationInfo = new EntityFieldOptimizationInfo(entityName, fieldName, relationshipType, type);
                    entityFieldWithListInfos.add(entityFieldOptimizationInfo);
                }
            }
        };
    }

    Set<EntityFieldOptimizationInfo> getEntityFieldWithListInfos() {
        return entityFieldWithListInfos;
    }
}