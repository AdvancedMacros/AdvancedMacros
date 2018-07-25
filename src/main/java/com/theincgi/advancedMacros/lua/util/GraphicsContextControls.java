package com.theincgi.advancedMacros.lua.util;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

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
			case drawImage:
				throw new LuaError("Unimplemented");
			case destroy: //aka dispose
				
				throw new LuaError("Unimplemented");
				
			case setColor:
				throw new LuaError("Unimplemented");
				
			case translate:
				throw new LuaError("Unimplemented");
				
			case clipRect:
				throw new LuaError("Unimplemented");
				
			case getColor:
				throw new LuaError("Unimplemented");
				
			case setPaintMode:
				throw new LuaError("Unimplemented");
				
			case setXORMode:
				throw new LuaError("Unimplemented");
				
			case getFont:
				throw new LuaError("Unimplemented");
				
			case setFont:
				throw new LuaError("Unimplemented");
				
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
