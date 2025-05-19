package com.theincgi.advancedmacros.guiScripts;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Optional;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;

import com.theincgi.advancedmacros.AdvancedMacros;

import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

public class BindingsMenu {
	
	LuaValue menu;
	
	public BindingsMenu() {
		try {
			Optional<Resource> res = AdvancedMacros.getMinecraft().getResourceManager().getResource(new Identifier(AdvancedMacros.MOD_ID, "scripts/gui/BindingsMenu.lua"));
			if(res.isEmpty()) {
				throw new FileNotFoundException("Bindings menu script is missing");
			}
			
			InputStream in = res.get().getInputStream();
			menu = AdvancedMacros.globals.load(in, "BindingsMenu", "t", AdvancedMacros.globals).call();
			in.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}		
	}
	
	public void triggerEvent(String eventType, String value, Varargs args) {
		
	}
	
}
