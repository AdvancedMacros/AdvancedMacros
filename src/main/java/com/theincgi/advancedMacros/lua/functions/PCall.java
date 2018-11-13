package com.theincgi.advancedMacros.lua.functions;

import java.io.File;
import java.io.FileReader;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.misc.Utils;
//pRun in _G
public class PCall extends VarArgFunction {
	@Override
	public Varargs invoke(Varargs arg0) {
		try {
			File f = Utils.parseFileLocation(arg0.arg1());
//			File f = new File(AdvancedMacros.macrosFolder, arg0.tojstring());
			FileReader fr = new FileReader(f);
			LuaValue function = AdvancedMacros.globals.load(fr, f.getAbsolutePath());
			Varargs args = function.invoke(arg0.subargs(2));
			return varargsOf(LuaValue.valueOf(true), args);
		} catch (Exception e) {
			LuaTable t = new LuaTable();
			t.set(1, LuaValue.FALSE);
			t.set(2, e.getMessage());
			return t.unpack();
		}
	}
}
