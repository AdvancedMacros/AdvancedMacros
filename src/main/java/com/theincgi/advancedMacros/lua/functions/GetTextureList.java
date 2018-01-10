package com.theincgi.advancedMacros.lua.functions;

import java.lang.reflect.Field;
import java.util.Map;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;

public class GetTextureList extends ZeroArgFunction{
	private final Map<String, TextureAtlasSprite> mapRegisteredSprites;
	
	public GetTextureList() throws NoSuchFieldException, RuntimeException, IllegalAccessException {
		TextureMap map = Minecraft.getMinecraft().getTextureMapBlocks();
		Field f = TextureMap.class.getDeclaredField("mapRegisteredSprites");
		f.setAccessible(true);
		mapRegisteredSprites = (Map<String, TextureAtlasSprite>) f.get(map);
	}
	
	@Override
	public LuaValue call() {
		LuaTable t = new LuaTable();
		int i = 0;
		for(TextureAtlasSprite o : mapRegisteredSprites.values()) {
			t.set(++i, o.getIconName());
		}
		return t;
	}
}
