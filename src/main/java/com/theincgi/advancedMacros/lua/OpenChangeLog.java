package com.theincgi.advancedMacros.lua;

import java.io.IOException;
import java.io.InputStream;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class OpenChangeLog extends ZeroArgFunction{
	@Override
	public LuaValue call() {
		openChangeLog();
		return NONE;
	}
	
	public static void openChangeLog() {
		try {
			InputStream in = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(AdvancedMacros.MODID, "scripts/changelogviewer.lua")).getInputStream();
			AdvancedMacros.globals.load(in, "changeLog", "t", AdvancedMacros.globals).call(valueOf("force"));
			in.close();
		} catch (IOException e) {e.printStackTrace();}
	}
}
