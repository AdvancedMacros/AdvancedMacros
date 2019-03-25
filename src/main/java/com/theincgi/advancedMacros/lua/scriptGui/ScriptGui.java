package com.theincgi.advancedMacros.lua.scriptGui;

import java.io.IOException;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaFunction;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.event.ForgeEventHandler;
import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.gui.Gui.InputSubscriber;
import com.theincgi.advancedMacros.gui.elements.GuiScrollBar.Orientation;
import com.theincgi.advancedMacros.misc.CallableTable;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

public class ScriptGui extends LuaTable implements InputSubscriber{
	private static int counter = 1;
	String guiName = "generic_"+counter++;
	Gui gui;
	//LuaTable controls = new LuaTable();
	LuaFunction onScroll, onMouseClick, onMouseRelease, onMouseDrag, onKeyPressed, onKeyRepeated, onKeyReleased, onGuiClose,
	onGuiOpen, onResize;
	Group guiGroup = new Group();
	ScriptGui parentGui = null;
	private boolean isOpen = false;
	private boolean pausesGame = true;
	private double lastMouseX, lastMouseY;
	public ScriptGui() { 
		gui = new Gui() {
			@Override
			public void onGuiClosed() {
				super.onGuiClosed();
				if(onGuiClose!=null) {
					Utils.pcall(onGuiClose);
				}
				isOpen = false;
				for(InputSubscriber s : inputSubscribers) {
					if(s instanceof ScriptGuiElement ) {
						ScriptGuiElement sge = (ScriptGuiElement)s;
						sge.resetMouseOver();
					}
				}
				//GuiScreen tmp = AdvancedMacros.getMinecraft().currentScreen;
			}
			@Override
			public void drawScreen(int mouseX, int mouseY, float partialTicks) {
				lastMouseX = mouseX;
				lastMouseY = mouseY;
				if(parentGui!=null) {
					GlStateManager.pushMatrix();
					GlStateManager.translate(0, 0, -50);
					parentGui.gui.drawScreen(mouseX, mouseY, partialTicks);
					GlStateManager.popMatrix();
					//setDrawDefaultBackground(false);
				}else {
					//setDrawDefaultBackground(true);
				}
				super.drawScreen(mouseX, mouseY, partialTicks);
			};
			@Override
			public void onResize(Minecraft mcIn, int w, int h) {
				ScriptGui.this.onGuiResize(mcIn, w, h);
			}
			@Override
			public boolean doesGuiPauseGame() {
				return pausesGame;
			}
			@Override
			protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
				if(!super.onMouseClicked(mouseX, mouseY, mouseButton))
					if(!ScriptGui.this.onMouseClick(this, mouseX, mouseY, mouseButton))
						if (parentGui != null)
							parentGui.onMouseClick(parentGui.gui, mouseX, mouseY, mouseButton);
			}
			@Override
			protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
				if(!super.onMouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick))
					if(!ScriptGui.this.onMouseClickMove(this, mouseX, mouseY, clickedMouseButton, timeSinceLastClick))
						if (parentGui != null)
							parentGui.onMouseClickMove(parentGui.gui, mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
			}
			@Override
			protected void mouseReleased(int mouseX, int mouseY, int state) {
				if(!super.onMouseReleased(mouseX, mouseY, state))
					if(!ScriptGui.this.onMouseRelease(this, mouseX, mouseY, state))
						if (parentGui != null)
							parentGui.onMouseRelease(parentGui.gui, mouseX, mouseY, state);
			}
			@Override
			public boolean onKeyTyped(char typedChar, int keyCode) {
				return super.onKeyTyped(typedChar, keyCode) ||                //java effectively returns as soon as one is true. only one should run
				 ScriptGui.this.onKeyPressed(this, typedChar, keyCode) ||
				 (parentGui!=null && parentGui.onKeyPressed(parentGui.gui, typedChar, keyCode));
			}
			@Override
			public boolean keyRepeated(char typedChar, int keyCode, int mod) {
				return super.keyRepeated(typedChar, keyCode, mod) ||
						ScriptGui.this.onKeyRepeat(this, typedChar, keyCode, mod) ||
						 (parentGui!=null && parentGui.onKeyRepeat(parentGui.gui, typedChar, keyCode, mod));
			}
			@Override
			public boolean onKeyRelease(Gui gui, char typedChar, int keyCode) {
				return super.onKeyRelease(gui, typedChar, keyCode)||
						ScriptGui.this.onKeyRelease(this, typedChar, keyCode) ||
						 (parentGui!=null && parentGui.onKeyRelease(parentGui.gui, typedChar, keyCode));
			}
			@Override
			public boolean mouseScroll(int i) {
				return super.mouseScroll(i) ||
						ScriptGui.this.onScroll(this, i) ||
						(parentGui!=null && parentGui.onScroll(parentGui.gui, i));
			}
			@Override
			public String toString() {
				return "ScriptGui:"+guiName;
			}
		};
		//gui.inputSubscribers.add(this); //tell yourself everything!
		
		for(OpCodes op : OpCodes.values()) {
			set(op.name(), new CallableTable( op.getDocLocation() , new DoOperation( op ) ));
		}
		set("__class", "advancedMacros.ScriptGui");
		addInputControls(this);
	}
	
	public void onGuiResize(Minecraft mcIn, int w, int h) {
		if(parentGui!=null)
			parentGui.onGuiResize(mcIn, w, h);
		gui.setWorldAndResolution(mcIn, w, h);
		if(onResize!=null)
			Utils.pcall(onResize, valueOf(w), valueOf(h));
	}
	
	public class DoOperation extends VarArgFunction {
		OpCodes opCode;
		
		public DoOperation(OpCodes opCode) {
			super();
			this.opCode = opCode;
		}

		@Override
		public Varargs invoke(Varargs args) {
			switch (opCode) {
			case close:
				AdvancedMacros.getMinecraft().displayGuiScreen(null);
				return NONE;
			case getName:
				return LuaValue.valueOf(guiName);
			case getParentGui:
				return parentGui == null? NIL : parentGui;
			case getSize:{
				LuaTable temp = new LuaTable();
				Minecraft mc = AdvancedMacros.getMinecraft();
				ScaledResolution scaled = new ScaledResolution(mc);
				temp.set(1, LuaValue.valueOf(scaled.getScaledWidth()));
				temp.set(2, LuaValue.valueOf(scaled.getScaledHeight()));
				temp.set(3, valueOf(mc.displayWidth));
				temp.set(4, valueOf(mc.displayHeight));
				return temp.unpack();
			}
			case newBox:
				GuiBox r = new GuiBox(gui, guiGroup);
				r.x = (float)args.optdouble(1, 0);
				r.y = (float)args.optdouble(2, 0);
				r.wid = (float)args.optdouble(3, 0);
				r.hei = (float)args.optdouble(4, 0);
				r.thickness = (float)args.optdouble(5, 1);
				return r;
			case newGroup:
				return new Group(guiGroup);
			case newImage:{
				GuiImage t = new GuiImage(gui, guiGroup);
				t.setTexture(args.optvalue(1, LuaValue.NIL));
				t.x = (float)args.optdouble(2, 0);
				t.y = (float)args.optdouble(3, 0);
				t.wid = (float)args.optdouble(4, 0);
				t.hei = (float)args.optdouble(5, 0);
				return t;
			}
			case newItem:
				GuiItemIcon gii = new GuiItemIcon(gui, guiGroup);
				if(!args.arg1().isnil())
					gii.setStack(args.checkjstring(1));
				gii.setPos(args.optint(2, 0), args.optint(3, 0));
				gii.setCount(args.optint(4, 1));
				return gii;
			case newMinecraftTextField:
				MCTextBar text = new MCTextBar(gui, guiGroup);
				text.setPos((int)args.optdouble(1, 0) , (int)args.optdouble(2, 0) );
				text.setWidth(  args.optint(3, 0) );
				text.setHeight( args.optint(4, 20) );
				text.setText( args.optstring(5, valueOf("")).tojstring() );
				return text;
			case newRectangle:
				GuiRectangle rect = new GuiRectangle(gui, guiGroup);
				rect.x = (float)args.optdouble(1, 0);
				rect.y = (float)args.optdouble(2, 0);
				rect.wid = (float)args.optdouble(3, 0);
				rect.hei = (float)args.optdouble(4, 0);
				return rect;
			case newScrollBar:
				int x = args.optint(1, 0);
				int y = args.optint(2, 0);
				int wid = args.optint(3, 0);
				int len = args.optint(4, 0);
				Orientation orient = Orientation.from(args.arg(5).optjstring("vertical"));
				if(orient.isLEFTRIGHT()) {
					int swap = wid;
					wid = len;
					len = swap;
				}
				return new ScriptGuiScrollBar(gui, guiGroup, x, y, wid, len, orient);
			case newText:{
				ScriptGuiText t = new ScriptGuiText(gui, guiGroup);
				t.setText(args.optjstring(1, ""));
				t.x = (float)args.optdouble(2, 0);
				t.y = (float)args.optdouble(3, 0);
				t.textSize = args.optint(4, 12);
				return t;
			}
			case newTextArea:
				GuiCTA cta = new GuiCTA(gui, guiGroup);
				cta.setPos((int)args.optdouble(1, 0) , (int)args.optdouble(2, 0) );
				cta.setWidth(  args.optint(3, 0) );
				cta.setHeight( args.optint(4, 0) );
				cta.getCTA().setText( args.optstring(5, valueOf("")).tojstring() );
				return cta;
			case open:{
				AdvancedMacros.forgeEventHandler.releaseAllKeys();
				Integer mx = null, my=null;
				if(args.isnumber(1) && args.isnumber(2)) {
					mx = args.checkint(1);
					my = args.checkint(2);
					Minecraft mc = AdvancedMacros.getMinecraft();
					ScaledResolution scaled = new ScaledResolution(mc);
					mx = mx*(mc.displayWidth/scaled.getScaledWidth());
					my = mc.displayHeight - my*(mc.displayHeight/scaled.getScaledHeight());
				}
				final Integer fmx = mx, fmy = my;
				AdvancedMacros.getMinecraft().addScheduledTask(()->{
					AdvancedMacros.getMinecraft().displayGuiScreen(gui);
					Mouse.setGrabbed(false);
					if(fmx!=null && fmy!=null)
						Mouse.setCursorPosition(fmx, fmy);
					//AdvancedMacros.getMinecraft().mouseHelper.ungrabMouseCursor();
				});
				if(onGuiOpen!=null)
					onGuiOpen.call();
				isOpen = true;
				return LuaValue.NONE;
			}
			case isOpen:
				return valueOf(isOpen);
			case setName:
				guiName = args.arg1().checkjstring();
				return NONE;
			case setParentGui:{
				LuaValue arg = args.arg1();
				if(arg.isnil()) {
					parentGui = null;
				}else if(arg instanceof ScriptGui){
					parentGui = (ScriptGui) arg;
				}else {
					throw new LuaError("Gui expected");
				}
				return NONE;
			}
			case isDefaultBackground:
				return valueOf(gui.getDrawDefaultBackground());
			case setDefaultBackground:
				gui.setDrawDefaultBackground(args.checkboolean(1));
				return NONE;
			case getMousePos:{
				return Utils.varargs(valueOf(lastMouseX), valueOf(lastMouseY));
			}
			case grabMouse:
				AdvancedMacros.getMinecraft().addScheduledTask(()->{
					//AdvancedMacros.getMinecraft().mouseHelper.grabMouseCursor();
					Mouse.setGrabbed(true);
				});
				return NONE;
			case ungrabMouse:
				AdvancedMacros.getMinecraft().addScheduledTask(()->{
					Mouse.setGrabbed(false);//AdvancedMacros.getMinecraft().mouseHelper.ungrabMouseCursor();
				});
				return NONE;
			case setPausesGame:
				pausesGame = args.checkboolean(1);
				return NONE;
			case isPausesGame:
				return valueOf(pausesGame);
			case clear:
				gui.inputSubscribers.clear();
				gui.clearDrawables();
				return NONE;
			default:
				throw new LuaError("This function hasn't been implemented D:");
			}
		}
	}

	public static enum OpCodes {
		clear,
		//remove,
		newRectangle,
		newBox,
		newGroup,
		newText,
		newTextArea,
		newScrollBar,
		newMinecraftTextField,
		newImage,
		newItem,
		open,
		close,
		setParentGui,
		getParentGui,
		getSize,
		setName,
		getName,
		setDefaultBackground,
		isDefaultBackground, 
		isOpen,
		getMousePos, 
		grabMouse, ungrabMouse, isPausesGame, setPausesGame;
		
		public String[] getDocLocation() {
			String[] out = new String[3];
			out[0] = "gui";
			out[1] = "new()";
			out[2] = this.name();
			return out;
		}
		
		
	}


	//TODO resize event



	public static class CreateScriptGui extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			return new ScriptGui();
		}
	}


	public void addInputControls(LuaTable s) {
		s.set("setOnResize", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				onResize = arg.checkfunction();
				return LuaValue.NONE;
			}
		});
		s.set("setOnScroll", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				onScroll = arg.checkfunction();
				return LuaValue.NONE;
			}
		});
		s.set("setOnMouseClick", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				onMouseClick = arg.checkfunction();
				return LuaValue.NONE;
			}
		});
		s.set("setOnMouseRelease", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				onMouseRelease = arg.checkfunction();
				return LuaValue.NONE;
			}
		});
		s.set("setOnMouseDrag", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				onMouseDrag = arg.checkfunction();
				return LuaValue.NONE;
			}
		});
		s.set("setOnKeyPressed", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				onKeyPressed = arg.checkfunction();
				return LuaValue.NONE;
			}
		});
		s.set("setOnKeyReleased", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				onKeyReleased = arg.checkfunction();
				return LuaValue.NONE;
			}
		});
		s.set("setOnKeyRepeated", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				onKeyRepeated = arg.checkfunction();
				return LuaValue.NONE;
			}
		});
		s.set("setOnOpen", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				onGuiOpen = arg.checkfunction();
				return NONE;
			}
		});
		s.set("setOnClose", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				onGuiClose = arg.checkfunction();
				return NONE;
			}
		});
	}

	@Override
	public boolean onScroll(Gui gui, int i) {
		if(onScroll != null) {
			return Utils.pcall(onScroll, LuaValue.valueOf(i)).optboolean(false);
		}
		return false;
	}

	@Override
	public boolean onMouseClick(Gui gui, int x, int y, int buttonNum) {
		if(onMouseClick != null)
			return Utils.pcall(onMouseClick, LuaValue.valueOf(x), LuaValue.valueOf(y), LuaValue.valueOf(buttonNum)).optboolean(false);
		return false;
	}

	@Override
	public boolean onMouseRelease(Gui gui, int x, int y, int state) {
		if(onMouseRelease!=null)
			return Utils.pcall(onMouseRelease, LuaValue.valueOf(x), LuaValue.valueOf(y), LuaValue.valueOf(state)).optboolean(false);
		return false;
	}

	@Override
	public boolean onMouseClickMove(Gui gui, int x, int y, int buttonNum, long timeSinceClick) {
		if(onMouseDrag!=null) {
			LuaTable args = new LuaTable();
			args.set(1, LuaValue.valueOf(x));
			args.set(2, LuaValue.valueOf(y));
			args.set(3, LuaValue.valueOf(buttonNum));
			args.set(4, LuaValue.valueOf(timeSinceClick));
			return Utils.pcall(onMouseDrag, args.unpack()).arg1().optboolean(false);
		}
		return false;
	}

	@Override
	public boolean onKeyPressed(Gui gui, char typedChar, int keyCode) {
		if(onKeyPressed!=null)
			return Utils.pcall(onKeyPressed, LuaValue.valueOf(typedChar+""), LuaValue.valueOf(keyCode)).optboolean(false);
		return false;
	}

	@Override
	public boolean onKeyRepeat(Gui gui, char typedChar, int keyCode, int repeatMod) {
		if(onKeyRepeated!=null)
			return Utils.pcall(onKeyRepeated, LuaValue.valueOf(typedChar+""), LuaValue.valueOf(keyCode), LuaValue.valueOf(repeatMod)).optboolean(false);
		return false;
	}

	@Override
	public boolean onKeyRelease(Gui gui, char typedChar, int keyCode) {
		if(onKeyReleased!=null)
			return Utils.pcall(onKeyReleased, LuaValue.valueOf(typedChar+""), LuaValue.valueOf(keyCode)).optboolean(false);
		return false;
	}


}
