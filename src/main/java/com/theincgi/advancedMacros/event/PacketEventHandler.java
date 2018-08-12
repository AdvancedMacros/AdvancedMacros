package com.theincgi.advancedMacros.event;

import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSetSlot;

import static com.theincgi.advancedMacros.event.ForgeEventHandler.EventName.*;
import static org.luaj.vm2_v3_0_1.LuaValue.valueOf;


import org.luaj.vm2_v3_0_1.LuaTable;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.misc.Utils;

import static com.theincgi.advancedMacros.event.ForgeEventHandler.createEvent;
/**
 * This class will be used to handle packet nonsense<br>
 * and convert it into useful lua data
 * *///Would have prefered forge events, but they were server side only :\ *shrugs*
public class PacketEventHandler {
	
	public static void onItemPickup(ItemStack i, int slotNum) {
		LuaTable e = createEvent(ItemPickup);
		e.set(3, Utils.itemStackToLuatable(i));
		e.set(4, valueOf(slotNum));
		AdvancedMacros.forgeEventHandler.fireEvent(ItemPickup, e);
	}

	public static void onItemBreak(int slot, ItemStack orig) {
		LuaTable e = createEvent(BreakItem);
		e.set(3, Utils.itemStackToLuatable(orig));
		e.set(4, valueOf(slot));
		AdvancedMacros.forgeEventHandler.fireEvent(BreakItem, e);
	}
	
	
}
