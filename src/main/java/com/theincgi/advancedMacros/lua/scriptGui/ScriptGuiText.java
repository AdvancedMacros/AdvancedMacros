package com.theincgi.advancedMacros.lua.scriptGui;

import java.util.Scanner;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;
import org.lwjgl.opengl.GL11;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.gui.elements.GuiButton;
import com.theincgi.advancedMacros.gui.elements.GuiRect;
import com.theincgi.advancedMacros.hud.hud2D.Hud2D_Rectangle;
import com.theincgi.advancedMacros.misc.FontRendererOverride;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.renderer.GlStateManager;

public class ScriptGuiText extends ScriptGuiElement{
	public int textSize = 12;
	private String text="";
	public boolean monospaced = true;
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
				if(!monospaced)
					text = Utils.toMinecraftColorCodes(text);
				return NONE;
			}
		});
		set("__class", "advancedMacros.GuiText");
	}
	
	@Override
	public void onDraw(Gui g, int mouseX, int mouseY, float partialTicks) {
		super.onDraw(g, mouseX, mouseY, partialTicks);
		if(!visible) return;
		
		GlStateManager.bindTexture(0);
		GlStateManager.color(1, 1, 1);
		//FontRenderer fr = AdvancedMacros.getMinecraft().fontRenderer;
		//FontRenderer fr;
		if(monospaced) {
			GlStateManager.enableBlend();
			GlStateManager.enableAlpha();
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			AdvancedMacros.customFontRenderer.renderText(x, y, z, text, color.getA(), textSize);
		}else {
			Scanner s = new Scanner(text);
			GlStateManager.enableBlend();
			GlStateManager.enableAlpha();
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			FontRendererOverride fr = AdvancedMacros.otherCustomFontRenderer;
			float old = fr.FONT_HEIGHT;
			//GlStateManager.bindTexture();
			fr.FONT_HEIGHT = (int)textSize;
			for(int i = 0; s.hasNextLine(); i+=textSize)
				fr.renderText(x, y+i, z, s.nextLine(), color.getA(), textSize);//(text, (int)x, (int)y, color.toInt());
			fr.FONT_HEIGHT = old;
			s.close();
		}
		
		//fr.drawString(text, (int)x, (int)y, color.toInt());
		
		//AdvancedMacros.customFontRenderer.renderText(x, y, z, text, color.getA(), textSize);
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
		if(!monospaced)
			text = Utils.toMinecraftColorCodes(text);
		this.wid = getItemWidth();
		this.hei = getItemHeight();
	}
	public String getText() {
		return text;
	}
	
	@Override
	public boolean onMouseClick(Gui gui, int x, int y, int buttonNum) {
		if(onMouseClick != null && GuiButton.isInBounds(x, y, (int)this.x, (int)this.y, (int)getItemWidth(), (int)getItemHeight()))
			return Utils.pcall(onMouseClick, LuaValue.valueOf(x), LuaValue.valueOf(y), LuaValue.valueOf(buttonNum)).toboolean();
		return false;
	}

	@Override
	public boolean onMouseRelease(Gui gui, int x, int y, int state) {
		if(onMouseRelease!=null && GuiButton.isInBounds(x, y, (int)this.x, (int)this.y, (int)getItemWidth(), (int)getItemHeight()))
			return Utils.pcall(onMouseRelease, LuaValue.valueOf(x), LuaValue.valueOf(y), LuaValue.valueOf(state)).toboolean();
		return false;
	}

	@Override
	public boolean onMouseClickMove(Gui gui, int x, int y, int buttonNum, long timeSinceClick) {
		if(onMouseDrag!=null && GuiButton.isInBounds(x, y, (int)this.x, (int)this.y, (int)getItemWidth(), (int)getItemHeight())) {
			LuaTable args = new LuaTable();
			args.set(1, LuaValue.valueOf(x));
			args.set(2, LuaValue.valueOf(y));
			args.set(3, LuaValue.valueOf(buttonNum));
			args.set(4, LuaValue.valueOf(timeSinceClick));
			return Utils.pcall(onMouseDrag,args.unpack()).toboolean();
		}
		return false;
	}
}
