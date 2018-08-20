package com.theincgi.advancedMacros.lua.functions;

import java.util.Set;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.SoundCategory;

public class MinecraftSettings extends LuaTable {
	public MinecraftSettings() {
		for (OpCode code : OpCode.values()) {
			set(code.name(), new DoOp(code)); //TODO document me
		}
	}

	private static class DoOp extends VarArgFunction {
		OpCode code;
		public DoOp(OpCode code) {
			this.code = code;
		}
		@Override
		public Varargs invoke(Varargs args) {
			Minecraft mc = Minecraft.getMinecraft();
			switch (code) {
			case getFov:
				return valueOf(mc.gameSettings.fovSetting);
			case getRenderDistance:
				return valueOf(mc.gameSettings.renderDistanceChunks);
			case getSkinCustomization:{
				Set<EnumPlayerModelParts> s = mc.gameSettings.getModelParts();
				if(args.arg1().isnil()) {
					LuaTable out = new LuaTable();
					for (EnumPlayerModelParts part : EnumPlayerModelParts.values()) {
						out.set(part.name().toLowerCase().replace('_', ' '), false);
					}
					for (EnumPlayerModelParts part : s) {
						out.set(part.name().toLowerCase().replace('_', ' '), true);
					}
				}else {
					String key = args.checkjstring(1);
					if(key.equals("helmet"))
						key = "hat";
					else if(key.equals("jacket"))
						key = "chest";
					key = key.toUpperCase();
					key = key.replace(' ', '_');
					return valueOf(s.contains(EnumPlayerModelParts.valueOf(key)));
				}
			}
			case getVolume:
				return valueOf(mc.gameSettings.getSoundLevel(SoundCategory.getByName(args.checkjstring(1).toUpperCase())));
			case isFullscreen:
				return valueOf(mc.isFullScreen());
			case setFov:
				mc.gameSettings.fovSetting = (float) args.checkdouble(1);
				return NONE;
			case setFullscreen:
				if(mc.isFullScreen() != args.optboolean(1, true))
					mc.toggleFullscreen();
				return NONE;
			case setRenderDistance:
				mc.gameSettings.renderDistanceChunks = Math.max(2, Math.min(args.checkint(1), 32));
				return NONE;
			case setVolume:
				mc.gameSettings.setSoundLevel(SoundCategory.getByName(args.checkjstring(1)), (float) args.checkdouble(2));
				return NONE;
			default:
				throw new LuaError("Undefined op "+code.name());
			}
		}
	}

	private enum OpCode {
		getFov,
		setFov,
		getVolume,
		setVolume,
		setRenderDistance,
		getRenderDistance,
		setFullscreen,
		isFullscreen,
		getSkinCustomization,
	}
}
