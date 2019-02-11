package com.theincgi.advancedMacros.lua.scriptGui;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;

import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.gui.elements.GuiScrollBar;
import com.theincgi.advancedMacros.gui.elements.GuiScrollBar.Orientation;
import com.theincgi.advancedMacros.misc.PropertyPalette;
import com.theincgi.advancedMacros.misc.Utils;

public class ScriptGuiScrollBar extends ScriptGuiElement{
	GuiScrollBar bar;
	PropertyPalette propPal;
	public ScriptGuiScrollBar(Gui gui, Group parent, int x, int y, int wid, int len, Orientation orient) {
		super(gui, parent, true);
		propPal = new PropertyPalette();
		bar = new GuiScrollBar(x, y, wid, len, orient, propPal) {
			@Override
			public boolean isVisible() {
				return ScriptGuiScrollBar.this.visible;
			}

		};
		this.set("color", propPal.settings);
		this.set("setMaxItems", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				bar.setItemCount(arg.checkint());
				return NONE;
			}
		});
		this.set("setVisibleItems", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				bar.setVisibleItems(arg.checkint());
				return NONE;
			}
		});
		this.set("setScrollPos", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				bar.focusToItem(arg.checkint());
				return NONE;
			}
		});
		this.set("getScrollPos", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				return valueOf(bar.getOffset());
			}
		});
		//		this.set("setWidth", new OneArgFunction() {
		//			@Override
		//			public LuaValue call(LuaValue arg) {
		//				bar.setWid(arg.checkint());
		//				return NONE;
		//			}
		//		});this.set("setLength", new OneArgFunction() {
		//			@Override
		//			public LuaValue call(LuaValue arg) {
		//				bar.setLen(arg.checkint());
		//				return NONE;
		//			}
		//		});
		//		this.set("getWidth", new OneArgFunction() {
		//			@Override
		//			public LuaValue call(LuaValue arg) {
		//				return valueOf(bar.getOffset());
		//			}
		//		});
		//		this.set("getLength", new OneArgFunction() {
		//			@Override
		//			public LuaValue call(LuaValue arg) {
		//				return valueOf(bar.getOffset());
		//			}
		//		});
		this.set("getOrientation", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				return valueOf(bar.getOrientation().toString());
			}
		});
		this.set("setOrientation", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				bar.setOrientation(Orientation.from(arg.checkjstring()));
				return NONE;
			}
		});
		enableSizeControl();

	}

	@Override
	public void onDraw(Gui g, int mouseX, int mouseY, float partialTicks) {
		super.onDraw(g, mouseX, mouseY, partialTicks);
		bar.onDraw(g, mouseX, mouseY, partialTicks);
	}
	@Override
	public boolean onMouseClick(Gui gui, int x, int y, int buttonNum) {
		if(bar.onMouseClick(gui, x, y, buttonNum)) {
			if (onMouseClick != null) 
				Utils.pcall(onMouseClick, LuaValue.valueOf(x), LuaValue.valueOf(y), LuaValue.valueOf(buttonNum));
			return true;
		}
		return false;
	}
	@Override
	public boolean onMouseClickMove(Gui gui, int x, int y, int buttonNum, long timeSinceClick) {
		if(bar.onMouseClickMove(gui, x, y, buttonNum, timeSinceClick)) {
			if(onMouseDrag!=null) {
				LuaTable args = new LuaTable();
				args.set(1, LuaValue.valueOf(x));
				args.set(2, LuaValue.valueOf(y));
				args.set(3, LuaValue.valueOf(buttonNum));
				args.set(4, LuaValue.valueOf(timeSinceClick));
				Utils.pcall(onMouseDrag,args.unpack());
			}
			return true;
		}
		return false;
	}
	@Override
	public boolean onMouseRelease(Gui gui, int x, int y, int state) {
		if(bar.onMouseRelease(gui, x, y, state)) {
			if(onMouseRelease!=null)
				Utils.pcall(onMouseRelease, LuaValue.valueOf(x), LuaValue.valueOf(y), LuaValue.valueOf(state));
			return true;
		}
		return false;
	}
	@Override
	public boolean onScroll(Gui gui, int i) {
		if(bar.onScroll(gui, i)) {
			Utils.pcall(onScroll, LuaValue.valueOf(i));
			return true;
		}
		return false;
	}
	@Override
	public void setX(int x) {
		bar.setX(x);
	}
	@Override
	public void setY(int y) {
		bar.setY(y);
	}
	@Override
	public void setX(double x) {
		bar.setX((int)x);
	}
	@Override
	public void setY(double y) {
		bar.setY((int)y);
	}
	@Override
	public int getItemHeight() {
		return bar.getItemHeight();
	}
	@Override
	public int getItemWidth() {
		return bar.getItemWidth();
	}
	@Override
	public void setWidth(int i) {
		bar.setWidth(i);
	}
	@Override
	public void setHeight(int i) {
		bar.setHeight(i);
	}
	@Override
	public void setPos(int x, int y) {
		bar.setPos(x, y);
	}
}
