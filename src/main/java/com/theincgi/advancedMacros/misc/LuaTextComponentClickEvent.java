package com.theincgi.advancedMacros.misc;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;

import com.theincgi.advancedMacros.lua.LuaDebug.LuaThread;

import net.minecraft.util.text.event.ClickEvent;

public class LuaTextComponentClickEvent extends ClickEvent{
	LuaValue onClick;
	private LuaTextComponent ltc;
	public LuaTextComponentClickEvent(LuaValue onClick, LuaTextComponent ltc) {
		super(null, ltc.getUnformattedComponentText());
		this.ltc = ltc;
		this.onClick =  onClick;
	}
	
	public void click() {
		LuaTable args = new LuaTable();
		args.set(1, ltc.getFormattedText());
		args.set(2, ltc.getUnformattedText());
		LuaThread t;
		if(onClick.istable() && !onClick.get("click").isnil())
			t = new LuaThread(onClick.get("click"), args.unpack(), "LuaTextComponentClick");
		else
			t = new LuaThread(onClick, args.unpack(), "LuaTextComponentClick");
		t.start();
	}
}
