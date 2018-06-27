package com.theincgi.advancedMacros.lua.scriptGui;

import java.util.HashMap;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.gui.elements.ColorTextArea;
import com.theincgi.advancedMacros.gui.elements.WidgetID;
import com.theincgi.advancedMacros.misc.Utils;

/**
 * Script controled color text area
 * */
public class GuiCTA extends ScriptGuiElement{
	ColorTextArea cta;
	public GuiCTA(Gui gui, Group parent) {
		super(gui, parent, false);
		cta = new ColorTextArea(AdvancedMacros.editorGUI.getCta().getWidgetID(), gui);
		cta.setVisible(true);
		enableSizeControl();
		cta.setEditable(true);
		//set("setFontSize")
		set("setText", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				cta.setText(arg.checkjstring());
				return NONE;
			}
		});
		set("getText", new ZeroArgFunction() {
			@Override public LuaValue call() {
				return valueOf(cta.getText());
			}
		});
		set("setSyntaxHighlight", new OneArgFunction() {
			@Override public LuaValue call(LuaValue arg) {
				cta.doSyntaxHighlighting = arg.checkboolean();
				return NONE;
			}
		});
		set("isSyntaxHighlight", new ZeroArgFunction() {
			@Override public LuaValue call() {
				return valueOf(cta.doSyntaxHighlighting);
			}
		});
		set("isEdited", new ZeroArgFunction() {
			@Override public LuaValue call() {
				return valueOf(cta.isNeedsSave());
			}
		});
		set("isFocused", new ZeroArgFunction() {
			@Override public LuaValue call() {
				return valueOf(cta.isFoucused());
			}
		});
		set("setFocused", new OneArgFunction() {
			@Override public LuaValue call(LuaValue arg) {
				cta.setFocused(arg.checkboolean());
				return NONE;
			}
		});
		set("setEditable", new OneArgFunction() {
			@Override public LuaValue call(LuaValue arg) {
				cta.setEditable(arg.checkboolean());;
				return NONE;
			}
		});
		set("isEditable", new ZeroArgFunction() {
			@Override public LuaValue call() {
				return valueOf(cta.isEditable());
			}
		});
		
//		this.set("setX", new OneArgFunction() {
//			@Override
//			public LuaValue call(LuaValue arg) {
//				cta.setPos( (int) arg.checkdouble(), cta.getY() );
//				return LuaValue.NONE;
//			}
//		});
//		this.set("getX", new ZeroArgFunction() {
//			@Override
//			public LuaValue call() {
//				return LuaValue.valueOf(cta.getX());
//			}
//		});
//		this.set("setY", new OneArgFunction() {
//			@Override
//			public LuaValue call(LuaValue arg) {
//				cta.setPos( cta.getX(), (int) arg.checkdouble());
//				return LuaValue.NONE;
//			}
//		});
//		this.set("getY", new ZeroArgFunction() {
//			@Override
//			public LuaValue call() {
//				return LuaValue.valueOf(cta.getY());
//			}
//		});
		this.set("setOpacity", NIL);
		this.set("getOpacity", NIL);
		
//		this.set("getWidth", new ZeroArgFunction() {
//			@Override
//			public LuaValue call() {
//				return LuaValue.valueOf(cta.getItemWidth());
//			}
//		});
//		this.set("getHeight", new ZeroArgFunction() {
//			@Override
//			public LuaValue call() {
//				return LuaValue.valueOf(cta.getItemHeight());
//			}
//		});

		this.set("setHoverTint", NIL);
		this.set("getHoverTint", NIL);
		
		
		
		
		
		
		
		set("updateKeywords", new ZeroArgFunction() {
			@Override public LuaValue call() {
				cta.updateKeywords();
				return NONE;
			}
		});
		set("getKeywords", new ZeroArgFunction() {
			@Override public LuaValue call() {
				LuaTable kw = new LuaTable();
				kw.set("tables", Utils.toLuaTable(cta.tables.keySet()));
				kw.set("functions", Utils.toLuaTable(cta.functions.keySet())); //TODO control tooltip
				kw.set("variables", Utils.toLuaTable(cta.variables.keySet()));
				kw.set("keywords", Utils.toLuaTable(cta.keywords.keySet()));
				return kw;
			}
		});
		set("setKeywords", new TwoArgFunction() {
			
			@Override
			public LuaValue call(LuaValue arg1, LuaValue arg2) {
				if(arg1.istable()) {
					LuaValue n = arg1.next(NIL).arg1();
					while(!n.isnil()) {
						setKeywords(n.checkjstring() , arg1.get(n).checktable());
						n = arg1.next(n).arg1();
					}
				}else if(arg1.isstring()) {
					setKeywords(arg1.checkjstring(), arg2.checktable());
				}else {
					throw new LuaError(String.format("Invalid arguments <table>/<string, table> expected, got <%s, %s>", arg1.typename(), arg2.typename()));
				}
				return NONE;
			}
			public void setKeywords(String type, LuaTable values) {
				HashMap<String, Object> map = null;
				switch (type) {
				case "tables":
					map = cta.tables;
					break;
				case "functions":
					map = cta.functions;
					break;
				case "variables":
					map = cta.variables;
					break;
				case "keywords":
					for( int k = 1; k<=values.length(); k++) {
						cta.keywords.put(values.get(k).tojstring(), true);
					}
					break;
				default:
					throw new LuaError("Un-used table '"+type+"' [tables/functions/variables/keywords]");
				}
				for( int k = 1; k<=values.length(); k++) {
					map.put(values.get(k).tojstring(), true);
				}
			}
		});
	}
	
	@Override
	public void onDraw(Gui g, int mouseX, int mouseY, float partialTicks) {
		super.onDraw(g, mouseX, mouseY, partialTicks);
		if(!visible) return;
		cta.setPos((int)x, (int)y);
		cta.resize((int)wid, (int)hei);
		cta.onDraw(g, mouseX, mouseY, partialTicks);
	}

	
	
	

	public ColorTextArea getCTA() {
		return cta;
	}

	@Override
	public int getItemHeight() {
		// TODO Auto-generated method stub
		return (int) hei;
	}

	@Override
	public int getItemWidth() {
		// TODO Auto-generated method stub
		return (int) wid;
	}

	@Override
	public void setWidth(int i) {
		wid = i;
	}

	@Override
	public void setHeight(int i) {
		hei = i;
	}
	
	@Override
	public boolean onMouseClick(Gui gui, int x, int y, int buttonNum) {
		return cta.onMouseClick(gui, x, y, buttonNum);
	}
	@Override
	public boolean onMouseClickMove(Gui gui, int x, int y, int buttonNum, long timeSinceClick) {
		return cta.onMouseClickMove(gui, x, y, buttonNum, timeSinceClick);
	}
	@Override
	public boolean onMouseRelease(Gui gui, int x, int y, int state) {
		return cta.onMouseRelease(gui, x, y, state);
	}
	@Override
	public boolean onKeyPressed(Gui gui, char typedChar, int keyCode) {
		return cta.onKeyPressed(gui, typedChar, keyCode);
	}
	@Override
	public boolean onKeyRelease(Gui gui, char typedChar, int keyCode) {
		return cta.onKeyRelease(gui, typedChar, keyCode);
	}
	@Override
	public boolean onKeyRepeat(Gui gui, char typedChar, int keyCode, int repeatMod) {
		return cta.onKeyRepeat(gui, typedChar, keyCode, repeatMod);
	}
	@Override
	public boolean onScroll(Gui g, int i) {
		return cta.onScroll(g, i);
	}
}
