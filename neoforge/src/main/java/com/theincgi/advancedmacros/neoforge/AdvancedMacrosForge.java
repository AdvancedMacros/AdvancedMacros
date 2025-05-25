package com.theincgi.advancedmacros.neoforge;

import com.theincgi.advancedmacros.AdvancedMacros;
import com.theincgi.advancedmacros.event.CombinedEventHandler;
import net.minecraft.client.MinecraftClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;

@Mod(AdvancedMacros.MOD_ID)
@Mod.EventBusSubscriber(modid = AdvancedMacros.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AdvancedMacrosForge {

    public AdvancedMacrosForge() {
        AdvancedMacros.init();
        NeoForge.EVENT_BUS.addListener(AdvancedMacrosForge::onTick);
    }

    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(AdvancedMacros.modKeybind);
    }

    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            CombinedEventHandler.onTick(MinecraftClient.getInstance());
        }
    }

}
