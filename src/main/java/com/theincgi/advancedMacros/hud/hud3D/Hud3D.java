package com.theincgi.advancedMacros.hud.hud3D;

import org.luaj.vm2_v3_0_1.LuaTable;

import com.theincgi.advancedMacros.lua.functions.AddHoloBlock;
import com.theincgi.advancedMacros.lua.functions.AddHoloText;
import com.theincgi.advancedMacros.lua.functions.ClearWorldHud;

public class Hud3D extends LuaTable{
	public Hud3D() {
		this.set("newBlock", new AddHoloBlock());
		this.set("newText", new AddHoloText());
		this.set("clearAll", new ClearWorldHud());
	}
}
