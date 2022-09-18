package com.theincgi.advancedMacros.lua.functions;

import java.util.List;


import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import com.google.common.base.Predicates;
import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.misc.CallableTable;
import com.theincgi.advancedMacros.misc.Pair;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
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
			Minecraft mc = AdvancedMacros.getMinecraft();
			EntityPlayer p = mc.player;

			Pair<Vec3d, Varargs> vec   = Utils.consumeVector(  args  , true, true); //look angle
			if(vec.a == null) {
				vec.a = p.getLookVec();
			}
			Pair<Vec3d, Varargs> optVec = Utils.consumeVector( vec.b, true,  false); //from pos
			if(optVec.a == null) {
				optVec.a = p.getPositionEyes(0);
			}
			double distance = EntityPlayer.REACH_DISTANCE.getDefaultValue();
			if(optVec.b.arg1().isnumber()) {
				distance = optVec.b.arg1().checkdouble();
				optVec.b = optVec.b.subargs(2);
			}
			//System.out.println(distance);
			//System.out.println(optVec.b);

			boolean stopOnLiquid = optVec.b.optboolean(1, false);


			Vec3d end = optVec.a.add( vec.a.scale( distance ) );
			RayTraceResult rtr = AdvancedMacros.getMinecraft().world.rayTraceBlocks( optVec.a, end, stopOnLiquid, false, true);
			//AdvancedMacros.getMinecraft().objectMouseOver
			LuaValue result = Utils.rayTraceResultToLuaValue(rtr);
			return result;
		}
	}
	public static class EntityRayTrace extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue arg) {
			arg = arg.isnil() ? new LuaTable() : arg; 
			LuaTable args = arg.checktable();
			Minecraft mc = AdvancedMacros.getMinecraft();
			EntityPlayer p = mc.player;
			
			LuaValue yaw    = args.get("yaw");
			LuaValue pitch  = args.get("pitch");
			LuaValue from   = args.get("from").or(args.get("src")).or(args.get("source"));
			LuaValue to     = args.get("to").or(args.get("dest")).or(args.get("destination")).or(args.get("target"));
			LuaValue fluids = args.get("includeFluids").or(args.get("fluids")).or(args.get("fluid")).or(args.get("liquids")).or(args.get("liquid")).or(args.get("stopOnLiquid"));
			LuaValue reach  = args.get("reach").or(args.get("dist")).or(args.get("distance")).or(args.get("range"));
			
			
			Vec3d fromVec = p.getPositionEyes(mc.getRenderPartialTicks() );
			
			if( from.istable() ) {
				LuaTable t = from.checktable();
				fromVec = new Vec3d( t.get(1).checkdouble(), t.get(2).checkdouble(), t.get(3).checkdouble());
			}
			
			double distance = reach.optdouble( EntityPlayer.REACH_DISTANCE.getDefaultValue() ); 
			
			Vec3d toVec;
			if( to.istable() ) {
				LuaTable t = to.checktable();
				toVec = new Vec3d( t.get(1).checkdouble(), t.get(2).checkdouble(), t.get(3).checkdouble());
				if( reach.isnumber() ) {
					toVec = toVec.subtract( fromVec ).normalize().add( fromVec ); //limit to distance
				}
			}else if( yaw.isnumber() || pitch.isnumber() ) {
				//north  0  0 -1
				//west  -1  0  0
				toVec = new Vec3d(0, 0, -distance); //north
				toVec = toVec.rotateYaw( (float) yaw.optdouble(MathHelper.wrapDegrees(p.rotationYaw)) );
				toVec = toVec.rotatePitch( (float) pitch.optdouble( MathHelper.wrapDegrees(p.rotationPitch) ) );
			} else {
				toVec = fromVec.add( p.getLookVec().normalize().scale( distance ) );
			}
			
			//ray trace logic, referenced from EntityRender
			RayTraceResult rtr = null; //for output
			RayTraceResult blockRtr = mc.world.rayTraceBlocks(fromVec, toVec, fluids.optboolean(true), true, true);
			
			if(blockRtr != null) {
				rtr = blockRtr;
				toVec = toVec.normalize().scale( distance = blockRtr.hitVec.distanceTo(fromVec) ); //shorten to hit pos
			}
			
			Vec3d reachVector = toVec.subtract(fromVec);
			AxisAlignedBB area = p.getEntityBoundingBox()
					.expand( reachVector.x, reachVector.y, reachVector.z )
					.grow( 1, 1, 1 );
			List<Entity> inArea = mc.world.getEntitiesInAABBexcluding(p, area, Predicates.and( EntitySelectors.NOT_SPECTATING, (e)->{
				return e != null && e.canBeCollidedWith();
			} ));
			
			for( Entity e : inArea ) {
				AxisAlignedBB entityAABB = e.getEntityBoundingBox().grow(e.getCollisionBorderSize());
				RayTraceResult eHit = entityAABB.calculateIntercept(fromVec, toVec);
				
				if( entityAABB.contains(fromVec) ) {
					if( distance >= 0 ) {
						rtr = eHit == null ? new RayTraceResult(e, fromVec) : eHit;
						distance = 0;
					}
				} else if( eHit != null ) {
					double eDist =fromVec.distanceTo(eHit.hitVec);
					if( eDist < distance || distance == 0 ) {
						if( e.getLowestRidingEntity() == p.getLowestRidingEntity() && !e.canRiderInteract() ) {
							if( distance == 0 )
								rtr = eHit;
						} else {
							rtr = new RayTraceResult(e, eHit.hitVec);//eHit; eHit is of block raytrace even though it's for an entity, because it checked the box only
							distance = eDist;
						}
					}
				}
			}
			//skip miss by distance
			
			return Utils.rayTraceResultToLuaValue(rtr);
		}
	}
}