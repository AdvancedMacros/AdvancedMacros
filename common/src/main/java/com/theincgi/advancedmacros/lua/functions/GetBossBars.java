package com.theincgi.advancedmacros.lua.functions;

import com.theincgi.advancedmacros.AdvancedMacros;
import com.theincgi.advancedmacros.misc.Utils;
import com.theincgi.advancedmacros.mixin.BossBarHudAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import org.luaj.vm2_v3_0_1.Lua;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ThreeArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import java.util.Map;
import java.util.UUID;

public class GetBossBars extends ZeroArgFunction {

    @Override
    public LuaValue call() {
        Map<UUID, ClientBossBar> bossBars = ((BossBarHudAccessor)AdvancedMacros.getMinecraft().inGameHud.getBossBarHud()).getBossBars();
        LuaTable t = new LuaTable();
        for (Map.Entry<UUID, ClientBossBar> entry : bossBars.entrySet()) {
            LuaTable bossBarTable = new LuaTable();
            ClientBossBar bossBar = entry.getValue();
            bossBarTable.set("name", bossBar.getName().getString());
            bossBarTable.set("percent", bossBar.getPercent());

            t.set(entry.getKey().toString(), bossBarTable);
        }
        return t;
    }

}
