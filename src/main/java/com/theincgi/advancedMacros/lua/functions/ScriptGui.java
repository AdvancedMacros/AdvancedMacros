package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaFunction;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;
import org.lwjgl.input.Mouse;

import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.gui.Gui.InputSubscriber;
import com.theincgi.advancedMacros.gui.elements.GuiScrollBar;
import com.theincgi.advancedMacros.gui.elements.GuiScrollBar.Orientation;
import com.theincgi.advancedMacros.lua.scriptGui.Group;
import com.theincgi.advancedMacros.lua.scriptGui.GuiBox;
import com.theincgi.advancedMacros.lua.scriptGui.GuiCTA;
import com.theincgi.advancedMacros.lua.scriptGui.GuiImage;
import com.theincgi.advancedMacros.lua.scriptGui.GuiItemIcon;
import com.theincgi.advancedMacros.lua.scriptGui.GuiRectangle;
import com.theincgi.advancedMacros.lua.scriptGui.MCTextBar;
import com.theincgi.advancedMacros.lua.scriptGui.ScriptGuiScrollBar;
import com.theincgi.advancedMacros.lua.scriptGui.ScriptGuiText;
import com.theincgi.advancedMacros.misc.CallableTable;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class ScriptGui extends LuaTable implements InputSubscriber{
	private static int counter = 1;
	String guiName = "generic_"+counter++;
	Gui gui;
	//LuaTable controls = new LuaTable();
	LuaFunction onScroll, onMouseClick, onMouseRelease, onMouseDrag, onKeyPressed, onKeyRepeated, onKeyReleased, onGuiClose,
	onGuiOpen, onResize;
	Group guiGroup = new Group();
	ScriptGui parentGui = null;
	public ScriptGui() { 
		gui = new Gui() {
			@Override
			public void onGuiClosed() {
				super.onGuiClosed();
				if(onGuiClose!=null) {
					onGuiClose.call();
				}
			}
			@Override
			public void drawScreen(int mouseX, int mouseY, float partialTicks) {
				if(parentGui!=null) {
					parentGui.gui.drawScreen(mouseX, mouseY, partialTicks);
					setDrawDefaultBackground(false);
				}else {
					setDrawDefaultBackground(true);
				}
				super.drawScreen(mouseX, mouseY, partialTicks);
			};
			@Override
			public void onResize(Minecraft mcIn, int w, int h) {
				super.onResize(mcIn, w, h);
				if(onResize!=null)
					onResize.call(valueOf(w), valueOf(h));
			}
			@Override
			public String toString() {
				return "ScriptGui:"+guiName;
			}
		};
		gui.inputSubscribers.add(this); //tell yourself everything!
		
		for(OpCodes op : OpCodes.values()) {
			set(op.name(), new CallableTable( op.getDocLocation() , new DoOperation( op ) ));
		}
		
		addInputControls(this);
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
				Minecraft.getMinecraft().displayGuiScreen(null);
				return NONE;
			case getName:
				return LuaValue.valueOf(guiName);
			case getParentGui:
				return parentGui;
			case getSize:{
				LuaTable temp = new LuaTable();
				Minecraft mc = Minecraft.getMinecraft();
				ScaledResolution scaled = new ScaledResolution(mc);
				temp.set(1, LuaValue.valueOf(scaled.getScaledWidth()));
				temp.set(2, LuaValue.valueOf(scaled.getScaledHeight()));
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
			case open:
				Minecraft.getMinecraft().displayGuiScreen(gui);
				Minecraft.getMinecraft().addScheduledTask(()->{
					Minecraft.getMinecraft().mouseHelper.ungrabMouseCursor();
				});
				if(onGuiOpen!=null)
					onGuiOpen.call();
				return LuaValue.NONE;
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
			default:
				throw new LuaError("This function hasn't been implemented D:");
			}
		}
	}

	public static enum OpCodes {
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
		getName;
		
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
