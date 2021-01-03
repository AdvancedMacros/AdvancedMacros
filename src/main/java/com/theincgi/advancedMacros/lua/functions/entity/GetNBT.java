package com.theincgi.advancedMacros.lua.functions.entity;

import java.util.concurrent.ExecutionException;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;

import com.google.common.util.concurrent.ListenableFuture;
import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.misc.Utils;
import com.theincgi.advancedMacros.misc.Utils.NBTUtils;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;

public class GetNBT extends OneArgFunction {

	/*public static class GetPlayerNBT extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue arg) {
			AbstractClientPlayer acpe = null;
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


	public static LuaValue getNbt(AbstractClientPlayer player) {
		return Utils.runOnMCAndWait(()->{
			try{
				return NBTUtils.fromCompound(player.serializeNBT());
			}catch (NullPointerException e) {
				try {
					NBTTagCompound ret = new NBTTagCompound();
					player.writeToNBT(ret);
					return  NBTUtils.fromCompound(ret);
				}catch(Exception ex) {
					ex.printStackTrace();
					return LuaValue.FALSE;
				}
			}
		});
	}

	public static LuaValue getNbt(Entity e) {
		return Utils.runOnMCAndWait(()->{
			try{
				return NBTUtils.fromCompound(e.serializeNBT());
			}catch (NullPointerException npe) {
				try {
					NBTTagCompound ret = new NBTTagCompound();
					e.writeToNBT(ret);
					return  NBTUtils.fromCompound(ret);
				}catch(Exception ex) {
					ex.printStackTrace();
					return LuaValue.FALSE;
				}
			}
		});
	}*/

	@Override
	public LuaValue call(LuaValue arg) {
		try {
			Entity entity = null;
			if (arg.isnil()) {
				entity = AdvancedMacros.getMinecraft().player;
			} else if (arg.isint()) {
				entity = AdvancedMacros.getMinecraft().world.getEntityByID(arg.checkint());
			} else if (arg.isstring()) {
				entity = Utils.findPlayerByName(AdvancedMacros.getMinecraft().world, arg.checkjstring());
			}

			if (entity == null)
				return LuaValue.FALSE;
			NBTTagCompound tag = new NBTTagCompound();
			LuaTable ret;

			entity.writeToNBT(tag);
			ret = NBTUtils.fromCompound(tag);
			ret.set("id", entity.getEntityId());
			ret.set("name", entity.getName());
			ret.set("class", entity.getClass().getName());
			return ret;

		} catch (Exception ex) {
			ex.printStackTrace();
			return LuaValue.FALSE;
		}
	}
}
