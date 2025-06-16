package com.theincgi.advancedmacros.mixin.events;

import static com.theincgi.advancedmacros.event.handlers.OnDeath.onDeath;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.theincgi.advancedmacros.event.events.DeathEvent;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {

	@Inject(
			method = "onDeath",
			at = @At("HEAD")
		)
		private void am_onDeath(DamageSource damageSource, CallbackInfo ci) {
			onDeath.onEvent(new DeathEvent(damageSource, (LivingEntity)(Object)this, ci));
		}
	
}
