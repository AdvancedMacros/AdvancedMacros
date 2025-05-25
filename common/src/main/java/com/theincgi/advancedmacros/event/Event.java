package com.theincgi.advancedmacros.event;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public abstract class Event {
	
	public CallbackInfo ci;

	public Event(CallbackInfo ci) {
		this.ci = ci;
	}
	
	public boolean isCancelable() {
		return ci.isCancellable();
	}
	
	public void cancel() {
		ci.cancel();
	}
	
	abstract public LuaTable createArgsTable();
}
