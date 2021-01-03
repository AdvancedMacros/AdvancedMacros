package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaFunction;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.ThreeArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.misc.Utils;
import com.theincgi.advancedMacros.misc.Utils.NBTUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
public class GetPlayer extends OneArgFunction {

	@Override
	public LuaValue call(LuaValue playerName) {
		if(playerName.isnil()) {
			return entityPlayerToTable(AdvancedMacros.getMinecraft().player);
		}else{
			try {
			return entityPlayerToTable(AdvancedMacros.getMinecraft().world.getPlayerEntityByName(playerName.checkjstring()));
			}catch(NullPointerException npe) {
				return LuaValue.FALSE;
			}
		}
	}

	public static LuaValue entityPlayerToTable(EntityPlayer player) {
		if(player == null) return NIL;
		try {
		LuaTable t = new LuaTable() {
			LuaFunction func = new ThreeArgFunction() {
				@Override public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
					player.setVelocity(arg1.checkdouble(), arg2.checkdouble(), arg3.checkdouble());
					return NONE;
				}
			};
			@Override
			public LuaValue rawget(LuaValue key) { //secret function
				if(key.checkjstring().equals("setVelocity") && player instanceof EntityPlayerSP){
			         return func;
				}
				return super.rawget(key);
			}
		};
		t.set("name", player.getName());
		t.set("inventory", Utils.inventoryToTable(player.inventory, !(player instanceof EntityPlayerSP)));
		{
			LuaTable pos = new LuaTable();
			pos.set(1, LuaValue.valueOf(player.posX));
			pos.set(2, LuaValue.valueOf(player.posY));
			pos.set(3, LuaValue.valueOf(player.posZ));
			t.set("pos", pos);
		}
		t.set("mainHand", Utils.itemStackToLuatable(player.getHeldItemMainhand()));
		t.set("offHand", Utils.itemStackToLuatable(player.getHeldItemOffhand()));
		if(player instanceof EntityPlayerSP)
			t.set("invSlot", ((EntityPlayerSP)player).inventory.currentItem+1);
		
		t.set("dimension", LuaValue.valueOf(player.dimension));
		t.set("pitch", MathHelper.wrapDegrees(player.rotationPitch));//player.rotationPitch);
		t.set("yaw", MathHelper.wrapDegrees(player.rotationYaw));//player.rotationYawHead);
		t.set("exp", player.experience);
		t.set("expLevel", player.experienceLevel);
		t.set("expTotal", player.experienceTotal);
		t.set("eyeHeight", player.eyeHeight);
		t.set("fallDist", player.fallDistance);
		t.set("height", player.height);
		t.set("width", player.width);
		t.set("hurtResTime", player.hurtResistantTime);
		//t.set("isAirborne", LuaValue.valueOf(player.isAirBorne));
		t.set("isCollidedHorz", LuaValue.valueOf(player.collidedHorizontally));
		t.set("isCollidedVert", LuaValue.valueOf(player.collidedVertically));
		t.set("swingProgress", LuaValue.valueOf(player.swingProgress));
		t.set("maxHurtResTime", LuaValue.valueOf(player.maxHurtResistantTime));
		t.set("isNoClip", LuaValue.valueOf(player.noClip));
		t.set("onGround", LuaValue.valueOf(player.onGround));
		t.set("isInvulnerable", LuaValue.valueOf(player.getIsInvulnerable()));
		{
			LuaTable pos = new LuaTable();
			BlockPos p = player.getBedLocation();
			if(p!=null) {
				pos.set(1, LuaValue.valueOf(p.getX()));
				pos.set(2, LuaValue.valueOf(p.getY()));
				pos.set(3, LuaValue.valueOf(p.getZ()));
				t.set("bedLocation", pos);
			}
		}
		t.set("team", player.getTeam()==null?FALSE:valueOf(player.getTeam().getName()));
		t.set("luck", player.getLuck());
		t.set("health", MathHelper.ceil(player.getHealth()));
		t.set("hunger", MathHelper.ceil(player.getFoodStats().getFoodLevel()));
		t.set("air", player.getAir());
		t.set("hasNoGravity", LuaValue.valueOf(player.hasNoGravity()));
		{
			LuaTable velocity = new LuaTable();
			Entity e = player.getLowestRidingEntity();
			velocity.set(1, LuaValue.valueOf(e.motionX));
			velocity.set(2, LuaValue.valueOf(e.motionY));
			velocity.set(3, LuaValue.valueOf(e.motionZ));
			t.set("velocity", velocity);
		}
		t.set("isSneaking", LuaValue.valueOf(player.isSneaking()));
		t.set("isOnLadder", LuaValue.valueOf(player.isOnLadder()));
		t.set("isInWater", LuaValue.valueOf(player.isInWater()));
		t.set("isInLava", LuaValue.valueOf(player.isInLava()));
		t.set("immuneToFire", LuaValue.valueOf(player.isImmuneToFire()));
		t.set("isImmuneToExplosion", LuaValue.valueOf(player.isImmuneToExplosions()));
		t.set("isEyltraFlying", LuaValue.valueOf(player.isElytraFlying()));
		t.set("isOnFire", LuaValue.valueOf(player.isBurning()));
		t.set("isSprinting", LuaValue.valueOf(player.isSprinting()));
		{
			LuaTable effects = new LuaTable();
			int i = 1;
			for(PotionEffect pe : player.getActivePotionEffects()) {
				effects.set(i++, Utils.effectToTable(pe));
			}
			t.set("potionEffects", effects);
		}
		t.set("entityRiding", Utils.entityToTable(player.getRidingEntity()));
		t.set("isSleeping", LuaValue.valueOf(player.isPlayerSleeping()));
		t.set("isInvisible", LuaValue.valueOf(player.isInvisible()));
		t.set("uuid", LuaValue.valueOf(player.getUniqueID().toString()));
		{
			RayTraceResult rtr = player.rayTrace(8, 0);
			if(rtr!=null) {
				BlockPos lookingAt = rtr.getBlockPos();
				if(lookingAt!=null) {
					LuaTable look = new LuaTable();
					look.set(1, LuaValue.valueOf(lookingAt.getX()));
					look.set(2, LuaValue.valueOf(lookingAt.getY()));
					look.set(3, LuaValue.valueOf(lookingAt.getZ()));
					t.set("lookingAt", look);
				}
			}
		}
		t.set("entityID", valueOf(player.getEntityId()));
		t.set("gamemode", player.isSpectator()?"spectator":player.isCreative()?"creative":"survival"); //FIXME ... adventure?
		try{
			t.set("nbt", NBTUtils.fromCompound(player.serializeNBT()));
		}catch (NullPointerException e) {
			try {
				NBTTagCompound ret = new NBTTagCompound();
				player.writeToNBT(ret);
				t.set("nbt", NBTUtils.fromCompound(ret));
			
			}catch(Exception ex) {
				ex.printStackTrace();
				t.set("nbt", LuaValue.FALSE);
			}
		}
		if(player.equals(AdvancedMacros.getMinecraft().player)) {
			t.set("target", Utils.rayTraceResultToLuaValue(AdvancedMacros.getMinecraft().objectMouseOver));
		}
		
		return t;
		}catch (Exception e) {
			e.printStackTrace();
			return NIL;
		}
	}
	

	
}