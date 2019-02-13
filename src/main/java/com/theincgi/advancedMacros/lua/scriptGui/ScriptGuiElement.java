package com.theincgi.advancedMacros.lua.scriptGui;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaFunction;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.gui.Color;
import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.gui.Gui.InputSubscriber;
import com.theincgi.advancedMacros.gui.elements.Drawable;
import com.theincgi.advancedMacros.gui.elements.GuiButton;
import com.theincgi.advancedMacros.gui.elements.Moveable;
import com.theincgi.advancedMacros.misc.Utils;

public abstract class ScriptGuiElement extends LuaTable implements Drawable, InputSubscriber, Moveable{
	//enable/disable draw
	//isDrawing
	//setHoverTint(color)
	//set/get pos
	//setOpacity
	//optional setColor
	protected Color color = Color.BLACK;
	private int colorInt;
	private Color hoverTint = Color.CLEAR;
	private int colorTintInt;
	LuaFunction onScroll, onMouseClick,
	onMouseRelease, onMouseDrag, onKeyPressed, onKeyReleased, onKeyRepeated,
	onMouseEnter, onMouseExit;
	public float x, y;
	float z;
	public float wid, hei;
	private boolean mouseWasOver = false;
	boolean visible = true;
	private Group parent;
	Object hoverTintLock = new Object(); 
	
	public ScriptGuiElement(Gui gui, Group parent) {this(gui, parent, true);}
	public ScriptGuiElement(Gui gui, Group parent, boolean addEventControls) {
		gui.inputSubscribers.add(this);
		gui.addDrawable(this);
		
		//generic properties
		this.set("setVisible", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				visible = arg.checkboolean();
				return LuaValue.NONE;
			}
		});
		this.set("isVisible", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(visible);
			}
		});
		this.set("setX", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				setX( arg.checkdouble());
				return LuaValue.NONE;
			}
		});
		this.set("getX", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(getX());
			}
		});
		this.set("setY", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				setY(arg.checkdouble());
				return LuaValue.NONE;
			}
		});
		this.set("getY", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(y);
			}
		});
		this.set("setPos", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue arg1, LuaValue arg2) {
				setPos((int)arg1.checkdouble(), (int)arg2.checkdouble());
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
		this.set("getZ", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(z);
			}
		});
		this.set("setZ", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				z = arg.checkint();
				return LuaValue.NONE;
			}
		});
		
		
		this.set("setOpacity", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				color.setA((int) (arg.checkdouble()*255));
				return LuaValue.NONE;
			}
		});
		this.set("getOpacity", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(color.getA()/255f);
			}
		});
		
		this.set("getWidth", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(getItemWidth());
			}
		});
		this.set("getHeight", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(getItemHeight());
			}
		});
		this.set("getSize", new VarArgFunction() {
			@Override public Varargs invoke(Varargs args) {
				LuaTable out=new LuaTable();
				out.set(1, getItemWidth());
				out.set(2, getItemHeight());
				return out.unpack();
			}
		});

		this.set("setHoverTint", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				//TODO check for additional synchronization 
				synchronized (hoverTintLock) {
					hoverTint = Utils.parseColor(args, AdvancedMacros.COLOR_SPACE_IS_255);
					colorTintInt = hoverTint.toInt();
				}
				return LuaValue.NONE;
			}
		});
		this.set("getHoverTint", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				synchronized (hoverTintLock) {
					return hoverTint.toLuaValue(AdvancedMacros.COLOR_SPACE_IS_255);
				}
			}
		});

		//event section
		if(addEventControls)
			addInputControls(this);
		
		
		this.set("setParent", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				if(arg instanceof Group)
					changeParent((Group) arg);
				else
					throw new LuaError("arg is not GuiGroup");
				return LuaValue.NONE;
			}
		});
		this.set("setOnMouseEnter", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				onMouseEnter = arg.checkfunction();
				return NONE;
			}
		});
		this.set("setOnMouseExit", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				onMouseExit = arg.checkfunction();
				return NONE;
			}
		});
		this.set("isHover", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(mouseWasOver);
			}
		});
		this.set("remove", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				gui.removeDrawables(ScriptGuiElement.this);//.remove(this);
				gui.inputSubscribers.remove(ScriptGuiElement.this);
				return NONE;
			}
		});
		parent.setParentControls(this);

	}
	
	public void enableColorControl() {
		set("setColor", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				color = Utils.parseColor(args, AdvancedMacros.COLOR_SPACE_IS_255);
				colorInt = color.toInt();
				return LuaValue.NONE;
			}
		});
		set("getColor", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return color.toLuaValue(AdvancedMacros.COLOR_SPACE_IS_255);
			}
		});
	}
	
	public void enableSizeControl() {
		set("setWidth", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				setWidth(arg.checkint());;
				return LuaValue.NONE;
			}
		});
		set("setHeight", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				setHeight(arg.checkint());
				return LuaValue.NONE;
			}
		});
		set("setSize", new TwoArgFunction() {
			@Override public LuaValue call(LuaValue arg1, LuaValue arg2) {
				int w = arg1.checkint();
				int h = arg2.checkint();
				setWidth(w);
				setHeight(h);
				return null;
			}
		});
	}
	private void addInputControls(LuaTable s) {
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
	}
	@Override
	public void onDraw(Gui g, int mouseX, int mouseY, float partialTicks) {
		boolean now = GuiButton.isInBounds(mouseX, mouseY, (int)x, (int)y, (int)wid, (int)hei);
		if(now!=mouseWasOver) {
			if(now) {
				onMouseEnter();
			}else{
				onMouseExit();
			}
			mouseWasOver = now;
		}
		
	}
	
	private void onMouseExit() {
		if(onMouseExit!=null)
			Utils.pcall(onMouseExit);
	}

	private void onMouseEnter() {
		if(onMouseEnter!=null)
			Utils.pcall(onMouseEnter);
	}

	@Override
	public void setPos(int x, int y) {
		setX(x);
		setY(y);
	}
	
	@Override
	public void setX(int x) {
		this.x = x;
	}
	@Override
	public void setY(int y) {
		this.y = y;
	}
	public void setX(double x) {
		this.x = (float) x;
	}
	public void setY(double y) {
		this.y = (float) y;
	}
	
	@Override
	public void setVisible(boolean b) {
		visible = b;
	}

	

	@Override
	public int getX() {
		return (int) x;
	}

	@Override
	public int getY() {
		return (int) y;
	}

	@Override
	public boolean onScroll(Gui gui, int i) {
		if(onScroll != null) {
			return Utils.pcall(onScroll, LuaValue.valueOf(i)).toboolean();
		}
		return false;
	}

	@Override
	public boolean onMouseClick(Gui gui, int x, int y, int buttonNum) {
		if(onMouseClick != null && GuiButton.isInBounds(x, y, (int)this.x, (int)this.y, (int)wid, (int)hei))
			return Utils.pcall(onMouseClick, LuaValue.valueOf(x), LuaValue.valueOf(y), LuaValue.valueOf(buttonNum)).toboolean();
		return false;
	}

	@Override
	public boolean onMouseRelease(Gui gui, int x, int y, int state) {
		if(onMouseRelease!=null && GuiButton.isInBounds(x, y, (int)this.x, (int)this.y, (int)wid, (int)hei))
			return Utils.pcall(onMouseRelease, LuaValue.valueOf(x), LuaValue.valueOf(y), LuaValue.valueOf(state)).toboolean();
		return false;
	}

	@Override
	public boolean onMouseClickMove(Gui gui, int x, int y, int buttonNum, long timeSinceClick) {
		if(onMouseDrag!=null && GuiButton.isInBounds(x, y, (int)this.x, (int)this.y, (int)wid, (int)hei)) {
			LuaTable args = new LuaTable();
			args.set(1, LuaValue.valueOf(x));
			args.set(2, LuaValue.valueOf(y));
			args.set(3, LuaValue.valueOf(buttonNum));
			args.set(4, LuaValue.valueOf(timeSinceClick));
			return Utils.pcall(onMouseDrag,args.unpack()).toboolean();
		}
		return false;
	}

	@Override
	public boolean onKeyPressed(Gui gui, char typedChar, int keyCode) {
		if(onKeyPressed!=null)
			return Utils.pcall(onKeyPressed, LuaValue.valueOf(typedChar), LuaValue.valueOf(keyCode)).toboolean();
		return false;
	}

	@Override
	public boolean onKeyRepeat(Gui gui, char typedChar, int keyCode, int repeatMod) {
		if(onKeyRepeated!=null)
			return Utils.pcall(onKeyRepeated, LuaValue.valueOf(typedChar), LuaValue.valueOf(keyCode), LuaValue.valueOf(repeatMod)).toboolean();
		return false;
	}

	@Override
	public boolean onKeyRelease(Gui gui, char typedChar, int keyCode) {
		if(onKeyReleased!=null)
			return Utils.pcall(onKeyReleased, LuaValue.valueOf(typedChar), LuaValue.valueOf(keyCode)).toboolean();
		return false;
	}
	public int getColorInt() {
		return colorInt;
	}
	public int getColorTintInt() {
		return colorTintInt;
	}
	public Color getColor() {
		return color;
	}
	public Color getHoverTint() {
		return hoverTint;
	}
	
	public void changeParent(Group g) {
		if(parent!=null)
			this.parent.children.remove(this);
		this.parent = g;
		g.setParentControls(this);
		g.children.add(this);
	}

}