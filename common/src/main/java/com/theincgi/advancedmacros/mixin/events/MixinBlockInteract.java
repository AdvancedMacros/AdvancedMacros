package com.theincgi.advancedmacros.mixin.events;

import static com.theincgi.advancedmacros.event.handlers.OnBlockInteract.onBlockInteract;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.theincgi.advancedmacros.event.events.BlockInteractEvent;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinBlockInteract {
	
	
	@Inject(
		method = "interactBlock",
		at = @At("HEAD")
	)
	private void am_onInteractBlock(ClientPlayerEntity entity, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> ci) {
		onBlockInteract.onEvent(new BlockInteractEvent(entity, hand, hitResult, ci));
	}
	
}
