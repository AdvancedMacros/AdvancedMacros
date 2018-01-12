package com.theincgi.advancedMacros.gui;

import java.io.IOException;
import java.util.List;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.gui.elements.Drawable;
import com.theincgi.advancedMacros.gui.elements.GuiRect;
import com.theincgi.advancedMacros.gui.elements.ListManager;
import com.theincgi.advancedMacros.gui.elements.Moveable;
import com.theincgi.advancedMacros.gui.elements.WidgetID;
import com.theincgi.advancedMacros.lua.LuaDebug;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public class InputGUI extends Gui{
	private Thread threadCheck;
	InputType inputType;
	private LuaDebug debug;
	GuiTextField textInput = new GuiTextField(0, fontRend, 5, 5, 30, 12);
	private IForgeRegistry<Item> blah = GameRegistry.findRegistry(Item.class);
	private List<Item> itemList = blah.getValues();
	private final int WHITE = Color.WHITE.toInt();
	private String prompt;
	private boolean answered = true;
	private LuaValue answer = LuaValue.NIL;
	private ListManager listItemPicker = new ListManager(5, 19, 30, 30, new WidgetID(800), "colors.promptGUI");
	private ListManager choices = new ListManager(5, 19, 30, 30, new WidgetID(800), "colors.promptGUI");
	private static ItemRenderer itemRenderer = Minecraft.getMinecraft().getItemRenderer();
	public InputGUI(LuaDebug debug) {
		this.debug = debug;
		textInput.height=12;
		listItemPicker.setDrawBG(false);
		listItemPicker.setAlwaysShowScroll(true);
		listItemPicker.setSpacing(3);
		for(int i = 0; i<itemList.size(); i++) {
			Item item = itemList.get(i);
			//argument for maxDamage is literally discarded
			//for(int d = 0; d<item.getMaxDamage(ItemStack.EMPTY); d++) {
				listItemPicker.add(new ItemOption(new ItemStack(item, 1, 0)));
			//}
		}
		inputSubscribers.add(listItemPicker);
		inputSubscribers.add(choices);
		listItemPicker.setScrollSpeed(10);
		choices.setScrollSpeed(10);
		choices.setSpacing(3);
	}

	public void setInputType(InputType inputType, String prompt) {
		synchronized (this) {
			if(threadCheck==null) {
				threadCheck = Thread.currentThread();
				this.inputType = inputType;
				this.prompt = prompt;
				answered=false;
				textInput.setFocused(inputType==InputType.TEXT);
				textInput.setVisible(inputType==InputType.TEXT);
				textInput.setText("");
				listItemPicker.setVisible(inputType==InputType.ITEM);
				
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		switch (inputType) {
		case TEXT:
			drawRect(1, height-49, width-1, height-1, 0xDD000000);
			int drawHei = height-5;
			textInput.y=(drawHei-=12);
			drawHei-=5;
			drawHei-=getFontRend().FONT_HEIGHT;
			getFontRend().drawString(prompt, 5, drawHei, WHITE);
			drawHei-=5;
			drawHei-=getFontRend().FONT_HEIGHT;
			getFontRend().drawString(debug.getLabel(threadCheck), 5, drawHei, WHITE);

			textInput.drawTextBox();
			break;
		case ITEM:
			getFontRend().drawString(debug.getLabel(threadCheck), 5, 5, WHITE);
			getFontRend().drawString(prompt, 5, 15, WHITE);
			listItemPicker.onDraw(this, mouseX, mouseY, partialTicks);
			break;
		case CHOICE:
			getFontRend().drawString(debug.getLabel(threadCheck), 5, 5, WHITE);
			getFontRend().drawString(prompt, 5, 15, WHITE);
			choices.onDraw(this, mouseX, mouseY, partialTicks);
			break;
		default:
			break;
		}	
	}
	@Override
	public void setWorldAndResolution(Minecraft mc, int width, int height) {
		super.setWorldAndResolution(mc, width, height);
		textInput.width=width-10;
		listItemPicker.setPos(5, 25);
		listItemPicker.setWidth(width/2-10);
		listItemPicker.setHeight(height-60);
		choices.setPos(5, 25);
		choices.setWidth(width/2-10);
		choices.setHeight(height-60);
		
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		if(keyCode==Keyboard.KEY_ESCAPE) {
			close(LuaValue.NIL);
		}else if(inputType==InputType.TEXT) {
			if(keyCode==Keyboard.KEY_RETURN) {
				close(LuaValue.valueOf(textInput.getText()));
				return;
			}

			textInput.textboxKeyTyped(typedChar, keyCode);
		}
	}
	@Override
	public void keyRepeated(char typedChar, int keyCode, int mod) {
		super.keyRepeated(typedChar, keyCode, mod);
		if(inputType==InputType.TEXT) {
			if(mod%2==0)
				textInput.textboxKeyTyped(typedChar, keyCode);
		}
	}
	private void close(LuaValue value) {
		answer = value;
		answered = true;
		textInput.setFocused(false);
		Minecraft.getMinecraft().player.closeScreen();
	}
	
	
	//BOOKMARK implement some fancy code here
	public static enum InputType{
		TEXT,
		ITEM,
		//NUMBER, //TODO more features! (Input types)
		//LOCATION,
		//PLAYER,
		//TIME,
		//FILE,
		CHOICE
	}

	private Prompt promptFunc = new Prompt();
	public Prompt getPrompt() {
		return promptFunc;
	}

	private static int BGColor=Color.WHITE.toInt(), 
			FILLColor = Color.TEXT_8.toInt(), 
			TEXTCOLOR=Color.WHITE.toInt(),
			HEIGHLIGHT = 0x550010F0;
	private class ItemOption implements Moveable, InputSubscriber, Drawable{
		ItemStack stack;
		private int x,y, width, height;

		private boolean isVisible = false;
		public ItemOption(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		public boolean onScroll(Gui gui, int i) {
			return false;
		}

		@Override
		public boolean onMouseClick(Gui gui, int x, int y, int buttonNum) {
			if(GuiRect.isInBounds(x, y, this.x, this.y, width, 20)) {
				close(Utils.itemStackToLuatable(stack));
				return true;
			}return false;
		}

		@Override
		public boolean onMouseRelease(Gui gui, int x, int y, int state) {
			return false;
		}

		@Override
		public boolean onMouseClickMove(Gui gui, int x, int y, int buttonNum, long timeSinceClick) {
			return false;
		}

		@Override
		public boolean onKeyPressed(Gui gui, char typedChar, int keyCode) {
			return false;
		}

		@Override
		public boolean onKeyRepeat(Gui gui, char typedChar, int keyCode, int repeatMod) {
			return false;
		}

		@Override
		public boolean onKeyRelease(Gui gui, char typedChar, int keyCode) {
			return false;
		}

		@Override
		public void setPos(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public void setVisible(boolean b) {
			isVisible = b;
		}

		@Override
		public int getItemHeight() {
			return 20;
		}

		@Override
		public int getItemWidth() {
			return width;
		}

		@Override
		public void setWidth(int i) {
			width = i;
		}

		@Override
		public void setHeight(int i) {
			//this is constant
		}

		@Override
		public int getX() {
			return x;
		}

		@Override
		public int getY() {
			return y;
		}

		@Override
		public void onDraw(Gui g, int mouseX, int mouseY, float partialTicks) {
			if(!isVisible) {return;}
			g.drawBoxedRectangle(x, y, width, 20, BGColor, FILLColor);
			RenderHelper.disableStandardItemLighting();
			RenderHelper.enableGUIStandardItemLighting();
			itemRender.renderItemAndEffectIntoGUI(stack, x+3, y+2);
			
			if(GuiRect.isInBounds(mouseX, mouseY, this.x, this.y, width, 20)) {
				net.minecraft.client.gui.Gui.drawRect(x, y, x+width, y+20, HEIGHLIGHT);
			}
			g.getFontRend().drawString(stack.getDisplayName(), x+25, y+5, TEXTCOLOR);
			
			
		}

	}
	private class CustomOption implements Moveable, InputSubscriber, Drawable{
		private int x,y, width, height;
		String option;
		
		private boolean isVisible = false;
		public CustomOption(String option) {
			this.option = option;
		}

		@Override
		public boolean onScroll(Gui gui, int i) {
			return false;
		}

		@Override
		public boolean onMouseClick(Gui gui, int x, int y, int buttonNum) {
			if(GuiRect.isInBounds(x, y, this.x, this.y, width, 20)) {
				close(LuaValue.valueOf(option));
				return true;
			}return false;
		}

		@Override
		public boolean onMouseRelease(Gui gui, int x, int y, int state) {
			return false;
		}

		@Override
		public boolean onMouseClickMove(Gui gui, int x, int y, int buttonNum, long timeSinceClick) {
			return false;
		}

		@Override
		public boolean onKeyPressed(Gui gui, char typedChar, int keyCode) {
			return false;
		}

		@Override
		public boolean onKeyRepeat(Gui gui, char typedChar, int keyCode, int repeatMod) {
			return false;
		}

		@Override
		public boolean onKeyRelease(Gui gui, char typedChar, int keyCode) {
			return false;
		}

		@Override
		public void setPos(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public void setVisible(boolean b) {
			isVisible = b;
		}

		@Override
		public int getItemHeight() {
			return 20;
		}

		@Override
		public int getItemWidth() {
			return width;
		}

		@Override
		public void setWidth(int i) {
			width = i;
		}

		@Override
		public void setHeight(int i) {
			//this is constant
		}

		@Override
		public int getX() {
			return x;
		}

		@Override
		public int getY() {
			return y;
		}

		@Override
		public void onDraw(Gui g, int mouseX, int mouseY, float partialTicks) {
			if(!isVisible) {return;}
			g.drawBoxedRectangle(x, y, width, 20, BGColor, FILLColor);
			
			
			if(GuiRect.isInBounds(mouseX, mouseY, this.x, this.y, width, 20)) {
				net.minecraft.client.gui.Gui.drawRect(x, y, x+width, y+20, HEIGHLIGHT);
			}
			g.getFontRend().drawString(option, x+7, y+5, TEXTCOLOR);
			
			
		}

	}
	private class Prompt extends VarArgFunction{

		@Override
		public LuaValue invoke(Varargs args) {
			LuaValue arg0 = args.arg1();
			LuaValue type = args.arg(2);
			//System.out.println("Block Waiting");
			AdvancedMacros.forgeEventHandler.releaseAllKeys();
			synchronized (InputGUI.this) {
				//System.out.println("Block Entered!");
				try {
					boolean stoped = false;

					inputType = InputType.valueOf(type.optjstring("TEXT").toUpperCase());
					
					if(inputType==InputType.CHOICE) {
						choices.clear();
						for(int i = 3; i<=args.narg(); i++) {
							choices.add(new CustomOption(args.arg(i).checkjstring()));
						}
					}
					
					while(!stoped && (threadCheck!=Thread.currentThread())) {
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							stoped = true;
						}
						setInputType(inputType, arg0.tojstring());
					}
					Minecraft.getMinecraft().addScheduledTask(new Runnable() {
						@Override
						public void run() {
							Mouse.setGrabbed(false);
						}
					});
					
					Minecraft.getMinecraft().displayGuiScreen(AdvancedMacros.inputGUI);
					//System.out.println("Not grabbed");
					while(!answered && !stoped && (threadCheck!=Thread.currentThread() || Minecraft.getMinecraft().currentScreen==InputGUI.this)) {
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							stoped = true;
						}
					}

				}catch (Exception e) {
					threadCheck=null;
					throw e;
				}
				LuaValue ourAns = answer; //used incase next thread happens to call and wipe out answer 
				threadCheck = null;
				return ourAns;


			}
		}
	}
}