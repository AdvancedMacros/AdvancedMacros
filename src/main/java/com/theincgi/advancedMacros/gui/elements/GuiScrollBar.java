package com.theincgi.advancedMacros.gui.elements;

import com.theincgi.advancedMacros.gui.Color;
import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.gui.Gui.Focusable;
import com.theincgi.advancedMacros.gui.Gui.InputSubscriber;
import com.theincgi.advancedMacros.misc.Property;
import com.theincgi.advancedMacros.misc.PropertyPalette;
import com.theincgi.advancedMacros.misc.Settings;
import com.theincgi.advancedMacros.misc.Utils;

public class GuiScrollBar implements Drawable, InputSubscriber, Focusable, Moveable{
	//boolean prop keep scroll, for exit and return to menu
	//Property scrollbarFrame, scrollbarBG, scrollBarButtonFrame, scrollBarButtonFill, scrollBarButtonDetail, scrollBarButtonShade;

	private int items=2;
	private int visible=1;
	private double pos;
	private Orientation orientation;
	private WidgetID wID;
	private int x,y,wid,len;
	public boolean needsFocus = false;
	private boolean isVisible=true;
	private double scrollSpeed = 1;
	
	PropertyPalette propertyPalette;
	
	public GuiScrollBar(int x, int y, int wid, int len, Orientation or, String...propPath) {
		this(x, y, wid, len, or, propPath.length==0 ? new PropertyPalette() : new PropertyPalette(propPath));
	}
	
	public GuiScrollBar(int x, int y, int wid, int len, Orientation or, PropertyPalette propPal) {
		this.propertyPalette = propPal;
		this.x = x;
		this.y = y;
		this.wid = wid;
		this.len = len;
		this.orientation = or;
		propPal.addColorIfNil(new Color(100, 	100, 	255), "colors", "bgFrame");
		propPal.addColorIfNil(new Color(142, 	142, 	142), "colors", "bgFill");
		propPal.addColorIfNil(new Color(220,	220,	220), "colors", "buttonFrame");
		propPal.addColorIfNil(new Color(204, 	223, 	255), "colors", "buttonFill");
		propPal.addColorIfNil(new Color(  0, 	 97, 	255), "colors", "buttonDetail");
		propPal.addColorIfNil(GuiRect.DEFAULT_SHADE,          "colors", "buttonShade");
	}
	
//	public GuiScrollBar(WidgetID wID, int x, int y, int wid, int len, Orientation or) {
//		this.wID = wID;
//		this.x = x;
//		this.y = y;
//		this.wid = wid;
//		this.len = len;
//		this.orientation = or;
////		scrollbarFrame 			= new Property("colors.standardScrollbar.bgFrame", 		new Color(100, 	100, 	255).toLuaValue(), 	"bgFrame", 		wID);
////		scrollbarBG    			= new Property("colors.standardScrollbar.bgFill",  		new Color(142, 	142, 	142).toLuaValue(), 	"bgFill",		wID);
////		scrollBarButtonFrame 	= new Property("colors.standardScrollbar.buttonFrame", 	new Color(220,	220,	220).toLuaValue(), 	"buttonFrame",	wID);
////		scrollBarButtonFill 	= new Property("colors.standardScrollbar.buttonFill", 	new Color(204, 	223, 	255).toLuaValue(), 	"buttonFill",	wID);
////		scrollBarButtonDetail 	= new Property("colors.standardScrollbar.buttonDetail", new Color(0, 	97, 	255).toLuaValue(), 	"buttonDetail", wID);
////		scrollBarButtonShade    = new Property("colors.standardScrollbar.buttonShade",  GuiRect.DEFAULT_SHADE.toLuaValue(), "buttonShade", wID);
//
//	}
//	/**@param defPropPointerPrefix used to change table for color property defaults<br>
//	 * param = "colors.dropDownBox" will put buttonFill in "colors.dropDownBox.buttonFill,<br>
//	 * use this constructor if it is an element of something else*/
//	public GuiScrollBar(WidgetID wID, int x, int y, int wid, int len, Orientation or, String defPropPointerPrefix) {
//		defPropPointerPrefix = defPropPointerPrefix==null?"colors.standardScrollbar":defPropPointerPrefix;
//		this.wID = wID;
//		this.x = x;
//		this.y = y;
//		this.wid = wid;
//		this.len = len;
//		this.orientation = or;
//		scrollbarFrame 			= new Property(defPropPointerPrefix+".bgFrame", 		new Color(100, 	100, 	255).toLuaValue(), 	"bgFrame", 		wID);
//		scrollbarBG    			= new Property(defPropPointerPrefix+".bgFill",  		new Color(142, 	142, 	142).toLuaValue(), 	"bgFill",		wID);
//		scrollBarButtonFrame 	= new Property(defPropPointerPrefix+".buttonFrame", 	new Color(220,	220,	220).toLuaValue(), 	"buttonFrame",	wID);
//		scrollBarButtonFill 	= new Property(defPropPointerPrefix+".buttonFill", 		new Color(204, 	223, 	255).toLuaValue(), 	"buttonFill",	wID);
//		scrollBarButtonDetail 	= new Property(defPropPointerPrefix+".buttonDetail", 	new Color(0, 	97, 	255).toLuaValue(), 	"buttonDetail", wID);
//		scrollBarButtonShade    = new Property(defPropPointerPrefix+".buttonShade",  	GuiRect.DEFAULT_SHADE.toLuaValue(), 		"buttonShade", 	wID);
//
//	}
	

	public void setItemCount(int items){
		this.items = items;
		setPos(getOffset());
	}
	public void setVisibleItems(int vItems){
		this.visible = vItems;
		setPos(getOffset());
	}
	/**This is the scrollbar position*/
	public double getOffset(){
		return pos;
	}


	public static enum Orientation{
		UPDOWN,
		LEFTRIGHT;
		public boolean isUPDOWN(){
			return this.equals(Orientation.UPDOWN);
		}
		public boolean isLEFTRIGHT(){
			return this.equals(Orientation.LEFTRIGHT);
		}
	}


	@Override
	public void onDraw(Gui gui, int mouseX, int mouseY, float partialTicks) {
		if(!isVisible){return;}
		int barFrame =   propertyPalette.getColor("colors", "bgFrame").toInt(),
		    barBG    =   propertyPalette.getColor("colors", "bgFill").toInt(),
		    buttonFrame= propertyPalette.getColor("colors", "buttonFrame").toInt(),
		    buttonFill = propertyPalette.getColor("colors", "buttonFill").toInt(),
		    buttonDetail=propertyPalette.getColor("colors", "buttonDetail").toInt(),
		    buttonShade =propertyPalette.getColor("colors", "buttonShade").toInt();
		
		if(orientation.isUPDOWN()){
			gui.drawBoxedRectangle(x, y, wid, len, barFrame, barBG);
			int buttonLen = getButtonLen()-1;
			int buttonY =   getButtonY()+1;
			gui.drawBoxedRectangle(x+1, buttonY, wid-2, buttonLen, buttonFrame, buttonFill);
			gui.drawHorizontalLine(x+4, x+wid-4, buttonY+buttonLen/2, buttonDetail);
			gui.drawHorizontalLine(x+4, x+wid-4, buttonY+buttonLen/2-2, buttonDetail);
			gui.drawHorizontalLine(x+4, x+wid-4, buttonY+buttonLen/2+2, buttonDetail);
			//System.out.println(">>> "+x+" "+y+" "+wid+" "+len+" "+buttonLen+" "+buttonY);

			if(anchorSet || isInButton(mouseX, mouseY)){
				net.minecraft.client.gui.Gui.drawRect(x+1, buttonY, wid+x, buttonLen+buttonY+1, buttonShade);
			}
		}else if(orientation.isLEFTRIGHT()){
			gui.drawBoxedRectangle(x, y, len, wid, barFrame, barBG);
			
			gui.drawBoxedRectangle(getButtonX(), y+1, getButtonLen()-1, wid-2, buttonFrame, buttonFill);
			
			gui.drawVerticalLine(getButtonX()+getButtonLen()/2, y+2, y+wid-2, buttonDetail);
			gui.drawVerticalLine(getButtonX()+getButtonLen()/2-2, y+2, y+wid-2, buttonDetail);
			gui.drawVerticalLine(getButtonX()+getButtonLen()/2+2, y+2, y+wid-2, buttonDetail);
			
			
			if(anchorSet || isInButton(mouseX, mouseY)){
				net.minecraft.client.gui.Gui.drawRect(getButtonX(),y+1,getButtonX()+getButtonLen(), y+wid, buttonShade);
			}
		}
		//		gui.drawBoxedRectangle(x, y, 
		//				orientation.isLEFTRIGHT()?len:wid,
		//				orientation.isLEFTRIGHT()?wid:len,
		//				Utils.parseColor(scrollbarFrame.getPropValue()).toInt(), 
		//				Utils.parseColor(scrollbarBG.getPropValue()).toInt());
		//		gui.drawBoxedRectangle(x+1+(orientation.isLEFTRIGHT()?buttonOffset():0), 
		//							   y+1+(orientation.isLEFTRIGHT()?0:buttonOffset()), 
		//							   orientation.isLEFTRIGHT()?len-2:wid-2,
		//							   orientation.isLEFTRIGHT()?wid-2:len-2,
		//							   scrollBarButtonFrame.getPropValue().toint(), 
		//							   scrollBarButtonFill.getPropValue().toint());
		//		if(orientation.isLEFTRIGHT()){
		//			gui.drawVerticalLine(x+1+buttonOffset()+len/2+2, y+2, y-2+wid, scrollBarButtonDetail.getPropValue().toint());
		//			gui.drawVerticalLine(x+1+buttonOffset()+len/2, y+2, y-2+wid, scrollBarButtonDetail.getPropValue().toint());
		//			gui.drawVerticalLine(x+1+buttonOffset()+len/2-2, y+2, y-2+wid, scrollBarButtonDetail.getPropValue().toint());
		//		}else{
		//			gui.drawHorizontalLine(x+2,		x-2+wid,	y+buttonOffset()+len/2+2,   scrollBarButtonDetail.getPropValue().toint());
		//			gui.drawHorizontalLine(x+2,		x-2+wid,	y+buttonOffset()+len/2,   scrollBarButtonDetail.getPropValue().toint());
		//			gui.drawHorizontalLine(x+2,		x-2+wid,	y+buttonOffset()+len/2-2,   scrollBarButtonDetail.getPropValue().toint());
		//		}
	}

	private double percentVisible(){
		return visible/(double)items;
	}
	private int buttonOffset(){
		return (int) (len*(pos/items));
	}

	
	double scrollMagnifier = 1;
	public void setScrollMagnifier(double scrollMagnifier) {
		this.scrollMagnifier = scrollMagnifier;
	}
	@Override
	public boolean onScroll(Gui gui, int sign) {
		double lastPos = getOffset();
		double i = sign*scrollSpeed;
		if(visible<items){
			setPos(pos - i*scrollMagnifier);
			return lastPos!=getOffset();
		}
		return false;
	}

	private boolean isInButton(int mouseX, int mouseY){
		if(orientation.isUPDOWN()){
			int buttonY = getButtonY(), buttonLen = getButtonLen();
			return (mouseX >= x+1 && 
					x-1+wid >= mouseX && 
					mouseY >= buttonY && 
					buttonY + buttonLen >=mouseY);
		}
		return mouseX >= getButtonX() && mouseX <= getButtonX()+getButtonLen() && mouseY >= y+1 && mouseY+1 <= y+wid-1;
	}
	private int getButtonLen(){
		if(items<visible){return len-2;}
		return (int) ((len-2)/(double)items * visible);
	}
	private int getButtonY(){
		return y+(int) (len*pos/items) +1;
	}
	private int getButtonX(){
		return x+(int) (len*pos/items) +1;
	}

	private int anchorX=0, anchorY=0;
	private double anchorPos=0;
	private boolean anchorSet = false;
	@Override
	public boolean onMouseClick(Gui gui, int x, int y, int buttonNum) {
		if(!isInButton(x, y)) return false;
		//System.out.println("Scrollbar: "+items+" v "+visible);
		if(anchorSet){System.out.println("double anchor set"); return true;}
		if(buttonNum == 0){
			anchorX = x;
			anchorY = y;
			anchorPos = pos;
			anchorSet = true;
			gui.setFocusItem(this);
			return true;
		}
		return false;
	}
	public void resetAnchors(){
		anchorSet = false;
	}
	
	public boolean isFocused(Gui gui){
		return gui.getFocusItem().equals(this);
	}

	@Override
	public boolean onMouseRelease(Gui gui, int x, int y, int state) {
		if(anchorSet){
			double pxlsMoved;
			if(orientation.isUPDOWN()){
				setPos(anchorPos + (y-anchorY)/(double)len * (items));
			}else{
				setPos(anchorPos + (x-anchorX)/(double)len * (items));
			}
			resetAnchors();
			return true;
		}
		return false;
	}

	/**capped from 0 to items-vis-1*/
	private void setPos(double pos){
		if(items<=visible){this.pos = 0; return;}
		this.pos = Math.min(items-visible, Math.max(0, pos));
	}
	/**This should totes work*/
	public void focusToItem(int i){
		setPos(i-visible/2);
	}

	@Override
	public boolean onMouseClickMove(Gui gui, int x, int y, int buttonNum, long timeSinceClick) {
		if(anchorSet){
			if(orientation.isUPDOWN()){
				setPos(anchorPos + (y-anchorY)/(double)len * items);
			}else{
				setPos(anchorPos + (x-anchorX)/(double)len * (items));
			}
			return true;
		}
		return false;
	}



	@Override
	public boolean onKeyPressed(Gui gui, char typedChar, int keyCode) {
		return false;
	}



	@Override
	public boolean onKeyRepeat(Gui gui, char typedChar, int keyCode, int repeatMod) {
		return false;
	}



	@Override
	public boolean onKeyRelease(Gui gui, char typedChar, int keyCode) {
		return false;
	}

	Runnable r;

	
	public void setOnChange(Runnable r){
		this.r = r;
	}


	/**x,y,wid,len*/
	public void reposition(int x2, int i, int j, int hei) {
		x = x2;
		y = i;
		wid = j;
		len = hei;
	}
	@Override
	public void setPos(int x, int y) {
		this.x = x;
		this.y = y;
	}
	@Override
	public void setVisible(boolean b) {
		isVisible = b;
	}
	@Override
	public int getItemHeight() {
		return orientation.isUPDOWN()?len : wid;
	}
	@Override
	public int getItemWidth() {
		return orientation.isUPDOWN()?wid : len;
	}
	public void resetPos() {
		setPos(0);
	}
	public void setWid(int newWid) {
		this.wid = newWid;
	}
	public void setLen(int newLen){
		this.len = newLen;
	}
	@Override
	public void setWidth(int i) {
		if(orientation.isUPDOWN()){
			setWid(i);
		}else{
			setLen(i);
		}
	}
	@Override
	public void setHeight(int i) {
		if(orientation.isUPDOWN()){
			setLen(i);
		}else{
			setWid(i);
		}
	}
	@Override
	public int getX() {
		return x;
	}
	@Override
	public int getY() {
		return y;
	}
	public int getVisibleItems() {
		return visible;
	}
	public int getItems() {
		return items;
	}
	
	
	public void setScrollSpeed(double speed) {
		scrollSpeed = speed;
	}
	
	/**Always focused*/
	@Override
	public boolean isFocused() {
		return true;
	}
	/**Always focused*/
	@Override
	public void setFocused(boolean unused) {
		
	}
}