package com.theincgi.advancedmacros.lua.scriptGui;

import com.theincgi.advancedmacros.AdvancedMacros;
import com.theincgi.advancedmacros.access.ITextFieldWidget;
import com.theincgi.advancedmacros.gui.Gui;
import com.theincgi.advancedmacros.misc.HIDUtils;
import com.theincgi.advancedmacros.misc.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

public class MCTextBar extends ScriptGuiElement {

    TextFieldWidget textField;

    public MCTextBar(Gui gui, Group parent) {
        super(gui, parent, true);
        enableSizeControl();
        textField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, 20, 20, Text.literal("")) {
            @Override
            public boolean isFocused() {
                return gui.getFocusItem() == this; //not .equals, must be this object exactly
            }

            @Override
            public void setFocused(boolean isFocusedIn) {
                super.setFocused(isFocusedIn);
                gui.setFocusItem(isFocusedIn ? this : null);
            }
        };

        this.set("getCursorPos", new TextFieldOp(Op.cursorPos));
        this.set("getMaxLength", new TextFieldOp(Op.maxStrLen));
        this.set("getSelectedText", new TextFieldOp(Op.getSelText));
        this.set("getSelectionSize", new TextFieldOp(Op.getSelRange));
        this.set("getText", new TextFieldOp(Op.getText));
        this.set("isFocused", new TextFieldOp(Op.isFocused));
        this.set("setFocused", new TextFieldOp(Op.setFocused));
        this.set("setCursorPos", new TextFieldOp(Op.setCursorPos));
        this.set("setEnabled", new TextFieldOp(Op.setEnabled));
        this.set("setDisabledTextColor", new TextFieldOp(Op.setDisabledTxtColor));
        this.set("setMaxLength", new TextFieldOp(Op.setMaxStrLen));
        this.set("setSelectionSize", new TextFieldOp(Op.setSelection));
        this.set("setText", new TextFieldOp(Op.setText));
        this.set("setTextColor", new TextFieldOp(Op.setTextColor));
        this.set("enableBackground", new TextFieldOp(Op.enableBackground));
        set("__class", "advancedMacros.GuiMinecraftTextField");
    }

    @Override
    public void onDraw(DrawContext drawContext, Gui g, int mouseX, int mouseY, float partialTicks) {
        super.onDraw(drawContext, g, mouseX, mouseY, partialTicks);
        if (!visible) {
            return;
        }

        textField.render(drawContext, mouseX, mouseY, partialTicks);
    }

    @Override
    public int getItemHeight() {
        return textField.getHeight();
    }

    @Override
    public int getItemWidth() {
        return textField.getWidth();
    }

    @Override
    public void setWidth(int i) {
        textField.setWidth(i);
    }

    @Override
    public void setHeight(int i) {

    }

    public class TextFieldOp extends VarArgFunction {

        Op op;

        public TextFieldOp(Op op) {
            this.op = op;
        }

        @Override
        public Varargs invoke(Varargs args) {
            switch (op) {
                case cursorPos:
                    return LuaValue.valueOf(textField.getCursor() + 1);
                case getSelRange:
                    return LuaValue.valueOf(textField.getSelectedText().length());
                case getSelText:
                    return LuaValue.valueOf(textField.getSelectedText());
                case getText:
                    return LuaValue.valueOf(textField.getText());
                case isFocused:
                    return LuaValue.valueOf(textField.isFocused());
                case maxStrLen:
                    return LuaValue.valueOf(((ITextFieldWidget) textField).am_getMaxLength());
                case setCursorPos:
                    textField.setCursor(args.arg1().checkint(), false);
                    return NONE;
                case setDisabledTxtColor:
                    textField.setUneditableColor(Utils.parseColor(args, AdvancedMacros.COLOR_SPACE_IS_255).toInt());
                    return NONE;
                case setEnabled:
                    textField.setEditable(args.arg1().checkboolean());
                    return NONE;
                case setFocused:
                    textField.setFocused(args.arg1().checkboolean());
                    return NONE;
                case setMaxStrLen:
                    textField.setMaxLength(args.arg1().checkint());
                    return NONE;
                case setSelection:
                    textField.setSelectionEnd(textField.getCursor() + args.arg1().checkint());
                    return NONE;
                case setText:
                    textField.setText(args.arg1().checkjstring());
                    return NONE;
                case setTextColor:
                    textField.setEditableColor(Utils.parseColor(args, AdvancedMacros.COLOR_SPACE_IS_255).toInt());
                    return NONE;
                case enableBackground:
                    textField.setDrawsBackground(args.optboolean(1, true));
                    return NONE;
                default:
                    throw new LuaError("Unimplemented");
            }
        }

    }

    public static enum Op {
        cursorPos,
        maxStrLen,
        getSelText,
        getSelRange,
        getText,
        isFocused,
        setFocused,
        setCursorPos,
        setEnabled,
        setDisabledTxtColor,
        setMaxStrLen,
        setSelection,
        setText,
        setTextColor,
        enableBackground;

    }

    public void setText(String tojstring) {
        textField.setText(tojstring);
    }

    @Override
    public void setX(int x) {
        textField.setX(x);
    }

    @Override
    public void setX(double x) {
        setX((int) x);
    }

    @Override
    public void setY(int y) {
        textField.setY(y);
    }

    @Override
    public void setPos(int x, int y) {
        textField.setX(x);
        textField.setY(y);
    }

    @Override
    public void setY(double y) {
        setY((int) y);
    }

    @Override
    public int getX() {
        return textField.getX();
    }

    @Override
    public int getY() {
        return textField.getY();
    }

    @Override
    public boolean onMouseClick(Gui gui, double x, double y, int buttonNum) {
        if (textField.mouseClicked(x, y, buttonNum)) {
            if (onMouseClick != null) {
            	try(var reset = Utils.setMCThreadWorkspace(workspace)) {
            		Utils.pcall(onMouseClick, LuaValue.valueOf(x), LuaValue.valueOf(y), LuaValue.valueOf(buttonNum));
            	}
            }
            textField.setFocused(true);
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyPressed(Gui gui, int keyCode, int scanCode, int modifiers) {
        if (!textField.isFocused() || !visible) {
            return false;
        }
        textField.keyPressed(keyCode, scanCode, modifiers);
        if (onKeyPressed != null) {
        	try(var reset = Utils.setMCThreadWorkspace(workspace)) {
        		Utils.pcall(onKeyPressed, valueOf(HIDUtils.Keyboard.nameOf(keyCode)), valueOf(scanCode), HIDUtils.Keyboard.modifiersToLuaTable(modifiers));
        	}
        }
        return true;
    }

    @Override
    public boolean onCharTyped(Gui gui, char typedChar, int mods) {
        if (!textField.isFocused() || !visible) {
            return false;
        }
        textField.charTyped(typedChar, mods);
        if (onCharTyped != null) {
    		try(var reset = Utils.setMCThreadWorkspace(workspace)) {
    			Utils.pcall(onCharTyped, valueOf(typedChar), valueOf(typedChar), HIDUtils.Keyboard.modifiersToLuaTable(mods));
    		}
        }
        return true;
    }

}
