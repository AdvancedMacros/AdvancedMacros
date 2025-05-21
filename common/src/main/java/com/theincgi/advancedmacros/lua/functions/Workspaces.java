package com.theincgi.advancedmacros.lua.functions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedmacros.AdvancedMacros;
import com.theincgi.advancedmacros.lua.LuaDebug;
import com.theincgi.advancedmacros.misc.LuaClassUtils;
import com.theincgi.advancedmacros.misc.Settings;
import com.theincgi.advancedmacros.misc.Utils;
import com.theincgi.advancedmacros.misc.Workspace;

public class Workspaces {
	
	
	
	
    
//    public static class SetWorkspaceByName extends OneArgFunction {
//    	@Override
//    	public LuaValue call(LuaValue arg) {
//    		Workspace workspace = getWorkspaceByName(arg.checkjstring());
//    		if(Thread.currentThread() == AdvancedMacros.getMinecraftThread()) {
//    			Utils.setMCThreadWorkspace( workspace );
//    		} else {
//    			LuaDebug.LuaThread.getCurrent().workspace = workspace;
//    		}
//    		return NONE;
//    	}
//    }
    
//    /**
//     * @throws LuaError if workspace not defined
//     * */
//    public static Workspace getWorkspaceByName( String name ) {
//    	name = name.trim();
//		if(name.isBlank())
//			throw new LuaError("Invalid workspace name \""+name+"\"");
//		
//		Optional<String> path = Settings.getWorkspacePath( name );
//		if( path.isEmpty() )
//			throw new LuaError("Workspace '"+name+"' is not defined in getSettings().workspaces");
//		
//		return new Workspace(name, path.get());
//    }
}
