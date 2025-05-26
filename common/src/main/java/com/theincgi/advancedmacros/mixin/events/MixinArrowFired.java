package com.theincgi.advancedmacros.mixin.events;

import static com.theincgi.advancedmacros.event.handlers.OnArrowFired.onArrowFired;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.theincgi.advancedmacros.event.events.ArrowFiredEvent;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(BowItem.class)
abstract public class MixinArrowFired {

	@Inject(
		method = "onStoppedUsing",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/world/World;isClient:Z",
			opcode = Opcodes.GETFIELD
		),
		locals = LocalCapture.CAPTURE_FAILSOFT
	)	
	private void am_onArrowFired(
			ItemStack stack, World world, LivingEntity user, int remainingUseTicks, 
			CallbackInfo ci, 
			PlayerEntity playerEntity, boolean bl, ItemStack itemStack, int i, float f, boolean bl2
		) {
		boolean infiniteArrows = bl;
		int charge = this.getMaxUseTime(stack) - remainingUseTicks;
		
		onArrowFired.onEvent(new ArrowFiredEvent((PlayerEntity)user, charge, stack, itemStack, remainingUseTicks, infiniteArrows, ci));
	}
		
	 @Shadow
	 public abstract int getMaxUseTime(ItemStack stack);
}
