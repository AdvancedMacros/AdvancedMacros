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

import io.netty.util.IllegalReferenceCountException;

import static org.objectweb.asm.Opcodes.*;

public class ChatLinesEditThingAndOthers implements IClassTransformer{
	/**
	 * {@link GuiNewChat}
	 * */
	public static String[] editedClasses = {
			"net.minecraft.client.gui.GuiNewChat"//,
			//"net.minecraft.item.ItemStack",
			//,"io.netty.channel.AbstractChannelHandlerContext"//TODO this asm this one is for testing dont keep
			,"io.netty.channel.local.LocalChannel"
	};

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if(basicClass == null) return null;

		boolean isObf = !name.equals(transformedName);
		AdvancedMacrosCorePlugin.isObfuscated |= isObf;
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
		System.out.printf("AdvancedMacros is transforming '%s'\n", editedClasses[index]);
		try {
			ClassNode	node   = new ClassNode();
			ClassReader reader = new ClassReader(basicClass);
			reader.accept(node, 0);


			switch (index) {
			case 0: //gui new chat
				transformGuiNewChat(node, isObf);
				break;
				//			case 1: //itemTool
				//				transormItemTool(node, isObf);
				//				break;
			case 1: 
				transformLocalChannelRead(node, isObf);
				transformLocalChannelWrite(node, isObf);
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

	//A note about this transformation, after reading this:
	//http://www.minecraftforge.net/forum/topic/2715-entityitempickupevent-lacking/
	//it seems that the pickup event (and likely others) are designed intentionally to be
	//server side only so instead I find myself listening to packets....
	//Forge events would have been preferred to this, but this is what I got
	//(I will admit that it certainly is educational to see how all these packets work real time)
	//TODO replace if an alternitive method becomes available
	/**
	 * Attaches a call to getPacketHacky() so that CLIENT_BOUND packets can be monitored
	 * */
	private static void transformLocalChannelRead(ClassNode node, boolean isObf) {
		if(isObf) return;
//		final String INVOKE_CHAN_READ = isObf? null : "invokeChannelRead";
//		final String DESC = isObf? null : "(Lio/netty/channel/AbstractChannelHandlerContext;Ljava/lang/Object;)V";
		final String INVOKE_CHAN_READ = isObf? null : "finishPeerRead0";
		final String DESC = isObf? null : "(Lio/netty/channel/local/LocalChannel;)V";
		for(MethodNode method : node.methods) {
			//System.out.println(method.name + " " + method.desc);
			if(method.name.equals(INVOKE_CHAN_READ) && method.desc.equals(DESC)) {
				//System.out.println("Located invokeChannel Read");
				//aload 2 for getting m
				System.out.println(INVOKE_CHAN_READ+" located");
				
				for(AbstractInsnNode instr : method.instructions.toArray()) {
					if(instr.getOpcode()==ASTORE) {
						VarInsnNode astore = (VarInsnNode) instr;
						if(astore.var==4) { //use to be 2
							
							MethodInsnNode myMethod = new MethodInsnNode(
									INVOKESTATIC,
									"com/theincgi/advancedMacros/asm/ChatLinesEditThingAndOthers", //class name
									"getPacketHackyRead",  //method name
									"(Ljava/lang/Object;)V",  //no args, returns int
									false); //not interface
							VarInsnNode loadM = new VarInsnNode(ALOAD, 4); //use to be 2
							InsnList list = new InsnList();
							list.add(loadM);
							list.add(myMethod);
							
							method.instructions.insert(instr, list);
							
							System.out.println("registered hook in " + INVOKE_CHAN_READ + " of " + editedClasses[1]);
							return;
						}
					}
				}
				
				
			}
		}
	}
	
	private static void transformLocalChannelWrite(ClassNode node, boolean isObf) {
		if(isObf) return;
//		final String INVOKE_CHAN_READ = isObf? null : "invokeChannelRead";
//		final String DESC = isObf? null : "(Lio/netty/channel/AbstractChannelHandlerContext;Ljava/lang/Object;)V";
		final String INVOKE_CHAN_READ = isObf? null : "doWrite";
		final String DESC = isObf? null : "(Lio/netty/channel/ChannelOutboundBuffer;)V";
		for(MethodNode method : node.methods) {
			//System.out.println(method.name + " " + method.desc);
			if(method.name.equals(INVOKE_CHAN_READ) && method.desc.equals(DESC)) {
				//System.out.println("Located invokeChannel Read");
				//aload 2 for getting m
				System.out.println("doWrite located");
				
				for(AbstractInsnNode instr : method.instructions.toArray()) {
					if(instr.getOpcode()==ASTORE) {
						VarInsnNode astore = (VarInsnNode) instr;
						if(astore.var==3) { //use to be 2
							
							MethodInsnNode myMethod = new MethodInsnNode(
									INVOKESTATIC,
									"com/theincgi/advancedMacros/asm/ChatLinesEditThingAndOthers", //class name
									"getPacketHackySend",  //method name
									"(Ljava/lang/Object;)V",  //no args, returns int
									false); //not interface
							VarInsnNode loadM = new VarInsnNode(ALOAD, 3); //use to be 2
							InsnList list = new InsnList();
							list.add(loadM);
							list.add(myMethod);
							
							method.instructions.insert(instr, list);
							
							System.out.println("registered hook in " + INVOKE_CHAN_READ + " of " + editedClasses[1]);
							return;
						}
					}
				}
				
				
			}
		}
	}
	
	private static boolean collectItemFlag = false;
	private static boolean brokenItemFlag = false;
	/**
	 * Seems packets like to show up in groups of 3<br>
	 * they all have the same hash code, so I've used that to block out the dupes.
	 * */
	public static LinkedList<Object> recentPackets = new LinkedList<>(); 
	public static void getPacketHackySend(Object packet) {
		
	}
	public static void getPacketHackyRead(Object packet) {
		try {
		if(AdvancedMacros.isServerSide()) return;
		try{if(recentPackets.contains(packet)) return;}catch (IllegalReferenceCountException e) {
			//TODO wth is this, must find out
//			io.netty.util.IllegalReferenceCountException: refCnt: 0
//			 	at io.netty.buffer.AbstractByteBuf.ensureAccessible(AbstractByteBuf.java:1408)
//			 	at io.netty.buffer.AbstractByteBuf.checkIndex(AbstractByteBuf.java:1347)
//			 	at io.netty.buffer.AbstractByteBuf.getLong(AbstractByteBuf.java:441)
//			 	at io.netty.buffer.ByteBufUtil.equals(ByteBufUtil.java:198)
//			 	at io.netty.buffer.ByteBufUtil.equals(ByteBufUtil.java:235)
//			 	at io.netty.buffer.AbstractByteBuf.equals(AbstractByteBuf.java:1311)
//			 	at java.util.LinkedList.indexOf(LinkedList.java:605)
//			 	at java.util.LinkedList.contains(LinkedList.java:317)
		}
		
		if(packet==null) return;
		String s = packet.getClass().toString();
		if(s.contains("Teleport") || s.contains("HeadLook") || s.contains("Velocity")) return;
		if(s.contains("LookMove") || s.contains("Position") || s.contains("RelMove")) return;
		if(s.contains("EntityStatus") || s.contains("TimeUpdate") || s.contains("KeepAlive")) return;
		
		recentPackets.add(packet);
		if(recentPackets.size()>40) recentPackets.removeFirst();
		switch(s) {
		case "class net.minecraft.network.play.server.SPacketEntityMetadata":
		case "class net.minecraft.network.play.server.SPacketDestroyEntities":
		case "class net.minecraft.network.play.server.SPacketBlockAction":
		case "class net.minecraft.network.play.server.SPacketEntityProperties":
		case "class net.minecraft.network.play.server.SPacketSpawnMob":
		case "class net.minecraft.network.play.server.SPacketEntity$S16PacketEntityLook":
		case "class net.minecraft.network.play.server.SPacketEffect":
		case "class net.minecraft.network.play.server.SPacketBlockChange":
		case "class net.minecraft.network.play.client.CPacketPlayer$Rotation":
		case "class net.minecraft.network.play.client.CPacketAnimation":
		case "class net.minecraft.network.play.server.SPacketSoundEffect":
		case "class net.minecraft.network.play.server.SPacketChunkData":
		case "class net.minecraft.network.play.server.SPacketUnloadChunk":
		case "class io.netty.buffer.UnpooledHeapByteBuf":
			return;
		case "class net.minecraft.network.play.server.SPacketPlayerListItem":{
			SPacketPlayerListItem pli = (SPacketPlayerListItem) packet;
			return;
		}case "class io.netty.buffer.PooledUnsafeDirectByteBuf":{
			return;
		}
		case "class net.minecraft.network.play.server.SPacketSpawnObject":{
			SPacketSpawnObject obj = (SPacketSpawnObject) packet;
			return;
		}
		case "class net.minecraft.network.play.server.SPacketSetSlot":{
			SPacketSetSlot slot = (SPacketSetSlot) packet;
			System.out.printf("SPacketSetSlot: %10s x %2d, Slot: %3d, WinID: %2d, OBJ: %s\n", slot.getStack().getDisplayName(), slot.getStack().getCount(), slot.getSlot(), slot.getWindowId(), packet.hashCode());
			if(collectItemFlag) {
				collectItemFlag = false;
				PacketEventHandler.onItemPickup(slot.getStack(), slot.getSlot());
			}else if(brokenItemFlag && slot.getStack().isEmpty()) {
				ItemStack orig = Minecraft.getMinecraft().player.inventory.getStackInSlot(slot.getSlot());
				PacketEventHandler.onItemBreak(slot.getSlot(), orig);
				brokenItemFlag = false;
			}
			return;
		}
		case "class net.minecraft.network.play.server.SPacketParticles":{
			SPacketParticles part = (SPacketParticles) packet;
			System.out.printf("Particles: %s\n", part.getParticleType());
			if(part.getParticleType().equals(EnumParticleTypes.ITEM_CRACK))
				brokenItemFlag = true;
			return;
		}
		case "class net.minecraft.network.play.server.SPacketRespawn":{
			SPacketRespawn sR = (SPacketRespawn) packet;
			System.out.printf("Respawn: %4d, %10s, %10s, %10s\n", sR.getDimensionID(), sR.getDifficulty(), sR.getGameType(), sR.getWorldType());
			return;
		}
		case "class net.minecraft.network.play.server.SPacketConfirmTransaction":{
			SPacketConfirmTransaction transact = (SPacketConfirmTransaction) packet;
			System.out.printf("TransactionConfirm: Action Num: %2d\n", transact.getActionNumber());
			return;
		}
		case "class net.minecraft.network.play.server.SPacketOpenWindow":{
			SPacketOpenWindow open = (SPacketOpenWindow) packet;
			System.out.printf("OpenWindow: %s, #slots: %2d\n", open.getGuiId(), open.getSlotCount());
			return;
		}
//		case "class net.minecraft.network.play.server.SPacketWindowItems":{
//			SPacketWindowItems win = (SPacketWindowItems) packet;
//			System.out.printf("WindowItems: %2d items in list\n", win.getItemStacks().size());
//			for(int i = 0; i<win.getItemStacks().size(); i++) {
//				ItemStack stac = win.getItemStacks().get(i);
//				System.out.printf("    %2d: %s\n", i, stac.getDisplayName());
//			}
//			//this should just notify AM that the container is ready for viewing
//			AdvancedMacros.forgeEventHandler.notifyContainerReady(win.getWindowId(), win.getItemStacks());
//			return;
//		}
		case "class net.minecraft.network.play.server.SPacketCollectItem":{
			SPacketCollectItem collectItem = (SPacketCollectItem) packet;
			System.out.printf("CollectItem: Amount: %2d, ItemEID: %4d, EID: %4d\n", collectItem.getAmount(), 
					collectItem.getCollectedItemEntityID(), collectItem.getEntityID());
			collectItemFlag = true;
			return;
		}
		case "class net.minecraft.network.play.server.SPacketMultiBlockChange":{
			SPacketMultiBlockChange change = (SPacketMultiBlockChange) packet;
			System.out.printf("MultiBlockChange: #updated: %3d\n", change.getChangedBlocks().length);
			return;
		}
		default:
			System.out.printf("%s\n",packet.getClass());
			break;
		}
		}catch (Exception | Error e) {
			recentPackets.clear();
			e.printStackTrace();
		}
	}
	//	private static void transormItemTool(ClassNode node, boolean isObf) {
	//		final String DAMAGE_ITEM = isObf? "a" : "damageItem";
	//		final String DESC = isObf? "(ILvn;)V" : "(ILnet/minecraft/entity/EntityLivingBase;)V"; 
	//
	//		for(MethodNode method : node.methods) {
	//			if(method.name.equals(DAMAGE_ITEM) )
	//				System.out.println("AdvancedMacros transformer: Located matching method name '"+method.desc+"'");
	//			if(method.desc.equals(DESC)) {
	//				System.out.println("AdvancedMacros: located ItemStack's damageItem");
	//				for( AbstractInsnNode instr : method.instructions.toArray()) {
	//					//					Target:
	//					//						Label l7 = new Label(); [AFTER THIS]
	//					//						mv.visitLabel(l7);
	//					//						mv.visitLineNumber(397, l7);
	//					//						mv.visitVarInsn(ALOAD, 0);
	//					//						mv.visitInsn(ICONST_1);
	//					//						mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/item/ItemStack", "shrink", "(I)V", false);
	//					//					Label l8 = new Label();
	//					if(instr.getOpcode()==ALOAD && instr.getNext().getOpcode()==ICONST_1 && instr.getNext().getNext().getOpcode()==INVOKEVIRTUAL) {
	//						VarInsnNode aloadIns = (VarInsnNode) instr;
	//						if(aloadIns.var!=0) 
	//							break;
	//						InsnList actions = new InsnList();
	//						//GETSTATIC AdvancedMacros.forgeEventHandler : ForgeEventHandler
	//						//mv.visitFieldInsn(GETSTATIC, "com/theincgi/advancedMacros/AdvancedMacros", "forgeEventHandler", "Lcom/theincgi/advancedMacros/ForgeEventHandler;");
	//						actions.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(AdvancedMacros.class), "forgeEventHandler", "Lcom/theincgi/advancedMacros/ForgeEventHandler;"));
	//						actions.add(new VarInsnNode(ALOAD, 0));
	//						actions.add(new MethodInsnNode(INVOKEVIRTUAL, 
	//								"com/theincgi/advancedMacros/ForgeEventHandler",
	//								"asmOnItemBreak", 
	//								isObf?"(Lain;)V":"(Lnet/minecraft/item/ItemStack;)V", 
	//										false)
	//								);
	//						//						actions.add(new VarInsnNode(ALOAD, 0));
	//						//						actions.add(new MethodInsnNode(INVOKEDYNAMIC, 
	//						//								"com/theincgi/advancedMacros/ForgeEventHandler",
	//						//								"helloByteCode", 
	//						//								isObf?"()V":"()V", 
	//						//								false)
	//						//						);
	//						method.instructions.insertBefore(instr, actions);
	//						System.out.println("Added call to AdvancedMacros.forgeEventHandler#onBreakItem before ItemStack this.shrink(1)");
	//						break;
	//					}
	//				}
	//			}
	//		}
	//	}

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
	
	
	//int value;
	//	public void whatWouldItLookLike() {
	//		//AdvancedMacros.forgeEventHandler.onItemBreak(null);
	//	}
	public static int getMaxLineCount() {
		try {
			return Settings.settings.get("chatMaxLines").optint(100);
		}catch (Exception | Error e) {
			return 100;
		}
	}


}
