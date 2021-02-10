package com.theincgi.advancedMacros.lua.functions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.misc.Utils;

public class Call extends VarArgFunction{
	@Override
	public Varargs invoke(Varargs arg0) {
		try {
			try{
				File f = Utils.parseFileLocation(arg0.arg1());
//				File f = new File(AdvancedMacros.macrosFolder, arg0.arg1().tojstring());
//				FileReader fr = new FileReader(f);
				BufferedReader fr = new BufferedReader(
					new InputStreamReader( new FileInputStream(f), "UTF8")
				);
				
				LuaValue function = AdvancedMacros.globals.load(fr, f.getAbsolutePath());
				Varargs args = function.invoke(arg0.subargs(2));
				return args;
			} catch (UnsupportedEncodingException e){
				e.printStackTrace();
				throw new LuaError("Unable to read UTF-8 in: "+arg0.arg1().checkjstring());
			}
		} catch (Exception e) {
			throw new LuaError(e);
		}
	}
}
