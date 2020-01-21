package com.theincgi.advancedMacros.lua.functions;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
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

import com.theincgi.advancedMacros.misc.Utils;

public class GetSound extends OneArgFunction{
	@Override
	public LuaValue call(LuaValue arg) {
		return play(Utils.parseFileLocation(arg));
	}
	
	
	public static LuaTable play(File f) { //rename
		try {
			final Clip clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(f))));
			
			final Cntrls cntrls = new Cntrls(clip);
			
			clip.addLineListener(new LineListener() {
				@Override
				public void update(LineEvent event) {
					if(event.getType().equals(LineEvent.Type.STOP) && !cntrls.paused) {
						clip.close();
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
				paused = true; //TODO  If desired, the retained data can bediscarded by invoking the flush method.When audio capture or playback stops, a STOP event is generated.
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
	}
}
