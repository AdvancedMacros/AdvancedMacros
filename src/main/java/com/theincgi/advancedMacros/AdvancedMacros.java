package com.theincgi.advancedMacros;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.PrintWriter;
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
import org.luaj.vm2_v3_0_1.LuaThread;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;
import org.luaj.vm2_v3_0_1.lib.jse.JsePlatform;
import org.lwjgl.input.Keyboard;

import com.theincgi.advancedMacros.event.ForgeEventHandler;
import com.theincgi.advancedMacros.gui.EditorGUI;
import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.gui.IBindingsGui;
import com.theincgi.advancedMacros.gui.InputGUI;
import com.theincgi.advancedMacros.gui.MacroMenuGui;
import com.theincgi.advancedMacros.gui.RunningScriptsGui;
import com.theincgi.advancedMacros.gui2.ScriptBrowser2;
import com.theincgi.advancedMacros.hud.hud2D.Hud2D;
import com.theincgi.advancedMacros.hud.hud3D.Hud3D;
import com.theincgi.advancedMacros.lua.DocumentationManager;
import com.theincgi.advancedMacros.lua.LuaDebug;
import com.theincgi.advancedMacros.lua.LuaFunctions;
import com.theincgi.advancedMacros.lua.OpenChangeLog;
import com.theincgi.advancedMacros.lua.functions.Action;
import com.theincgi.advancedMacros.lua.functions.AdvLog;
import com.theincgi.advancedMacros.lua.functions.Call;
import com.theincgi.advancedMacros.lua.functions.Connect;
import com.theincgi.advancedMacros.lua.functions.Disconnect;
import com.theincgi.advancedMacros.lua.functions.FileSystem;
import com.theincgi.advancedMacros.lua.functions.GetBiome;
import com.theincgi.advancedMacros.lua.functions.GetBlock;
import com.theincgi.advancedMacros.lua.functions.GetBlockList;
import com.theincgi.advancedMacros.lua.functions.GetInventory;
import com.theincgi.advancedMacros.lua.functions.GetLoadedPlayers;
import com.theincgi.advancedMacros.lua.functions.GetPlayer;
import com.theincgi.advancedMacros.lua.functions.GetPlayerBlockPos;
import com.theincgi.advancedMacros.lua.functions.GetPlayerList;
import com.theincgi.advancedMacros.lua.functions.GetPlayerPos;
import com.theincgi.advancedMacros.lua.functions.GetProfile;
import com.theincgi.advancedMacros.lua.functions.GetRecipe;
import com.theincgi.advancedMacros.lua.functions.GetScreen;
import com.theincgi.advancedMacros.lua.functions.GetSound;
import com.theincgi.advancedMacros.lua.functions.GetTextureList;
import com.theincgi.advancedMacros.lua.functions.GetWorld;
import com.theincgi.advancedMacros.lua.functions.HTTP;
import com.theincgi.advancedMacros.lua.functions.IsKeyHeld;
import com.theincgi.advancedMacros.lua.functions.LightAt;
import com.theincgi.advancedMacros.lua.functions.MathPlus;
import com.theincgi.advancedMacros.lua.functions.NewThread;
import com.theincgi.advancedMacros.lua.functions.OpenInventory;
import com.theincgi.advancedMacros.lua.functions.PCall;
import com.theincgi.advancedMacros.lua.functions.PlaySound;
import com.theincgi.advancedMacros.lua.functions.RayTrace;
import com.theincgi.advancedMacros.lua.functions.RunThread;
import com.theincgi.advancedMacros.lua.functions.SetProfile;
import com.theincgi.advancedMacros.lua.functions.SkinCustomizer;
import com.theincgi.advancedMacros.lua.functions.StopAllScripts;
import com.theincgi.advancedMacros.lua.functions.StringTrim;
import com.theincgi.advancedMacros.lua.functions.Toast;
import com.theincgi.advancedMacros.lua.functions.entity.GetAABB;
import com.theincgi.advancedMacros.lua.functions.entity.GetEntityData;
import com.theincgi.advancedMacros.lua.functions.entity.GetEntityList;
import com.theincgi.advancedMacros.lua.functions.entity.HighlightEntity;
import com.theincgi.advancedMacros.lua.functions.midi.MidiLib2;
import com.theincgi.advancedMacros.lua.functions.minecraft.GetChunkUpdates;
import com.theincgi.advancedMacros.lua.functions.minecraft.GetFPS;
import com.theincgi.advancedMacros.lua.functions.os.ClipBoard;
import com.theincgi.advancedMacros.lua.functions.os.GetOSMilliseconds;
import com.theincgi.advancedMacros.lua.modControl.EditorControls;
import com.theincgi.advancedMacros.lua.scriptGui.ScriptGui;
import com.theincgi.advancedMacros.lua.util.BufferedImageControls;
import com.theincgi.advancedMacros.lua.util.GraphicsContextControls;
import com.theincgi.advancedMacros.lua.util.LuaMutex;
import com.theincgi.advancedMacros.misc.CallableTable;
import com.theincgi.advancedMacros.misc.CustomFontRenderer;
import com.theincgi.advancedMacros.misc.FontRendererOverride;
import com.theincgi.advancedMacros.misc.JarLibSearcher;
import com.theincgi.advancedMacros.misc.Settings;
import com.theincgi.advancedMacros.misc.Utils;
import com.theincgi.advancedMacros.publicInterfaces.LuaPlugin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@Mod(modid = AdvancedMacros.MODID, version = AdvancedMacros.VERSION)
public class AdvancedMacros {
	/**advancedMacros*/
	public static final String MODID = "advancedmacros";

	public static final String VERSION = "7.7.7"; //${version} ??

	public static final File macrosRootFolder = getRootFolder();
	public static final File macrosFolder = new File(macrosRootFolder, "macros");
	public static final File macroSoundsFolder = new File(macrosRootFolder, "sounds");
	public static final File customDocsFolder = new File(macrosRootFolder, "docs");
	public static KeyBinding modKeybind;
	public static IBindingsGui macroMenuGui;
	public static EditorGUI editorGUI;
	public static final LuaTable advancedMacrosTable = new LuaTable();
	public static ScriptBrowser2 scriptBrowser2;
	public static RunningScriptsGui runningScriptsGui;
	public static Gui lastGui;
	public static Gui prevGui;
	public static InputGUI inputGUI;
	public static Globals globals = JsePlatform.standardGlobals();
	public static final LuaDebug debug = new LuaDebug();
	public static ForgeEventHandler forgeEventHandler;
	public static final CustomFontRenderer customFontRenderer = new CustomFontRenderer();
	public static FontRendererOverride otherCustomFontRenderer;
	private static final DocumentationManager documentationManager = new DocumentationManager();
	private JarLibSearcher jarLibSearcher;
	private static Thread minecraftThread;
	private static Minecraft mc;
	private static ModContainer advMacrosModContainer;
	public static final boolean COLOR_SPACE_IS_255 = false;


	@EventHandler @SideOnly(Side.CLIENT)//skipped the proxy system, this is only client side
	public void init(FMLInitializationEvent event){
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER) return; //lik srsly
		try {
			if(event.getSide().isServer()){return;}
			minecraftThread = Thread.currentThread();
			globals.setLuaThread(minecraftThread, new LuaThread(globals));
			macrosRootFolder.mkdirs();
			macrosFolder.mkdirs();
			macroSoundsFolder.mkdirs();
			customDocsFolder.mkdirs();
			modKeybind = new KeyBinding("Bindings Menu", Keyboard.KEY_L, "AdvancedMacros");
			advMacrosModContainer = Loader.instance().activeModContainer();
			MinecraftForge.EVENT_BUS.register(forgeEventHandler = new ForgeEventHandler());
			
//			Test code!
//			MinecraftForge.EVENT_BUS.register(new Object() {
//				@SubscribeEvent @SideOnly(Side.CLIENT)
//				public void onChat(final ClientChatReceivedEvent sEvent){
//					logFunc.call("The secondary event handler was called!");
//					sEvent.setCanceled(true);
//				}
//			});
			
			getMinecraft().getSoundHandler().addListener(forgeEventHandler.SOUND_LISTENER);
			ClientRegistry.registerKeyBinding(modKeybind);
			try {
				Settings.load();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			
			loadFunctions();
			loadLibJars();
			
			loadScripts();
			
			
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
			macroMenuGui.updateProfileList();
			Settings.getProfileList();//generate DEFAULT 
			macroMenuGui.loadProfile("DEFAULT");
			
			globals.set("prompt", inputGUI.getPrompt());
		}catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@SideOnly(Side.CLIENT)
	public void postInit(FMLPostInitializationEvent event) {
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER) return; //lik srsly
		editorGUI.postInit();

	}


	public static LuaFunctions.Log logFunc;
	public static LuaFunctions.Say sayFunc;
	public static LuaFunctions.Sleep sleepFunc;
	public static LuaFunctions.Debug debugFunc;
	public static LuaTable debugTable;
	public static OpenInventory openInventory;


	private void loadFunctions() {
		globals.load(debug);
		debugTable = globals.get("debug").checktable();
		globals.set("_MOD_VERSION", VERSION);
		globals.set("__GAME_VERSION", Minecraft.getMinecraft().getVersion());
		
		globals.set("advancedMacros", advancedMacrosTable);
		LuaTable editor = new LuaTable();
		advancedMacrosTable.set("editor", editor);
		advancedMacrosTable.set("openChangeLog", new OpenChangeLog());
		editor.set("jumpToLine", new EditorControls.JumpToLine());
		
		globals.set("run", new Call());
		globals.set("pRun", new PCall());
		globals.set("runThread", new RunThread());
		LuaTable thread = new LuaTable();
			thread.set("new", new NewThread());
			thread.set("current", new LuaDebug.GetCurrent());
			thread.set("listRunning", new LuaDebug.GetRunningScripts());
		globals.set("thread", thread);
		
		globals.set("getProfile", new GetProfile());
		globals.set("setProfile", new SetProfile());
		globals.set("stopAllScripts", new StopAllScripts());
		

		try {
			globals.set("listTextures", new GetTextureList());
		} catch (NoSuchFieldException | IllegalAccessException | RuntimeException e) {
			e.printStackTrace();
		}
		globals.set("getBlockList", new GetBlockList());
		globals.set("log", logFunc = new LuaFunctions.Log());
		globals.set("advLog", new AdvLog());
		globals.set("say", sayFunc = new LuaFunctions.Say());
		globals.set("toast", new Toast.ToastNotification());
		
		globals.set("sleep", sleepFunc = new LuaFunctions.Sleep());
		globals.set("print", new LuaFunctions.Debug());
		globals.set("getSettings", new Settings.GetSettings());
		globals.set("newMutex", new LuaMutex());
		
		globals.get("os").set("millis", new GetOSMilliseconds());
		globals.get("os").set("exit", LuaValue.NIL);
		globals.get("os").set("getClipboard", new ClipBoard.GetClipboard());
		globals.get("os").set("setClipboard", new ClipBoard.SetClipboard());
		globals.get("string").set("trim", new StringTrim());
		
		LuaTable imgTools = new LuaTable();
		imgTools.set("new", new BufferedImageControls.CreateImg());
		imgTools.set("load", new BufferedImageControls.LoadImg());
		imgTools.set("getFormats", new BufferedImageControls.GetFormats());
		imgTools.set("getFonts", new BufferedImageControls.GetFonts());
		imgTools.set("measureString", new GraphicsContextControls.MeasureString());
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

		globals.set("connect", new Connect());
		globals.set("disconnect", new Disconnect());
		globals.set("httpRequest", new HTTP());
		globals.set("getWorld", new GetWorld());
		globals.set("getBlock", new GetBlock());
		globals.set("getPlayer", new GetPlayer());
		globals.set("getPlayerList", new GetPlayerList()); //everywhere
		globals.set("getLoadedPlayers", new GetLoadedPlayers()); //your loaded chunks
		globals.set("getPlayerPos", new GetPlayerPos());
		globals.set("getPlayerBlockPos", new GetPlayerBlockPos());

		//globals.set("minecraft", new MinecraftFunctions());
		globals.set("getRecipes", new GetRecipe());
		globals.set("getFps", new GetFPS());
		globals.set("getChunkUpdateCount", new GetChunkUpdates());
		
		globals.set("getEntityList", new GetEntityList());
		globals.set("getEntity", new GetEntityData());
		globals.set("getBoundingBox", new GetAABB().getFunc()); 
		globals.set("highlightEntity", new CallableTable(new String[] {"highlightEntity"}, new HighlightEntity()));
		
		globals.set("getScreen", new GetScreen());
		
		LuaTable hud2D;
		globals.set("hud2D", hud2D = new Hud2D());
		globals.set("hud3D",         new Hud3D());
		hud2D.set("title", new Toast.ToastTitle());
		hud2D.set("actionbar", new Toast.ToastActionBar());
		
		
		globals.set("rayTrace", RayTrace.getFunc());

		new Action().getKeybindFuncts(globals);
		globals.set("getInventory", new GetInventory());
		globals.set("openInventory", openInventory = new OpenInventory());

		globals.set("getLight", new LightAt.AllLight());
		globals.set("getBlockLight", new LightAt.BlockLight());
		globals.set("getSkyLight", new LightAt.SkyLight());
		globals.set("getBiome", new GetBiome());

		globals.set("playSound", new PlaySound.FromFile());
		globals.set("getSound", new GetSound());
		globals.set("midi", new MidiLib2());
		globals.set("customizeSkin", new SkinCustomizer());
		
		globals.set("isKeyDown", new IsKeyHeld());
		globals.set("getHeldKeys", new AdvancedMacros().forgeEventHandler.new GetHeldKeys());
		globals.set("filesystem", new FileSystem());


		LuaTable guiStuff = new LuaTable();
		guiStuff.set("new", new ScriptGui.CreateScriptGui());
		globals.set("gui", guiStuff);
		
		LuaTable searchers = globals.get("package").get("searchers").checktable();
		searchers.set(searchers.length() + 1, jarLibSearcher = new JarLibSearcher());
		globals.set("getJarLibLoaders", new ZeroArgFunction() {public LuaValue call() {return jarLibSearcher.loaders;}});
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
			InputStream in = AdvancedMacros.getMinecraft().getResourceManager().getResource(new ResourceLocation(AdvancedMacros.MODID, "scripts/searcher.lua")).getInputStream();
			globals.load(in, "searcher", "t", globals).call();
			in.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			InputStream in = AdvancedMacros.getMinecraft().getResourceManager().getResource(new ResourceLocation(AdvancedMacros.MODID, "scripts/settings_fix.lua")).getInputStream();
			globals.load(in, "settingsFix", "t", globals).call();
			in.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			InputStream in = AdvancedMacros.getMinecraft().getResourceManager().getResource(new ResourceLocation(AdvancedMacros.MODID, "scripts/morefunc.lua")).getInputStream();
			globals.load(in, "moreFunctions", "t", globals).call();
			in.close();
		} catch (Throwable e) {
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
			LuaValue function = AdvancedMacros.globals.load(fr, f.getAbsolutePath());
			LuaDebug.LuaThread t = new LuaDebug.LuaThread(function, args.unpack(), scriptName);
			t.start();
		} catch (FileNotFoundException e) {
			Utils.logError(new LuaError("Could not find script '"+scriptName+"'"));
			AdvancedMacros.logFunc.call(LuaValue.valueOf("&c"+"Could not find script '"+scriptName+"'"));
			e.printStackTrace();
		}catch (LuaError le){
			Utils.logError(le);
		}catch (Throwable e) {
			Utils.logError(e);
		}
	}
	private static File getRootFolder() {
		File defaultRoot = new File(AdvancedMacros.getMinecraft().gameDir,"mods/advancedMacros");
		File f = new File(AdvancedMacros.getMinecraft().gameDir,"config/advancedMacros.cfg");
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
	public static Thread getMinecraftThread() {
		return minecraftThread;
	}

	public static boolean isServerSide() {
		return (FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER); //lik srsly;
	}
	public static Minecraft getMinecraft() {
		if (mc == null) mc = Minecraft.getMinecraft();
		return mc;
	}

	public static ModContainer getModContainer() {
		return advMacrosModContainer;
	}
}
