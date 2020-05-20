package com.theincgi.advancedMacros.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collections;
import java.util.LinkedList;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.event.ForgeEventHandler;
import com.theincgi.advancedMacros.event.ForgeEventHandler.EventName;
import com.theincgi.advancedMacros.gui.elements.Drawable;
import com.theincgi.advancedMacros.gui.elements.GuiBinding;
import com.theincgi.advancedMacros.gui.elements.GuiBinding.EventMode;
import com.theincgi.advancedMacros.gui.elements.GuiButton;
import com.theincgi.advancedMacros.gui.elements.GuiDropDown;
import com.theincgi.advancedMacros.gui.elements.GuiDropDown.OnSelectHandler;
import com.theincgi.advancedMacros.gui.elements.ListManager;
import com.theincgi.advancedMacros.gui.elements.Moveable;
import com.theincgi.advancedMacros.gui.elements.OnClickHandler;
import com.theincgi.advancedMacros.gui.elements.PopupPrompt;
import com.theincgi.advancedMacros.gui.elements.PopupPrompt.Answer;
import com.theincgi.advancedMacros.gui.elements.PopupPrompt.Choice;
import com.theincgi.advancedMacros.gui.elements.WidgetID;
import com.theincgi.advancedMacros.lua.LuaDebug;
import com.theincgi.advancedMacros.lua.OpenChangeLog;
import com.theincgi.advancedMacros.lua.LuaDebug.OnScriptFinish;
import com.theincgi.advancedMacros.misc.PropertyPalette;
import com.theincgi.advancedMacros.misc.Settings;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.Minecraft;

public class MacroMenuGui extends Gui implements IBindingsGui{
	//BufferedImage img = null;
	//GuiDropDown guiDropDown = new GuiDropDown(new WidgetID(10), 5, 5, 150, 12, 150, null);

	private String propAddress = "colors.bindingsMenu";

	//GuiDropDown profileSelect = new GuiDropDown(new WidgetID(1), 5, 5, width-23, 12, 12*7, propAddress);
	GuiDropDown profileSelect = new GuiDropDown(5, 5, width-22, 12, 12*7, "bindingsMenu", "profileSelect");
	GuiButton   addProfile    = new GuiButton(width-22, 5, 12, 12, Settings.getTextureID("resource:whiteplus.png"), LuaValue.NIL, "bindingsMenu","addProfileButton");
	//GuiButton   addProifle    = new GuiButton(new WidgetID(2),   width-22, 5, 12, 12, Settings.getTextureID("resource:whiteplus.png"), LuaValue.NIL, propAddress, Color.BLACK, com.theincgi.advancedMacros.gui.Color.WHITE, Color.WHITE);
	//GuiButton removeProifle   = new GuiButton(new WidgetID(3),   width-22, 5, 12, 12, Settings.getTextureID("resource:trashcan.png"), LuaValue.NIL, propAddress, Color.BLACK, com.theincgi.advancedMacros.gui.Color.WHITE, Color.WHITE);
	GuiButton removeProfile   = new GuiButton(width-22, 5, 12, 12, Settings.getTextureID("resource:trashcan.png"), LuaValue.NIL, "bindingsMenu", "removeProfileButton");
	//GuiButton addBinding       = new GuiButton(new WidgetID(4),   5, 17, 36, 12, Settings.getTextureID("resource:whitenewbinding.png"), LuaValue.NIL, propAddress, Color.BLACK, Color.WHITE, Color.WHITE);
	GuiButton addBinding       = new GuiButton(5, 17, 36, 12, Settings.getTextureID("resource:whitenewbinding.png"), LuaValue.NIL, "bindingsMenu", "addBindingButton");
	GuiButton showScriptManager=new GuiButton(41, 17, 36, 12, Settings.getTextureID("resource:whitescripts.png"), LuaValue.NIL, "bindingsMenu", "scriptManagerButton");
	//whitescripts
	GuiButton openChangeLog = new GuiButton(width-36-6, 17, 37, 12, Settings.getTextureID("resource:whiteopenchangelog.png"), LuaValue.NIL, "bindingsMenu", "openChangeLog");
	//GuiButton showScriptManager=new GuiButton(new WidgetID(5), 41, 17, 36, 12, Settings.getTextureID("resource:whitescripts.png"), LuaValue.NIL, propAddress, Color.BLACK, Color.WHITE, Color.WHITE); //file browser thing, no folders tho, all macros are in /macros, this will be sepereate gui housing the script manager
	//GuiButton gotoSettings	  = new GuiButton(new WidgetID(6), width-5-36, 17, 36, 12, Settings.getTextureID("resource:whitesettings.png"), LuaValue.NIL, null, Color.BLACK, Color.WHITE, Color.BLACK);
	Prompting prompting;
	PopupPrompt prompt;


	
	ListManager bindingsList = new ListManager(5, 33, 100, 12*10, /*new WidgetID(33), propAddress*/ new PropertyPalette());
	//	GuiBinding gb = new GuiBinding(new WidgetID(11), 5, 5, 300, bindingsList, this);
	//	GuiBinding gb2 = new GuiBinding(new WidgetID(11), 5, 5, 300, bindingsList, this);
	//	GuiBinding gb3 = new GuiBinding(new WidgetID(11), 5, 5, 300, bindingsList, this);
	private static WidgetID bindingID = new WidgetID(11);
	private OnClickHandler onRemoveBinding;
	public MacroMenuGui() {	
		//		bindingsList.add(gb);
		//		bindingsList.add(gb2);
		//		bindingsList.add(gb3);
		bindingsList.setAlwaysShowScroll(true);
		bindingsList.setDrawBG(false);

		bindingsList.setScrollbarWidth(12);
		bindingsList.setScrollbarGap(5);
		bindingsList.setSpacing(8);

		bindingsList.setScrollSpeed(4);
		
		prompt = new PopupPrompt(new WidgetID(303), width/3, height/3, width/3, height/3, this);


		//		for(EventName s : ForgeEventHandler.EventName.values())
		//			GuiBinding.addEventName(s.name());

		//update size regularly
		//		drawables.add(new Drawable() {
		//			@Override
		//			public void onDraw(Gui g, int mouseX, int mouseY, float partialTicks) {
		//				//listManager.setWidth(width-24);
		//				gb.setWidth(width-24);
		//			}
		//		});


		addBinding.setOnClick(new OnClickHandler() {
			@Override
			public void onClick(int button, GuiButton sButton) {
				GuiBinding g;
				bindingsList.add(g=new GuiBinding(bindingID, 5, 5, 300, bindingsList, MacroMenuGui.this));
				markDirty();
			}
		});

		profileSelect.setOnOpen(new OnClickHandler() {
			@Override
			public void onClick(int button, GuiButton sButton) {
				if(profileSelect.isOpen()){
					profileSelect.close();
				}else{
					updateProfileList();
					updateProfileChanges();
					profileSelect.open();
				}
			}


		});
		profileSelect.setOnSelect(new OnSelectHandler() {
			@Override
			public void onSelect(int index, String text) {
				loadProfile(profileSelect.getSelection());
				System.out.println("Loading... "+profileSelect.getSelection());
			}
		});

		showScriptManager.setOnClick(new OnClickHandler() {
			@Override
			public void onClick(int button, GuiButton sButton) {
				//if(Gui.isAltKeyDown())
				updateProfileChanges();
				ForgeEventHandler.showMenu(AdvancedMacros.scriptBrowser2);
				//else
				//	ForgeEventHandler.showMenu(AdvancedMacros.scriptBrowser);
				//AdvancedMacros.getMinecraft().displayGuiScreen(AdvancedMacros.scriptBrowser);
			}
		});
		addProfile.setOnClick(new OnClickHandler() {
			@Override
			public void onClick(int button, GuiButton sButton) {
				prompting = Prompting.PROFILE_NAME;
				prompt.prompt("New profile name:");
				markDirty();
			}
		});

		prompt.setOnAns(new Runnable() {
			@Override
			public void run() {
				Answer a = prompt.checkAns();
				if(a!=null && a.c.equals(Choice.OK)){
					if (prompting.equals(Prompting.PROFILE_NAME)) {
						if(Settings.getProfileTable().get(a.answer).isnil()){
							Settings.getProfileTable().set(a.answer, new LuaTable());
							Settings.save();
							loadProfile(a.answer);
						}else{
							prompting=Prompting.ERROR;
							prompt.promptError("Profile already exists");
						}
						//}else if(prompting.equals(Prompting.CONFIRM_DELETE_FILE)){
					}else if(prompting.equals(Prompting.CONFIRM_DELETE_PROFILE)){
						Settings.getProfileTable().set(profileSelect.getSelection(), LuaValue.NIL);
						Settings.save();
						updateProfileList();
						loadProfile("DEFAULT");
					}
				}
			}
		});
		removeProfile.setOnClick(new OnClickHandler() {
			@Override
			public void onClick(int button, GuiButton sButton) {
				prompting = Prompting.CONFIRM_DELETE_PROFILE;
				prompt.promptConfirm("Delete this profile?");
			}
		});
		
		openChangeLog.setOnClick(new OnClickHandler() {
			@Override
			public void onClick(int button, GuiButton sButton) {
				OpenChangeLog.openChangeLog();
			}
		});

		addInputSubscriber(bindingsList);
		addInputSubscriber(profileSelect);
		addInputSubscriber(removeProfile);
		addInputSubscriber(addProfile);
		addInputSubscriber(showScriptManager);
		//inputSubscribers.add(gotoSettings);
		addInputSubscriber(addBinding);
		addInputSubscriber(openChangeLog);

		addDrawable(bindingsList);
		addDrawable(profileSelect);
		addDrawable(removeProfile);
		addDrawable(addProfile);
		addDrawable(showScriptManager);
		//drawables.add(gotoSettings);
		addDrawable(addBinding);
		addDrawable(openChangeLog);

		//updateProfileList();
//		profileSelect.select("DEFAULT"); done in INIT
		onResize(AdvancedMacros.getMinecraft(), width, height);

	}
	
	
	
	public void reloadCurrentProfile() {
		loadProfile(profileSelect.getSelection());
	}
	
	public void updateProfileList() {
		profileSelect.clear(false);
		for(String profile : Settings.getProfileList()){
			profileSelect.addOption(profile);
		}		
	}
	
    @Override
    public void removeBinding(IBinding binding) {
    	Drawable drawable = binding.getDrawableElement();
    	removeDrawables(drawable);
    	if (binding instanceof InputSubscriber) {
			InputSubscriber in = (InputSubscriber) binding;
			removeInputSubscriber(in);
			
		}
    	bindingsList.remove(drawable);
    	markDirty();
    }
//    @Override
//    public void removeBinding(int index) {
//    	Moveable d = bindingsList.getItem(index);
//    	
//    }
//	public void removeBinding(GuiBinding guib) {
//		removeDrawables(guib);
//		inputSubscribers.remove(guib);
//		bindingsList.remove(guib);
//		markDirty();
//		//TODO save remove
//	}

	/**Ignore isKeydown for non key events, doesn't matter, 
	 * @param isKey simply, is this a key event?<br>
	 * @param eventName this should match the name in the trigger, key name or event, doesn't matter, just needs to match<br>
	 * @param args launch args for script, includes how the script was called<br>
	 * @param isKeyDown, if key is down for key event*/
	public void fireEvent(boolean isKey, String eventName, Varargs args, boolean isKeyDown){
		fireEvent(isKey, eventName, args, isKeyDown, null);
	}
	
	/**Exists, and is enabled*/
	public boolean doesEventExist(String eventName){
		for(Moveable m : bindingsList.getItems()){
			if(m instanceof IBinding){
				IBinding b = (IBinding) m;
				if(b.isDisabled()) 
					continue; //disabled, skip
				//System.out.println("Event type matched");
				//System.out.println("Not key or keyAllowed");
				if(b.getEventName().equals(eventName) && !b.getEventMode().isKeyType()){ //right event
					return true;
				}
			}
		}
		return false;
	}
	public void fireEvent(boolean isKey, String eventName, Varargs args, boolean isKeyDown, OnScriptFinish onScriptFinish){
		for(Moveable m : bindingsList.getItems()){
			if(m instanceof IBinding){
				IBinding b = (IBinding) m;
				if(b.isDisabled()) 
					continue; //disabled, skip
				boolean override = b.getEventMode().equals(EventMode.EVENT) && b.getEventName().equals(ForgeEventHandler.EventName.Anything.name());
				if(eventName.equals(EventName.Chat.name())){
					override = false;
				}
				if(override || (isKey==b.getEventMode().isKeyType() )){ 
					//System.out.println("Event type matched");
					if(override || (!isKey || (isKey &&	(b.getEventMode().isKeyAllowed(isKeyDown))))){ //right mode
						//System.out.println("Not key or keyAllowed");
						if(override || b.getEventName().equals(eventName)){ //right event
							//System.out.println("Trigger matched!");

							//TODO pcall of some kind
							if(b.getScriptName()==null) return;
							File f = new File(AdvancedMacros.macrosFolder, b.getScriptName());
							if(f.exists() && f.isFile()) {
								try {
									FileReader fr = new FileReader(f);
									LuaValue function = AdvancedMacros.globals.load(fr, f.getAbsolutePath());
									LuaDebug.LuaThread t = new LuaDebug.LuaThread(function, args, b.getScriptName());
									t.start(onScriptFinish);
								} catch (FileNotFoundException e) {
									e.printStackTrace();
								}catch (LuaError le){
									Utils.logError(le);
								}
							}
						}
					}
				}
			}
		}
	}
	
	public LinkedList<String> getMatchingScripts(boolean isKey, String eventName, boolean isKeyDown) {
		LinkedList<String> out = new LinkedList<>();
		for(Moveable m : bindingsList.getItems()){
			if(m instanceof IBinding){
				IBinding g = (IBinding) m;
				if(g.isDisabled()) continue;
				if(g.getEventName().equals(eventName) &&
				   !g.getEventMode().isKeyType())
					out.add(g.getScriptName());
			}
		}
		return out;
	}

	public boolean loadProfile(String profile){
		updateProfileList();
		updateScriptList();
		//System.out.println(Settings.getProfileList());

		profileSelect.select(profile);
		if(profileSelect.getSelection().equals(profile)){ //select wont set if it doesnt exist
			bindingsList.clear();

			LuaTable profiles = Settings.getProfileTable().get(profile).checktable();
			LinkedList<Integer> keys = new LinkedList<>();

			for(LuaValue v = profiles.next(LuaValue.NIL).arg1(); !v.isnil(); v = profiles.next(v).arg1()){
				if(v.isint()){
					keys.add(v.checkint());
				}else{
					System.out.println("Key isnt int "+v.toString());
				}
			}
			Collections.sort(keys);
			System.out.println(keys);
			for(int i : keys){
				System.out.println("MacroMenu: LoadProfile: "+i);
				LuaTable t = profiles.get(i).checktable();

				GuiBinding g;
				g=new GuiBinding(bindingID, 5, 5, 300, bindingsList, MacroMenuGui.this);
				//g.updateEvents();
				//g.updateScripts();
				g.loadFromLuaTable(t);
				//				g.setEnabled(t.get("enabled").checkboolean());
				//				
				//				g.setIsKey(t.get("mode").tojstring().equals("key"));
				//				g.setEvent(t.get("event").tojstring());
				//				g.setScript(t.get("script").tojstring());
				bindingsList.add(g);
			}
			LuaTable args = AdvancedMacros.forgeEventHandler.createEvent(EventName.ProfileLoaded);
			args.set(3, LuaValue.valueOf(profile));
			AdvancedMacros.forgeEventHandler.fireEvent(EventName.ProfileLoaded, args);

			return true;
		}else{
			System.out.printf("No profile %s could be found\n", profile);
			return false;
		}
	}
	
	
	
	@Override
	public void onResize(Minecraft mcIn, int w, int h) {
		super.onResize(mcIn, w, h);
		addProfile.setPos(width-5-24, 5);
		removeProfile.setPos(width-5-12, 5);
		//gotoSettings.setPos(width-5-36, 17);
		bindingsList.setWidth(w-24);
		//gotoSettings.setPos(12*6+5, 17);
		profileSelect.setPos(5, 5);
		profileSelect.setWidth(w-35);
		profileSelect.setMaxHeight(h/2);
		openChangeLog.setPos(width -36-6, 17);
		openChangeLog.setWidth(37);
		
		bindingsList.setWidth(width-10);
		bindingsList.setHeight(h-15-24);
		prompt.setPos(width/3, height/3);
		prompt.resize(width/3, height/3);
		bindingsList.setBonusSpace(h/3);
	}
	public void markDirty() {dirty = true; System.out.println("Profile marked for saving.");}
	private boolean dirty = false;
	@Override
	public void onGuiClosed() {
		updateProfileChanges();
		super.onGuiClosed();
	}

	public void updateProfileChanges() {
		if(!dirty) return;
		dirty = false;
		LuaTable profiles = Settings.getProfileTable();
		LuaTable profile = new LuaTable();
		profiles.set(profileSelect.getSelection(), profile);
		int slot = 1;
		for(Moveable m : bindingsList.getItems()){
			if(m instanceof GuiBinding){
				profile.set(slot++, ((GuiBinding) m).toLuaTable());
			}
		}
		Settings.save();
	}
	//private static final ResourceLocation HOPPER_GUI_TEXTURE = new ResourceLocation("textures/gui/container/hopper.png");
	//GuiButton button = new GuiButton(this,new WidgetID(1), 5, 5, 30, 30, Settings.getTextureID("D:\\backup\\Ian\\Pictures\\dan.png"), LuaValue.valueOf("Hello"));
	//ResourceLocation textureTest;
	//GuiScrollBar scrollBar = new GuiScrollBar(this, new WidgetID(2), 10	, 5, 7, 120, Orientation.UPDOWN); 
	//	ListManager listManager = new ListManager(15, 15, 150, 150, new WidgetID(3), "testList");
	//	//GuiScrollBar scrollBar2 = new GuiScrollBar(this, new WidgetID(3), 	10	, 30, 7, 120, Orientation.LEFTRIGHT);
	//	//GuiRect r = new GuiRect(this, new WidgetID(2), 30, 30, 100, 100);
	//	GuiButton button1  = new GuiButton(new WidgetID(4), 0, 0, 40, 20, LuaValue.NIL, LuaValue.valueOf("Blah1"), null);
	//	GuiButton button2  = new GuiButton(new WidgetID(5), 0, 0, 40, 20, LuaValue.NIL, LuaValue.valueOf("Blah2"), null);
	//	GuiButton button3  = new GuiButton(new WidgetID(6), 0, 0, 40, 20, LuaValue.NIL, LuaValue.valueOf("Blah3"), null);
	//	GuiButton button4  = new GuiButton(new WidgetID(7), 0, 0, 40, 20, LuaValue.NIL, LuaValue.valueOf("Blah4"), null);
	//	GuiButton button5  = new GuiButton(new WidgetID(8), 0, 0, 40, 20, LuaValue.NIL, LuaValue.valueOf("Blah5"), null);
	//	GuiButton button6  = new GuiButton(new WidgetID(9), 0, 0, 40, 20, LuaValue.NIL, LuaValue.valueOf("Blah6"), null);
	//	GuiButton button7  = new GuiButton(new WidgetID(9), 0, 0, 40, 20, LuaValue.NIL, LuaValue.valueOf("Blah7"), null);
	//	GuiButton button8  = new GuiButton(new WidgetID(9), 0, 0, 40, 20, LuaValue.NIL, LuaValue.valueOf("Blah8"), null);
	//	GuiButton button9  = new GuiButton(new WidgetID(9), 0, 0, 40, 20, LuaValue.NIL, LuaValue.valueOf("Blah9"), null);
	//	GuiButton button10 = new GuiButton(new WidgetID(9), 0, 0, 40, 20, LuaValue.NIL, LuaValue.valueOf("Blah10"),null);
	//	
	
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		
		
		for(Moveable m: bindingsList.getItems()){
			if(m instanceof GuiBinding){
				GuiBinding b = ((GuiBinding) m);
				//System.out.println("1: "+height);
				b.setMaxDropHeight(height-5);

			}
		}
		onResize(AdvancedMacros.getMinecraft(), width, height);
		//gb.setWidth(width-10);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	public static void updateScriptList(){
		GuiBinding.clearScripts();
		for(File s : AdvancedMacros.getScriptList()){
			GuiBinding.addScriptName(s.getName());
			//System.out.println("SCRIPT ADD: "+s.getName());
		}
	}


	public void onGuiOpened(){

	}

	public void triggerPrompt(String msg){
		prompt.prompt(msg);
		this.drawLast = prompt;
	}
	public PopupPrompt.Answer checkPromptAns(){
		return prompt.checkAns();
	}

	public static void showMenu(){
		AdvancedMacros.getMinecraft().displayGuiScreen(AdvancedMacros.macroMenuGui.getGui());
		AdvancedMacros.macroMenuGui.onGuiOpened();
	}
	private enum Prompting{
		PROFILE_NAME,
		ERROR,
		CONFIRM_DELETE_FILE,
		CONFIRM_DELETE_PROFILE;
	}
	@Override
	public String getSelectedProfile() {
		return profileSelect.getSelection();
	}
	@Override
	public Gui getGui() {
		return this;
	}
}