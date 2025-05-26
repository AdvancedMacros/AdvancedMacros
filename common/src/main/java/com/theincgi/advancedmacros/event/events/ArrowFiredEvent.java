package com.theincgi.advancedmacros.event.events;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.theincgi.advancedmacros.event.Event;
import com.theincgi.advancedmacros.misc.Utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class ArrowFiredEvent extends Event {

	public PlayerEntity player;
	public double progress;
	public int chargeTime;
	public ItemStack bowStack, arrowStack;
	public int remainingUseTicks;
	public boolean isInfinite;

	public ArrowFiredEvent(PlayerEntity player, int charge, ItemStack bowStack, ItemStack arrowStack, int remainingUseTicks, boolean infinite, CallbackInfo ci) {
		super(ci);
		this.player = player;
		this.chargeTime = charge;
		this.bowStack = bowStack;
		this.arrowStack = arrowStack;
		this.remainingUseTicks = remainingUseTicks;
		this.isInfinite = infinite;
	}
	
	@Override
	public LuaTable createArgsTable() {
		LuaTable args = new LuaTable();
		
		args.set(1, Utils.itemStackToLuatable(bowStack));
		args.set(2, chargeTime);
		args.set(3, !arrowStack.isEmpty() || isInfinite);
		args.set(4, Utils.codedFromTextComponent(player.getName(), false).a);
		args.set(5, Utils.itemStackToLuatable(arrowStack));
		
		return args;
	}
	
}
