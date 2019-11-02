package com.theincgi.advancedMacros.gui2;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.lwjgl.glfw.GLFW;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.gui.Color;
import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.gui.elements.GuiButton;
import com.theincgi.advancedMacros.gui.elements.GuiDropDown;
import com.theincgi.advancedMacros.gui.elements.GuiRect;
import com.theincgi.advancedMacros.misc.PropertyPalette;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class PopupPrompt2 extends Gui{
	GuiRect background;
	TextFieldWidget textField;
	GuiDropDown choiceBox;
	GuiButton ok, cancel;
	//static final long baseWidgetID = 500;
	
	//Property promptTextColor = new Property("colors.popupPrompt2.text", Color.WHITE.toLuaValue(), "msgColor", new WidgetID(504));
	PropertyPalette propertyPalette;
	private Result result;
	private Type type;
	private ResultHandler resultHandler;
	private StringTextComponent title;
	String msg;
	private Gui owner;

	public PopupPrompt2(Gui owner, PropertyPalette propPal) {
		this.owner = owner;
		this.propertyPalette = propPal;
		propPal.addColorIfNil(Color.WHITE, "popupPrompt", "colors", "text");
		//background = new GuiRect(new WidgetID(500), 5, 5, 12, 12, "colors.popupPrompt2.background", Color.BLACK, Color.WHITE);
		background = new GuiRect(5, 5, 12, 12, "popupPrompt", "background");
		textField = new TextFieldWidget(getFontRend(), 5, 5, 12, 12, "");
		//choiceBox = new GuiDropDown(new WidgetID(501), 5, 5, 12, 12, 36, "colors.popupPrompt2");
		choiceBox = new GuiDropDown(5, 5, 12, 12, 36, "popupPrompt", "choiceBox");
		//ok = new GuiButton(new WidgetID(502), 5, 5, 12, 12, LuaValue.NIL, LuaValue.valueOf("Ok"), "colors.popupPrompt2.ok");
		ok = new GuiButton(5, 5, 12, 12, LuaValue.NIL, LuaValue.valueOf("Ok"), "popupPrompt","okButton");
		//cancel = new GuiButton(new WidgetID(503), 5, 5, 12, 12, LuaValue.NIL, LuaValue.valueOf("Cancel"), "colors.popupPrompt2.cancel");
		cancel = new GuiButton(5, 5, 12, 12, LuaValue.NIL, LuaValue.valueOf("Cancel"), "popupPrompt", "cancelButton");
		title = new StringTextComponent("Prompt");
		
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
		
		addDrawable(background);
		addDrawable(choiceBox);
		addDrawable(ok);
		addDrawable(cancel);
		
		addInputSubscriber(choiceBox);
		addInputSubscriber(ok);
		addInputSubscriber(cancel);
		
//		
//		choiceBox.setOnOpen((int button, GuiButton sButton) -> {
//			PopupPrompt2.this.nextKeyListen = choiceBox; 
//		});
	}
	
	private void returnToOwner() {
		AdvancedMacros.getMinecraft().displayGuiScreen(owner);
	}

	public void setResultHandler(ResultHandler rh) {
		this.resultHandler = rh;
	}
	
	
	public Result getResult() {
		return result;
	}

	/**Msg + OK*/
	public synchronized void showNotification(String msg) {
		resize(AdvancedMacros.getMinecraft(), width, height);
		this.msg = msg;
		
		cancel.setVisible(false);
		choiceBox.setVisible(false);
		textField.setVisible(false);

		textField.setFocused2(false);
		
		type = Type.Notification;
		AdvancedMacros.getMinecraft().displayGuiScreen(this);
	}

	/**Msg + OK/Cancel*/
	public synchronized void showConfirmation(String msg) {
		resize(AdvancedMacros.getMinecraft(), width, height);
		owner.setDrawDefaultBackground(false);
		this.msg = msg;
		
		cancel.setVisible(true);
		choiceBox.setVisible(false);
		textField.setVisible(false);

		textField.setFocused2(false);
		
		type = Type.Confirmation;
		AdvancedMacros.getMinecraft().displayGuiScreen(this);
	}

	/**Msg + Text Input + OK/Cancel*/
	public synchronized void prompt(String prompt) {
		title = new StringTextComponent(prompt);
		resize(AdvancedMacros.getMinecraft(), width, height);
		owner.setDrawDefaultBackground(false);
		this.msg = prompt;
		
		cancel.setVisible(true);
		choiceBox.setVisible(false);
		textField.setVisible(true);

		textField.setFocused2(true);
		textField.setText("");
	//	System.out.println(background.getFrame());
		
		type = Type.Prompt;
		AdvancedMacros.getMinecraft().displayGuiScreen(this);
	}
	
	@Override
	public ITextComponent getTitle() {
		return title;
	}

	/**Msg + ChoiceBox + OK/Cancel*/
	public synchronized void promptChoice(String prompt, String...options) {
		title = new StringTextComponent(prompt);
		resize(AdvancedMacros.getMinecraft(), width, height);
		owner.setDrawDefaultBackground(false);
		this.msg = prompt;
		
		choiceBox.clear(false);
		for(String s : options)
			choiceBox.addOption(s);
		
		cancel.setVisible(true);
		choiceBox.setVisible(true);
		textField.setVisible(false);

		textField.setFocused2(false);
		
		choiceBox.select(options[0]);
		
		type = Type.Choice;
		AdvancedMacros.getMinecraft().displayGuiScreen(this);
	}
	
	@Override
	public void resize(Minecraft mc, int width, int height) {
		super.resize(mc, width, height);
		owner.setDrawDefaultBackground(false);
		int prefWid = width/3;
		int prefHei = height/3;
		
		background.resize(prefWid, prefHei);
		background.setPos(prefWid, prefHei);
		
		textField.setWidth( prefWid-3 );
		textField.setHeight( 20 );
		
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
	public void render(int mouseX, int mouseY, float partialTicks) {
		owner.render(mouseX, mouseY, partialTicks);
		super.render(mouseX, mouseY, partialTicks);
		textField.render(mouseX, mouseY, partialTicks);
		
		getFontRend().drawString(msg, background.getX()+2, background.getY()+8, propertyPalette.getColor("popupPrompt", "colors", "text").toInt());
	}
	
	@Override
	public boolean charTyped(char typedChar, int modifiers) {
		if(super.charTyped(typedChar, modifiers)) return true;
		if(textField.isFocused())
			if(textField.charTyped(typedChar, modifiers)) return true;
		return false;
	}
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if(textField.isFocused())
			if(keyCode == GLFW.GLFW_KEY_ENTER) {
				ok.getOnClickHandler().onClick(-1, ok);
				return true;
			}else {
				if(textField.keyPressed(keyCode, scanCode, modifiers)) return true;
			}
		return false;
	}
//	@Override
//	public boolean keyRepeated(char typedChar, int keyCode, int mod) {
//		super.keyRepeated(typedChar, keyCode, mod);
//		if(mod%5==0 && textField.isFocused())
//			textField.charTyped(typedChar, keyCode);
//		return textField.isFocused();
//	}

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
