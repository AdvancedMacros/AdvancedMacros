package com.theincgi.advancedmacros.event.handlers;

import com.theincgi.advancedmacros.event.CombinedEventHandler.EventName;
import com.theincgi.advancedmacros.event.EventHandler;
import com.theincgi.advancedmacros.event.events.AttackEntityEvent;
import com.theincgi.advancedmacros.mixin.events.MixinPlayerEntity;

public class OnAttackEntity extends EventHandler<AttackEntityEvent> {
	
	/**{@link MixinPlayerEntity}*/
	public static OnAttackEntity onAttackEntity = new OnAttackEntity();
	
	@Override
	public String getEventValue(AttackEntityEvent event) {
		return EventName.AttackEntity.name();
	}
	
}
