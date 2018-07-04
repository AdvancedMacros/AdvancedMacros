package com.theincgi.advancedMacros.misc;

import org.luaj.vm2_v3_0_1.Globals;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;

import com.theincgi.advancedMacros.gui.Color;

public class PropertyPalette{
	public final String[] key;
	private LuaTable settings;
	
	public PropertyPalette() {
		settings = new LuaTable();
		key = null;
	}
	public PropertyPalette(String[] key, LuaTable t) {
		this.key = key;
		this.settings = t;
	}
	
	public PropertyPalette addColor( String key, Color c ) {
		getTableFromKey().set(key, c.toLuaValue());
		return this;
	}
	public PropertyPalette addColor( Color c, String...keyPath) {
		setProp(c.toLuaValue(), keyPath); return this;
	}
	public PropertyPalette setProp ( LuaValue c, String...keyPath) {
		LuaTable t = getTableFromKey();
		for(int i = 0; i<keyPath.length-1; i++) {
			if(t.get(keyPath[i]).isnil()) t.set(keyPath[i], new LuaTable());
			t = t.get(keyPath[i]).checktable();
		}
		t.set( keyPath[ keyPath.length -1 ], c );
		return this;
	}
	
	public Color getColor( String key ) {
		return Utils.parseColor( getTableFromKey().get(key) );
	}
	public Color getColor(String...keyPath) {
		return Utils.parseColor(getValue(keyPath));
	}
	public LuaValue getValue( String key ) {
		return getTableFromKey().get(key);
	}
	public LuaValue getValue( String...keyPath) {
		LuaTable t = getTableFromKey();
		for(int i = 0; i<keyPath.length-1; i++) {
			if(t.get(keyPath[i]).isnil()) t.set(keyPath[i], new LuaTable());
			t = t.get(keyPath[i]).checktable();
		}
		return t.get(keyPath[ keyPath.length -1 ]);
	}
	
	public LuaTable getTableFromKey() {
		LuaTable t = settings;
		if(key != null)
			for( String k : this.key) {
				if(t.get(k).isnil()) t.set(k, new LuaTable());
				t = t.get(k).checktable();
			}
		return t;
	}
	public PropertyPalette addColorIfNil(String key, Color color) {
		LuaTable t = getTableFromKey();
		if(t.get(key).isnil())
			t.set(key, color.toLuaValue());
		return this;
	}
	public PropertyPalette addColorIfNil( Color color, String...keyPath) {
		return setPropIfNil(color.toLuaValue(), keyPath);
	}
	public PropertyPalette setPropIfNil(LuaValue prop, String... keyPath) {
		LuaTable t = getTableFromKey();
		for(int i = 0; i<keyPath.length-1; i++) {
			if(t.get(keyPath[i]).isnil()) t.set(keyPath[i], new LuaTable());
			t = t.get(keyPath[i]).checktable();
		}
		if(t.get(keyPath[ keyPath.length -1 ]).isnil())
			t.set( keyPath[ keyPath.length -1 ], prop );
		return this;
	}
	public PropertyPalette propertyPaletteOf(String...keyPath) {
		LuaValue v = getValue(keyPath);
		if(v.isnil())
			setProp(v = new LuaTable(), keyPath);
		return new PropertyPalette(null, v.checktable());
	}
}
