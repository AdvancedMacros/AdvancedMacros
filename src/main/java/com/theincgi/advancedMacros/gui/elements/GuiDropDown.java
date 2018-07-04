package com.theincgi.advancedMacros.gui.elements;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;

import com.theincgi.advancedMacros.gui.Color;
import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.gui.Gui.InputSubscriber;
import com.theincgi.advancedMacros.misc.Property;
import com.theincgi.advancedMacros.misc.PropertyPalette;
import com.theincgi.advancedMacros.misc.Settings;
import com.theincgi.advancedMacros.misc.Utils;

public class GuiDropDown implements Drawable, InputSubscriber, Moveable{
	ListManager listManager;
	GuiRect txtBG/*, listBG*/;
	GuiButton dropButton;
	String dispText = "";
	int index = 0;
	//Property colorText;
	
	
	
	private static final String defaultTableName = "colors.standardDropDownBox";
	private WidgetID wID;
	private int x,y,width,height,maxHeight;
	String nullOrPropTableName;
	private OnClickHandler och;
	
	PropertyPalette propertyPalette;
	PropertyPalette optionPalette;
	
	
	public GuiDropDown(int x, int y, int width, int height, int maxHeight, String...propPath) {
		this(x, y, width, height, maxHeight, propPath.length==0? new PropertyPalette() : new PropertyPalette(propPath, Settings.settings));
	}
	public GuiDropDown(int x, int y, int width, int height, int maxHeight, LuaTable propHome, String...propPath) {
		this(x, y, width, height, maxHeight, propPath.length==0? new PropertyPalette() : new PropertyPalette(propPath, propHome));
	}
	
	public GuiDropDown(int x, int y, int width, int height, int maxHeight, PropertyPalette propertyPalette) {
		this.propertyPalette = propertyPalette;
		this.optionPalette = propertyPalette.propertyPaletteOf("options");
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.maxHeight = maxHeight;
		
		dropButton = new GuiButton(x+width-height, 	y, 
				height, 		height, 
				Settings.getTextureID("resource:whiteDownTri.png"), LuaValue.NIL, propertyPalette.propertyPaletteOf("dropButton"));
		txtBG = new GuiRect(x, y, width-dropButton.getWid(), height, propertyPalette.propertyPaletteOf("textBackground"));
		propertyPalette.addColorIfNil(Color.WHITE, "colors","text");
//	}
//	public GuiDropDown(WidgetID wID, int x, int y, int width, int height, int maxHeight, String nullOrPropTableName) {
//		this.wID = wID;
//		this.x = x; this.y = y;
//		this.width = width; this.height = height;
//		this.maxHeight = maxHeight;
//		this.nullOrPropTableName = nullOrPropTableName = nullOrPropTableName==null?defaultTableName:nullOrPropTableName;
//		
//		
//		dropButton = new GuiButton(wID, 
//				x+width-height, 	y, 
//				height, 		height, 
//				Settings.getTextureID("resource:whiteDownTri.png"), LuaValue.NIL, //"resource:whiteDownTri.png" 
//				nullOrPropTableName, 
//				Color.BLACK, Color.WHITE, Color.WHITE);
//		//dropButton = new GuiButton()
//		txtBG = new GuiRect(wID, x, y, width-dropButton.getWid(), height, nullOrPropTableName, Color.BLACK, Color.WHITE);
		listManager = new ListManager(x, y+height, width, maxHeight-height, /*wID, nullOrPropTableName*/ propertyPalette);
		listManager.scrollBar.setWid(height);
		//listBG = new GuiRect(wID, listManager.getX(), listManager.getY(), listManager.getItemWidth(), listManager.getItemHeight(), null, Color.BLACK, Color.WHITE);
		//colorText = new Property(nullOrPropTableName+".textColor", Color.WHITE.toLuaValue(), "textColor", wID);
		
		och = new OnClickHandler() {
			@Override
			public void onClick(int button, GuiButton sButton) {
				index = listManager.find(sButton);
				dispText = sButton.getText();
				close();
				if(onSelect!=null)
					onSelect.onSelect(index, dispText);
			}
		};
		
		dropButton.setOnClick(new OnClickHandler() {
			@Override
			public void onClick(int button, GuiButton sButton) {
				open();
			}
		});
		listManager.setVisible(false);
		listManager.setSpacing(0);
		dropButton.doAnimation = false;
		addOption("");
	}
	
	
	
	public void addOption(String s){
		//-listManager.scrollBar.getItemWidth()
//		GuiButton b = new GuiButton(wID, x, y, txtBG.getWid(), height, LuaValue.NIL, LuaValue.NIL, nullOrPropTableName, Color.BLACK, Color.WHITE, Color.WHITE);
		GuiButton b = new GuiButton(x, y, txtBG.getWid(), height, LuaValue.NIL, LuaValue.NIL, optionPalette);
		b.changeTexture(null);
		b.setScaleTo(1.05);
		listManager.add(b);
		b.setOnClick(och);
		b.setText(s);
	}
	public GuiButton makeOption(String s){
//		GuiButton b = new GuiButton(wID, x, y, txtBG.getWid(), height, LuaValue.NIL, LuaValue.NIL, nullOrPropTableName, Color.BLACK, Color.WHITE, Color.WHITE);
		GuiButton b = new GuiButton(x, y, txtBG.getWid(), height, LuaValue.NIL, LuaValue.NIL, optionPalette);
		b.changeTexture(null);
		b.setScaleTo(1.05);
		b.setOnClick(och);
		b.setText(s);
		return b;
	}
	public void removeOption(int opt){
		listManager.remove(opt);
	}
	public void clear(boolean addEmpty){
		listManager.clear();
		if(addEmpty){
			addOption("");
		}
	}
	public String getText(int index){
		Moveable m = listManager.getItem(index);
		if(m instanceof GuiButton){
			return ((GuiButton) m).getText();
		}
		return "";
	}
	
//	Runnable onChoice;
//	public void setOnChoice(Runnable onChoice) {
//		this.onChoice = onChoice;
//	}
	
	@Override
	public void setPos(int x, int y) {
		this.x = x;
		this.y = y;
		txtBG.move(x, y);
		dropButton.move(txtBG.getX()+txtBG.getWid(), y);
		listManager.setPos(x, y+txtBG.getHei());
		//System.out.println("SET "+x);
	}
	private boolean isVisible = true;
	@Override
	public void setVisible(boolean b) {
		txtBG.setVisible(b);
		dropButton.setVisible(b);
		isVisible = b;
	}
	public boolean isVisible() {
		return isVisible;
	}
	@Override
	public int getItemHeight() {
		return txtBG.getHei();
	}
	@Override
	public int getItemWidth() {
		return txtBG.getWid() + dropButton.getWid();
	}
	
	private OnSelectHandler onSelect;
	public void setOnSelect(OnSelectHandler oSel){
		onSelect = oSel;
	}
	
	public boolean isInBounds(int x, int y){
		if(txtBG.getX()<=x && x <= dropButton.getX()+dropButton.getWid() && txtBG.getY() <= y){
			if(listManager.isVisible()){
				return y <= listManager.getY()+listManager.getItemHeight();
			}else{
				return y <= txtBG.getY()+txtBG.getHei();
			}
		}
		return false;
	}
	
	@Override
	public void onDraw(Gui gui, int mouseX, int mouseY, float partialTicks) {
		if(!isVisible)return;
		//if(gui.height< listManager.getY()+listManager.getTotalHeight()){
			//listManager.setPos(x, x-Math.min(listManager.getTotalHeight(), maxHeight));
		//}else{
		listManager.setPos(x, y+height);
		//}
		//System.out.println(x);
		txtBG.setPos(x, y);
		txtBG.setWidth(width);
		txtBG.onDraw(gui, mouseX, mouseY, partialTicks);
		if(listManager.isVisible()){
			gui.drawLast=drawTop;
		}
		dropButton.setPos(x+width-dropButton.getWid(), y);
		dropButton.onDraw(gui, mouseX, mouseY, partialTicks);
		if(dispText!=null)
			gui.drawCenteredString(gui.getFontRend(), dispText, txtBG.getX()+txtBG.getWid()/2, txtBG.getY()+txtBG.getHei()/2, propertyPalette.getColor("colors","text").toInt());
		
	}
	
	public String getSelection(){
		return dispText;
	}
	
	private Drawable drawTop=new Drawable() {
		@Override
		public void onDraw(Gui gui, int mouseX, int mouseY, float partialTicks) {
			if(listManager.isVisible()){
				gui.drawWorldBackground(10);
//				listBG.setPos(x, listManager.getY());
//				listBG.setWidth(listManager.getItemWidth()-listManager.scrollBar.getItemHeight());
//				listBG.setHeight(listManager.getTotalHeight());
//				listBG.onDraw(gui, mouseX, mouseY, partialTicks);
				//System.out.printf("MaxHeight: %s, Height: %s = %s\n", maxHeight, height, maxHeight-height);
				//listManager.setHeight(maxHeight-height); 
				
				listManager.onDraw(gui, mouseX, mouseY, partialTicks);
			}
		}
	};
	
	@Override
	public boolean onScroll(Gui gui, int i) {
		//if(dropButton.onScroll(gui, i)){return true;}
		if(listManager.isVisible()){
			if(listManager.onScroll(gui, i))return true;
		}
		return false;
	}
	@Override
	public boolean onMouseClick(Gui gui, int x, int y, int buttonNum) {
		if(dropButton.onMouseClick(gui, x, y, buttonNum)){return true;}
		if(listManager.isVisible()){
			if(listManager.onMouseClick(gui, x, y, buttonNum))return true;
		}
		return false;
	}
	@Override
	public boolean onMouseRelease(Gui gui, int x, int y, int state) {
		if(dropButton.onMouseRelease(gui, x, y, state)){return true;}
		if(listManager.isVisible()){
			if(listManager.onMouseRelease(gui, x, y, state))return true;
		}
		return false;
	}
	@Override
	public boolean onMouseClickMove(Gui gui, int x, int y, int buttonNum, long timeSinceClick) {
		if(dropButton.onMouseClickMove(gui, x, y, buttonNum, timeSinceClick)){return true;}
		if(listManager.isVisible()){
			if(listManager.onMouseClickMove(gui, x, y, buttonNum, timeSinceClick))return true;
		}
		return false;
	}
	@Override
	public boolean onKeyPressed(Gui gui, char typedChar, int keyCode) {
		if(dropButton.onKeyPressed(gui, typedChar, keyCode)){return true;}
		if(listManager.isVisible()){
			if(listManager.onKeyPressed(gui, typedChar, keyCode))return true;
		}
		return false;
	}
	@Override
	public boolean onKeyRepeat(Gui gui, char typedChar, int keyCode, int repeatMod) {
		if(dropButton.onKeyRepeat(gui, typedChar, keyCode, repeatMod)){return true;}
		if(listManager.isVisible()){
			if(listManager.onKeyRepeat(gui, typedChar, keyCode, repeatMod))return true;
		}
		return false;
	}
	@Override
	public boolean onKeyRelease(Gui gui, char typedChar, int keyCode) {
		if(dropButton.onKeyRelease(gui, typedChar, keyCode)){return true;}
		if(listManager.isVisible()){
			if(listManager.onKeyRelease(gui, typedChar, keyCode))return true;
		}
		return false;
	}
	
	public static abstract class OnSelectHandler{
		abstract public void onSelect(int index, String text);
	}

	@Override
	public void setWidth(int i) {
		this.width = i;
		txtBG.setWidth(i-dropButton.getWid());
		listManager.setWidth(i);
	}

	
	public void open(){
		listManager.setVisible(true);
		listManager.scrollBar.focusToItem(index);
	}
	public void close(){
		listManager.setVisible(false);
	}
	public boolean isOpen(){
		return listManager.isVisible();
	}
	
	@Override
	public void setHeight(int i) {
		this.height = i;
	}
	
	/**be sure to set some code to call open and close*/
	public void setOnOpen(OnClickHandler och) {
		dropButton.setOnClick(och);
	}
	public OnClickHandler getonOpen(){
		return dropButton.getOnClickHandler();
	}

	
	public void setMaxHeight(int maxHeight) {
		this.maxHeight = maxHeight;
		//System.out.printf("Max height set to %s (boxes = %s)\n", (maxHeight-height), (maxHeight-height)/12);
		listManager.setHeight(maxHeight-height);
		//listManager.setHeight(300);
	}

	@Override
	public int getX() {
		return x;
	}



	@Override
	public int getY() {
		return y;
	}



	public void setScrollSpeed(int i) {
		listManager.scrollBar.setScrollMagnifier(i);
	}

	
	//TODO select by int
	
	public void select(String string) {
		for(int i = 0; i<listManager.getItems().size(); i++){
			if(getText(i).equals(string)){
				dispText = string;
				return;
			}
		}
		System.out.println("Drop down "+string+" not found");
		for(int i = 0; i<listManager.getItems().size();i++){
			System.out.println(getText(i));
		}
	}
	
}