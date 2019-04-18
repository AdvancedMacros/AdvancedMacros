package com.theincgi.advancedMacros.hud.hud3D;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.misc.CallableTable;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

public class Hud3D extends LuaTable{
	public Hud3D() {
		for (Hud3DOpCode op : Hud3DOpCode.values()) {
			this.set(op.toString(), new CallableTable(op.getDocLocation(), new DoOp(op)));
		}
		
	}
	
	private class DoOp extends VarArgFunction {
		Hud3DOpCode op;
		public DoOp(Hud3DOpCode op) {
			super();
			this.op = op;
		}
		@Override
		public Varargs invoke(Varargs args) {
			EntityPlayerSP p = AdvancedMacros.getMinecraft().player;
			switch (op) {
			case clearAll:{
				AdvancedMacros.forgeEventHandler.clearWorldHud();
				return LuaValue.NONE;
			}
			case newBlock:{
				HoloBlock hb = new HoloBlock();
				hb.setPos(args.optint(1, (int) Math.floor(p.posX)),
						  args.optint(2, (int) Math.floor(p.posY)),
						  args.optint(3, (int) Math.floor(p.posZ)));
				hb.setTexture(Utils.parseTexture(args.arg(4)));
				return hb.getControls();
			}
			case newText:{
				HudText text = new HudText();
				text.setText(args.arg1().optjstring(""));
				text.setPos(args.optint(2, (int) Math.floor(p.posX)),
						  args.optint(3, (int) Math.floor(p.posY)),
						  args.optint(4, (int) Math.floor(p.posZ)));
				return text.getControls();
			}
			case newPane:{
				Hud3DPane pane = new Hud3DPane(args.arg(1).checkjstring());
				pane.setPos(args.optint(2, (int) Math.floor(p.posX)),
						    args.optint(3, (int) Math.floor(p.posY)),
						    args.optint(4, (int) Math.floor(p.posZ)));
				if(!args.arg(5).isnil())
					pane.changeTexture(args.arg(5));
				return pane.getControls();
			}
			case newObject:{
				Hud3DElement element = new Hud3DElement();
				element.setPos(	args.optint(1, (int) Math.floor(p.posX)),
						  		args.optint(2, (int) Math.floor(p.posY)),
					  			args.optint(3, (int) Math.floor(p.posZ)));
				return element.getControls();
			}
			default:
				throw new LuaError("Unimplemented function "+op);
			}
		}
	}
	
	private static enum Hud3DOpCode {
		newBlock,
		newText,
		newPane,
		newObject,
		clearAll;
		
		public String[] getDocLocation(){
			String[] loc = new String[2];
			loc[0] = "hud3D";
			switch (this) {
			case clearAll:
			case newBlock:
			case newText:
			case newPane:
				loc[1] = this.toString();
				return loc;
			default:
				return null;
			}
		}
	}
}
