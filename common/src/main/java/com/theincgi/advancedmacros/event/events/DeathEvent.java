package com.theincgi.advancedmacros.event.events;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.theincgi.advancedmacros.event.Event;
import com.theincgi.advancedmacros.misc.Utils;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;

public class DeathEvent extends Event {
	
	DamageSource damageSource;
	LivingEntity entity;
	
	public DeathEvent(DamageSource damageSource, LivingEntity playerEntity, CallbackInfo ci) {
		super(ci);
		this.damageSource = damageSource;
		this.entity = playerEntity;
	}


	@Override
	public LuaTable createArgsTable() {
		var args = new LuaTable();
		
		args.set("entity", Utils.entityToTable(entity));
		args.set("damageSource", Utils.damageSourceToLuaValue(damageSource));
		
		return args;
	}
	
}
