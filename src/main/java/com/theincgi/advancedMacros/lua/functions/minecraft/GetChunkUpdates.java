package com.theincgi.advancedMacros.lua.functions.minecraft;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import net.minecraft.client.renderer.chunk.ChunkRender;

public class GetChunkUpdates extends ZeroArgFunction{
	@Override
	public LuaValue call() {
		return valueOf(ChunkRender.renderChunksUpdated);
	}
}
