package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.AdvancementToast;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.gui.toasts.IToast.Visibility;
import net.minecraft.client.gui.toasts.RecipeToast;
import net.minecraft.client.gui.toasts.SystemToast;
import net.minecraft.client.gui.toasts.SystemToast.Type;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.event.entity.minecart.MinecartCollisionEvent;

public class Toast extends TwoArgFunction{
	
	@Override
	public LuaValue call(LuaValue arg1, LuaValue arg2) {
		toast(arg1, arg2);
		return LuaValue.NONE;
	}
	
	public void toast(LuaValue arg1, LuaValue arg2) {
		//Minecraft.getMinecraft().ingameGUI.setOverlayMessage(message, animateColor);
		//Minecraft.getMinecraft().ingameGUI.setRecordPlayingMessage(recordName);
		//Minecraft.getMinecraft().ingameGUI.displayTitle(title, subTitle, timeFadeIn, displayTime, timeFadeOut);
		ITextComponent comp1, comp2;
		comp1 = AdvancedMacros.logFunc.formatString(arg1);
		comp2 = AdvancedMacros.logFunc.formatString(arg2);
		Minecraft.getMinecraft().getToastGui().add(
				//new AdvancementToast(Advancement.Builder.)
				//new RecipeToast(ItemStack)
				new SystemToast(Type.TUTORIAL_HINT, comp1, comp2)
				//new SystemToast(Type.NARRATOR_TOGGLE, comp1, comp2)
				
				);
				
		
	}
}
