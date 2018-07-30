package com.theincgi.advancedMacros.asm;


import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.*;

public class ChatLinesEditThing implements IClassTransformer{

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if(basicClass == null) return null;
		ClassReader reader = new ClassReader(basicClass);
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);

		ClassVisitor visitor = writer;
		visitor = new ChatLinesVisitor( visitor );
		
		reader.accept(visitor, 0);
		return writer.toByteArray();
	}


	public static class ChatLinesVisitor extends ClassVisitor {
		private String clsName = null;
		private static final String callbackOwner = org.objectweb.asm.Type.getInternalName(ChatLinesVisitor.class);


		private ChatLinesVisitor(ClassVisitor cv) {
			super(org.objectweb.asm.Opcodes.ASM5, cv);
		}

		@Override
		public void visit(int version, int access, String name, String signature, String superName,
				String[] interfaces) {
			super.visit(version, access, name, signature, superName, interfaces);
			this.clsName = name;
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

			return new MethodVisitor(Opcodes.ASM5, super.visitMethod(access, name, desc, signature, exceptions)) {
				@Override
				public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
					if(opcode == Opcodes.INVOKEVIRTUAL &&
					   owner.equals("net/minecraft/client/gui/GuiNewChat") &&
					   name .equals("setChatLine"))
						System.out.printf("Method call located!\nDetails:\n\t%s\n\t%s", desc, String.valueOf(itf));
						
					super.visitMethodInsn(opcode, owner, name, desc, itf);
				}
			};
		}
	}
}
