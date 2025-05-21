package com.theincgi.advancedmacros.lua.functions;

import com.theincgi.advancedmacros.AdvancedMacros;
import com.theincgi.advancedmacros.misc.Permissions;
import com.theincgi.advancedmacros.misc.Utils;
import com.theincgi.advancedmacros.misc.Permissions.Permission;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

public class Toast {

    public static class ToastNotification extends TwoArgFunction {

        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2) {
        	Permissions.check(Permission.TOAST_NOTIFICATION);
            toast(arg1, arg2);
            return NONE;
        }

    }

    public static class ToastActionBar extends VarArgFunction {

        @Override
        public Varargs invoke(Varargs args) {
        	Permissions.check(Permission.TOAST_ACTION_BAR);
            toastActionBar(args.checkjstring(1), args.optboolean(2, false));
            return NONE;
        }

    }

    public static class ToastTitle extends VarArgFunction {

        @Override
        public Varargs invoke(Varargs args) {
        	Permissions.check(Permission.TOAST_TITLE);
            toastTitle(args.checkjstring(1), args.optjstring(2, null), args.optint(3, -1), args.optint(4, -1), args.optint(5, -1));
            return NONE;
        }

    }

    public static void toast(LuaValue arg1, LuaValue arg2) {

        Text comp1, comp2;
        comp1 = AdvancedMacros.logFunc.formatString(arg1);
        comp2 = AdvancedMacros.logFunc.formatString(arg2);
        MinecraftClient.getInstance().getToastManager().add(
                //new AdvancementToast(Advancement.Builder.)
                //new RecipeToast(ItemStack)
                new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION, comp1, comp2)
                //new SystemToast(Type.NARRATOR_TOGGLE, comp1, comp2)

        );

    }

    public static void toastTitle(String text, String subtitle, int ticksUp, int ticksIn, int ticksOut) {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.inGameHud.setTitle(null);
        mc.inGameHud.setTitleTicks(ticksIn, ticksUp, ticksOut);
        if (subtitle != null) {
            mc.inGameHud.setSubtitle(Text.literal(Utils.toMinecraftColorCodes(subtitle)));
        }
        mc.inGameHud.setTitle(Text.literal(Utils.toMinecraftColorCodes(text)));
        mc.inGameHud.setTitleTicks(-1, -1, -1);
    }

    public static void toastActionBar(String text, boolean colorize) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (colorize) {
            mc.inGameHud.setOverlayMessage(Text.literal(text), true);
        } else {
            mc.inGameHud.setOverlayMessage(Text.literal(Utils.toMinecraftColorCodes(text)), false);
        }
    }

}
