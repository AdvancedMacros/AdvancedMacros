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
import org.lwjgl.input.Keyboard;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.event.ForgeEventHandler;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

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
			if(AdvancedMacros.getMinecraft().player.onGround || arg.isstring()&&arg.tojstring().equals("forced")) {
				AdvancedMacros.getMinecraft().player.jump();
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
	class Attack extends OneArgFunction{
		final Method m = ReflectionHelper.findMethod(Minecraft.class, "clickMouse", "func_147116_af", new Class[] {});
		@Override
		public LuaValue call(LuaValue arg) {
			if(arg.isnil() || (arg.islong()&&arg.checklong()==0)){
				Class c = minecraft.getClass();
				m.setAccessible(true);
				Utils.runOnMCAndWait(()-> {
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
			return LuaValue.valueOf(AdvancedMacros.getMinecraft().player.inventory.currentItem+1);
		}
	}
	class Use extends OneArgFunction{
		Method m = ReflectionHelper.findMethod(Minecraft.class, "rightClickMouse", "func_147121_ag", new Class[] {});
		@Override
		public LuaValue call(LuaValue arg) {
			if(arg.isnil() || (arg.islong() && arg.checklong()==0)){
				Class c = minecraft.getClass();
				//try {

				//Method m = c.getDeclaredMethod("rightClickMouse");
				m.setAccessible(true);
				Utils.runOnMCAndWait(()->{
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
			AdvancedMacros.getMinecraft().player.dropItem(arg.optboolean(false));
			//tapKeybind(sets.keyBindDrop);
			return LuaValue.NONE;
		}
	}
	class SwapHand extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			AdvancedMacros.getMinecraft().getConnection().sendPacket(
					new CPacketPlayerDigging(
							net.minecraft.network.play.client.CPacketPlayerDigging.Action.SWAP_HELD_ITEMS, 
							BlockPos.ORIGIN, 
							EnumFacing.DOWN));
			//tapKeybind(sets.keyBindSwapHands);
			
			return LuaValue.NONE;
		}
	}
	class PickBlock extends ZeroArgFunction{
		Minecraft mc = AdvancedMacros.getMinecraft();
		@Override
		public LuaValue call() {
			net.minecraftforge.common.ForgeHooks.onPickBlock(mc.objectMouseOver, mc.player, mc.world);
			return LuaValue.NONE;
		}
	}
	class Sprint extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue v) {
			AdvancedMacros.getMinecraft().player.setSprinting(v.optboolean(true));
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
			EntityPlayerSP player = minecraft.player;
			double dx = x-player.posX;
			double dy = y-player.posY-player.getEyeHeight();
			double dz = z-player.posZ;
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
			holdKeybind(Keyboard.getKeyIndex(arg1.checkjstring()), arg2.optlong(0));
			return LuaValue.NONE;
		}
	}
	class WaitTick extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			if(Thread.currentThread()!=AdvancedMacros.getMinecraftThread())
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
					EntityPlayerSP player = AdvancedMacros.getMinecraft().player;
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
		tapKeybind(kb.getKeyCode());
	}
	private void tapKeybind(int keyCode){
		Minecraft minecraft = AdvancedMacros.getMinecraft();
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
		holdKeybind(kb.getKeyCode(), time);
	}
	private void holdKeybind(int keycode, long time){
		if(time==0){KeyBinding.setKeyBindState(keycode, false); return;} //changed for insant release
		KeyBinding.setKeyBindState(keycode, true);
		if(time<0){return;}
		AdvancedMacros.forgeEventHandler.releaseKeybindAt(keycode, System.currentTimeMillis()+time);

	}
}