package de.olfillasodikno.agent;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class CryptVisitor extends ClassVisitor{
		
	private String name;
	public CryptVisitor(ClassVisitor cv) {
		super(Opcodes.ASM5, cv);
	}
	
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		this.name = name;
		super.visit(version, access, name, signature, superName, interfaces);
	}
	
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		if(desc.equals("(Ljavax/crypto/SecretKey;)V")&&(!this.name.startsWith("sun"))) {
			return new CryptMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions));
		}
		return super.visitMethod(access, name, desc, signature, exceptions);
	}
	
	public static final class CryptMethodVisitor extends MethodVisitor{

		public CryptMethodVisitor(MethodVisitor mv) {
			super(Opcodes.ASM5, mv);
		}
		
		@Override
		public void visitCode() {
			super.visitCode();
			super.visitVarInsn(Opcodes.ALOAD, 1);
			super.visitMethodInsn(Opcodes.INVOKESTATIC, "de/olfillasodikno/agent/Agent", "save", "(Ljavax/crypto/SecretKey;)V", false);
		}
	}
}
