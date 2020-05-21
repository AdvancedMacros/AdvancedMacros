package com.theincgi.advancedMacros.lua.functions.entity;

import java.util.concurrent.ExecutionException;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;

import com.google.common.util.concurrent.ListenableFuture;
import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.event.TaskDispatcher;
import com.theincgi.advancedMacros.misc.Utils;
import com.theincgi.advancedMacros.misc.Utils.NBTUtils;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;

public class GetNBT {

	public static class GetPlayerNBT extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue arg) {
			AbstractClientPlayerEntity  acpe = null;
			if(arg.isnil()) {
				acpe = AdvancedMacros.getMinecraft().player;
			}
			if(acpe==null)
				acpe = Utils.findPlayerByName(AdvancedMacros.getMinecraft().world, arg.checkjstring());
			if(acpe!=null)
				return getNbt(acpe);

			return FALSE;
		}
	}

	public static class GetEntityNBT extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue arg) {
			Entity  entity = AdvancedMacros.getMinecraft().world.getEntityByID(arg.checkint());

			if(entity!=null)
				return getNbt(entity);

			return FALSE;
		}
	}


	public static LuaValue getNbt(AbstractClientPlayerEntity player) {
		ListenableFuture<LuaValue> f = TaskDispatcher.addTask(()->{
			try{
				return NBTUtils.fromCompound(player.serializeNBT());
			}catch (NullPointerException e) {
				try {
					CompoundNBT ret = player.serializeNBT();//TODO check me //getEntityData();
					return NBTUtils.fromCompound(ret);
				}catch(Exception ex) {
					ex.printStackTrace();
					return LuaValue.FALSE;
				}
			}
		});
		TaskDispatcher.waitFor(f);
		try {
			return f.get();
		} catch (InterruptedException | ExecutionException e) {
			return LuaValue.FALSE;
		}
	}

	public static LuaValue getNbt(Entity e) {
		ListenableFuture<LuaValue> f = TaskDispatcher.addTask(()->{
			try{
				return NBTUtils.fromCompound(e.serializeNBT());
			}catch (NullPointerException npe) {
				try {
					CompoundNBT ret = e.serializeNBT();//TODO CHECK ME// getEntityData();
					return NBTUtils.fromCompound(ret);
				}catch(Exception ex) {
					ex.printStackTrace();
					return LuaValue.FALSE;
				}
			}
		});
		TaskDispatcher.waitFor(f);
		try {
			return f.get();
		} catch (InterruptedException | ExecutionException e1) {
			return LuaValue.FALSE;
		}
	}
}
