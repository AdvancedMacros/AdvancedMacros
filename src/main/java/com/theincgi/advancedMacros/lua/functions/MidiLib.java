package com.theincgi.advancedMacros.lua.functions;

import java.util.ArrayList;
import java.util.Scanner;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.MidiDevice.Info;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaFunction;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.lua.functions.MidiLib.GetMIDIKeyboard.KeyboardInstance;
import com.theincgi.advancedMacros.misc.Utils;

import scala.reflect.internal.Trees.Throw;

public class MidiLib extends LuaTable {
	private final static Object instancesSyncLock = new Object();
	public MidiLib() {
		this.set("getDevice", new GetMIDIKeyboard());
		this.set("stopAll", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				synchronized (instancesSyncLock) {
					System.out.println("Entered sync block");
					System.out.println(GetMIDIKeyboard.instances);
					for (KeyboardInstance ki : GetMIDIKeyboard.instances) {
						System.out.println("LOOPING");
						try {
							if(ki.keyboard.isOpen()) {
								System.out.println("CLosing part 1");
								ki.receiver.close();
								System.out.println("Attempting to close");
								ki.keyboard.close();
								
								System.out.println("Closed!");
							}
						}catch (Throwable e) {
							e.printStackTrace();
							throw new LuaError(e);
						}
					}
					GetMIDIKeyboard.instances.clear();
					System.out.println("Done");
				}
				System.out.println("Returning...");
				return NONE;
			}
		});
		this.set("list", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return getInfoList();
			}
		});
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
	
	public static class GetMIDIKeyboard extends TwoArgFunction{
		
		static ArrayList<KeyboardInstance> instances = new ArrayList<>();

		@Override
		public LuaValue call(LuaValue arg, LuaValue onAction) {
			Object actionSyncronizeLock = new Object();
			LuaFunction action = null;
			if(!onAction.isnil())
				action = onAction.checkfunction();

			String filter;
			if(arg.isnil())
				filter = null;
			else
				filter = arg.checkjstring();
			MidiDevice keyboard = getKeyboard(filter);
			if(keyboard==null)
				return LuaValue.FALSE;

			try {
				KeyboardInstance ki = new KeyboardInstance(keyboard, action);
				synchronized (instancesSyncLock) {
					instances.add(ki);
				}
				return ki;
			} catch (MidiUnavailableException e) {
				throw new LuaError(e);
			}
		}

		public static MidiDevice getKeyboard(String filter) {
			MidiDevice.Info[] infoArr = MidiSystem.getMidiDeviceInfo();
			for (Info info : infoArr) {
				try {
					MidiDevice dev = MidiSystem.getMidiDevice(info);
					if(!(dev instanceof Synthesizer) && !(dev instanceof Sequencer)) {
						if(dev.getMaxTransmitters()!=0) {//has outputs
							if(filter == null || info.getName().toLowerCase().contains(filter.toLowerCase())) {
								System.out.println("Got MIDI: " + info.getDescription());
								return dev;
							}
						}
					}
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
			return null;
		}
		
		

		
		

		public static MidiDevice getKeyboard() {
			return getKeyboard(null);
		}

		private static String getControlName(int i) {
			switch (i) {
			case 0x00: return "Bank Select (Controller # 32 more commonly used)";
			case 0x01: return "Modulation Wheel";
			case 0x02: return "Breath Contoller";
			case 0x03: return "Undefined";
			case 0x04: return "Foot Controller";
			case 0x05: return "Portamento Time";
			case 0x06: return "Data Entry MSB";
			case 0x07: return "Main Volume";
			case 0x08: return "Balance";
			case 0x09: return "Undefined";
			case 0x0A: return "Pan";
			case 0x0B: return "0Ch";
			case 0x0C: return "Effect Control 1";
			case 0x0D: return "Effect Control 2";
			case 0x40: return "Damper Pedal (Sustain) [Data Byte of 0-63=0ff, 64-127=On]";
			case 0x41: return "Portamento";
			case 0x42: return "Sostenuto";
			case 0x43: return "Soft Pedal";
			case 0x44: return "Legato Footswitch";
			case 0x45: return "Hold 2";
			case 0x46: return "Sound Controller 1 (default: Sound Variation)";
			case 0x47: return "Sound Controller 2 (default: Timbre/Harmonic Content)";
			case 0x48: return "Sound Controller 3 (default: Release Time)";
			case 0x49: return "Sound Controller 4 (default: Attack Time)";
			case 0x4A: return "Sound Controller 5 (default: Brightness)";
			case 0x54: return "Portamento Control";
			case 0x5B: return "Effects 1 Depth (previously External Effects Depth)";
			case 0x5C: return "Effects 2 Depth (previously Tremolo Depth)";
			case 0x5D: return "Effects 3 Depth (previously Chorus Depth)";
			case 0x5E: return "Effects 4 Depth (previously Detune Depth)";
			case 0x5F: return "Effects 5 Depth (previously Phaser Depth)";
			case 0x60: return "Data Increment";
			case 0x61: return "Data Decrement";
			case 0x62: return "Non-Registered Parameter Number LSB";
			case 0x63: return "Non-Registered Parameter Number LSB";
			case 0x64: return "Registered Parameter Number LSB";
			case 0x65: return "Registered Parameter Number MSB";
			case 0x79: return "Reset All Controllers";
			case 0x7A: return "Local Control";
			case 0x7B: return "All Notes Off";
			case 0x7C: return "Omni Off";
			case 0x7D: return "Omni On";
			case 0x7E: return "Mono On (Poly Off)";
			case 0x7F: return "Poly On (Mono Off)";
			default:
				if(0x10<=i && i <= 0x13) return "General Purpose Controllers (Nos. 1-4)";
				if(0x20<=i && i <= 0x3F) return "LSB for Controllers 0-31 (rarely implemented)";
				if(0x4B<=i && i <= 0x4F) return "Sound Controller 6-10 (no defaults)";
				if(0x50<=i && i <= 0x53) return "General Purpose Controllers (Nos. 5-8)";
				return "Undefined";

			}
		}

		public static class KeyboardInstance extends LuaTable{
			MidiDevice keyboard;
			LuaFunction action;
			Object actionSyncronizeLock = new Object();
			Receiver receiver;

			public KeyboardInstance(MidiDevice keyboard, LuaFunction action2) throws MidiUnavailableException {
				this.keyboard = keyboard;
				this.action = action2;

				set("open", new ZeroArgFunction() {
					@Override
					public LuaValue call() {
						try {
							keyboard.open();
						} catch (MidiUnavailableException e) {
							throw new LuaError(e);
						}
						return LuaValue.NONE;
					}
				});
				set("isOpen", new ZeroArgFunction() {
					@Override
					public LuaValue call() {
						return LuaValue.valueOf(keyboard.isOpen());
					}
				});
				set("setOnEvent", new OneArgFunction() {
					@Override
					public LuaValue call(LuaValue arg) {
						synchronized (actionSyncronizeLock) {
							action = arg.checkfunction();
						}
						return null;
					}
				});
				set("close", new ZeroArgFunction() {
					@Override
					public LuaValue call() {
						receiver.close();
						keyboard.close();

						return LuaValue.NONE;
					}
				});
				set("getName", new ZeroArgFunction() {
					@Override
					public LuaValue call() {
						return LuaValue.valueOf(keyboard.getDeviceInfo().getName());
					}
				});
				set("getDescription", new ZeroArgFunction() {
					@Override
					public LuaValue call() {
						return LuaValue.valueOf(keyboard.getDeviceInfo().getDescription());
					}
				});
				set("getDescription", new ZeroArgFunction() {
					@Override
					public LuaValue call() {
						return LuaValue.valueOf(keyboard.getDeviceInfo().getVendor());
					}
				});
				set("getVersion", new ZeroArgFunction() {
					@Override
					public LuaValue call() {
						return LuaValue.valueOf(keyboard.getDeviceInfo().getVersion());
					}
				});

				receiver = new Receiver() {
					@Override
					public void send(MidiMessage message, long timeStamp) {
						byte[] bytes = message.getMessage();
						int len = message.getLength();
						parse(bytes, len);
					}

					private void parse(byte[] bytes, int len) {
						int status = ((int)bytes[0]) & 0xF0;//message is left 4 bits, right 4 is channel
						int channel = ((int)bytes[0]) & 0x0F;
						LuaTable t = new LuaTable();
						t.set("channel", channel);
						switch (status) {
						case 0x80:{ //1000nnnn - note off
							for(int i = 1; i<len-1; i+=2) {
								int key = (int)bytes[i  ] & 0xFF;
								int vel = (int)bytes[i+1] & 0xFF;
								t.set("status", "noteOff");
								t.set("key", key);
								t.set("velocity", vel);
								synchronized (actionSyncronizeLock) {
									if(action!=null && !action.isnil())
										try {
											action.invoke(t);
										}catch (Throwable e) {
											throw new LuaError(e);
										}
								}
								//System.out.printf("NOTE OFF - Channel: %3d, Key: %3d, Vel: %3d\n", channel, key, vel);
							}
							break;
						}
						case 0x90:{ //1001nnnn - note on
							for(int i = 1; i<len-1; i+=2) {
								int key = (int)bytes[i  ] & 0xFF;
								int vel = (int)bytes[i+1] & 0xFF;
								t.set("status", "noteOn");
								t.set("key", key);
								t.set("velocity", vel);
								synchronized (actionSyncronizeLock) {
									if(action!=null && !action.isnil())
										try {
											action.invoke(t);
										}catch (Throwable e) {
											throw new LuaError(e);
										}
								}
								//System.out.printf("NOTE ON - Channel: %3d, Key: %3d, Vel: %3d\\n", channel, key, vel);
							}
							break;
						}
						case 0xA0:{ //1010nnnn - poly key pressure
							for(int i = 1; i<len-1; i+=2) {
								int key = (int)bytes[i] & 0xFF;
								int pressure = (int)bytes[i+1] & 0xFF;
								t.set("status", "poly key pressure");
								t.set("key", key);
								t.set("pressure", pressure);
								synchronized (actionSyncronizeLock) {
									if(action!=null && !action.isnil())
										try {
											action.invoke(t);
										}catch (Throwable e) {
											throw new LuaError(e);
										}
								}
								//System.out.printf("Poly Key Pressure - Channel: %3d, Key: %3d, Pressure: %3d\n", channel, key, pressure);
							}
							break;
						}
						case 0xB0:{ //1011nnnn - controller change
							for(int i = 1; i<len-1; i+=2) {
								int cntrlNum = ((int)bytes[i]) & 0xFF;
								String controlName = getControlName(cntrlNum);
								int value = (int)bytes[i+1] &0xFF;
								t.set("status", "controllerChange");
								t.set("control", controlName);
								t.set("controlID", cntrlNum);
								t.set("value", value);
								synchronized (actionSyncronizeLock) {
									if(action!=null && !action.isnil())
										try {
											action.invoke(t);
										}catch (Throwable e) {
											throw new LuaError(e);
										}
								}
								//System.out.printf("Controller Change - Channel: %3d, %s (%3d), %3d\n", channel, controlName, cntrlNum, value);
							}
						}
						case 0xC0:{ //1100nnnn - program change
							for(int i = 1; i<len-1; i+=1) {
								int presetNumber = (int)bytes[i] & 0xFF;
								t.set("status", "programChange");
								t.set("preset", presetNumber);
								synchronized (actionSyncronizeLock) {
									if(action!=null && !action.isnil())
										try {
											action.invoke(t);
										}catch (Throwable e) {
											throw new LuaError(e);
										}
								}
								//System.out.printf("Program Change - Channel: %3d, Preset: %3d\n", channel, presetNumber);
							}
							break;
						}
						case 0xD0:{ //1101nnnn - channel pressure
							for(int i = 1; i<len-1; i+=1) {
								int pressure = (int)bytes[i] & 0xFF;
								t.set("status", "channelPressure");
								t.set("pressure", pressure);
								synchronized (actionSyncronizeLock) {
									if(action!=null && !action.isnil())
										try {
											action.invoke(t);
										}catch (Throwable e) {
											throw new LuaError(e);
										}
								}
								//System.out.printf("Channel Pressure - Channel: %3d, Pressure: %3d\n", channel, pressure);
							}
							break;
						}
						case 0xE0:{ //1110nnnn - pitch bend
							for(int i = 1; i<len-1; i+=2) {
								int fine = (int)bytes[i] & 0xFF;
								int coarse = (int)bytes[i+1] & 0xFF;
								t.set("status", "pitchBend");
								t.set("fine", fine);
								t.set("coarse", coarse);
								synchronized (actionSyncronizeLock) {
									if(action!=null && !action.isnil())
										try {
											action.invoke(t);
										}catch (Throwable e) {
											throw new LuaError(e);
										}
								}
								//System.out.printf("Pitch Bend - Channel: %3d, Fine: %3d, Coarse: %3d\n", channel, fine, coarse);
							}
							break;
						}
						default:
							break;
						}
					}

					@Override
					public void close() {

					}
				};
				keyboard.getTransmitter().setReceiver(receiver);
			}

			public void close() {
				try {
				receiver.close();
				}catch (Throwable e) {
					e.printStackTrace();
				}
				try {
					if(keyboard.isOpen())
						keyboard.close();
				}catch (Throwable e) {
					e.printStackTrace();
				}
			}



			//		public static void load(String filter) throws MidiUnavailableException, InterruptedException {
			//			MidiDevice keyboard = getKeyboard(filter);
			//			if(keyboard==null) {
			//				System.out.println("Keyboard returned null... Ending test");
			//				return;
			//			}
			//		}
		}
	}
}
