package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EnumPlayerModelParts;

public class SkinCustomizer extends TwoArgFunction{
	@Override
	public LuaValue call(LuaValue arg0, LuaValue arg1) {
		GameSettings set = AdvancedMacros.getMinecraft().gameSettings;
		switch (arg0.checkjstring()) {
		case "hat":
		case "helmet":
			set.setModelPartEnabled(EnumPlayerModelParts.HAT, arg1.checkboolean());
			break;
		case "jacket":
		case "chest":
			set.setModelPartEnabled(EnumPlayerModelParts.JACKET, arg1.checkboolean());
			break;
		case "left leg":
			set.setModelPartEnabled(EnumPlayerModelParts.LEFT_PANTS_LEG, arg1.checkboolean());
			break;
		case "right leg":
			set.setModelPartEnabled(EnumPlayerModelParts.RIGHT_PANTS_LEG, arg1.checkboolean());
			break;
		case "left arm":
			set.setModelPartEnabled(EnumPlayerModelParts.LEFT_SLEEVE, arg1.checkboolean());
			break;
		case "right arm":
			set.setModelPartEnabled(EnumPlayerModelParts.RIGHT_SLEEVE, arg1.checkboolean());
			break;
		case "cape":
			set.setModelPartEnabled(EnumPlayerModelParts.CAPE, arg1.checkboolean());
			break;

		default:
			throw new LuaError("Unknown part");
		}
		return LuaValue.NONE;
	}
	@Override
	public LuaValue tostring() {
		return LuaValue.valueOf("function: customizeSkin(part, enable)\n"
				+ "parts:{cape, hat/helmet, jacket/chest, \n"
				+ "left/right leg, left/right arm}");
	}
}
