package com.theincgi.advancedMacros.lua.functions.midi.device;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.ShortMessage;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;

import com.theincgi.advancedMacros.misc.Utils;

public class MidiSend extends OneArgFunction {
	MidiDevice device;
	public MidiSend(MidiDevice device) {
		this.device = device;
	}

	@Override
	public LuaValue call(LuaValue arg) {
		LuaTable tab = arg.checktable();
		ShortMessage message = new ShortMessage();
		int statusCode = getMstStatusCode(tab.get("status").checkjstring());
		
		int data1, data2 = 0, channel=1;
		channel = tab.get("channel").checkint();
		
		switch (statusCode) {
		case ShortMessage.NOTE_ON:
			data1 = tab.get("key").checkint();
			data2 = tab.get("velocity").optint(127);
			break;
		case ShortMessage.NOTE_OFF:
			data1 = tab.get("key").checkint();
			data2 = tab.get("velocity").optint(0);
			break;
		case ShortMessage.CHANNEL_PRESSURE:
			data1 = tab.get("pressure").optint(0);
			break;
		case ShortMessage.PITCH_BEND:
			data1 = tab.get("fine").checkint();
			data2 = tab.get("coarse").optint(0);
			break;
		case ShortMessage.CONTROL_CHANGE:
			data1 = tab.get("control").checkint();
			data2 = tab.get("value").optint(0);
			break;
		case ShortMessage.PROGRAM_CHANGE:
			data1 = tab.get("preset").checkint();
			break;
		case ShortMessage.POLY_PRESSURE:
			data1 = tab.get("key").checkint();
			data2 = tab.get("pressure").optint(0);
			break;
		default:
			throw new LuaError("Invalid status"); 
		}
		
		try {
			message.setMessage(statusCode, channel, data1, data2);
			
			device.getReceiver().send(message, System.currentTimeMillis());
		} catch (InvalidMidiDataException | MidiUnavailableException e) {
			throw Utils.toLuaError(e);
		}
		return NONE;
	}
	
	public int getMstStatusCode(String s) {
		switch (s) {
		case "noteOn":
			return ShortMessage.NOTE_ON;
		case "noteOff":
			return ShortMessage.NOTE_OFF;
		case "controllerChange":
			return ShortMessage.CONTROL_CHANGE;
		case "polyKeyPressure":
			return ShortMessage.POLY_PRESSURE;
		case "programChange":
			return ShortMessage.PROGRAM_CHANGE;
		case "channelPressure":
			return ShortMessage.CHANNEL_PRESSURE;
		case "pitchBend":
			return ShortMessage.PITCH_BEND;
		default:
			throw new LuaError("Invalid status");
		}
	}
	
}
