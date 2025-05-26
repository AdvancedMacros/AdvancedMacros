package com.theincgi.advancedmacros.event.handlers;

import com.theincgi.advancedmacros.event.FilterEvent;
import com.theincgi.advancedmacros.event.CombinedEventHandler.EventName;
import com.theincgi.advancedmacros.event.FilterEvent.EventProcessor;
import com.theincgi.advancedmacros.mixin.events.MixinChatHud.ChatEvent;

public class OnChat extends FilterEvent<ChatEvent> {
	
	public static OnChat onChat = new OnChat();
	
	@Override
	public String getEventValue(ChatEvent event) {
		return EventName.ChatFilter.name();
	}
	
	@Override
	public String getUnfilteredEventName() {
		return EventName.Chat.name();
	}

	@Override
	public void fireEvent(FilterEvent<ChatEvent>.EventProcessor event) {
		throw new RuntimeException("Not implemented!");
	}
	
}
