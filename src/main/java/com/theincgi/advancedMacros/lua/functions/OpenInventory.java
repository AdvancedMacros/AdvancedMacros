package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ThreeArgFunction;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketClientStatus;

public class OpenInventory extends ZeroArgFunction{
	private static LuaValue mapping = LuaValue.FALSE;
	@Override
	public LuaValue call() {
		//InventoryPlayer inv = Minecraft.getMinecraft().player.inventory;
		//ContainerPlayer  invContainer =(ContainerPlayer) Minecraft.getMinecraft().player.inventoryContainer;
		
		
		throw new LuaError("Unimplemented, Sorry!");
		
//		NetHandlerPlayClient nhpc = Minecraft.getMinecraft().getConnection();
//		
//		LuaTable controls = new LuaTable();
//		
//		
//		nhpc.sendPacket(new CPacketClientStatus(CPacketClientStatus.State.OPEN_INVENTORY_ACHIEVEMENT));
//		
//		controls.set("swapStack", new SwapStack());
//		controls.set("splitStack", new SplitStack());
//		controls.set("close", new CloseInventory());
//		controls.set("mapping", getMapping());
//		
//		return controls;
	}
	private LuaValue getMapping() {
		if(mapping.istable()){
			return mapping;
		}
		mapping = new LuaTable();
		mapping.set("hotbar", quickTable(1,9));
		mapping.set("main", quickTable(10,36));
		mapping.set("boots", 37);
		mapping.set("leggings", 38);
		mapping.set("chestplate", 39);
		mapping.set("helmet", 40);
		mapping.set("offHand", 41);
		mapping.set("craftingIn", quickTable(42,45));
		mapping.set("craftingOut", 46);
		return mapping;
	}
	private LuaValue quickTable(int i, int j) {
		LuaTable t = new LuaTable();
		t.set(1, LuaValue.valueOf(i));
		t.set(2, LuaValue.valueOf(j));
		return t;
	}
	private static class CloseInventory extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			Minecraft.getMinecraft().player.closeScreenAndDropStack();
			return LuaValue.NONE;
		}
	}
	private static class SwapStack extends TwoArgFunction{
		@Override
		public LuaValue call(LuaValue arg1, LuaValue arg2) {
			InventoryPlayer inv = Minecraft.getMinecraft().player.inventory;
			ContainerPlayer  invContainer =(ContainerPlayer) Minecraft.getMinecraft().player.inventoryContainer;
			NetHandlerPlayClient nhpc = Minecraft.getMinecraft().getConnection();

			int sourceSlot = arg1.checkint()-1;
			int sinkSlot = arg2.checkint()-1;

			int sourceIndex = getSlotNum(sourceSlot);
			int sinkIndex = getSlotNum(sinkSlot);

			System.out.printf("Source: %d -> %d\n",sourceSlot, sourceIndex);
			System.out.printf("Sink: %d -> %d\n",sinkSlot, sinkIndex);
			if(sourceIndex==-1 || sinkIndex==-1){return LuaValue.NONE;}
		
			
			doClick(sourceIndex, 0); //should be left
			doClick(sinkIndex, 0);
			doClick(sourceIndex,  0);
			//invContainer.inventorySlots.get(sourceIndex).putStack(stackSink);
			//invContainer.inventorySlots.get(sinkIndex).putStack(stackSource);
			
			//BOOKMARK slots outside hotbar cause click on first slot for an unknown reason
			//pls use debug mode later
			return LuaValue.NONE;
		}	
	}
	private static class SplitStack extends ThreeArgFunction{
		@Override
		public LuaValue call(LuaValue arg0, LuaValue arg1, LuaValue arg2) {
			InventoryPlayer inv = Minecraft.getMinecraft().player.inventory;
			ContainerPlayer  invContainer =(ContainerPlayer) Minecraft.getMinecraft().player.inventoryContainer;
			NetHandlerPlayClient nhpc = Minecraft.getMinecraft().getConnection();
			ItemStack sourceStack = inv.getStackInSlot(arg1.checkint()-1);
			ItemStack sinkStack = inv.getStackInSlot(arg1.checkint()-1);
			if(!sourceStack.isEmpty() && 
					!Utils.itemsEqual(sourceStack, sinkStack)) 
				return LuaValue.FALSE;
			int amount = arg2.optint((int) (inv.getStackInSlot(arg0.checkint()-1).getCount()/2f+.5));
			amount = Math.min(inv.getStackInSlot(arg0.checkint()-1).getCount(), amount);
			int sourceSlot = arg0.checkint()-1;
			int sinkSlot = arg1.checkint()-1;

			int sourceIndex = getSlotNum(sourceSlot);
			int sinkIndex = getSlotNum(sinkSlot);

			System.out.printf("Source: %d -> %d\n",sourceSlot, sourceIndex);
			System.out.printf("Sink: %d -> %d\n",sinkSlot, sinkIndex);
			if(sourceIndex==-1 || sinkIndex==-1){return LuaValue.NONE;}
			//System.out.println("Debugging!");
			
			doClick(sourceIndex, 0); //should be left
			for(int i = 0; i<amount; i++)
				doClick(sinkIndex, 1);
			doClick(sourceIndex, 0); //should be left
			//doClick(sourceIndex,  0);
			//invContainer.inventorySlots.get(sourceIndex).putStack(stackSink);
			//invContainer.inventorySlots.get(sinkIndex).putStack(stackSource);
			
			//BOOKMARK slots outside hotbar cause click on first slot for an unknown reason
			//pls use debug mode later
			return LuaValue.NONE;
		}
	}
	private static void doClick(int index, int buttonNum){
		EntityPlayer player = Minecraft.getMinecraft().player;
		ClickType clickType = ClickType.PICKUP;
		hndlMsClick(index, buttonNum, clickType);
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	private static void hndlMsClick(int indx, int dragType, ClickType cType){
		EntityPlayer player = Minecraft.getMinecraft().player;
		
		player.inventoryContainer.slotClick(indx, dragType, cType, player);
		
		NetHandlerPlayClient nhpc = Minecraft.getMinecraft().getConnection();
		nhpc.sendPacket(new CPacketClickWindow(player.inventoryContainer.windowId,
				indx, //ID of slot clicked TODO check me, idk if ID is slot ID or index 
				dragType,//button num used 
				cType, 
				player.inventory.getItemStack(), 
				player.inventoryContainer.getNextTransactionID(player.inventory)));
	}
	
	private ClickType getCType() {
		if(Minecraft.getMinecraft().player.inventory.getItemStack().isEmpty()){
			return ClickType.PICKUP;
		}
		return ClickType.SWAP;
	}
//	private void doClick(int index, ClickType type, int buttonNum){
//		System.out.println("Clicked on index "+index);
//		InventoryPlayer inv = Minecraft.getMinecraft().player.inventory;
//		
//		ContainerPlayer  invContainer =(ContainerPlayer) Minecraft.getMinecraft().player.inventoryContainer;
//		NetHandlerPlayClient nhpc = Minecraft.getMinecraft().getConnection();
//		
//		invContainer.slotClick(index, 0, type, Minecraft.getMinecraft().player);
//		nhpc.sendPacket(new CPacketClickWindow(invContainer.windowId,
//				index, //ID of slot clicked TODO check me, idk if ID is slot ID or index 
//				buttonNum,//button num used 
//				type, 
//				inv.getStackInSlot(index), 
//				invContainer.getNextTransactionID(inv)));
//		
//		//BOOKMARK investigating slotClick() method
//	}

	private static int getSlotNum(int invIndx){
		InventoryPlayer inv = Minecraft.getMinecraft().player.inventory;
		ContainerPlayer  invContainer =(ContainerPlayer) Minecraft.getMinecraft().player.inventoryContainer;

		if(inRange(invIndx, 0, 8)){
			return invIndx+36;
		}else if(inRange(invIndx, 9, 35)){
			return invIndx;
		}else if(inRange(invIndx, 36, 39)){
			return invIndx-31;
		}else if(invIndx==40){
			return 45;
		}else if(inRange(invIndx, 41, 44)){
			return invIndx-40;
		}else if(invIndx==45){
			return 0;
		}
		for(int i = 0; i<invContainer.inventorySlots.size(); i++){
			if(invIndx==invContainer.inventorySlots.get(i).getSlotIndex()){
				System.out.println("INDEX "+invIndx+" = "+i);
				return i;
			}
		}
		return -1;
	}
	public static boolean inRange(int x, int a, int b){
		return a<=x && x<=b;
	}
}