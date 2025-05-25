package com.theincgi.advancedmacros.event;

import com.theincgi.advancedmacros.event.handlers.OnChat;
import com.theincgi.advancedmacros.event.handlers.OnKey;
import com.theincgi.advancedmacros.mixin.events.MixinKeyboard;

public class EventHandlers {
	
	public static OnChat onChat = new OnChat();
	
	/**{@link MixinKeyboard}*/
	public static OnKey onKey  = new OnKey();
	
}
