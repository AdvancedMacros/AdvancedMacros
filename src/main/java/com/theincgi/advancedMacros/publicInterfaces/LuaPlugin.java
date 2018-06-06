package com.theincgi.advancedMacros.publicInterfaces;

import org.luaj.vm2_v3_0_1.LuaValue;

/**Object must be of type LuaFunction<br>
 * A no arg constructor is also required (reflection used)*/
public interface LuaPlugin {
	/**Returns the name of the library used in require "yourLibraryName" */
	public String getLibraryName();
	
}
