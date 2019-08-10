package com.theincgi.advancedMacros.lua;

import java.io.InputStream;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.lua.LuaDebug.LuaThread;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.util.ResourceLocation;

public class OpenChangeLog extends ZeroArgFunction{
	@Override
	public LuaValue call() {
		openChangeLog(true);
		return NONE;
	}
	

	public static void openChangeLog(boolean force) {
		try {
			InputStream in = AdvancedMacros.getMinecraft().getResourceManager().getResource(new ResourceLocation(AdvancedMacros.MODID, "scripts/changelogviewer.lua")).getInputStream();
			LuaValue sFunc = AdvancedMacros.globals.load(in, "changeLog", "t", AdvancedMacros.globals);
			in.close();
			if(force) 
				new LuaThread(sFunc, Utils.varargs(valueOf("force")), "Start up Change Log").start();
			else
				new LuaThread(sFunc, "Start up Change Log").start();
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
