package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ThreeArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

public class GetBlock extends ThreeArgFunction{
	@Override
	public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
		int x = (arg1.checkint()), y = arg2.checkint(), z = (arg3.checkint());
		BlockPos pos = new BlockPos(x,y,z);
		Chunk chunk = AdvancedMacros.getMinecraft().world.getChunkFromBlockCoords(pos);
		TileEntity te = AdvancedMacros.getMinecraft().world.getTileEntity(pos);
		if(!chunk.isLoaded()){
			return LuaValue.FALSE;
		}
		IBlockState block = chunk.getBlockState(x,y,z);
		LuaTable result = Utils.blockToTable(block, te);
		result.set("mapColor", Utils.parseColor(block.getMapColor(AdvancedMacros.getMinecraft().player.world, pos)));
		return result;
	}
}