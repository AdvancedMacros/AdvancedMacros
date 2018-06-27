package com.theincgi.advancedMacros.lua.scriptGui;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.gui.elements.ColorTextArea;
import com.theincgi.advancedMacros.gui.elements.WidgetID;

/**
 * Script controled color text area
 * */
public class GuiCTA extends ScriptGuiElement{
	ColorTextArea cta;
	public GuiCTA(Gui gui, Group parent) {
		super(gui, parent);
		cta = new ColorTextArea(AdvancedMacros.editorGUI.getCta().getWidgetID(), gui);
		enableSizeControl();
		
		//set("setFontSize")
		set("setText", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				cta.setText(arg.checkjstring());
				return NONE;
			}
		});
		set("getText", new ZeroArgFunction() {
			@Override public LuaValue call() {
				return valueOf(cta.getText());
			}
		});
		set("setSyntaxHighlight", new OneArgFunction() {
			@Override public LuaValue call(LuaValue arg) {
				cta.doSyntaxHighlighting = arg.checkboolean();
				return NONE;
			}
		});
		set("isSyntaxHighlight", new ZeroArgFunction() {
			@Override public LuaValue call() {
				return valueOf(cta.doSyntaxHighlighting);
			}
		});
	}
	
	@Override
	public void onDraw(Gui g, int mouseX, int mouseY, float partialTicks) {
		super.onDraw(g, mouseX, mouseY, partialTicks);
		if(!visible) return;
		cta.onDraw(g, mouseX, mouseY, partialTicks);
	}

	@Override
	public int getItemHeight() {
		return cta.getItemHeight();
	}

	@Override
	public int getItemWidth() {
		return cta.getItemWidth();
	}

	@Override
	public void setWidth(int i) {
		cta.setWidth(i);
	}

	@Override
	public void setHeight(int i) {
		cta.setHeight(i);
	}
	
}
