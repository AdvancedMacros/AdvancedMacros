package com.theincgi.advancedmacros;

import com.theincgi.advancedmacros.access.IMinecraftClient;
import com.theincgi.advancedmacros.event.EventHandler;
import com.theincgi.advancedmacros.gui.EditorGUI;
import com.theincgi.advancedmacros.gui.Gui;
import com.theincgi.advancedmacros.gui.IBindingsGui;
import com.theincgi.advancedmacros.gui.InputGUI;
import com.theincgi.advancedmacros.gui.MacroMenuGui;
import com.theincgi.advancedmacros.gui.RunningScriptsGui;
import com.theincgi.advancedmacros.gui2.ScriptBrowser2;
import com.theincgi.advancedmacros.hud.hud2D.Hud2D;
import com.theincgi.advancedmacros.hud.hud3D.Hud3D;
import com.theincgi.advancedmacros.lua.DocumentationManager;
import com.theincgi.advancedmacros.lua.LuaDebug;
import com.theincgi.advancedmacros.lua.LuaFunctions;
import com.theincgi.advancedmacros.lua.OpenChangeLog;
import com.theincgi.advancedmacros.lua.functions.*;
import com.theincgi.advancedmacros.lua.functions.entity.GetAABB;
import com.theincgi.advancedmacros.lua.functions.entity.GetEntityData;
import com.theincgi.advancedmacros.lua.functions.entity.GetEntityList;
import com.theincgi.advancedmacros.lua.functions.entity.GetNBT;
import com.theincgi.advancedmacros.lua.functions.entity.HighlightEntity;
import com.theincgi.advancedmacros.lua.functions.midi.MidiLib2;
import com.theincgi.advancedmacros.lua.functions.minecraft.GetChunkUpdates;
import com.theincgi.advancedmacros.lua.functions.minecraft.GetFPS;
import com.theincgi.advancedmacros.lua.functions.minecraft.MinecraftFunctions;
import com.theincgi.advancedmacros.lua.functions.os.ClipBoard;
import com.theincgi.advancedmacros.lua.functions.os.GetOSMilliseconds;
import com.theincgi.advancedmacros.lua.modControl.EditorControls;
import com.theincgi.advancedmacros.lua.scriptGui.ScriptGui;
import com.theincgi.advancedmacros.lua.util.BufferedImageControls;
import com.theincgi.advancedmacros.lua.util.GraphicsContextControls;
import com.theincgi.advancedmacros.lua.util.LuaMutex;
import com.theincgi.advancedmacros.misc.CallableTable;
import com.theincgi.advancedmacros.misc.CustomFontRenderer;
import com.theincgi.advancedmacros.misc.JarLibSearcher;
import com.theincgi.advancedmacros.misc.Settings;
import com.theincgi.advancedmacros.misc.Utils;
import com.theincgi.advancedmacros.publicInterfaces.LuaPlugin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.luaj.vm2_v3_0_1.Globals;
import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaFunction;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaThread;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;
import org.luaj.vm2_v3_0_1.lib.jse.JsePlatform;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Optional;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AdvancedMacros {

    /**
     * advancedMacros
     */
    public static final String MOD_ID = "advancedmacros";

    public static final String VERSION = "11.0.0a";
    public static final String GAME_VERSION = "1.20.4";

    public static final File MACROS_ROOT_FOLDER = getRootFolder();
    public static final File MACROS_FOLDER = new File(MACROS_ROOT_FOLDER, "macros");
    public static final File MACRO_SOUNDS_FOLDER = new File(MACROS_ROOT_FOLDER, "sounds");
    public static final File CUSTOM_DOCS_FOLDER = new File(MACROS_ROOT_FOLDER, "docs");
    public static KeyBinding modKeybind;
    public static IBindingsGui macroMenuGui;
    public static EditorGUI editorGUI;
    public static final LuaTable ADVANCED_MACROS_TABLE = new LuaTable();
    public static ScriptBrowser2 scriptBrowser2;
    public static RunningScriptsGui runningScriptsGui;
    public static Gui lastGui;
    public static Gui prevGui;
    public static InputGUI inputGUI;
    public static Globals globals = JsePlatform.standardGlobals();
    public static final LuaDebug LUA_DEBUG = new LuaDebug();
    public static final CustomFontRenderer CUSTOM_FONT_RENDERER = new CustomFontRenderer();
    private static final DocumentationManager DOCUMENTATION_MANAGER = new DocumentationManager();
    public static Action actions;
    private static Thread minecraftThread;
    private static MinecraftClient mc;
    public static final boolean COLOR_SPACE_IS_255 = false;
    public static LuaValue repl;

    public static final EventHandler EVENT_HANDLER = new EventHandler();

    public AdvancedMacros() {

    }

    public static void init() {
        MACROS_ROOT_FOLDER.mkdirs();
        MACROS_FOLDER.mkdirs();
        MACRO_SOUNDS_FOLDER.mkdirs();
        CUSTOM_DOCS_FOLDER.mkdirs();

        modKeybind = new KeyBinding("Bindings Menu", GLFW.GLFW_KEY_L, "AdvancedMacros");

        try {
            Settings.load();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static void postInit() {
        minecraftThread = ((IMinecraftClient) MinecraftClient.getInstance()).am_getThread();
        globals.setLuaThread(minecraftThread, new LuaThread(globals));
        globals.setLuaThread(Thread.currentThread(), new LuaThread(globals));

        loadFunctions();
        loadLibJars();
        loadScripts();

        //otherCustomFontRenderer = new FontRendererOverride();
        //otherCustomFontRenderer.onResourceManagerReload();
        //Utils.loadTextCodes();
        macroMenuGui = new MacroMenuGui();
        editorGUI = new EditorGUI();
        //scriptBrowser = new ScriptBrowser();
        scriptBrowser2 = new ScriptBrowser2();
        inputGUI = new InputGUI(LUA_DEBUG);
        runningScriptsGui = new RunningScriptsGui(LUA_DEBUG);
        Settings.save(); //changed order to save after loading the themes

        macroMenuGui.updateProfileList();
        Settings.getProfileList();//generate DEFAULT
        macroMenuGui.loadProfile("DEFAULT");
        globals.set("prompt", inputGUI.getPrompt());
        editorGUI.postInit();

        MinecraftClient.getInstance().getSoundManager().registerListener(EVENT_HANDLER.SOUND_LISTENER);

    }

    public static LuaFunctions.Log logFunc;
    public static LuaFunctions.GetMinecraft getMinecraft;
    public static LuaFunctions.Say sayFunc;
    public static LuaFunctions.Sleep sleepFunc;
    public static LuaFunctions.Debug debugFunc;
    public static LuaTable debugTable;
    public static OpenInventory openInventory;

    private static void loadFunctions() {
        globals.load(LUA_DEBUG);
        debugTable = globals.get("debug").checktable();
        globals.set("_MOD_VERSION", VERSION);
        globals.set("__GAME_VERSION", GAME_VERSION);

        //globals.set("_S", new _S());

        globals.set("advancedMacros", ADVANCED_MACROS_TABLE);
        LuaTable editor = new LuaTable();
        ADVANCED_MACROS_TABLE.set("editor", editor);
        ADVANCED_MACROS_TABLE.set("openChangeLog", new OpenChangeLog());
        editor.set("jumpToLine", new EditorControls.JumpToLine());

        globals.set("run", new Call());
        globals.set("runOnMC", new RunOnMC());
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
        //globals.set("advLog", new AdvLog());
        globals.set("say", sayFunc = new LuaFunctions.Say());
        globals.set("toast", new Toast.ToastNotification());
        globals.set("narrate", new Narrate());

        globals.set("sleep", sleepFunc = new LuaFunctions.Sleep());
        globals.set("getMinecraft", getMinecraft = new LuaFunctions.GetMinecraft());
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
        globals.set("getBlockName", new GetBlockName());
        globals.set("getPlayer", new GetPlayer());
        globals.set("playerDetails", GetPlayer.playerFunctions);
        globals.set("getPlayerList", new GetPlayerList()); //everywhere
        globals.set("getLoadedPlayers", new GetLoadedPlayers()); //your loaded chunks
        globals.set("getPlayerPos", new GetPlayerPos());
        globals.set("getPlayerBlockPos", new GetPlayerBlockPos());
        globals.set("getPlayerNBT", new GetNBT.GetPlayerNBT());

//        globals.set("minecraft", new MinecraftFunctions());
        globals.set("getRecipes", new GetRecipe());
        globals.set("getFps", new GetFPS());
        globals.set("getChunkUpdateCount", new GetChunkUpdates());

        globals.set("getEntityList", new GetEntityList());
        globals.set("getEntity", new GetEntityData());
        globals.set("getBossBars", new GetBossBars());

        globals.set("getEntityNBT", new GetNBT.GetEntityNBT());
        globals.set("getBoundingBox", new GetAABB().getFunc());
        globals.set("highlightEntity", new CallableTable(new String[]{"highlightEntity"}, new HighlightEntity()));

        globals.set("getScreen", new GetScreen());

        LuaTable hud2D;
        globals.set("hud2D", hud2D = new Hud2D());
        globals.set("hud3D", new Hud3D());
        hud2D.set("title", new Toast.ToastTitle());
        hud2D.set("actionbar", new Toast.ToastActionBar());

        globals.set("rayTrace", RayTrace.getFunc());

        (actions = new Action()).getKeybindFuncts(globals);
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

        LuaTable hid = new LuaTable();
        hid.set("getState", new HID.GetHIDState());
        hid.set("getTypes", new HID.GetHIDTypes());
        globals.set("HID", hid);

        globals.set("filesystem", new FileSystem());

        LuaTable guiStuff = new LuaTable();
        guiStuff.set("new", new ScriptGui.CreateScriptGui());
        globals.set("gui", guiStuff);

        LuaTable searchers = globals.get("package").get("searchers").checktable();
        searchers.set(searchers.length() + 1, new JarLibSearcher());
        globals.set("getJarLibLoaders", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return JarLibSearcher.loaders;
            }
        });
    }

    private static void loadLibJars() {
        final Class[] params = new Class[0];
        try {
            File libs = new File(MACROS_FOLDER, "libs");
            if (libs.exists() && libs.isDirectory()) {
                for (File f : libs.listFiles()) {
                    try {
                        if (f.getName().endsWith(".jar")) {
                            JarFile jarFile = new JarFile(f);
                            Enumeration<JarEntry> e = jarFile.entries();
                            URL[] urls = {new URL("jar:file:" + f.getPath().replace('\\', '/') + "!/")};
                            URLClassLoader cl = URLClassLoader.newInstance(urls, LuaPlugin.class.getClassLoader());

                            while (e.hasMoreElements()) {
                                JarEntry je = e.nextElement();
                                if (je.isDirectory() || !je.getName().endsWith(".class")) {
                                    continue;
                                }
                                String className = je.getName().substring(0, je.getName().length() - 6);
                                className = className.replace('/', '.');
                                try {
                                    Class<?> c = cl.loadClass(className);
                                    if (c.getName().contains("DL4J4Lua")) //TODO remove test code...
                                    {
                                        System.out.println("");
                                    }

                                    if (LuaPlugin.class.isAssignableFrom(c)) {
                                        if (LuaFunction.class.isAssignableFrom(c)) {
                                            System.out.println("Loaded from jar " + c.getName());
                                            LuaFunction luaFunction = (LuaFunction) c.newInstance();
                                            String name = ((LuaPlugin) luaFunction).getLibraryName();
                                            JarLibSearcher.loaders.set(name, luaFunction);
                                            System.out.println("Loaded LuaPlugin '" + name + "'");
                                        } else {
                                            System.err.println("Skipping LuaPlugin '" + c.getName() + "'. Does not extends LuaFunction");
                                        }
                                    }
                                } catch (Exception | Error e1) {
                                    System.err.println(e1.getClass().getName() + ": " + e1.getMessage());
                                }
                            }
                            jarFile.close();
                            System.out.println("Closing jar...");
                        }

                    } catch (Exception | Error e) {
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadScripts() {

    	String[] scripts = new String[] {
    			"searcher",
    			"settings_fix",
    			"morefunc",
    			"easings",
    			"httpquick",
    			"class",
    			"utils"
		};
		for( String script : scripts ) {
			try {
				Optional<Resource> res = getMinecraft().getResourceManager().getResource(new Identifier(AdvancedMacros.MOD_ID, "scripts/"+script+".lua"));
				if(res.isEmpty()) {
					System.err.println("Couldn't load packaged script '"+script+"'");
					continue;
				}
				InputStream in = res.get().getInputStream();
				globals.load(in, script, "t", globals).call();
				in.close();
			} catch (Throwable e) {
				e.printStackTrace();
			}			
		}
		
		//REPL is different, result saved to variable
		try {
			Optional<Resource> res = getMinecraft().getResourceManager().getResource(new Identifier(AdvancedMacros.MOD_ID, "scripts/repl.lua"));
			if(res.isEmpty()) {
				System.err.println("Couldn't load packaged script 'REPL'");
			}
			InputStream in = res.get().getInputStream();
			repl = globals.load(in, "REPL", "t", globals);
			in.close();
		} catch (Throwable e) {
			e.printStackTrace();
    	}
    }

    public static File[] getScriptList() {
        File[] l = MACROS_FOLDER.listFiles();
        return l == null ? new File[]{} : l;
    }

    public static void stopAll() {
        LUA_DEBUG.stopAll();
    }

    public static DocumentationManager getDocumentationManager() {
        return DOCUMENTATION_MANAGER;
    }

    /**
     * Run any script with a generic 'manual' argument, used in scriptBrowser 2
     */
    public static void runScript(String scriptName) {
        System.out.println("Running script " + scriptName);
        File f = new File(AdvancedMacros.MACROS_FOLDER, scriptName);
        LuaTable args = new LuaTable();
        args.set(1, "manual");
        try {
            FileReader fr = new FileReader(f);
            LuaValue function = AdvancedMacros.globals.load(fr, f.getAbsolutePath());
            LuaDebug.LuaThread t = new LuaDebug.LuaThread(function, args.unpack(), scriptName);
            t.start();
        } catch (FileNotFoundException e) {
            Utils.logError(new LuaError("Could not find script '" + scriptName + "'"));
            AdvancedMacros.logFunc.call(LuaValue.valueOf("&c" + "Could not find script '" + scriptName + "'"));
            e.printStackTrace();
        } catch (Throwable le) {
            Utils.logError(le);
        }
    }

    private static File getRootFolder() {
        File defaultRoot = CommonPlatform.getConfigDirectory().resolve("advancedMacros").toFile();
        File f = defaultRoot.toPath().resolve("advancedMacros.cfg").toFile();
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try (PrintWriter pw = new PrintWriter(f)) {
                pw.write("advancedMacrosRootFolder=" + defaultRoot + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return defaultRoot;
        }
        try (Scanner s = new Scanner(f)) {
            String line = s.nextLine();
            if (line.startsWith("advancedMacrosRootFolder=")) {
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

    public static MinecraftClient getMinecraft() {
        return MinecraftClient.getInstance();
    }

}
