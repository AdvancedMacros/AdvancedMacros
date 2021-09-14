package com.theincgi.advancedMacros.misc;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaValue;

import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.event.HoverEvent.Action;

public class LuaTextComponent extends StringTextComponent{
	private String text;
	private LuaValue action;
	private boolean allowHover;
	
	public LuaTextComponent(String text) {
		super(text);
	
	}
	public LuaTextComponent(String text, LuaValue action, boolean allowHover) {
		super(text);
		this.text = text;
		this.action = action;
		this.allowHover = allowHover;
		if( action.isCallable() )
			getStyle().setClickEvent( new LuaTextComponentClickEvent(action, this));
		if( (action.isstring() || action.istable()) && allowHover) {
			if(action.istable() && !action.get("click").isnil()) {
				if(!action.get("click").isCallable())
					throw new LuaError("'click' value of clickable text component table must be callable.");
				getStyle().setClickEvent(new LuaTextComponentClickEvent(action.get("click"), this));
			}
			if(action.istable() && !action.get("hover").isnil())
				getStyle().setHoverEvent(new HoverEvent(Action.SHOW_TEXT, Utils.toTextComponent(action.get("hover").tojstring(), null, false).a));
			else
				getStyle().setHoverEvent(new HoverEvent(Action.SHOW_TEXT, Utils.toTextComponent(action.tojstring(), null, false).a));
		}
			
	}

	
//	@Override
//	public String getUnformattedComponentText() {
//		return text;
//	}

//	@Override
//	public ITextComponent createCopy() {
//		return new LuaTextComponent(this.text, action, allowHover);
//	}

}
