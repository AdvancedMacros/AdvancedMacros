package com.theincgi.advancedMacros.gui2;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;

import org.luaj.vm2_v3_0_1.LuaValue;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.gui.Color;
import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.gui.elements.ColorTextArea;
import com.theincgi.advancedMacros.gui.elements.Drawable;
import com.theincgi.advancedMacros.gui.elements.GuiButton;
import com.theincgi.advancedMacros.gui.elements.GuiRect;
import com.theincgi.advancedMacros.gui.elements.ListManager;
import com.theincgi.advancedMacros.gui.elements.Moveable;
import com.theincgi.advancedMacros.gui.elements.OnClickHandler;
import com.theincgi.advancedMacros.gui.elements.PopupPrompt;
import com.theincgi.advancedMacros.gui.elements.PopupPrompt.Answer;
import com.theincgi.advancedMacros.gui.elements.WidgetID;
import com.theincgi.advancedMacros.misc.Property;
import com.theincgi.advancedMacros.misc.Settings;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.Minecraft;

public class ScriptBrowser2 extends Gui{

	GuiButton returnButton;
	GuiButton backButton, forwardButton;
	GuiButton createFolderButton, createFileButton;
	GuiButton searchButton;

	GuiRect addressBackdrop;

	ListManager listManager;
	ColorTextArea filePreview;

	private PopupPrompt popupPrompt;
	private PromptType promptType = null;
	private File clipboardFile = null;

	private File selectedFile;
	private File activePath=AdvancedMacros.macrosFolder;

	public ScriptBrowser2() {
		super();
		int defWid = 12;
		int defHei = 12;
		returnButton = new GuiButton(new WidgetID(600), 5, 5, defWid, defHei, Settings.getTextureID("resource:whitereturn.png"), LuaValue.NIL, null, Color.BLACK, Color.WHITE, Color.WHITE);
		backButton = new GuiButton(new WidgetID(601), 5, 5, defWid, defHei, Settings.getTextureID("resource:whiteback.png"), LuaValue.NIL, null, Color.BLACK, Color.WHITE, Color.WHITE);
		forwardButton = new GuiButton(new WidgetID(602), 5, 5, defWid, defHei, Settings.getTextureID("resource:whiteforward.png"), LuaValue.NIL, null, Color.BLACK, Color.WHITE, Color.WHITE);
		createFolderButton = new GuiButton(new WidgetID(603), 5, 5, defWid, defHei, Settings.getTextureID("resource:whitecreatefolder.png"), LuaValue.NIL, null, Color.BLACK, Color.WHITE, Color.WHITE);
		createFileButton = new GuiButton(new WidgetID(604), 5, 5, defWid, defHei, Settings.getTextureID("resource:whitecreatefile.png"), LuaValue.NIL, null, Color.BLACK, Color.WHITE, Color.WHITE);
		searchButton = new GuiButton(new WidgetID(605), 5, 5, defWid, defHei, Settings.getTextureID("resource:whitesearch.png"), LuaValue.NIL, null, Color.BLACK, Color.WHITE, Color.WHITE);

		addressBackdrop = new GuiRect(new WidgetID(606), 5, 5, defWid, defHei, "scriptBrowser.addressBackground", Color.BLACK, Color.WHITE);

		popupPrompt = new PopupPrompt(new WidgetID(404), width/3, 36, width/3, height/3,this);

		filePreview = new ColorTextArea(new WidgetID(300), this);
		filePreview.setEditable(false);
		filePreview.setFocused(true);
		
		listManager = new ListManager(5, 5, 5, 5, new WidgetID(607), "scriptBrowser.list");

		drawables.add(returnButton);
		drawables.add(backButton);
		drawables.add(forwardButton);
		drawables.add(createFolderButton);
		drawables.add(createFileButton);
		drawables.add(searchButton);
		drawables.add(listManager);
		drawables.add(filePreview);
		drawables.add(popupPrompt);

		inputSubscribers.add(returnButton);
		inputSubscribers.add(backButton);
		inputSubscribers.add(forwardButton);
		inputSubscribers.add(createFolderButton);
		inputSubscribers.add(createFileButton);
		inputSubscribers.add(searchButton);
		inputSubscribers.add(listManager);
		inputSubscribers.add(filePreview);
		//no popupPrompt, it gives itself the firstListener prop
		
		setWorldAndResolution(Minecraft.getMinecraft(), width, height);
		
		popupPrompt.setOnAns(()->{
			Answer answer = popupPrompt.checkAns();
			switch (promptType) {
			case FileAction:{
				if(answer.answer!=null)
					switch (FileActions.valueOf(answer.answer)) {
					case Copy:{
						clipboardFile = selectedFile;
						break;
					}
					case DELETE:{
						if(selectedFile!=null && selectedFile.exists())
							selectedFile.delete(); 
						break;
					}
					case Paste:{
						File temp = new File(activePath, clipboardFile.getName());
						{
							int i = 1;
							while(temp.exists()) {
								temp = new File(activePath, splice(clipboardFile.getName(), i));
							}
						}
						System.out.println("Copy from "+clipboardFile.toString()+" to "+temp.toString());
						try {
							Files.copy(clipboardFile.toPath(), temp.toPath(), new CopyOption[]{});
						} catch (IOException e) {
							e.printStackTrace();
						}
						break;
					}
					case Rename:{
						//TODO //BOOKMARK  another popup thing
						break;
					}
					default:
						break;
					}
				break;
			}
			case FileName:
				break;
			default:
				System.err.println("Unknown enum ("+promptType+") in com.theincgi.gui2.ScriptBrowser2#ScriptBrowser2");;
			}
		});
		
		populateList(AdvancedMacros.macrosFolder);
	}

	private String splice(String name, int i) {
		int m = name.lastIndexOf('.');
		String a = name.substring(0, m);
		String b = name.substring(m);
		return a+" ("+i+")"+b;
	}
@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {

		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	@Override
	public void setWorldAndResolution(Minecraft mc, int width, int height) {
		super.setWorldAndResolution(mc, width, height);

		//update sizes

		returnButton.setWidth(width/5);
		backButton.setWidth((int) Math.ceil(returnButton.getWid()/2f));
		forwardButton.setWidth((int) Math.floor(returnButton.getWid()/2f));
		createFolderButton.setWidth(returnButton.getItemWidth());
		createFileButton.setWidth(returnButton.getItemWidth());
		addressBackdrop.setWidth(width-10-returnButton.getItemWidth());

		int heiAvail = height - 20 - returnButton.getItemHeight() - backButton.getItemHeight();

		listManager.setWidth(width-10);
		listManager.setHeight((int) Math.ceil(heiAvail*2/3f));
		
		filePreview.resize(width-10, (int) Math.floor(heiAvail/3f));

		//update positions

		addressBackdrop.setPos(returnButton.getX()+returnButton.getItemWidth(), returnButton.getY());
		backButton.setPos(5, 5+returnButton.getItemHeight());
		forwardButton.setPos(backButton.getX()+backButton.getItemWidth(), backButton.getY());
		createFolderButton.setPos(forwardButton.getX()+forwardButton.getItemWidth(), forwardButton.getY());
		createFileButton.setPos(createFolderButton.getX()+createFolderButton.getItemWidth(), createFolderButton.getY());
		searchButton.setPos(width - 5 - searchButton.getItemWidth(), backButton.getY());

		listManager.setPos(5, backButton.getY()+backButton.getItemHeight()+5);
		filePreview.setPos(5, listManager.getY() + listManager.getItemHeight()+5);
	}

	public void populateList(File folder) {
		if(folder.isDirectory()) {
			File[] files = folder.listFiles();
			int needed = (files.length+2)/FileRow.SIZE;
			int has = listManager.getItems().size();

			for(int i = 0; i < has-needed; i++) {//has more than needs
				listManager.remove(listManager.getItems().size()-1);
			}
			for(int i = 0; i< needed-has; i++) {//doesnt have enough
				listManager.add(new FileRow());
			}

			for (int i = 0; i < files.length; i+=FileRow.SIZE) {
				((FileRow)listManager.getItem(i/FileRow.SIZE)).populate(files, i);
			}
		}
	}

	public class FileRow implements Moveable, Drawable, InputSubscriber{
		final static int SIZE = 3;
		final static int bufferSize = 5;
		float elementWidth = 0;
		int x,y;
		FileElement[] fileElements = new FileElement[SIZE];
		public FileRow() {
			for (int i = 0; i < fileElements.length; i++) {
				fileElements[i] = new FileElement();
			}
		}
		public void populate(File[] files, int offset) {
			for (int i = offset, m=0; i < offset+fileElements.length; i++, m++) {
				if(i<files.length)
					fileElements[m].update(files[i]);
				else
					fileElements[m].update(null);
			}
		}
		@Override
		public void setPos(int x, int y) {
			this.x = x;
			this.y = y;
			for (int i = 0; i < fileElements.length; i++) {
				if(i==0)
					fileElements[i].button.setPos(x, y);
				else
					fileElements[i].button.setPos((int) (x + Math.floor(elementWidth+bufferSize)*i), y);
			}
		}
		@Override
		public void setVisible(boolean b) {
			for (int i = 0; i < fileElements.length; i++) {
				fileElements[i].setVisible(b);
			}
		}
		@Override
		public int getItemHeight() {
			return fileElements[0].button.getItemHeight();
		}
		@Override
		public int getItemWidth() {
			return fileElements[0].button.getItemWidth()*fileElements.length + bufferSize*(fileElements.length-1);
		}
		@Override
		public void setWidth(int i) {
			int spacing = bufferSize*(fileElements.length-1);
			float indiv = i/((float)fileElements.length);
			elementWidth = indiv;
			for (int j = 0; j < fileElements.length; j++) {
				fileElements[j].button.setWidth((int) (j==0?Math.ceil(indiv):Math.floor(indiv)));
			}
			setPos(this.x, this.y);
		}
		@Override
		public void setHeight(int i) {
			for (int j = 0; j < fileElements.length; j++) {
				fileElements[j].button.setHeight(i);
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
		@Override
		public void onDraw(Gui g, int mouseX, int mouseY, float partialTicks) {
			for (int i = 0; i < fileElements.length; i++) {
				fileElements[i].button.onDraw(g, mouseX, mouseY, partialTicks);
			}
		}
		@Override
		public boolean onScroll(Gui gui, int i) {
			return false;
		}
		@Override
		public boolean onMouseClick(Gui gui, int x, int y, int buttonNum) {
			for (int i = 0; i < fileElements.length; i++) {
				if(fileElements[i].button.onMouseClick(gui, x, y, buttonNum))
					return true;
			};
			return false;
		}
		@Override
		public boolean onMouseRelease(Gui gui, int x, int y, int state) {
			for (int i = 0; i < fileElements.length; i++) {
				if(fileElements[i].button.onMouseRelease(gui, x, y, state))
					return true;
			};
			return false;
		}
		@Override
		public boolean onMouseClickMove(Gui gui, int x, int y, int buttonNum, long timeSinceClick) {
			for (int i = 0; i < fileElements.length; i++) {
				if(fileElements[i].button.onMouseClickMove(gui, x, y, buttonNum, timeSinceClick))
					return true;
			};
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

	public class FileElement {
		private final WidgetID widgetID = new WidgetID(610);

		private Property fileColorProp = new Property("colors.scriptBrowser2.file", Color.TEXT_f.toLuaValue(), "color.file", widgetID);
		private Property folderColorProp = new Property("colors.scriptBrowser2.folder", Color.TEXT_e.toLuaValue(), "color.folder", widgetID);

		File filePath;
		GuiButton button;
		public FileElement() {
			button = new GuiButton(widgetID, 5, 5, 12, 12, LuaValue.NIL, LuaValue.NIL, null, Color.BLACK, Color.WHITE, Color.WHITE);
			button.setOnClick((int mouseButton, GuiButton b)->{
				if(mouseButton == OnClickHandler.LMB) {
					if(ScriptBrowser2.this.selectedFile!=null && ScriptBrowser2.this.selectedFile.equals(filePath)) {
						//TODO open file in browser
					}else {
						ScriptBrowser2.this.selectedFile = filePath;
						filePreview.openScript(selectedFile.toString());  //FIXME Still not right, also prompt size
					}
				}else if(mouseButton == OnClickHandler.RMB) {
					if(ScriptBrowser2.this.promptType==null) {
						ScriptBrowser2.this.promptType=PromptType.FileAction;
						ScriptBrowser2.this.popupPrompt.promptChoice("Action:", FileActions.getActionList());
					}
				}
			});
		}

		public void update(File f) {
			this.filePath = f;
			if(f==null) {
				button.setVisible(false);
				return;
			}
			button.setVisible(true);
			if(f.isDirectory()) {
				button.setTextColor(Utils.parseColor(folderColorProp.getPropValue()));
			}else {
				button.setTextColor(Utils.parseColor(fileColorProp.getPropValue()));
			}
			button.setText(f.getName());
		}
		
		public void setVisible(boolean b) {
			button.setVisible(b && filePath!=null);
		}

		public GuiButton getButton() {
			return button;
		}
		public File getFilePath() {
			return filePath;
		}
	}

	private static enum PromptType {
		FileAction,
		FileName;
	}
	private static enum FileActions{
		Rename,Copy,Paste,DELETE;
		private static String[] nameArray;
		static {
			nameArray = new String[FileActions.values().length];
			FileActions[] ar = values();
			for (int i = 0; i<ar.length; i++) {
				nameArray[i] = ar[i].toString();
			}
		}
		public static String[] getActionList() {
			return nameArray;
		}
	}
}
