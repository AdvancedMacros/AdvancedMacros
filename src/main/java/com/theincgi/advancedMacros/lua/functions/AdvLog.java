package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.Minecraft;

public class AdvLog extends OneArgFunction{
	@Override
	public LuaValue call(LuaValue arg0) {
		if(arg0.istable()) {
			AdvancedMacros.getMinecraft().ingameGUI.getChatGUI().printChatMessage(Utils.luaTableToComponentJson(arg0.checktable()));
		}
		return LuaValue.NONE;
	}
}
