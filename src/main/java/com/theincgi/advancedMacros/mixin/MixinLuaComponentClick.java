package com.theincgi.advancedMacros.mixin;

import com.theincgi.advancedMacros.misc.LuaTextComponentClickEvent;

import org.apache.logging.log4j.Logger;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.Final;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;

@Mixin(GuiScreen.class)
public class MixinLuaComponentClick {

    @Shadow
    @Final
    private static Logger LOGGER;

    @Inject(
        method = "handleComponentClick(Lnet/minecraft/util/text/ITextComponent;)Z",
        at = @At(
            value = "INVOKE",
            target = "org/apache/logging/log4j/Logger.error(Ljava/lang/String;Ljava/lang/Object;)V",
            remap = false,
            ordinal = 0
            ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void handleComponentClick(ITextComponent component, CallbackInfoReturnable<Boolean> ci, ClickEvent clickevent) {
        LOGGER.info("Attempting to handle {}", (Object) clickevent.getClass().toString());
        if (isLuaTextComponent(clickevent)) {
            clickLuaTextComponent(clickevent);
        } else {
            LOGGER.error("Don't know how to handle {}", (Object)clickevent);
        }
    }

    @Redirect(
        method = "handleComponentClick(Lnet/minecraft/util/text/ITextComponent;)Z",
        at = @At(
            value = "INVOKE",
            target = "org/apache/logging/log4j/Logger.error(Ljava/lang/String;Ljava/lang/Object;)V",
            remap = false,
            ordinal = 0
        )
    )
    public void killLogger(Logger logger, String message, Object clickevent) {}

    public boolean isLuaTextComponent(Object obj) {
        return obj instanceof LuaTextComponentClickEvent;
    }

    public void clickLuaTextComponent(Object ltc) {
        ((LuaTextComponentClickEvent)ltc).click();
    }
}
