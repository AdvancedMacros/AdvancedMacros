package com.theincgi.advancedMacros.lua.functions;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;
import org.luaj.vm2_v3_0_1.lib.jse.CoerceJavaToLua;

import com.theincgi.advancedMacros.AdvancedMacros;

public class PlaySound {
	public static class FromFile extends OneArgFunction{



		public static LuaTable play(File f) {
			try {
				final AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(f)));
				DataLine.Info info = new DataLine.Info(Clip.class, ais.getFormat());
				final Clip clip = AudioSystem.getClip();
				clip.open( ais );
				
				final Cntrls cntrls = new Cntrls(clip);
				
				clip.addLineListener(new LineListener() {
					@Override
					public void update(LineEvent event) {
						if(event.getType().equals(LineEvent.Type.STOP) && !cntrls.paused) {
							clip.close();
							try {
								ais.close();
							} catch (IOException e) {
							}
						}
					}
				});
				
				return cntrls.loadFuncs();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				throw new LuaError("FileNotFoundException");
			} catch (LineUnavailableException e) {
				e.printStackTrace();
				throw new LuaError("LineUnavailableException");
			} catch (IOException e) {
				e.printStackTrace();
				throw new LuaError("IOExceptionException");
			} catch (UnsupportedAudioFileException e) {
				e.printStackTrace();
				throw new LuaError("UnsupportedAudioFileException");
			}
		}
		public static class Cntrls{
			Clip c;
			boolean paused=false;
			public Cntrls(Clip c) {
				this.c = c;
			}
			
			public LuaTable loadFuncs() {
				LuaTable t = new LuaTable();
				t.set("isPlaying", new IsPlaying());
				t.set("stop", new Stop());
				t.set("loop", new Loop());
				t.set("pause", new Pause());
				t.set("play", new Start());
				t.set("setVolume", new SetVolume());
				t.set("getClip", new GetRaw());
				return t;
			}
			public class SetVolume extends OneArgFunction{
				@Override
				public LuaValue call(LuaValue arg) {
					FloatControl gainControl = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
					float min = gainControl.getMinimum();
					float range = gainControl.getMaximum() - min;
					float gain = (float) ((range * arg.checkdouble()) + min);
					gainControl.setValue(gain);
					return NONE;
				}
			}
			private class Start extends ZeroArgFunction{
				@Override
				public LuaValue call() {
					c.start();
					paused = false;
					return LuaValue.NONE;
				}
			}
			private class Pause extends ZeroArgFunction{
				@Override
				public LuaValue call() {
					paused = true;
					c.stop();
					return LuaValue.FALSE;
				}
			}
			private class Stop extends ZeroArgFunction{
				@Override
				public LuaValue call() {
					paused = false;
					c.stop();
					return LuaValue.NONE;
				}
			}
			private class Loop extends OneArgFunction{
				@Override
				public LuaValue call(LuaValue arg) {
					if(arg.isnil()) {
						c.loop(Clip.LOOP_CONTINUOUSLY);
					}else {
						c.loop(arg.checkint());
					}
					return LuaValue.NONE;
				}
			}
			private class IsPlaying extends ZeroArgFunction{	
				@Override
				public LuaValue call() {
					return LuaValue.valueOf(c.isRunning());
				}
			}
			private class GetRaw extends ZeroArgFunction{	
				@Override
				public LuaValue call() {
					return CoerceJavaToLua.coerce(c);
				}
			}
		}
		//fromFile
		@Override
		public LuaValue call(LuaValue arg) {
			File f = new File(AdvancedMacros.macroSoundsFolder, arg.checkjstring()); //TODO starts with *:// or something, and make folder
			return play(f);
		}
	}
}
