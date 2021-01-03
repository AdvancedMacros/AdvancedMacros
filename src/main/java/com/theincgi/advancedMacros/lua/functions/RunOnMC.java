package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaFunction;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import com.theincgi.advancedMacros.misc.Utils;

public class RunOnMC extends VarArgFunction{
	public RunOnMC() {
	}
	
	@Override
	public Varargs invoke(Varargs args) {
		final LuaValue arg1 = args.arg1();
		final Varargs fArgs = args.subargs(2);
		if(arg1.isstring()) {
			//TODO file support
		}else if(arg1.istable()) {
			//TODO metatable supoprt
		}else if(!arg1.isfunction()) {
			
		}
		
		final LuaFunction theFunction = arg1.checkfunction();
		return Utils.runOnMCAndWait(()->{
			return theFunction.invoke( fArgs );
		});
	}
	
}