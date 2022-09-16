package com.theincgi.advancedMacros.lua.functions.entity;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import com.theincgi.advancedMacros.misc.CallableTable;
import com.theincgi.advancedMacros.misc.Pair;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class CreateAABB extends CallableTable{
	static final String[] docName = {"createAABB"};
	public CreateAABB() {
		super(docName, new CreateGenerator());
	}
	
	private static class CreateGenerator extends VarArgFunction {
		
		@Override
		public Varargs invoke(Varargs args) {
			Pair<Vec3d, Varargs> min   = Utils.consumeVector(  args  , false, false);
			Pair<Vec3d, Varargs> max   = Utils.consumeVector(  min.b , false, false);
			
			AxisAlignedBB aabb = new AxisAlignedBB(min.a, max.a);
			return new GetAABB.AABB( aabb );
		}
		
	}
}
