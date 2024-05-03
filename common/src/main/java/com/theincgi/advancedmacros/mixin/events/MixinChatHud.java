package com.theincgi.advancedmacros.mixin.events;

import com.theincgi.advancedmacros.AdvancedMacros;
import com.theincgi.advancedmacros.event.EventHandler;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class MixinChatHud {


    @Inject(at = @At("HEAD"), method = "addMessage(Lnet/minecraft/text/Text;)V", cancellable = true)
    public void am_onOnChatMessage(Text message, CallbackInfo ci) {
        if (AdvancedMacros.EVENT_HANDLER.eventExists(EventHandler.EventName.ChatFilter)) {
            AdvancedMacros.EVENT_HANDLER.onChatEvent(message, EventHandler.EventName.ChatFilter);
            ci.cancel();
            return;
        }
        AdvancedMacros.EVENT_HANDLER.onChatEvent(message, EventHandler.EventName.Chat);
    }
}