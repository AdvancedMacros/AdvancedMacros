package com.theincgi.advancedMacros.misc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.luaj.vm2_v3_0_1.LuaFunction;
import org.luaj.vm2_v3_0_1.LuaString;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.lua.DocumentationManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.util.ResourceLocation;

public class CallableTable extends LuaTable{

	String[] docName;
	LuaFunction function;
	private static String selectedLanguageCode = null;
	private static JsonObject json;
	//LuaTable meta;
	public CallableTable(String[] docName, LuaFunction function) {
		this.docName = docName;
		this.function = function;
		this.set("__luaFunction", TRUE);
		//meta = new LuaTable();
		//this.setmetatable(meta);
		if (docName == null) {
			return;
		}
		//meta.set("__call", function);
		this.set("definition", getJsonDefinition());
		this.set("tooltip",    getJsonTooltip());
		this.set("luaDoc",     getJsonLuaDoc());
		this.set("argTypes",   getJsonArgTypes());
		
	}

	@Override
	public Varargs invoke(Varargs args) {
		return function.invoke(args);
	}

	@Override
	public String tojstring() {
		return (tostring().isnil())? "tableFunction( ? )" : tostring().checkjstring();
	}
	@Override
	public LuaValue tostring() {
		return (this.get("definition").isnil())? super.tostring() : this.get("definition");
	}
	public LuaValue getJsonTooltip() {
		JsonElement jObj = getObjectFromJson(docName);
		if(jObj.isJsonObject() && jObj.getAsJsonObject().has("tooltip")) {
			jObj = jObj.getAsJsonObject().get("tooltip");
			if(jObj.isJsonArray()) {
				JsonArray jArr = jObj.getAsJsonArray();
				LuaTable out = new LuaTable();
				for(int i = 0; i< jArr.size(); i++)
					out.set(i+1, jArr.get(i).getAsString());
				return out;
			}else if(jObj.isJsonPrimitive()) {
				return LuaString.valueOf( jObj.getAsString() );
			}else
				return LuaString.valueOf("err: invalid input");
		}else
			return NIL;
	}
	public LuaValue getJsonLuaDoc() {
		JsonElement jObj = getObjectFromJson(docName);
		if(jObj.isJsonObject() && jObj.getAsJsonObject().has("luaDoc")) {
			jObj = jObj.getAsJsonObject().get("luaDoc").getAsJsonObject();
			if(jObj.isJsonArray()) {
				JsonArray jArr = jObj.getAsJsonArray();
				LuaTable out = new LuaTable();
				for(int i = 0; i< jArr.size(); i++)
					out.set(i+1, jArr.get(i).getAsString());
				return out;
			}else if(jObj.isJsonPrimitive()) {
				return LuaString.valueOf( jObj.getAsString() );
			}else
				return LuaString.valueOf("err: invalid input");
		}else
			return NIL;
	}
	public LuaValue getJsonArgTypes() {
		JsonElement jObj = getObjectFromJson(docName);
		if(jObj.isJsonObject() && jObj.getAsJsonObject().has("types")) {
			jObj = jObj.getAsJsonObject().get("types");
			if(jObj.isJsonArray()) {
				JsonArray jArr = jObj.getAsJsonArray();
				LuaTable out = new LuaTable();
				for(int i = 0; i< jArr.size(); i++) {
					JsonElement je = jArr.get(i);
					if(je.isJsonArray()) { //multiple types
						JsonArray sub = je.getAsJsonArray();
						LuaTable table = new LuaTable();
						for(int j = 0; j<sub.size(); j++) {
							table.set(j+1, sub.get(i).getAsString());
						}
						out.set(i+1, table);
					}else{
						out.set(i+1, jArr.get(i).getAsString());
					}
				}
				return out;
			}else
				return LuaString.valueOf("err: invalid input");
		}else
			return NIL;
	}
	public LuaValue getJsonDefinition() {
		JsonObject jObj = getObjectFromJson(docName);
		if(jObj.has("definition")) {
			JsonElement je = jObj.get("definition");
			if(je.isJsonPrimitive()) {
				return LuaString.valueOf( je.getAsString() );
			}else
				return LuaString.valueOf("err: invalid input");
		}else
			return NIL;
	}
	
	
	public static JsonObject getObjectFromJson(String[] path) {
		JsonObject temp = getDocJson();
		for(String s : path)
			if(temp.has(s))
				temp = temp.get(s).getAsJsonObject();
			else
				return null;
		return temp;
	}
	
	public static String getCurrentLanguage() {
		return Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
	}
	public static JsonObject getDocJson() {
		return getDocJson( getCurrentLanguage() );
	}
	/**defaults to en_us if it cant load any other files, null otherwise*/
	public static JsonObject getDocJson( String languageCode ) { //TODO default to English on fail
		if(languageCode.equals(selectedLanguageCode) && json != null)
			return json;
		
		try(
				InputStream in = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(AdvancedMacros.MODID, "newdocs/"+languageCode+".lang")).getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));){
			Gson g = new Gson();
			JsonElement je = g.fromJson(reader, JsonElement.class);
			return json = je.getAsJsonObject();
		}catch(Exception e) {
			if(!languageCode.equals( "en_us" ))
				return json = getDocJson("en_us");
			else {
				e.printStackTrace();
				return json = null;
			}
		}finally {
			selectedLanguageCode = languageCode;
		}
	}
}
