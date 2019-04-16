package com.theincgi.advancedMacros.lua.functions.midi;

import java.util.ArrayList;

import javax.sound.midi.MidiDevice;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;

public class MidiLib2 extends LuaTable{
	public static final ArrayList<MidiDevice> devices = new ArrayList<>(); 
	public MidiLib2() {
		this.set("list", new MidiList());
		this.set("getDevice", new GetMidiDevice());
		this.set("openMidiFile", new OneArgFunction() {
			@Override public LuaValue call(LuaValue arg) {
				return new MidiFileInput(arg);
			}
		});
		this.set("stopAll", new StopAll());
		this.set("getNoteName", new OneArgFunction() {
			@Override public LuaValue call(LuaValue arg) {
				return LuaValue.valueOf(LuaReceiver.getNoteName(arg.checkint()));
			}
		});
		this.set("getNoteOctave", new OneArgFunction() {
			
			@Override
			public LuaValue call(LuaValue arg) {
				return LuaValue.valueOf(LuaReceiver.getOctaveNumber(arg.checkint()));
			}
		});
	}
}
