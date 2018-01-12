package com.theincgi.advancedMacros.lua;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;

import com.theincgi.advancedMacros.AdvancedMacros;

public class ProtectedLuaTable extends LuaTable {
	public ProtectedLuaTable() {
		LuaTable meta = new LuaTable();
		meta.set("__newindex", AdvancedMacros.globals.load("error(\"Table is read only\")"));
		meta.set("__metatable", new LuaTable());
		super.setmetatable(meta);
	}
	
	public void secSet(LuaValue u, LuaValue v){
		super.rawset(u, v);
	}
	
	@Override
	public void rawset(LuaValue key, LuaValue value) {
		err();
	}
	@Override
	public void rawset(int key, LuaValue value) {
		err();
	}
	@Override
	public void rawset(int key, String value) {
		err();
	}
	@Override
	public void rawset(String key, double value) {
		err();
	}
	@Override
	public void rawset(String key, int value) {
		err();
	}
	@Override
	public void rawset(String key, LuaValue value) {
		err();
	}
	@Override
	public void rawset(String key, String value) {
		err();
	}
	@Override
	public void rawsetlist(int key0, Varargs values) {
		err();
	}
	@Override
	public void set(LuaValue key, LuaValue value) {
		err();
	}
	@Override
	public void set(int key, LuaValue value) {
		err();
	}
	@Override
	public void set(int key, String value) {
		err();
	}
	@Override
	public void set(String key, double value) {
		err();
	}
	@Override
	public void set(String key, int value) {
		err();
	}
	@Override
	public void set(String key, LuaValue value) {
		err();
	}
	@Override
	public void set(String key, String value) {
		err();
	}
	@Override
	public LuaValue setmetatable(LuaValue metatable) {
		throw new LuaError("Table is protected");
	}
	
	private void err(){throw new LuaError("Table is protected");}
}