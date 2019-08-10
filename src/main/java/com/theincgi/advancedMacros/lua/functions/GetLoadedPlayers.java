package com.theincgi.advancedMacros.lua.functions;

import java.util.List;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

public class GetLoadedPlayers extends ZeroArgFunction {
	@Override
	public LuaValue call() {
		LuaTable table = new LuaTable();
		int i = 1;
		List<AbstractClientPlayerEntity> players = AdvancedMacros.getMinecraft().world.getPlayers();
		for(int j = 0; j<players.size(); j++) {
			PlayerEntity ep = players.get(j);
			table.set(i++, ep.getName().getUnformattedComponentText());
		}
		return table;
	}
}