package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import com.theincgi.advancedMacros.misc.CallableTable;
import com.theincgi.advancedMacros.misc.Pair;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;


public class RayTrace {
	
	private static CallableTable func;
	
	private RayTrace() {}
	
	public static CallableTable getFunc() {
		if(func==null) 
			genFunc();
		return func;
	}
	
	private static void genFunc() {
		func = new CallableTable(new String[] {"rayTrace"}, new RayTraceFunc());
	}
	
	public static class RayTraceFunc extends VarArgFunction{
		@Override
		public Varargs invoke(Varargs args) {
			//args: {vector}, <{from}>, <maxDist/REACH_LIMIT>, stopOnLiquid
			//args: {vector}, <maxDist/REACH_LIMIT>, stopOnLiquid
			Minecraft mc = Minecraft.getMinecraft();
			EntityPlayer p = mc.player;


			Pair<Vec3d, Varargs> vec   = Utils.consumeVector(  args  , true, true); //look angle
			if(vec.a == null) {
				vec.a = p.getLookVec();
			}
			Pair<Vec3d, Varargs> optVec = Utils.consumeVector( vec.b, true,  false); //from pos
			if(optVec.a == null) {
				optVec.a = p.getPositionEyes(0);
			}
			double distance = p.REACH_DISTANCE.getDefaultValue();
			if(optVec.b.arg1().isnumber()) {
				distance = optVec.b.arg1().checkdouble();
				optVec.b = optVec.b.subargs(2);
			}
			//System.out.println(distance);
			//System.out.println(optVec.b);

			boolean stopOnLiquid = optVec.b.optboolean(1, false);


			Vec3d end = optVec.a.add( vec.a.scale( distance ) );
			RayTraceResult rtr = Minecraft.getMinecraft().world.rayTraceBlocks( optVec.a, end, stopOnLiquid, false, true);
			if(rtr==null) return FALSE;
			LuaTable result = new LuaTable();

			switch (rtr.typeOfHit) {
			case MISS:
				return FALSE;
			case ENTITY:
				result.set("entity", Utils.entityToTable(rtr.entityHit));
			case BLOCK:
				BlockPos pos = rtr.getBlockPos();
				result.set("pos", Utils.blockPosToTable(pos));
				IBlockState ibs = mc.world.getBlockState(pos);
				TileEntity te = mc.world.getTileEntity(pos);
				result.set("block", Utils.blockToTable(ibs, te));
			default:
				break;
			}
			result.set("side", rtr.sideHit.name().toLowerCase());
			result.set("subHit", rtr.subHit);
			return result;
		}
	}
}