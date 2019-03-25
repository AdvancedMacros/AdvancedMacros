package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.misc.CallableTable;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiEnchantment;
import net.minecraft.client.gui.GuiHopper;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.client.gui.inventory.GuiBeacon;
import net.minecraft.client.gui.inventory.GuiBrewingStand;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiDispenser;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.inventory.GuiScreenHorseInventory;
import net.minecraft.client.gui.inventory.GuiShulkerBox;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;

public class OpenInventory extends ZeroArgFunction{
	private static LuaValue mapping = LuaValue.FALSE;



	@Override
	public LuaValue call() {
		LuaTable controls = new LuaTable();

		for(OpCode op : OpCode.values()) {
			controls.set(op.name(), new CallableTable(op.getDocLocation(), new DoOp(op)));
		}
		controls.set("mapping", getMapping());
		controls.set("LMB", 0);
		controls.set("RMB", 1);
		controls.set("MMB", 2);
		return controls;
	}

	private class DoOp extends VarArgFunction {
		OpCode code;
		public DoOp(OpCode code) {
			super();
			this.code = code;

		}

		@Override
		public Varargs invoke(Varargs args) {
			Minecraft mc = AdvancedMacros.getMinecraft();
			GuiContainer container;
			if(mc.currentScreen instanceof GuiContainer) {
				container = (GuiContainer) mc.currentScreen;
			}else{
				container = new GuiInventory(mc.player);
			}

			//ItemStack held = mc.player.inventory.getItemStack();


			PlayerControllerMP ctrl = mc.playerController;
			int wID = container.inventorySlots.windowId;

			switch(code) {
			case click:{
				Utils.runOnMCAndWait(()->{
					int slotA = args.arg1().checkint();
					int mouseButton = args.optint(2, 0);
					ClickType type = ClickType.PICKUP;
					ctrl.windowClick(wID, slotA-1, mouseButton, type, mc.player);
				});
				Utils.waitTick();
				return NONE;
			}
			case closeAndDrop:
				Utils.runOnMCAndWait(()->{
					ItemStack held = mc.player.inventory.getItemStack();
					if(!held.isEmpty())
						ctrl.windowClick(wID, -999, 0, ClickType.PICKUP, mc.player);
				});
				Utils.waitTick();
				return NONE;
			case close:
				mc.player.closeScreen();
				return NONE;
			case quick:{
				Utils.runOnMCAndWait(()->{
					int slotA = args.arg1().checkint();
					ClickType type = ClickType.QUICK_MOVE;
					ctrl.windowClick(wID, slotA-1, 0, type, mc.player);
				});
				Utils.waitTick();
				return NONE;
			}
			case split:{
				Utils.runOnMCAndWait(()->{
					int slotA = args.arg1().checkint();
					ClickType type = ClickType.PICKUP;
					int slotB = args.checkint(2);
					if(container.inventorySlots.getSlot(slotB).getHasStack())
						throw new LuaError("Destination slot is occupied");
					ctrl.windowClick(wID, slotA-1, 1, type, mc.player);
					ctrl.windowClick(wID, slotB-1, 0, type, mc.player);
				});
				Utils.waitTick();
				return NONE;
			}
			case getHeld:{
				ItemStack held = mc.player.inventory.getItemStack();
				return Utils.itemStackToLuatable(held);
			}
			case getSlot:{
				int slotA = args.arg1().checkint();
				return Utils.itemStackToLuatable( container.inventorySlots.getSlot(slotA-1).getStack() );
			}
			case swap:{
				Utils.runOnMCAndWait((Runnable)()->{
					ItemStack held = mc.player.inventory.getItemStack();
					int slotA = args.checkint(1);
					int slotB = args.checkint(2);
					ItemStack is1 = container.inventorySlots.getSlot(slotA).getStack();
					ItemStack is2 = container.inventorySlots.getSlot(slotB).getStack();
					if(is1.isEmpty() && is2.isEmpty()) return;

					ClickType type = ClickType.PICKUP;
					if(!is1.isEmpty()) 
						ctrl.windowClick(wID, slotA-1, 1, type, mc.player);
					held = mc.player.inventory.getItemStack();
					if((!is2.isEmpty()) || (!held.isEmpty()))
						ctrl.windowClick(wID, slotB-1, 0, type, mc.player);
					held = mc.player.inventory.getItemStack();
					if(held.isEmpty()) return;
					ctrl.windowClick(wID, slotA-1, 1, type, mc.player);
				});
				Utils.waitTick();
				return NONE;
			}
			case grabAll:{
				Utils.runOnMCAndWait(()->{
					int slotA = args.checkint(1);
					ctrl.windowClick(wID, slotA-1, 1, ClickType.PICKUP, mc.player);
					ctrl.windowClick(wID, slotA-1, 1, ClickType.PICKUP_ALL, mc.player);
				});
				Utils.waitTick();
				return NONE;
			}
			case getType:
				if(container instanceof GuiInventory)
					return valueOf("inventory");
				if (container instanceof GuiEnchantment)
					return valueOf("enchantment table");
				if(container instanceof GuiMerchant)
					return valueOf("villager");
				if(container instanceof GuiRepair)
					return valueOf("anvil");
				if(container instanceof GuiBeacon)
					return valueOf("beacon");
				if(container instanceof GuiBrewingStand)
					return valueOf("brewing stand");
				if(container instanceof GuiChest)
					return valueOf("chest");
				if(container instanceof GuiCrafting)
					return valueOf("crafting table");
				if(container instanceof GuiDispenser)
					return valueOf("dispenser");
				if(container instanceof GuiFurnace)
					return valueOf("furnace");
				if(container instanceof GuiHopper)
					return valueOf("hopper");
				if(container instanceof GuiScreenHorseInventory)
					return valueOf("horse inventory");
				if(container instanceof GuiShulkerBox)
					return valueOf("shulker box");
				return valueOf(container.getClass().toString());
			case getTotalSlots: { //as suggested by swadicalrag
				return valueOf(container.inventorySlots.inventorySlots.size());
			}
			default:
				break;
			}
			return NONE;
		}

	}

	private static enum OpCode {
		close,
		closeAndDrop,
		swap,
		split,
		getHeld,
		getSlot,
		quick,
		grabAll,
		getType,
		getTotalSlots,
		click;

		public String[] getDocLocation() {
			return new String[] {"openInventory()", name()};
		}
	}

	private LuaValue getMapping() {
		if(mapping.istable()){
			return mapping;
		}
		mapping = new LuaTable();

		LuaTable inv = new LuaTable();
		mapping.set("inventory", inv);

		inv.set("hotbar", quickTable(37,45));
		inv.set("main", quickTable(10,36));
		inv.set("boots", 9);
		inv.set("leggings", 8);
		inv.set("chestplate", 7);
		inv.set("helmet", 6);
		inv.set("offHand", 46);
		inv.set("craftingIn", quickTable(2,5));
		inv.set("craftingOut", 1);

		LuaTable beacon = new LuaTable();
		mapping.set("beacon", beacon);
		beacon.set("slot", 1);
		beacon.set("main", quickTable(2, 28));
		beacon.set("hotbar", quickTable(29, 37));

		LuaTable brew = new LuaTable();
		mapping.set("brewing stand", brew);

		brew.set("fuel", 5);
		brew.set("input", 4);
		brew.set("output", quickTable(1, 3));
		brew.set("main", quickTable(6, 32));
		brew.set("hotbar", quickTable(33, 41));

		LuaTable chest = new LuaTable();
		mapping.set("chest", chest);
		mapping.set("shulker box", chest);

		chest.set("contents", quickTable(1, 27));
		chest.set("main", quickTable(28, 54));
		chest.set("hotbar", quickTable(55, 63));

		LuaTable craft = new LuaTable();
		mapping.set("crafting table", craft);
		craft.set("craftingIn", quickTable(2, 10));
		craft.set("craftOut", 0);
		craft.set("main", quickTable(11, 37));
		craft.set("hotbar", quickTable(38, 46));

		LuaTable disp = new LuaTable();
		mapping.set("dispenser", disp);
		disp.set("contents", quickTable(1, 9));
		disp.set("main", quickTable(10, 36));
		disp.set("hotbar", quickTable(37, 45));

		LuaTable furn = new LuaTable();
		mapping.set("furnace", furn);
		furn.set("input", 1);
		furn.set("fuel", 2);
		furn.set("output", 3);
		furn.set("main", quickTable(4, 30));
		furn.set("hotbar", quickTable(31, 39));

		LuaTable hopper = new LuaTable();
		mapping.set("hopper", hopper);
		hopper.set("contents", quickTable(1, 5));
		hopper.set("main", quickTable(6, 32));
		hopper.set("hotbar", quickTable(33, 41));

		LuaTable anv = new LuaTable();
		mapping.set("anvil", anv);
		anv.set("item", 1);
		anv.set("material", 2);
		anv.set("input", quickTable(1, 2));
		anv.set("output", 3);
		anv.set("main", quickTable(4, 30));
		anv.set("hotbar", quickTable(31, 39));

		LuaTable enc = new LuaTable();
		mapping.set("enchantment table", enc);
		enc.set("tool", 1);
		enc.set("lapis", 2);
		enc.set("main", quickTable(3, 29));
		enc.set("hotbar", quickTable(30, 38));

		LuaTable vil = new LuaTable();
		mapping.set("villager", vil);
		vil.set("input", quickTable(1, 2));
		vil.set("output", 3);
		vil.set("main", quickTable(4, 30));
		vil.set("hotbar", quickTable(31, 39));

		return mapping;
	}
	private LuaValue quickTable(int i, int j) {
		LuaTable t = new LuaTable();
		for(int n = 1; i<=j; i++, n++)
			t.set(n, LuaValue.valueOf(i));
		return t;
	}
	//	private static class CloseInventory extends ZeroArgFunction{
	//		@Override
	//		public LuaValue call() {
	//			AdvancedMacros.getMinecraft().player.closeScreenAndDropStack();
	//			return LuaValue.NONE;
	//		}
	//	}
	//	private static class SwapStack extends TwoArgFunction{
	//		@Override
	//		public LuaValue call(LuaValue arg1, LuaValue arg2) {
	//			InventoryPlayer inv = AdvancedMacros.getMinecraft().player.inventory;
	//			ContainerPlayer  invContainer =(ContainerPlayer) AdvancedMacros.getMinecraft().player.inventoryContainer;
	//			NetHandlerPlayClient nhpc = AdvancedMacros.getMinecraft().getConnection();
	//
	//			int sourceSlot = arg1.checkint()-1;
	//			int sinkSlot = arg2.checkint()-1;
	//
	//			int sourceIndex = getSlotNum(sourceSlot);
	//			int sinkIndex = getSlotNum(sinkSlot);
	//
	//			System.out.printf("Source: %d -> %d\n",sourceSlot, sourceIndex);
	//			System.out.printf("Sink: %d -> %d\n",sinkSlot, sinkIndex);
	//			if(sourceIndex==-1 || sinkIndex==-1){return LuaValue.NONE;}
	//		
	//			
	//			doClick(sourceIndex, 0); //should be left
	//			doClick(sinkIndex, 0);
	//			doClick(sourceIndex,  0);
	//			//invContainer.inventorySlots.get(sourceIndex).putStack(stackSink);
	//			//invContainer.inventorySlots.get(sinkIndex).putStack(stackSource);
	//			
	//			//BOOKMARK slots outside hotbar cause click on first slot for an unknown reason
	//			//pls use debug mode later
	//			return LuaValue.NONE;
	//		}	
	//	}
	//	private static class SplitStack extends ThreeArgFunction{
	//		@Override
	//		public LuaValue call(LuaValue arg0, LuaValue arg1, LuaValue arg2) {
	//			InventoryPlayer inv = AdvancedMacros.getMinecraft().player.inventory;
	//			ContainerPlayer  invContainer =(ContainerPlayer) AdvancedMacros.getMinecraft().player.inventoryContainer;
	//			NetHandlerPlayClient nhpc = AdvancedMacros.getMinecraft().getConnection();
	//			ItemStack sourceStack = inv.getStackInSlot(arg1.checkint()-1);
	//			ItemStack sinkStack = inv.getStackInSlot(arg1.checkint()-1);
	//			if(!sourceStack.isEmpty() && 
	//					!Utils.itemsEqual(sourceStack, sinkStack)) 
	//				return LuaValue.FALSE;
	//			int amount = arg2.optint((int) (inv.getStackInSlot(arg0.checkint()-1).getCount()/2f+.5));
	//			amount = Math.min(inv.getStackInSlot(arg0.checkint()-1).getCount(), amount);
	//			int sourceSlot = arg0.checkint()-1;
	//			int sinkSlot = arg1.checkint()-1;
	//
	//			int sourceIndex = getSlotNum(sourceSlot);
	//			int sinkIndex = getSlotNum(sinkSlot);
	//
	//			System.out.printf("Source: %d -> %d\n",sourceSlot, sourceIndex);
	//			System.out.printf("Sink: %d -> %d\n",sinkSlot, sinkIndex);
	//			if(sourceIndex==-1 || sinkIndex==-1){return LuaValue.NONE;}
	//			//System.out.println("Debugging!");
	//			
	//			doClick(sourceIndex, 0); //should be left
	//			for(int i = 0; i<amount; i++)
	//				doClick(sinkIndex, 1);
	//			doClick(sourceIndex, 0); //should be left
	//			//doClick(sourceIndex,  0);
	//			//invContainer.inventorySlots.get(sourceIndex).putStack(stackSink);
	//			//invContainer.inventorySlots.get(sinkIndex).putStack(stackSource);
	//			
	//			//BOOKMARK slots outside hotbar cause click on first slot for an unknown reason
	//			//pls use debug mode later
	//			return LuaValue.NONE;
	//		}
	//	}
	//	private static void doClick(int index, int buttonNum){
	//		EntityPlayer player = AdvancedMacros.getMinecraft().player;
	//		ClickType clickType = ClickType.PICKUP;
	//		hndlMsClick(index, buttonNum, clickType);
	//		try {
	//			Thread.sleep(50);
	//		} catch (InterruptedException e) {
	//			e.printStackTrace();
	//		}
	//	}
	//	private static void hndlMsClick(int indx, int dragType, ClickType cType){
	//		EntityPlayer player = AdvancedMacros.getMinecraft().player;
	//		
	//		player.inventoryContainer.slotClick(indx, dragType, cType, player);
	//		
	//		NetHandlerPlayClient nhpc = AdvancedMacros.getMinecraft().getConnection();
	//		nhpc.sendPacket(new CPacketClickWindow(player.inventoryContainer.windowId,
	//				indx, //ID of slot clicked TODO check me, idk if ID is slot ID or index 
	//				dragType,//button num used 
	//				cType, 
	//				player.inventory.getItemStack(), 
	//				player.inventoryContainer.getNextTransactionID(player.inventory)));
	//	}
	//	
	private ClickType getCType() {
		if(AdvancedMacros.getMinecraft().player.inventory.getItemStack().isEmpty()){
			return ClickType.PICKUP;
		}
		return ClickType.SWAP;
	}
	//	private void doClick(int index, ClickType type, int buttonNum){
	//		System.out.println("Clicked on index "+index);
	//		InventoryPlayer inv = AdvancedMacros.getMinecraft().player.inventory;
	//		
	//		ContainerPlayer  invContainer =(ContainerPlayer) AdvancedMacros.getMinecraft().player.inventoryContainer;
	//		NetHandlerPlayClient nhpc = AdvancedMacros.getMinecraft().getConnection();
	//		
	//		invContainer.slotClick(index, 0, type, AdvancedMacros.getMinecraft().player);
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
		InventoryPlayer inv = AdvancedMacros.getMinecraft().player.inventory;
		ContainerPlayer  invContainer =(ContainerPlayer) AdvancedMacros.getMinecraft().player.inventoryContainer;

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