package com.theincgi.advancedmacros.lua.scriptGui;

import com.theincgi.advancedmacros.AdvancedMacros;
import com.theincgi.advancedmacros.gui.Color;
import com.theincgi.advancedmacros.gui.Gui;
import com.theincgi.advancedmacros.gui.Gui.InputSubscriber;
import com.theincgi.advancedmacros.gui.elements.Drawable;
import com.theincgi.advancedmacros.gui.elements.GuiButton;
import com.theincgi.advancedmacros.gui.elements.Moveable;
import com.theincgi.advancedmacros.lua.functions.Workspaces;
import com.theincgi.advancedmacros.misc.HIDUtils;
import com.theincgi.advancedmacros.misc.Utils;
import com.theincgi.advancedmacros.misc.Workspace;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.ColorHelper;

import org.joml.Matrix4f;
import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaFunction;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

public abstract class ScriptGuiElement extends LuaTable implements Drawable, InputSubscriber, Moveable {

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
            onMouseRelease, onMouseDrag, onKeyPressed, onKeyReleased, onCharTyped,
            onMouseEnter, onMouseExit, onKeyRepeated;
    public float x, y;
    float z;
    public float wid, hei;
    private boolean mouseWasOver = false;
    private boolean mouseIsOver = false;
    boolean visible = true;
    private Group parent;
    Object hoverTintLock = new Object();
    private Object removeLock = new Object();
    private boolean isRemoved = false;
	public final Gui gui;
	Workspace workspace;

    public ScriptGuiElement(Gui gui, Group parent) {
        this(gui, parent, true);
    }

    public ScriptGuiElement(Gui gui, Group initParent, boolean addEventControls) {
    	this.gui = gui;
    	this.workspace = Utils.currentWorkspace();
//        gui.addInputSubscriber(this);
//        gui.addDrawable(this);
        changeParent(initParent);

        //generic properties
        this.set("remove", new ZeroArgFunction() { //ready for garbage collectin
            @Override
            public LuaValue call() {
                synchronized (removeLock) {
                    //System.out.println("******************\n******************\n******************\n");
                    if (isRemoved) {
                        return NONE;
                    }
                    isRemoved = true;
                    if(parent != null) {
                    	parent.children.remove(ScriptGuiElement.this);
                    }
                    return NONE;
                }
            }
        });
        this.set("unremove", new ZeroArgFunction() { //back from the dead
            @Override
            public LuaValue call() {
                synchronized (removeLock) {
                    if (!isRemoved) {
                        return NONE;
                    }
                    isRemoved = false;
                    if(parent != null) {
                    	parent.children.add(ScriptGuiElement.this);
                    }
//                    gui.addInputSubscriber(ScriptGuiElement.this);
//                    gui.addDrawable(ScriptGuiElement.this);
                    
                    return NONE;
                }
            }
        });

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
                setX(arg.checkdouble());
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
                return LuaValue.valueOf(getY());
            }
        });
        this.set("setPos", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue arg1, LuaValue arg2) {
                setPos((int) arg1.checkdouble(), (int) arg2.checkdouble());
                return LuaValue.NONE;
            }
        });
        this.set("getPos", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs arg) {
                LuaTable t = new LuaTable();
                t.set(1, LuaValue.valueOf(getX()));
                t.set(2, LuaValue.valueOf(getY()));
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
                color.setA((int) (arg.checkdouble() * 255));
                return LuaValue.NONE;
            }
        });
        this.set("getOpacity", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.valueOf(color.getA() / 255f);
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
            @Override
            public Varargs invoke(Varargs args) {
                LuaTable out = new LuaTable();
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
        if (addEventControls) {
            addInputControls(this);
        }

        this.set("setParent", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue arg, LuaValue applyTransforms) {
                if (arg instanceof Group) {
                	Group group = (Group) arg;
                	if(group == parent)
                		return NONE;
                	if( applyTransforms.optboolean( true )) {
	                	move( group.x, group.y );
	                	if( parent != null ) {
	                		move( -parent.x, -parent.y );
	                	}
                	}
                    changeParent(group);
                } else {
                    throw new LuaError("arg is not GuiGroup");
                }
                return LuaValue.NONE;
            }
        });
        this.set("setOnMouseEnter", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                onMouseEnter = arg.isnil()? null : arg.checkfunction();;
                return NONE;
            }
        });
        this.set("setOnMouseExit", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                onMouseExit = arg.isnil()? null : arg.checkfunction();;
                return NONE;
            }
        });
        this.set("isHover", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.valueOf(mouseIsOver);
            }
        });
        //TODO set gui workspace on creation
//        this.set("setEventWorkspace", new OneArgFunction() {
//			@Override
//			public LuaValue call(LuaValue arg) {
//				workspace = Workspaces.getWorkspaceByName(arg.checkjstring(1));
//				return workspace.asTable();
//			}
//		});
        this.set("getEventWorkspace", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return workspace.toLuaValue();
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
                setWidth(arg.checkint());
                ;
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
            @Override
            public LuaValue call(LuaValue arg1, LuaValue arg2) {
                int w = arg1.checkint();
                int h = arg2.checkint();
                setWidth(w);
                setHeight(h);
                return NONE;
            }
        });
    }

    private void addInputControls(LuaTable s) {
        s.set("setOnScroll", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                onScroll = arg.isnil()? null : arg.checkfunction();
                return LuaValue.NONE;
            }
        });
        s.set("setOnMouseClick", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                onMouseClick = arg.isnil()? null : arg.checkfunction();
                return LuaValue.NONE;
            }
        });
        s.set("setOnMouseRelease", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                onMouseRelease = arg.isnil()? null : arg.checkfunction();
                return LuaValue.NONE;
            }
        });
        s.set("setOnMouseDrag", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                onMouseDrag = arg.isnil()? null : arg.checkfunction();
                return LuaValue.NONE;
            }
        });
        s.set("setOnKeyPressed", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                onKeyPressed = arg.isnil()? null : arg.checkfunction();
                return LuaValue.NONE;
            }
        });
        s.set("setOnKeyReleased", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                onKeyReleased = arg.isnil()? null : arg.checkfunction();
                return LuaValue.NONE;
            }
        });
        s.set("setOnKeyRepeated", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                onKeyRepeated = arg.isnil()? null : arg.checkfunction();
                return LuaValue.NONE;
            }
        });
        s.set("setOnCharTyped", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                onCharTyped = arg.isnil()? null : arg.checkfunction();
                return LuaValue.NONE;
            }
        });
    }

    @Override
    public void onDraw(DrawContext drawContext, Gui g, int mouseX, int mouseY, float partialTicks) {
        boolean now = GuiButton.isInBounds(mouseX, mouseY, (int) x, (int) y, (int) wid, (int) hei);
        if (now != mouseWasOver) {
            if (now) {
            	mouseIsOver = true;
                onMouseEnter();
            } else {
            	mouseIsOver = false;
                onMouseExit();
            }
            mouseWasOver = now;
        }

    }
    
    public static void fill(DrawContext ctx, float x1, float y1, float x2, float y2, float z, int color) {
//    	fill(ctx, (int)x1,  (int)y1, (int) x2,  (int)y2, z, color);
    	
    	RenderLayer layer = RenderLayer.getGui();
        float i;
        Matrix4f matrix4f = ctx.getMatrices().peek().getPositionMatrix();
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }
        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }
        float f = (float)ColorHelper.Argb.getAlpha(color) / 255.0f;
        float g = (float)ColorHelper.Argb.getRed(color) / 255.0f;
        float h = (float)ColorHelper.Argb.getGreen(color) / 255.0f;
        float j = (float)ColorHelper.Argb.getBlue(color) / 255.0f;
        VertexConsumer vertexConsumer = ctx.getVertexConsumers().getBuffer(layer);
        vertexConsumer.vertex(matrix4f, x1, y1, z).color(g, h, j, f).next();
        vertexConsumer.vertex(matrix4f, x1, y2, z).color(g, h, j, f).next();
        vertexConsumer.vertex(matrix4f, x2, y2, z).color(g, h, j, f).next();
        vertexConsumer.vertex(matrix4f, x2, y1, z).color(g, h, j, f).next();
        ctx.draw();
    }

    public static void resetMouseOver() {

    }

    private void onMouseExit() {
        if (onMouseExit != null) {
        	Utils.setMCThreadWorkspace(workspace);
            Utils.pcall(onMouseExit);
        }
    }

    private void onMouseEnter() {
        if (onMouseEnter != null) {
        	Utils.setMCThreadWorkspace(workspace);
            Utils.pcall(onMouseEnter);
        }
    }

    @Override
    public void setPos(int x, int y) {
        setX(x);
        setY(y);
    }
    
    public void move(int dx, int dy) {
    	setX( dx + getX() );
    	setY( dy + getY() );
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
    public boolean onScroll(Gui gui, double i) {
        if (onScroll != null) {
        	Utils.setMCThreadWorkspace(workspace);
            return Utils.pcall(onScroll, LuaValue.valueOf(i)).toboolean();
        }
        return false;
    }

    @Override
    public boolean onMouseClick(Gui gui, double x, double y, int buttonNum) {
        if (onMouseClick != null && GuiButton.isInBounds(x, y, (int) this.x, (int) this.y, (int) wid, (int) hei)) {
        	Utils.setMCThreadWorkspace(workspace);
            return Utils.pcall(onMouseClick, LuaValue.valueOf(x), LuaValue.valueOf(y), LuaValue.valueOf(buttonNum)).toboolean();
        }
        return false;
    }

    @Override
    public boolean onMouseRelease(Gui gui, double x, double y, int state) {
        if (onMouseRelease != null && GuiButton.isInBounds(x, y, (int) this.x, (int) this.y, (int) wid, (int) hei)) {
        	Utils.setMCThreadWorkspace(workspace);
            return Utils.pcall(onMouseRelease, LuaValue.valueOf(x), LuaValue.valueOf(y), LuaValue.valueOf(state)).toboolean();
        }
        return false;
    }

    @Override
    public boolean onMouseClickMove(Gui gui, double x, double y, int buttonNum, double q, double r) {
        if (onMouseDrag != null && GuiButton.isInBounds(x, y, (int) this.x, (int) this.y, (int) wid, (int) hei)) {
            LuaTable args = new LuaTable();
            args.set(1, x);
            args.set(2, y);
            args.set(3, buttonNum);
            args.set(4, q);
            args.set(5, r);
            Utils.setMCThreadWorkspace(workspace);
            return Utils.pcall(onMouseDrag, args.unpack()).toboolean();
        }
        return false;
    }

    @Override
    public boolean onKeyPressed(Gui gui, int keyCode, int scanCode, int modifiers) {
        if (onKeyPressed != null) {
        	Utils.setMCThreadWorkspace(workspace);
            return Utils.pcall(onKeyPressed,
                    LuaValue.valueOf(HIDUtils.Keyboard.nameOf(keyCode)),
                    LuaValue.valueOf(scanCode),
                    HIDUtils.Keyboard.modifiersToLuaTable(modifiers)
            ).toboolean();
        }
        return false;
    }

    @Override
    public boolean onCharTyped(Gui gui, char typedChar, int mods) {
        if (onCharTyped != null) {
        	Utils.setMCThreadWorkspace(workspace);
            return Utils.pcall(onCharTyped,
                    LuaValue.valueOf(typedChar),
                    HIDUtils.Keyboard.modifiersToLuaTable(mods)
            ).toboolean();
        }
        return false;
    }

    @Override
    public boolean onKeyRelease(Gui gui, int keyCode, int scanCode, int modifiers) {
        if (onKeyReleased != null) {
        	Utils.setMCThreadWorkspace(workspace);
            return Utils.pcall(onKeyReleased, LuaValue.valueOf(HIDUtils.Keyboard.nameOf(keyCode)), LuaValue.valueOf(scanCode), HIDUtils.Keyboard.modifiersToLuaTable(modifiers)).toboolean();
        }
        return false;
    }

    @Override
    public boolean onKeyRepeat(Gui gui, int keyCode, int scanCode, int modifiers, int n) {
        if (onKeyRepeated != null) {
        	Utils.setMCThreadWorkspace(workspace);
            return Utils.pcall(onKeyRepeated, LuaValue.valueOf(HIDUtils.Keyboard.nameOf(keyCode)), LuaValue.valueOf(scanCode), HIDUtils.Keyboard.modifiersToLuaTable(modifiers), LuaValue.valueOf(n)).toboolean();
        }
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
        if (parent != null) {
            this.parent.children.remove(this);
        }
        this.parent = g;
        g.setParentControls(this);
        g.children.add(this);
    }

    //	@Override
    //	protected void finalize() throws Throwable {
    //		AdvancedMacros.logFunc.call("&6Debug: &eGui element is being finalized");
    //	}

}
