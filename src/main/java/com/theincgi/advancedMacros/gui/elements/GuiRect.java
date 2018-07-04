package com.theincgi.advancedMacros.gui.elements;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.gui.Color;
import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.misc.Property;
import com.theincgi.advancedMacros.misc.PropertyPalette;
import com.theincgi.advancedMacros.misc.Settings;
import com.theincgi.advancedMacros.misc.Utils;

public class GuiRect implements Drawable, Moveable{
	WidgetID wID;
//	Property colorFrame;
//	Property colorFill;
//	Property colorShade;
	
	PropertyPalette propertyPalette;
	
	public boolean isVisible = true;
	private int x,y,wid,hei;
	public Color fill = DEFAULT_FILL;
	public Color frame = DEFAULT_FRAME;
	public Color shade = DEFAULT_SHADE;
	public static final Color DEFAULT_SHADE = new Color(100, 100, 100, 255);
	public static final Color DEFAULT_FILL = Color.BLACK;
	public static final Color DEFAULT_FRAME = Color.WHITE;
	protected boolean doAnimation = false;
	private int drawX, drawY, drawWid,drawHei;
	private double scale;
	//protected Gui gui;
	private static final String defaultPropTableName = "colors.standardRect";
	
	public GuiRect(int x, int y, int wid, int hei, String...propPath) {
		this(x, y, wid, hei, new PropertyPalette(propPath, Settings.settings));
	}
	public GuiRect(int x, int y, int wid, int hei, PropertyPalette propPal) {
		this.x = x;
		this.y = y;
		this.wid = wid;
		this.hei = hei;
		this.propertyPalette = propPal;
		propPal.addColorIfNil(DEFAULT_FRAME, "colors", "frame");
		propPal.addColorIfNil(DEFAULT_SHADE, "colors", "shade");
		propPal.addColorIfNil(DEFAULT_FILL , "colors", "fill" );
		
		fill =  propPal.getColor("colors", "fill" );
		frame = propPal.getColor("colors", "frame");
		shade = propPal.getColor("colors", "shade");
	}
	
//	public GuiRect(WidgetID wID, int x, int y, int wid, int hei, String nullOrPropTableName) {
//		super();
//		this.wID = wID;
//		nullOrPropTableName = nullOrPropTableName==null?defaultPropTableName:nullOrPropTableName;
//		loadProps(nullOrPropTableName);
//		//this.gui = g;
//		this.x = x;
//		this.y = y;
//		this.wid = wid;
//		this.hei = hei;
//		scale = 1;
//		scale(1);
//		fill = propertyPalette.getColor("colors", "fill");//Utils.parseColor(colorFill.getPropValue());
//		frame= propertyPalette.getColor("colors", "frame");//Utils.parseColor(colorFrame.getPropValue());
//		shade= propertyPalette.getColor("colors", "shade");//Utils.parseColor(colorShade.getPropValue());
//	}
//	public GuiRect(WidgetID wID, int x, int y, int wid, int hei, String nullOrPropTableName, Color fill, Color frame) {
//		super();
//		this.wID = wID;
//		nullOrPropTableName = nullOrPropTableName==null?defaultPropTableName:nullOrPropTableName;
//		loadProps(nullOrPropTableName, fill, frame);
//		//this.gui = g;
//		this.x = x;
//		this.y = y;
//		this.wid = wid;
//		this.hei = hei;
//		scale = 1;
//		scale(1);
//		this.fill = Utils.parseColor(colorFill.getPropValue());
//		this.frame= Utils.parseColor(colorFrame.getPropValue());
//		shade= Utils.parseColor(colorShade.getPropValue());
//	}
//	protected void loadProps(String nullOrPropTableName) {
//		nullOrPropTableName = nullOrPropTableName==null?defaultPropTableName :nullOrPropTableName;
//		colorFill  = new Property(nullOrPropTableName+".fill" , new Color(0, 0, 0).toLuaValue(), "color.fill", wID);
//		colorFrame = new Property(nullOrPropTableName+".frame", new Color(0,0,0,0).toLuaValue(), "color.frame", wID);
//		colorShade = new Property(nullOrPropTableName+".shade", DEFAULT_SHADE.toLuaValue(), "color.shade", wID);
//	}
//	protected void loadProps(String nullOrPropTableName, Color fill, Color frame) {
//		nullOrPropTableName = nullOrPropTableName==null?defaultPropTableName :nullOrPropTableName;
//		colorFill  = new Property(nullOrPropTableName+".fill" , fill.toLuaValue(), "color.fill", wID);
//		colorFrame = new Property(nullOrPropTableName+".frame", frame.toLuaValue(), "color.frame", wID);
//		colorShade = new Property(nullOrPropTableName+".shade", DEFAULT_SHADE.toLuaValue(), "color.shade", wID);
//	}
	public GuiRect setFill(Color fill){
		this.fill = fill;
		return this;
	}
	public GuiRect setFrame(Color frame){
		this.frame = frame;
		return this;
	}
	public GuiRect setShadeFill(Color shade){
		this.shade = shade;
		return this;
	}
	public void move(int x, int y){
		this.x = x;
		this.y = y;
		scale(scale);
	}
	public void resize(int wid, int hei){
		this.wid = wid;
		this.hei = hei;
		scale(scale);
	}
	/**scale(1) will reset*/
	public void scale(double s){
		double cX = (x + x+wid)/2;
		double cY = (y + y+hei)/2;
		drawX = (int) (cX-(wid*s)/2);
		drawY = (int) (cY-(hei*s)/2);
		drawWid = (int) (wid*s);
		drawHei = (int) (hei*s);
	}
	public double getScale(){return scale;}
	public boolean isInBounds(int x, int y){
		if(x>this.x && y>this.y && x<(this.x+wid) && y<(this.y+hei))
			return true;
		return false;
	}
	public static boolean isInBounds(int clickX, int clickY, int x, int y, int wid, int hei){
		if(clickX>x && clickY>y && clickX<(x+wid) && clickY<(y+hei))
			return true;
		return false;
	}
	public boolean isInScaledBounds(int x, int y){
		if(x>drawX && y>drawY && x<(drawX+drawWid) && y<(drawY+drawHei))
			return true;
		return false;
	}
	
	protected void drawShade(Gui gui){
		//GlStateManager.enableAlpha();
		net.minecraft.client.gui.Gui.drawRect(drawX+1, drawY+1, drawX+drawWid, drawY+drawHei, shade.toInt());
		//System.out.println(shade);
	}
	@Override
	public int getX() {
		return x;
	}
	@Override
	public int getY() {
		return y;
	}
	public int getWid() {
		return wid;
	}
	public int getHei() {
		return hei;
	}
	public int getDrawX() {
		return drawX;
	}
	public int getDrawY() {
		return drawY;
	}
	public int getDrawWid() {
		return drawWid;
	}
	public int getDrawHei() {
		return drawHei;
	}
	/**Only generate on request, there may end up being lots of buttons!*/
	public LuaTable getWidgetControls(){
		LuaTable controls = new LuaTable();
		controls.set("setFill", new SetFill());
		controls.set("setFrame", new SetFrame());
		controls.set("setShade", new SetShade());
		controls.set("move", new Move());
		controls.set("resize", new Resize());
		
		controls.set("getFill", new GetFill());
		controls.set("getFrame", new GetFrame());
		
		controls.set("getPos", new GetPos());
		controls.set("getSize", new GetSize());
		controls.set("getShade", new GetShade());
		//TODO Animation and additional properties
		return controls;
	}
	private class SetFill extends VarArgFunction{
		@Override
		public Varargs invoke(Varargs args) {
			fill = Utils.parseColor(args);
			return LuaValue.NONE;
		}
	}
	private class SetFrame extends VarArgFunction{
		@Override
		public Varargs invoke(Varargs args) {
			frame = Utils.parseColor(args);
			return LuaValue.NONE;
		}
	}
	private class SetShade extends VarArgFunction{
		@Override
		public Varargs invoke(Varargs args) {
			shade = Utils.parseColor(args);
			return LuaValue.NONE;
		}
	}
	
	private class Move extends TwoArgFunction{
		@Override
		public LuaValue call(LuaValue arg1, LuaValue arg2) {
			move(arg1.checkint(), arg2.checkint());
			return LuaValue.NONE;
		}
	}
	private class Resize extends TwoArgFunction{
		@Override
		public LuaValue call(LuaValue arg1, LuaValue arg2) {
			resize(arg1.checkint(), arg2.checkint());
			return LuaValue.NONE;
		}
	}
	
	private class GetFill extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			return fill.toLuaValue();
		}
	}
	private class GetFrame extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			return frame.toLuaValue();
		}
	}
	
	private class GetPos extends VarArgFunction{
		@Override
		public Varargs invoke(Varargs args) {
			LuaTable temp = new LuaTable();
			temp.set(1, LuaValue.valueOf(getX()));
			temp.set(2, LuaValue.valueOf(getY()));
			return temp.unpack();
		}
	}
	private class GetSize extends VarArgFunction{
		@Override
		public Varargs invoke(Varargs args) {
			LuaTable temp = new LuaTable();
			temp.set(1, LuaValue.valueOf(getWid()));
			temp.set(2, LuaValue.valueOf(getHei()));
			return temp.unpack();
		}
	}
	private class GetShade extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			return shade.toLuaValue();
		}
	}
	
	public static LuaTable getDefaultColorScheme(){
		LuaTable buttonColors = new LuaTable();
		buttonColors.set("frame", new Color(255, 255, 255).toLuaValue());
		buttonColors.set("fill", new Color(100, 100, 100).toLuaValue());
		buttonColors.set("shade", new Color(0, 0, 0, 0).toLuaValue());
		//buttonColors.set("text", new Color(255, 255, 255).toLuaValue());
		return buttonColors;
	}
	//public int opacity = 255;
	@Override
	public void onDraw(Gui gui, int mouseX, int mouseY, float partialTicks) {
		//scale(GuiAnimation.map(scaleAnimation.doInterpolate(), 0, 1, 1, 1.2)); //map p from [0,1] to [1,1.5]
		//doOpacity();
		if(!isVisible)return;
		//System.out.println(frame);
		//System.out.println(fill);
		if(doAnimation){
			scale(scale);
			gui.drawBoxedRectangle(drawX, drawY, drawWid, drawHei, frame.toInt(), fill.toInt());
		}else{
			gui.drawBoxedRectangle(x, y, wid, hei, frame.toInt(), fill.toInt());
		}
	}
	@Override
	public void setPos(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public void setVisible(boolean b) {
		isVisible = b;
//		if(b){
//			fade.setReverse(false);
//		}else{
//			fade.setReverse(true);
//		}
//		fade.start();
	}
	@Override
	public int getItemHeight() {
		return hei;
	}
	@Override
	public int getItemWidth() {
		return wid;
	}
//	public void doOpacity() {
//		opacity = (int) (fade.doInterpolate()*255);
//	}
	@Override
	public void setWidth(int newWidth) {
		this.wid = newWidth;
	}
	@Override
	public void setHeight(int newHei) {
		hei = newHei;
	}
	public Color getFrame() {
		return frame;
	}
	public Color getFill() {
		return fill;
	}
}