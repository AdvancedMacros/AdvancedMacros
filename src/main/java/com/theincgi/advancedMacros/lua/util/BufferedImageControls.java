package com.theincgi.advancedMacros.lua.util;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
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
import com.theincgi.advancedMacros.lua.LuaValTexture;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.renderer.texture.DynamicTexture;

public class BufferedImageControls extends LuaTable{
	BufferedImage img;
	DynamicTexture dynamicTexture;
	LuaValTexture tex;
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
			File f = Utils.parseFileLocation(arg);//new File(AdvancedMacros.macrosRootFolder, arg.checkjstring());
			try {
				return new BufferedImageControls(ImageIO.read(f));
			} catch (IOException e) {
				throw new LuaError("IOException occurred, "+e.getMessage());
			}
		}
	}
	public static class GetFonts extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			LuaTable fonts = new LuaTable();
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

			for (Font font : ge.getAllFonts()) {
				if(!font.getFontName().toLowerCase().contains("italic") && !font.getFontName().toLowerCase().contains("bold"))
					fonts.set(fonts.length()+1, font.getFontName());
			}
			return fonts;
		}
	}
	public static class GetFormats extends ZeroArgFunction {
		@Override
		public LuaValue call() {
			LuaTable out = new LuaTable();
			LuaTable temp = new LuaTable();
			for(String s : ImageIO.getReaderFormatNames())
				temp.set(temp.length()+1, s);
			out.set("readers", temp);
			temp = new LuaTable();
			for(String s : ImageIO.getWriterFormatNames())
				temp.set(temp.length()+1, s);
			out.set("writers", temp);
			return out;
		}
	}
	

	public BufferedImageControls(BufferedImage img) {
		this.img = img;
		set("getPixel", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue x, LuaValue y) {
				Color b = colorBuffer.get();
				b.fromHex(img.getRGB(x.checkint()-1, y.checkint()-1));
				return b.toLuaValue(AdvancedMacros.COLOR_SPACE_IS_255);
			}
		});
		set("setPixel", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				int x = args.checkint(1)-1;
				int y = args.checkint(2)-1;
				args = args.subargs(3);
				img.setRGB(x, y, Utils.parseColor(args, AdvancedMacros.COLOR_SPACE_IS_255).toInt());
				return NONE;
			}
		});
		set("save", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue arg, LuaValue optFormat) {
					try {
						File f = Utils.parseFileLocation(arg);//FileSystem.parseFileLocation(arg);
						ImageIO.write(img, optFormat.optjstring("png"), f);
					} catch (IOException e) {
						throw new LuaError("IOException occurred, "+e.getMessage());
					}
				return NONE;
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
						sCol.set(y+1, buf.toLuaValue(AdvancedMacros.COLOR_SPACE_IS_255));
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
		set("graphics", new GraphicsContextControls(this));
		LuaTable texture = new LuaTable();
		set("update", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				if (dynamicTexture != null) { 
					AdvancedMacros.getMinecraft().addScheduledTask(()->{ //changed to non blocking to prevent possible thread locks
						img.getRGB(0, 0, img.getWidth(), img.getHeight(), dynamicTexture.getTextureData(), 0, img.getWidth());
						dynamicTexture.updateDynamicTexture();
					});
				}
				return NONE;
				//throw new LuaError("Dynamic texture not created yet");
			}
		});
		


	}

	public BufferedImage getImg() {
		return img;
	}
	private void createTexture() {
		Utils.runOnMCAndWait(()->{
			dynamicTexture = new DynamicTexture(img);
			tex = new LuaValTexture(dynamicTexture);
		});
	}
	public DynamicTexture getDynamicTexture() {
		if (dynamicTexture == null) {
			//throw new LuaError("Dynamic texture not created yet");
			//dynamicTexture = new DynamicTexture(img);
			createTexture();
		}
		return dynamicTexture;
	}
	public LuaValTexture getLuaValTexture() {
		if(tex==null)
			createTexture();
		return tex;
	}
}
