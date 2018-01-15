package com.theincgi.advancedMacros.gui2;

import java.io.IOException;

import org.luaj.vm2_v3_0_1.LuaValue;

import com.theincgi.advancedMacros.gui.Color;
import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.gui.elements.GuiButton;
import com.theincgi.advancedMacros.gui.elements.GuiDropDown;
import com.theincgi.advancedMacros.gui.elements.GuiRect;
import com.theincgi.advancedMacros.gui.elements.WidgetID;
import com.theincgi.advancedMacros.misc.Property;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;

public class PopupPrompt2 extends Gui{
	GuiRect background;
	GuiTextField textField;
	GuiDropDown choiceBox;
	GuiButton ok, cancel;
	//static final long baseWidgetID = 500;
	
	Property promptTextColor = new Property("colors.popupPrompt2.text", Color.WHITE.toLuaValue(), "msgColor", new WidgetID(504));
	
	private Result result;
	private Type type;
	private ResultHandler resultHandler;
	String msg;
	private Gui owner;

	public PopupPrompt2(Gui owner) {
		this.owner = owner;
		background = new GuiRect(new WidgetID(500), 5, 5, 12, 12, "colors.popupPrompt2.background", Color.BLACK, Color.WHITE);
		textField = new GuiTextField(1, getFontRend(), 5, 5, 12, 12);
		choiceBox = new GuiDropDown(new WidgetID(501), 5, 5, 12, 12, 36, "colors.popupPrompt2");
		ok = new GuiButton(new WidgetID(502), 5, 5, 12, 12, LuaValue.NIL, LuaValue.valueOf("Ok"), "colors.popupPrompt2.ok");
		cancel = new GuiButton(new WidgetID(503), 5, 5, 12, 12, LuaValue.NIL, LuaValue.valueOf("Cancel"), "colors.popupPrompt2.cancel");

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
			if(!resultHandler.onResult(result)) return;
			
			returnToOwner();
		});
		//ok.setImg(null);
		cancel.setOnClick((int mouseButton, GuiButton button)->{
			result = new Result();
			result.canceled = true;
			resultHandler.onResult(result);
			owner.setDrawDefaultBackground(true);
			
			if(!resultHandler.onResult(result)) return;
			returnToOwner();
		});
		//cancel.setImg(null);
		
		drawables.add(background);
		drawables.add(choiceBox);
		drawables.add(ok);
		drawables.add(cancel);
		
		inputSubscribers.add(choiceBox);
		inputSubscribers.add(ok);
		inputSubscribers.add(cancel);
		
//		
//		choiceBox.setOnOpen((int button, GuiButton sButton) -> {
//			PopupPrompt2.this.nextKeyListen = choiceBox; 
//		});
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
		textField.setText("");
	//	System.out.println(background.getFrame());
		
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
		
		choiceBox.select(options[0]);
		
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
		
		textField.width = prefWid-3;
		textField.height = 20;
		
		choiceBox.setWidth(prefWid-2);
		choiceBox.setHeight(12);
		
		ok.setWidth((int) Math.ceil(prefWid/2));
		cancel.setWidth((int) Math.floor(prefWid/2));
		
		ok.setPos(prefWid, prefHei*2-12);
		cancel.setPos(prefWid+ok.getItemWidth(), prefHei*2-12);
		
		choiceBox.setPos(prefWid+1, ok.getY()-17);
		
		textField.x=prefWid+2;
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
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		if(textField.isFocused())
			textField.textboxKeyTyped(typedChar, keyCode);
	}
	@Override
	public void keyRepeated(char typedChar, int keyCode, int mod) {
		super.keyRepeated(typedChar, keyCode, mod);
		if(mod%5==0 && textField.isFocused())
			textField.textboxKeyTyped(typedChar, keyCode);
	}

	public static class Result{
		String result;
		boolean canceled = false;
		public String getResult() {
			return result;
		}
		public boolean isCanceled() {
			return canceled;
		}
	}
	private static enum Type{
		Notification,
		Confirmation,
		Prompt,
		Choice;
	}
	@FunctionalInterface
	public static interface ResultHandler{
		public boolean onResult(Result r);
	}
}
