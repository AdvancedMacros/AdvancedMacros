package com.theincgi.advancedMacros.asm;


import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.launchwrapper.IClassTransformer;

import java.util.Arrays;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.misc.Settings;

import static org.objectweb.asm.Opcodes.*;

public class ChatLinesEditThingAndOthers implements IClassTransformer{
	/**
	 * {@link GuiNewChat}
	 * */
	public static String[] editedClasses = {
			"net.minecraft.client.gui.GuiNewChat",
			"net.minecraft.item.ItemStack"
	};

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if(basicClass == null) return null;

		boolean isObf = !name.equals(transformedName);
		int index = Arrays.asList(editedClasses).indexOf(transformedName);


		return index==-1 ? basicClass : transform(index, basicClass, isObf);





		//		ClassReader reader = new ClassReader(basicClass);
		//		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		//
		//		ClassVisitor visitor = writer;
		//		visitor = new ChatLinesVisitor( visitor );
		//		
		//		reader.accept(visitor, 0);
		//		return writer.toByteArray();
	}

	private static byte[] transform(int index, byte[] basicClass, boolean isObf) {
		System.out.printf("Transforming '%s'", editedClasses[index]);
		try {
			ClassNode	node   = new ClassNode();
			ClassReader reader = new ClassReader(basicClass);
			reader.accept(node, 0);


			switch (index) {
			case 0: //gui new chat
				transformGuiNewChat(node, isObf);
				break;
			case 1: //itemTool
				transormItemTool(node, isObf);
				break;
			default:
				break;
			}


			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			node.accept(writer);
			return writer.toByteArray();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return basicClass;
	} 


	private static void transormItemTool(ClassNode node, boolean isObf) {
		final String DAMAGE_ITEM = isObf? "a" : "damageItem";
		final String DESC = isObf? "(ILvn;)V" : "(ILnet/minecraft/entity/EntityLivingBase;)V"; 

		for(MethodNode method : node.methods) {
			if(method.name.equals(DAMAGE_ITEM) && method.desc.equals(DESC)) {
				for( AbstractInsnNode instr : method.instructions.toArray()) {
//					Target:
//						Label l7 = new Label(); [AFTER THIS]
//						mv.visitLabel(l7);
//						mv.visitLineNumber(397, l7);
//						mv.visitVarInsn(ALOAD, 0);
//						mv.visitInsn(ICONST_1);
//						mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/item/ItemStack", "shrink", "(I)V", false);
//					Label l8 = new Label();
					if(instr.getOpcode()==ALOAD && instr.getNext().getOpcode()==ICONST_1 && instr.getNext().getNext().getOpcode()==INVOKEVIRTUAL) {
						VarInsnNode aloadIns = (VarInsnNode) instr;
						if(aloadIns.var!=0) 
							break;
						InsnList actions = new InsnList();
						//GETSTATIC AdvancedMacros.forgeEventHandler : ForgeEventHandler
						//mv.visitFieldInsn(GETSTATIC, "com/theincgi/advancedMacros/AdvancedMacros", "forgeEventHandler", "Lcom/theincgi/advancedMacros/ForgeEventHandler;");
						actions.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(AdvancedMacros.class), "forgeEventHandler", "Lcom/theincgi/advancedMacros/ForgeEventHandler;"));
						actions.add(new VarInsnNode(ALOAD, 0));
						actions.add(new MethodInsnNode(INVOKEVIRTUAL, 
										"com/theincgi/advancedMacros/ForgeEventHandler",
										"onItemBreak", 
										isObf?"(Lain;)V":"(Lnet/minecraft/item/ItemStack;)V", 
										false)
								);
//						actions.add(new VarInsnNode(ALOAD, 0));
//						actions.add(new MethodInsnNode(INVOKEDYNAMIC, 
//								"com/theincgi/advancedMacros/ForgeEventHandler",
//								"helloByteCode", 
//								isObf?"()V":"()V", 
//								false)
//						);
						method.instructions.insertBefore(instr, actions);
						System.out.println("Added call to AdvancedMacros.forgeEventHandler#onBreakItem before this.shrink(1)");
						break;
					}
				}
			}
		}
	}

	private static void transformGuiNewChat(ClassNode node, boolean isObf) {
		final String SET_CHAT_LINE = isObf? "a" : "setChatLine";
		final String DESCRIPTOR = isObf? "(Lhh;IIZ)V" : "(Lnet/minecraft/util/text/ITextComponent;IIZ)V";
		/*
		 * 	INVOKEINTERFACE List.size() : int
    		BIPUSH 100
    		IF_ICMPLE L13

    		and

    		INVOKEINTERFACE List.size() : int
    		BIPUSH 100
    		IF_ICMPLE L15
		 * */
		for(MethodNode method : node.methods) {
			if(method.name.equals(SET_CHAT_LINE) && method.desc.equals(DESCRIPTOR)) {
				AbstractInsnNode target = null;
				int maxOps = 2;
				for (AbstractInsnNode instr : method.instructions.toArray()) {
					if(instr.getOpcode() == BIPUSH) {
						IntInsnNode intInstruct = (IntInsnNode) instr;
						if (intInstruct.operand == 100 && intInstruct.getNext().getOpcode() == IF_ICMPLE) {
							target = instr;
							maxOps--;

							MethodInsnNode myMethod = new MethodInsnNode(
									INVOKESTATIC,
									"com/theincgi/advancedMacros/asm/ChatLinesEditThingAndOthers", //class name
									"getMaxLineCount",  //method name
									"()I",  //no args, returns int
									false); //not interface
							method.instructions.insertBefore(target, myMethod);
							method.instructions.remove(target);
							if(maxOps==0) break;
						}
					}
				}

			}
		}
	}
	int value;
	public void whatWouldItLookLike() {
		AdvancedMacros.forgeEventHandler.onItemBreak(null);
	}
	public static int getMaxLineCount() {
		return Settings.settings.get("chatMaxLines").optint(100);
	}


}
