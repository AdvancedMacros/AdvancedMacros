package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.event.TaskDispatcher;
import com.theincgi.advancedMacros.misc.CallableTable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConnectingScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.I18n;

public class Connect extends CallableTable{

	public Connect() {
		super(new String[] {"connect"}, new Op());
	}

	private static class Op extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue arg) {

			TaskDispatcher.addTask( () -> {
				Minecraft mc = AdvancedMacros.getMinecraft();
				if(mc.world != null)
					Disconnect.disconnect();
				ServerData sDat = new ServerData(I18n.format("selectServer.defaultName"), "", false);
				sDat.serverIP = arg.checkjstring();

				MultiplayerScreen mp = new MultiplayerScreen(null);
				
				mc.displayGuiScreen(new ConnectingScreen(mp, mc, sDat)); //TESTME direct connect
				
			});
			return NONE;
		}
	}
}
