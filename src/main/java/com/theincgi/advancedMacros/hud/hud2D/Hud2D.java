package com.theincgi.advancedMacros.hud.hud2D;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.lua.functions.ScriptGui.OpCodes;
import com.theincgi.advancedMacros.misc.CallableTable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class Hud2D extends LuaTable{
	public Hud2D() {
		//this.set("newProgressBar", new Hud2D_ProgressBar());
		
		for (Hud2DOpCode op : Hud2DOpCode.values()) {
			this.set(op.toString(), new CallableTable(op.getDocLocation() , new DoOp(op)));
		}
		
		
	}
	
	private class DoOp extends VarArgFunction {
		Hud2DOpCode code;
		public DoOp(Hud2DOpCode code) {
			super();
			this.code = code;
		}
		
		@Override
		public Varargs invoke(Varargs args) {
			switch (code) {
			case clearAll:
				AdvancedMacros.forgeEventHandler.clear2DHud();
				return LuaValue.NONE;
			case getSize:
				LuaTable temp = new LuaTable();
				Minecraft mc = Minecraft.getMinecraft();
				ScaledResolution scaled = new ScaledResolution(mc);
				temp.set(1, LuaValue.valueOf(scaled.getScaledWidth()));
				temp.set(2, LuaValue.valueOf(scaled.getScaledHeight()));
				return temp.unpack();
			case newBox:
				Hud2D_Box box = new Hud2D_Box();
				box.x         = (float) args.optdouble(1, 0);
				box.y         = (float) args.optdouble(2, 0);
				box.wid       = (float) args.optdouble(3, 0);
				box.hei       = (float) args.optdouble(4, 0);
				box.thickness = (float) args.optdouble(5, 1);
				return box.controls;
			case newImage:
				Hud2D_Image img = new Hud2D_Image();
				img.setTexture(args.optvalue(1, LuaValue.NIL));
				img.x   = (float) args.optdouble(2, 0);
				img.y   = (float) args.optdouble(3, 0);
				img.wid = (float) args.optdouble(4, 0);
				img.hei = (float) args.optdouble(5, 0);
				return img.controls;
			case newItem:
				Hud2d_itemIcon gii = new Hud2d_itemIcon();
				if(!args.arg1().isnil())
					gii.setStack(args.checkjstring(1));
				gii.x = args.optint(2, 0);
				gii.y = args.optint(3, 0);
				gii.setCount(args.optint(4, 1));
				return gii.controls;
			case newRectangle:
				Hud2D_Rectangle rect = new Hud2D_Rectangle();
				rect.x   = (float) args.optdouble(1, 0);
				rect.y   = (float) args.optdouble(2, 0);
				rect.wid = (float) args.optdouble(3, 0);
				rect.hei = (float) args.optdouble(4, 0);
				return rect.controls;
			case newText:
				Hud2D_Text text = new Hud2D_Text();
				text.text = args.optjstring(1, "");
				text.x    = (float) args.optdouble(2, 0);
				text.y    = (float) args.optdouble(3, 0);
				text.size = (float) args.optdouble(4, 12);
				return text.controls;
			default:
				throw new LuaError("Unimplemented method "+code);
			}
		}
	}
	
	private enum Hud2DOpCode {
		newRectangle,
		newBox,
		newImage,
		newText,
		newItem,
		clearAll,
		getSize;
		
		public String[] getDocLocation(){
			String[] loc = new String[2];
			loc[0] = "hud2D";
			switch (this) {
			case clearAll:
			case getSize:
			case newBox:
			case newImage:
			case newItem:
			case newRectangle:
			case newText:
				loc[1] = this.toString();
				return loc;
			default:
				return null;
			}
		}
	} 
}
