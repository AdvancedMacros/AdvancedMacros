package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;

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

	private static class Op extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue arg) {

			Utils.runOnMCAndWait( () -> {
				try {
				Minecraft mc = Minecraft.getMinecraft();
				if(mc.world != null)
					Disconnect.disconnect();
				ServerData sDat = new ServerData(I18n.format("selectServer.defaultName"), "", false);
				sDat.serverIP = arg.checkjstring();

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
