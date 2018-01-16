package com.theincgi.advancedMacros.hud.hud2D;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class Hud2D extends LuaTable{
	public Hud2D() {
		//rectangle, image, easy progress bar, text[, canvas?]
		
		this.set("addRectangle", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return new Hud2D_Rectangle().getControls();
			}
		});
		this.set("addBox", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return new Hud2D_Box().getControls();
			}
		});
		this.set("addImage", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return new Hud2D_Image().getControls();
			}
		});
		//this.set("addProgressBar", new Hud2D_ProgressBar());
		this.set("addText", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return new Hud2D_Text().getControls();
			}
		});
//		this.set("addLine", new ZeroArgFunction() {
//			@Override
//			public LuaValue call() {
//				return new Hud2D_Line().getControls();
//			}
//		});
		this.set("clearAll", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				AdvancedMacros.forgeEventHandler.clear2DHud();
				return LuaValue.NONE;
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
	}
}
