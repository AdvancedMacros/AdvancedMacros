package com.theincgi.advancedmacros.mixin;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.theincgi.advancedmacros.misc.Settings;
import com.theincgi.advancedmacros.misc.Utils;

import net.minecraft.client.gui.hud.ChatHud;

@Mixin(ChatHud.class)
public class MixinChatHud {

	@ModifyConstant(
		method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V",
		constant = @Constant(intValue = 100, ordinal = 0)
	)
	private int modifyVisibleMessageLimit(int original) {
		return modifyChatLimit(original);
	}
	
	@ModifyConstant(
		method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V",
		constant = @Constant(intValue = 100, ordinal = 1)
	)
	private int modifyStoredMessageLimit(int original) {
		return modifyChatLimit(original);		
	}
	
	private int modifyChatLimit(int original) {
		LuaValue chatSettings;
		
		if((chatSettings = Settings.settings.get("chat")).isnil()) {
            Settings.settings.set("chat", chatSettings = new LuaTable());
        }
		var value = chatSettings.get("maxLines");
		if(value.isnil()) {
			Settings.settings.get("chat").set("maxLines", original);
			return original;
		}
		try{
			return value.checkint();
		} catch (LuaError e) {
			Utils.logError(e);
			Settings.settings.set("maxLines", original);
			return original;
		}
	}
	
}
