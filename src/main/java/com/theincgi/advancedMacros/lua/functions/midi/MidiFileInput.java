package com.theincgi.advancedMacros.lua.functions.midi;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.misc.Utils;

public class MidiFileInput extends LuaTable{
	public MidiFileInput(String fileName) {
		try {
			File f;
			if(fileName.contains(":")) {//absolute location
				f = new File(fileName);
			}else { //relative location (in macros folder)
				f = new File(AdvancedMacros.macrosRootFolder, fileName);
			}
			
			Sequencer sequencer = MidiSystem.getSequencer(false);
			sequencer.setSequence(MidiSystem.getSequence(f));
			
			LuaReceiver receiver = new LuaReceiver();
			sequencer.getTransmitter().setReceiver(receiver);
			
			
			
			this.set("getName", new ZeroArgFunction() {
				@Override public LuaValue call() {
					return LuaValue.valueOf(sequencer.getDeviceInfo().getName());
				}
			});
			this.set("getDescription", new ZeroArgFunction() {
				@Override public LuaValue call() {
					return LuaValue.valueOf(sequencer.getDeviceInfo().getDescription());
				}
			});
			this.set("getVersion", new ZeroArgFunction() {
				@Override public LuaValue call() {
					return LuaValue.valueOf(sequencer.getDeviceInfo().getVersion());
				}
			});
			this.set("getVendor", new ZeroArgFunction() {
				@Override public LuaValue call() {
					return LuaValue.valueOf(sequencer.getDeviceInfo().getVendor());
				}
			});
			this.set("setOnEvent", new OneArgFunction() {

				@Override
				public LuaValue call(LuaValue arg) {
					if(arg.isnil())
						receiver.setOnEvent(null);
					else
						receiver.setOnEvent(arg.checkfunction());
					return NONE;
				}
			});
			//
			this.set("start", new ZeroArgFunction() {
				@Override public LuaValue call() {
					 sequencer.start(); return NONE;
				}
			});
			this.set("isOpen", new ZeroArgFunction() {
				@Override public LuaValue call() {
					return LuaValue.valueOf(sequencer.isOpen());
				}
			});

			//Control section
			this.set("open", new ZeroArgFunction() {
				@Override public LuaValue call() {
					try {
						sequencer.open();
						MidiLib2.devices.add(sequencer);
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
					sequencer.close();
					MidiLib2.devices.remove(sequencer);
					return NONE;
				}
			});
			this.set("stop", new ZeroArgFunction() {
				@Override public LuaValue call() {
					sequencer.stop();
					return NONE;
				}
			});
			this.set("isPlaying", new ZeroArgFunction() {
				@Override public LuaValue call() {
					return LuaValue.valueOf(sequencer.isRunning());
				}
			});
			this.set("getBPM", new ZeroArgFunction() {
				@Override public LuaValue call() {
					return LuaValue.valueOf(sequencer.getTempoInBPM());
				}
			});
			this.set("getLength", new ZeroArgFunction() {
				@Override public LuaValue call() {
					return LuaValue.valueOf(sequencer.getTickLength());
				}
			});
			this.set("getPos", new ZeroArgFunction() {
				@Override public LuaValue call() {
					return LuaValue.valueOf(sequencer.getTickPosition());
				}
			});
			this.set("seek", new OneArgFunction() {
				@Override public LuaValue call(LuaValue arg) {
					sequencer.setTickPosition(arg.checklong());
					return NONE;
				}
			});
			this.set("getLoop", new ZeroArgFunction() {
				@Override public LuaValue call() {
					return LuaValue.valueOf(sequencer.getLoopCount());
				}
			});
			this.set("setCount", new OneArgFunction() {
				@Override public LuaValue call(LuaValue arg) {
					if(arg.checkint()==-1)
						sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
					else
						sequencer.setLoopCount(arg.checkint());
					return NONE;
				}
			});
			this.set("setLoopStart", new OneArgFunction() {
				@Override public LuaValue call(LuaValue arg) {
					sequencer.setLoopStartPoint(arg.checklong());
					return NONE;
				}
			});
			this.set("setLoopEnd", new OneArgFunction() {
				@Override public LuaValue call(LuaValue arg) {
					sequencer.setLoopEndPoint(arg.checklong());
					return NONE;
				}
			});
			this.set("getLoopStart", new ZeroArgFunction() {
				@Override public LuaValue call() {
					return LuaValue.valueOf(sequencer.getLoopStartPoint());
				}
			});
			this.set("getLoopStart", new ZeroArgFunction() {
				@Override public LuaValue call() {
					return LuaValue.valueOf(sequencer.getLoopEndPoint());
				}
			});
		} catch (MidiUnavailableException | InvalidMidiDataException | IOException e) {
			e.printStackTrace();
			throw Utils.toLuaError(e);
		}
	}
}
