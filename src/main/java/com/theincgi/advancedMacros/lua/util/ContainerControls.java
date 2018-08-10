package com.theincgi.advancedMacros.lua.util;

import org.luaj.vm2_v3_0_1.LuaTable;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;

public class ContainerControls extends LuaTable{
	Container inv;
	public ContainerControls(Container inv) {
		this.inv = inv;
	}
}
