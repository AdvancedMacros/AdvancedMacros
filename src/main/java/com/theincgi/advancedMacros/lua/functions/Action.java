package com.theincgi.advancedMacros.lua.functions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.event.TaskDispatcher;
import com.theincgi.advancedMacros.misc.HIDUtils.Keyboard;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings.Input;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class Action {
	Minecraft minecraft = AdvancedMacros.getMinecraft();
	GameSettings sets = minecraft.gameSettings;
	class Forward extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg) {
			holdKeybind(sets.keyBindForward, arg.optlong(0));
			return LuaValue.NONE;
		}
	}
	class Back extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg) {
			holdKeybind(sets.keyBindBack, arg.optlong(0));
			return LuaValue.NONE;
		}
	}
	class Left extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg) {
			holdKeybind(sets.keyBindLeft, arg.optlong(0));
			return LuaValue.NONE;
		}
	}
	class Right extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg) {
			holdKeybind(sets.keyBindRight, arg.optlong(0));
			return LuaValue.NONE;
		}
	}
	//the legit version
	class Jump extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg) {
			//do not document forced jumps unless they are discovered...maybe
			if(minecraft.player.onGround || arg.isstring()&&arg.tojstring().equals("forced")) {
				minecraft.player.jump();
			}
			//holdKeybind(sets.keyBindJump, arg.optlong(0));
			return LuaValue.NONE;
		}
	}
	class Sneak extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg) {
			holdKeybind(sets.keyBindSneak, arg.checklong());
			return LuaValue.NONE;
		}
	}
	
	private BlockPos attackTarget = null;
	private class WaitForBreak extends ZeroArgFunction {
		@Override
		public LuaValue call() {
			while( attackTarget!=null ) {
				Utils.waitTick();
			}
			return NONE;
		}
		
	} WaitForBreak waitForBreak = new WaitForBreak();
	
	private boolean attackTargetIsBlock() {
		synchronized (waitForBreak) {
			IChunk chunk = minecraft.world.getChunk(attackTarget);
			BlockState state = chunk.getBlockState(attackTarget);
			Block block = state.getBlock();
			if(!block.isAir(state, minecraft.world, attackTarget)) return true;
			return false;
		}
	}
	
	public void checkBlockBreakStatus() {
		synchronized (waitForBreak) {
			if(attackTarget == null) return;
			if(attackTargetIsBlock()) return;
			holdKeybind(sets.keyBindAttack, 0);
			attackTarget = null;
		}
	}
	
	class Attack extends OneArgFunction{
		final Method m = ObfuscationReflectionHelper.findMethod(Minecraft.class, "func_147116_af"); //clickMouse()
		@Override
		public LuaValue call(LuaValue arg) {
			if(arg.isstring() && arg.tojstring().equals("break")) {
				RayTraceResult rtr = minecraft.player.pick(PlayerEntity.REACH_DISTANCE.getDefaultValue(), 0, false); //raytrace
				if(rtr==null)
					return waitForBreak;
				BlockPos lookingAt = ((BlockRayTraceResult)rtr).getPos();
				synchronized (waitForBreak) {
					attackTarget = lookingAt;
				}
				holdKeybind(sets.keyBindAttack, -1);
				return waitForBreak;
			}
			if(arg.isnil()){
				Class c = minecraft.getClass();
				m.setAccessible(true);
				TaskDispatcher.addTask(()-> {
						try {
							m.invoke(minecraft);
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						}
				});
			}else{
				holdKeybind(sets.keyBindAttack, arg.checklong());
			}
			return LuaValue.NONE;
		}
	}
	class GetHotbar extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			return LuaValue.valueOf(minecraft.player.inventory.currentItem+1);
		}
	}
	class Use extends OneArgFunction{
		Method m = ObfuscationReflectionHelper.findMethod(Minecraft.class, "func_147121_ag");//"rightClickMouse", 
		@Override
		public LuaValue call(LuaValue arg) {
			if(arg.isnil()){
				Class c = minecraft.getClass();
				//try {

				//Method m = c.getDeclaredMethod("rightClickMouse");
				m.setAccessible(true);
				TaskDispatcher.addTask(()->{
						try {
							m.invoke(minecraft);
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						}
				});
			}else{
				holdKeybind(sets.keyBindUseItem, arg.optlong(0));
			}
			return LuaValue.NONE;
		}
	}
	class Drop extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg) {
			minecraft.player.drop(arg.optboolean(false));
			//tapKeybind(sets.keyBindDrop);
			return LuaValue.NONE;
		}
	}
	class SwapHand extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			if (!minecraft.player.isSpectator()) {
			    minecraft.getConnection().sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.SWAP_HELD_ITEMS, BlockPos.ZERO, Direction.DOWN));
			}
			//tapKeybind(sets.keyBindSwapHands);
			
			return LuaValue.NONE;
		}
	}
	class PickBlock extends ZeroArgFunction{
		Minecraft mc = minecraft;
		@Override
		public LuaValue call() {
			net.minecraftforge.common.ForgeHooks.onPickBlock(mc.objectMouseOver, mc.player, mc.world);
			return LuaValue.NONE;
		}
	}
	class Sprint extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue v) {
			minecraft.player.setSprinting(v.optboolean(true));
			return LuaValue.NONE;
		}
	}
	class SetHotbar extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg) {
			int i = arg.checkint();
			if(i<1 || i>9){throw new LuaError("There is no hotbar slot "+i);}
			minecraft.player.inventory.currentItem = i-1;
			return LuaValue.NONE;
		}
	}
	class LookAt extends VarArgFunction{
		@Override
		public Varargs invoke(Varargs args) {
			if(args.narg() < 3){throw new LuaError("Not enough args [x,y,z]<,time>");}
			long time = 0;

			double x = args.arg(1).checkdouble();
			double y = args.arg(2).checkdouble();
			double z = args.arg(3).checkdouble();
			ClientPlayerEntity player = minecraft.player;
			double dx = x-player.getPosX();
			double dy = y-player.getPosY()-player.getEyeHeight();
			double dz = z-player.getPosZ();
			double toYaw = Math.toDegrees(Math.atan2(dz, dx))-90; //dz might need to be negative cause the whole z is backwards thing
			if(toYaw<=-180){toYaw+=360;}
			double h = Math.sqrt(dx*dx+dz*dz);
			double toPitch = -Math.toDegrees(Math.atan2(dy, h));
			LuaTable vargs = new LuaTable();
			vargs.set(1, LuaValue.valueOf(toYaw));
			vargs.set(2, LuaValue.valueOf(toPitch));
			vargs.set(3, args.arg(4));                       
			return look.invoke(vargs.unpack());
			//BOOKMARK Does 360 when switching from neg to pos
		}

	}
	//	class Screenshot extends ZeroArgFunction{
	//		@Override
	//		public LuaValue call() {
	//			holdKeybind(sets.keyBindScreenshot,25);
	//			return LuaValue.NONE;
	//		}
	//	}
	class Key extends TwoArgFunction{
		@Override
		public LuaValue call(LuaValue arg1, LuaValue arg2) {
			holdKeybind(Keyboard.codeOf(arg1.checkjstring()), arg2.optlong(0));
			return LuaValue.NONE;
		}
	}
	class WaitTick extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			Utils.waitTick();
			return LuaValue.NONE;
		}
	}
	class Look extends VarArgFunction{
		@Override
		public Varargs invoke(Varargs args) {
			//BOOKMARK ForgeEventHandler on tick, release key from list of timers and also look solver MathHelper cos*range+start to fit in time
			if(args.narg()==2 || (args.narg()==3 && args.arg(3).islong())){
				long time = args.arg(3).optlong(0);
				if(time>0){
					AdvancedMacros.forgeEventHandler.lookTo((float)args.arg(1).todouble(), (float)args.arg(2).todouble(), time);
				}else{
					ClientPlayerEntity player = minecraft.player;
					player.rotationPitch = (float) args.arg(2).todouble();
					player.rotationYaw = (float) args.arg(1).todouble();
					System.out.println(player.rotationYaw);
				}
			}else{throw new LuaError("Args: [yaw][,pitch]<,time>");}
			return LuaValue.NONE;
		}
	}

	private Look look;
	private LuaTable controls;
	/**Add functions to given table*/
	public LuaTable getKeybindFuncts(LuaTable controls){
		controls.set("attack", new Attack());
		controls.set("back", new Back());
		controls.set("drop", new Drop());
		controls.set("forward", new Forward());
		controls.set("jump", new Jump());
		controls.set("left", new Left());
		controls.set("pickBlock", new PickBlock());
		controls.set("right", new Right());
		//controls.set("screenshot", new Screenshot());
		controls.set("sprint", new Sprint());
		controls.set("swapHand", new SwapHand());
		controls.set("use", new Use());
		controls.set("key", new Key());
		controls.set("waitTick", new WaitTick());
		controls.set("look", look = new Look());
		controls.set("lookAt", new LookAt());
		controls.set("sneak", new Sneak());
		controls.set("setHotbar", new SetHotbar());
		controls.set("getHotbar", new GetHotbar());
		return controls;
	}
	private void tapKeybind(KeyBinding kb){
		tapKeybind(kb.getKey());
	}
	private void tapKeybind(Input keyCode){
		GameSettings sets = minecraft.gameSettings;
		KeyBinding.setKeyBindState(keyCode, true);
		int t = AdvancedMacros.forgeEventHandler.getSTick()+1;
		while(t>=AdvancedMacros.forgeEventHandler.getSTick()){
			try {
				Thread.sleep(1); //tick should be 20, lil bit faster this way
			} catch (InterruptedException e) {} 
		}
		KeyBinding.setKeyBindState(keyCode, false);
	}

	private void holdKeybind(KeyBinding kb, long time){
		holdKeybind(kb.getKey(), time);
	}
	
	private void holdKeybind(Input input, long time){
		if(time==0){KeyBinding.setKeyBindState(input, false); return;} //changed for insant release
		KeyBinding.setKeyBindState(input, true);
		if(time<0){return;}
		AdvancedMacros.forgeEventHandler.releaseKeybindAt(input, System.currentTimeMillis()+time);
	}
	private void holdKeybind(int keycode, long time){
		Input input =net.minecraft.client.util.InputMappings.getInputByCode(keycode, 0); //TESTME keybinding holds
		holdKeybind(input, time);
	}
}