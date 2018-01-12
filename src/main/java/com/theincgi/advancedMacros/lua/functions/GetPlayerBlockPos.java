package com.theincgi.advancedMacros.lua.functions;

import java.util.List;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class GetPlayerBlockPos extends VarArgFunction{
	@Override
	public Varargs invoke(Varargs args) {
		if(args.narg()==0){
			EntityPlayer player = Minecraft.getMinecraft().player;
			LuaTable t = new LuaTable();
			t.set(1, LuaValue.valueOf(Math.floor(player.posX)));
			t.set(2, LuaValue.valueOf(Math.floor(player.posY)));
			t.set(3, LuaValue.valueOf(Math.floor(player.posZ)));
			return t.unpack();
		}else{
			String sPlayer = args.checkjstring(1);
			List<EntityPlayer> players = Minecraft.getMinecraft().world.playerEntities;
			for(int i = 0; i<players.size(); i++) {
				EntityPlayer player = players.get(i);
			//for(EntityPlayer player : Minecraft.getMinecraft().world.playerEntities){
				if(player.getName().equals(sPlayer)){
					LuaTable t = new LuaTable();
					t.set(1, LuaValue.valueOf(Math.floor(player.posX)));
					t.set(2, LuaValue.valueOf(Math.floor(player.posY)));
					t.set(3, LuaValue.valueOf(Math.floor(player.posZ)));
					return t.unpack();
				}
			}
			return LuaValue.FALSE;
		}
	}
}