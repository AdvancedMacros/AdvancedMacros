package com.theincgi.advancedmacros.misc;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class Permissions {
	
	public final HashSet<String> permissions = new HashSet<>();
	
	public static enum Permission {
		ALL("all"),
		FILEIO_READ("fileIO.read", ListMode.PATTERN),
		FILEIO_WRITE("fileIO.write", ListMode.PATTERN),
		RUN_WORKSPACE("run.workspace", ListMode.EXACT),
		LUAJAVA("luajava", ListMode.PATTERN),
		EXEC("exec", ListMode.PATTERN), //run a program (non lua)
		DEBUG("debug"),
		CONTROL_PLAYER("controlPlayer"),
		MANAGE_INVENTORY("manageInventory"),
		MANAGE_BINDINGS("manageBindings"),
		SPEAK("speak"),
		RUN_COMMAND("runCommand", ListMode.PATTERN),
		NARRATE("narrate"),
		TOAST_NOTIFICATION("toast.notification"),
		TOAST_TITLE("toast.title"),
		TOAST_ACTION_BAR("toast.actionBar"),
		INTERNET("internet"),
		SETTINGS("settings"),
		LOAD_BINARY("load.binary"),
		CLIPBOARD_READ("clipboard.read"),
		CLIPBOARD_WRITE("clipboard.write"),
		MODIFY_PERMISSIONS("modifyPermissions"),
		MODIFY_WORKSPACE("modifyWorkspace");
		
		public final String key;
		public final ListMode listMode;
		
		private Permission(String key) {this(key, ListMode.NOT_LIST);}
		private Permission(String key, ListMode listMode) {
			this.key = key;
			this.listMode = listMode;	
		}
		
		public static enum ListMode {
			NOT_LIST,
			PATTERN,
			EXACT;
			public boolean isList() {
				return !this.equals(NOT_LIST);
			}
		}
	}
	
	public Permissions(boolean all) {
		grant(Permission.ALL);
	}
	
	public void grant(String permission) {
		permissions.add(permission);
	}
	
	public void grant(Permission permission) {
		if(permission.listMode.isList())
			throw new IllegalArgumentException("permission %s is a list type and requires a value".formatted(permission.name()));
		permissions.add(permission.key);
	}
	
	public void grant(Permission permission, String value) {
		if(!permission.listMode.isList())
			throw new IllegalArgumentException("permission %s is not a list type");
		
		permissions.add(permission.key+":"+value);
	}
	
	public void revoke(String permission) {
		permissions.remove(permission);
	}
	
	public void revoke(Permission permission) {
		if(permission.listMode.isList())
			throw new IllegalArgumentException("permission %s is a list type and requires a value".formatted(permission.name()));
		permissions.remove(permission.key);
	}
	
	public void revoke(Permission permission, String value) {
		if(!permission.listMode.isList())
			throw new IllegalArgumentException("permission %s is not a list type");
		
		permissions.remove(permission.key+":"+value);
	}
	
	public boolean hasPermission(String permission) {
		if(permissions.contains(Permission.ALL.key)) return true;
		if(permissions.contains(permission)) return true;
		return false;
	}
	
	public boolean hasPermission(String permission, String value) {
		if(permissions.contains(Permission.ALL.key)) return true;
		try {
			var perm = Permission.valueOf(permission);
			return hasPermission(perm, value);
		} catch(IllegalArgumentException iae) {
			return hasPermission(permission + ":" + value);
		}
	}
	
	public boolean hasPermission(Permission permission) {
		if(permissions.contains(Permission.ALL.key)) return true;
		if(permissions.contains(permission.key)) return true;
		return false;
	}
	
	public boolean hasPermission(Permission permission, String value) {
		if(permissions.contains(Permission.ALL.key)) return true;
		
		var currentWorkpace = Utils.currentWorkspace();
		
		return permissions.stream()
			.filter(p->p.startsWith(permission.key+":"))
			.map(p->p.substring(permission.key.length()+1))
			.map(pattern->{
				if(permission.listMode.equals(Permission.ListMode.PATTERN))
					return pattern.replace("{WORKSPACE}", currentWorkpace.getPath());
				return pattern;
			})
			.filter(pattern->switch(permission.listMode) {
				case PATTERN -> value.matches(pattern);
				case EXACT -> value.equals(pattern);
				case NOT_LIST -> throw new IllegalStateException("value provided to non list type permission");
				default -> throw new IllegalStateException("invalid list mode (internal error)"); 
			})
			.findAny().isPresent();
	}
	
	public static void check(Permission permission) {
		var callingWorkspace = Utils.currentWorkspace();
		if(!callingWorkspace.permissions.hasPermission(permission))
			throw new LuaError("Missing permission: "+permission.key);
	}
	
	public static void check(Permission permission, String value) {
		var callingWorkspace = Utils.currentWorkspace();
		if(!callingWorkspace.permissions.hasPermission(permission, value))
			throw new LuaError("Missing permission: "+permission.key);
	}
	public static void check(String permission) {
		var callingWorkspace = Utils.currentWorkspace();
		if(!callingWorkspace.permissions.hasPermission(permission))
			throw new LuaError("Missing permission: "+permission);
	}
	
	public static void checkRunFile(File file) {
		try {
			var workspace = Utils.currentWorkspace();
			if(!workspace.containsFile(file))
				if(Workspace.workspacesWithFile(file)
				.filter(w->w.permissions.hasPermission(Permission.RUN_WORKSPACE, w.getName()))
				.findAny().isEmpty())
					throw new LuaError("Missing permission: %s:?".formatted(Permission.RUN_WORKSPACE.key));
		} catch (IOException e) {
			throw new LuaError(e);
		}
	}
	
	public JsonElement toJson() {
		var json = new JsonArray();
		for(var perm : permissions)
			json.add(perm);
		
		return json;
	}
	
	public LuaTable toLuaTable() {
		LuaTable tbl = new LuaTable();
		
		var i = 1;
		for(var perm : permissions)
			tbl.set(i++, perm);
		
		return tbl;
	}

	public void load(JsonArray json) {
		permissions.clear();
		for(var entry : json) {
			grant(entry.getAsString());
		}
	}
	
}
