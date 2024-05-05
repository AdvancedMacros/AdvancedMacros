package com.theincgi.advancedmacros.lua.functions;

import com.theincgi.advancedmacros.AdvancedMacros;
import com.theincgi.advancedmacros.access.IMinecraftClient;
import com.theincgi.advancedmacros.event.TaskDispatcher;
import com.theincgi.advancedmacros.misc.HIDUtils.Keyboard;
import com.theincgi.advancedmacros.misc.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.Chunk;
import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

public class Action {

    MinecraftClient minecraft = MinecraftClient.getInstance();
    GameOptions sets = minecraft.options;

    class Forward extends OneArgFunction {

        @Override
        public LuaValue call(LuaValue arg) {
            holdKeybind(sets.forwardKey, arg.optlong(0));
            return LuaValue.NONE;
        }

    }

    class Back extends OneArgFunction {

        @Override
        public LuaValue call(LuaValue arg) {
            holdKeybind(sets.backKey, arg.optlong(0));
            return LuaValue.NONE;
        }

    }

    class Left extends OneArgFunction {

        @Override
        public LuaValue call(LuaValue arg) {
            holdKeybind(sets.leftKey, arg.optlong(0));
            return LuaValue.NONE;
        }

    }

    class Right extends OneArgFunction {

        @Override
        public LuaValue call(LuaValue arg) {
            holdKeybind(sets.rightKey, arg.optlong(0));
            return LuaValue.NONE;
        }

    }

    //the legit version
    class Jump extends OneArgFunction {

        @Override
        public LuaValue call(LuaValue arg) {
            //do not document forced jumps unless they are discovered...maybe
            if (minecraft.player.isOnGround() || arg.isstring() && arg.tojstring().equals("forced")) {
                minecraft.player.jump();
            }
            //holdKeybind(sets.keyBindJump, arg.optlong(0));
            return LuaValue.NONE;
        }

    }

    class Sneak extends OneArgFunction {

        @Override
        public LuaValue call(LuaValue arg) {
            holdKeybind(sets.sneakKey, arg.checklong());
            return LuaValue.NONE;
        }

    }

    private BlockPos attackTarget = null;

    private class WaitForBreak extends ZeroArgFunction {

        @Override
        public LuaValue call() {
            while (attackTarget != null) {
                Utils.waitTick();
            }
            return NONE;
        }

    }

    WaitForBreak waitForBreak = new WaitForBreak();

    private boolean attackTargetIsBlock() {
        synchronized (waitForBreak) {
            Chunk chunk = minecraft.world.getChunk(attackTarget);
            BlockState state = chunk.getBlockState(attackTarget);
            return !state.isAir();
        }
    }

    public void checkBlockBreakStatus() {
        synchronized (waitForBreak) {
            if (attackTarget == null) {
                return;
            }
            if (attackTargetIsBlock()) {
                return;
            }
            holdKeybind(sets.attackKey, 0);
            attackTarget = null;
        }
    }

    class Attack extends OneArgFunction {

        @Override
        public LuaValue call(LuaValue arg) {
            if (arg.isstring() && arg.tojstring().equals("break")) {
                HitResult rtr = minecraft.player.raycast(minecraft.interactionManager.getReachDistance(), 0, false); //raytrace
                if (rtr == null) {
                    return waitForBreak;
                }
                BlockPos lookingAt = ((BlockHitResult) rtr).getBlockPos();
                synchronized (waitForBreak) {
                    attackTarget = lookingAt;
                }
                holdKeybind(sets.attackKey, -1);
                return waitForBreak;
            }
            if (arg.isnil()) {
                TaskDispatcher.addTask(() -> {
                    ((IMinecraftClient) minecraft).am_doAttack();
                });
            } else {

                holdKeybind(sets.attackKey, arg.checklong());
            }
            return LuaValue.NONE;
        }

    }

    class GetHotbar extends ZeroArgFunction {

        @Override
        public LuaValue call() {
            return LuaValue.valueOf(minecraft.player.getInventory().selectedSlot + 1);
        }

    }

    class Use extends OneArgFunction {

        @Override
        public LuaValue call(LuaValue arg) {
            if (arg.isnil()) {
                TaskDispatcher.addTask(() -> {
                    ((IMinecraftClient) minecraft).am_doItemUse();
                });
            } else {
                holdKeybind(sets.useKey, arg.optlong(0));
            }
            return LuaValue.NONE;
        }

    }

    class Drop extends OneArgFunction {

        @Override
        public LuaValue call(LuaValue arg) {
            minecraft.player.dropSelectedItem(arg.optboolean(false));
            //tapKeybind(sets.keyBindDrop);
            return LuaValue.NONE;
        }

    }

    class SwapHand extends ZeroArgFunction {

        @Override
        public LuaValue call() {
            if (!minecraft.player.isSpectator()) {
                minecraft.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, new BlockPos(0, 0, 0), Direction.DOWN));
            }
            //tapKeybind(sets.keyBindSwapHands);

            return LuaValue.NONE;
        }

    }

    class PickBlock extends ZeroArgFunction {

        @Override
        public LuaValue call() {
            ((IMinecraftClient) minecraft).am_doItemPick();
            return LuaValue.NONE;
        }

    }

    class Sprint extends OneArgFunction {

        @Override
        public LuaValue call(LuaValue v) {
            minecraft.player.setSprinting(v.optboolean(true));
            return LuaValue.NONE;
        }

    }

    class SetHotbar extends OneArgFunction {

        @Override
        public LuaValue call(LuaValue arg) {
            int i = arg.checkint();
            if (i < 1 || i > 9) {
                throw new LuaError("There is no hotbar slot " + i);
            }
            minecraft.player.getInventory().selectedSlot = i - 1;
            return LuaValue.NONE;
        }

    }

    class LookAt extends VarArgFunction {

        @Override
        public Varargs invoke(Varargs args) {
            if (args.narg() < 3) {
                throw new LuaError("Not enough args [x,y,z]<,time>");
            }
            long time = 0;

            double x = args.arg(1).checkdouble();
            double y = args.arg(2).checkdouble();
            double z = args.arg(3).checkdouble();
            ClientPlayerEntity player = minecraft.player;
            double dx = x - player.getX();
            double dy = y - player.getY() - player.getEyeHeight(player.getPose());
            double dz = z - player.getZ();
            double toYaw = Math.toDegrees(Math.atan2(dz, dx)) - 90; //dz might need to be negative cause the whole z is backwards thing
            if (toYaw <= -180) {
                toYaw += 360;
            }
            double h = Math.sqrt(dx * dx + dz * dz);
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
    class Key extends TwoArgFunction {

        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2) {
            holdKeybind(Keyboard.codeOf(arg1.checkjstring()), arg2.optlong(0));
            return LuaValue.NONE;
        }

    }

    class WaitTick extends ZeroArgFunction {

        @Override
        public LuaValue call() {
            Utils.waitTick();
            return LuaValue.NONE;
        }

    }

    class Look extends VarArgFunction {

        @Override
        public Varargs invoke(Varargs args) {
            //BOOKMARK ForgeEventHandler on tick, release key from list of timers and also look solver MathHelper cos*range+start to fit in time
            if (args.narg() == 2 || (args.narg() == 3 && args.arg(3).islong())) {
                long time = args.arg(3).optlong(0);
                if (time > 0) {
                    AdvancedMacros.EVENT_HANDLER.lookTo((float) args.arg(1).todouble(), (float) args.arg(2).todouble(), time);
                } else {
                    ClientPlayerEntity player = minecraft.player;
                    player.setPitch((float) args.arg(2).todouble());
                    player.setYaw((float) args.arg(1).todouble());
                }
            } else {
                throw new LuaError("Args: [yaw][,pitch]<,time>");
            }
            return LuaValue.NONE;
        }

    }

    private Look look;
    private LuaTable controls;

    /**
     * Add functions to given table
     */
    public LuaTable getKeybindFuncts(LuaTable controls) {
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

    private void tapKeybind(KeyBinding kb) {
        tapKeybind(kb.getDefaultKey());
    }

    private void tapKeybind(InputUtil.Key keyCode) {
        GameOptions sets = minecraft.options;
        KeyBinding.setKeyPressed(keyCode, true);
        int t = AdvancedMacros.EVENT_HANDLER.getSTick() + 1;
        while (t >= AdvancedMacros.EVENT_HANDLER.getSTick()) {
            try {
                Thread.sleep(1); //tick should be 20, lil bit faster this way
            } catch (InterruptedException e) {
            }
        }
        KeyBinding.setKeyPressed(keyCode, false);
    }

    private void holdKeybind(KeyBinding kb, long time) {
        holdKeybind(kb.getDefaultKey(), time);
    }

    private void holdKeybind(InputUtil.Key input, long time) {
        if (time == 0) {
            KeyBinding.setKeyPressed(input, false);
            return;
        } //changed for insant release
        KeyBinding.setKeyPressed(input, true);
        if (time < 0) {
            return;
        }
        AdvancedMacros.EVENT_HANDLER.releaseKeybindAt(input, System.currentTimeMillis() + time);
    }

    private void holdKeybind(int keycode, long time) {
        InputUtil.Key input = InputUtil.fromKeyCode(keycode, 0); //TESTME keybinding holds
        holdKeybind(input, time);
    }

}
