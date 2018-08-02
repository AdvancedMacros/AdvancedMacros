package com.theincgi.advancedMacros;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.luaj.vm2_v3_0_1.Globals;
import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaFunction;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;
import org.luaj.vm2_v3_0_1.lib.jse.JsePlatform;
import org.lwjgl.input.Keyboard;

import com.theincgi.advancedMacros.gui.EditorGUI;
import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.gui.InputGUI;
import com.theincgi.advancedMacros.gui.MacroMenuGui;
import com.theincgi.advancedMacros.gui.RunningScriptsGui;
import com.theincgi.advancedMacros.gui2.ScriptBrowser2;
import com.theincgi.advancedMacros.hud.hud2D.Hud2D;
import com.theincgi.advancedMacros.hud.hud3D.Hud3D;
import com.theincgi.advancedMacros.lua.DocumentationManager;
import com.theincgi.advancedMacros.lua.LuaDebug;
import com.theincgi.advancedMacros.lua.LuaFunctions;
import com.theincgi.advancedMacros.lua.functions.Action;
import com.theincgi.advancedMacros.lua.functions.AdvLog;
import com.theincgi.advancedMacros.lua.functions.Call;
import com.theincgi.advancedMacros.lua.functions.FileSystem;
import com.theincgi.advancedMacros.lua.functions.GetBiome;
import com.theincgi.advancedMacros.lua.functions.GetBlock;
import com.theincgi.advancedMacros.lua.functions.GetEntityData;
import com.theincgi.advancedMacros.lua.functions.GetEntityList;
import com.theincgi.advancedMacros.lua.functions.GetInventory;
import com.theincgi.advancedMacros.lua.functions.GetLoadedPlayers;
import com.theincgi.advancedMacros.lua.functions.GetOSMilliseconds;
import com.theincgi.advancedMacros.lua.functions.GetPlayer;
import com.theincgi.advancedMacros.lua.functions.GetPlayerBlockPos;
import com.theincgi.advancedMacros.lua.functions.GetPlayerList;
import com.theincgi.advancedMacros.lua.functions.GetPlayerPos;
import com.theincgi.advancedMacros.lua.functions.GetProfile;
import com.theincgi.advancedMacros.lua.functions.GetScreen;
import com.theincgi.advancedMacros.lua.functions.GetTextureList;
import com.theincgi.advancedMacros.lua.functions.GetWorld;
import com.theincgi.advancedMacros.lua.functions.HTTP;
import com.theincgi.advancedMacros.lua.functions.IsKeyHeld;
import com.theincgi.advancedMacros.lua.functions.LightAt;
import com.theincgi.advancedMacros.lua.functions.MathPlus;
import com.theincgi.advancedMacros.lua.functions.OpenInventory;
import com.theincgi.advancedMacros.lua.functions.PCall;
import com.theincgi.advancedMacros.lua.functions.PlaySound;
import com.theincgi.advancedMacros.lua.functions.RunThread;
import com.theincgi.advancedMacros.lua.functions.ScriptGui;
import com.theincgi.advancedMacros.lua.functions.SetProfile;
import com.theincgi.advancedMacros.lua.functions.SkinCustomizer;
import com.theincgi.advancedMacros.lua.functions.StopAllScripts;
import com.theincgi.advancedMacros.lua.functions.Toast;
import com.theincgi.advancedMacros.lua.functions.midi.MidiLib2;
import com.theincgi.advancedMacros.lua.util.BufferedImageControls;
import com.theincgi.advancedMacros.misc.CustomFontRenderer;
import com.theincgi.advancedMacros.misc.FontRendererOverride;
import com.theincgi.advancedMacros.misc.JarLibSearcher;
import com.theincgi.advancedMacros.misc.Settings;
import com.theincgi.advancedMacros.publicInterfaces.LuaPlugin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@Mod(modid = AdvancedMacros.MODID, version = AdvancedMacros.VERSION)
public class AdvancedMacros {
	/**advancedMacros*/
	public static final String MODID = "advancedmacros";
	public static final String VERSION = "4.1.1a"; //${version} ?? //previously .1
	public static final File macrosRootFolder = getRootFolder();
	public static final File macrosFolder = new File(macrosRootFolder, "macros");
	public static final File macroSoundsFolder = new File(macrosRootFolder, "sounds");
	public static final File customDocsFolder = new File(macrosRootFolder, "docs");
	public static KeyBinding modKeybind;
	public static MacroMenuGui macroMenuGui;
	public static EditorGUI editorGUI;
	//public static ScriptBrowser scriptBrowser;
	public static ScriptBrowser2 scriptBrowser2;
	public static RunningScriptsGui runningScriptsGui;
	public static Gui lastGui;
	public static Gui prevGui;
	public static InputGUI inputGUI;
	public static Globals globals = JsePlatform.standardGlobals();
	private static LuaDebug debug = new LuaDebug();
	public static ForgeEventHandler forgeEventHandler;
	public static final CustomFontRenderer customFontRenderer = new CustomFontRenderer();
	public static FontRendererOverride otherCustomFontRenderer;
	private static final DocumentationManager documentationManager = new DocumentationManager();
	private JarLibSearcher jarLibSearcher;

	//protected static ArrayList customEventNames;

	//	public static FloatBuffer modelView3d = BufferUtils.createFloatBuffer(16);
	//	public static FloatBuffer projView3d = BufferUtils.createFloatBuffer(16);
	//	public static FloatBuffer modelView2d = BufferUtils.createFloatBuffer(16);
	//	public static FloatBuffer projView2d = BufferUtils.createFloatBuffer(16);

	@EventHandler @SideOnly(Side.CLIENT)//skipped the proxy system, this is only client side
	public void init(FMLInitializationEvent event){
		try {
			if(event.getSide().isServer()){return;}
			macrosRootFolder.mkdirs();
			macrosFolder.mkdirs();
			macroSoundsFolder.mkdirs();
			customDocsFolder.mkdirs();
			modKeybind = new KeyBinding("Bindings Menu", Keyboard.KEY_L, "AdvancedMacros");
			MinecraftForge.EVENT_BUS.register(forgeEventHandler = new ForgeEventHandler());
			
			ClientRegistry.registerKeyBinding(modKeybind);
			try {
				Settings.load();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			otherCustomFontRenderer = new FontRendererOverride();
			otherCustomFontRenderer.onResourceManagerReload(null);
			//Utils.loadTextCodes();
			macroMenuGui = new MacroMenuGui();
			editorGUI = new EditorGUI();
			//scriptBrowser = new ScriptBrowser();
			scriptBrowser2 = new ScriptBrowser2();
			inputGUI = new InputGUI(debug);
			runningScriptsGui = new RunningScriptsGui(debug);
			Settings.save(); //changed order
			Settings.getProfileList();//generate DEFAULT 
			
			loadFunctions();
			loadLibJars();
			macroMenuGui.loadProfile("DEFAULT");
			loadScripts();
			//		HudText text = new HudText(true);
			//		text.setDrawType(DrawType.XRAY);
			//		text.setPos(0, 5, 0);
			//		forgeEventHandler.addWorldHudItem(text);
		}catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@SideOnly(Side.CLIENT)
	public void postInit(FMLPostInitializationEvent event) {
		editorGUI.postInit();

	}


	public static LuaFunctions.Log logFunc;
	public static LuaFunctions.Say sayFunc;
	public static LuaFunctions.Sleep sleepFunc;
	public static LuaFunctions.Debug debugFunc;


	private void loadFunctions() {
		globals.load(debug);

		globals.set("run", new Call());
		globals.set("pRun", new PCall());
		globals.set("runThread", new RunThread());
		globals.set("getProfile", new GetProfile());
		globals.set("setProfile", new SetProfile());
		globals.set("stopAllScripts", new StopAllScripts());

		try {
			globals.set("listTextures", new GetTextureList());
		} catch (NoSuchFieldException | IllegalAccessException | RuntimeException e) {
			e.printStackTrace();
		}

		globals.set("log", logFunc = new LuaFunctions.Log());
		globals.set("advLog", new AdvLog());
		globals.set("say", sayFunc = new LuaFunctions.Say());
		globals.set("toast", new Toast());
		globals.set("sleep", sleepFunc = new LuaFunctions.Sleep());
		globals.set("print", new LuaFunctions.Debug());
		globals.set("getSettings", new Settings.GetSettings());

		globals.get("os").set("millis", new GetOSMilliseconds());
		globals.get("os").set("exit", LuaValue.NIL);
		
		LuaTable imgTools = new LuaTable();
		imgTools.set("new", new BufferedImageControls.CreateImg());
		imgTools.set("load", new BufferedImageControls.LoadImg());
		imgTools.set("getFormats", new BufferedImageControls.GetFormats());
		globals.set("image", imgTools);
		//math tweaks
		LuaTable math = globals.get("math").checktable();
		math.set("ln", math.get("log")); //because log is some how base e instead of 10
		math.set("log", new MathPlus.Log());
		math.set("e", MathPlus.const_e);
		
		//		//5.3 string tweaks //migrated to org.luaj.vm2_v3_0_1.lib.StringLib
		//		{
		//			LuaTable string = globals.get("string").checktable();
		//			string.set("pack", new StringSerialization.StringPack());
		//			string.set("unpack", new StringSerialization.StringUnpack());
		//		}

		globals.set("httpRequest", new HTTP());
		globals.set("getWorld", new GetWorld());
		globals.set("getBlock", new GetBlock());
		globals.set("getPlayer", new GetPlayer());
		globals.set("getPlayerList", new GetPlayerList()); //everywhere
		globals.set("getLoadedPlayers", new GetLoadedPlayers()); //your loaded chunks
		globals.set("getPlayerPos", new GetPlayerPos());
		globals.set("getPlayerBlockPos", new GetPlayerBlockPos());

		globals.set("getEntityList", new GetEntityList());
		globals.set("getEntity", new GetEntityData());
		globals.set("getScreen", new GetScreen());
		//		globals.set("addHoloBlock", new AddHoloBlock());
		//		globals.set("addHoloText", new AddHoloText());
		//		globals.set("clearWorldHud", new ClearWorldHud());

		globals.set("hud2D", new Hud2D());
		globals.set("hud3D", new Hud3D());

		new Action().getKeybindFuncts(globals);
		globals.set("getInventory", new GetInventory());
		globals.set("openInventory", new OpenInventory());

		globals.set("getLight", new LightAt.AllLight());
		globals.set("getBlockLight", new LightAt.BlockLight());
		globals.set("getSkyLight", new LightAt.SkyLight());
		globals.set("getBiome", new GetBiome());

		globals.set("playSound", new PlaySound.FromFile());
		globals.set("midi", new MidiLib2());
		globals.set("customizeSkin", new SkinCustomizer());
		//globals.set("getVillages", new GetVillages());
		globals.set("isKeyDown", new IsKeyHeld());
		globals.set("getHeldKeys", new AdvancedMacros().forgeEventHandler.new GetHeldKeys());
		globals.set("filesystem", new FileSystem());
		globals.set("prompt", inputGUI.getPrompt());

		LuaTable guiStuff = new LuaTable();
		guiStuff.set("newGui", new ScriptGui.CreateScriptGui());
		globals.set("gui", guiStuff);
		
		LuaTable searchers = globals.get("package").get("searchers").checktable();
		searchers.set(searchers.length() + 1, jarLibSearcher = new JarLibSearcher());
		globals.set("getJarLibLoaders", new ZeroArgFunction() {public LuaValue call() {return jarLibSearcher.loaders;}});
		//System.out.println("FUNCTIONS:\n"+ColorTextArea.getVariableList(globals.checktable(), LuaValue.TFUNCTION, true, "", new HashMap<LuaTable, Boolean>()));
	}

	private void loadLibJars() {
		final Class[] params = new Class[0];
		try {
			File libs = new File(macrosFolder, "libs");
			if(libs.exists() && libs.isDirectory()) {
				for (File f : libs.listFiles()) {
					try {
						if(f.getName().endsWith(".jar")) {
							JarFile jarFile = new JarFile(f);
							Enumeration<JarEntry> e = jarFile.entries();
							URL[] urls = { new URL("jar:file:" + f.getPath().replace('\\', '/') +"!/" )};
							URLClassLoader cl = URLClassLoader.newInstance(urls, LuaPlugin.class.getClassLoader());
							
							while(e.hasMoreElements()) {
								JarEntry je = e.nextElement();
								if(je.isDirectory() || !je.getName().endsWith(".class"))
									continue;
								String className = je.getName().substring(0, je.getName().length()-6);
								className = className.replace('/', '.');
								try {
									Class c = cl.loadClass(className);
									if(c.getName().contains("DL4J4Lua"))
										System.out.println("");
									
									if(LuaPlugin.class.isAssignableFrom(c)) {
										if(LuaFunction.class.isAssignableFrom(c)) {
											System.out.println("Loaded from jar "+c.getName());
											LuaFunction luaFunction = (LuaFunction) c.newInstance();
											String name = ((LuaPlugin)luaFunction).getLibraryName();
											jarLibSearcher.loaders.set(name, luaFunction);
											System.out.println("Loaded LuaPlugin '"+name+"'");
										}else {
											System.err.println("Skipping LuaPlugin '"+c.getName()+"'. Does not extends LuaFunction");
										}
									}
								} catch (Exception | Error e1) {
									System.err.println(e1.getClass().getName() + ": " +e1.getMessage());
								}
							}
							jarFile.close();
							System.out.println("Closing jar...");
						}

					} catch (Exception | Error e) {
						System.err.println(e.getClass().getName() + ": " +e.getMessage());
					}
				}

			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void loadScripts() {
		try {
			InputStream in = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(AdvancedMacros.MODID, "scripts/searcher.lua")).getInputStream();
			globals.load(in, "searcher", "t", globals).call();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			InputStream in = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(AdvancedMacros.MODID, "scripts/settings_fix.lua")).getInputStream();
			globals.load(in, "settingsFix", "t", globals).call();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static File[] getScriptList(){
		File[] l = macrosFolder.listFiles();
		return l==null?new File[]{} : l;
	}


	public static void stopAll() {
		debug.stopAll();
	}
	public static DocumentationManager getDocumentationManager() {
		return documentationManager;
	}


	/**Run any script with a generic 'manual' argument, used in scriptBrowser 2*/
	public static void runScript(String scriptName) {
		File f = new File(AdvancedMacros.macrosFolder, scriptName);
		LuaTable args = new LuaTable();
		args.set(1, "manual");
		try {
			FileReader fr = new FileReader(f);
			LuaValue function = AdvancedMacros.globals.load(fr, scriptName);
			LuaDebug.LuaThread t = new LuaDebug.LuaThread(function, args, scriptName);
			t.start();
		} catch (FileNotFoundException e) {
			AdvancedMacros.logFunc.call(LuaValue.valueOf("&c"+"Could not find script '"+scriptName+"'"));
			e.printStackTrace();
		}catch (LuaError le){
			//TODO allow for option to not log error to chat
			le.printStackTrace();
			AdvancedMacros.logFunc.call(LuaValue.valueOf("&c"+le.toString()));
		}catch (Throwable e) {
			AdvancedMacros.logFunc.call(LuaValue.valueOf("&c"+e.toString()));
		}
	}
	private static File getRootFolder() {
		File defaultRoot = new File(Minecraft.getMinecraft().mcDataDir,"mods/advancedMacros");
		File f = new File(Minecraft.getMinecraft().mcDataDir,"config/advancedMacros.cfg");
		if(!f.exists()) {
			try (PrintWriter pw = new PrintWriter(f)){
				pw.write("advancedMacrosRootFolder=" +defaultRoot.toString()+"\n");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			return defaultRoot;
		}
		try(Scanner s = new Scanner(f)){
			String line = s.nextLine();
			if(line.startsWith("advancedMacrosRootFolder=")) {
				String value = line.substring(line.indexOf('=') + 1);
				return new File(value);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		return defaultRoot;
	}
}
