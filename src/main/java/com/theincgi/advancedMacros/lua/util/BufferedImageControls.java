package com.theincgi.advancedMacros.lua.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.gui.Color;
import com.theincgi.advancedMacros.lua.functions.FileSystem;
import com.theincgi.advancedMacros.misc.Utils;

public class BufferedImageControls extends LuaTable{
	BufferedImage img;
	//buffering means multiple objects arn't created and destroyed, but provide a thread safe way to buffer without syncronization
	ThreadLocal<Color> colorBuffer = new ThreadLocal<Color>() { @Override protected Color initialValue() {return new Color(0);} } ;
	
	public static class CreateImg extends TwoArgFunction {
		@Override
		public LuaValue call(LuaValue arg1, LuaValue arg2) {
			BufferedImage img = new BufferedImage(arg1.checkint(), arg2.checkint(), BufferedImage.TYPE_INT_ARGB);
			return new BufferedImageControls(img);
		}
	}
	public static class LoadImg extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue arg) {
			File f = new File(AdvancedMacros.macrosRootFolder, arg.checkjstring());
			try {
				return new BufferedImageControls(ImageIO.read(f));
			} catch (IOException e) {
				throw new LuaError("IOException occurred, "+e.getMessage());
			}
		}
	}
	
	
	public BufferedImageControls(BufferedImage img) {
		this.img = img;
		set("getPixel", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue x, LuaValue y) {
				Color b = colorBuffer.get();
				b.fromHex(img.getRGB(x.checkint(), y.checkint()));
				return b.toLuaValue();
			}
		});
		set("setPixel", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				int x = args.checkint(1);
				int y = args.checkint(2);
				args = args.subargs(3);
				img.setRGB(x, y, Utils.parseColor(args).toInt());
				return NONE;
			}
		});
		set("save", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue arg, LuaValue optFormat) {
				File f = new File(AdvancedMacros.macrosRootFolder, arg.checkjstring());
				if(FileSystem.isValidAddress(arg.checkjstring()))
					try {
						ImageIO.write(img, optFormat.optjstring("png"), f);
					} catch (IOException e) {
						throw new LuaError("IOException occurred, "+e.getMessage());
					}
				else
					throw new LuaError("Invalid address");
				return null;
			}
		});
		set("convertToTable", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				Color buf = colorBuffer.get();
				LuaTable cols = new LuaTable();
				for(int x = 0; x<img.getWidth(); x++) {
					LuaTable sCol = new LuaTable();
					cols.set(x+1, sCol);
					for(int y = 0; y<img.getHeight(); y++) {
						buf.fromHex( img.getRGB(x, y) );
						sCol.set(y+1, buf.toLuaValue());
					}
				}
				return cols;
			}
		});
		set("getWidth",  new ZeroArgFunction() {@Override public LuaValue call() { return valueOf(img.getWidth());};});
		set("getHeight", new ZeroArgFunction() {@Override public LuaValue call() { return valueOf(img.getHeight());};});
		set("getSize",   new VarArgFunction() {
			@Override public Varargs invoke(Varargs unused) { 
				LuaTable temp = new LuaTable();
				temp.set(1, valueOf(img.getWidth()));
				temp.set(2, valueOf(img.getHeight()));
				return temp.unpack();
			}
		});
		
	}
}
