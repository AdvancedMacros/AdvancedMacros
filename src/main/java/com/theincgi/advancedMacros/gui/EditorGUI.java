package com.theincgi.advancedMacros.gui;

import java.io.IOException;

import org.luaj.vm2_v3_0_1.LuaValue;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.ForgeEventHandler;
import com.theincgi.advancedMacros.gui.elements.ColorTextArea;
import com.theincgi.advancedMacros.gui.elements.GuiButton;
import com.theincgi.advancedMacros.gui.elements.OnClickHandler;
import com.theincgi.advancedMacros.gui.elements.WidgetID;
import com.theincgi.advancedMacros.gui2.ScriptBrowser2;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;

public class EditorGUI extends Gui{

	ColorTextArea cta = new ColorTextArea(new WidgetID(300), this);
	GuiTextField gtf;
	GuiButton save, exit, quickRun;
	
	//TODO autosave option property for run
	
	//TODO Help bar
	public void postInit() {
		gtf.setFocused(false); //FIXME somehow this causes an error with inventory tweaks.... idk how
	}
	
	public EditorGUI() {
		inputSubscribers.add(cta);
		gtf = new GuiTextField(0, getFontRend(), 6, 2, width/3, 20);
		gtf.setCanLoseFocus(true);
		
		cta.setFocused(true);
		save 	= new GuiButton(new WidgetID(301), 7+width/3, 				2	, width/4, 10, LuaValue.NIL, LuaValue.valueOf("Save"), "editor.save", Color.BLACK, Color.TEXT_8, Color.WHITE);
		exit 	= new GuiButton(new WidgetID(302), 7+width/3, 				12	, width/4, 10, LuaValue.NIL, LuaValue.valueOf("Exit"), "editor.exit", Color.BLACK, Color.TEXT_8, Color.WHITE);
		quickRun= new GuiButton(new WidgetID(303), save.getX()+save.getWid(),12	, width/8, 10, LuaValue.NIL, LuaValue.valueOf("Run"), "editor.run", Color.BLACK, Color.TEXT_8, Color.WHITE);
		inputSubscribers.add(save);
		inputSubscribers.add(exit);
		inputSubscribers.add(quickRun);
		save.setOnClick(new OnClickHandler() {
			@Override
			public void onClick(int button, GuiButton sButton) {
				cta.save();
				updateKeywords();
			}
		});
		exit.setOnClick(new OnClickHandler() {
			@Override
			public void onClick(int button, GuiButton sButton) {
				if(cta.isNeedsSave() && !ColorTextArea.isCTRLDown()){
					//not gonna do anything
				}else{
					//either overridden for exit or saved, so all good
					ForgeEventHandler.showPrevMenu();
				}
			}
		});
		quickRun.setOnClick(new OnClickHandler() {//TODO shift to save and run? or run with args?
			@Override
			public void onClick(int button, GuiButton sButton) {
				save.getOnClickHandler().onClick(button, save);
				ForgeEventHandler.closeMenu();
				AdvancedMacros.runScript(ScriptBrowser2.getScriptPath(cta.getScriptFile()));
			}
		});
		
//		cta.setOnNeedsSaveChanged(new Runnable() {
//			@Override
//			public void run() {
//				if(cta.isNeedsSave())
//					exit.setFill(Color.TEXT_4);
//				else
//					exit.setFill(Color.TEXT_2);
//			}
//		});
	}
	
	public ColorTextArea getCta() {
		return cta;
	}
	
	public void runScriptFromEditor() {
		quickRun.getOnClickHandler().onClick(0, save);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if(cta.isNeedsSave()){
			if(ColorTextArea.isCTRLDown()) {
				exit.setFill(Color.TEXT_6);//Orange
			}else {
				exit.setFill(Color.TEXT_4);//Red
			}
		}else{
			exit.setFill(Color.TEXT_2);//Green
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		gtf.drawTextBox();
		save.onDraw(this, mouseX, mouseY, partialTicks);
		exit.onDraw(this, mouseX, mouseY, partialTicks);
		quickRun.onDraw(this, mouseX, mouseY, partialTicks);
		cta.onDraw(this, mouseX, mouseY, partialTicks);
		this.getFontRend().drawString(String.format("%3d, %4d", cta.getCursor().getX()+1, cta.getCursor().getY()+1), (int) (8+width*7/8f), 5, Color.WHITE.toInt());
	}

	
	public void updateKeywords(){
		ColorTextArea.updateKeywords();
	}
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		gtf.mouseClicked(mouseX, mouseY, mouseButton);
		cta.setFocused(!gtf.isFocused());
	}
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		if(gtf.isFocused()){
			gtf.textboxKeyTyped(typedChar, keyCode);
			//TODO what to do if the text box is updated here
		}
	}
	
	@Override
	public void setWorldAndResolution(Minecraft mc, int width, int height) {
		super.setWorldAndResolution(mc, width, height);
		gtf.width = width/3;
		save.setWidth(width/4);
		exit.setWidth(width/4);
		quickRun.setWidth(width/8);
		save.setPos(7+width/3, 2);
		exit.setPos(7+width/3, 12);
		quickRun.setPos(save.getX()+save.getWid(), 12);
		cta.setPos(5, gtf.y+gtf.height+1);
		cta.resize(width-10, height-6-gtf.height);
		
	}
	public void openScript(String sScript) {
		cta.openScript(sScript);
		gtf.setText(sScript);
		gtf.setCursorPosition(0);
	}

	
}