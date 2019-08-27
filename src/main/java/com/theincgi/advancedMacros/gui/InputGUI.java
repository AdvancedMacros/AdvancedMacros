package com.theincgi.advancedMacros.gui;

import java.util.Collection;
import java.util.Iterator;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.lwjgl.glfw.GLFW;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.event.TaskDispatcher;
import com.theincgi.advancedMacros.gui.elements.Drawable;
import com.theincgi.advancedMacros.gui.elements.GuiRect;
import com.theincgi.advancedMacros.gui.elements.ListManager;
import com.theincgi.advancedMacros.gui.elements.Moveable;
import com.theincgi.advancedMacros.lua.LuaDebug;
import com.theincgi.advancedMacros.misc.HIDUtils.Mouse;
import com.theincgi.advancedMacros.misc.PropertyPalette;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public class InputGUI extends Gui{
	private Thread threadCheck;
	InputType inputType;
	private LuaDebug debug;
	TextFieldWidget textInput = new TextFieldWidget(fontRend, 5, 5, 30, 12, "");
	private IForgeRegistry<Item> blah = GameRegistry.findRegistry(Item.class);
	private Collection<Item> itemList = blah.getValues();
	private final int WHITE = Color.WHITE.toInt();
	private String prompt;
	private boolean answered = true;
	private LuaValue answer = LuaValue.NIL;
	PropertyPalette propPalette = new PropertyPalette(new String[] {"promptGui"});
	private ListManager listItemPicker = new ListManager(5, 19, 30, 30, /*new WidgetID(800), "colors.promptGUI"*/ propPalette);
	private ListManager choices = new ListManager(5, 19, 30, 30, /*new WidgetID(800), "colors.promptGUI"*/ propPalette);
	private static ItemRenderer itemRenderer = AdvancedMacros.getMinecraft().getItemRenderer();
	public InputGUI(LuaDebug debug) {
		this.debug = debug;
		textInput.setHeight(12);
		listItemPicker.setDrawBG(false);
		listItemPicker.setAlwaysShowScroll(true);
		listItemPicker.setSpacing(3);
		Iterator<Item> itemItter = itemList.iterator();
		for(int i = 0; itemItter.hasNext(); i++) {
			Item item = itemItter.next();
			//argument for maxDamage is literally discarded
			//for(int d = 0; d<item.getMaxDamage(ItemStack.EMPTY); d++) {
				listItemPicker.add(new ItemOption(new ItemStack(item, 1)));
			//}
		}
		addInputSubscriber(listItemPicker);
		addInputSubscriber(choices);
		listItemPicker.setScrollSpeed(10);
		choices.setScrollSpeed(10);
		choices.setSpacing(3);
		textInput.setMaxStringLength(Integer.MAX_VALUE);
	}

	public void setInputType(InputType inputType, String prompt) {
		synchronized (this) {
			if(threadCheck==null) {
				threadCheck = Thread.currentThread();
				this.inputType = inputType;
				this.prompt = prompt;
				answered=false;
				textInput.setFocused2(inputType==InputType.TEXT);
				textInput.setVisible(inputType==InputType.TEXT);
				textInput.setText("");
				listItemPicker.setVisible(inputType==InputType.ITEM);
				
			}
		}
	}

	@Override
	public ITextComponent getTitle() {
		return new StringTextComponent("Input Screen");
	}
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		super.render(mouseX, mouseY, partialTicks);
		switch (inputType) {
		case TEXT:
			fill(1, height-49, width-1, height-1, 0xDD000000);
			int drawHei = height-5;
			textInput.y=(drawHei-=12);
			drawHei-=5;
			drawHei-=getFontRend().FONT_HEIGHT;
			getFontRend().drawString(prompt, 5, drawHei, WHITE);
			drawHei-=5;
			drawHei-=getFontRend().FONT_HEIGHT;
			getFontRend().drawString(debug.getLabel(threadCheck), 5, drawHei, WHITE);

			textInput.render(mouseX, mouseY, partialTicks);
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
	public void resize(Minecraft mc, int width, int height) {
		super.resize(mc, width, height);
		textInput.setWidth(width-10);
		listItemPicker.setPos(5, 25);
		listItemPicker.setWidth(width/2-10);
		listItemPicker.setHeight(height-60);
		choices.setPos(5, 25);
		choices.setWidth(width/2-10);
		choices.setHeight(height-60);
		
	}

	
	@Override
	public boolean charTyped(char typedChar, int mods) {
		if(inputType==InputType.TEXT) {
			return textInput.charTyped(typedChar, mods);
		}
		
		return super.charTyped(typedChar, mods);
	}
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if(keyCode==GLFW.GLFW_KEY_ESCAPE) {
			close(LuaValue.NIL);
			return true;
		}else if(keyCode==GLFW.GLFW_KEY_ENTER) {
			close(LuaValue.valueOf(textInput.getText()));
			return true;
		}
		if(inputType==InputType.TEXT) {
			return textInput.keyPressed(keyCode, scanCode, modifiers);
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	@Override
	public boolean onKeyRepeated(Gui gui, int keyCode, int scanCode, int mods, int n) {
		return super.onKeyRepeated(gui, keyCode, scanCode, mods, n) || (n%2==0 && textInput.keyPressed(keyCode, scanCode, mods));
	}
//	@Override
//	public boolean keyRepeated(char typedChar, int keyCode, int mod) {
//		super.keyRepeated(typedChar, keyCode, mod);
//		if(inputType==InputType.TEXT) {
//			if(mod%2==0)
//				textInput.charTyped(typedChar, keyCode);
//			return textInput.isFocused();
//		}
//		return false;
//	}
	private void close(LuaValue value) {
		answer = value;
		answered = true;
		textInput.setFocused2(false);
		AdvancedMacros.getMinecraft().player.closeScreen();
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
		public boolean onScroll(Gui gui, double i) {
			return false;
		}

		@Override
		public boolean onMouseClick(Gui gui, double x, double y, int buttonNum) {
			if(GuiRect.isInBounds(x, y, this.x, this.y, width, 20) && isVisible) {
				close(Utils.itemStackToLuatable(stack));
				return true;
			}return false;
		}

		@Override
		public boolean onMouseRelease(Gui gui, double x, double y, int state) {
			return false;
		}
		@Override
		public boolean onMouseClickMove(Gui gui, double x, double y, int buttonNum, double q, double r) {
			return false;
		}


		@Override
		public boolean onKeyPressed(Gui gui, int keyCode, int scanCode, int modifiers) {
			return false;
		}
		@Override
		public boolean onCharTyped(Gui gui, char typedChar, int mods) {
			return false;
		}

		@Override
		public boolean onKeyRelease(Gui gui, int keyCode, int scanCode, int modifiers) {
			return false;
		}
		@Override
		public boolean onKeyRepeat(Gui gui, int keyCode, int scanCode, int modifiers, int n) {
			return false;
		}

		@Override
		public void setPos(int x, int y) {
			this.x = x;
			this.y = y;
		}
		@Override
		public void setX(int x) {
			this.x = x;
		}
		@Override
		public void setY(int y) {
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
			AdvancedMacros.getMinecraft().getItemRenderer().renderItemAndEffectIntoGUI(stack, x+3, y+2);
			
			if(GuiRect.isInBounds(mouseX, mouseY, this.x, this.y, width, 20)) {
				fill(x, y, x+width, y+20, HEIGHLIGHT);
			}
			g.getFontRend().drawString(stack.getDisplayName().getFormattedText(), x+25, y+5, TEXTCOLOR);
			
			
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
		public boolean onScroll(Gui gui, double i) {
			return false;
		}

		@Override
		public boolean onMouseClick(Gui gui, double x, double y, int buttonNum) {
			if(GuiRect.isInBounds(x, y, this.x, this.y, width, 20) && isVisible) {
				close(LuaValue.valueOf(option));
				return true;
			}return false;
		}

		@Override
		public boolean onMouseRelease(Gui gui, double x, double y, int state) {
			return false;
		}

		@Override
		public boolean onMouseClickMove(Gui gui, double x, double y, int buttonNum, double q, double r) {
			return false;
		}

		@Override
		public boolean onCharTyped(Gui gui, char typedChar, int mods) {
			return false;
		}
		@Override
		public boolean onKeyPressed(Gui gui, int keyCode, int scanCode, int modifiers) {
			return false;
		}

		@Override
		public boolean onKeyRelease(Gui gui, int keyCode, int scanCode, int modifiers) {
			return false;
		}
		@Override
		public boolean onKeyRepeat(Gui gui, int keyCode, int scanCode, int modifiers, int n) {
			return false;
		}

		@Override
		public void setPos(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		@Override
		public void setX(int x) {
			setPos(x, y);
		}
		
		@Override
		public void setY(int y) {
			setPos(x, y);
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
				fill(x, y, x+width, y+20, HEIGHLIGHT);
			}
			g.getFontRend().drawString(option, x+7, y+5, TEXTCOLOR);
			
			
		}

	}
	private class Prompt extends VarArgFunction{

		@Override
		public LuaValue invoke(Varargs args) {
			while(AdvancedMacros.getMinecraft().currentScreen==AdvancedMacros.runningScriptsGui) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					throw new LuaError(e);
				}
			}
			LuaValue arg0 = args.arg1();
			LuaValue type = args.arg(2);
			//System.out.println("Block Waiting");
//			AdvancedMacros.forgeEventHandler.releaseAllKeys();
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
						setInputType(inputType, arg0.tojstring()); //seems to effect the thread check
					}
					TaskDispatcher.addTask(new Runnable() {
						@Override
						public void run() {
							Mouse.setGrabbed(false);
						}
					});
					
					AdvancedMacros.getMinecraft().displayGuiScreen(AdvancedMacros.inputGUI);
					//System.out.println("Not grabbed");
					while(!answered && !stoped && (threadCheck!=Thread.currentThread() || AdvancedMacros.getMinecraft().currentScreen==InputGUI.this)) {
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