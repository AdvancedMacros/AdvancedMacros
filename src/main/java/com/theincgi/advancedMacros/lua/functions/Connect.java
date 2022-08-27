package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.misc.CallableTable;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreenServerList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.I18n;

public class Connect extends CallableTable{

	public Connect() {
		super(new String[] {"connect"}, new Op());
	}

	private static class Op extends TwoArgFunction {
		@Override
		public LuaValue call(LuaValue ip, LuaValue name) {

			Utils.runOnMCAndWait( () -> {
				try {
				Minecraft mc = AdvancedMacros.getMinecraft();
				if(mc.world != null)
					Disconnect.disconnect();
				ServerData sDat = new ServerData("", "", false);
				sDat.serverName = name.optjstring(I18n.format("selectServer.defaultName"));
				sDat.serverIP = ip.checkjstring();

				GuiMultiplayer mp = new GuiMultiplayer(null);

				net.minecraftforge.fml.client.FMLClientHandler.instance().connectToServer(mp, sDat);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			});
			return NONE;
		}
	}
}
