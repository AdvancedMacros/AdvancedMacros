package com.theincgi.advancedmacros.event.events;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.theincgi.advancedmacros.event.Event;
import com.theincgi.advancedmacros.misc.Utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class AttackEntityEvent extends Event {
	
	PlayerEntity attacker;
	Entity entity;
	
	
	public AttackEntityEvent(PlayerEntity attacker, Entity entity, CallbackInfo ci) {
		super(ci);
		this.attacker = attacker;
		this.entity = entity;
	}


	@Override
	public LuaTable createArgsTable() {
		var args = new LuaTable();
		args.set("target", Utils.entityToTable(entity));
		args.set("attacker", Utils.entityToTable(attacker));
		return args;
	}

}
