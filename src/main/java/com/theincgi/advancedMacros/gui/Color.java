package com.theincgi.advancedMacros.gui;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;

import net.minecraft.client.renderer.GlStateManager;

public class Color {
	int a,r,g,b;
	//public boolean quoteMode;
	public static final Color WHITE = new Color(255, 255, 255);
	public static final Color BLACK = new Color(  0,   0,   0);
	/**0-Black<br>
	 * 1-Dark Blue<br>
	 * 2-Dark Green<br>
	 * 3-Dark Aqua<br>
	 * 4-Dark Red<br>
	 * 5-Dark Purple<br>
	 * 6-Orange<br>
	 * 7-Light Gray<br>
	 * 8-Gray<br>
	 * 9-Blue<br>
	 * A-Lime<br>
	 * B-Sky Blue<br>
	 * C-Red<br>
	 * D-Light Purple<br>
	 * E-Yellow<br>
	 * F-White<br>*/
	public static final Color

	TEXT_0 = new Color(  0,   0,   0),
	TEXT_1 = new Color(  0,   0, 170),
	TEXT_2 = new Color(  0, 170,   0),
	TEXT_3 = new Color(  0, 170, 170),
	TEXT_4 = new Color(170,   0,   0),
	TEXT_5 = new Color(170,   0, 170),
	TEXT_6 = new Color(255, 170,   0),
	TEXT_7 = new Color(170, 170, 170),
	TEXT_8 = new Color(85 ,  85,  85),
	TEXT_9 = new Color(85,   85, 255),
	TEXT_a = new Color(85,  255,  85),
	TEXT_b = new Color(85,  255, 255),
	TEXT_c = new Color(255,  85,  85),
	TEXT_d = new Color(255,  85, 255),
	TEXT_e = new Color(255, 255,  85),
	TEXT_f = new Color(255, 255, 255);
	public static final Color CLEAR = new Color(0x00_00_00_00);


	public Color(int a, int r, int g, int b) {
		if(a < 0   || r < 0   || g < 0   || b < 0 || 
				a > 255 || r > 255 || g > 255 || b > 255) {
			throw new IllegalArgumentException("Value out of range 0-255");
		}
		this.a = a;
		this.r = r;
		this.g = g;
		this.b = b;
	}

	public Color(int r, int g, int b) {
		this(0xFF,r,g,b);
	}
	
	public Color(int hexCode){
		fromHex(hexCode);
	}

	public Color(float r, float g, float b) {
		this(1, r, g, b);
	}
	public Color(float a, float r, float g, float b) {
		this((int)(a*255),(int)(r*255),(int)(g*255),(int)(b*255));
	}
	
	/**Sets this color from the hex code given*/
	public void fromHex(int hexCode) {
		this.r = (hexCode >> 16 & 255);
		this.b = (hexCode >> 0 & 255);
		this.g = (hexCode >> 8 & 255);
		this.a = (hexCode >> 24 & 255);		
	}

	public static int getA(int hexCode) {
		return (hexCode >> 24 & 255);
	}
	public static int getR(int hexCode) {
		return (hexCode >> 16 & 255);
	}
	public static int getG(int hexCode) {
		return (hexCode >> 8 & 255);
	}
	public static int getB(int hexCode) {
		return (hexCode >> 0 & 255);
	}

	public int getA() {
		return a;
	}

	public float getAFloat() {
		return a/255f;
	}
	
	public Color setA(int a) {
		this.a = a;
		return this;
	}
	public Color setA(float a) {
		this.a = (int) Math.floor(a*255);
		return this;
	}
	
	public int getR() {
		return r;
	}

	public Color setR(int r) {
		this.r = r;
		return this;
	}
	public Color setR(float r) {
		this.r = (int) Math.floor(r*255);
		return this;
	}

	public int getG() {
		return g;
	}

	public Color setG(int g) {
		this.g = g;
		return this;
	}
	public Color setG(float g) {
		this.g = (int) Math.floor(g*255);
		return this;
	}

	public int getB() {
		return b;
	}

	public Color setB(int b) {
		this.b = b;
		return this;
	}
	public Color setB(float b) {
		this.b = (int) Math.floor(b*255);
		return this;
	}
	
	public void apply() {
		GlStateManager.color(r/255f, g/255f, b/255f, a/255f);
	}
	
	@Override
	public String toString() {
		return "Color [a=" + a + ", r=" + r + ", g=" + g + ", b=" + b + "]";
	}

	public int toInt() {
		int i = 0;
		i+= a<<24;
		i+= r<<16;
		i+= b<<0;
		i+= g<<8;
		//		this.red = (float)(color >> 16 & 255) / 255.0F;
		//        this.blue = (float)(color >> 8 & 255) / 255.0F;
		//        this.green = (float)(color & 255) / 255.0F;
		//        this.alpha = (float)(color >> 24 & 255) / 255.0F;
		return i;
	}
	public LuaTable toLuaValue(boolean use255Space){
		LuaTable t = new LuaTable();
		//		t.set("r", LuaValue.valueOf(r));
		//		t.set("g", LuaValue.valueOf(g));
		//		t.set("b", LuaValue.valueOf(b));//TODO niceify output
		//		t.set("a", LuaValue.valueOf(a));
		if(use255Space) {
			t.set(1, LuaValue.valueOf(r));
			t.set(2, LuaValue.valueOf(g));
			t.set(3, LuaValue.valueOf(b));
			t.set(4, LuaValue.valueOf(a));
		}else {
			t.set(1, LuaValue.valueOf(r/255f));
			t.set(2, LuaValue.valueOf(g/255f));
			t.set(3, LuaValue.valueOf(b/255f));
			t.set(4, LuaValue.valueOf(a/255f));
		}
		return t;
	}
	public Color copy(){
		return new Color(toInt());
	}

	public Color setRGB(int r, int g, int b) {
		this.r = r;
		this.g = g;
		this.b = b;
		return this;
	}
	public Color setARGB(int a, int r,int g, int b){
		setRGB(r, g, b);
		this.a = a;
		return this;
	}
	/**Copies values from object, this is so you dont need to make a new obj when changing colors to a setting<br>returns this*/
	public Color setFrom(Color c){
		setARGB(c.a, c.r, c.g, c.b);
		return this;
	}

	public java.awt.Color toAWTColor() {
		return new java.awt.Color(r, g, b, a);
	}

}
