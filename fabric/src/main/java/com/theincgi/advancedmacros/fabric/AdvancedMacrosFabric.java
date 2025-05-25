package com.theincgi.advancedmacros.fabric;

import com.theincgi.advancedmacros.AdvancedMacros;
import com.theincgi.advancedmacros.event.CombinedEventHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public class AdvancedMacrosFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AdvancedMacros.init();
        ClientTickEvents.END_CLIENT_TICK.register(CombinedEventHandler::onTick);
        KeyBindingHelper.registerKeyBinding(AdvancedMacros.modKeybind);
    }

}
