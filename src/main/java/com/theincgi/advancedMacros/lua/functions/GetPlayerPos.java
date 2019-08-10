package com.theincgi.advancedMacros.lua.functions;

import java.util.List;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.event.ForgeEventHandler;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

public class GetPlayerPos extends VarArgFunction{
	@Override
	public Varargs invoke(Varargs args) {
		
		if(args.narg()==0){
			BlockPos pos = AdvancedMacros.getMinecraft().player.getPosition();
			PlayerEntity player = AdvancedMacros.getMinecraft().player;
			LuaTable t = new LuaTable();
			float pt = AdvancedMacros.getMinecraft().getRenderPartialTicks();
			t.set(1, LuaValue.valueOf(ForgeEventHandler.accuPlayerX(pt, player)));
			t.set(2, LuaValue.valueOf(ForgeEventHandler.accuPlayerY(pt, player)));
			t.set(3, LuaValue.valueOf(ForgeEventHandler.accuPlayerZ(pt, player)));
			return t.unpack();
		}else{
			final String sPlayer = args.checkjstring(1);
			final LuaTable t = new LuaTable();
			t.set(1, FALSE);
			//ListenableFuture<Object> f = AdvancedMacros.getMinecraft().addScheduledTask(new Runnable() { //scheduledTask is too slow
			//	@Override
			//	public void run() {
				List<AbstractClientPlayerEntity> players = AdvancedMacros.getMinecraft().world.getPlayers();
					for(int i = 0; i<players.size(); i++) {
						PlayerEntity player = players.get(i);
					//for(EntityPlayer player : AdvancedMacros.getMinecraft().world.playerEntities){
						if(player!=null && player.getName().equals(sPlayer)){
							float pt = AdvancedMacros.getMinecraft().getRenderPartialTicks();
							t.set(1, LuaValue.valueOf(ForgeEventHandler.accuPlayerX(pt, player)));
							t.set(2, LuaValue.valueOf(ForgeEventHandler.accuPlayerY(pt, player)));
							t.set(3, LuaValue.valueOf(ForgeEventHandler.accuPlayerZ(pt, player)));
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