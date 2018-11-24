package com.theincgi.advancedMacros.publicInterfaces;

/**Object must be of type LuaFunction<br>
 * A no arg constructor is also required (reflection used)*/
public interface LuaPlugin {
	/**Returns the name of the library used in require "yourLibraryName" */
	public String getLibraryName();
	
}
