package com.theincgi.advancedMacros.lua.util;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import com.theincgi.advancedMacros.gui.Color;
import com.theincgi.advancedMacros.misc.Utils;

public class GraphicsContextControls extends LuaTable{
	BufferedImage img;
	Graphics g;
	public GraphicsContextControls(BufferedImage bi) {
		img = bi;
		g = bi.getGraphics();

		loadFuncs();

		//setColor
		//draw/fill rect, 

		g.draw
		//destroy
	}


	private void loadFuncs() {
		for(Draw op : Draw.values()) {
			this.set(op.name(), new DrawOp(op));
		}
	}

	public class DrawOp extends VarArgFunction {
		Draw op;
		public DrawOp(Draw op) {
			this.op = op;
		}
		@Override
		public Varargs invoke(Varargs args) {
			switch(op) {
			//case getFontMetrics:
			//  throw new LuaError("Unimplemented");
			case drawImage:{
				LuaValue v = args.arg1();
				if(v instanceof BufferedImageControls) {
					BufferedImageControls bic = (BufferedImageControls) v;
					args = args.subargs(2);
					switch (args.narg()) {
					case 2:
						g.drawImage(bic.getImg(), args.arg(1).checkint(), args.arg(2).checkint(), null);
						return NONE;
					case 4:
						g.drawImage(bic.getImg(), args.checkint(1), args.checkint(2), //dest x, y 1
								                  args.checkint(3), args.checkint(4), //dest x, y 2
								                  args.checkint(5), args.checkint(6), //source x, y 1
								                  args.checkint(7), args.checkint(8), null);  //source x, y 2
						return NONE;
					default:
						throw new LuaError("Unexpected argument count, 3 or 5, got "+(args.narg()+1));
					}
				}else {
					throw new LuaError("arg 1 is not a usable image");
				}
			}
			case destroy: //aka dispose
				g.dispose();
				return NONE;
				
			case setColor: {
				g.setColor(Utils.parseColor(args).toAWTColor());
				return NONE;
			}
			case translate:
				g.translate(args.checkint(1), args.checkint(2));
				return NONE;
				
			case clipRect:
				g.clipRect(args.checkint(1), args.checkint(2), //x, y
					       args.checkint(3), args.checkint(4)); //width, height
				return NONE;
			case getColor:
				return new Color(g.getColor().getRGB()).toLuaValue();
				
			case setPaintMode:
				g.setPaintMode();
				return NONE;
				
			case setXORMode:
				g.setXORMode(Utils.parseColor(args).toAWTColor());
				return NONE;
				
			case getFont:{
				Font f = g.getFont();
				LuaTable out = new LuaTable();
				out.set("name", f.getFamily());
				out.set("size", f.getSize());
				out.set("isBold",   LuaValue.valueOf(f.isBold()));
				out.set("isItalic", LuaValue.valueOf(f.isItalic()));
				return out; //TODO font measurments
			}
			case setFont:
				//TODO table arg and vararg name, <size>, <bold>, <italic>
				throw new LuaError("Unimplemented");
			case getFonts:
				
			case getClipBounds:
				throw new LuaError("Unimplemented");
				
			case setClip:
				throw new LuaError("Unimplemented");
				
			case getClip:
				throw new LuaError("Unimplemented");
				
			case copyArea:
				throw new LuaError("Unimplemented");
				
			case drawLine:
				throw new LuaError("Unimplemented");
				
			case fillRect:
				throw new LuaError("Unimplemented");
				
			case drawRect:
				throw new LuaError("Unimplemented");
				
			case clearRect:
				throw new LuaError("Unimplemented");
				
			case drawRoundRect:
				throw new LuaError("Unimplemented");
				
			case fillRoundRect:
				throw new LuaError("Unimplemented");
				
			case draw3DRect:
				throw new LuaError("Unimplemented");
				
			case fill3DRect:
				throw new LuaError("Unimplemented");
				
			case drawOval:
				throw new LuaError("Unimplemented");
				
			case fillOval:
				throw new LuaError("Unimplemented");
				
			case drawArc:
				throw new LuaError("Unimplemented");
				
			case fillArc:
				throw new LuaError("Unimplemented");
				
			case drawPolyline:
				throw new LuaError("Unimplemented");
				
			case drawPolygon:
				throw new LuaError("Unimplemented");
				
			case fillPolygon:
				throw new LuaError("Unimplemented");
				
			case drawString:
				throw new LuaError("Unimplemented");
				
			case drawChars:
				throw new LuaError("Unimplemented");
				
			case drawBytes:
				throw new LuaError("Unimplemented");
				
			case getClipRect:
				throw new LuaError("Unimplemented");
				
			case hitClip:
				throw new LuaError("Unimplemented");
				//			case equals:
				//			  throw new LuaError("Unimplemented");
			case hashCode:
				throw new LuaError("Unimplemented");;

			}
		}
	}
	public static enum Draw {
		//getFontMetrics, 
		drawImage, 
		hashCode, 
		destroy, 
		hitClip, 
		getClipRect, 
		drawBytes, 
		drawChars, 
		drawString, 
		fillPolygon, 
		drawPolygon, 
		drawPolyline, 
		fillArc, 
		setColor, 
		translate, 
		clipRect, 
		getColor, 
		setPaintMode, 
		setXORMode, 
		drawLine, 
		copyArea, 
		clearRect, 
		fillRoundRect, 
		draw3DRect, 
		fill3DRect, 
		drawOval, 
		drawArc, 
		fillOval, 
		getFont, 
		setFont, 
		getClipBounds, 
		setClip, 
		getClip, 
		fillRect, 
		drawRect, 
		drawRoundRect
		//equals, 
	}
}
