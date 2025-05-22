package com.theincgi.advancedmacros.guiScripts;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Optional;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;

import com.theincgi.advancedmacros.AdvancedMacros;
import com.theincgi.advancedmacros.misc.Utils;
import com.theincgi.advancedmacros.misc.Workspace;

import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

public class BindingsMenu {
	
	private LuaValue menuClass;
	private LuaValue menu;
	
	public BindingsMenu() {
		load();
	}
	
	public void load() {
		try {
			Optional<Resource> res = AdvancedMacros.getMinecraft().getResourceManager().getResource(new Identifier(AdvancedMacros.MOD_ID, "gui/bindingsmenu.lua"));
			if(res.isEmpty()) {
				throw new FileNotFoundException("Bindings menu script is missing");
			}
			
			InputStream in = res.get().getInputStream();
			try(var reset = Utils.tempSetCurrentWorkspace(Workspace.INTERNAL)) {
				menuClass = AdvancedMacros.globals.load(in, "BindingsMenu", "t", AdvancedMacros.globals).call();
				menu = menuClass.get("new").call(menuClass);
			}
			in.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}		
	}
	
	public void openBindingsMenu() {
		call("open");
	}
	
	public Varargs triggerEvent(String eventType, String value) {
		LuaTable args = new LuaTable();
		args.set(1, eventType);
		args.set(2, value);
		return triggerEvent(args);
	}
	
	public Varargs triggerEvent(LuaTable args) {
		return invoke("trigger", args.unpack());
	}
	
	protected LuaValue call(String func) {
		return menu.get(func).call(menu);
	}
	protected LuaValue call(String func, LuaValue arg) {
		return menu.get(func).call(menu, arg);
	}
	protected Varargs invoke(String func, Varargs args) {
		return menu.get(func).invoke(menu, args);
	}
	
}
