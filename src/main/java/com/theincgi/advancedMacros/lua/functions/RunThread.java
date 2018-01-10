package com.theincgi.advancedMacros.lua.functions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.lua.LuaDebug;
import com.theincgi.advancedMacros.lua.LuaDebug.LuaThread;

public class RunThread extends VarArgFunction{
	//	private LuaDebug luaDebug;
	//	
	//	public RunThread(LuaDebug luaDebug) {
	//		super();
	//		this.luaDebug = luaDebug;
	//	}

	@Override
	public Varargs invoke(Varargs args) {
		try {
			LuaValue function = null;
			if(args.arg1().isstring()) {
				File f = new File(AdvancedMacros.macrosFolder, args.arg1().tojstring());

				FileReader fr = new FileReader(f);
				function = AdvancedMacros.globals.load(fr, args.arg1().tojstring());
			}else if(args.arg1().isfunction()) {
				function = args.arg1();
			}else {
				throw new LuaError("Arg 1 must be string or function");
			}
			LuaDebug.LuaThread thread = new LuaThread(function, args.subargs(2), args.arg1().tojstring());
			return thread.start();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new LuaError("No such file: "+args.arg1().tojstring());
		}
	}
}
