package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ThreeArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.IChunk;

public class GetBlock extends ThreeArgFunction{
	@Override
	public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
		int x = (arg1.checkint()), y = arg2.checkint(), z = (arg3.checkint());
		BlockPos pos = new BlockPos(x,y,z);
		IChunk chunk = AdvancedMacros.getMinecraft().world.getChunk(pos);
		
		TileEntity te = AdvancedMacros.getMinecraft().world.getTileEntity(pos);
		if(AdvancedMacros.getMinecraft().world.getChunkProvider().isChunkLoaded(chunk.getPos())){
			return LuaValue.FALSE;
		}
		
		BlockState block = chunk.getBlockState(new BlockPos(x,y,z));
		LuaTable result = Utils.blockToTable(block, te);
		BlockState s;
		result.set("mapColor", Utils.parseColor(block.getMaterialColor(AdvancedMacros.getMinecraft().player.world, pos)));
		return result;
	}
}