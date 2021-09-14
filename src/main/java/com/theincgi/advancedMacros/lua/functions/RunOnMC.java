package com.theincgi.advancedMacros.lua.functions;

import java.util.concurrent.ExecutionException;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaFunction;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import com.google.common.util.concurrent.ListenableFuture;
import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.event.TaskDispatcher;
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
		}else if(!arg1.isfunction()) {
			
		}
		final LuaFunction theFunction = arg1.checkfunction();
		ListenableFuture<Varargs> f = TaskDispatcher.addTask(()->{
			try {
				return theFunction.invoke(fArgs);
			}catch (Throwable e) {
				throw new LuaError(e);
			}
		});
		try {
			return f.get();
		} catch (InterruptedException e) {
			return NONE;
		} catch (ExecutionException e) {
			throw new LuaError(e);
		}
	}
}