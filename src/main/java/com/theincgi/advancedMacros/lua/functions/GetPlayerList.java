package com.theincgi.advancedMacros.lua.functions;

import java.util.Iterator;
import java.util.Collection;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.google.common.util.concurrent.ListenableFuture;
import com.theincgi.advancedMacros.AdvancedMacros;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.text.ITextComponent;

public class GetPlayerList extends ZeroArgFunction {
	@Override
	public LuaValue call() {
		final LuaTable table=new LuaTable();
		ListenableFuture<Object> f = AdvancedMacros.getMinecraft().addScheduledTask(new Runnable() {
			@Override
			public void run() {
				int i = 1;
//				NetworkPlayerInfo[] list = (NetworkPlayerInfo[]) AdvancedMacros.getMinecraft().player.connection.getPlayerInfoMap().toArray();
//				
//				for(int j = 0; j<list.length; j++) {
//					NetworkPlayerInfo player = list[j];
//					table.set(i++, player.getGameProfile().getName());
//				}
				
				Minecraft mc = AdvancedMacros.getMinecraft();
				try {
					Iterator<NetworkPlayerInfo> iter = mc.getConnection().getPlayerInfoMap().iterator();
					while (iter.hasNext()) {
						NetworkPlayerInfo playerInfo = iter.next();
						/*ITextComponent disp = playerInfo.getDisplayName();
						if( disp != null ) {
							String name = disp.getUnformattedText();
							if( !name.isEmpty() ) // should it use String.trim() ?
								if( !Character.isWhitespace(name.charAt(0)) )
									//
						}*/

						GameProfile player = playerInfo.getGameProfile();
						if (player.getId().version() != 2)
							if (player.getName().charAt(0) != '!')
								table.set(i++, player.getName());
					}
				} catch (Throwable t) {
					t.printStackTrace();
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