package com.theincgi.advancedMacros.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.google.common.util.concurrent.ListenableFuture;
import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.event.TaskDispatcher;
import com.theincgi.advancedMacros.lua.LuaValTexture;
import com.theincgi.advancedMacros.lua.ProtectedLuaTable;
import com.theincgi.advancedMacros.lua.functions.MinecraftSettings;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class Settings {
	private static final File settingsFile = new File(AdvancedMacros.macrosRootFolder,"settings.txt");
	public static LuaTable settings = new LuaTable();
	private static ProtectedLuaTable textures = new ProtectedLuaTable();

	

	public static void load() throws FileNotFoundException{
		System.out.println("Loading Advanced Macros settings...");
		settings = new LuaTable(); //wipe everything out then read from file
		if(settingsFile.exists()){
			System.out.println("Settings file exists");
			Scanner s = new Scanner(settingsFile);
			s.nextLine(); //read "table 0"
			HashMap<Integer, LuaTable> tbls = new HashMap<>();
			tbls.put(0, settings);
			loadTable(s, settings, tbls);
			//loadDefaults(false); //allows for new items in future versions to be updated auto
		}
		settings.set("textures", textures);
		settings.set("save", new Save());
		settings.set("load", new Load());
		settings.set("minecraft", new MinecraftSettings());
		if(settings.get("chat").isnil()) 
			settings.set("chat", new LuaTable());
		if(settings.get("chat").get("maxLines").isnil())
			settings.set("maxLines", 100);
	}
	//	private static void loadDefaults(boolean force) {
	//		
	//			
	//			LuaTable buttonColors = new LuaTable();
	//			buttonColors.set("frame", new Color(255, 255, 255).toLuaValue());
	//			buttonColors.set("fill", new Color(100, 100, 100).toLuaValue());
	//			buttonColors.set("shade", new Color(100, 100, 100, 255).toLuaValue());
	//			buttonColors.set("text", new Color(255, 255, 255).toLuaValue());
	//			if(force){
	//				Utils.tableFromProp(settings, "colors", new LuaTable()).checktable().set("standardButton", buttonColors);
	//				Utils.tableFromProp(settings, "sounds", new LuaTable()).checktable().set("standardButtonClick", GuiButton.defaultSoundValue);
	//			}else{
	//				Utils.tableFromProp(settings, "colors.standardButton", buttonColors);
	//				Utils.tableFromProp(settings, "sounds.standardButtonClick", LuaValue.valueOf("buttonClick.wav"));
	//			}
	//		
	//		if(force || settings.get("sounds.standardButtonClick").isnil())
	//			settings.set("sounds.standardButtonClick", "/sounds/buttonClick.wav");
	//		
	//
	//	}


	public static void unloadTexture(String file){
		LuaValue v = textures.get(file);
		if(v instanceof LuaValTexture){
			((LuaValTexture) v).getDynamicTexture().deleteGlTexture();
		}
		textures.secSet(LuaValue.valueOf(file), LuaValue.NIL);
	}



	private static transient net.minecraftforge.registries.IForgeRegistry<Block> blah = GameRegistry.findRegistry(Block.class);
	private static transient Collection<Block> blockList = blah.getValues();

	/**nil if error loading, if exists, gets from table, if not will try to load<br>
	 * use "resource:" at the beginning to specify something in the mod<br>
	 * use "web:" prefix to load from a URL [not implemented] */
	public static LuaValue getTextureID(String file){

		//		if(Thread.currentThread()!=minecraftThread) {
		//			final String file2 = file;
		//			ListenableFuture<LuaValue> future = AdvancedMacros.getMinecraft().addScheduledTask(new Callable<LuaValue>() {
		//				
		//				@Override
		//				public LuaValue call() throws Exception {
		//					return getTextureID(file2);
		//				}
		//			});
		//			while(!future.isDone()) {
		//				try {
		//					Thread.sleep(10);
		//				} catch (InterruptedException e) {break;}
		//				
		//			}
		//			if(!future.isCancelled())
		//				try {
		//					return future.get();
		//				} catch (InterruptedException e) {
		//				} catch (ExecutionException e) {
		//				}
		//			return null;
		//		}

		if(settings.get("textures").isnil()){
			settings.set("textures", textures); //keep it available, no excuses
		}

		LuaValue val = LuaValue.NIL;
		if(!textures.get(file).isnil()){
			return textures.get(file);
		}

		if(file.startsWith("web:")){
			//feature not included yet.. might want security check or something, maybe just ends with .png/.jpg?
		}else if(file.startsWith("resource:")){
			file = file.substring("resource:".length());
			//System.out.println("Resource is \""+file+"\"");
			ResourceLocation r = new ResourceLocation(AdvancedMacros.MODID, file);
			val = new LuaValTexture("resource:"+file, r);
		}else if(file.startsWith("block:")){
			file = file.substring("block:".length());

			ResourceLocation r = AtlasTexture.LOCATION_BLOCKS_TEXTURE;
			TextureAtlasSprite sprite = AdvancedMacros.getMinecraft().getTextureMap().getSprite(r);
			LuaValTexture tex;
			val = tex = new LuaValTexture("game:"+file, r);
			tex.setUV(sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV());

		}else{
			val = loadTex(file);
		}
		textures.secSet(LuaValue.valueOf(file), val);
		return val;
	}



	//	private static ResourceLocation tryGetBlockResource(String id) {
	//		Set<Entry<ResourceLocation, Block>> list = ForgeRegistries.BLOCKS.getEntries();
	//		Iterator<Entry<ResourceLocation, Block>> iter = list.iterator();
	//		while(iter.hasNext()) {
	//			Entry e = iter.next();
	//			Block b = (Block)(e.getValue());
	//			b.
	//		}
	//	}
	private static LuaValue loadTex(String file) {
		return loadTex(file, null);
	}
	private static LuaValue loadTex(String file, Thread caller) {
		try {
			if(Thread.currentThread() != AdvancedMacros.getMinecraftThread()) {
				final String sFile = file;
				final Thread callingThread = Thread.currentThread();
				ListenableFuture<LuaValue> future = TaskDispatcher.addTask(new Callable<LuaValue>() {
					@Override
					public LuaValue call() throws Exception {
						return loadTex(sFile, callingThread);
					}
				});
				try {
					while(!future.isDone()) {
						Thread.sleep(1);
					}
					if(future.isCancelled())
						return LuaValue.NIL;
					return future.get();
				} catch (InterruptedException e) {
					System.err.println("Interrupted durring texture grab");
					return LuaValue.NIL;
				} catch (ExecutionException e) {
					e.printStackTrace();
					return LuaValue.NIL;
				}
			}
			DynamicTexture dTex;
			if(caller!=null)
				dTex = new DynamicTexture(NativeImage.read(new FileInputStream(Utils.parseFileLocation(caller, file, 1)))); //ImageIO.read(Utils.parseFileLocation(caller, file, 1)/*new File(file)*/));
			else 
				dTex = new DynamicTexture(NativeImage.read(new FileInputStream(Utils.parseFileLocation(file, 1))));//ImageIO.read(Utils.parseFileLocation(file, 1)/*new File(file)*/));
			return new LuaValTexture(file, dTex);
		} catch (IOException e) {
			return LuaValue.NIL;
		}
	}

	public static ResourceLocation fromDynamic(String name, DynamicTexture t){
		return AdvancedMacros.getMinecraft().getTextureManager().getDynamicTextureLocation(name, t);
	}

	public static class GetSettings extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			return settings;
		}
	}

	private static void loadTable(Scanner s, LuaTable t, HashMap<Integer, LuaTable> tables){
		String regEx = "[^\"\\s]+|\"(\\\\.|[^\\\\\"])*\"";
		while(s.hasNext()){
			String key = s.findWithinHorizon(regEx,0);
			LuaValue luaKey, luaVal = LuaValue.NIL;
			//boolean keyIsString = false;
			if(key.startsWith("\"")){
				key = key.substring(1, key.length()-1).replaceAll("\\\"", "\"");
				luaKey = LuaValue.valueOf(key);
			}else if(key.equals("endTable")){
				break;
			}else{
				if(key.contains(".")){
					luaKey = LuaValue.valueOf(Double.parseDouble(key.trim()));
				}else{
					luaKey = LuaValue.valueOf(Long.parseLong(key.trim()));
				}
			}
			s.next(); //read = that's just for readability
			String val = s.findWithinHorizon(regEx,0);
			if(val.toLowerCase().equals("true") || val.toLowerCase().equals("false")){
				luaVal = LuaValue.valueOf(val.equals("true"));
			}else if(val.startsWith("\"")){
				val = val.substring(1, val.length()-1).replaceAll("\\\"", "\"");
				luaVal = LuaValue.valueOf(val);
			}else if(val.equals("table")){
				LuaTable next = new LuaTable();
				int id = Integer.parseInt(s.nextLine().trim());
				tables.put(id, next);
				loadTable(s, next, tables);
				luaVal = next;
			}else if(val.equals("recursive")){
				int id = Integer.parseInt(s.nextLine().trim());
				luaVal = tables.get(id);
			}else{
				if(val.contains(".")){
					luaVal = LuaValue.valueOf(Double.parseDouble(val.trim()));
				}else{
					luaVal = LuaValue.valueOf(Long.parseLong(val.trim()));
				}
			}

			t.set(luaKey, luaVal);
		}
	}
	public static void save(){
		try {
			FileOutputStream fos = new FileOutputStream(settingsFile);
			PrintWriter pw = new PrintWriter(fos);
			saveTable(pw, settings);
			pw.close();
			System.out.println("AdvancedMacros settings saved!");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**Save a table to file<br>
	 * Allowed types:<br>
	 * Keys:{number, string}<br>
	 * Values:{number, string, boolean, table}<br>
	 * other values will be omitted 
	 * @param pw PrintWriter that will write to file<br>
	 * @param t table to print<br?
	 * @param tblID table id, set this to 0 when calling, this method is recursive<br>
	 * @param tables set this to new LinkedList<LuaTable>()*/
	public static void saveTable(PrintWriter pw, LuaTable t){
		saveTable(pw, t, new IntPointer(), new LinkedList<LuaTable>(), 0);
	}
	private static class IntPointer{
		int i=0;
		public int getI() {
			return i;
		}
		public IntPointer inc(){
			++i;
			return this;
		}
		@Override
		public String toString() {
			return i+"";
		}
	}
	private static void saveTable(PrintWriter pw, LuaTable t, IntPointer tblID, LinkedList<LuaTable> tables, int indent){
		//for(int i = 0; i<tblID;i++){pw.print("  ");}
		if(t.getClass().equals(ProtectedLuaTable.class)){return;}  //do not save protected tables
		tables.add(t);
		//for(int i = 0; i<indent;i++){pw.print("  ");}
		pw.println("table "+tblID);
		for(LuaValue key = t.next(LuaValue.NIL).arg1(); !key.isnil(); key = t.next(key).arg1()){
			String line = "";
			for(int i = 0; i<indent+1;i++){line+="  ";} //indent for readablility in file
			//System.out.println(key + " = " + t.get(key));
			if(key.isint()){
				line+=key.toString()+" = ";
			}else if(key.islong()){
				line+=key.toString()+" = ";
			}else if(key.isstring()){
				line+="\""+ escQuotes(key.toString()) +"\" = ";
			}else if(key.isnumber()){
				line+=key.toString()+" = ";
			}

			LuaValue val = t.get(key);
			if(val.isboolean()){ 
				//System.out.println("BOOLEAN SAVE"); 
				pw.println(line + val.toString());
			}else if(val.isint()){
				pw.println(line + val.toString());
			}else if(val.islong()){
				pw.println(line + val.toString());
			}else if(val.isnumber()){
				pw.println(line + val.toString());
			}else if(val.isstring()){
				pw.println(line + "\""+escQuotes(val.toString())+"\"");
			}else if(val.istable()){
				if(tables.indexOf(val.checktable())>=0){
					pw.println(line + "recursive "+tables.indexOf(val.checktable()));
				}else{
					//System.out.println(tables);
					if(val.getClass().equals(ProtectedLuaTable.class)){continue;}
					pw.print(line); line = "";
					saveTable(pw, val.checktable(), tblID.inc(), tables, indent+1);

				}
			}
		}
		for(int i = 0; i<indent;i++){pw.print("  ");}
		pw.println("endTable");
	}
	private static String escQuotes(String s){
		return s.replaceAll("\"", "\\\" ");
	}
	public static void setProp(String propName, LuaValue value) {
		//example propName = "colors.standardButton" value = {r,g,b} needs colors table to set value
		LuaTable t = Utils.tableFromProp(settings, propName.substring(0, propName.lastIndexOf('.')), new LuaTable()).checktable();
		t.set(propName.substring(propName.lastIndexOf('.')+1), value);
	}


	public static LinkedList<String> getProfileList() {
		try {
			load();
		} catch (FileNotFoundException e) {
			//eh, its ok...ish
			e.printStackTrace();
		} //force a refresh from file
		if(settings.get("profiles").isnil()){
			settings.set("profiles", new LuaTable());
		}
		LuaTable profiles = settings.get("profiles").checktable();
		if(!profiles.get("DEFAULT").istable()){
			profiles.set("DEFAULT", new LuaTable());
		}

		LinkedList<String> names = new LinkedList<>();
		for(LuaValue key = profiles.next(LuaValue.NIL).arg1(); !key.isnil(); key = profiles.next(key).arg1()){
			if(key.isstring() && profiles.get(key).istable()){
				names.add(key.tojstring());
			}
		}
		System.out.println("Profiles: "+names);
		return names;
	}


	public static LuaTable getProfileTable() {
		if(settings.get("profiles").isnil()){
			settings.set("profiles", new LuaTable());
		}
		return settings.get("profiles").checktable();
	}

	public static class Save extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			save();
			if(AdvancedMacros.macroMenuGui!=null)
				AdvancedMacros.macroMenuGui.reloadCurrentProfile();
			return LuaValue.NIL;
		}
	}
	public static class Load extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			try {
				Settings.load();
				AdvancedMacros.macroMenuGui.reloadCurrentProfile();
			} catch (FileNotFoundException e) {
				return LuaValue.FALSE;
			}
			return LuaValue.TRUE;
		}
	}

}