package com.theincgi.advancedMacros.lua.functions.midi.device;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.lua.functions.midi.LuaReceiver;
import com.theincgi.advancedMacros.lua.functions.midi.MidiLib2;

public class MidiDeviceControl extends LuaTable {
	
	public MidiDeviceControl(MidiDevice device, LuaReceiver luaReceiver) {
		//Info section
		this.set("getName", new ZeroArgFunction() {
			@Override public LuaValue call() {
				return LuaValue.valueOf(device.getDeviceInfo().getName());
			}
		});
		this.set("getDescription", new ZeroArgFunction() {
			@Override public LuaValue call() {
				return LuaValue.valueOf(device.getDeviceInfo().getDescription());
			}
		});
		this.set("getVersion", new ZeroArgFunction() {
			@Override public LuaValue call() {
				return LuaValue.valueOf(device.getDeviceInfo().getVersion());
			}
		});
		this.set("getVendor", new ZeroArgFunction() {
			@Override public LuaValue call() {
				return LuaValue.valueOf(device.getDeviceInfo().getVendor());
			}
		});
		this.set("isOpen", new ZeroArgFunction() {
			@Override public LuaValue call() {
				return LuaValue.valueOf(device.isOpen());
			}
		});
		
		//Control section
		this.set("open", new ZeroArgFunction() {
			@Override public LuaValue call() {
				try {
					device.open();
					MidiLib2.devices.add(device);
				} catch (MidiUnavailableException e) {
					e.printStackTrace();
					throw new LuaError(e);
				}
				return NONE;
			}
		});
		this.set("close", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				device.close();
				MidiLib2.devices.remove(device);
				return NONE;
			}
		});
		this.set("setOnEvent", new OneArgFunction() {
			
			@Override
			public LuaValue call(LuaValue arg) {
				if(arg.isnil())
					luaReceiver.setOnEvent(null);
				else
					luaReceiver.setOnEvent(arg.checkfunction());
				return NONE;
			}
		});
	}
}
