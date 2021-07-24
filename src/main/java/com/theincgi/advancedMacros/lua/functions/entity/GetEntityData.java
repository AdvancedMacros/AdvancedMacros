package com.theincgi.advancedMacros.lua.functions.entity;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;
import org.luaj.vm2_v3_0_1.lib.jse.CoerceJavaToLua;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

public class GetEntityData extends TwoArgFunction {

	@Override
	public LuaValue call(LuaValue arg0, LuaValue opt) {
			Entity e = AdvancedMacros.getMinecraft().world.getEntityByID(arg0.checkint());
			if(e==null) {return LuaValue.FALSE;}
			if(opt.isnil() || (opt.isboolean() && opt.checkboolean() == false))
				return Utils.entityToTable(e);
			else
				return CoerceJavaToLua.coerce(e);
	}

}