package com.theincgi.advancedmacros.mixin.events;

import static com.theincgi.advancedmacros.event.handlers.OnAttackEntity.onAttackEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.theincgi.advancedmacros.event.events.AttackEntityEvent;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(PlayerEntity.class)
public class MixinPlayerEntity {

	@Inject(
		method = "attack(Lnet/minecraft/entity/Entity;)V",
		at = @At("HEAD")
	)
	private void am_onAttackEntity(Entity target, CallbackInfo ci) {
		PlayerEntity player = (PlayerEntity)(Object)this;
		onAttackEntity.onEvent(new AttackEntityEvent(player, target, ci));
	}
	
	
}
