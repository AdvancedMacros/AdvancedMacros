package com.theincgi.advancedMacros.lua.scriptGui;

import java.util.ArrayList;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.gui.Gui.InputSubscriber;
import com.theincgi.advancedMacros.gui.elements.Drawable;
import com.theincgi.advancedMacros.gui.elements.Moveable;


public class Group extends LuaTable implements Moveable, InputSubscriber, Drawable{
	//gui group, xy 0,0
	//passes events to child elements/groups
	//child element needs getParent detail functions
	//moving a group should shift all child elements
	ArrayList<Object> children = new ArrayList<>();
	boolean groupVisiblity = true;
	int x = 0, y = 0;
	Group parent = null;
	//LuaFunction widthCalculate, heightCalculate;
	
	public Group(Group parent) {
		this();
		changeParent(parent);
	}
	public Group() {
		this.set("setVisible", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				groupVisiblity = arg.checkboolean();
				return LuaValue.NONE;
			}
		});
		this.set("isVisible", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(groupVisiblity);
			}
		});
		this.set("move", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue arg1, LuaValue arg2) {
				int dx = arg1.checkint();
				int dy = arg2.checkint();
				move(dx, dy);
				return LuaValue.NONE;
			}
		});
		this.set("setPos", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue arg1, LuaValue arg2) {
				setPos(arg1.checkint(), arg2.checkint());
				return LuaValue.NONE;
			}
		});
		this.set("getPos", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs arg) {
				LuaTable t = new LuaTable();
				t.set(1, LuaValue.valueOf(x));
				t.set(2, LuaValue.valueOf(y));
				return t.unpack();
			}
		});
		this.set("getX", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return valueOf(x);
			}
		});
		this.set("getY", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return valueOf(y);
			}
		});
		this.set("setParent", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				if(arg instanceof Group)
					changeParent((Group)arg);
				else
					throw new LuaError("Arg not a GuiGroup");
				return LuaValue.NONE;
			}
		});
		this.set("addSubGroup", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return new Group(Group.this);
			}
		});
		
		this.set("getChildren", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				LuaTable result = new LuaTable();
				for (Object o : children) {
					if(o instanceof LuaTable)
						result.set(result.length()+1, (LuaValue) o);
				}
				return result;
			}
		});
		
		this.set("__class", "advancedMacros.GuiGroup");
		//		controls.set("setWidthCalculate", new OneArgFunction() {
		//			@Override
		//			public LuaValue call(LuaValue arg) {
		//				widthCalculate = arg.checkfunction();
		//				return LuaValue.NONE;
		//			}
		//		});
		//		controls.set("setHeightCalculate", new OneArgFunction() {
		//			@Override
		//			public LuaValue call(LuaValue arg) {
		//				heightCalculate = arg.checkfunction();
		//				return LuaValue.NONE;
		//			}
		//		});
	}

	
	
	@Override
	public void onDraw(Gui g, int mouseX, int mouseY, float partialTicks) {
		if(groupVisiblity)
			for (int i = 0; i < children.size(); i++) {
				if(children.get(i) instanceof ScriptGuiElement) {
					ScriptGuiElement child = (ScriptGuiElement) children.get(i);
					child.onDraw(g, mouseX, mouseY, partialTicks);
				}
			}
	}


	@Override
	public boolean onScroll(Gui gui, double i) {
		int di = (int) Math.signum(i);
		if(groupVisiblity)
			for (int j = 0; j < children.size(); j++) {
				if(children.get(j) instanceof InputSubscriber) {
					InputSubscriber child = (InputSubscriber) children.get(di);
					if(child.onScroll(gui, i)) return true;
				}
			}
		return false;
	}


	@Override
	public boolean onMouseClick(Gui gui, double x, double y, int buttonNum) {
		if(groupVisiblity)
			for (int j = 0; j < children.size(); j++) {
				if(children.get(j) instanceof InputSubscriber) {
					InputSubscriber child = (InputSubscriber) children.get(j);
					if(child.onMouseClick(gui, x, y, buttonNum)) return true;
				}
			}
		return false;
	}


	@Override
	public boolean onMouseRelease(Gui gui, double x, double y, int state) {
		if(groupVisiblity)
			for (int j = 0; j < children.size(); j++) {
				if(children.get(j) instanceof InputSubscriber) {
					InputSubscriber child = (InputSubscriber) children.get(j);
					if(child.onMouseRelease(gui, x, y, state)) return true;
				}
			}
		return false;
	}


	@Override
	public boolean onMouseClickMove(Gui gui, double x, double y, int buttonNum, double q, double r) {
		if(groupVisiblity)
			for (int j = 0; j < children.size(); j++) {
				if(children.get(j) instanceof InputSubscriber) {
					InputSubscriber child = (InputSubscriber) children.get(j);
					if(child.onMouseClickMove(gui, x, y, buttonNum, q, r)) return true;
				}
			}
		return false;
	}


	@Override
	public boolean onCharTyped(Gui gui, char typedChar, int mods) {
		if(groupVisiblity)
			for (int j = 0; j < children.size(); j++) {
				if(children.get(j) instanceof InputSubscriber) {
					InputSubscriber child = (InputSubscriber) children.get(j);
					if(child.onCharTyped(gui, typedChar, mods)) return true;
				}
			}
		return false;
	}
	@Override
	public boolean onKeyPressed(Gui gui, int keyCode, int scanCode, int modifiers) {
		if(groupVisiblity)
			for (int j = 0; j < children.size(); j++) {
				if(children.get(j) instanceof InputSubscriber) {
					InputSubscriber child = (InputSubscriber) children.get(j);
					if(child.onKeyPressed(gui, keyCode, scanCode, modifiers)) return true;
				}
			}
		return false;
	}


//	@Override
//	public boolean onKeyRepeat(Gui gui, char typedChar, int keyCode, int repeatMod) {
//		if(groupVisiblity)
//			for (int j = 0; j < children.size(); j++) {
//				if(children.get(j) instanceof InputSubscriber) {
//					InputSubscriber child = (InputSubscriber) children.get(j);
//					if(child.onKeyRepeat(gui, typedChar, keyCode, repeatMod)) return true;
//				}
//			}
//		return false;
//	}


	@Override
	public boolean onKeyRelease(Gui gui, char typedChar, int keyCode) {
		if(groupVisiblity)
			for (int j = 0; j < children.size(); j++) {
				if(children.get(j) instanceof InputSubscriber) {
					InputSubscriber child = (InputSubscriber) children.get(j);
					if(child.onKeyRelease(gui, typedChar, keyCode)) return true;
				}
			}
		return false;
	}


	@Override
	public void setPos(int x, int y) {
		int dx = x - this.getX();
		int dy = y - this.getY();
		move(dx, dy);
	}
	
	@Override
	public void setX(int x) {
		setPos(x, 0);
	}
	@Override
	public void setY(int y) {
		setPos(0, y);
	}
	
	@Override
	public void setVisible(boolean b) {
		this.groupVisiblity = b;
	}


	@Override
	public int getItemHeight() {
		//return heightCalculate.call().checkint();
		return 0;
	}


	@Override
	public int getItemWidth() {
		//return widthCalculate.call().checkint();
		return 0;
	}


	@Override
	public void setWidth(int i) {
		//System.err.println("Group#setWidth called, function is used to calculate width");
	}


	@Override
	public void setHeight(int i) {
		//System.err.println("Group#setHeight called, function is used to calculate height");
	}


	@Override
	public int getX() {
		return x;
	}


	@Override
	public int getY() {
		return y;
	}

	public void move(int dx, int dy) {
		for (int i = 0; i < children.size(); i++) {
			if(children.get(i) instanceof Moveable) {
				Moveable e = (Moveable) children.get(i);
			e.setPos(e.getX() + dx, e.getY() + dy);
			}
		}
		x+=dx;
		y+=dy;
	}


//	public void setParentControls(ExtendedLuaTableObject element) {
//		element.set("getParent", controls);
//	}
	public void setParentControls(LuaTable element) {
		element.set("getParent", this); //TODO inspect
	}

	@Override
	public int type() {
		return LuaValue.TUSERDATA;
	}


	@Override
	public String typename() {
		return LuaValue.TYPE_NAMES[type()];
	}
	
	
	//remove from old parent, change controls to use new parent, save, add to list of children in new list
	public void changeParent(Group arg) {
		if(parent != null)
			this.parent.children.remove(this);
		arg.setParentControls(this);
		this.parent = arg;
		arg.children.add(this);
	}
}
