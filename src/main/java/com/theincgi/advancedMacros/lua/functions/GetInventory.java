package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

public class GetInventory extends OneArgFunction{
	@Override
	public LuaValue call(LuaValue arg) {
		IInventory inventory;
		LuaTable output = new LuaTable();
		if(arg.isnil()){
			inventory = AdvancedMacros.getMinecraft().player.inventory;
		}else{
			EntityPlayer player = AdvancedMacros.getMinecraft().world.getPlayerEntityByName(arg.checkjstring());
			if(player==null){return LuaValue.FALSE;}
			inventory = player.inventory;
		}
		for(int i = 0; i<inventory.getSizeInventory(); i++){
			output.set(i+1, Utils.itemStackToLuatable(inventory.getStackInSlot(i)));
		}
		return output;
	}
	
}