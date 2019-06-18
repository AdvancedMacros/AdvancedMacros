package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.misc.CallableTable;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;

public class Disconnect extends CallableTable {

	public Disconnect() {
		super(new String[] {"disconnect"}, new Op());
	}

	private static class Op extends ZeroArgFunction {
		@Override
		public LuaValue call() {
			disconnect();
			return NONE;
		}
	}
	
	
	public static void disconnect() { //FIXME
		Minecraft mc = AdvancedMacros.getMinecraft();
		Utils.runOnMCAndWait(()->{
			if(mc.world != null && !mc.world.isRemote)
				mc.world.sendQuittingDisconnectingPacket();
			mc.loadWorld(null);
			mc.displayGuiScreen(new GuiMainMenu());
		});
	}
}
