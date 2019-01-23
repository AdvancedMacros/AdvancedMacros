package com.theincgi.advancedMacros.lua.functions.minecraft;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import com.theincgi.advancedMacros.lua.functions.GetRecipe;
import com.theincgi.advancedMacros.misc.CallableTable;

public class MinecraftFunctions extends LuaTable{
	
	public MinecraftFunctions() {
		for (OpCodes op : OpCodes.values()) {
			this.set(op.name(), new CallableTable(op.getDocName(), new DoOp(op)));
		}
		this.set("getRecipes", new GetRecipe());
	}
	
	private class DoOp extends VarArgFunction {
		OpCodes op;

		public DoOp(OpCodes op) {
			this.op = op;
		}
		
		@Override
		public Varargs invoke(Varargs args) {
			switch (op) {
			
			default:
				
			}
			return NONE;
		}
		
	}
	
	private static enum OpCodes{
		//getBlockList
		//getBlockDrops,
		//getMobDrops
		;
		String[] getDocName() {
			String[] name = new String[2];
			name[0] = "minecraft";
			switch (this) {
			//case getBlockDrops:
			//case getMobDrops:
			//case getRecipes:
			//	name[1] = this.name();
			default:
				return null;
			}
		}
	}
}
