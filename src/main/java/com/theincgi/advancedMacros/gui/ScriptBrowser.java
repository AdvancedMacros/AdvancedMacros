package com.theincgi.advancedMacros.gui;

import java.io.File;
import java.io.IOException;

import org.luaj.vm2.LuaValue;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.ForgeEventHandler;
import com.theincgi.advancedMacros.gui.elements.ColorTextArea;
import com.theincgi.advancedMacros.gui.elements.Drawable;
import com.theincgi.advancedMacros.gui.elements.GuiButton;
import com.theincgi.advancedMacros.gui.elements.GuiRect;
import com.theincgi.advancedMacros.gui.elements.ListManager;
import com.theincgi.advancedMacros.gui.elements.Moveable;
import com.theincgi.advancedMacros.gui.elements.OnClickHandler;
import com.theincgi.advancedMacros.gui.elements.PopupPrompt;
import com.theincgi.advancedMacros.gui.elements.PopupPrompt.Answer;
import com.theincgi.advancedMacros.gui.elements.PopupPrompt.Choice;
import com.theincgi.advancedMacros.gui.elements.WidgetID;
import com.theincgi.advancedMacros.misc.Settings;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.Minecraft;

public class ScriptBrowser extends Gui{
	ListManager listManager;
	public static final String PROP_TABLE = "colors.scriptBrowser";
	ColorTextArea textPreview;
	private File previewing = null;
	GuiButton back, createFile;
	PopupPrompt prompt;
	Prompting prompting = null;
	public ScriptBrowser() {
		//SScript.width=width/2;
		//SScript.height=12;
		listManager = new ListManager(5, 22, width/3-5, height-27, new WidgetID(400), PROP_TABLE);
		prompt = new PopupPrompt(new WidgetID(404), width/3, 36, width/3, height/3,this);
		listManager.setSpacing(4);
		listManager.setAlwaysShowScroll(true);
		listManager.setDrawBG(false); 
		//listManager.setScrollbarGap(5);
		textPreview = new ColorTextArea(new WidgetID(300), this);//made to match settings from editor
		back = new GuiButton(new WidgetID(402), 5, 5, listManager.getItemWidth()/2, 12, LuaValue.NIL, LuaValue.valueOf("Back"), PROP_TABLE, Color.BLACK, Color.WHITE, Color.WHITE);
		createFile = new GuiButton(new WidgetID(403), 5+back.getWid(), 5, back.getWid(), 12, LuaValue.NIL, LuaValue.valueOf("Create File"), PROP_TABLE, Color.BLACK, Color.WHITE, Color.WHITE);
		textPreview.setEditable(false);
		textPreview.setFocused(true);

		listManager.setScrollSpeed(4);

		inputSubscribers.add(listManager);
		inputSubscribers.add(textPreview);
		inputSubscribers.add(back);
		inputSubscribers.add(createFile);

		drawables.add(listManager);
		drawables.add(textPreview);
		drawables.add(back);
		drawables.add(createFile);
		populate();

		back.setOnClick(new OnClickHandler() {
			@Override
			public void onClick(int button, GuiButton sButton) {
				ForgeEventHandler.showPrevMenu();;
			}
		});
		createFile.setOnClick(new OnClickHandler() {
			@Override
			public void onClick(int button, GuiButton sButton) {
				prompting = Prompting.FILE_NAME;
				prompt.prompt("New file name:");
			}
		});
		prompt.setOnAns(new Runnable() {
			@Override
			public void run() {
				Answer a = prompt.checkAns();
				if(a!=null && a.c.equals(Choice.OK)){
					if(prompting.equals(Prompting.FILE_NAME)){
						File f = new File(AdvancedMacros.macrosFolder, a.answer);
						try {
							f.createNewFile();
						} catch (IOException e) {
							e.printStackTrace();
						}
						populate();
					}else if(prompting.equals(Prompting.RENAME)){
						if(a.answer.contains("..")||a.answer.contains("/")){
							prompt.promptError("Name can't contain\n"
									+ "'..' or '/'");
							return;
						}
						previewing.renameTo(new File(AdvancedMacros.macrosFolder, a.answer));
						populate();
					}else if(prompting.equals(Prompting.CONFIRM_DELETE)){
						previewing.delete();
						previewing=null;
						populate();
					}
				}
			}
		});
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);	
	}
	public void updateKeywords(){
		ColorTextArea.updateKeywords();
	}
	private void populate() {
		listManager.clear();
		for(File f:AdvancedMacros.getScriptList()){
			SScript sspt = new SScript(new WidgetID(401), 5, 5, f);
			sspt.setHeight(12);
			listManager.add(sspt);
		}
	}

	@Override
	public void onOpen() {
		populate();
		//System.out.println("Open'd script browser");
		if(previewing!=null){
			textPreview.openScript(previewing.getName());
		}
	}

	@Override
	public void setWorldAndResolution(Minecraft mc, int width, int height) {
		super.setWorldAndResolution(mc, width, height);
		listManager.setWidth(width/3-10);
		listManager.setHeight(height-27);
		textPreview.resize(width*2/3-5, height-10);
		textPreview.setPos(width/3, 5);
		back.setWidth(listManager.getItemWidth()/2);
		createFile.setWidth(listManager.getItemWidth()/2);
		createFile.setPos(back.getX()+back.getWid(), 5);
		prompt.setPos(width/3, height/3);
		prompt.resize(width/3, 42);
		//SScript.width = width/2-5;
	}
	public void setPreview(File sFile) {
		previewing = sFile;
		textPreview.openScript(sFile.getName());

	}
	private static LuaValue trashTexture = Settings.getTextureID("resource:trashcan.png"),
			renameTexture = Settings.getTextureID("resource:whiterename.png");
	private class SScript implements Drawable, Moveable, InputSubscriber{
		GuiButton trash;
		GuiButton rename;
		int WHITE = Color.WHITE.toInt(), BLACK = Color.BLACK.toInt();
		int FILE_COLOR = WHITE, SELECT_COLOR=Color.TEXT_b.toInt();
		int x,y;
		File sFile;
		int width,height;
		private boolean isVisible=false;
		public SScript(WidgetID wID, int x, int y, final File sFile) {
			trash = new GuiButton(wID, x, y, height, height, LuaValue.NIL, LuaValue.NIL, PROP_TABLE, Color.BLACK, Color.WHITE, Color.WHITE);
			trash.changeTexture(Utils.checkTexture(trashTexture));
			rename = new GuiButton(wID, x+height, y, height*3, height, LuaValue.NIL, LuaValue.NIL, PROP_TABLE, Color.BLACK, Color.WHITE, Color.WHITE);
			rename.changeTexture(Utils.checkTexture(renameTexture));
			this.sFile = sFile;

			trash.setOnClick(new OnClickHandler() {
				@Override
				public void onClick(int button, GuiButton sButton) {
					prompting = Prompting.CONFIRM_DELETE;
					prompt.promptConfirm("Delete '"+sFile.getName()+"'?");
					previewing = sFile;
				}
			});
			rename.setOnClick(new OnClickHandler() {
				@Override
				public void onClick(int button, GuiButton sButton) {
					prompting = Prompting.RENAME;
					previewing = sFile;
					prompt.prompt("Rename '"+sFile.getName()+"' to:");
				}
			});
		}
		@Override
		public void setWidth(int width) {
			this.width = width;
		}
		@Override
		public void setHeight(int height) {
			this.height = height;
			trash.resize(height, height);
			rename.resize(height*3, height);
		}
		@Override
		public void setPos(int x, int y){
			this.x = x;
			this.y = y;
			trash.setPos(x, y);
			rename.setPos(x+height, y);
		}
		@Override
		public void onDraw(Gui gui, int mouseX, int mouseY, float partialTicks) {
			if(!isVisible){return;}
			gui.drawBoxedRectangle(x, y, width, height, WHITE, BLACK); 
			trash.onDraw(gui, mouseX, mouseY, partialTicks);
			rename.onDraw(gui, mouseX, mouseY, partialTicks);
			int old = gui.getFontRend().FONT_HEIGHT;
			gui.getFontRend().FONT_HEIGHT=height-2;
			gui.getFontRend().drawString(sFile.getName(), x+height*4+2, y+2, 
					sFile.equals(ScriptBrowser.this.previewing)?SELECT_COLOR:FILE_COLOR); //MAYBE later change to also allow for directories... maybe
			gui.getFontRend().FONT_HEIGHT=old;
		}
		@Override
		public void setVisible(boolean b) {
			isVisible = b;
		}
		@Override
		public int getItemHeight() {
			return height;
		}
		@Override
		public int getItemWidth() {
			return width;
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
		public boolean onScroll(Gui gui, int i) {
			return false;
		}
		@Override
		public boolean onMouseClick(Gui gui, int x, int y, int buttonNum) {
			if(GuiRect.isInBounds(x, y, this.x+this.height*4, this.y, this.width, this.height)){
				ScriptBrowser.this.setPreview(sFile);
				if(ColorTextArea.isShiftDown()){
					ForgeEventHandler.showMenu(AdvancedMacros.editorGUI, ScriptBrowser.this);
					AdvancedMacros.editorGUI.openScript(sFile.getName());
				}
				return true;
			}
			if(rename.onMouseClick(gui, x, y, buttonNum)) return true;
			if(trash.onMouseClick(gui, x, y, buttonNum)) return true;
			return false;
		}
		@Override
		public boolean onMouseRelease(Gui gui, int x, int y, int state) {
			if(GuiRect.isInBounds(x, y, this.x+this.height*4, this.y, this.width, this.height)){
				return true;
			}
			if(rename.onMouseRelease(gui, x, y, state)) return true;
			if(trash.onMouseRelease(gui, x, y, state))return true;
			return false;
		}
		@Override
		public boolean onMouseClickMove(Gui gui, int x, int y, int buttonNum, long timeSinceClick) {
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
	}
	private enum Prompting{
		FILE_NAME,
		RENAME,
		CONFIRM_DELETE;
	}
}