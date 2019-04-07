package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.SystemToast;
import net.minecraft.client.gui.toasts.SystemToast.Type;
import net.minecraft.util.text.ITextComponent;

public class Toast {
	
	public static class ToastNotification extends TwoArgFunction{
		@Override
		public LuaValue call(LuaValue arg1, LuaValue arg2) {
			toast(arg1, arg2);
			return NONE;
		}
	}
	
	public static class ToastActionBar extends VarArgFunction {
		@Override
		public Varargs invoke(Varargs args) {
			toastActionBar(args.checkjstring(1), args.optboolean(2,false));
			return NONE;
		}
	}
	
	public static class ToastTitle extends VarArgFunction {
		@Override
		public Varargs invoke(Varargs args) {
			toastTitle(args.checkjstring(1), args.optjstring(2, null), args.optint(3, -1), args.optint(4, -1), args.optint(5,  -1));
			return NONE;
		}
	}
	
	
	public static void toast(LuaValue arg1, LuaValue arg2) {

		ITextComponent comp1, comp2;
		comp1 = AdvancedMacros.logFunc.formatString(arg1);
		comp2 = AdvancedMacros.logFunc.formatString(arg2);
		AdvancedMacros.getMinecraft().getToastGui().add(
				//new AdvancementToast(Advancement.Builder.)
				//new RecipeToast(ItemStack)
				new SystemToast(Type.TUTORIAL_HINT, comp1, comp2)
				//new SystemToast(Type.NARRATOR_TOGGLE, comp1, comp2)
				
				);
				
		
	}
	
	public static void toastTitle(String text, String subtitle, int ticksUp, int ticksIn, int ticksOut) {
		Minecraft mc = AdvancedMacros.getMinecraft();
		mc.ingameGUI.displayTitle(null, null, ticksIn, ticksUp, ticksOut);
		if(subtitle!=null)
			mc.ingameGUI.displayTitle(null, Utils.toMinecraftColorCodes(subtitle), -1, -1, -1);
		mc.ingameGUI.displayTitle(Utils.toMinecraftColorCodes(text), null, -1, -1, -1);
	}
	public static void toastActionBar(String text, boolean colorize) {
		Minecraft mc = AdvancedMacros.getMinecraft();
		if(colorize) {
			mc.ingameGUI.setOverlayMessage(text, true);
		}else{
			mc.ingameGUI.setOverlayMessage(Utils.toMinecraftColorCodes(text), false);
		}
	}
}
