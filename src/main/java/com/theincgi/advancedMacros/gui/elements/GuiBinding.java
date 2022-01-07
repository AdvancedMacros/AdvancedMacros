package com.theincgi.advancedMacros.gui.elements;

import java.io.File;
import java.util.LinkedList;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.event.ForgeEventHandler;
import com.theincgi.advancedMacros.event.ForgeEventHandler.EventName;
import com.theincgi.advancedMacros.gui.Color;
import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.gui.Gui.InputSubscriber;
import com.theincgi.advancedMacros.gui.IBindingsGui.IBinding;
import com.theincgi.advancedMacros.gui.MacroMenuGui;
import com.theincgi.advancedMacros.gui.elements.GuiDropDown.OnSelectHandler;
import com.theincgi.advancedMacros.gui2.PopupPrompt2.Result;
import com.theincgi.advancedMacros.gui2.PopupPrompt2.ResultHandler;
import com.theincgi.advancedMacros.misc.HIDUtils.Keyboard;
import com.theincgi.advancedMacros.misc.HIDUtils.Mouse;
import com.theincgi.advancedMacros.misc.Settings;
import com.theincgi.advancedMacros.misc.Utils;

public class GuiBinding implements Moveable, Drawable, InputSubscriber, IBinding{
	//[enable] [key/event] [detail]  [script] [edit] [move:up/down] [remove]
	//move by drag
	//needs a drag grid tool
	//shift up if y becomes greater n down if it becomes less than mouse n all that
	//do with animation
	//	private static Property enableTexture = new Property("", defaultPropValue, propName, wID),
	//							disableTexture,
	//							keyEventTexture,
	//							gameEventTexture,;
	//static Property enableTexProp;
	//TODO allow property for textures, note use string to change the value for load tex id
	static LuaValue enableTexture = Settings.getTextureID("resource:script_enabled.png"),
			disableTexture = Settings.getTextureID("resource:script_disabled.png"),
			allKeyEventTexture = Settings.getTextureID("resource:allkeyevent.png"),
			keyDownEventTexture = Settings.getTextureID("resource:keydownevent.png"),
			keyUpEventTexture = Settings.getTextureID("resource:keyupevent.png"),
			gameEventTexture = Settings.getTextureID("resource:gameevent.png"),
			trashTexture = Settings.getTextureID("resource:remove.png"),
			editTexture = Settings.getTextureID("resource:whiteedit.png"),
			moveTexture = Settings.getTextureID("resource:whitemove.png");

	private Gui gui;
	
	GuiButton enableButton, modeButton, editButton, moveButton, removeButton;
	GuiDropDown eventSelector/*, scriptSelector*/;
	String script;
	GuiButton pickScript;
	
	private boolean isVisible = false;
	private boolean enable = true;
	//private boolean isKey = true;
	private EventMode eventMode = EventMode.KEY_DOWN;
	private int sWid;
	private int x,y;
	public GuiBinding(WidgetID wID, int x, int y, int guiWid, final ListManager container, final Gui gui) {
		this.gui = gui;
		this.x = x; this.y = y;
		sWid = guiWid;
		
		//enableTexProp = new Property("texture.binding.enabled", LuaValue.valueOf("resource:greencheck.png"), "enabled", wID);
		
		//removeButton = new GuiButton(wID, x, y, 12, 12, LuaValue.NIL, LuaValue.NIL, "colors.binding", Color.BLACK, Color.WHITE, Color.WHITE);
		removeButton = new GuiButton(x, y, 12, 12, LuaValue.NIL, LuaValue.NIL, "bindings", "removeButton");
		//enableButton = new GuiButton(wID, x, y, 12, 12, LuaValue.NIL, LuaValue.NIL, "colors.binding", Color.BLACK, Color.WHITE, Color.WHITE);
		enableButton = new GuiButton(x, y, 12, 12, LuaValue.NIL, LuaValue.NIL, "bindings", "enableButton");
		//editButton   = new GuiButton(wID, x, y, 12*3, 12, LuaValue.NIL, LuaValue.NIL, "colors.binding", Color.BLACK, Color.WHITE, Color.WHITE);
		editButton   = new GuiButton(x, y, 12*3, 12, LuaValue.NIL, LuaValue.NIL, "bindings", "editButton");
		//modeButton   = new GuiButton(wID, x, y, 12, 12, LuaValue.NIL, LuaValue.NIL, "colors.binding", Color.BLACK, Color.WHITE, Color.WHITE);
		modeButton   = new GuiButton(x, y, 12, 12, LuaValue.NIL, LuaValue.NIL, "bindings", "modeButton");
		//moveButton   = new GuiButton(wID, x, y, 12, 12, LuaValue.NIL, LuaValue.NIL, "colors.binding", Color.BLACK, Color.WHITE, Color.WHITE);
		moveButton   = new GuiButton(x, y, 12, 12, LuaValue.NIL, LuaValue.NIL, "bindings", "moveButton");
		
		removeButton.setEnabled(true);

		removeButton.changeTexture(Utils.checkTexture(trashTexture));
		editButton.changeTexture(Utils.checkTexture(editTexture));
		moveButton.changeTexture(Utils.checkTexture(moveTexture));
		//		removeButton.doScaleAnimation =false;
		//		enableButton.doScaleAnimation =false;
		//		editButton.doScaleAnimation = false;
		//		modeButton.doScaleAnimation = false;
		//		moveButton.doScaleAnimation = false;
		int remaining = guiWid - 12*7;
		//eventSelector = new GuiDropDown(wID, x, y, remaining/3, 12, 12*5, "colors.binding.event");
		eventSelector = new GuiDropDown(x, y, remaining/3, 12, 12*5, "bindings","eventSelector");
		remaining/=2;
//		scriptSelector = new GuiDropDown(wID, x, y, remaining, 12, 12*5, "colors.binding.script");
		//pickScript     = new GuiButton(new WidgetID(77), x, y, 12, 12, Settings.getTextureID("resource:whitedowntri.png"), LuaValue.NIL, "colors.binding", Color.BLACK, Color.WHITE, Color.WHITE);
		pickScript     = new GuiButton(x, y, 12, 12, Settings.getTextureID("resource:whitedowntri.png"), LuaValue.NIL, "bindings", "pickScript");
		
		
		eventSelector.setScrollSpeed(6);
		//scriptSelector.setScrollSpeed(6);
		
		setWidth(sWid);
		setPos(x, y);
		OnClickHandler och = new OnClickHandler() {
			private long lastTrash = 0;
			@Override
			public void onClick(int button, GuiButton sButton) {
				if(sButton.equals(removeButton)){
					//TODO gui->addPopup->new Confirmation msg
					//if yes do action
					if(gui instanceof MacroMenuGui) {
						((MacroMenuGui)gui).removeBinding(GuiBinding.this);
						
					}
				}else if(sButton.equals(enableButton)){
					enable = !enable;
					updateEnableButton();
					((MacroMenuGui)gui).markDirty();
				}else if(sButton.equals(editButton)){
					if(script!=null && !script.isEmpty()) {
						AdvancedMacros.editorGUI.updateKeywords();
						AdvancedMacros.editorGUI.openScript(script);
						((MacroMenuGui)gui).updateProfileChanges();
						ForgeEventHandler.showMenu(AdvancedMacros.editorGUI, AdvancedMacros.macroMenuGui.getGui());
					}
				}else if(sButton.equals(moveButton)){
					container.grab(GuiBinding.this);
					((MacroMenuGui)gui).markDirty();
				}else if(sButton.equals(modeButton)){
					eventMode = eventMode.cycleNext();
					((MacroMenuGui)gui).markDirty();
					updateModeButton();
					eventSelector.dispText="";
				}

			}
		};

		removeButton.setOnClick(och);
		enableButton.setOnClick(och);
		editButton.setOnClick(och);
		modeButton.setOnClick(och); //TODO make it so changing the mode closes the eventSelector
		moveButton.setOnClick(och);
		eventSelector.setOnSelect(
				new OnSelectHandler() {
					@Override
					public void onSelect(int index, String text) {
						((MacroMenuGui)gui).markDirty();
					}
				}
		);
		eventSelector.setOnOpen(new OnClickHandler() {
			@Override
			public void onClick(int button, GuiButton sButton) {
				//if(scriptSelector.isOpen()){return;}
				if(eventMode.isKeyType()){
					if(gui.nextKeyListen==null){
						eventSelector.dispText = "-Press Key-";
						gui.nextKeyListen = GuiBinding.this;
					}else{
						gui.nextKeyListen = null;
						eventSelector.dispText = eventSelector.getText(eventSelector.index);
						((MacroMenuGui)gui).markDirty();
					}
				}else{
					if(eventSelector.isOpen()){
						eventSelector.close();
					}else{
						eventSelector.clear(true);
						for (EventName string : ForgeEventHandler.EventName.values()) {
							eventSelector.addOption(string.name());
						}
						
						eventSelector.open();
					}
				}
			}
		});
//		scriptSelector.setOnOpen(new OnClickHandler() {
//			@Override
//			public void onClick(int button, GuiButton sButton) {
//				if(eventSelector.isOpen()){return;}
////				if(scriptSelector.isOpen()){
////					scriptSelector.close();
////				}else{
////					scripts.clear();
////					for(File f : AdvancedMacros.getScriptList()){
////						GuiBinding.addScriptName(f.getName());
////					}
////					scriptSelector.clear(true);
////					for (String string : scripts) {
////						scriptSelector.addOption(string);
////					}
////					scriptSelector.open();
////				}
//				AdvancedMacros.scriptBrowser2.getSelection(GuiBinding.this.gui, (result)->{
//					if(result.isCanceled()) return true;
//					scriptSelector.set
//				});
//			}
//		});
		pickScript.setOnClick((int mb, GuiButton button)->{
			AdvancedMacros.scriptBrowser2.setActivePath(scriptHome(script));
			AdvancedMacros.scriptBrowser2.setSelectedFile(script);
			AdvancedMacros.scriptBrowser2.getSelection(AdvancedMacros.macroMenuGui.getGui(), new ResultHandler() {
				@Override public boolean onResult(Result r) {
					if(r.isCanceled()) return true;
					script = r.getResult();
					((MacroMenuGui)gui).markDirty();
					return true;
			}});
		});
		updateEnableButton();
		updateModeButton();
		//pass events
		//onclick
		//get values for ddb's
		//get enable/disable
		//get mode
	}

	/**
	 * "folder/macro.lua" - > [mods]/advancedMacros/macros/folder
	 * */
	private File scriptHome(String script2) {
		if(script2==null)return AdvancedMacros.macrosFolder;
		if(script2.contains("/")) {
			return new File(AdvancedMacros.macrosFolder, script.substring(0, script2.lastIndexOf('/')));
		}
		return AdvancedMacros.macrosFolder;
	}

	private void updateEnableButton() {
		enableButton.changeTexture(Utils.checkTexture(enable?enableTexture:disableTexture));
	}
	private void updateModeButton(){
		LuaValue sTex=LuaValue.NIL;
		switch (eventMode) {
		case EVENT:
			sTex = gameEventTexture;
			break;
		case KEY_DOWN:
			sTex = keyDownEventTexture;
			break;
		case KEY_UP:
			sTex = keyUpEventTexture;
			break;
		case KEY_ALL:
			sTex = allKeyEventTexture;
			break;
		}
		modeButton.changeTexture(Utils.checkTexture(sTex));
	}


	@Override
	public void setVisible(boolean b) {
		removeButton.setVisible(b/* && ColorTextArea.isCTRLDown()*/);
		enableButton.setVisible(b);
		modeButton.setVisible(b);
		moveButton.setVisible(b);
		editButton.setVisible(b);
		eventSelector.setVisible(b);
		//scriptSelector.setVisible(b);
		pickScript.setVisible(b);
		isVisible = b;
	}
	@Override
	public int getItemHeight() {
		return 12;
	}
	@Override
	public int getItemWidth() {
		return sWid;
	}
	@Override
	public boolean onScroll(Gui gui, double i) {
		//if(scriptSelector.onScroll(gui, i))return true;
		if(eventSelector.onScroll(gui, i))return true;
		return false;
	}
	@Override
	public boolean onMouseClick(Gui gui, double x, double y, int buttonNum) {
		
		if(gui.nextKeyListen!=null){
			switch (buttonNum) {
			case 0:
				eventSelector.dispText = "LMB";
				break;
			case 1:
				eventSelector.dispText = "RMB";
				break;
			case 2:
				eventSelector.dispText = "MMB";
				break;
			default:
				eventSelector.dispText = "MOUSE:"+buttonNum;
				break;
			}
			((MacroMenuGui)gui).markDirty();
			return true;
		}
		
		if(eventSelector.isOpen() && !eventSelector.isInBounds(x, y)){eventSelector.close(); return true;}
		//if(scriptSelector.isOpen() && !scriptSelector.isInBounds(x, y)){scriptSelector.close(); return true;}
		if(pickScript       .onMouseClick(gui, x, y, buttonNum))return true;
		if(removeButton		.onMouseClick(gui, x, y, buttonNum))return true;
		if(enableButton		.onMouseClick(gui, x, y, buttonNum))return true;
		if(modeButton		.onMouseClick(gui, x, y, buttonNum))return true;
		if(eventSelector	.onMouseClick(gui, x, y, buttonNum))return true;
		//if(scriptSelector	.onMouseClick(gui, x, y, buttonNum))return true;
		if(editButton		.onMouseClick(gui, x, y, buttonNum))return true;
		if(moveButton		.onMouseClick(gui, x, y, buttonNum))return true;
		return false;
	}
	@Override
	public boolean onMouseRelease(Gui gui, double x, double y, int state) {
		if(removeButton		.onMouseRelease(gui, x, y, state))return true;
		if(enableButton		.onMouseRelease(gui, x, y, state))return true;
		if(modeButton		.onMouseRelease(gui, x, y, state))return true;
		if(eventSelector	.onMouseRelease(gui, x, y, state))return true;
		//if(scriptSelector	.onMouseRelease(gui, x, y, state))return true;
		if(pickScript		.onMouseRelease(gui, x, y, state))return true;
		if(editButton		.onMouseRelease(gui, x, y, state))return true;
		if(moveButton		.onMouseRelease(gui, x, y, state))return true;
		return false;
	}
	@Override
	public boolean onMouseClickMove(Gui gui, double x, double y, int buttonNum, double q, double r) {
		if(removeButton		.onMouseClickMove(gui, x, y, buttonNum, q, r))return true;
		if(enableButton		.onMouseClickMove(gui, x, y, buttonNum, q, r))return true;
		if(modeButton		.onMouseClickMove(gui, x, y, buttonNum, q, r))return true;
		if(eventSelector	.onMouseClickMove(gui, x, y, buttonNum, q, r))return true;
		if(pickScript		.onMouseClickMove(gui, x, y, buttonNum, q, r))return true;
		if(editButton		.onMouseClickMove(gui, x, y, buttonNum, q, r))return true;
		if(moveButton		.onMouseClickMove(gui, x, y, buttonNum, q, r))return true;
		return false;
	}
	
	@Override
	public boolean onKeyPressed(Gui gui, int keyCode, int scanCode, int modifiers) {
		if(gui.nextKeyListen!=null){
			eventSelector.dispText = Keyboard.nameOf(keyCode);
			((MacroMenuGui)gui).markDirty();
			return true;
		}
		return false;
	}
	@Override
	public boolean onCharTyped(Gui gui, char typedChar, int mods) {
		return false;
	}
	@Override
	public boolean onKeyRelease(Gui gui, int keyCode, int scanCode, int modifiers) {
		return false;
	}
	@Override
	public boolean onKeyRepeat(Gui gui, int keyCode, int scanCode, int modifiers, int n) {
		return false;
	}
	@Override
	public void onDraw(Gui gui, int mouseX, int mouseY, float partialTicks) {
		if(!isVisible) return;
		gui.fill(x, y, x+fullWid, y+12, Color.BLACK.toInt());
		removeButton.onDraw(gui, mouseX, mouseY, partialTicks);
		enableButton.onDraw(gui, mouseX, mouseY, partialTicks);
		modeButton.onDraw(gui, mouseX, mouseY, partialTicks);
		eventSelector.onDraw(gui, mouseX, mouseY, partialTicks);
		pickScript.onDraw(gui, mouseX, mouseY, partialTicks);
		editButton.onDraw(gui, mouseX, mouseY, partialTicks);
		moveButton.onDraw(gui, mouseX, mouseY, partialTicks);
		
		gui.drawString(gui.getFontRend(), shortScriptName(script), eventSelector.getX()+eventSelector.getItemWidth()+2, eventSelector.getY()+3, Color.WHITE.toInt());//TODO customizeable color
		gui.drawHorizontalLine(x, x+fullWid, y, Color.WHITE.toInt());
		gui.drawHorizontalLine(x, x+fullWid, y+12, Color.WHITE.toInt());
	}

	private String shortScriptName(String script2) {
		if(script2==null)return "";
		if(script2.contains("/")) {
			return script2.substring(script2.lastIndexOf('/')+1);
		}
		return script2;
	}

	int fullWid;
	@Override
	public void setWidth(int i) {
		fullWid = i;
		i -= removeButton.getWid() + enableButton.getWid() + modeButton.getWid() + editButton.getWid() + moveButton.getWid();
		eventSelector.setWidth(i/2);
		i/=2;
		//scriptSelector.setWidth(eventSelector.getItemWidth());
		//System.out.println("Resized to "+i);
		sWid = i;  //TODO this is the reason it makes grab'd bindings look tiny, keeping it though
		updatePos(x, y);
	}
	@Override
	public void setPos(int x, int y) {
		this.x = x;
		this.y = y;
		updatePos(x, y);
	}
	@Override
	public void setX(int x) {
		setPos(x, y);
	}
	@Override
	public void setY(int y) {
		setPos(x, y);
	}
	private void updatePos(int x, int y){
		int uw = x;
		removeButton.setPos(uw,    y);
		enableButton.setPos(uw+=removeButton.getWid(), y);
		modeButton.setPos(  uw+=enableButton.getWid(), y);
		eventSelector.setPos(uw+=modeButton.getWid(), y);
		
		uw = fullWid-pickScript.getItemWidth() - editButton.getItemWidth() - moveButton.getItemWidth()+5;
		pickScript.setPos(uw, y);
		//uw=scriptSelector.getItemWidth()-editButton.getWid()-moveButton.getWid()+5;
		editButton.setPos(uw+=pickScript.getItemWidth(), y);
		moveButton.setPos(uw+=editButton.getWid(), y);
	}
	@Override
	public void setHeight(int i) {
		//GUI binding has fixed height of 12
	}

	static LinkedList<String> scripts = new LinkedList<>();
	public static void addScriptName(String string) {
		scripts.add(string);
	}
	public static void clearScripts(){
		scripts.clear();
	}
//	static LinkedList<String> events = new LinkedList<>();
//	public static void addEventName(String string) {
//		events.add(string);
//	}
	
	public void fromString(String s){
		String temp = "";
		int prop = 0;
		for(int i = 0; i<s.length(); i++){
			if(s.charAt(i)==':'){
				switch (prop++) {
				case 0:
					enable = temp.equals("true");
					break;
				}
			}
		}
	}
	
	@Override
	public String toString(){
		return enable + ":" + (eventMode) + ":" + eventSelector.dispText + ":" + script;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	
	@Override
	public EventMode getEventMode() {
		return eventMode;
	}
	
	
	@Override
	public boolean isEnabled(){
		return enable;
	}
	@Override
	public boolean isDisabled() {
		return !enable;
	}

	@Override
	public String getEventName() {
		return eventSelector.dispText;
	}
//	public String getTriggerName() {
//		return eventSelector.dispText;
//	}
	@Override
	public String getScriptName() {
		return script;
	}
	
	
	/**pass screen height, it will drop as far as there*/
	public void setMaxDropHeight(int height) {
//		if(getScriptName().equals("")){
//			System.out.printf("Heigh will be set to %s of %s\n",height - (scriptSelector.getY()+scriptSelector.getItemHeight()), height);
//++++++++++++++++++++		}
		//System.out.println("2:"+(height - (scriptSelector.getY()+scriptSelector.getItemHeight())));
		//scriptSelector.setMaxHeight(height - (scriptSelector.getY()+scriptSelector.getItemHeight()));
		eventSelector.setMaxHeight(height - (eventSelector.getY()+eventSelector.getItemHeight()));
	};
	
	public LuaTable toLuaTable(){
		LuaTable t = new LuaTable();
		t.set("enabled", LuaValue.valueOf(isEnabled()));
		//t.set("mode", LuaValue.valueOf(isKey?"key":"event"));
		t.set("mode", LuaValue.valueOf(eventMode.name()));
		t.set("event", eventSelector.dispText);
		t.set("script", LuaValue.valueOf(script==null?"":script));
		return t;
	}
	
	public void loadFromLuaTable(LuaTable t){
		enable = t.get("enabled").checkboolean();
		updateEnableButton();
		String mode = t.get("mode").checkjstring();
		switch (mode) {
		case "event": //legacy (aka before v1 even released
		case "EVENT":
			eventMode = EventMode.EVENT;
			break;
		
		case "KEY_DOWN":
			eventMode = EventMode.KEY_DOWN;
			break;
			
		case "KEY_UP":
			eventMode = EventMode.KEY_UP;
			break;
			
		case "key": //legacy (aka before v1 even released
		case "KEY_ALL":
			eventMode = EventMode.KEY_ALL;
			break;
		default:
			eventMode = EventMode.KEY_ALL;
			break;
		}
		
		setEvent(t.get("event").tojstring());
		setScript(t.get("script").tojstring());
		updateModeButton();
		
	}

	public void setEnabled(boolean checkboolean) {
		enable = checkboolean;
		updateEnableButton();
	}

	
	public void setEventMode(EventMode eventMode) {
		this.eventMode = eventMode;
		updateModeButton();
	}
	
	/**will check that event exists, or key is defined based on current mode*/
	public void setEvent(String tojstring) {
		((MacroMenuGui)gui).markDirty();
		if(!eventMode.isKeyType()){
			eventSelector.clear(true);
			for (EventName string : ForgeEventHandler.EventName.values()) {
				eventSelector.addOption(string.name());
			}
			eventSelector.select(tojstring);
		}else{
			if(Keyboard.codeOf(tojstring)!=Keyboard.UNKNOWN_KEY_CODE || Mouse.codeOf(tojstring)!=Mouse.UNKNOWN_MOUSE_BUTTON)
				eventSelector.dispText = tojstring;
		}
	}

	public void setScript(String tojstring) {
		script = tojstring;
	}
	
//	public void updateScripts(){
//		scriptSelector.clear(true);
//		for (String string : scripts) {
//			scriptSelector.addOption(string);
//		}
//	}
//	public void updateEvents(){
//		eventSelector.clear(true);
//		for (String string : events) {
//			eventSelector.addOption(string);
//		}
//	}
	public static enum EventMode{
		KEY_DOWN,
		KEY_UP,
		KEY_ALL,
		EVENT;
		public boolean isKeyType(){
			return !this.equals(EVENT);
		}
		public EventMode cycleNext(){
			switch (this) {
			case EVENT:
				return KEY_DOWN;
			case KEY_DOWN:
				return EventMode.KEY_UP;
			case KEY_UP:
				return KEY_ALL;
			case KEY_ALL:
				return EVENT;
			}
			return null;
		}
		public boolean isKeyAllowed(boolean keyDown){
			if(this.equals(KEY_ALL))
				return true;
			if(this.equals(EVENT))
				return false;
			return (this.equals(KEY_DOWN))==keyDown;
		}
	}
	
	
	@Override
	public Drawable getDrawableElement() {
		return this;
	}
}
