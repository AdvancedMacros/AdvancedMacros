package com.theincgi.advancedmacros.mixin.events;

import java.util.HashMap;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.theincgi.advancedmacros.AdvancedMacros;
import com.theincgi.advancedmacros.event.EventHandlers;
import com.theincgi.advancedmacros.event.events.KeyEvent;
import com.theincgi.advancedmacros.gui.Gui;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

@Mixin(Keyboard.class)
public class MixinKeyboard {
	
	private HashMap<Integer, Integer> repeatingKeys = new HashMap<>();
	
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onKey", at = @At("HEAD"))
    private void onKey(long window, int key, int scancode, int action, int mods, final CallbackInfo info) {
        if (window != client.getWindow().getHandle()) {
            return;
        }
        @SuppressWarnings("resource")
		Screen s = AdvancedMacros.getMinecraft().currentScreen;
        if (action == GLFW.GLFW_PRESS) {
            repeatingKeys.put(scancode, 0);
        }
        if (action == GLFW.GLFW_REPEAT && s == null) {
            return;
        }
        if (s != null) {
            if (s instanceof Gui g) {
                int n;
                repeatingKeys.put(scancode, n = (repeatingKeys.getOrDefault(scancode, 0) + 1));
                g.onKeyRepeated(g, key, scancode, mods, n);
            }
            return;
        }
        
        var event = new KeyEvent(window, key, scancode, action, mods, info);
        EventHandlers.onKey.onEvent(event);
        
//        AdvancedMacros.EVENT_HANDLER.onKeyInput(key, scancode, action, mods);
    }
    
    

}
