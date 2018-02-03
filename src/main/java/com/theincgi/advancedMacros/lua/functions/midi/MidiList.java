package com.theincgi.advancedMacros.lua.functions.midi;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiDevice.Info;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

public class MidiList extends ZeroArgFunction {

	@Override
	public LuaValue call() {
		return getInfoList();
	}
	
	public static LuaTable getInfoList() {
		LuaTable tab = new LuaTable();
		
		MidiDevice.Info[] infoArr = MidiSystem.getMidiDeviceInfo();
		for (Info info : infoArr) {
			tab.set(tab.length()+1, toTable(info));
		}
		return tab;
	}
	
	private static LuaValue toTable(Info info) {
		LuaTable t = new LuaTable();
		t.set("name", info.getName());
		t.set("description", info.getDescription());
		t.set("vendor", info.getVendor());
		t.set("version", info.getVersion());
		return t;
	}

}
