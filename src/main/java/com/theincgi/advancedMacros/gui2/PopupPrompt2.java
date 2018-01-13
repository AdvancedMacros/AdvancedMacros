package com.theincgi.advancedMacros.gui2;

import org.luaj.vm2_v3_0_1.LuaValue;

import com.theincgi.advancedMacros.gui.Color;
import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.gui.elements.GuiButton;
import com.theincgi.advancedMacros.gui.elements.GuiDropDown;
import com.theincgi.advancedMacros.gui.elements.GuiRect;
import com.theincgi.advancedMacros.gui.elements.WidgetID;
import com.theincgi.advancedMacros.misc.Property;
import com.theincgi.advancedMacros.misc.Utils;

import javafx.scene.layout.Background;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;

public class PopupPrompt2 extends Gui{
	GuiRect background;
	GuiTextField textField;
	GuiDropDown choiceBox;
	GuiButton ok, cancel;
	static final WidgetID WIDGET_ID = new WidgetID(505);
	
	Property promptTextColor = new Property("colors.popupPrompt2.text", Color.WHITE.toLuaValue(), "msgColor", WIDGET_ID);
	
	private Result result;
	private Type type;
	private ResultHandler resultHandler;
	String msg;
	private Gui owner;

	public PopupPrompt2(Gui owner) {
		this.owner = owner;
		background = new GuiRect(WIDGET_ID, 5, 5, 12, 12, "popupPrompt2");
		textField = new GuiTextField(1, getFontRend(), 5, 5, 12, 12);
		choiceBox = new GuiDropDown(WIDGET_ID, 5, 5, 12, 12, 36, "popupPrompt2");
		ok = new GuiButton(WIDGET_ID, 5, 5, 12, 12, LuaValue.NIL, LuaValue.valueOf("Ok"), "popupPrompt2");
		cancel = new GuiButton(WIDGET_ID, 5, 5, 12, 12, LuaValue.NIL, LuaValue.valueOf("Cancel"), "popupPrompt2");

		ok.setOnClick((int mouseButton, GuiButton button)->{
			result = new Result();
			result.canceled = false;
			switch (type) {
			case Choice:
				result.result = choiceBox.getSelection();
				break;
			case Confirmation:
			case Notification:
				break;
			case Prompt:
				result.result = textField.getText();
				break;
			default:
				break;			
			}
			owner.setDrawDefaultBackground(true);
			resultHandler.onResult(result);
			
			returnToOwner();
		});
		cancel.setOnClick((int mouseButton, GuiButton button)->{
			result = new Result();
			result.canceled = true;
			resultHandler.onResult(result);
			owner.setDrawDefaultBackground(true);
			
			returnToOwner();
		});
		
		drawables.add(background);
		drawables.add(choiceBox);
		drawables.add(ok);
		drawables.add(cancel);
		
		inputSubscribers.add(ok);
		inputSubscribers.add(cancel);
		inputSubscribers.add(choiceBox);
	}
	
	private void returnToOwner() {
		Minecraft.getMinecraft().displayGuiScreen(owner);
	}

	public void setResultHandler(ResultHandler rh) {
		this.resultHandler = rh;
	}
	
	
	public Result getResult() {
		return result;
	}

	/**Msg + OK*/
	public synchronized void showNotification(String msg) {
		setWorldAndResolution(Minecraft.getMinecraft(), width, height);
		this.msg = msg;
		
		cancel.setVisible(false);
		choiceBox.setVisible(false);
		textField.setVisible(false);

		textField.setFocused(false);
		
		type = Type.Notification;
		Minecraft.getMinecraft().displayGuiScreen(this);
	}

	/**Msg + OK/Cancel*/
	public synchronized void showConfirmation(String msg) {
		setWorldAndResolution(Minecraft.getMinecraft(), width, height);
		owner.setDrawDefaultBackground(false);
		this.msg = msg;
		
		cancel.setVisible(true);
		choiceBox.setVisible(false);
		textField.setVisible(false);

		textField.setFocused(false);
		
		type = Type.Confirmation;
		Minecraft.getMinecraft().displayGuiScreen(this);
	}

	/**Msg + Text Input + OK/Cancel*/
	public synchronized void prompt(String prompt) {
		setWorldAndResolution(Minecraft.getMinecraft(), width, height);
		owner.setDrawDefaultBackground(false);
		this.msg = prompt;
		
		cancel.setVisible(true);
		choiceBox.setVisible(false);
		textField.setVisible(true);

		textField.setFocused(true);
		
		type = Type.Prompt;
		Minecraft.getMinecraft().displayGuiScreen(this);
	}

	/**Msg + ChoiceBox + OK/Cancel*/
	public synchronized void promptChoice(String prompt, String...options) {
		setWorldAndResolution(Minecraft.getMinecraft(), width, height);
		owner.setDrawDefaultBackground(false);
		this.msg = prompt;
		
		choiceBox.clear(false);
		for(String s : options)
			choiceBox.addOption(s);
		
		cancel.setVisible(true);
		choiceBox.setVisible(true);
		textField.setVisible(false);

		textField.setFocused(false);
		
		type = Type.Choice;
		Minecraft.getMinecraft().displayGuiScreen(this);
	}
	
	@Override
	public void setWorldAndResolution(Minecraft mc, int width, int height) {
		super.setWorldAndResolution(mc, width, height);
		owner.setDrawDefaultBackground(false);
		int prefWid = width/3;
		int prefHei = height/3;
		
		background.resize(prefWid, prefHei);
		background.setPos(prefWid, prefHei);
		
		textField.width = prefWid-2;
		textField.height = 20;
		
		choiceBox.setWidth(prefWid-2);
		choiceBox.setHeight(12);
		
		ok.setWidth((int) Math.ceil(prefWid/2));
		cancel.setWidth((int) Math.floor(prefWid/2));
		
		ok.setPos(prefWid, prefHei*2-12);
		cancel.setPos(prefWid+ok.getItemWidth(), prefHei*2-12);
		
		choiceBox.setPos(prefWid+1, ok.getY()-17);
		
		textField.x=prefWid+1;
		textField.y=ok.getY()-25;
		
		choiceBox.setMaxHeight(height - choiceBox.getY() - 12 - 5);
	}

	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		owner.drawScreen(mouseX, mouseY, partialTicks);
		super.drawScreen(mouseX, mouseY, partialTicks);
		textField.drawTextBox();
		getFontRend().drawString(msg, background.getX()+2, background.getY()+8, Utils.parseColor(promptTextColor.getPropValue()).toInt());
	}

	public class Result{
		String result;
		boolean canceled = false;
	}
	private static enum Type{
		Notification,
		Confirmation,
		Prompt,
		Choice;
	}
	@FunctionalInterface
	public static interface ResultHandler{
		public void onResult(Result r);
	}
}
