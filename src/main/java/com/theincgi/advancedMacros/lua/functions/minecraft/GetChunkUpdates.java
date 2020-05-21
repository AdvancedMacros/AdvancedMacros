package com.theincgi.advancedMacros.lua.functions.minecraft;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;


public class GetChunkUpdates extends ZeroArgFunction{
	@Override
	public LuaValue call() {
		return LuaValue.valueOf(-1); //FIXME valueOf(ChunkRender.renderChunksUpdated);
	}
}
