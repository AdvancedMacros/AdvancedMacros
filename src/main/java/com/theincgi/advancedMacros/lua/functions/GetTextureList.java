package com.theincgi.advancedMacros.lua.functions;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;

import net.minecraft.client.renderer.texture.AtlasTexture;//TextureMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class GetTextureList extends ZeroArgFunction{
	private final Map<String, TextureAtlasSprite> mapRegisteredSprites;
	
	public GetTextureList() throws NoSuchFieldException, RuntimeException, IllegalAccessException {
		this.mapRegisteredSprites = new HashMap<String, TextureAtlasSprite>();
//		AtlasTexture map = AdvancedMacros.getMinecraft().textureManager.getTexture(textureLocation)getTextureMap();
//		Field f = ObfuscationReflectionHelper.findField(AtlasTexture.class, "field_94252_e"); //TESTME getTextureList
//		//Field f = TextureMap.class.getDeclaredField(isObf?"j":"mapRegisteredSprites");
//		f.setAccessible(true);
//		mapRegisteredSprites = (Map<String, TextureAtlasSprite>) f.get(map);
	}
	
	@Override
	public LuaValue call() {
		LuaTable t = new LuaTable();
		int i = 0;
		for(TextureAtlasSprite o : mapRegisteredSprites.values()) {
			t.set(++i, o.getName().toString());
		}
		return t;
	}
}
