package com.theincgi.advancedMacros.lua.functions.midi;

import java.util.ArrayList;

import javax.sound.midi.MidiDevice;

import org.luaj.vm2_v3_0_1.LuaTable;

public class MidiLib2 extends LuaTable{
	public static final ArrayList<MidiDevice> devices = new ArrayList<>(); 
	public MidiLib2() {
		this.set("list", new MidiList());
		this.set("getDevice", new GetMidiDevice());
		this.set("stopAll", new StopAll());
	}
}
