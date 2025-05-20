package com.theincgi.advancedmacros.lua.functions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;

import com.theincgi.advancedmacros.AdvancedMacros;

import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

public class GetResource extends OneArgFunction {
	
	@Override
	public LuaValue call(LuaValue arg) {	
		Optional<Resource> resource = AdvancedMacros.getMinecraft()
			.getResourceManager()
			.getResource(new Identifier(AdvancedMacros.MOD_ID, arg.checkjstring()));
		
		if(resource.isEmpty())
			return FALSE;
		
		
		try ( InputStream in = resource.get().getInputStream() ){
			return LuaValue.valueOf( new String(in.readAllBytes()) );
		} catch (IOException e) {
			throw new LuaError( e );
		}
		
	}
	
}
