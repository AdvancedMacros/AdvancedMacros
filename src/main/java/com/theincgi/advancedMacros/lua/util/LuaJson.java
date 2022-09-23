package com.theincgi.advancedMacros.lua.util;

import static org.luaj.vm2_v3_0_1.LuaValue.NIL;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;

public class LuaJson {

	private LuaJson() {
	}

	public static LuaTable makeLib() {
		LuaTable lib = new LuaTable();

		lib.set("setObjMode", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue arg1, LuaValue arg2) {
				setObjMode(arg1.checktable(), arg2.checkboolean());
				return arg1;
			}
		});

		lib.set("toJson", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				try {
					return userdataOf(auto(arg.checktable()));
				} catch (JSONException e) {
					throw new LuaError(e);
				}
			}
		});
		lib.set("fromJson", new OneArgFunction() {

			@Override
			public LuaValue call(LuaValue arg) {
				Object data = arg.checkuserdata();

				try {
					if (data instanceof JSONArray) {
						return table((JSONArray) data);
					} else if (data instanceof JSONObject) {
						return table((JSONObject) data);
					} else {
						throw new LuaError("Userdata was not a JSON structure");
					}
				} catch (JSONException e) {
					throw new LuaError(e);
				}
			}
		});
		return lib;
	}

	public static LuaTable setObjMode(LuaTable tbl, boolean asObj) {
		LuaValue meta = tbl.getmetatable();
		if (meta.isnil()) {
			meta = new LuaTable();
			tbl.setmetatable(meta);
		}
		meta.set("__json_obj", asObj);
		return tbl;
	}

	public static boolean isPreferObj(LuaTable tbl) {
		LuaValue meta = tbl.getmetatable();
		if (meta.isnil())
			return false;
		return meta.get("__json_obj").optboolean(false);
	}

	public static Object auto(LuaTable tbl) throws JSONException {
		if (tbl.keys().length == tbl.length() && !isPreferObj(tbl)) {
			return array(tbl);
		}
		return object(tbl);
	}

	public static JSONObject object(LuaTable tbl) throws JSONException {
		JSONObject out = new JSONObject();

		for (Varargs v = tbl.next(NIL); !v.arg1().isnil(); v = tbl.next(v.arg1())) {
			if (!v.isstring(1))
				continue;

			String key = v.checkjstring(1);
			LuaValue val = v.arg(2);

			

			if (val.isint())
				out.put(key, val.checkint());
			
			else if (val.islong())
				out.put(key, val.checklong());
			
			else if (val.isnumber())
				out.put(key, val.todouble());

			else if (val.isboolean())
				out.put(key, val.checkboolean());

			else if (val.istable())
				out.put(key, auto(val.checktable()));
			
			else if (val.isstring())
				out.put(key, val.checkstring());
		}

		return out;
	}

	public static JSONArray array(LuaTable tbl) throws JSONException {
		JSONArray out = new JSONArray();

		for (int i = 1; i <= tbl.length(); i++) {
			LuaValue val = tbl.get(i);

			
			if (val.isint())
				out.put(val.checkint());
			
			else if (val.islong())
				out.put(val.checklong());
			
			else if (val.isnumber())
				out.put(val.todouble());

			else if (val.isboolean())
				out.put(val.checkboolean());

			else if (val.istable())
				out.put(auto(val.checktable()));
			
			else if (val.isstring())
				out.put(val.checkstring());

		}
		return out;
	}

	public static LuaTable table(JSONObject obj) throws JSONException {
		LuaTable out = new LuaTable();
		
		@SuppressWarnings("unchecked")
		Iterator<String> it = obj.keys();
		if (it.hasNext())
			for (String k = it.next(); k != null; k = it.hasNext() ? it.next() : null) {
				Object val = obj.get(k);

				if (val instanceof JSONArray) {
					out.set(k, table((JSONArray) val));
				} else if (val instanceof JSONObject) {
					out.set(k, table((JSONObject) val));
				} else if (val instanceof String) {
					out.set(k, LuaValue.valueOf((String) val));
				} else if (val instanceof Boolean) {
					out.set(k, LuaValue.valueOf((Boolean) val));
				} else if (val instanceof Integer) {
					out.set(k, LuaValue.valueOf((Integer) val));
				} else if (val instanceof Double) {
					out.set(k, LuaValue.valueOf((Double) val));
				} else if (val instanceof Long) {
					out.set(k, LuaValue.valueOf((Long) val));
				}
			}
		return out;
	}

	public static LuaTable table(JSONArray arr) throws JSONException {
		LuaTable t = new LuaTable();

		for (int i = 0; i < arr.length(); i++) {
			Object val = arr.get(i);
			if (val instanceof JSONArray) {
				t.set(i + 1, table((JSONArray) val));
			} else if (val instanceof JSONObject) {
				t.set(i + 1, table((JSONObject) val));
			} else if (val instanceof String) {
				t.set(i + 1, (String) val);
			} else if (val instanceof Boolean) {
				t.set(i + 1, (Boolean) val);
			} else if (val instanceof Integer) {
				t.set(i + 1, (Integer) val);
			} else if (val instanceof Double) {
				t.set(i + 1, (Double) val);
			} else if (val instanceof Long) {
				t.set(i + 1, (Long) val);
			}
		}

		return t;
	}

}
