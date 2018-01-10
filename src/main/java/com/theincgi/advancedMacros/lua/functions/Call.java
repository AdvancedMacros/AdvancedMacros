package com.theincgi.advancedMacros.lua.functions;

import java.io.File;
import java.io.FileReader;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;

public class Call extends VarArgFunction{
	@Override
	public Varargs invoke(Varargs arg0) {
		try {
			File f = new File(AdvancedMacros.macrosFolder, arg0.tojstring());
			FileReader fr = new FileReader(f);
			LuaValue function = AdvancedMacros.globals.load(fr, arg0.tojstring());
			Varargs args = function.invoke(arg0.subargs(2));
			return args;
		} catch (Exception e) {
			throw new LuaError(e.getMessage());
		}
	}
}
