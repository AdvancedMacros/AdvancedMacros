package com.theincgi.advancedMacros.lua.scriptGui;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.gui.Color;
import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.gui.elements.GuiRect;
import com.theincgi.advancedMacros.hud.hud2D.Hud2D_Rectangle;

import net.minecraft.client.renderer.GlStateManager;

public class ScriptGuiText extends ScriptGuiElement{
	public int textSize = 12;
	private String text="";
	public ScriptGuiText(Gui gui, Group parent) {
		super(gui, parent);
		this.set("setTextSize", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				textSize = arg.checkint();
				return NONE;
			}
		});
		this.set("getWidth", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(getItemWidth());
			}
		});
		this.set("getHeight", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(getItemHeight());
			}
		});
		this.set("getText", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(text);
			}
		});
		this.set("setText", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				text = arg.checkjstring();
				return NONE;
			}
		});
		
	}
	
	@Override
	public void onDraw(Gui g, int mouseX, int mouseY, float partialTicks) {
		super.onDraw(g, mouseX, mouseY, partialTicks);
		if(!visible) return;
		GlStateManager.bindTexture(0);
		AdvancedMacros.customFontRenderer.renderText(x, y, z, text, color.getA(), textSize);
		if(getHoverTint()!=null && GuiRect.isInBounds(mouseX, mouseY, (int)x, (int)y, (int)wid, (int)hei)) {
			Hud2D_Rectangle.drawRectangle(x, y, wid, hei, getHoverTint(), z);
		}
	}
	
	@Override
	public int getItemHeight() {
		return (int) AdvancedMacros.customFontRenderer.measureHeight(text, textSize);
	}

	@Override
	public int getItemWidth() {
		return (int) AdvancedMacros.customFontRenderer.measureWidth(text, textSize);
	}

	@Override
	public void setWidth(int i) {
	}

	@Override
	public void setHeight(int i) {
	}
	
	public void setText(String text) {
		this.text = text;
		this.wid = getItemWidth();
		this.hei = getItemHeight();
	}
	public String getText() {
		return text;
	}
}
