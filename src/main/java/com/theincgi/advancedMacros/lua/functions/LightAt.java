package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;

public class LightAt{

	public static class AllLight extends VarArgFunction{
		@Override
		public Varargs invoke(Varargs args) {
			BlockPos pos = null;
			if(args.narg()==3) {
				pos = new BlockPos(args.arg1().checkint(), args.arg(2).checkint(), args.arg(3).checkint());
			}else if(args.narg()==0) {
				pos = Minecraft.getMinecraft().player.getPosition();
			}else {
				throw new LuaError("Invalid args: NONE or X,Y,Z");
			}
			Chunk c = Minecraft.getMinecraft().player.getEntityWorld().getChunk(pos);
			int overall = c.getLightSubtracted(pos, 0);
			int block = LightAt.getBlockLight(c, pos);
			int sky = LightAt.getSkyLight(c, pos);
			LuaTable t = new LuaTable();
			t.set(1, LuaValue.valueOf(overall));
			t.set(2, LuaValue.valueOf(sky));
			t.set(3, LuaValue.valueOf(block));
			return t.unpack();
		}
	}
	public static class BlockLight extends VarArgFunction{
		@Override
		public Varargs invoke(Varargs args) {
			BlockPos pos = null;
			if(args.narg()==3) {
				pos = new BlockPos(args.arg1().checkint(), args.arg(2).checkint(), args.arg(3).checkint());
			}else if(args.narg()==0) {
				pos = Minecraft.getMinecraft().player.getPosition();
			}else {
				throw new LuaError("Invalid args: NONE or X,Y,Z");
			}
			Chunk c = Minecraft.getMinecraft().player.getEntityWorld().getChunk(pos);
			return LuaValue.valueOf(getBlockLight(c, pos));
		}
	}
	public static class SkyLight extends VarArgFunction{
		@Override
		public Varargs invoke(Varargs args) {
			BlockPos pos = null;
			if(args.narg()==3) {
				pos = new BlockPos(args.arg1().checkint(), args.arg(2).checkint(), args.arg(3).checkint());
			}else if(args.narg()==0) {
				pos = Minecraft.getMinecraft().player.getPosition();
			}else {
				throw new LuaError("Invalid args: NONE or X,Y,Z");
			}
			Chunk c = Minecraft.getMinecraft().player.getEntityWorld().getChunk(pos);
			return LuaValue.valueOf(getSkyLight(c, pos));
		}
	}
	
	private static int getBlockLight(Chunk c, BlockPos p) {
		return c.getLightFor(EnumSkyBlock.BLOCK, p);
	}
	private static int getSkyLight(Chunk c, BlockPos p) {
		return c.getLightFor(EnumSkyBlock.SKY, p);
	}
	
	//list.add("Biome: " + chunk.getBiome(blockpos, this.mc.world.getBiomeProvider()).getBiomeName());
    //list.add("Light: " + chunk.getLightSubtracted(blockpos, 0) + " (" + chunk.getLightFor(EnumSkyBlock.SKY, blockpos) + " sky, " + chunk.getLightFor(EnumSkyBlock.BLOCK, blockpos) + " block)");
}
