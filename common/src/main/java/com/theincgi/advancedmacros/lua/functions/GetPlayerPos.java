package com.theincgi.advancedmacros.lua.functions;

import com.theincgi.advancedmacros.event.CombinedEventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import java.util.List;

public class GetPlayerPos extends VarArgFunction {

    @Override
    public Varargs invoke(Varargs args) {

        if (args.narg() == 0) {
            BlockPos pos = MinecraftClient.getInstance().player.getBlockPos();
            PlayerEntity player = MinecraftClient.getInstance().player;
            LuaTable t = new LuaTable();
            float pt = MinecraftClient.getInstance().getTickDelta();
            t.set(1, LuaValue.valueOf(CombinedEventHandler.accuPlayerX(pt, player)));
            t.set(2, LuaValue.valueOf(CombinedEventHandler.accuPlayerY(pt, player)));
            t.set(3, LuaValue.valueOf(CombinedEventHandler.accuPlayerZ(pt, player)));
            return t.unpack();
        } else {
            final String sPlayer = args.checkjstring(1);
            final LuaTable t = new LuaTable();
            t.set(1, FALSE);
            //ListenableFuture<Object> f = MinecraftClient.getInstance().addScheduledTask(new Runnable() { //scheduledTask is too slow
            //	@Override
            //	public void run() {
            List<AbstractClientPlayerEntity> players = MinecraftClient.getInstance().world.getPlayers();
            for (int i = 0; i < players.size(); i++) {
                PlayerEntity player = players.get(i);
                //for(EntityPlayer player : MinecraftClient.getInstance().world.playerEntities){
                if (player != null && player.getName().equals(sPlayer)) {
                    float pt = MinecraftClient.getInstance().getTickDelta();
                    t.set(1, LuaValue.valueOf(CombinedEventHandler.accuPlayerX(pt, player)));
                    t.set(2, LuaValue.valueOf(CombinedEventHandler.accuPlayerY(pt, player)));
                    t.set(3, LuaValue.valueOf(CombinedEventHandler.accuPlayerZ(pt, player)));
                    break;
                }
            }
            //		}
            //});
            //	while(!f.isDone()) {
            //try {
            //Thread.sleep(1);
            //} catch (InterruptedException e) {}
            //	}
            return t.unpack();
        }
    }

}
