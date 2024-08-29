package com.additionaltools.relationship;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashSet;
import java.util.Set;

public class RelationshipCollector extends ClassVisitor {

    private final String entityName;
    private final Set<EntityFieldOptimizationInfo> entityFieldWithListInfos = new HashSet<>();

    public RelationshipCollector(String entityName) {
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
                } else if ("Ljakarta/persistence/ManyToOne;".equals(descriptor)) {
                    relationshipType = "@ManyToOne";
                } else if ("Ljakarta/persistence/ManyToMany;".equals(descriptor)) {
                    relationshipType = "@ManyToMany";
                } else if ("Ljakarta/persistence/CollectionTable;".equals(descriptor)) {//todo veryfi
                    relationshipType = "@CollectionTable";
                } else if ("Ljakarta/persistence/JoinTable;".equals(descriptor)) {
                    relationshipType = "@CollectionTable";
                }
                return super.visitAnnotation(descriptor, visible);
            }


            @Override
            public void visitEnd() {
                if (relationshipType != null && descriptor.contains("Ljava/util/List;")) {
                    EntityFieldOptimizationInfo entityFieldOptimizationInfo = new EntityFieldOptimizationInfo(entityName, fieldName, relationshipType);
                    entityFieldWithListInfos.add(entityFieldOptimizationInfo);
                }
                super.visitEnd();
            }
        };
    }

    Set<EntityFieldOptimizationInfo> getEntityFieldWithListInfos() {
        return entityFieldWithListInfos;
    }
}