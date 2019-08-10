package com.theincgi.advancedMacros.lua.scriptGui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.misc.HIDUtils;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class MCTextBar extends ScriptGuiElement{
	TextFieldWidget textField;
	public MCTextBar(Gui gui, Group parent) {
		super(gui, parent, true);
		enableSizeControl();
		textField = new TextFieldWidget(AdvancedMacros.getMinecraft().fontRenderer, 0, 0, 20, 20, "") {
			@Override
			public boolean isFocused() {
				return gui.getFocusItem()==this; //not .equals, must be this object exactly
			}
			@Override
			public void setFocused(boolean isFocusedIn) {
				super.setFocused(isFocusedIn);
				gui.setFocusItem(isFocusedIn? this : null);
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
	public void onDraw(Gui g, int mouseX, int mouseY, float partialTicks) {
		super.onDraw(g, mouseX, mouseY, partialTicks);
		if(!visible) return;
		
		textField.render(mouseX, mouseY, partialTicks);
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
		textField.setHeight(i);
	}
	
	
	private static Method getMaxStringLength;
	public class TextFieldOp extends VarArgFunction{
		Op op;
		public TextFieldOp(Op op) {
			this.op = op;
			if(getMaxStringLength==null) {
				getMaxStringLength = ObfuscationReflectionHelper.findMethod(TextFieldWidget.class, "func_146208_g");
				getMaxStringLength.setAccessible(true);
			}
		}
		@Override
		public Varargs invoke(Varargs args) {
			switch (op) {
			case cursorPos:
				return LuaValue.valueOf(textField.getCursorPosition()+1);
			case getSelRange:
				return LuaValue.valueOf(textField.getSelectedText().length());
			case getSelText:
				return LuaValue.valueOf(textField.getSelectedText());
			case getText:
				return LuaValue.valueOf(textField.getText());
			case isFocused:
				return LuaValue.valueOf(textField.isFocused());
			case maxStrLen:
				try {
					return LuaValue.valueOf((int)getMaxStringLength.invoke(textField));
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new LuaError(e); //TESTME
				}
			case setCursorPos:
				textField.setCursorPosition(args.arg1().checkint());
				return NONE;
			case setDisabledTxtColor:
				textField.setDisabledTextColour(Utils.parseColor(args, AdvancedMacros.COLOR_SPACE_IS_255).toInt());
				return NONE;
			case setEnabled:
				textField.setEnabled(args.arg1().checkboolean());
				return NONE;
			case setFocused:
				textField.setFocused2(args.arg1().checkboolean());
				return NONE;
			case setMaxStrLen:
				textField.setMaxStringLength(args.arg1().checkint());
				return NONE;
			case setSelection:
				textField.setSelectionPos(textField.getCursorPosition() + args.arg1().checkint());
				return NONE;
			case setText:
				textField.setText(args.arg1().checkjstring());
				return NONE;
			case setTextColor:
				textField.setTextColor(Utils.parseColor(args, AdvancedMacros.COLOR_SPACE_IS_255).toInt());
				return NONE;
			case enableBackground:
				textField.setEnableBackgroundDrawing(args.optboolean(1, true));
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
		textField.x = x;
	}
	@Override
	public void setX(double x) {
		setX((int)x);
	}
	@Override
	public void setY(int y) {
		textField.y = y;
	}
	@Override
	public void setPos(int x, int y) {
		textField.x = x;
		textField.y = y;
	}
	@Override
	public void setY(double y) {
		setY((int)y);
	}
	
	@Override
	public int getX() {
		return textField.x;
	}
	@Override
	public int getY() {
		return textField.y;
	}
	
	@Override
	public boolean onMouseClick(Gui gui, double x, double y, int buttonNum) {
		if(textField.mouseClicked(x, y, buttonNum)) {
			if (onMouseClick != null) 
				Utils.pcall(onMouseClick, LuaValue.valueOf(x), LuaValue.valueOf(y), LuaValue.valueOf(buttonNum));
			textField.setFocused2(true);
			return true;
		}
		return false;
	}
	
	
	@Override
	public boolean onKeyPressed(Gui gui, int keyCode, int scanCode, int modifiers) {
		return false;
	}
	@Override
	public boolean onCharTyped(Gui gui, char typedChar, int mods) {
		if(!textField.isFocused() || !visible)
			return false;
		textField.charTyped(typedChar, mods);
		if(onCharTyped!=null)
			Utils.pcall(onCharTyped, valueOf(typedChar), valueOf(typedChar), HIDUtils.Keyboard.modifiersToLuaTable(mods));
		return true;
	}
	
}
