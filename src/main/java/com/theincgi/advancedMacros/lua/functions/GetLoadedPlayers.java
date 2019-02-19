package com.theincgi.advancedMacros.lua.functions;

import java.util.List;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class GetLoadedPlayers extends ZeroArgFunction {
	@Override
	public LuaValue call() {
		LuaTable table = new LuaTable();
		int i = 1;
		List<EntityPlayer> players = AdvancedMacros.getMinecraft().world.playerEntities;
		for(int j = 0; j<players.size(); j++) {
			EntityPlayer ep = players.get(j);
			table.set(i++, ep.getName());
		}
		return table;
	}
}