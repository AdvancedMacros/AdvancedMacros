package com.theincgi.advancedmacros.lua.scriptGui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theincgi.advancedmacros.AdvancedMacros;
import com.theincgi.advancedmacros.gui.Gui;
import com.theincgi.advancedmacros.gui.elements.GuiButton;
import com.theincgi.advancedmacros.gui.elements.GuiRect;
import com.theincgi.advancedmacros.hud.hud2D.Hud2D_Rectangle;
import com.theincgi.advancedmacros.misc.CustomFontRenderer;
import com.theincgi.advancedmacros.misc.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import java.util.Scanner;

public class ScriptGuiText extends ScriptGuiElement {

    public int textSize = 12;
    private String text = "";
    public boolean monospaced = false;

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
                if (!monospaced) {
                    text = Utils.toMinecraftColorCodes(text);
                }
                return NONE;
            }
        });
        set("__class", "advancedMacros.GuiText");
    }

    @Override
    public void onDraw(DrawContext drawContext, Gui g, int mouseX, int mouseY, float partialTicks) {
        super.onDraw(drawContext, g, mouseX, mouseY, partialTicks);
        if (!visible) {
            return;
        }

        RenderSystem.bindTexture(0);
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        //RenderSystem.enableAlphaTest();
        RenderSystem.defaultBlendFunc();
        //FontRenderer fr = MinecraftClient.getInstance().fontRenderer;
        //FontRenderer fr;
        if (monospaced) {
            AdvancedMacros.CUSTOM_FONT_RENDERER.renderText(x, y, z, text, color.getA(), textSize);
        } else {
            Scanner s = new Scanner(text);
            TextRenderer fr = MinecraftClient.getInstance().textRenderer; //AdvancedMacros.otherCustomFontRenderer;
            MatrixStack matrixStack = drawContext.getMatrices();

            for (int i = 0; s.hasNextLine(); i += textSize) {
                matrixStack.push();
                matrixStack.translate(0, 0, z);
                drawContext.drawText(fr, Text.literal(s.nextLine()), (int) x, (int) y + i, color.toInt(), false);
                matrixStack.pop();
            }
            s.close();
        }

        //fr.drawString(text, (int)x, (int)y, color.toInt());

        //AdvancedMacros.customFontRenderer.renderText(x, y, z, text, color.getA(), textSize);
        if (getHoverTint() != null && GuiRect.isInBounds(mouseX, mouseY, (int) x, (int) y, (int) wid, (int) hei)) {
            Hud2D_Rectangle.drawRectangle(x, y, wid, hei, getHoverTint(), z);
        }
    }

    @Override
    public int getItemHeight() {
        return (int) CustomFontRenderer.measureHeight(text, textSize);
    }

    @Override
    public int getItemWidth() {
        return (int) CustomFontRenderer.measureWidth(text, textSize);
    }

    @Override
    public void setWidth(int i) {
    }

    @Override
    public void setHeight(int i) {
    }

    public void setText(String text) {
        this.text = text;
        if (!monospaced) {
            text = Utils.toMinecraftColorCodes(text);
        }
        this.wid = getItemWidth();
        this.hei = getItemHeight();
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean onMouseClick(Gui gui, double x, double y, int buttonNum) {
        if (onMouseClick != null && GuiButton.isInBounds(x, y, (int) this.x, (int) this.y, (int) getItemWidth(), (int) getItemHeight())) {
            return Utils.pcall(onMouseClick, LuaValue.valueOf(x), LuaValue.valueOf(y), LuaValue.valueOf(buttonNum)).toboolean();
        }
        return false;
    }

    @Override
    public boolean onMouseRelease(Gui gui, double x, double y, int state) {
        if (onMouseRelease != null && GuiButton.isInBounds(x, y, (int) this.x, (int) this.y, (int) getItemWidth(), (int) getItemHeight())) {
            return Utils.pcall(onMouseRelease, LuaValue.valueOf(x), LuaValue.valueOf(y), LuaValue.valueOf(state)).toboolean();
        }
        return false;
    }

    @Override
    public boolean onMouseClickMove(Gui gui, double x, double y, int buttonNum, double q, double r) {
        if (onMouseDrag != null && GuiButton.isInBounds(x, y, (int) this.x, (int) this.y, (int) getItemWidth(), (int) getItemHeight())) {
            LuaTable args = new LuaTable();
            args.set(1, x);
            args.set(2, y);
            args.set(3, buttonNum);
            args.set(4, q);
            args.set(5, r);
            return Utils.pcall(onMouseDrag, args.unpack()).toboolean();
        }
        return false;
    }

}
