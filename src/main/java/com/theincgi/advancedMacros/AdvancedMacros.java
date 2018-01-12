package com.theincgi.advancedMacros;

import java.io.File;
import java.io.FileNotFoundException;

import org.luaj.vm2_v3_0_1.Globals;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.jse.JsePlatform;
import org.lwjgl.input.Keyboard;

import com.theincgi.advancedMacros.ForgeEventHandler.EventName;
import com.theincgi.advancedMacros.gui.EditorGUI;
import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.gui.InputGUI;
import com.theincgi.advancedMacros.gui.MacroMenuGui;
import com.theincgi.advancedMacros.gui.RunningScriptsGui;
import com.theincgi.advancedMacros.gui.ScriptBrowser;
import com.theincgi.advancedMacros.hud.hud3D.HoloBlock.DrawType;
import com.theincgi.advancedMacros.hud.hud3D.HudText;
import com.theincgi.advancedMacros.lua.DocumentationManager;
import com.theincgi.advancedMacros.lua.LuaDebug;
import com.theincgi.advancedMacros.lua.LuaFunctions;
import com.theincgi.advancedMacros.lua.functions.Action;
import com.theincgi.advancedMacros.lua.functions.AddHoloBlock;
import com.theincgi.advancedMacros.lua.functions.AddHoloText;
import com.theincgi.advancedMacros.lua.functions.AdvLog;
import com.theincgi.advancedMacros.lua.functions.Call;
import com.theincgi.advancedMacros.lua.functions.ClearWorldHud;
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
import com.theincgi.advancedMacros.lua.functions.GetTextureList;
import com.theincgi.advancedMacros.lua.functions.GetWorld;
import com.theincgi.advancedMacros.lua.functions.IsKeyHeld;
import com.theincgi.advancedMacros.lua.functions.LightAt;
import com.theincgi.advancedMacros.lua.functions.MathPlus;
import com.theincgi.advancedMacros.lua.functions.OpenInventory;
import com.theincgi.advancedMacros.lua.functions.PCall;
import com.theincgi.advancedMacros.lua.functions.PlaySound;
import com.theincgi.advancedMacros.lua.functions.RunThread;
import com.theincgi.advancedMacros.lua.functions.SetProfile;
import com.theincgi.advancedMacros.lua.functions.SkinCustomizer;
import com.theincgi.advancedMacros.lua.functions.StopAllScripts;
import com.theincgi.advancedMacros.misc.CustomFontRenderer;
import com.theincgi.advancedMacros.misc.Settings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
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
	public static final String VERSION = "3.8.2"; //${version} ??
	public static final File macrosRootFolder = new File(Minecraft.getMinecraft().mcDataDir,"mods/advancedMacros");
	public static final File macrosFolder = new File(macrosRootFolder, "macros");
	public static final File macroSoundsFolder = new File(macrosRootFolder, "sounds");
	public static final File customDocsFolder = new File(macrosRootFolder, "docs");
	public static KeyBinding modKeybind;
	public static MacroMenuGui macroMenuGui;
	public static EditorGUI editorGUI;
	public static ScriptBrowser scriptBrowser;
	public static RunningScriptsGui runningScriptsGui;
	public static Gui lastGui;
	public static Gui prevGui;
	public static InputGUI inputGUI;
	public static Globals globals = JsePlatform.standardGlobals();
	private static LuaDebug debug = new LuaDebug();
	public static ForgeEventHandler forgeEventHandler;
	public static final CustomFontRenderer customFontRenderer = new CustomFontRenderer();
	private static final DocumentationManager documentationManager = new DocumentationManager();
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
		//Utils.loadTextCodes();
		macroMenuGui = new MacroMenuGui();
		editorGUI = new EditorGUI();
		scriptBrowser = new ScriptBrowser();
		inputGUI = new InputGUI(debug);
		runningScriptsGui = new RunningScriptsGui(debug);
		Settings.getProfileList();//generate DEFAULT 
		Settings.save();
			loadFunctions();
		macroMenuGui.loadProfile("DEFAULT");
		
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
		forgeEventHandler.fireEvent(EventName.Startup,ForgeEventHandler.createEvent(EventName.Startup));
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
		globals.set("sleep", sleepFunc = new LuaFunctions.Sleep());
		//globals.set("debug", debugFunc = new LuaFunctions.Debug());
		globals.set("print", LuaValue.NIL);
		globals.set("getSettings", new Settings.GetSettings());
		
		globals.get("os").set("millis", new GetOSMilliseconds());
		globals.get("os").set("exit", LuaValue.NIL);
		
		//math tweaks
		LuaTable math = globals.get("math").checktable();
		math.set("ln", math.get("log")); //because log is some how base e instead of 10
		math.set("log", new MathPlus.Log());
		math.set("e", MathPlus.const_e);
		
		
		globals.set("getWorld", new GetWorld());
		globals.set("getBlock", new GetBlock());
		globals.set("getPlayer", new GetPlayer());
		globals.set("getPlayerList", new GetPlayerList()); //everywhere
		globals.set("getLoadedPlayers", new GetLoadedPlayers()); //your loaded chunks
		globals.set("getPlayerPos", new GetPlayerPos());
		globals.set("getPlayerBlockPos", new GetPlayerBlockPos());
		
		globals.set("getEntityList", new GetEntityList());
		globals.set("getEntity", new GetEntityData());
		
		globals.set("addHoloBlock", new AddHoloBlock());
		globals.set("addHoloText", new AddHoloText());
		globals.set("clearWorldHud", new ClearWorldHud());
		
		new Action().getKeybindFuncts(globals);
		globals.set("getInventory", new GetInventory());
		globals.set("openInventory", new OpenInventory());
		
		globals.set("getLight", new LightAt.AllLight());
		globals.set("getBlockLight", new LightAt.BlockLight());
		globals.set("getSkyLight", new LightAt.SkyLight());
		globals.set("getBiome", new GetBiome());
		
		globals.set("playSound", new PlaySound.FromFile());
		globals.set("customizeSkin", new SkinCustomizer());
		//globals.set("getVillages", new GetVillages());
		globals.set("isKeyDown", new IsKeyHeld());
		globals.set("filesystem", new FileSystem());
		globals.set("prompt", inputGUI.getPrompt());
		//System.out.println("FUNCTIONS:\n"+ColorTextArea.getVariableList(globals.checktable(), LuaValue.TFUNCTION, true, "", new HashMap<LuaTable, Boolean>()));
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


	
}
