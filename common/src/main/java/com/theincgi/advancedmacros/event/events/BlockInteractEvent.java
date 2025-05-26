package com.theincgi.advancedmacros.event.events;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.theincgi.advancedmacros.event.Event;
import com.theincgi.advancedmacros.misc.Utils;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

public class BlockInteractEvent extends Event {

	ClientPlayerEntity entity;
	Hand hand;
	BlockHitResult hitResult;
	
	public BlockInteractEvent(ClientPlayerEntity entity, Hand hand, BlockHitResult hitResult, CallbackInfo ci) {
		super(ci);
		this.entity = entity;
		this.hand = hand;
		this.hitResult = hitResult;
	}



	@Override
	public LuaTable createArgsTable() {
		var args = new LuaTable();
		
		args.set("hitResult", Utils.blockHitResultToLuaValue(hitResult));
		args.set("heldItem",  Utils.itemStackToLuatable(hand.equals(Hand.MAIN_HAND) ? entity.getMainHandStack() : entity.getOffHandStack()));
		args.set("hand", hand.equals(Hand.MAIN_HAND)?"main hand":"off hand");
		args.set("player", Utils.codedFromTextComponent(entity.getName(), false).a);
		
		return args;
	}
	
}
