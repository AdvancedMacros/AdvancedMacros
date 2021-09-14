package com.theincgi.advancedMacros.lua.functions;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaFunction;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.ThreeArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import com.google.common.util.concurrent.ListenableFuture;
import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.event.TaskDispatcher;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.block.Blocks;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.dimension.DimensionType;
@SuppressWarnings("resource")
public class GetPlayer extends OneArgFunction {
	
	public static final LuaTable playerFunctions = new LuaTable();
	static {
		playerFunctions.set("getInventory", new PlayerValueFunction("getInventory", player->{
			return Utils.inventoryToTable(player.inventory, !(player instanceof ClientPlayerEntity));
		}).threadSensitive());
		
		playerFunctions.set("getMainHand",  	 new PlayerValueFunction("getMainHand",       player-> {return Utils.itemStackToLuatable(player.getHeldItemMainhand());}));
		playerFunctions.set("getOffHand",   	 new PlayerValueFunction("getOffHand",        player-> {return Utils.itemStackToLuatable(player.getHeldItemOffhand());}));
		playerFunctions.set("getDimension", 	 new PlayerValueFunction("getDimension" ,     player-> {return Utils.toTable(player.dimension);}));
		playerFunctions.set("getPitch",     	 new PlayerValueFunction("getPitch",     	  player-> {return valueOf(MathHelper.wrapDegrees(player.rotationPitch));}));
		playerFunctions.set("getYaw",      		 new PlayerValueFunction("getYaw",      	  player-> {return valueOf(MathHelper.wrapDegrees(player.rotationYaw));}));
		playerFunctions.set("getExp",       	 new PlayerValueFunction("getExp",       	  player-> {return valueOf(player.experience);}));
		playerFunctions.set("getExpLevel",  	 new PlayerValueFunction("getExpLevel",  	  player-> {return valueOf(player.experienceLevel);}));
		playerFunctions.set("getExpTotal",  	 new PlayerValueFunction("getExpTotal",  	  player-> {return valueOf(player.experienceTotal);}));
		playerFunctions.set("getEyeHeight",    	 new PlayerValueFunction("getEyeHeight",      player-> {return valueOf(player.getEyeHeight());}));
		playerFunctions.set("getFallDist",     	 new PlayerValueFunction("getFallDist",       player-> {return valueOf(player.fallDistance);}));
		playerFunctions.set("getHeight", 		 new PlayerValueFunction("getHeight", 		  player-> {return valueOf(player.getHeight());}));
		playerFunctions.set("getWidth", 		 new PlayerValueFunction("getWidth", 		  player-> {return valueOf(player.getWidth());}));
		playerFunctions.set("getHurtResTime", 	 new PlayerValueFunction("getHurtResTime", 	  player-> {return valueOf(player.hurtResistantTime);}));
		playerFunctions.set("isAirborne", 		 new PlayerValueFunction("isAirborne", 		  player-> {return valueOf(player.isAirBorne);}));
		playerFunctions.set("isCollidedHorz", 	 new PlayerValueFunction("isCollidedHorz", 	  player-> {return valueOf(player.collidedHorizontally);}));
		playerFunctions.set("isCollidedVert", 	 new PlayerValueFunction("isCollidedVert", 	  player-> {return valueOf(player.collidedVertically);}));
		playerFunctions.set("getSwingProgress",  new PlayerValueFunction("getSwingProgress",  player-> {return valueOf(player.swingProgress);}));
		playerFunctions.set("isSwingInProgress", new PlayerValueFunction("isSwingInProgress", player-> {return valueOf(player.isSwingInProgress);}));
		playerFunctions.set("getMaxHurtResTime", new PlayerValueFunction("getMaxHurtResTime", player-> {return valueOf(player.maxHurtResistantTime);}));
		playerFunctions.set("isNoClip", 		 new PlayerValueFunction("isNoClip", 		  player-> {return valueOf(player.noClip);}));
		playerFunctions.set("isOnGround", 		 new PlayerValueFunction("isOnGround", 		  player-> {return valueOf(player.onGround);}));
		playerFunctions.set("isInvulnerable", 	 new PlayerValueFunction("isInvulnerable", 	  player-> {return valueOf(player.isInvulnerable());}));
		playerFunctions.set("getBedLocation", 	 new PlayerValueFunction("getBedLocation", 	  player-> {
			LuaTable pos = new LuaTable();
			BlockPos p = player.getBedLocation(DimensionType.OVERWORLD);
			if(p!=null) {
				pos.set(1, LuaValue.valueOf(p.getX()));
				pos.set(2, LuaValue.valueOf(p.getY()));
				pos.set(3, LuaValue.valueOf(p.getZ()));
				return pos.unpack();
			}
			return FALSE;
		}));
		playerFunctions.set("getTeam", 			new PlayerValueFunction("getTeam", 			player-> {return player.getTeam()==null?FALSE:valueOf(player.getTeam().getName());}));
		playerFunctions.set("getLuck", 			new PlayerValueFunction("getLuck", 			player-> {return valueOf(player.getLuck());}));
		playerFunctions.set("getHealth", 		new PlayerValueFunction("getHealth", 		player-> {return valueOf(player.getHealth());}));
		playerFunctions.set("getHunger", 		new PlayerValueFunction("getHunger", 		player-> {return valueOf(MathHelper.ceil(player.getFoodStats().getFoodLevel()));}));
		playerFunctions.set("getHungerExact", 	new PlayerValueFunction("getHungerExact", 	player-> {return valueOf(player.getFoodStats().getFoodLevel());}));
		playerFunctions.set("getAir", 			new PlayerValueFunction("getAir", 			player-> {return valueOf(player.getAir());}));
		playerFunctions.set("hasNoGravity", 	new PlayerValueFunction("hasNoGravity", 	player-> {return valueOf(player.hasNoGravity());}));
		playerFunctions.set("getVelocity", 		new PlayerValueFunction("getVelocity", 		player-> {
			Entity e = player.getLowestRidingEntity();
			return Utils.toTable(e.getMotion()).unpack();
		}));
		playerFunctions.set("isSneaking", 			new PlayerValueFunction("isSneaking", 			player-> {return valueOf(player.isSneaking());}));
		playerFunctions.set("isOnLadder", 			new PlayerValueFunction("isOnLadder", 			player-> {return valueOf(player.isOnLadder());}));
		playerFunctions.set("isInWater", 			new PlayerValueFunction("isInWater", 			player-> {return valueOf(player.isInWater());}));
		playerFunctions.set("isInLava", 			new PlayerValueFunction("isInLava", 			player-> {return valueOf(player.isInLava());}));
		playerFunctions.set("isImmuneToFire", 		new PlayerValueFunction("isImmuneToFire", 		player-> {return valueOf(player.isImmuneToFire());}));
		playerFunctions.set("isImmuneToExplosion", 	new PlayerValueFunction("isImmuneToExplosion", 	player-> {return valueOf(player.isImmuneToExplosions());}));
		playerFunctions.set("isEyltraFlying", 		new PlayerValueFunction("isEyltraFlying", 		player-> {return valueOf(player.isElytraFlying());}));
		playerFunctions.set("isOnFire", 			new PlayerValueFunction("isOnFire", 			player-> {return valueOf(player.isBurning());}));
		playerFunctions.set("isSprinting", 			new PlayerValueFunction("isSprinting", 			player-> {return valueOf(player.isSprinting());}));
		playerFunctions.set("getPotionEffects", 	new PlayerValueFunction("getPotionEffects", 	player-> {
			LuaTable effects = new LuaTable();
			int i = 1;
			for(Object pe : player.getActivePotionEffects().toArray()) {
				if(pe instanceof EffectInstance)
					effects.set(i++, Utils.effectToTable((EffectInstance) pe));
			}
			return effects;
		}).threadSensitive());
		playerFunctions.set("getRidingEntity", 	new PlayerValueFunction("getRidingEntity", 	player-> {return Utils.entityToTable(player.getRidingEntity());}));
		playerFunctions.set("isSleeping", 		new PlayerValueFunction("isSleeping", 		player-> {return valueOf(player.isSleeping());}));
		playerFunctions.set("isInvisible", 		new PlayerValueFunction("isInvisible", 		player-> {return valueOf(player.isInvisible());}));
		playerFunctions.set("getUUID", 			new PlayerValueFunction("getUUID", 			player-> {return valueOf(player.getUniqueID().toString());}));
		playerFunctions.set("lookingAt", 		new PlayerValueFunction("lookingAt", 		player-> {
			RayTraceResult rtr = player.pick(8, 0, false); //CHECKME
			if(rtr!=null) {
				BlockPos lookingAt = ((BlockRayTraceResult)rtr).getPos();
				if(lookingAt!=null) {
					LuaTable look = new LuaTable();
					look.set(1, LuaValue.valueOf(lookingAt.getX()));
					look.set(2, LuaValue.valueOf(lookingAt.getY()));
					look.set(3, LuaValue.valueOf(lookingAt.getZ()));
					return look.unpack();
				}
			}
			return FALSE;
		}));
		playerFunctions.set("getEntityID", new PlayerValueFunction("getEntityID",player-> {return valueOf(player.getEntityId());}));
		playerFunctions.set("getGamemode", new PlayerValueFunction("getGamemode",player-> {return valueOf(player.isSpectator()?"spectator":player.isCreative()?"creative":"survival");})); //TODO adventure?
		playerFunctions.set("getTarget",   new PlayerValueFunction("getTarget",  player-> {
			if(player.equals(AdvancedMacros.getMinecraft().player)) {
				return Utils.rayTraceResultToLuaValue(AdvancedMacros.getMinecraft().objectMouseOver);
			}
			return FALSE;
		}));

	
		
		playerFunctions.set("isBlocking",    		new PlayerValueFunction("isBlocking",    	player-> {return valueOf(player.isActiveItemStackBlocking());}));
		playerFunctions.set("isActualySwimming",    new PlayerValueFunction("isActualySwimming",player-> {return valueOf(player.isActualySwimming());}));
		playerFunctions.set("getPose",    			new PlayerValueFunction("getPose",    		player-> {return valueOf(player.getPose().name());}));
		playerFunctions.set("isGlowing",    		new PlayerValueFunction("isGlowing",    	player-> {return valueOf(player.isGlowing());}));
		playerFunctions.set("isInBubbleColumn",    	new PlayerValueFunction("isInBubbleColumn", player-> {return valueOf(player.world.getBlockState(new BlockPos(player)).getBlock() == Blocks.BUBBLE_COLUMN);}));
		playerFunctions.set("isPassenger",    		new PlayerValueFunction("isPassenger",    	player-> {return valueOf(player.isPassenger());}));
		playerFunctions.set("isFullyAsleep",    	new PlayerValueFunction("isFullyAsleep",    player-> {return valueOf(player.isPlayerFullyAsleep());}));
		playerFunctions.set("canBePushedByWater",   new PlayerValueFunction("canBePushedByWater",  player-> {return valueOf(player.isPushedByWater());}));
		playerFunctions.set("isSpinAttacking",    	new PlayerValueFunction("isSpinAttacking",  player-> {return valueOf(player.isSpinAttacking());}));
		playerFunctions.set("isWet",    			new PlayerValueFunction("isWet",    		player-> {return valueOf(player.isWet());}));
//		if(player instanceof ClientPlayerEntity) getHotbar
//			t.set("invSlot", ((ClientPlayerEntity)player).inventory.currentItem+1);
	}
	
	@Override
	public LuaValue call(LuaValue playerName) {
		@SuppressWarnings("resource") //getMinecraft().close()
		ListenableFuture<LuaValue> future = TaskDispatcher.addTask(()->{
			if(playerName.isnil()) {
				return entityPlayerToTable(AdvancedMacros.getMinecraft().player);
			}else{
				try {
					String toFind = playerName.checkjstring();
					AbstractClientPlayerEntity acpe = Utils.findPlayerByName(AdvancedMacros.getMinecraft().world, toFind);
					if(acpe!=null)
						return entityPlayerToTable(acpe);
					return FALSE;
				}catch(NullPointerException npe) {
					return LuaValue.FALSE;
				}
			}
		});
		try {
			return future.get();
		}catch(Exception ex) {
			Utils.logError(ex);
			return FALSE;
		}
	}

	public static LuaValue entityPlayerToTable(PlayerEntity player) {
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
				if(key.checkjstring().equals("setVelocity") && player instanceof ClientPlayerEntity){
			         return func;
				}
				return super.rawget(key);
			}
		};
		t.set("name", player.getName().getUnformattedComponentText());
		t.set("inventory", Utils.inventoryToTable(player.inventory, !(player instanceof ClientPlayerEntity)));
		{
			LuaTable pos = new LuaTable();
			pos.set(1, LuaValue.valueOf(player.getPosX()));
			pos.set(2, LuaValue.valueOf(player.getPosY()));
			pos.set(3, LuaValue.valueOf(player.getPosZ()));
			t.set("pos", pos);
		}
		t.set("mainHand", Utils.itemStackToLuatable(player.getHeldItemMainhand()));
		t.set("offHand", Utils.itemStackToLuatable(player.getHeldItemOffhand()));
		if(player instanceof ClientPlayerEntity)
			t.set("invSlot", ((ClientPlayerEntity)player).inventory.currentItem+1);
		
		t.set("dimension", Utils.toTable(player.dimension));
		t.set("pitch", MathHelper.wrapDegrees(player.rotationPitch));//player.rotationPitch);
		t.set("yaw", MathHelper.wrapDegrees(player.rotationYaw));//player.rotationYawHead);
		t.set("exp", player.experience);
		t.set("expLevel", player.experienceLevel);
		t.set("expTotal", player.experienceTotal);
		t.set("eyeHeight", player.getEyeHeight());
		t.set("fallDist", player.fallDistance);
		t.set("height", player.getHeight());
		t.set("width", player.getWidth());
		t.set("hurtResTime", player.hurtResistantTime);
		//t.set("isAirborne", LuaValue.valueOf(player.isAirBorne));
		t.set("isCollidedHorz", LuaValue.valueOf(player.collidedHorizontally));
		t.set("isCollidedVert", LuaValue.valueOf(player.collidedVertically));
		t.set("swingProgress", LuaValue.valueOf(player.swingProgress));
		t.set("maxHurtResTime", LuaValue.valueOf(player.maxHurtResistantTime));
		t.set("isNoClip", LuaValue.valueOf(player.noClip));
		t.set("onGround", LuaValue.valueOf(player.onGround));
		t.set("isInvulnerable", LuaValue.valueOf(player.isInvulnerable()));
		{
			LuaTable pos = new LuaTable();
			BlockPos p = player.getBedLocation(DimensionType.OVERWORLD);
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
			Entity e = player.getLowestRidingEntity();
			LuaTable velocity = Utils.toTable(e.getMotion());

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
			for(Object pe : player.getActivePotionEffects().toArray()) {
				if(pe instanceof EffectInstance)
					effects.set(i++, Utils.effectToTable((EffectInstance) pe));
			}
			t.set("potionEffects", effects);
		}
		t.set("entityRiding", Utils.entityToTable(player.getRidingEntity()));
		t.set("isSleeping", LuaValue.valueOf(player.isSleeping()));
		t.set("isInvisible", LuaValue.valueOf(player.isInvisible()));
		t.set("uuid", LuaValue.valueOf(player.getUniqueID().toString()));
		{
			RayTraceResult rtr = player.pick(8, 0, false); //CHECKME
			if(rtr!=null) {
				BlockPos lookingAt = ((BlockRayTraceResult)rtr).getPos();
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
		
		if(player.equals(AdvancedMacros.getMinecraft().player)) {
			t.set("target", Utils.rayTraceResultToLuaValue(AdvancedMacros.getMinecraft().objectMouseOver));
		}
		
		return t;
		}catch (Exception e) {
			e.printStackTrace();
			return NIL;
		}
	}
	
	public static Optional<PlayerEntity> getPlayerFromLuaValue(LuaValue value) {
		if(value.isnil()) {
			return Optional.ofNullable(AdvancedMacros.getMinecraft().player);
		}else{
			try {
				String toFind = value.checkjstring();
				AbstractClientPlayerEntity acpe = Utils.findPlayerByName(AdvancedMacros.getMinecraft().world, toFind);
				if(acpe!=null)
					return Optional.ofNullable(acpe);
				return Optional.empty();
			}catch(NullPointerException npe) {
				return Optional.empty();
			}
		}
	}
	
	private static class PlayerValueFunction extends VarArgFunction {
		private boolean threadSensitive = false;
		private final Function<PlayerEntity, Varargs> get;
		private final String fName; 
		
		public PlayerValueFunction(String fName, Function<PlayerEntity, Varargs> get) {
			this.fName = fName;
			this.get = get;
		}
		
		public PlayerValueFunction threadSensitive() {
			threadSensitive = true;
			return this;
		}
		
		@Override
		public Varargs invoke(Varargs args) {
			final Optional<PlayerEntity> player = getPlayerFromLuaValue(args.arg1());
			if(!player.isPresent())
				return NIL;
			if(threadSensitive) {
				ListenableFuture<Varargs> x = TaskDispatcher.addTask(()->{
					return get.apply(player.get());
				});
				try {
					return x.get();
				} catch (InterruptedException | ExecutionException e) {
					throw new LuaError(e);
				}
			}else {
				return get.apply(player.get());
			}
		}
		@Override
		public LuaValue tostring() {
			return valueOf(toString());
		}
		@Override
		public String toString() {
			return "function "+fName+"([String: playerName])";
		}
	}
	
}