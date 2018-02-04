package com.theincgi.advancedMacros.lua.functions.midi;

import javax.sound.midi.MidiDevice;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

public class StopAll extends ZeroArgFunction {

	@Override
	public LuaValue call() {
		synchronized (MidiLib2.devices) {

			for(MidiDevice device : MidiLib2.devices) {
				try {
					device.close();
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
			MidiLib2.devices.clear();
		}
		return null;
	}

}
