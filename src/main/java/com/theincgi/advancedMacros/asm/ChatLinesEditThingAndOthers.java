package com.theincgi.advancedMacros.asm;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.network.play.server.SPacketCollectItem;
import net.minecraft.network.play.server.SPacketConfirmTransaction;
import net.minecraft.network.play.server.SPacketMultiBlockChange;
import net.minecraft.network.play.server.SPacketOpenWindow;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.network.play.server.SPacketWindowItems;
import net.minecraft.util.EnumParticleTypes;
import scala.util.control.Exception.Catch;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Set;

import org.luaj.vm2_v3_0_1.LuaValue;
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
import com.theincgi.advancedMacros.event.PacketEventHandler;
import com.theincgi.advancedMacros.misc.Settings;
import com.theincgi.advancedMacros.misc.Utils;

import io.netty.util.IllegalReferenceCountException;

import static org.objectweb.asm.Opcodes.*;

public class ChatLinesEditThingAndOthers implements IClassTransformer{
	/**
	 * {@link GuiNewChat}
	 * */
	public static String[] editedClasses = {
			"net.minecraft.client.gui.GuiNewChat"
	};

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if(basicClass == null) return null;

		boolean isObf = !name.equals(transformedName);
		AdvancedMacrosCorePlugin.isObfuscated |= isObf;
		int index = Arrays.asList(editedClasses).indexOf(transformedName);


		return index==-1 ? basicClass : transform(index, basicClass, isObf);
	}

	private static byte[] transform(int index, byte[] basicClass, boolean isObf) {
		System.out.printf("AdvancedMacros is transforming '%s'\n", editedClasses[index]);
		try {
			ClassNode	node   = new ClassNode();
			ClassReader reader = new ClassReader(basicClass);
			reader.accept(node, 0);


			switch (index) {
			case 0: //gui new chat
				transformGuiNewChat(node, isObf);
				break;
			default:
				break;
			}


			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			node.accept(writer);
			return writer.toByteArray();

		} catch (Exception|Error e) {
			e.printStackTrace();
		}
		return basicClass;
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
							if(maxOps==0) { System.out.println("AdvancedMacros: max chat lines is now editable");break; }
						}
					}
				}

			}
		}
	}
	
	
	public static int getMaxLineCount() {
		try {
			return Utils.tableFromProp(Settings.settings, "chat.maxLines", LuaValue.valueOf(100)).checkint();
		}catch (Exception | Error e) {
			return 100;
		}
	}


}
