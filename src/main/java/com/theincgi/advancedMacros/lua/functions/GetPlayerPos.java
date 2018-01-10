package com.theincgi.advancedMacros.lua.functions;

import java.util.List;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

import com.google.common.util.concurrent.ListenableFuture;
import com.theincgi.advancedMacros.ForgeEventHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public class GetPlayerPos extends VarArgFunction{
	@Override
	public Varargs invoke(Varargs args) {
		
		if(args.narg()==0){
			BlockPos pos = Minecraft.getMinecraft().player.getPosition();
			EntityPlayer player = Minecraft.getMinecraft().player;
			LuaTable t = new LuaTable();
			float pt = Minecraft.getMinecraft().getRenderPartialTicks();
			t.set(1, LuaValue.valueOf(ForgeEventHandler.accuPlayerX(pt, player)));
			t.set(2, LuaValue.valueOf(ForgeEventHandler.accuPlayerY(pt, player)));
			t.set(3, LuaValue.valueOf(ForgeEventHandler.accuPlayerZ(pt, player)));
			return t.unpack();
		}else{
			final String sPlayer = args.checkjstring(1);
			final LuaTable t = new LuaTable();
			t.set(1, FALSE);
			//ListenableFuture<Object> f = Minecraft.getMinecraft().addScheduledTask(new Runnable() { //scheduledTask is too slow
			//	@Override
			//	public void run() {
				List<EntityPlayer> players = Minecraft.getMinecraft().world.playerEntities;
					for(int i = 0; i<players.size(); i++) {
						EntityPlayer player = players.get(i);
					//for(EntityPlayer player : Minecraft.getMinecraft().world.playerEntities){
						if(player!=null && player.getName().equals(sPlayer)){
							float pt = Minecraft.getMinecraft().getRenderPartialTicks();
							t.set(1, LuaValue.valueOf(ForgeEventHandler.accuPlayerX(pt, player)));
							t.set(2, LuaValue.valueOf(ForgeEventHandler.accuPlayerY(pt, player)));
							t.set(3, LuaValue.valueOf(ForgeEventHandler.accuPlayerZ(pt, player)));
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