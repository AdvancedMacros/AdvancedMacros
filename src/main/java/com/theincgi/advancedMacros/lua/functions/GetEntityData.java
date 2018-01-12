package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;

import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

public class GetEntityData extends OneArgFunction {

	@Override
	public LuaValue call(LuaValue arg0) {
			Entity e = Minecraft.getMinecraft().world.getEntityByID(arg0.checkint());
			if(e==null) {return FALSE;}
			return Utils.entityToTable(e);
	}

}