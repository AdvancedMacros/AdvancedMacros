package com.theincgi.advancedmacros.guiScripts;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;

import com.google.common.util.concurrent.ListenableFuture;
import com.theincgi.advancedmacros.AdvancedMacros;
import com.theincgi.advancedmacros.event.TaskDispatcher;
import com.theincgi.advancedmacros.lua.LuaDebug;
import com.theincgi.advancedmacros.misc.SimpleListenableFuture;
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
				AdvancedMacros.globals.get("advancedMacros").set("bindingsMenu", menu);
			}
			in.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}		
	}
	
	public void openBindingsMenu() {
		call("open");
	}

	public boolean hasBindingsForEvent(String eventType, String value, boolean includeAnything) {
		var args = new LuaTable();
		args.set("eventType", eventType);
		args.set("value", value);
		args.set("includeAnything", includeAnything);
		return call("listMatchingBindings", args).length() > 0;
	}
	
	public SimpleListenableFuture<Varargs> triggerEvent(String eventType, String value) {
		LuaTable args = new LuaTable();
		args.set("eventType", eventType);
		args.set("value", value);
		return triggerEvent(args);
	}
	
	public SimpleListenableFuture<Varargs> triggerEvent(String eventType, String value, LuaTable eventArgs) {
		LuaTable args = new LuaTable();
		args.set("eventType", eventType);
		args.set("value", value);
		args.set("eventArgs", eventArgs);
		return triggerEvent(args);
	}
	
	public SimpleListenableFuture<Varargs> triggerEvent(LuaTable args) {
		final var future = new SimpleListenableFuture<Varargs>();
		var thread = new LuaDebug.JavaThread(()->{
			future.setResult(invoke("trigger", args));
		});
		thread.workspace = Workspace.INTERNAL;
		thread.start();
		return future;
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
	
	public static BindingsMenu getBindingsMenu() {
		return AdvancedMacros.bindingsMenu;
	}
}
