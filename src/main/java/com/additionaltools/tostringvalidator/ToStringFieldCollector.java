package com.additionaltools.tostringvalidator;

//import com.additionaltools.relationship.EntityFieldOptimizationInfo;
//import org.objectweb.asm.ClassVisitor;
//import org.objectweb.asm.MethodVisitor;
//import org.objectweb.asm.Opcodes;

import org.springframework.asm.ClassVisitor;
import org.springframework.asm.MethodVisitor;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.asm.Opcodes.ACC_PUBLIC;
import static org.springframework.asm.Opcodes.ASM9;

/**
 * The {@code HashCodeFieldCollector} class is a custom {@link ClassVisitor} that visits bytecode of classes
 * to collect fields and methods used within the {@code hashCode} method. It extends {@code ClassVisitor}
 * from the ASM library to inspect class files.
 *
 * <p>This class is used to analyze which fields and methods are referenced by the {@code toString} method
 * to ensure that hashCode implementations adhere to best practices, such as using stable fields.
 */
public class ToStringFieldCollector extends ClassVisitor {

    private Set<ToStringData> fieldsUsedInToString = new HashSet<>();
    private Set<ToStringData> methodsUsedInToString = new HashSet<>();


    public ToStringFieldCollector() {
        super(ASM9);
    }

    /**
     * Visits a method in the class being analyzed.
     *
     * <p>If the method is {@code toSting} and is public, it returns a {@code MethodVisitor} that collects
     * fields and methods used within the {@code hashCode} method.
     *
     * @param access     The access flags of the method (e.g., {@code ACC_PUBLIC}).
     * @param name       The name of the method.
     * @param descriptor The method's descriptor.
     * @param signature  The method's signature.
     * @param exceptions The exceptions thrown by the method.
     * @return A {@code MethodVisitor} for the {@code hashCode} method, or {@code super.visitMethod} otherwise.
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if ("toString".equals(name) && (access & ACC_PUBLIC) != 0) {
            return new MethodVisitor(ASM9) {

                @Override
                public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                    if (180 == opcode && !descriptor.contains("Ljava/lang/")) {
                        ToStringData data = new ToStringData(name, removePrefix(descriptor));
                        fieldsUsedInToString.add(data);
                    }
                    super.visitFieldInsn(opcode, owner, name, descriptor);
                }

                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                    if (182 == opcode && !descriptor.contains("()Ljava/lang/")) {
                        ToStringData data = new ToStringData(name, removePrefix(descriptor));
                        methodsUsedInToString.add(data);
                    } else {
                        System.out.println(descriptor);
                    }
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                }
            };
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }


    public Set<ToStringData> getFieldsUsedInHashCode() {
        return fieldsUsedInToString;
    }

    public Set<ToStringData> getMethodsUsedInHashCode() {
        return methodsUsedInToString;
    }

    private String removePrefix(String description) {
        final String PREFIX = "()L";
        if (description.startsWith(PREFIX)) {
            return description.substring(PREFIX.length());
        }
        return description;
    }
}
