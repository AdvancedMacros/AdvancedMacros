package com.theincgi.advancedMacros.lua.functions.entity;

import org.luaj.vm2_v3_0_1.LuaFunction;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import com.theincgi.advancedMacros.misc.CallableTable;

public class CreateAABB extends CallableTable{
	static final String[] docName = {"createAABB"};
	public CreateAABB() {
		super(docName, new CreateGenerator());
		// TODO Auto-generated constructor stub
	}
	
	private static class CreateGenerator extends VarArgFunction {
		
	}
}
