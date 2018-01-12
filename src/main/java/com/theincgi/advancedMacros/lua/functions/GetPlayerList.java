package com.theincgi.advancedMacros.lua.functions;

import java.util.Collection;
import java.util.Iterator;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.google.common.util.concurrent.ListenableFuture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;

public class GetPlayerList extends ZeroArgFunction {
	@Override
	public LuaValue call() {
		final LuaTable table=new LuaTable();
		ListenableFuture<Object> f = Minecraft.getMinecraft().addScheduledTask(new Runnable() {
			@Override
			public void run() {
				int i = 1;
//				NetworkPlayerInfo[] list = (NetworkPlayerInfo[]) Minecraft.getMinecraft().player.connection.getPlayerInfoMap().toArray();
//				
//				for(int j = 0; j<list.length; j++) {
//					NetworkPlayerInfo player = list[j];
//					table.set(i++, player.getGameProfile().getName());
//				}
				
				Minecraft mc = Minecraft.getMinecraft();
				Iterator<NetworkPlayerInfo> iter = mc.getConnection().getPlayerInfoMap().iterator();
				while(iter.hasNext()) {
					NetworkPlayerInfo playerInfo = iter.next();
					String name = mc.ingameGUI.getTabList().getPlayerName(playerInfo);
					String formated   = name
							.replaceAll("&", "&&")
							.replaceAll("\u00A7", "&")
							.replaceAll("&k", "&O") //Obfuscated
							.replaceAll("&l", "&B") //Bold
							.replaceAll("&m", "&S") //Strikethru
							.replaceAll("&o", "&I") //Italics
							.replaceAll("&r", "&f")   //reset (to white in this case)
							;
					if(name!=null)
						table.set(i++, formated);
				}
				
			}
		});
		while(!f.isDone()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e1) {
				throw new LuaError("Thread interrupted");
			}
		}
		return table;
	}

}