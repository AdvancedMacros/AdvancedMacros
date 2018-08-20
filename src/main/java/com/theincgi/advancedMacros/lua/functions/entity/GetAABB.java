package com.theincgi.advancedMacros.lua.functions.entity;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import com.theincgi.advancedMacros.misc.CallableTable;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class GetAABB {
	private CallableTable func;
	private static final String[] LOCATION = {"entity","getAABB"};
	public GetAABB() {
		func = new CallableTable(LOCATION, new Get());
	}
	
	public CallableTable getFunc() {
		return func;
	}
	
	private class Get extends VarArgFunction {
		Minecraft mc = Minecraft.getMinecraft();
		World world = mc.world;
		/**
		 * Given 1 table:
		 * Given 3 numbers: used as block pos break;
		 * Given 1 number : used as entity id break;
		 * */
		@Override
		public Varargs invoke(Varargs args) {
			world = mc.world;
			if(args.narg()==3 || args.arg1().istable()) {
				BlockPos pos;
				if(args.arg1().istable()) { LuaValue t = args.arg1();
					pos = new BlockPos(t.get(1).checkdouble(), t.get(2).checkdouble(), t.get(3).checkdouble());
				}else {
					pos = new BlockPos(args.arg(1).checkdouble(), args.arg(2).checkdouble(), args.arg(3).checkdouble());
				}
				AxisAlignedBB bb = world.getBlockState(pos).getSelectedBoundingBox(world, pos);
				if(bb==null) return FALSE;
				return new AABB(bb);
			}else if(args.narg() == 1) {
				Entity e = world.getEntityByID(args.arg1().checkint());
				AxisAlignedBB bb = e.getEntityBoundingBox();
				if(bb==null) return FALSE;
				return new AABB(bb);
			}else {
				throw new LuaError("Invalid arguments");
			}
		}
	}
	
	private static class AABB extends LuaTable {
		AxisAlignedBB aabb;
		public AABB(AxisAlignedBB aabb) {
			super();
			this.aabb = aabb;
			for (OpCode code : OpCode.values()) {
				this.set(code.name(), new DoOp(code));
			}
			this.set("__class", "AxisAlignedBoudingBox");
		}
		
		private class DoOp extends VarArgFunction {
			OpCode code;

			public DoOp(OpCode code) {
				super();
				this.code = code;
			}
			
			@Override
			public Varargs invoke(Varargs args) {
				switch (code) {
				case contains: //check if a point is inside the AABB
					return valueOf(aabb.contains(new Vec3d(args.arg1().checkdouble(), args.arg(2).checkdouble(), args.arg(3).checkdouble())));
				case contract: //make it smaller in one dir, negative effects lower end 
					return new AABB( aabb.contract(args.arg1().checkdouble(), args.arg(2).checkdouble(), args.arg(3).checkdouble()) );
				case expand:
					return new AABB( aabb.expand(args.arg1().checkdouble(), args.arg(2).checkdouble(), args.arg(3).checkdouble()) );
				case getCenter:{
					LuaTable temp = new LuaTable();
					Vec3d v = aabb.getCenter();
					temp.set(1, v.x);
					temp.set(2, v.y);
					temp.set(3, v.z);
					return temp.unpack();
				}
				case getPoints:{
					LuaTable temp = new LuaTable();
					temp.set(1, aabb.minX);
					temp.set(2, aabb.minY);
					temp.set(3, aabb.minZ);
					temp.set(4, aabb.maxX);
					temp.set(5, aabb.maxY);
					temp.set(6, aabb.maxZ);
					return temp.unpack();
				}
				case grow:{
					double def = args.arg1().checkdouble();
					return new AABB(aabb.grow( def, args.optdouble(2, def), args.optdouble(3, def) ));
				}
				case intersects:{
					if(args.arg1() instanceof AABB ) {
						AABB a = (AABB) args.arg1();
						return valueOf(aabb.intersects(a.aabb));
					}
					throw new LuaError("Not an Axis Aligned Bounding Box");
				}
				case intersect:
					if(args.arg1() instanceof AABB ) {
						AABB a = (AABB) args.arg1();
						return new AABB(aabb.intersect(a.aabb));
					}
					throw new LuaError("Not an Axis Aligned Bounding Box");
				case offset:
					return new AABB( aabb.offset(args.arg1().checkdouble(), args.arg(2).checkdouble(), args.arg(3).checkdouble()) );
				case shrink:
					return new AABB( aabb.shrink(args.arg1().checkdouble()) );
				case union:
					if(args.arg1() instanceof AABB ) {
						AABB a = (AABB) args.arg1();
						return new AABB(aabb.union(a.aabb));
					}
					throw new LuaError("Not an Axis Aligned Bounding Box");
				default:
					throw new LuaError("Undefined operation: "+code.name());
				}
			}
		}
		
		static enum OpCode {
			getPoints,
			contains,
			expand,
			contract,
			getCenter,
			grow,
			shrink,
			intersect,
			intersects,
			offset,
			union,
		}
	}
}
