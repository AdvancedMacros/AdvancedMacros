package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaFunction;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;
import org.lwjgl.input.Mouse;

import com.theincgi.advancedMacros.gui.Color;
import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.gui.Gui.InputSubscriber;
import com.theincgi.advancedMacros.gui.elements.Drawable;
import com.theincgi.advancedMacros.gui.elements.GuiButton;
import com.theincgi.advancedMacros.gui.elements.Moveable;
import com.theincgi.advancedMacros.lua.LuaValTexture;
import com.theincgi.advancedMacros.lua.scriptGui.GuiRectangle;
import com.theincgi.advancedMacros.lua.scriptGui.ScriptGuiElement;
import com.theincgi.advancedMacros.lua.scriptGui.ScriptGuiText;
import com.theincgi.advancedMacros.lua.scriptGui.Group;
import com.theincgi.advancedMacros.lua.scriptGui.GuiBox;
import com.theincgi.advancedMacros.lua.scriptGui.GuiImage;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class ScriptGui extends LuaTable implements InputSubscriber{
	private static int counter = 1;
	String guiName = "generic_"+counter++;
	Gui gui = new Gui() {
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
		public String toString() {
			return "ScriptGui:"+guiName;
		}
	};
	//LuaTable controls = new LuaTable();
	LuaFunction onScroll, onMouseClick, onMouseRelease, onMouseDrag, onKeyPressed, onKeyRepeated, onKeyReleased, onGuiClose,
	onGuiOpen;
	Group guiGroup = new Group();
	ScriptGui parentGui = null;
	public ScriptGui() { 
		gui.inputSubscribers.add(this); //tell yourself everything!
		set("addRectangle", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				GuiRectangle r = new GuiRectangle(gui, guiGroup);
				r.x = (float)args.optdouble(1, 0);
				r.y = (float)args.optdouble(2, 0);
				r.wid = (float)args.optdouble(3, 0);
				r.hei = (float)args.optdouble(4, 0);
				return r;
			}
		});
		set("addBox", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				GuiBox r = new GuiBox(gui, guiGroup);
				r.x = (float)args.optdouble(1, 0);
				r.y = (float)args.optdouble(2, 0);
				r.wid = (float)args.optdouble(3, 0);
				r.hei = (float)args.optdouble(4, 0);
				r.thickness = (float)args.optdouble(5, 1);
				return r;
			}
		});
		set("addGroup", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return new Group(guiGroup);
			}
		});
		set("addText", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				ScriptGuiText t = new ScriptGuiText(gui, guiGroup);
				t.setText(args.optjstring(1, ""));
				t.x = (float)args.optdouble(2, 0);
				t.y = (float)args.optdouble(3, 0);
				t.textSize = args.optint(4, 12);
				return t;
			}
		});
		set("addImage", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				GuiImage t = new GuiImage(gui, guiGroup);
				t.setTexture(args.optvalue(1, LuaValue.NIL));
				t.x = (float)args.optdouble(2, 0);
				t.y = (float)args.optdouble(3, 0);
				t.wid = (float)args.optdouble(4, 0);
				t.hei = (float)args.optdouble(5, 0);
				return t;
			}
		});
		set("open", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				Minecraft.getMinecraft().displayGuiScreen(gui);
				Mouse.setGrabbed(false);
				if(onGuiOpen!=null)
					onGuiOpen.call();
				return LuaValue.NONE;
			}
		});
		set("close", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				Minecraft.getMinecraft().displayGuiScreen(null);
				return NONE;
			}
		});
		set("setParentGui", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				if(arg.isnil()) {
					parentGui = null;
				}else if(arg instanceof ScriptGui){
					parentGui = (ScriptGui) arg;
				}else {
					throw new LuaError("Gui expected");
				}
				return NONE;
			}
		});
		this.set("getParentGui", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return parentGui;
			}
		});
		this.set("getSize", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs v) {
				LuaTable temp = new LuaTable();
				Minecraft mc = Minecraft.getMinecraft();
				ScaledResolution scaled = new ScaledResolution(mc);
				temp.set(1, LuaValue.valueOf(scaled.getScaledWidth()));
				temp.set(2, LuaValue.valueOf(scaled.getScaledHeight()));
				return temp.unpack();
			}
		});
		this.set("setName", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				guiName = arg.checkjstring();
				return NONE;
			}
		});
		this.set("getName", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(guiName);
			}
		});
		addInputControls(this);
	}

	
	
	
	
	//TODO resize event
	

public static class CreateScriptGui extends ZeroArgFunction{
	@Override
	public LuaValue call() {
		return new ScriptGui();
	}
}


	public void addInputControls(LuaTable s) {
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
			return Utils.pcall(onKeyPressed, LuaValue.valueOf(typedChar), LuaValue.valueOf(keyCode)).optboolean(false);
		return false;
	}

	@Override
	public boolean onKeyRepeat(Gui gui, char typedChar, int keyCode, int repeatMod) {
		if(onKeyRepeated!=null)
			return Utils.pcall(onKeyRepeated, LuaValue.valueOf(typedChar), LuaValue.valueOf(keyCode), LuaValue.valueOf(repeatMod)).optboolean(false);
		return false;
	}

	@Override
	public boolean onKeyRelease(Gui gui, char typedChar, int keyCode) {
		if(onKeyReleased!=null)
			return Utils.pcall(onKeyReleased, LuaValue.valueOf(typedChar), LuaValue.valueOf(keyCode)).optboolean(false);
		return false;
	}
	
	
}
