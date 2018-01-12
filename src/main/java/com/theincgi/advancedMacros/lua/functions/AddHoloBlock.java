package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import com.theincgi.advancedMacros.hud.hud3D.HoloBlock;
import com.theincgi.advancedMacros.lua.LuaValTexture;
import com.theincgi.advancedMacros.misc.Settings;
import com.theincgi.advancedMacros.misc.Utils;
public class AddHoloBlock extends VarArgFunction{
	@Override
	public Varargs invoke(Varargs args) {
		LuaValTexture lvt = null;


		LuaValue v = args.arg1();
		if(v instanceof LuaValTexture){
			lvt = (LuaValTexture) v;
		}else if(v.isstring()){
			lvt = Utils.checkTexture(Settings.getTextureID(v.checkjstring()));
		}else{
			lvt = Utils.checkTexture(Settings.getTextureID("resource:holoblock.png"));
		}
		HoloBlock hb = new HoloBlock(lvt);
		//BOOKMARK lua controls and auto remove/persistance and check texture works nice
		return getLuaControls(hb);
	}

	public LuaTable getLuaControls(HoloBlock hb){
		return hb.getControls().checktable();
	}

}