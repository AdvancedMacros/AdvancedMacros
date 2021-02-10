package com.theincgi.advancedMacros.lua.functions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.lua.LuaDebug;
import com.theincgi.advancedMacros.lua.LuaDebug.LuaThread;
import com.theincgi.advancedMacros.misc.Utils;

public class RunThread extends VarArgFunction{
	@Override
	public Varargs invoke(Varargs args) {
		try {
			LuaValue function = null;
			if(args.arg1().isstring()) {
				//new File(AdvancedMacros.macrosFolder, args.arg1().tojstring());
				File f = Utils.parseFileLocation(args.arg1());
				//FileReader fr = new FileReader(f);
				BufferedReader fr = new BufferedReader(
					new InputStreamReader( new FileInputStream(f), "UTF8")
				);
				function = AdvancedMacros.globals.load(fr, f.getAbsolutePath());
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
		} catch (UnsupportedEncodingException e){
			e.printStackTrace();
			throw new LuaError("Unable to read UTF-8 in: "+args.arg1().tojstring());
		}
	}
}
