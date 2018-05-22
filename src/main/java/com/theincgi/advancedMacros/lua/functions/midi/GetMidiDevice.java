package com.theincgi.advancedMacros.lua.functions.midi;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaFunction;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;

import com.theincgi.advancedMacros.lua.functions.midi.device.MidiDeviceControl;

public class GetMidiDevice extends TwoArgFunction {

	@Override
	public LuaValue call(LuaValue arg1, LuaValue arg2) {
		MidiDevice device;
		if(arg1.isuserdata()) {
			try {
				device = MidiSystem.getMidiDevice((Info) arg1.checkuserdata());
			} catch (MidiUnavailableException e) {
				e.printStackTrace();
				throw new LuaError(e);
			}
		}else {
			String filter = null;
			if(!arg1.isnil())
				filter = arg1.checkjstring();
			

			try {
				device = findDevice(filter);
			} catch (MidiUnavailableException e) {
				e.printStackTrace();
				throw new LuaError(e);
			}
		}
		if(device == null)
			return FALSE;
		
		LuaFunction onEvent = null;
		if(!arg2.isnil())
			onEvent = arg2.checkfunction();

		LuaReceiver receiver = new LuaReceiver();
		try {
			device.getTransmitter().setReceiver(receiver);
		} catch (MidiUnavailableException e) {
			receiver.close();
			receiver = null;
		}

		return new MidiDeviceControl(device, receiver);
	}

	private MidiDevice findDevice(String filter) throws MidiUnavailableException {
		Info[] infos = MidiSystem.getMidiDeviceInfo();
		for (Info info : infos) {
			MidiDevice dev = MidiSystem.getMidiDevice(info);

			//if() {
			if(info.getName().toLowerCase().contains(filter.toLowerCase())){
				return dev;
			}
			//			}
		}
		return null;
	}

}
