package com.theincgi.advancedmacros.lua.functions.entity;

import com.theincgi.advancedmacros.AdvancedMacros;
import com.theincgi.advancedmacros.event.CombinedEventHandler.RenderFlags;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ThreeArgFunction;

public class HighlightEntity extends ThreeArgFunction {

    @Override
    public LuaValue call(LuaValue arg, LuaValue action, LuaValue active) {
        Entity e = MinecraftClient.getInstance().world.getEntityById(arg.checkint());
        boolean flag = active.optboolean(true);
        switch (action.checkjstring()) {
            case "glow": {
                RenderFlags r = AdvancedMacros.EVENT_HANDLER.entityRenderFlags.computeIfAbsent(e, (key) -> {
                    return new RenderFlags();
                });
                r.setGlow(flag);
                break;
            }
            case "xray": {
                RenderFlags r = AdvancedMacros.EVENT_HANDLER.entityRenderFlags.computeIfAbsent(e, (key) -> {
                    return new RenderFlags();
                });
                r.setXray(flag);
                break;
            }
            default:
                throw new LuaError("Unknown action type '" + action.checkjstring() + "'");
        }

        return NONE;
    }

}
