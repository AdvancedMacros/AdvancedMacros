package com.theincgi.advancedMacros.event;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.objectweb.asm.Type;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.CullFace;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.gui.MacroMenuGui;
import com.theincgi.advancedMacros.gui.elements.ColorTextArea;
import com.theincgi.advancedMacros.hud.hud2D.Hud2DItem;
import com.theincgi.advancedMacros.hud.hud3D.WorldHudItem;
import com.theincgi.advancedMacros.lua.LuaDebug.JavaThread;
import com.theincgi.advancedMacros.lua.LuaDebug.LuaThread;
import com.theincgi.advancedMacros.lua.LuaDebug.OnScriptFinish;
import com.theincgi.advancedMacros.lua.OpenChangeLog;
import com.theincgi.advancedMacros.lua.functions.GuiControls;
import com.theincgi.advancedMacros.misc.HIDUtils;
import com.theincgi.advancedMacros.misc.HIDUtils.Keyboard;
import com.theincgi.advancedMacros.misc.HIDUtils.Mouse;
import com.theincgi.advancedMacros.misc.Pair;
import com.theincgi.advancedMacros.misc.Settings;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ISoundEventListener;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.SaveToFile;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.eventbus.ASMEventHandler;
import net.minecraftforge.eventbus.ListenerList;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventListener;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.network.ConnectionType;
import net.minecraftforge.fml.network.NetworkHooks;

public class ForgeEventHandler {
	int lastAir, lastHealth,
	lastItemDurablity, lastHotbar, lastHunger;
	boolean playerWasNull = true, lastSleepingState = false;
	/**Used for glowing, sneaking breaks it*/
	private boolean wasSneaking = false;
	//added lastSwingProgress even though it exists in the player because it would fire multiple times
	float lastSaturation, lastSwingProgress;
	ItemStack lastHeldItem;
	//int lastDimension;
	int[] lastArmourDurability = new int[4];
	int lastXP, lastXPLevel;
	DimensionType lastDim;
	boolean wasRaining, wasThundering;
	/**Keeping this syncronized!*/
	private LinkedList<WorldHudItem> worldHudItems = new LinkedList<>();
	private LinkedList<Hud2DItem> hud2DItems = new LinkedList<>();
	private int sTick = 0; private Object sTickSync = new Object();
	private ConcurrentHashMap<UUID, String> lastPlayerList = new ConcurrentHashMap<>();
	private ConcurrentHashMap<UUID, String> nowPlayerList = new ConcurrentHashMap<>();
	public WeakHashMap<Entity, RenderFlags> entityRenderFlags = new WeakHashMap<>();
	private boolean wasOnFire = false;
	private HashMap<Integer, Integer> repeatingKeys = new HashMap<>();
	private Object tickLock = new Object();

	//private Queue<List<ItemStack>> receivedInventories = new LinkedList<>();
	public ForgeEventHandler() {
		heldMouseButtons = new ArrayList<>(Mouse.getButtonCount());
		for(int i = 0; i<Mouse.getButtonCount(); i++)
			heldMouseButtons.add(false);
	}



	/**These enums contain lowercase letters because they're names get used directly*/
	public static enum EventName{
		Chat,					//COMPLETE
		ChatFilter,				//COMPLETE
		ChatSendFilter,
		Title,
		Actionbar,
		//LoggedIn,
		//LoggedOut,
		JoinWorld,				//COMPLETE
		LeaveWorld,
		Respawn,				//COMPLETE
		Death,					//COMPLETE
		HealthChanged,			//COMPLETE
		HungerChanged,			//COMPLETE
		SaturationChanged,		//COMPLETE
		AirChanged,				//COMPLETE
		DimensionChanged,       //COMPLETE
		//DamageTaken, //healthChanged covers this
		ItemPickup,
		ItemCrafted,
		//ItemSmelted,this is server side
		HotbarChanged, //slot changed					//COMPLETE
		PotionStatus, 
		Weather,				//COMPLETE
		PlayerIgnited, //ouch
		//FOVChanged, //does this need to be an event?
		GUIOpened,
		GUIClosed,
		//AnvilUpdate, //server event
		//PotionBrewed, //server event
		ItemTossed,
		WorldSaved,
		ArrowFired,				//COMPLETE
		AttackEntity,
		EntityInteract,
		BlockInteract,
		//showTooltip, Usage?
		ContainerOpen,
		UseBed,
		WakeUp,
		UseItem, //start, stop and dinish
		BreakItem,
		ArmourDurability,
		ItemDurability,
		PlayerJoin,
		PlayerLeave,
		XP,						//COMPLETE
		//PlayerDropItems, //server event
		//EntityDropItems, //server event
		AttackReady,
		ProfileLoaded,
		Sound,
		Startup, //right after everything is loaded, custom libraries should go here!
		Anything //also includes key event or mouse event,
		;//	TEST;
	}

	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event){
		if(AdvancedMacros.modKeybind.isKeyDown()){ 
			if(ColorTextArea.isCTRLDown()){
				if(ColorTextArea.isShiftDown()) {
					AdvancedMacros.stopAll();
				}else {
					AdvancedMacros.getMinecraft().displayGuiScreen(AdvancedMacros.runningScriptsGui);
				}
			}else if(ColorTextArea.isShiftDown()){
				showMenu(AdvancedMacros.scriptBrowser2, AdvancedMacros.macroMenuGui.getGui());
			}else if(HIDUtils.Keyboard.isAlt()) {
				LuaThread thread = new LuaThread(AdvancedMacros.repl, "REPL");
				thread.start();
			}else{
				if(AdvancedMacros.lastGui!=null){
					TaskDispatcher.delayTask(()->{
						AdvancedMacros.lastGui.showGui();
					}, 85);
				}else{
					MacroMenuGui.showMenu();
				}
			}
		}else{
			int eventKey = event.getKey(); //TESTME
			Screen s = AdvancedMacros.getMinecraft().currentScreen;
			if(event.getAction() == GLFW.GLFW_PRESS) {
				repeatingKeys.put(event.getScanCode(), 0);
			}
			if(event.getAction() == GLFW.GLFW_REPEAT && s == null) return;
			if(s!=null) {
				if(s instanceof Gui) {
					Gui g = (Gui)s;
					int n;
					repeatingKeys.put(event.getScanCode(), n = (repeatingKeys.getOrDefault(event.getScanCode(), 0)+1));
					g.onKeyRepeated(g, event.getKey(), event.getScanCode(), event.getModifiers(), n);
				}
				return;
			}

			//Keyboard.onKey(eventKey, event.getAction());

			LuaTable eventDat = new LuaTable();
			eventDat.set(1, "key");
			eventDat.set(2, Keyboard.nameOf(eventKey));
			eventDat.set(3, LuaValue.valueOf(event.getAction()==GLFW.GLFW_PRESS?"down":"up"));
			eventDat.set(4, LuaValue.valueOf(eventKey));
			AdvancedMacros.macroMenuGui.fireEvent(true, Keyboard.nameOf(eventKey), eventDat.unpack(), event.getAction()==GLFW.GLFW_PRESS, null);
		}
	}

	private ArrayList<Boolean> heldMouseButtons;
	public void onMouseClick(int mButton, boolean state) {
		//System.out.println("MOUSE FIRED");
		String buttonName;
		switch (mButton) {
		case 0:
			buttonName = "LMB";
			break;
		case 1:
			buttonName = "RMB";
			break;
		case 2:
			buttonName = "MMB";
			break;
		default:
			buttonName = "MOUSE:"+mButton;
		}
		LuaTable eDat = new LuaTable();
		eDat.set(1, "mouse");
		eDat.set(2, buttonName);
		eDat.set(3, state?"down":"up");
		AdvancedMacros.macroMenuGui.fireEvent(true, buttonName, eDat.unpack(), state, null);
	}

	@SubscribeEvent 
	public void onPlayerTick(TickEvent.PlayerTickEvent event){
		if(event.phase.equals(TickEvent.Phase.START)) return; //only do on the second half of tick after all stuff happens



		for(int i = 0; i<Mouse.getButtonCount(); i++) {
			boolean b = Mouse.isDown(i);
			if(b!=heldMouseButtons.get(i)) {
				onMouseClick(i, b);
				heldMouseButtons.set(i, b);
			}
		}
		synchronized (sTickSync) {
			sTick++;
			synchronized (tickLock) {
				tickLock.notifyAll();
			}
		}
		if(look!=null){
			look.look();
		}
		LinkedList<net.minecraft.client.util.InputMappings.Input> toRemove = new LinkedList<>();
		for(net.minecraft.client.util.InputMappings.Input i : keyBindReleaseMap.keySet()) { 
			HeldKeybinds hk = keyBindReleaseMap.get(i);
			if(hk.releaseTime<System.currentTimeMillis()) {
				KeyBinding.setKeyBindState(i, false);
				hk.done=true; //is this even needed anymore?
				toRemove.add(i);
			}
		}
		while(!toRemove.isEmpty()) {
			keyBindReleaseMap.remove(toRemove.pop());
		}

		PlayerEntity player = AdvancedMacros.getMinecraft().player;
		if(player==null){playerWasNull = true; return;}
		if(playerWasNull){
			resetLastStats();
			playerWasNull=false;
		}

		if(!player.dimension .equals(lastDim)) {
			if(lastDim!=null) {
				LuaTable e = createEvent(EventName.DimensionChanged);
				e.set(3, Utils.toTable(player.dimension));
				e.set(4, Utils.toTable(lastDim));
				fireEvent(EventName.DimensionChanged, e);
			}
			lastDim = player.dimension;
		}
		if(player.isSleeping() != lastSleepingState) {
			if(player.isSleeping())
				fireEvent(EventName.UseBed, createEvent(EventName.UseBed));
			else {
				fireEvent(EventName.WakeUp, createEvent(EventName.WakeUp));
			}
			lastSleepingState = player.isSleeping();
		}

		//AirChanged
		int currentAir = player.getAir()/3;
		if(currentAir!=lastAir){
			LuaTable e = createEvent(EventName.AirChanged);
			e.set(3, LuaValue.valueOf(currentAir));
			e.set(4, LuaValue.valueOf(currentAir-lastAir));
			fireEvent(EventName.AirChanged, e);
			lastAir = currentAir;
			//System.out.println("FIRED air");
		}

		//ArmorChanged
		NonNullList<ItemStack> armor = (NonNullList<ItemStack>) player.getArmorInventoryList();
		boolean armorChanged = false;
		int[] armorDurability = new int[4];
		for (int i = 0; i < armorDurability.length; i++) {
			int temp = armor.get(i).getMaxDamage() - armor.get(i).getDamage();
			if(temp!=lastArmourDurability[i]){armorChanged = true;}
			armorDurability[i] = temp;
		}
		if(armorChanged){
			LuaTable e = createEvent(EventName.ArmourDurability);
			LuaTable current = new LuaTable();
			LuaTable change = new LuaTable();
			e.set(3, current);
			e.set(4, change);
			for(int i = 0; i<armorDurability.length; i++){
				current.set(i+1, LuaValue.valueOf(armorDurability[i]));
				change.set(i+1, LuaValue.valueOf(armorDurability[i]-lastArmourDurability[i]));
				lastArmourDurability[i] = armorDurability[i];
			}
			fireEvent(EventName.ArmourDurability, e);
		}

		int potionUpdateFrequency;
		try{
			potionUpdateFrequency = Utils.tableFromProp(Settings.settings, "events.potionStatusFrequency", LuaValue.valueOf(20)).checkint();
		}catch (Exception e) {
			Settings.settings.get("events").set("potionStatusFrequency", LuaValue.valueOf(20));
			potionUpdateFrequency = 20;
		}
		for (EffectInstance e : player.getActivePotionEffects()) {
			LuaTable evnt = createEvent(EventName.PotionStatus);
			int dur = e.getDuration()-1;
			int ddur = (dur) / potionUpdateFrequency;

			if(dur % potionUpdateFrequency == 0) {
				evnt.set(3, Utils.effectToTable(e));
				fireEvent(EventName.PotionStatus, evnt);
			}

		}

		if(wasOnFire!=player.isBurning()) {
			wasOnFire = player.isBurning();
			LuaTable e = createEvent(EventName.PlayerIgnited);
			e.set(3, LuaValue.valueOf( player.isBurning() ));
			fireEvent(EventName.PlayerIgnited, e); //no pun intended
		}

		//health
		int health = (int)(player.getHealth());
		if (lastHealth!=health){
			if(lastHealth==0 && health>=1){
				fireEvent(EventName.Respawn, createEvent(EventName.Respawn));
			}
			LuaTable e = createEvent(EventName.HealthChanged);
			e.set(3, LuaValue.valueOf(health));
			e.set(4, LuaValue.valueOf(health-lastHealth));
			if(player.getLastDamageSource()!=null)
				e.set(5, LuaValue.valueOf(player.getLastDamageSource().damageType));
			fireEvent(EventName.HealthChanged, e);

			if(health==0){
				fireEvent(EventName.Death, createEvent(EventName.Death));
			}

			lastHealth = health;
		}
		//hotbar
		int hotbar = player.inventory.currentItem;
		if(lastHotbar!=hotbar){
			LuaTable e = createEvent(EventName.HotbarChanged);
			e.set(3, LuaValue.valueOf(hotbar+1));
			fireEvent(EventName.HotbarChanged, e);
			lastHotbar = hotbar;
			resetItemDurablitity();
		}
		//hunger
		//player.getFoodStats()
		int hunger = player.getFoodStats().getFoodLevel();
		if(lastHunger!=hunger){
			LuaTable e = createEvent(EventName.HungerChanged);
			e.set(3, LuaValue.valueOf(hunger));
			e.set(4, LuaValue.valueOf(hunger-lastHunger));
			lastHunger = hunger;
			fireEvent(EventName.HungerChanged, e);
		}
		float saturation = player.getFoodStats().getSaturationLevel();
		if(lastSaturation!=saturation){
			LuaTable e = createEvent(EventName.SaturationChanged);
			e.set(3, LuaValue.valueOf(saturation));
			e.set(4, LuaValue.valueOf(saturation-lastSaturation));
			fireEvent(EventName.SaturationChanged, e);
			lastSaturation = saturation;
		}
		if(lastSwingProgress != 0 && player.swingProgress == 0 && !player.isSwingInProgress) {
			fireEvent(EventName.AttackReady, createEvent(EventName.AttackReady));
		}
		lastSwingProgress = player.swingProgress;
		boolean rain = AdvancedMacros.getMinecraft().world.isRaining();
		boolean thunder = AdvancedMacros.getMinecraft().world.isThundering();
		if(rain!=wasRaining || wasThundering!=thunder){
			wasRaining = rain;
			wasThundering = thunder;
			LuaTable e = createEvent(EventName.Weather);
			String stat;
			if(rain && thunder){
				stat = "thunder";
			}else if(rain && !thunder){
				stat = "rain";
			}else if(!rain && thunder){
				stat = "only thunder";
			}else{
				stat = "clear";
			}

			e.set(3,stat);
			fireEvent(EventName.Weather, e);
		}
		if(lastHeldItem==null || lastHeldItem.isEmpty()){ //skip on pickup or create or w/e
			resetItemDurablitity();
			lastHeldItem=player.getHeldItemMainhand();
		}
		int currentDura = player.getHeldItemMainhand().getMaxDamage()-player.getHeldItemMainhand().getDamage();
		if(currentDura!=lastItemDurablity && !player.getHeldItemMainhand().isEmpty()){
			LuaTable e = createEvent(EventName.ItemDurability);
			e.set(3, Utils.itemStackToLuatable(player.getHeldItemMainhand()));
			e.set(4, LuaValue.valueOf(currentDura - lastItemDurablity));
			fireEvent(EventName.ItemDurability, e);
			resetItemDurablitity();
		}

		int thisXP = player.experienceTotal;
		int thisXPLevel = player.experienceLevel;
		if(thisXP!=lastXP || thisXPLevel!=this.lastXPLevel){
			LuaTable e = createEvent(EventName.XP);
			e.set(3, LuaValue.valueOf(thisXP));
			e.set(4, LuaValue.valueOf(thisXPLevel));
			e.set(5, LuaValue.valueOf(thisXP-lastXP));
			e.set(6, LuaValue.valueOf(thisXPLevel-lastXPLevel));
			fireEvent(EventName.XP, e);
			lastXP = thisXP;
			lastXPLevel = thisXPLevel;
		}

		if(lastPlayerList==null) {
			lastPlayerList = new ConcurrentHashMap<>();
			populatePlayerList(lastPlayerList);
		}
		nowPlayerList.clear();
		populatePlayerList(nowPlayerList);
		//System.out.println(nowPlayerList);
		for(UUID uuid: nowPlayerList.keySet()) {
			if(!lastPlayerList.containsKey(uuid)) {
				//player joined
				String val = nowPlayerList.get(uuid);
				if(val==null)continue;
				LuaTable e = createEvent(EventName.PlayerJoin);
				e.set(3, val);
				fireEvent(EventName.PlayerJoin, e);
				lastPlayerList.put(uuid, val);
			}
		}
		for(UUID uuid: lastPlayerList.keySet()) {
			//	System.out.println("Checking: "+s);
			if(!nowPlayerList.containsKey(uuid)) {
				//left world
				//System.out.println("left");
				LuaTable e = createEvent(EventName.PlayerLeave);
				e.set(3, lastPlayerList.get(uuid));
				fireEvent(EventName.PlayerLeave, e);
				lastPlayerList.remove(uuid);
			}//else System.out.println("Stayed");
		}
		checkTitle();
	}


	private Field titlesTimer, titleDisplayTime, titleFadeOutTime, titleFadeInTime, title, subtitle,
	actionbarTimer, actionbarText, isColorized;
	public void checkTitle() {
		try {
			Minecraft mc = AdvancedMacros.getMinecraft();
			if(titlesTimer==null) { //special thanks to "MCP Mapping Viewer" by bspkrs 
				titlesTimer 		= ObfuscationReflectionHelper.findField(IngameGui.class, "field_175195_w"); //checked for 1.14.3
				titleDisplayTime 	= ObfuscationReflectionHelper.findField(IngameGui.class, "field_175192_A");
				titleFadeInTime 	= ObfuscationReflectionHelper.findField(IngameGui.class, "field_175199_z");
				titleFadeOutTime 	= ObfuscationReflectionHelper.findField(IngameGui.class, "field_175193_B");
				title 				= ObfuscationReflectionHelper.findField(IngameGui.class, "field_175201_x");
				subtitle 			= ObfuscationReflectionHelper.findField(IngameGui.class, "field_175200_y");
				actionbarTimer 		= ObfuscationReflectionHelper.findField(IngameGui.class, "field_73845_h");
				actionbarText 		= ObfuscationReflectionHelper.findField(IngameGui.class, "field_73838_g");
				isColorized 		= ObfuscationReflectionHelper.findField(IngameGui.class, "field_73844_j");
			}
			IngameGui gui = mc.ingameGUI;
			{
				int disp = titleDisplayTime.getInt(gui);
				int fadeIn = titleFadeInTime.getInt(gui);
				int fadeOut = titleFadeOutTime.getInt(gui);
				int timer = titlesTimer.getInt(gui);
				if(timer == (fadeIn + fadeOut + disp)-1) {
					//AdvancedMacros.logFunc.call("&d&BDEBUG:&7 A new title has been displayed!");
					String titleText, subtitleText;
					titleText = Utils.fromMinecraftColorCodes((String)title.get(gui));
					subtitleText = Utils.fromMinecraftColorCodes((String)subtitle.get(gui) );

					if(titleText.endsWith("&f")) titleText = titleText.substring(0, titleText.length()-2);
					if(subtitleText.endsWith("&f")) subtitleText = subtitleText.substring(0, subtitleText.length()-2);

					LuaTable event = createEvent(EventName.Title);
					event.set(3, LuaValue.valueOf( titleText ));
					event.set(4, LuaValue.valueOf( subtitleText ));
					event.set(5, disp);
					event.set(6, fadeIn);
					event.set(7, fadeOut);
					fireEvent(EventName.Title, event);
				}
			}
			{
				int timer = actionbarTimer.getInt(gui);
				if(timer == 59) { //hardcoded value from the class is 60, we see it one tick later
					//AdvancedMacros.logFunc.call("&d&BDEBUG:&7 A new actionbar has been displayed!");
					LuaTable event = createEvent(EventName.Actionbar);
					String text = Utils.fromMinecraftColorCodes((String)actionbarText.get(gui));
					if(text.endsWith("&f")) text = text.substring(0, text.length()-2);
					event.set(3, LuaValue.valueOf( text ));
					event.set(4, isColorized.getBoolean(gui));
					fireEvent(EventName.Actionbar, event);
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

	}

	@SubscribeEvent 
	public void onArrowFired(ArrowLooseEvent event){//CONFIRMED MP

		LuaTable e = createEvent(EventName.ArrowFired);
		e.set(3, Utils.itemStackToLuatable(event.getBow()));
		e.set(4, LuaValue.valueOf(event.getCharge()));
		e.set(5, LuaValue.valueOf(event.hasAmmo()));
		fireEvent(EventName.ArrowFired, e);
	}
	@SubscribeEvent 
	public void onAttackEntity(AttackEntityEvent event) {

		LuaTable e = createEvent(EventName.AttackEntity);
		e.set(3, Utils.entityToTable(event.getTarget()));
		fireEvent(EventName.AttackEntity, e);
	}
	//not in multi :c
//	@SubscribeEvent
//	public void onDeath(LivingDeathEvent event) {
//		PlayerEntity player = AdvancedMacros.getMinecraft().player;
//		if(event.getEntityLiving().equals(player)) {
//			LuaTable table = createEvent(EventName.Death);
//			table.set(3, event.getSource().damageType);
//			fireEvent(EventName.Death, table);
//		}
//	}
	
	@SubscribeEvent 
	public void onEntityInteract(EntityInteract event) {  

		LuaTable e = createEvent(EventName.EntityInteract);
		e.set(3, Utils.entityToTable(event.getTarget()));
		e.set(4, Utils.itemStackToLuatable(event.getItemStack()));
		e.set(5, event.getHand().equals(Hand.MAIN_HAND)?"main hand":"off hand");
		if(event.getFace()!=null)
			e.set(6, LuaValue.valueOf(event.getFace().getName()));
		fireEvent(EventName.EntityInteract, e);
	}
	@SubscribeEvent 
	public void onBlockInteract(PlayerInteractEvent event) { 

		if(event.getFace()==null) return;
		LuaTable e = createEvent(EventName.BlockInteract);
		e.set(3, Utils.blockPosToTable(event.getPos()));
		e.set(4, Utils.itemStackToLuatable(event.getItemStack()));
		e.set(5, event.getHand().equals(Hand.MAIN_HAND)?"main hand":"off hand");
		if(event.getFace()!=null)
			e.set(6, LuaValue.valueOf(event.getFace().getName()));
		fireEvent(EventName.BlockInteract, e);
	}

	@SubscribeEvent 
	public void onItemPickup(PlayerEvent.ItemPickupEvent event) { //DEAD //FIXME

		LuaTable e = createEvent(EventName.ItemPickup);
		e.set(3, Utils.itemStackToLuatable(event.getStack()));
		fireEvent(EventName.ItemPickup, e);
	}
	@SubscribeEvent 
	public void onDimChange(PlayerEvent.PlayerChangedDimensionEvent event) { //DEAD //FIXME

		LuaTable e = createEvent(EventName.DimensionChanged);
		e.set(3, Utils.toTable(event.getTo()));
		e.set(4, Utils.toTable(event.getFrom()));
		fireEvent(EventName.DimensionChanged, e);
	}
	@SubscribeEvent 
	public void onCraft(PlayerEvent.ItemCraftedEvent event) { //CONFIRMED MP

		//System.out.println(event.getPhase());
		LuaTable e = createEvent(EventName.ItemCrafted);
		e.set(3, Utils.itemStackToLuatable(event.getCrafting()));
		LuaTable matrix = new LuaTable();
		int size = (int) Math.sqrt(event.getInventory().getSizeInventory()); //craftMatrix, but named poorly imo (src says return craftMatrix)
		for(int x = 1; x <= size; x++) {
			LuaTable m = new LuaTable();
			matrix.set(x, m);
			for(int y = 1; y <= size; y++) {
				m.set(y, Utils.itemStackToLuatable( event.getInventory().getStackInSlot( (x-1) + (y-1)*size) ));
			}
		}
		e.set(4, matrix);
		fireEvent(EventName.ItemCrafted, e);
	}

	@SubscribeEvent 
	public void onItemToss(ItemTossEvent event) {//FIXME DEAD
		LuaTable e = createEvent(EventName.ItemTossed);
		e.set(3, Utils.itemStackToLuatable(event.getEntityItem().getItem()));
		fireEvent(EventName.ItemTossed, e);
	}

	@SubscribeEvent 
	public void onItemBreak(PlayerDestroyItemEvent event) { //FIXME ULTRA DEAD
		ItemStack yeWhoBrokeith = event.getOriginal(); 
		LuaTable e = createEvent(EventName.BreakItem);
		e.set(3, Utils.itemStackToLuatable(yeWhoBrokeith));
		fireEvent(EventName.BreakItem, e);
	}

	//FIXME event is missing D:
	@SubscribeEvent 
	public void onJoinedWorld(PlayerEvent.PlayerLoggedInEvent event){ //EntityJoinWorldEvent, GatherLoginPayloadsEvent, PlayerLoggedInEvent

		TaskDispatcher.delayTask(()->{
			OpenChangeLog.openChangeLog(false);
		}, 1500);

		Minecraft mc = AdvancedMacros.getMinecraft();
		LuaTable e = createEvent(EventName.JoinWorld);
		if(mc.getConnection()!=null && mc.getConnection().getNetworkManager()!=null)
			e.set(3, NetworkHooks.getConnectionType(()->mc.getConnection().getNetworkManager()).name() ); //yeilded modded
		else
			e.set(3, ConnectionType.MODDED.name());
		e.set(4,  mc.isSingleplayer()?"SP":"MP");
		if(AdvancedMacros.getMinecraft().getCurrentServerData()!=null){
			ServerData sd = AdvancedMacros.getMinecraft().getCurrentServerData();
			if(sd!=null) {
				e.set(5, sd.serverName==null? LuaValue.FALSE    : LuaValue.valueOf(sd.serverName));
				e.set(6, sd.serverMOTD==null? LuaValue.FALSE    : LuaValue.valueOf(sd.serverMOTD));
				e.set(7, sd.serverIP==null?   LuaValue.FALSE    : LuaValue.valueOf(sd.serverIP));
			}
			//e.set(8, GetWorld.worldToTable(event.));
		}
		fireEvent(EventName.JoinWorld, e);
		resetLastStats();

	}
	@SubscribeEvent 
	public void OnLeaveWorld(PlayerLoggedOutEvent event) {
		LuaTable e = createEvent(EventName.LeaveWorld);
		fireEvent(EventName.LeaveWorld, e);
		lastPlayerList = null;
	}

	@SubscribeEvent 
	public void  onWorldSaved(SaveToFile event) {
		fireEvent(EventName.WorldSaved, createEvent(EventName.WorldSaved));
	}

	private boolean startupHasFired = false;
	@SubscribeEvent
	public void onGuiStartup(GuiScreenEvent.InitGuiEvent.Post sEvent) {
		if(sEvent.getGui().getClass().equals(MainMenuScreen.class)) {
			if(startupHasFired) return;
			//TaskDispatcher.addTask(()->{
			fireEvent(EventName.Startup,ForgeEventHandler.createEvent(EventName.Startup));
			//});
			startupHasFired = true;
		}
	}

	@SubscribeEvent
	public void onGuiOpened(GuiOpenEvent sEvent) {
		Screen sGui = sEvent.getGui();
		if(sGui==null) {
			AdvancedMacros.forgeEventHandler.fireEvent(EventName.GUIClosed, createEvent(EventName.GUIClosed));
		}else{
			String name = sGui.getClass().toString();
			if(sGui.toString().startsWith("ScriptGui:")) {
				name = sGui.toString();
			}else {
				if(name.startsWith("class ")) {
					name = name.substring(6);
				}

				switch (name) {
				case "com.theincgi.advancedMacros.gui.MacroMenuGui":
					name = "AdvancedMacros:BindingsMenu";
					break;
				case "com.theincgi.advancedMacros.gui2.ScriptBrowser2":
					name = "AdvancedMacros:ScriptBrowser";
					break;
				case "com.theincgi.advancedMacros.gui.RunningScriptsGui":
					name = "AdvancedMacros:RunningScripts";
					break;
				case "com.theincgi.advancedMacros.gui.EditorGUI":
					name = "AdvancedMacros:Editor";
					break;
				case "net.minecraft.client.gui.inventory.GuiInventory":
					name = "inventory";
					break;
				case "net.minecraft.client.gui.GuiEnchantment":
					name = "enchantment table";
					break;
				case "net.minecraft.client.gui.GuiMerchant":
					name = "villager";
					break;
				case "net.minecraft.client.gui.GuiRepair":
					name = "anvil";
					break;
				case "net.minecraft.client.gui.inventory.GuiBeacon": //FIXME names need the Screen versions
					name = "beacon";
					break;
				case "net.minecraft.client.gui.inventory.GuiBrewingStand":
					name = "brewing stand";
					break;
				case "net.minecraft.client.gui.inventory.GuiChest":
					name = "chest";
					break;
				case "net.minecraft.client.gui.inventory.GuiCrafting":
					name = "crafting table";
					break;
				case "net.minecraft.client.gui.inventory.GuiDispenser":
					name = "dispenser";
					break;
				case "net.minecraft.client.gui.inventory.GuiFurnace":
					name = "furnace";
					break;
				case "net.minecraft.client.gui.GuiHopper":
					name = "hopper";
					break;
				case "net.minecraft.client.gui.inventory.GuiScreenHorseInventory":
					name = "horse inventory";
					break;
				case "net.minecraft.client.gui.inventory.GuiShulkerBox":
					name = "shulker box";
					break;
				case "net.minecraft.client.gui.inventory.GuiEditSign":
					name = "sign";
					break;
				case "net.minecraft.client.gui.GuiScreenBook":
					name = "book";
					break;
				case "net.minecraft.client.gui.GuiCommandBlock":
					name = "command block";
					break;
				default:
					if(name.startsWith("net.minecraft.client.gui")) {
						name = "Minecraft:"+name.substring(name.lastIndexOf('.'));
					}
					break; //defaults to full class name
				}
			}

			final String fName = name;
			final LuaValue controls = GuiControls.load(sGui);

			if(sGui instanceof ContainerScreen) {
				Thread test = new Thread(()->{
					ContainerScreen<?> gCon = (ContainerScreen) sGui; 
					List<ItemStack> stacks = gCon.getContainer().getInventory(); 
					LuaTable e = createEvent(EventName.ContainerOpen);

					LuaTable ctrl;
					if(controls.isnil())
						ctrl = new LuaTable();
					else
						ctrl = controls.checktable();

					ctrl.set("inventory", AdvancedMacros.openInventory.call());
					e.set(3, ctrl);
					e.set(4, fName);
					//					LuaTable items = new LuaTable();
					//					if(stacks!=null) {
					//						for(int i = 0; i<stacks.size(); i++) {
					//							items.set(i+1, Utils.itemStackToLuatable(stacks.get(i)));
					//						}
					//						e.set(3, items);
					//					}else{
					//						e.set(3, LuaValue.FALSE);
					//					}
					//					
					//System.out.println(AdvancedMacros.getMinecraft().ingameGUI.getClass());

					fireEvent(EventName.ContainerOpen, e);

				});
				test.start();

			}
			LuaTable args = createEvent(EventName.GUIOpened);

			args.set(3, controls);
			args.set(4, name);


			fireEvent(EventName.GUIOpened, args);

		}
	}



	@SubscribeEvent 
	public void onUseItem( LivingEntityUseItemEvent event) {//CONFIRMED MP
		//lik srsly
		if(!event.getEntity().equals(AdvancedMacros.getMinecraft().player)) return;
		int useItemFrequency = 1;
		try{
			useItemFrequency = Utils.tableFromProp(Settings.settings, "events.useItemFrequency", LuaValue.valueOf(20)).checkint();
		}catch (Exception e) {
			Settings.settings.get("events").set("useItemFrequency", LuaValue.valueOf(20));
			useItemFrequency = 20;
		}
		if(event.getDuration()%useItemFrequency != 0 && event.getClass().getSimpleName().toLowerCase().equals("tick")) return;
		LuaTable e = createEvent(EventName.UseItem);
		e.set(3, Utils.itemStackToLuatable(event.getItem()));
		e.set(4, LuaValue.valueOf(event.getDuration())); 
		e.set(5, event.getClass().getSimpleName().toLowerCase());
		fireEvent(EventName.UseItem, e);
	}

	public final SoundListener SOUND_LISTENER = new SoundListener(); 
	public class SoundListener implements ISoundEventListener {
		@Override
		public void onPlaySound(ISound sound, SoundEventAccessor accessor) {
			LuaTable event = createEvent(EventName.Sound);
			event.set(3, sound.getSoundLocation().toString());

			LuaTable details = new LuaTable();

			try {
				details.set("pitch", sound.getPitch());
			}catch (NullPointerException e) {
				details.set("pitch", 1);
			}
			try{
				details.set("volume", sound.getVolume());
			}catch (NullPointerException e) {
				details.set("volume", 1);
			}
			try{details.set("pos", Utils.posToTable(sound.getX(), sound.getY(), sound.getZ()));}catch(NullPointerException e) {

			}
			try{details.set("category", sound.getCategory().getName().toLowerCase());}catch(NullPointerException e) {

			}
			event.set(4, details);

			LuaTable controls = new LuaTable();
			controls.set("isPlaying", new ZeroArgFunction() {
				@Override
				public LuaValue call() {
					return LuaValue.valueOf(AdvancedMacros.getMinecraft().getSoundHandler().isPlaying(sound));
				}
			});
			controls.set("stop", new ZeroArgFunction() {
				@Override
				public LuaValue call() {
					AdvancedMacros.getMinecraft().getSoundHandler().stop(sound);
					return NONE;
				}
			});
			event.set(5, controls);
			fireEvent(EventName.Sound, event);

		}


	}


	private static Object messageCounterLock = new Object();
	private static long messageIndex = 0;
	private static long nextMessageToAddToChat = 0;
	@SubscribeEvent
	public void onChat(final ClientChatReceivedEvent sEvent){//TODO out going chat msg filter
		final ClientChatReceivedEvent event = sEvent; //arg not final because it's acquired thru reflection
		final long thisMessageIndex;
		synchronized (messageCounterLock) {
			thisMessageIndex = messageIndex++;
		}

		System.out.println("Got " + event.getMessage().getString() + " as " + thisMessageIndex);

		final LinkedList<String> toRun = AdvancedMacros.macroMenuGui.getMatchingScripts(false, EventName.ChatFilter.name(), false);
		JavaThread t = new JavaThread(()->{

			LuaTable e = createEvent(EventName.Chat);
			LuaTable e2 = createEvent(EventName.ChatFilter);
			String unformated = event.getMessage().getString();

			Pair<String, LuaTable> pair   = Utils.codedFromTextComponent(event.getMessage());//fromMinecraftColorCodes(event.getMessage().getFormattedText());
			String formated = pair.a;

			//formated = formated.substring(0, formated.length()-2);//gets rid of last &f that does nothing for us
			//System.out.println(sEvent.getMessage().getSiblings());
			//TODO simplfy formating
			LuaTable actions = pair.b;

			e.set(3, formated);
			e2.set(3, formated);
			e.set(4, unformated);
			e2.set(4,unformated);
			e.set(5, actions);
			e2.set(5, actions);

			for (String script : toRun) {
				if(script==null) continue;
				File f = new File(AdvancedMacros.macrosFolder, script);
				if(f.exists() && f.isFile()) {
					try {
						FileReader fr = new FileReader(f);
						Thread.currentThread().setName("ChatFilter - " + script);
						LuaValue function = AdvancedMacros.globals.load(fr, f.getAbsolutePath());
						Varargs ret = function.invoke(e2.unpack());
						if(!ret.toboolean(1)) 
							return;
						e2 = createEvent(EventName.ChatFilter);
						for(int i = 1; i<= ret.narg(); i++)
							e2.set(2+i, ret.arg(i));
					} catch (FileNotFoundException ex) {
						ex.printStackTrace();
					}catch (LuaError le){
						Utils.logError(le);
					}
				}
			}
			if(toRun.size() > 0)
				for(int i = 0; i<e2.length(); i++) 
					e.set(6+i, e2.get(3+i));
			else {
				LuaTable toUnpack = e.get(5).checktable();
				for(int i = 1; i<=Math.max(toUnpack.length(), 2); i++)
					e2.set(3+i, toUnpack.get(i));
			}


			LuaValue timeoutProp = Settings.settings.get("chatFilterTimeout");
			if(timeoutProp.isnil())
				Settings.settings.set("chatFilterTimeout", 3000);
			long timeout = System.currentTimeMillis() + timeoutProp.optlong(3000);
			while(true) {
				long current;
				synchronized (messageCounterLock) {
					if(nextMessageToAddToChat >= thisMessageIndex)
						break;
				}
				if(System.currentTimeMillis() >= timeout)
					break;
				try {Thread.sleep(50);}catch(Exception ex) {}
			}

			System.out.println("Adding msg "+thisMessageIndex);
			if(e2.get(3).toboolean()) {
				//AdvancedMacros.logFunc.invoke(e2.unpack().subargs(3));
				Pair<ITextComponent, Varargs> text = Utils.toTextComponent(e2.unpack().arg(3).checkjstring(), e2.unpack().subargs(4), true);
				ClientChatReceivedEvent ccre = new ClientChatReceivedEvent(sEvent.getType(), text.a);
				repostForgeEvent(ccre);
			}
			fireEvent(EventName.Chat, e);
			synchronized (messageCounterLock) {
				nextMessageToAddToChat = Math.max(thisMessageIndex+1, nextMessageToAddToChat);
			}
		});
		if(toRun.size() > 0) {
			t.start(); 
			sEvent.setCanceled(true);
		}
	}
	@SubscribeEvent 
	public void sendingChat(final ClientChatEvent event) {
		JavaThread thread = new JavaThread(() -> {
			LuaTable e = createEvent(EventName.ChatSendFilter);
			e.set(3, LuaValue.valueOf(event.getMessage()));
			//			LuaValue maxTime       = Utils.tableFromProp(Settings.settings, "chat.maxFilterTime", LuaValue.valueOf(500));
			//			LuaValue timeoutAciton = Utils.tableFromProp(Settings.settings, "chat.cancelOnTimeout", LuaValue.TRUE);

			LinkedList<String> toRun = AdvancedMacros.macroMenuGui.getMatchingScripts(false, EventName.ChatSendFilter.name(), false);

			for (String script : toRun) {
				if(script==null) return;
				File f = new File(AdvancedMacros.macrosFolder, script);
				if(f.exists() && f.isFile()) {
					try {
						FileReader fr = new FileReader(f);
						Thread.currentThread().setName("ChatSendFilter - " + script);
						LuaValue function = AdvancedMacros.globals.load(fr, f.getAbsolutePath());
						Varargs ret = function.invoke(e.unpack());
						if(!ret.toboolean(1)) 
							return;
						e = createEvent(EventName.ChatSendFilter);
						for(int i = 1; i<= ret.narg(); i++)
							e.set(2+i, ret.arg(i));
					} catch (FileNotFoundException ex) {
						ex.printStackTrace();
					}catch (LuaError le){
						Utils.logError(le);
					}
				}
			}
			forceSendMsg(e.get(3).tojstring(), true);
		});
		thread.start();
		event.setCanceled( true );
		AdvancedMacros.getMinecraft().ingameGUI.getChatGUI().addToSentMessages(event.getOriginalMessage());
	}

	@SubscribeEvent 
	public void onEntityRender(RenderLivingEvent.Pre event) {
		RenderFlags r = entityRenderFlags.get(event.getEntity());
		if(r==null) return;

		//event.getRenderer().setRenderOutlines(true);
		boolean flag = false;
		if(event.getEntity() instanceof PlayerEntity) {
			PlayerEntity p = (PlayerEntity) event.getEntity();
			flag = p.isSneaking() | wasSneaking;
			wasSneaking = p.isSneaking();
		}

		Method m = getGlowMethod();
		if(m!=null) {
			m.setAccessible(true);
			try {
				m.invoke(event.getEntity(), 6, r.glow);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		r.reset();

		if(r.xray) {
			GlStateManager.disableDepthTest();
		}
	}

	private static Method glowMethod;
	private static Method getGlowMethod() {
		if(glowMethod!=null)return glowMethod;
		try {
			return ObfuscationReflectionHelper.findMethod(Entity.class, "func_70052_a", int.class, boolean.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@SubscribeEvent 
	public void onEntityRender(RenderLivingEvent.Post event) {
		RenderFlags r = entityRenderFlags.get(event.getEntity());
		if(r==null) return;
		if(r.xray)
			GlStateManager.enableDepthTest();
	}

	//this is from the Screen class, skips the forge event though
	private void forceSendMsg(String msg, boolean addToChat) {
		//msg = net.minecraftforge.event.ForgeEventFactory.onClientSendMessage(msg);
		if (msg.isEmpty()) return;
		if (addToChat) {
			AdvancedMacros.getMinecraft().ingameGUI.getChatGUI().addToSentMessages(msg);
		}
		//if (net.minecraftforge.client.ClientCommandHandler.instance.executeCommand(mc.player, msg) != 0) return; //Forge: TODo Client command re-write

		AdvancedMacros.getMinecraft().player.sendChatMessage(msg);
	}

	@SubscribeEvent 
	public void onItemPickup(EntityItemPickupEvent ipe){ //check net.minecraftforge.event.ForgeEventFactory onItemPickup
		LuaTable e = createEvent(EventName.ItemPickup);
		e.set(3, Utils.codedFromTextComponent(ipe.getEntityPlayer().getName()).a);
		e.set(4, Utils.itemStackToLuatable(ipe.getItem().getItem()));
		fireEvent(EventName.ItemPickup, e);
	}


	@SubscribeEvent
	public void postRenderGameLoop(TickEvent.RenderTickEvent event) { //runs very frequently, this is where the old add scheduled tasks use to go (close enough anyway)
		if(event.phase.equals(Phase.END))
			TaskDispatcher.runTasks(); //TODO trigger via hook so if someone disables the world render they can still use task dispatcher dependent functions
	}

	//+===================================================================================================+
	//|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~END OF FORGE EVENTS~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|
	//+===================================================================================================+
	//not including this world render, but its not for the trigger list

	private Field MCForge_EventBusID;
	private Field readable;
	private String myOnChatMethodReadable;
	private void repostForgeEvent(Event event) {
		try {
			ListenerList list = event.getListenerList();
			if(MCForge_EventBusID==null) {
				MCForge_EventBusID = MinecraftForge.EVENT_BUS.getClass().getDeclaredField("busID");
				MCForge_EventBusID.setAccessible(true);
				readable = ASMEventHandler.class.getDeclaredField("readable");
				readable.setAccessible(true);
				Method onChat = this.getClass().getDeclaredMethod("onChat", ClientChatReceivedEvent.class);
				myOnChatMethodReadable = "ASM: " + this + " " + onChat.getName() + Type.getMethodDescriptor(onChat);
			}
			int busId = MCForge_EventBusID.getInt(MinecraftForge.EVENT_BUS);
			IEventListener[] listeners = list.getListeners(busId);
			boolean flag = true;
			for(int i = 0; i<listeners.length; i++) {
				if(flag) {
					IEventListener iel = listeners[i];
					if(!(iel instanceof ASMEventHandler))
						continue;
					ASMEventHandler asmeh = (ASMEventHandler) iel;


					if( myOnChatMethodReadable.equals(readable.get(asmeh)) )
						flag = false;
					continue;
				}
				listeners[i].invoke(event);
			}
			if(!event.isCanceled())
				if(event instanceof ClientChatReceivedEvent) {
					ClientChatReceivedEvent ccre = (ClientChatReceivedEvent) event;
					AdvancedMacros.getMinecraft().ingameGUI.addChatMessage(ccre.getType(), ccre.getMessage());
				}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void resetItemDurablitity(){
		ItemStack i = AdvancedMacros.getMinecraft().player.getHeldItemMainhand();
		lastItemDurablity = i.getMaxDamage()-i.getDamage();
	}
	private void resetLastStats(){
		PlayerEntity player = AdvancedMacros.getMinecraft().player;
		if(player==null){return;}
		lastAir = player.getAir()/3;
		lastHealth = (int) player.getHealth();
		lastHunger = player.getFoodStats().getFoodLevel();
		lastSaturation = player.getFoodStats().getSaturationLevel();
		//lastDimension = player.dimension;
		resetItemDurablitity();

		NonNullList<ItemStack> armor = (NonNullList<ItemStack>) player.getArmorInventoryList();
		boolean armorChanged = false;
		int[] armorDurability = new int[4];
		for (int i = 0; i < armorDurability.length; i++) {
			int temp = armor.get(i).getMaxDamage() - armor.get(i).getDamage();
			if(temp!=lastArmourDurability[i]){armorChanged = true;}
			armorDurability[i] = temp;
		}
		for(int i = 0; i<armorDurability.length; i++){
			lastArmourDurability[i] = armorDurability[i];
		}
		lastXP = player.experienceTotal;
		lastXPLevel = player.experienceLevel;
	}
	//public static LuaTable worldHud = new LuaTable();
	@SubscribeEvent 
	public void onLastWorldRender(RenderWorldLastEvent rwle){
		//		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, AdvancedMacros.modelView3d);
		//		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, AdvancedMacros.projView3d);
		ActiveRenderInfo renderInfo = AdvancedMacros.getMinecraft().gameRenderer.getActiveRenderInfo();
		Vec3d projectedView = renderInfo.getProjectedView();
		

		//double x,y,z,uMin,vMin,uMax,vMax, wid, hei;
		float p = AdvancedMacros.getMinecraft().getRenderPartialTicks();
		Entity player = AdvancedMacros.getMinecraft().player;

		GlStateManager.pushTextureAttributes();
		GlStateManager.enableCull();
		GlStateManager.cullFace(CullFace.BACK);
		GlStateManager.enableBlend();


		//src color -> src color?
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.pushMatrix();

		//GlStateManager.enableLighting();
		
		//GlStateManager.translated(0, -(player.getEyeHeight()), 0);
		synchronized (worldHudItems) {
			for (WorldHudItem worldHudItem : worldHudItems) {
				//System.out.println(worldHudItem);
				if(worldHudItem.getDrawType().isXRAY()){
					GlStateManager.disableDepthTest();
				}else{
					GlStateManager.enableDepthTest();
				}
				GlStateManager.color4f(1, 1, 1, worldHudItem.getOpacity());
				worldHudItem.render(projectedView.x, projectedView.y, projectedView.z);
			}
		}
		GlStateManager.popMatrix();
		GlStateManager.disableBlend();//F1 is black otherwise
		GlStateManager.popAttributes();
	}

	@SubscribeEvent
	public void afterOverlay(RenderGameOverlayEvent.Post event) {

		float p = AdvancedMacros.getMinecraft().getRenderPartialTicks();
		//Entity player = AdvancedMacros.getMinecraft().player;

		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

		GlStateManager.disableBlend();
		GlStateManager.enableBlend();
		//GL11.glEnable(GL11.GL_BLEND);
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		//GlStateManager.enableBlend();
		//GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 1);
		GlStateManager.disableAlphaTest();
		//		GlStateManager.enableAlpha();
		GlStateManager.bindTexture(0);
		//GlStateManager.enableLighting();

		synchronized (hud2DItems) {
			for (Hud2DItem hudItem : hud2DItems) {
				//System.out.println(worldHudItem);
				GlStateManager.color4f(1, 1, 1, hudItem.getOpacity()/255f);
				hudItem.render(p);
			}
		}
		//GlStateManager.disableBlend();//F1 is black otherwise
		GL11.glPopAttrib();
		//GlStateManager.color(0, 0, 0, 0);
		GlStateManager.color4f(1, 1, 1, 1);
		//GlStateManager.disableBlend();
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.disableAlphaTest();
		GlStateManager.enableAlphaTest();
		GlStateManager.disableBlend();
		GlStateManager.enableBlend();
		GlStateManager.color4f(1, 1, 1, 1);
		//	GlStateManager.disableTexture2D();
	}

	public static double accuPlayerX(float pTick, Entity e){
		return e.posX*pTick + e.lastTickPosX*(1-pTick);
	}
	public static double accuPlayerY(float pTick, Entity e){
		return e.posY*pTick + e.lastTickPosY*(1-pTick);
	}
	public static double accuPlayerZ(float pTick, Entity e){
		return e.posZ*pTick + e.lastTickPosZ*(1-pTick);
	}



	/**so I dont have to type out the same new LuaTable and fill in event, eventName*/
	public static LuaTable createEvent(String eventName){
		LuaTable t = new LuaTable();
		t.set(1, "event");
		t.set(2, eventName);
		return t;
	}
	public static LuaTable createEvent(EventName eventName){
		return createEvent(eventName.name());
	}
	public void fireEvent(EventName event, LuaTable args){
		AdvancedMacros.macroMenuGui.fireEvent(false, event.name(), args.unpack(), false, null);
	}
	public void fireEvent(String eventString, LuaTable args){
		AdvancedMacros.macroMenuGui.fireEvent(false, eventString, args.unpack(), false, null);
	}
	public void fireEvent(EventName event, LuaTable args, OnScriptFinish onScriptFinish){
		AdvancedMacros.macroMenuGui.fireEvent(false, event.name(), args.unpack(), false, onScriptFinish);
	}
	public boolean eventExists(EventName eName){
		return AdvancedMacros.macroMenuGui.doesEventExist(eName.name());
	}


	/*the main menu for this mod*/
	public static void showMenu(){
		showMenu(AdvancedMacros.macroMenuGui.getGui());
	}
	public static void closeMenu() {
		AdvancedMacros.getMinecraft().displayGuiScreen(null);
	}
	public static void showMenu(Gui gui){
		if(gui==null){
			showMenu();
		}
		AdvancedMacros.lastGui = gui;//the one to return to on open, not prev gui
		AdvancedMacros.getMinecraft().displayGuiScreen(gui);
		gui.onOpen();

	}
	public static void showMenu(Gui gui, Gui prevGui){
		AdvancedMacros.getMinecraft().displayGuiScreen(gui);
		AdvancedMacros.prevGui = prevGui;
		AdvancedMacros.lastGui = gui;
		gui.onOpen();
	}
	public static void showPrevMenu(){
		if(AdvancedMacros.prevGui==null){
			AdvancedMacros.prevGui=AdvancedMacros.macroMenuGui.getGui();
		}
		showMenu(AdvancedMacros.prevGui);
		AdvancedMacros.prevGui = null;
	}
	public void addWorldHudItem(WorldHudItem whi){
		synchronized(worldHudItems){
			worldHudItems.add(whi);
		}
	}
	public void removeWorldHudItem(WorldHudItem whi){
		synchronized (worldHudItems) {
			worldHudItems.remove(whi);
		}
	}
	public void clearWorldHud(){
		synchronized (worldHudItems) {
			while(!worldHudItems.isEmpty()){
				worldHudItems.getFirst().disableDraw(); //removed when disable draw is called
			}
			worldHudItems.clear();
		}
	}

	public void addHud2DItem(Hud2DItem item) {
		synchronized (hud2DItems) {
			hud2DItems.add(item);
		}
	}
	public void removeHud2DItem(Hud2DItem item) {
		synchronized (hud2DItems) {
			hud2DItems.remove(item);
		}
	}
	public void clear2DHud() {
		synchronized (hud2DItems) {
			while(!hud2DItems.isEmpty()) {
				hud2DItems.getLast().disableDraw();
			}
			hud2DItems.clear();
		}
	}

	public int getSTick() {
		synchronized (sTickSync) {
			return sTick;
		}
	}
	public Object getTickLock() {
		return tickLock;
	}

	private Look look;
	public void lookTo(float sYaw, float sPitch, long time) {
		PlayerEntity player = AdvancedMacros.getMinecraft().player;
		look = new Look(player.rotationYawHead, player.rotationPitch,sYaw, sPitch, time);

	}
	private class Look{
		float fromYaw, fromPitch;
		float toYaw, toPitch;
		long time, start;
		public Look(float fromYaw, float fromPitch, float toYaw, float toPitch, long time) {
			super();
			fromYaw = fixYaw(fromYaw);
			toYaw = fixYaw(toYaw);
			this.fromYaw = fromYaw;

			this.fromPitch = fromPitch;
			this.toYaw = toYaw;
			this.toPitch = toPitch;

			PlayerEntity player = AdvancedMacros.getMinecraft().player;
			player.rotationYaw = fixYaw( player.rotationYaw );
			player.prevRotationYaw = fixYaw( player.prevRotationYaw );

			double b = toYaw-fromYaw;
			double a = 360-b;
			a%=360;
			boolean flag = Math.abs(b) > 180;
			if(flag){//better to turn other way
				float amount =  (float) (360 * Math.signum(b));
				this.fromYaw += amount;
				player.rotationYaw += amount;
				player.prevRotationYaw +=amount;
				//System.out.println("Reverse spin");
			}else if(fromYaw%360==toYaw%360){ //same do nothing really
				fromYaw=toYaw;
			}
			//AdvancedMacros.logFunc.call(String.format("%f, %f -> %f, %f (&%s%f, %f&f)", this.fromYaw, fromPitch, toYaw, toPitch, flag?"a":"c" , a, b));
			start = System.currentTimeMillis();
			this.time = time;
		}
		private float fixYaw(float yaw) {
			return (yaw + 540) %360 -180;
		}
		public void look(){
			if(System.currentTimeMillis()<=time+start){
				PlayerEntity player = AdvancedMacros.getMinecraft().player;


				player.rotationYaw = interpolate(fromYaw, toYaw);
				player.rotationPitch = interpolate(fromPitch, toPitch);
			}
		}
		private float interpolate(float f, float t){
			float x = System.currentTimeMillis()-start;
			float u = (f-t)/2; 
			return u*MathHelper.cos((float) ((x*Math.PI)/time)) - u + f;
		}
	}

	private class HeldKeybinds{
		net.minecraft.client.util.InputMappings.Input input;
		long releaseTime;
		boolean done = false; //removal flag
		public HeldKeybinds(net.minecraft.client.util.InputMappings.Input input, long releaseTime) {
			super();
			this.input = input;
			this.releaseTime = releaseTime;
		}

	}



	private void populatePlayerList(ConcurrentHashMap<UUID, String> map) {
		Minecraft mc = AdvancedMacros.getMinecraft();
		Iterator<NetworkPlayerInfo> iter = mc.getConnection().getPlayerInfoMap().iterator();
		while(iter.hasNext()) {
			NetworkPlayerInfo playerInfo = iter.next();
			UUID uuid = playerInfo.getGameProfile().getId();
			String name = Utils.codedFromTextComponent(mc.ingameGUI.getTabList().getDisplayName(playerInfo)).a;
			if(name!=null) {
				String formated   = name
					.replaceAll("&", "&&")
					.replaceAll("\u00A7", "&")
					.replaceAll("&k", "&O") //Obfuscated
					.replaceAll("&l", "&B") //Bold
					.replaceAll("&m", "&S") //Strikethru
					.replaceAll("&o", "&I") //Italics
					.replaceAll("&r", "&f")   //reset (to white in this case)
					.replaceAll("&[^&]", "").replaceAll("&&", "&")
					;
				map.put(uuid, formated);
			}
		}
	}

	//private LinkedList<HeldKeybinds> heldKeybinds = new LinkedList<>();
	ConcurrentHashMap<net.minecraft.client.util.InputMappings.Input, HeldKeybinds> keyBindReleaseMap = new ConcurrentHashMap<>();
	public void releaseKeybindAt(net.minecraft.client.util.InputMappings.Input input, long l) {
		//heldKeybinds.add(new HeldKeybinds(keycode, l));
		keyBindReleaseMap.put(input, new HeldKeybinds(input, l));
	}



	public static class RenderFlags{
		private boolean xray = false;
		private boolean glow = false;
		private boolean changed = false;
		public void setXray(boolean xray) {
			this.xray = xray;
		}
		public void setGlow(boolean glow) {
			this.glow = glow;
			changed = true;
		}
		public void reset() {
			changed = false;
		}
		public boolean isChanged() {
			return changed;
		}
	}
}
