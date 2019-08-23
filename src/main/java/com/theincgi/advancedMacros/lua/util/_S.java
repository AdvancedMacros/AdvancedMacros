package com.theincgi.advancedMacros.lua.util;

import java.util.HashMap;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaThread;
import org.luaj.vm2_v3_0_1.LuaValue;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.lua.LuaDebug;

/**
 * Script local environment<br>
 * All instances of a script will access the same table from this.<br>
 * Automaticly syncronized
 * */
public class _S extends LuaTable{
	private static HashMap<String, LuaTable> map = new HashMap<>();
	
	public _S() {
		
	}
	
	@Override
	public void hashset(LuaValue key, LuaValue value) {
		LuaThread lt = AdvancedMacros.globals.getCurrentLuaThread();
		String src = LuaDebug.getSourceName(lt);
		
		synchronized ( src ) {
			map.computeIfAbsent( src , (k)->{return new LuaTable();}).hashset(key, value);
		}
	}
	@Override
	public LuaValue get(int key) {
		if(AdvancedMacros.globals==null) return NIL;
		if(AdvancedMacros.globals.getCurrentLuaThread()==null) return NIL;
		LuaThread lt = AdvancedMacros.globals.getCurrentLuaThread();
		String src = LuaDebug.getSourceName(lt);
		
		synchronized ( src ) {
			return map.computeIfAbsent( src , (k)->{return new LuaTable();}).get(key);
		}
	}
	@Override
	public LuaValue get(LuaValue key) {
		if(AdvancedMacros.globals==null) return NIL;
		if(AdvancedMacros.globals.getCurrentLuaThread()==null) return NIL;
		LuaThread lt = AdvancedMacros.globals.getCurrentLuaThread();
		String src = LuaDebug.getSourceName(lt);
		
		synchronized ( src ) {
			return map.computeIfAbsent( src , (k)->{return new LuaTable();}).get(key);
		}
	}
	@Override
	public LuaValue get(String key) {
		if(AdvancedMacros.globals==null) return NIL;
		if(AdvancedMacros.globals.getCurrentLuaThread()==null) return NIL;
		LuaThread lt = AdvancedMacros.globals.getCurrentLuaThread();
		String src = LuaDebug.getSourceName(lt);
		
		synchronized ( src ) {
			return map.computeIfAbsent( src , (k)->{return new LuaTable();}).get(key);
		}
	}
	@Override
	public LuaValue setmetatable(LuaValue metatable) {
		LuaThread lt = AdvancedMacros.globals.getCurrentLuaThread();
		String src = LuaDebug.getSourceName(lt);
		
		synchronized ( src ) {
			return map.computeIfAbsent( src , (k)->{return new LuaTable();}).setmetatable(metatable);
		}
	}
	@Override
	public LuaValue getmetatable() {
		LuaThread lt = AdvancedMacros.globals.getCurrentLuaThread();
		String src = LuaDebug.getSourceName(lt);
		
		synchronized ( src ) {
			return map.computeIfAbsent( src , (k)->{return new LuaTable();}).getmetatable();
		}
	}
}
