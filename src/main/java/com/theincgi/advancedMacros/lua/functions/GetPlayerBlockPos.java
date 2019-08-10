package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

public class GetPlayerBlockPos extends VarArgFunction{
	@Override
	public Varargs invoke(Varargs args) {
		if(args.narg()==0){
			PlayerEntity player = AdvancedMacros.getMinecraft().player;
			LuaTable t = new LuaTable();
			t.set(1, LuaValue.valueOf(Math.floor(player.posX)));
			t.set(2, LuaValue.valueOf(Math.floor(player.posY)));
			t.set(3, LuaValue.valueOf(Math.floor(player.posZ)));
			return t.unpack();
		}else{
			String sPlayer = args.checkjstring(1);
			AbstractClientPlayerEntity player = Utils.findPlayerByName(AdvancedMacros.getMinecraft().player.getEntityWorld(), sPlayer);

			//for(EntityPlayer player : AdvancedMacros.getMinecraft().world.playerEntities){
			if(player.getName().equals(sPlayer)){
				LuaTable t = new LuaTable();
				t.set(1, LuaValue.valueOf(Math.floor(player.posX)));
				t.set(2, LuaValue.valueOf(Math.floor(player.posY)));
				t.set(3, LuaValue.valueOf(Math.floor(player.posZ)));
				return t.unpack();
			}

			return LuaValue.FALSE;
		}
	}
}