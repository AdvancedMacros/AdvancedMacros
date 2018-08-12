package com.theincgi.advancedMacros.event;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.gui.MacroMenuGui;
import com.theincgi.advancedMacros.gui.elements.ColorTextArea;
import com.theincgi.advancedMacros.hud.hud2D.Hud2DItem;
import com.theincgi.advancedMacros.hud.hud3D.WorldHudItem;
import com.theincgi.advancedMacros.lua.LuaDebug.OnScriptFinish;
import com.theincgi.advancedMacros.lua.util.ContainerControls;
import com.theincgi.advancedMacros.misc.Settings;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.CullFace;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.SaveToFile;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.util.control.TailCalls.Cont;

public class ForgeEventHandler {
	ConcurrentHashMap<Integer, Boolean> heldKeys = new ConcurrentHashMap<>(10);
	int lastAir, lastHealth,
	lastItemDurablity, lastHotbar, lastHunger;
	boolean playerWasNull = true, lastSleepingState = false;

	//added lastSwingProgress even though it exists in the player because it would fire multiple times
	float lastSaturation, lastSwingProgress;
	ItemStack lastHeldItem;
	//int lastDimension;
	int[] lastArmourDurability = new int[4];
	int lastXP, lastXPLevel;
	int lastDim;
	boolean wasRaining, wasThundering;
	/**Keeping this syncronized!*/
	private LinkedList<WorldHudItem> worldHudItems = new LinkedList<>();
	private LinkedList<Hud2DItem> hud2DItems = new LinkedList<>();
	int sTick = 0;
	private ConcurrentHashMap<String, Boolean> lastPlayerList;
	private ConcurrentHashMap<String, Boolean> nowPlayerList = new ConcurrentHashMap<>();
	private boolean wasOnFire = false;
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
	public void releaseAllKeys() {
		heldKeys.clear();
	}
	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event){
		if(AdvancedMacros.modKeybind.isKeyDown()){ //TODO allow mouse buttons too
			if(ColorTextArea.isCTRLDown()){
				if(ColorTextArea.isShiftDown()) {
					AdvancedMacros.stopAll();
				}else {
					Minecraft.getMinecraft().displayGuiScreen(AdvancedMacros.runningScriptsGui);
				}
			}else if(ColorTextArea.isShiftDown()){
				showMenu(AdvancedMacros.scriptBrowser2, AdvancedMacros.macroMenuGui);
			}else{
				if(AdvancedMacros.lastGui!=null){
					AdvancedMacros.lastGui.showGui();
				}else{
					MacroMenuGui.showMenu();
				}
			}
		}else{
			int eventKey = Keyboard.getEventKey();
			boolean oldState = heldKeys.getOrDefault(eventKey, false);
			if(oldState && Keyboard.isKeyDown(eventKey)){
				//was in, is in now
				return;
			}
			if(Keyboard.isKeyDown(eventKey))
				heldKeys.put(eventKey, true);
			else
				heldKeys.remove(eventKey);
			LuaTable eventDat = new LuaTable();
			eventDat.set(1, LuaValue.valueOf("key"));
			eventDat.set(2, Keyboard.getKeyName(eventKey));
			eventDat.set(3, LuaValue.valueOf(Keyboard.isKeyDown(eventKey)?"down":"up"));
			eventDat.set(4, LuaValue.valueOf(eventKey));
			AdvancedMacros.macroMenuGui.fireEvent(true, Keyboard.getKeyName(eventKey), eventDat.unpack(), Keyboard.isKeyDown(eventKey));
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
		eDat.set(1, LuaValue.valueOf("mouse"));
		eDat.set(2, buttonName);
		eDat.set(3, state?"down":"up");
		AdvancedMacros.macroMenuGui.fireEvent(true, buttonName, eDat.unpack(), state);
	}

	@SubscribeEvent @SideOnly(Side.CLIENT)
	public void onPlayerTick(TickEvent.PlayerTickEvent event){
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER) 
			return; //lik srsly
		if(event.phase.equals(TickEvent.Phase.START)) return; //only do on the second half of tick after all stuff happens
		for(int i = 0; i<Mouse.getButtonCount(); i++) {
			boolean b = Mouse.isButtonDown(i);
			if(b!=heldMouseButtons.get(i)) {
				onMouseClick(i, b);
				heldMouseButtons.set(i, b);
			}
		}
		sTick++;
		if(look!=null){
			look.look();
		}
		LinkedList<Integer> toRemove = new LinkedList<>();
		for(Integer i : keyBindReleaseMap.keySet()) {
			HeldKeybinds hk = keyBindReleaseMap.get(i);
			if(hk.releaseTime<System.currentTimeMillis()) {
				KeyBinding.setKeyBindState(hk.keyCode, false);
				hk.done=true; //is this even needed anymore?
				toRemove.add(i);
			}
		}
		while(!toRemove.isEmpty()) {
			keyBindReleaseMap.remove(toRemove.pop());
		}

		EntityPlayerSP player = Minecraft.getMinecraft().player;
		if(player==null){playerWasNull = true; return;}
		if(playerWasNull){
			resetLastStats();
			playerWasNull=false;
		}

		if(player.dimension != lastDim) {
			LuaTable e = createEvent(EventName.DimensionChanged);
			e.set(3, LuaValue.valueOf(player.dimension));
			e.set(4, LuaValue.valueOf(lastDim));
			fireEvent(EventName.DimensionChanged, e);
			lastDim = player.dimension;
		}
		if(player.isPlayerSleeping() != lastSleepingState) {
			if(player.isPlayerSleeping())
				fireEvent(EventName.UseBed, createEvent(EventName.UseBed));
			else {
				fireEvent(EventName.WakeUp, createEvent(EventName.WakeUp));
			}
			lastSleepingState = player.isPlayerSleeping();
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
			int temp = armor.get(i).getMaxDamage() - armor.get(i).getItemDamage();
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
		for (PotionEffect e : player.getActivePotionEffects()) {
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
		boolean rain = Minecraft.getMinecraft().world.isRaining();
		boolean thunder = Minecraft.getMinecraft().world.isThundering();
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
		int currentDura = player.getHeldItemMainhand().getMaxDamage()-player.getHeldItemMainhand().getItemDamage();
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
		for(String s: nowPlayerList.keySet()) {
			if(!lastPlayerList.getOrDefault(s, false)) {
				//player joined
				LuaTable e = createEvent(EventName.PlayerJoin);
				e.set(3, s);
				fireEvent(EventName.PlayerJoin, e);
				lastPlayerList.put(s, true);
			}
		}
		for(String s: lastPlayerList.keySet()) {
			//	System.out.println("Checking: "+s);
			if(!nowPlayerList.containsKey(s)) {
				//left world
				//System.out.println("left");
				LuaTable e = createEvent(EventName.PlayerLeave);
				e.set(3, s);
				fireEvent(EventName.PlayerLeave, e);
				lastPlayerList.remove(s);
			}//else System.out.println("Stayed");
		}

	}

	@SubscribeEvent @SideOnly(Side.CLIENT)
	public void onArrowFired(ArrowLooseEvent event){//CONFIRMED MP
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER) return; //lik srsly
		LuaTable e = createEvent(EventName.ArrowFired);
		e.set(3, Utils.itemStackToLuatable(event.getBow()));
		e.set(4, LuaValue.valueOf(event.getCharge()));
		e.set(5, LuaValue.valueOf(event.hasAmmo()));
		fireEvent(EventName.ArrowFired, createEvent(EventName.ArrowFired));
	}
	@SubscribeEvent @SideOnly(Side.CLIENT)
	public void onAttackEntity(AttackEntityEvent event) {
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER) return; //lik srsly
		LuaTable e = createEvent(EventName.AttackEntity);
		e.set(3, Utils.entityToTable(event.getTarget()));
		fireEvent(EventName.AttackEntity, e);
	}
	@SubscribeEvent @SideOnly(Side.CLIENT)
	public void onEntityInteract(EntityInteract event) {  //DEAD
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER) return; 
		LuaTable e = createEvent(EventName.EntityInteract);
		e.set(3, Utils.entityToTable(event.getTarget()));
		e.set(4, Utils.itemStackToLuatable(event.getItemStack()));
		e.set(5, event.getHand().equals(EnumHand.MAIN_HAND)?"main hand":"off hand");
		if(event.getFace()!=null)
			e.set(6, LuaValue.valueOf(event.getFace().getName()));
		fireEvent(EventName.EntityInteract, e);
	}
	@SubscribeEvent @SideOnly(Side.CLIENT)
	public void onBlockInteract(PlayerInteractEvent event) { //CONFIRMED MP
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER) return; //lik srsly
		if(event.getFace()==null) return;
		LuaTable e = createEvent(EventName.BlockInteract);
		e.set(3, Utils.blockPosToTable(event.getPos()));
		e.set(4, Utils.itemStackToLuatable(event.getItemStack()));
		e.set(5, event.getHand().equals(EnumHand.MAIN_HAND)?"main hand":"off hand");
		if(event.getFace()!=null)
			e.set(6, LuaValue.valueOf(event.getFace().getName()));
		fireEvent(EventName.BlockInteract, e);
	}

	@SubscribeEvent @SideOnly(Side.CLIENT)
	public void onItemPickup(PlayerEvent.ItemPickupEvent event) { //DEAD //FIXME
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER) return; //lik srsly
		LuaTable e = createEvent(EventName.ItemPickup);
		e.set(3, Utils.itemStackToLuatable(event.getStack()));
		fireEvent(EventName.ItemPickup, e);
	}
	@SubscribeEvent @SideOnly(Side.CLIENT)
	public void onDimChange(PlayerEvent.PlayerChangedDimensionEvent event) { //DEAD //FIXME
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER) return; //lik srsly
		LuaTable e = createEvent(EventName.DimensionChanged);
		e.set(3, LuaValue.valueOf(event.toDim));
		e.set(4, LuaValue.valueOf(event.fromDim));
		fireEvent(EventName.DimensionChanged, e);
	}
	@SubscribeEvent @SideOnly(Side.CLIENT)
	public void onCraft(PlayerEvent.ItemCraftedEvent event) { //CONFIRMED MP
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER) return; //lik srsly
		System.out.println(event.getPhase());
		LuaTable e = createEvent(EventName.ItemCrafted);
		e.set(3, Utils.itemStackToLuatable(event.crafting));
		LuaTable matrix = new LuaTable();
		int size = (int) Math.sqrt(event.craftMatrix.getSizeInventory());
		for(int x = 1; x <= size; x++) {
			LuaTable m = new LuaTable();
			matrix.set(x, m);
			for(int y = 1; y <= size; y++) {
				m.set(y, Utils.itemStackToLuatable( event.craftMatrix.getStackInSlot( (x-1) + (y-1)*size) ));
			}
		}
		e.set(4, matrix);
		fireEvent(EventName.ItemCrafted, e);
	}

	@SubscribeEvent @SideOnly(Side.CLIENT)
	public void onItemToss(ItemTossEvent event) {//FIXME DEAD
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER) return; //lik srsly
		LuaTable e = createEvent(EventName.ItemTossed);
		e.set(3, Utils.itemStackToLuatable(event.getEntityItem().getItem()));
		fireEvent(EventName.ItemTossed, e);
	}

	@SubscribeEvent @SideOnly(Side.CLIENT)
	public void onItemBreak(PlayerDestroyItemEvent event) { //FIXME ULTRA DEAD
		ItemStack yeWhoBrokeith = event.getOriginal(); 
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER) return; //lik srsly
		LuaTable e = createEvent(EventName.BreakItem);
		e.set(3, Utils.itemStackToLuatable(yeWhoBrokeith));
		fireEvent(EventName.BreakItem, e);
	}


	@SubscribeEvent @SideOnly(Side.CLIENT)
	public void onJoinedWorld(FMLNetworkEvent.ClientConnectedToServerEvent event){
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER) return; //lik srsly
		Thread t = new Thread(()->{
			try {
				InputStream in = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(AdvancedMacros.MODID, "scripts/changelogviewer.lua")).getInputStream();
				AdvancedMacros.globals.load(in, "changeLog", "t", AdvancedMacros.globals).call();
				in.close();
			} catch (IOException e) {e.printStackTrace();}
		});
		t.start();

		LuaTable e = createEvent(EventName.JoinWorld);
		e.set(3, event.getConnectionType()); //yeilded modded
		e.set(4, LuaValue.valueOf(event.isLocal()?"SP":"MP")); //yeilded false on localhost multiplayer true on single player
		if(Minecraft.getMinecraft().getCurrentServerData()!=null){
			ServerData sd = Minecraft.getMinecraft().getCurrentServerData();
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
	@SubscribeEvent @SideOnly(Side.CLIENT)
	public void OnLeaveWorld(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER) return; //lik srsly
		LuaTable e = createEvent(EventName.LeaveWorld);
		fireEvent(EventName.LeaveWorld, e);
		lastPlayerList = null;
	}

	@SubscribeEvent @SideOnly(Side.CLIENT)
	public void  onWorldSaved(SaveToFile event) {
		fireEvent(EventName.WorldSaved, createEvent(EventName.WorldSaved));
	}

	private boolean startupHasFired = false;
	@SubscribeEvent
	public void onGuiStartup(GuiScreenEvent.InitGuiEvent.Post sEvent) {
		if(sEvent.getGui().getClass().equals(GuiMainMenu.class)) {
			if(startupHasFired) return;
			Minecraft.getMinecraft().addScheduledTask(()->{
				fireEvent(EventName.Startup,ForgeEventHandler.createEvent(EventName.Startup));
			});
			startupHasFired = true;
		}
	}

	@SubscribeEvent
	public void onGuiOpened(GuiOpenEvent sEvent) {
		GuiScreen sGui = sEvent.getGui();
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
				if(name.startsWith("net.minecraft.client.gui")) {
					name = "Minecraft:"+name.substring(name.lastIndexOf('.'));
				}else {
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
					default:
						break; //defaults to full class name
					}
				}
			}
			if(sGui instanceof GuiContainer) {
				Thread test = new Thread(()->{
					GuiContainer gCon = (GuiContainer) sGui; 
					List<ItemStack> stacks = gCon.inventorySlots.getInventory(); 
					LuaTable e = createEvent(EventName.ContainerOpen);
					LuaTable items = new LuaTable();
					if(stacks!=null) {
						for(int i = 0; i<stacks.size(); i++) {
							items.set(i+1, Utils.itemStackToLuatable(stacks.get(i)));
						}
						e.set(3, items);
					}else{
						e.set(3, LuaValue.FALSE);
					}
					System.out.println(Minecraft.getMinecraft().ingameGUI.getClass());
					fireEvent(EventName.ContainerOpen, e);

				});
				test.start();

			}
			LuaTable args = createEvent(EventName.GUIOpened);
			args.set(3, name);
			fireEvent(EventName.GUIOpened, args);

		}
	}
	
	@SubscribeEvent //TODO
	public void sendingChat(ClientChatEvent event) {
		LuaTable e = createEvent(EventName.ChatSendFilter);
		e.set(3, LuaValue.valueOf(event.getMessage()));
		LuaValue maxTime       = Utils.tableFromProp(Settings.settings, "chat.maxFilterTime", LuaValue.valueOf(500));
		LuaValue timeoutAciton = Utils.tableFromProp(Settings.settings, "chat.cancelOnTimeout", LuaValue.TRUE);
		
	}

	@SubscribeEvent @SideOnly(Side.CLIENT)
	public void onUseItem( LivingEntityUseItemEvent event) {//CONFIRMED MP
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER) return; //lik srsly
		if(!event.getEntity().equals(Minecraft.getMinecraft().player)) return;
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

	@SubscribeEvent
	public void onSound(PlaySoundEvent pse) {
		LuaTable event = createEvent(EventName.Sound);
		event.set(3, pse.getName());

		fireEvent(EventName.Sound, event);
		LuaTable controls = new LuaTable();
		controls.set("isPlaying", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(pse.getManager().isSoundPlaying(pse.getSound()));
			}
		});
		controls.set("stop", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				pse.getManager().stopSound(pse.getSound());
				return NONE;
			}
		});
		event.set(4, controls);
	}

	@SubscribeEvent @SideOnly(Side.CLIENT)
	public void onChat(ClientChatReceivedEvent sEvent){//TODO out going chat msg filter
		final ClientChatReceivedEvent event = sEvent; //arg not final because it's acquired thru reflection
		LuaTable e = createEvent(EventName.Chat);
		LuaTable e2 = createEvent(EventName.ChatFilter);
		String unformated = event.getMessage().getUnformattedText();
		String formated   = "&f"+event.getMessage().getFormattedText()
				.replaceAll("&", "&&")
				.replaceAll("\u00A7", "&")
				.replaceAll("&k", "&O") //Obfuscated
				.replaceAll("&l", "&B") //Bold
				.replaceAll("&m", "&S") //Strikethru
				.replaceAll("&o", "&I") //Italics
				.replaceAll("&r", "&f")   //reset (to white in this case)
				;
		formated = formated.substring(0, formated.length()-2);//gets rid of last &f that does nothing for us
		//System.out.println(sEvent.getMessage().getSiblings());
		//TODO simplfy formating
		e.set(3, formated);
		e2.set(3, formated);
		e.set(4, unformated);
		e2.set(4,unformated);
		event.setCanceled(event.isCancelable() && eventExists(EventName.ChatFilter));
		//		OnScriptFinish afterFormating = new OnScriptFinish() {
		//			@Override
		//			public void onFinish(Varargs v) {
		//				if(v.narg()>0){
		//					event.setMessage(advancedMacros.logFunc.formatString(v));
		//				}
		//			}
		//		};
		fireEvent(EventName.Chat, e);
		if(event.isCanceled())
			fireEvent(EventName.ChatFilter, e2);

	}



	@SubscribeEvent @SideOnly(Side.CLIENT)
	public void onItemPickup(EntityItemPickupEvent ipe){
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER) return;
		LuaTable e = createEvent(EventName.ItemPickup);
		e.set(3, ipe.getEntityPlayer().getName());
		e.set(4, Utils.itemStackToLuatable(ipe.getItem().getItem()));
		fireEvent(EventName.ItemPickup, e);
	}


	//+===================================================================================================+
	//|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~END OF FORGE EVENTS~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|
	//+===================================================================================================+
	//not including this world render, but its not for the trigger list

	private void resetItemDurablitity(){
		ItemStack i = Minecraft.getMinecraft().player.getHeldItemMainhand();
		lastItemDurablity = i.getMaxDamage()-i.getItemDamage();
	}
	private void resetLastStats(){
		EntityPlayerSP player = Minecraft.getMinecraft().player;
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
			int temp = armor.get(i).getMaxDamage() - armor.get(i).getItemDamage();
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
	@SubscribeEvent @SideOnly(Side.CLIENT)
	public void onLastWorldRender(RenderWorldLastEvent rwle){
		//		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, AdvancedMacros.modelView3d);
		//		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, AdvancedMacros.projView3d);


		//double x,y,z,uMin,vMin,uMax,vMax, wid, hei;
		float p = Minecraft.getMinecraft().getRenderPartialTicks();
		Entity player = Minecraft.getMinecraft().player;

		GlStateManager.pushAttrib();
		GlStateManager.enableCull();
		GlStateManager.cullFace(CullFace.BACK);
		GlStateManager.enableBlend();


		//src color -> src color?
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);


		//GlStateManager.enableLighting();

		synchronized (worldHudItems) {
			for (WorldHudItem worldHudItem : worldHudItems) {
				//System.out.println(worldHudItem);
				if(worldHudItem.getDrawType().isXRAY()){
					GlStateManager.disableDepth();
				}else{
					GlStateManager.enableDepth();
				}
				GlStateManager.color(1, 1, 1, worldHudItem.getOpacity());
				worldHudItem.render(accuPlayerX(p, player), accuPlayerY(p, player), accuPlayerZ(p, player));
			}
		}
		GlStateManager.disableBlend();//F1 is black otherwise
		GlStateManager.popAttrib();
	}

	@SubscribeEvent
	public void afterOverlay(RenderGameOverlayEvent.Post event) {

		float p = Minecraft.getMinecraft().getRenderPartialTicks();
		//Entity player = Minecraft.getMinecraft().player;

		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

		GlStateManager.disableBlend();
		GlStateManager.enableBlend();
		//GL11.glEnable(GL11.GL_BLEND);
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		//GlStateManager.enableBlend();
		//GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 1);
		GlStateManager.disableAlpha();
		//		GlStateManager.enableAlpha();
		GlStateManager.bindTexture(0);
		//GlStateManager.enableLighting();

		synchronized (hud2DItems) {
			for (Hud2DItem hudItem : hud2DItems) {
				//System.out.println(worldHudItem);
				GlStateManager.color(1, 1, 1, hudItem.getOpacity()/255f);
				hudItem.render(p);
			}
		}
		//GlStateManager.disableBlend();//F1 is black otherwise
		GL11.glPopAttrib();
		//GlStateManager.color(0, 0, 0, 0);
		GlStateManager.color(1, 1, 1, 1);
		//GlStateManager.disableBlend();
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.disableAlpha();
		GlStateManager.enableAlpha();
		GlStateManager.disableBlend();
		GlStateManager.enableBlend();
		GlStateManager.color(1, 1, 1, 1);
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
		AdvancedMacros.macroMenuGui.fireEvent(false, event.name(), args.unpack(), false);
	}
	public void fireEvent(String eventString, LuaTable args){
		AdvancedMacros.macroMenuGui.fireEvent(false, eventString, args.unpack(), false);
	}
	public void fireEvent(EventName event, LuaTable args, OnScriptFinish onScriptFinish){
		AdvancedMacros.macroMenuGui.fireEvent(false, event.name(), args.unpack(), false, onScriptFinish);
	}
	public boolean eventExists(EventName eName){
		return AdvancedMacros.macroMenuGui.doesEventExist(eName.name());
	}


	/*the main menu for this mod*/
	public static void showMenu(){
		showMenu(AdvancedMacros.macroMenuGui);
	}
	public static void closeMenu() {
		Minecraft.getMinecraft().displayGuiScreen(null);
	}
	public static void showMenu(Gui gui){
		if(gui==null){
			showMenu();
		}
		AdvancedMacros.lastGui = gui;//the one to return to on open, not prev gui
		Minecraft.getMinecraft().displayGuiScreen(gui);
		gui.onOpen();

	}
	public static void showMenu(Gui gui, Gui prevGui){
		Minecraft.getMinecraft().displayGuiScreen(gui);
		AdvancedMacros.prevGui = prevGui;
		AdvancedMacros.lastGui = gui;
		gui.onOpen();
	}
	public static void showPrevMenu(){
		if(AdvancedMacros.prevGui==null){
			AdvancedMacros.prevGui=AdvancedMacros.macroMenuGui;
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
				worldHudItems.getFirst().destroy(); //removed when disable draw is called
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
				hud2DItems.getLast().destroy();
			}
			hud2DItems.clear();
		}
	}

	public int getSTick() {
		return sTick;
	}

	private Look look;
	public void lookTo(float sYaw, float sPitch, long time) {
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		look = new Look(player.rotationYawHead, player.rotationPitch,sYaw, sPitch, time);

	}
	private class Look{
		float fromYaw, fromPitch;
		float toYaw, toPitch;
		long time, start;
		public Look(float fromYaw, float fromPitch, float toYaw, float toPitch, long time) {
			super();
			this.fromYaw = fromYaw;
			this.fromPitch = fromPitch;
			this.toYaw = toYaw;
			this.toPitch = toPitch;

			double b = toYaw%360-fromYaw%360;
			double a = fromYaw%360+360-toYaw%360;
			if(a<b){//better to turn other way
				fromYaw+=360;
				//System.out.println("Reverse spin");
			}else if(fromYaw%360==toYaw%360){ //same do nothing really
				fromYaw=toYaw;
			}

			start = System.currentTimeMillis();
			this.time = time;
		}
		public void look(){
			if(System.currentTimeMillis()<=time+start){
				EntityPlayerSP player = Minecraft.getMinecraft().player;


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
		int keyCode;
		long releaseTime;
		boolean done = false; //removal flag
		public HeldKeybinds(int keyCode, long releaseTime) {
			super();
			this.keyCode = keyCode;
			this.releaseTime = releaseTime;
		}

	}
	public ConcurrentHashMap<Integer, Boolean> getHeldKeys() {
		return heldKeys;
	}
	public ArrayList<Boolean> getHeldMouseButtons() {
		return heldMouseButtons;
	}

	private void populatePlayerList(ConcurrentHashMap<String, Boolean> map) {
		Minecraft mc = Minecraft.getMinecraft();
		Iterator<NetworkPlayerInfo> iter = mc.getConnection().getPlayerInfoMap().iterator();
		while(iter.hasNext()) {
			NetworkPlayerInfo playerInfo = iter.next();
			String name = mc.ingameGUI.getTabList().getPlayerName(playerInfo);
			String formated   = name
					.replaceAll("&", "&&")
					.replaceAll("\u00A7", "&")
					.replaceAll("&k", "&O") //Obfuscated
					.replaceAll("&l", "&B") //Bold
					.replaceAll("&m", "&S") //Strikethru
					.replaceAll("&o", "&I") //Italics
					.replaceAll("&r", "&f")   //reset (to white in this case)
					;
			if(name!=null)
				map.put(formated.replaceAll("&[^&]", "").replaceAll("&&", "&"), true);
		}
	}

	//private LinkedList<HeldKeybinds> heldKeybinds = new LinkedList<>();
	ConcurrentHashMap<Integer, HeldKeybinds> keyBindReleaseMap = new ConcurrentHashMap<>();
	public void releaseKeybindAt(int keycode, long l) {
		//heldKeybinds.add(new HeldKeybinds(keycode, l));
		keyBindReleaseMap.put(keycode, new HeldKeybinds(keycode, l));
	}

	public class GetHeldKeys extends ZeroArgFunction {
		@Override
		public LuaValue call() {
			LuaTable t = new LuaTable();
			int j = 1;
			for(int i : heldKeys.keySet()) {
				t.set(j++, Keyboard.getKeyName(i));
			}
			for(int i = 0; i < heldMouseButtons.size(); i++) {
				String s;
				switch (i) {
				case 0:
					s = "LMB";
					break;
				case 1:
					s = "RMB";
					break;
				case 2:
					s = "MMB";
					break;
				default:
					s = "MOUSE:"+i;
					break;
				}
				if(heldMouseButtons.get(i))
					t.set(j++, s);
			}
			return t;
		}
	}
}
