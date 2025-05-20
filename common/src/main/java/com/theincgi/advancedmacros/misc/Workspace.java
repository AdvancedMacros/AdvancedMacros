package com.theincgi.advancedmacros.misc;

import org.luaj.vm2_v3_0_1.LuaTable;

public record Workspace(String name, String path) {
	
	public static final Workspace INTERNAL = new Workspace("internal", "resource:");
	
	public LuaTable asTable() {
		LuaTable tbl = new LuaTable();
		tbl.set("name", name);
		tbl.set("path", path);
		return tbl;
	}
	
}
