package com.theincgi.advancedMacros.lua.scriptGui;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;

public class MCTextBar extends ScriptGuiElement{
	GuiTextField textField;
	public MCTextBar(Gui gui, Group parent) {
		super(gui, parent, true);
		enableSizeControl();
		textField = new GuiTextField(0, AdvancedMacros.getMinecraft().fontRenderer, 0, 0, 20, 20) {
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
		set("__class", "advancedMacros.GuiMinecraftTextField");
	}
	
	@Override
	public void onDraw(Gui g, int mouseX, int mouseY, float partialTicks) {
		super.onDraw(g, mouseX, mouseY, partialTicks);
		if(!visible) return;
		
		textField.drawTextBox();
	}
	
	@Override
	public int getItemHeight() {
		return textField.height;
	}
	@Override
	public int getItemWidth() {
		return textField.width;
	}
	@Override
	public void setWidth(int i) {
		textField.width = i;
	}
	@Override
	public void setHeight(int i) {
		textField.height = i;
	}
	
	public class TextFieldOp extends VarArgFunction{
		Op op;
		public TextFieldOp(Op op) {
			this.op = op;
		}
		@Override
		public Varargs invoke(Varargs args) {
			switch (op) {
			case cursorPos:
				return LuaValue.valueOf(textField.getCursorPosition()+1);
			case getSelRange:
				return LuaValue.valueOf(textField.getSelectionEnd() - textField.getCursorPosition());
			case getSelText:
				return LuaValue.valueOf(textField.getSelectedText());
			case getText:
				return LuaValue.valueOf(textField.getText());
			case isFocused:
				return LuaValue.valueOf(textField.isFocused());
			case maxStrLen:
				return LuaValue.valueOf(textField.getMaxStringLength());
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
				textField.setFocused(args.arg1().checkboolean());
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
		setTextColor;
		
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
	public boolean onMouseClick(Gui gui, int x, int y, int buttonNum) {
		if(textField.mouseClicked(x, y, buttonNum)) {
			if (onMouseClick != null) 
				Utils.pcall(onMouseClick, LuaValue.valueOf(x), LuaValue.valueOf(y), LuaValue.valueOf(buttonNum));
			textField.setFocused(true);
			return true;
		}
		return false;
	}
	
	
	@Override
	public boolean onKeyPressed(Gui gui, char typedChar, int keyCode) {
		if(!textField.isFocused() || !visible)
			return false;
		textField.textboxKeyTyped(typedChar, keyCode);
		if(onKeyPressed!=null)
			Utils.pcall(onKeyPressed, valueOf(typedChar), valueOf(keyCode));
		return true;
	}
	@Override
	public boolean onKeyRepeat(Gui gui, char typedChar, int keyCode, int repeatMod) {
		if(!textField.isFocused() || !visible)
			return false;
		textField.textboxKeyTyped(typedChar, keyCode);
		if(onKeyRepeated!=null)
			Utils.pcall(onKeyRepeated, valueOf(typedChar), valueOf(keyCode));
		return true;
	}
	
}
