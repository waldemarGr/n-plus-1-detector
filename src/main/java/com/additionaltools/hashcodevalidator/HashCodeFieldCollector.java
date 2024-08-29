package com.additionaltools.hashcodevalidator;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashSet;
import java.util.Set;

/**
 * The {@code HashCodeFieldCollector} class is a custom {@link ClassVisitor} that visits bytecode of classes
 * to collect fields and methods used within the {@code hashCode} method. It extends {@code ClassVisitor}
 * from the ASM library to inspect class files.
 *
 * <p>This class is used to analyze which fields and methods are referenced by the {@code hashCode} method
 * to ensure that hashCode implementations adhere to best practices, such as using stable fields.
 */
public class HashCodeFieldCollector extends ClassVisitor {

    private Set<String> fieldsUsedInHashCode = new HashSet<>();
    private Set<String> methodsUsedInHashCode = new HashSet<>();

    public HashCodeFieldCollector() {
        super(Opcodes.ASM9);
    }

    /**
     * Visits a method in the class being analyzed.
     *
     * <p>If the method is {@code hashCode} and is public, it returns a {@code MethodVisitor} that collects
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
        if ("hashCode".equals(name) && (access & Opcodes.ACC_PUBLIC) != 0) { // Tylko publiczne metody
            return new MethodVisitor(Opcodes.ASM9) {

                @Override
                public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                    fieldsUsedInHashCode.add(name);
                    super.visitFieldInsn(opcode, owner, name, descriptor);
                }

                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                    methodsUsedInHashCode.add(name);
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                }
            };
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    public Set<String> getFieldsUsedInHashCode() {
        return fieldsUsedInHashCode;
    }

    public Set<String> getMethodsUsedInHashCode() {
        return methodsUsedInHashCode;
    }
}