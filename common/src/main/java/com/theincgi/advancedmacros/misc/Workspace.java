package com.theincgi.advancedmacros.misc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;

import com.google.gson.JsonParser;
import com.theincgi.advancedmacros.AdvancedMacros;

public class Workspace {
	public final String name;
	public final String path;
	
	public Permissions permissions;
	
	public static final Workspace INTERNAL = new Workspace("internal", "resource:");
	
	public Workspace(String name, String path) {
		super();
		this.name = name;
		this.path = path;
	}
	
	public static Workspace load(String name) throws IOException {
		return load(new File(AdvancedMacros.WORKSPACES_FOLDER, name+".json"));
	}
	
	public static Workspace load(File file) throws IOException {
		String contents = new String(Files.readAllBytes(file.toPath()));
		var json = JsonParser.parseString(contents).getAsJsonObject();
		var name = json.get("workspaceName").getAsString();
		var path = json.get("workspacePath").getAsString();
		var workspace = new Workspace(name, path);
		
		//TODO deserialize permissions
		return workspace;
	}
	
	public static ArrayList<String> listWorkspaceNames() {
		var out = new ArrayList<String>();
		for(var file : AdvancedMacros.WORKSPACES_FOLDER.list()) {
			if(file.endsWith(".json"))
				out.add(file.substring(0, file.length()-5));
		}
		return out;
	}

	public LuaTable asTable() {
		LuaTable tbl = new LuaTable();
		LuaTable permTable = new LuaTable();
		
		//TODO serialize permissions to LuaTable
		
		tbl.set("name", name);
		tbl.set("path", path);
		tbl.set("permissions", permTable);
		return tbl;
	}
	
	public LuaValue asLuaClass() {
		return LuaClassUtils.newInstance("Workspace", asTable());
	}
}
