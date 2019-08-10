package com.theincgi.advancedMacros.lua.functions;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.google.common.util.concurrent.ListenableFuture;
import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.event.TaskDispatcher;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.NetworkPlayerInfo;

public class GetPlayerList extends ZeroArgFunction {
	@Override
	public LuaValue call() {
		
		ListenableFuture<LuaTable> f = TaskDispatcher.addTask(new Callable<LuaTable>() {
			@Override
			public LuaTable call() throws Exception {
				LuaTable table=new LuaTable();
				int i = 1;
//				NetworkPlayerInfo[] list = (NetworkPlayerInfo[]) AdvancedMacros.getMinecraft().player.connection.getPlayerInfoMap().toArray();
//				
//				for(int j = 0; j<list.length; j++) {
//					NetworkPlayerInfo player = list[j];
//					table.set(i++, player.getGameProfile().getName());
//				}
				
				Minecraft mc = AdvancedMacros.getMinecraft();
				Iterator<NetworkPlayerInfo> iter = mc.getConnection().getPlayerInfoMap().iterator();
				while(iter.hasNext()) {
					NetworkPlayerInfo playerInfo = iter.next();
					String name = mc.ingameGUI.getTabList().getDisplayName(playerInfo).getUnformattedComponentText();
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
				return table;
			}
		});
		while(!f.isDone()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e1) {
				throw new LuaError("Thread interrupted");
			}
		}
		try {
			return f.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new LuaError(e);
		}
	}

}