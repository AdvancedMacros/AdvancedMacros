package com.theincgi.advancedMacros.lua.functions.midi;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaFunction;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;

import com.theincgi.advancedMacros.lua.functions.midi.device.MidiDeviceControl;

public class GetMidiDevice extends TwoArgFunction {

	@Override
	public LuaValue call(LuaValue arg1, LuaValue arg2) {
		String filter = null;
		if(!arg1.isnil())
			filter = arg1.checkjstring();
		LuaFunction onEvent = null;
		if(!arg2.isnil())
			onEvent = arg2.checkfunction();
		
		MidiDevice device;
		try {
			device = findDevice(filter);
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
			throw new LuaError(e);
		}
		if(device == null)
			return FALSE;
		
		LuaReceiver receiver = new LuaReceiver();
		
		return new MidiDeviceControl(device, luaReceiver);
	}

	private MidiDevice findDevice(String filter) throws MidiUnavailableException {
		Info[] infos = MidiSystem.getMidiDeviceInfo();
		for (Info info : infos) {
			MidiDevice dev = MidiSystem.getMidiDevice(info);
			if(!(dev instanceof Synthesizer) && !(dev instanceof Sequencer)) {
				if(info.getName().toLowerCase().contains(filter.toLowerCase())){
					return dev;
				}
			}
		}
		return null;
	}
	
}
