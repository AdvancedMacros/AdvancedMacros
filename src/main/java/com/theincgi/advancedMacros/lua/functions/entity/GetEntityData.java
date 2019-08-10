package com.theincgi.advancedMacros.lua.functions.entity;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.entity.Entity;

public class GetEntityData extends OneArgFunction {

	@Override
	public LuaValue call(LuaValue arg0) {
			Entity e = AdvancedMacros.getMinecraft().world.getEntityByID(arg0.checkint());
			if(e==null) {return FALSE;}
			return Utils.entityToTable(e);
	}

}