package com.theincgi.advancedmacros.event;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.theincgi.advancedmacros.AdvancedMacros;
import com.theincgi.advancedmacros.access.IEntity;
import com.theincgi.advancedmacros.gui.EditorGUI;
import com.theincgi.advancedmacros.gui.Gui;
import com.theincgi.advancedmacros.gui.MacroMenuGui;
import com.theincgi.advancedmacros.gui.RunningScriptsGui;
import com.theincgi.advancedmacros.gui.elements.ColorTextArea;
import com.theincgi.advancedmacros.gui2.ScriptBrowser2;
import com.theincgi.advancedmacros.hud.hud2D.Hud2DItem;
import com.theincgi.advancedmacros.hud.hud3D.WorldHudItem;
import com.theincgi.advancedmacros.lua.LuaDebug;
import com.theincgi.advancedmacros.lua.LuaText;
import com.theincgi.advancedmacros.lua.functions.GuiControls;
import com.theincgi.advancedmacros.misc.HIDUtils;
import com.theincgi.advancedmacros.misc.Pair;
import com.theincgi.advancedmacros.misc.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.Camera;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundInstanceListener;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;


public class EventHandler {

    int lastAir, lastHealth,
            lastItemDurablity, lastHotbar, lastHunger;
    boolean playerWasNull = true, lastSleepingState = false;
    private boolean wasSneaking = false;
    //added lastSwingProgress even though it exists in the player because it would fire multiple times
    float lastSaturation, lastSwingProgress;
    ItemStack lastHeldItem;
    //int lastDimension;
    int[] lastArmourDurability = new int[4];
    int lastXP, lastXPLevel;
    DimensionType lastDim;
    boolean wasRaining, wasThundering;
    private ArrayList<Boolean> heldMouseButtons;
    //Keeping this syncronized!
    private final LinkedList<WorldHudItem> worldHudItems = new LinkedList<>();
    private final LinkedList<Hud2DItem> hud2DItems = new LinkedList<>();
    private int sTick = 0;
    private final Object sTickSync = new Object();
    private ConcurrentHashMap<UUID, String> lastPlayerList = new ConcurrentHashMap<>();
    private ConcurrentHashMap<UUID, String> nowPlayerList = new ConcurrentHashMap<>();
    public WeakHashMap<Entity, RenderFlags> entityRenderFlags = new WeakHashMap<>();
    private boolean wasOnFire = false;
    private HashMap<Integer, Integer> repeatingKeys = new HashMap<>();
    private Object tickLock = new Object();

    //private Queue<List<ItemStack>> receivedInventories = new LinkedList<>();
    public EventHandler() {
        heldMouseButtons = new ArrayList<>(HIDUtils.Mouse.getButtonCount());
        for (int i = 0; i < HIDUtils.Mouse.getButtonCount(); i++) {
            heldMouseButtons.add(false);
        }
    }

    public void onChatEvent(Text message, EventName eventName) {

        LuaTable chatEvent = createEvent(EventName.Chat);
        LuaTable chatFilterEvent = createEvent(EventName.ChatFilter);
        String unformatted = message.getString();

        MutableText mutableText = Text.literal("");
        mutableText.append(message);
        LuaText formatted = new LuaText(message);


        chatEvent.set(3,formatted);
        chatFilterEvent.set(3, formatted);
        chatEvent.set(4, unformatted);
        chatFilterEvent.set(4, unformatted);
        AdvancedMacros.macroMenuGui.fireEvent(false, eventName.name(), chatEvent.unpack(),false, null);
    }

    public static enum EventName {
        Chat,                    //COMPLETE
        ChatFilter,                //COMPLETE
        ChatSendFilter,
        Title,
        Actionbar,
        //LoggedIn,
        //LoggedOut,
        JoinWorld,                //COMPLETE
        LeaveWorld,
        Respawn,                //COMPLETE
        Death,                    //COMPLETE
        HealthChanged,            //COMPLETE
        HungerChanged,            //COMPLETE
        SaturationChanged,        //COMPLETE
        AirChanged,                //COMPLETE
        DimensionChanged,       //COMPLETE
        //DamageTaken, //healthChanged covers this
        ItemPickup,
        ItemCrafted,
        //ItemSmelted,this is server side
        HotbarChanged, //slot changed					//COMPLETE
        PotionStatus,
        Weather,                //COMPLETE
        PlayerIgnited, //ouch
        //FOVChanged, //does this need to be an event?
        GUIOpened,
        GUIClosed,
        //AnvilUpdate, //server event
        //PotionBrewed, //server event
        ItemTossed,
        WorldSaved,
        ArrowFired,                //COMPLETE
        AttackEntity,
        EntityInteract,
        BlockInteract,
        //showTooltip, Usage?
        ContainerOpen,
        UseBed,
        WakeUp,
        UseItem, //start, stop and finish
        BreakItem,
        ArmourDurability,
        ItemDurability,
        PlayerJoin,
        PlayerLeave,
        XP,                        //COMPLETE
        //PlayerDropItems, //server event
        //EntityDropItems, //server event
        AttackReady,
        ProfileLoaded,
        Sound,
        Startup, //right after everything is loaded, custom libraries should go here!
        Anything //also includes key event or mouse event,
        ;//	TEST;
    }

    public static void onTick(MinecraftClient client) {
        TaskDispatcher.runTasks();
    }

    public void onKeyInput(int key, int scancode, int action, int mods) {
        if (AdvancedMacros.modKeybind.isPressed()) {
            if (ColorTextArea.isCTRLDown()) {
                if (ColorTextArea.isShiftDown()) {
                    AdvancedMacros.stopAll();
                } else {
                    MinecraftClient.getInstance().setScreen(AdvancedMacros.runningScriptsGui);
                }
            } else if (ColorTextArea.isShiftDown()) {
                showMenu(AdvancedMacros.scriptBrowser2, AdvancedMacros.macroMenuGui.getGui());
            } else if (HIDUtils.Keyboard.isAlt()) {
                LuaDebug.LuaThread thread = new LuaDebug.LuaThread(AdvancedMacros.repl, "REPL");
                thread.start();
            } else {
                if (AdvancedMacros.lastGui != null) {
                    TaskDispatcher.delayTask(() -> {
                        AdvancedMacros.lastGui.showGui();
                    }, 85);
                } else {
                    MacroMenuGui.showMenu();
                }
            }
            return;
        }

        Screen s = MinecraftClient.getInstance().currentScreen;
        if (action == GLFW.GLFW_PRESS) {
            repeatingKeys.put(scancode, 0);
        }
        if (action == GLFW.GLFW_REPEAT && s == null) {
            return;
        }
        if (s != null) {
            if (s instanceof Gui g) {
                int n;
                repeatingKeys.put(scancode, n = (repeatingKeys.getOrDefault(scancode, 0) + 1));
                g.onKeyRepeated(g, key, scancode, mods, n);
            }
            return; } //Keyboard.onKey(eventKey, event.getAction());

        LuaTable eventDat = new LuaTable();
        eventDat.set(1, "key");
        String name = HIDUtils.Keyboard.nameOf(key);
        eventDat.set(2, name.equals(HIDUtils.Keyboard.UNKNOWN_KEY_NAME) ? HIDUtils.Mouse.nameOf(key) : name );
        eventDat.set(3, LuaValue.valueOf(action == GLFW.GLFW_PRESS ? "down" : "up"));
        eventDat.set(4, LuaValue.valueOf(key));
        AdvancedMacros.macroMenuGui.fireEvent(true, HIDUtils.Keyboard.nameOf(key), eventDat.unpack(), action == GLFW.GLFW_PRESS, null);
    }

    public void onMouseClick(int mButton, boolean state) {
//        System.out.println("MOUSE FIRED");
        String buttonName = switch (mButton) {
            case 0 -> "LMB";
            case 1 -> "RMB";
            case 2 -> "MMB";
            default -> "MOUSE:" + mButton;
        };
        LuaTable eDat = new LuaTable();
        eDat.set(1, "mouse");
        eDat.set(2, buttonName);
        eDat.set(3, state ? "down" : "up");
        AdvancedMacros.macroMenuGui.fireEvent(true, buttonName, eDat.unpack(), state, null);
    }


    MinecraftClient minecraft = MinecraftClient.getInstance();
    public void onPlayerTick() {
        synchronized (sTickSync) {
            sTick++;
            synchronized (tickLock) {
                tickLock.notifyAll();
            }
        }

        LinkedList<InputUtil.Key> toRemove = new LinkedList<>();
        for ( InputUtil.Key i : keyBindReleaseMap.keySet()) {
            HeldKeybinds hk = keyBindReleaseMap.get(i);
            if (hk.releaseTime < System.currentTimeMillis()) {
                KeyBinding.setKeyPressed(i, false);
                hk.done = true; //is this even needed anymore?
                toRemove.add(i);
            }

            GameOptions sets = minecraft.options;
            InputUtil.Key attackKey = sets.attackKey.getDefaultKey();
            if (hk.input.equals(attackKey)) {
                MinecraftClient minecraft = AdvancedMacros.getMinecraft();
                if (minecraft.crosshairTarget != null && minecraft.crosshairTarget.getType() == net.minecraft.util.hit.HitResult.Type.BLOCK) {
                    BlockHitResult blockHitResult = (BlockHitResult)minecraft.crosshairTarget;
                    BlockPos blockPos = blockHitResult.getBlockPos();
                    if (!minecraft.world.getBlockState(blockPos).isAir()) {
                        Direction direction = blockHitResult.getSide();
                        if (minecraft.interactionManager.updateBlockBreakingProgress(blockPos, direction)) {
                            minecraft.particleManager.addBlockBreakingParticles(blockPos, direction);
                            minecraft.player.swingHand(Hand.MAIN_HAND);
                        }
                    }
                } else {
                    minecraft.interactionManager.cancelBlockBreaking();
                }
            }
        }
        while (!toRemove.isEmpty()) {
            keyBindReleaseMap.remove(toRemove.pop());
        }
    }


/*


    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase.equals(TickEvent.Phase.START)) {
            return; //only do on the second half of tick after all stuff happens
        }

        for (int i = 0; i < Mouse.getButtonCount(); i++) {
            boolean b = Mouse.isDown(i);
            if (b != heldMouseButtons.get(i)) {
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
        if (look != null) {
            look.look();
        }
        AdvancedMacros.actions.checkBlockBreakStatus();
        LinkedList<net.minecraft.client.util.InputMappings.Input> toRemove = new LinkedList<>();
        for (net.minecraft.client.util.InputMappings.Input i : keyBindReleaseMap.keySet()) {
            HeldKeybinds hk = keyBindReleaseMap.get(i);
            if (hk.releaseTime < System.currentTimeMillis()) {
                KeyBinding.setKeyPressed(i, false);
                hk.done = true; //is this even needed anymore?
                toRemove.add(i);
            }
        }
        while (!toRemove.isEmpty()) {
            keyBindReleaseMap.remove(toRemove.pop());
        }

        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            playerWasNull = true;
            return;
        }
        if (playerWasNull) {
            resetLastStats();
            playerWasNull = false;
        }

        if (!player.dimension.equals(lastDim)) {
            if (lastDim != null) {
                LuaTable e = createEvent(EventName.DimensionChanged);
                e.set(3, Utils.toTable(player.dimension));
                e.set(4, Utils.toTable(lastDim));
                fireEvent(EventName.DimensionChanged, e);
            }
            lastDim = player.dimension;
        }
        if (player.isSleeping() != lastSleepingState) {
            if (player.isSleeping()) {
                fireEvent(EventName.UseBed, createEvent(EventName.UseBed));
            } else {
                fireEvent(EventName.WakeUp, createEvent(EventName.WakeUp));
            }
            lastSleepingState = player.isSleeping();
        }

        //AirChanged
        int currentAir = player.getAir() / 3;
        if (currentAir != lastAir) {
            LuaTable e = createEvent(EventName.AirChanged);
            e.set(3, LuaValue.valueOf(currentAir));
            e.set(4, LuaValue.valueOf(currentAir - lastAir));
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
            if (temp != lastArmourDurability[i]) {
                armorChanged = true;
            }
            armorDurability[i] = temp;
        }
        if (armorChanged) {
            LuaTable e = createEvent(EventName.ArmourDurability);
            LuaTable current = new LuaTable();
            LuaTable change = new LuaTable();
            e.set(3, current);
            e.set(4, change);
            for (int i = 0; i < armorDurability.length; i++) {
                current.set(i + 1, LuaValue.valueOf(armorDurability[i]));
                change.set(i + 1, LuaValue.valueOf(armorDurability[i] - lastArmourDurability[i]));
                lastArmourDurability[i] = armorDurability[i];
            }
            fireEvent(EventName.ArmourDurability, e);
        }

        int potionUpdateFrequency;
        try {
            potionUpdateFrequency = Utils.tableFromProp(Settings.settings, "events.potionStatusFrequency", LuaValue.valueOf(20)).checkint();
        } catch (Exception e) {
            Settings.settings.get("events").set("potionStatusFrequency", LuaValue.valueOf(20));
            potionUpdateFrequency = 20;
        }
        for (EffectInstance e : player.getActivePotionEffects()) {
            LuaTable evnt = createEvent(EventName.PotionStatus);
            int dur = e.getDuration() - 1;
            int ddur = (dur) / potionUpdateFrequency;

            if (dur % potionUpdateFrequency == 0) {
                evnt.set(3, Utils.effectToTable(e));
                fireEvent(EventName.PotionStatus, evnt);
            }

        }

        if (wasOnFire != player.isBurning()) {
            wasOnFire = player.isBurning();
            LuaTable e = createEvent(EventName.PlayerIgnited);
            e.set(3, LuaValue.valueOf(player.isBurning()));
            fireEvent(EventName.PlayerIgnited, e); //no pun intended
        }

        //health
        int health = (int) (player.getHealth());
        if (lastHealth != health) {
            if (lastHealth == 0 && health >= 1) {
                fireEvent(EventName.Respawn, createEvent(EventName.Respawn));
            }
            LuaTable e = createEvent(EventName.HealthChanged);
            e.set(3, LuaValue.valueOf(health));
            e.set(4, LuaValue.valueOf(health - lastHealth));
            if (player.getLastDamageSource() != null) {
                e.set(5, LuaValue.valueOf(player.getLastDamageSource().damageType));
            }
            fireEvent(EventName.HealthChanged, e);

            if (health == 0) {
                fireEvent(EventName.Death, createEvent(EventName.Death));
            }

            lastHealth = health;
        }
        //hotbar
        int hotbar = player.inventory.currentItem;
        if (lastHotbar != hotbar) {
            LuaTable e = createEvent(EventName.HotbarChanged);
            e.set(3, LuaValue.valueOf(hotbar + 1));
            fireEvent(EventName.HotbarChanged, e);
            lastHotbar = hotbar;
            resetItemDurablitity();
        }
        //hunger
        //player.getFoodStats()
        int hunger = player.getFoodStats().getFoodLevel();
        if (lastHunger != hunger) {
            LuaTable e = createEvent(EventName.HungerChanged);
            e.set(3, LuaValue.valueOf(hunger));
            e.set(4, LuaValue.valueOf(hunger - lastHunger));
            lastHunger = hunger;
            fireEvent(EventName.HungerChanged, e);
        }
        float saturation = player.getFoodStats().getSaturationLevel();
        if (lastSaturation != saturation) {
            LuaTable e = createEvent(EventName.SaturationChanged);
            e.set(3, LuaValue.valueOf(saturation));
            e.set(4, LuaValue.valueOf(saturation - lastSaturation));
            fireEvent(EventName.SaturationChanged, e);
            lastSaturation = saturation;
        }
        if (lastSwingProgress != 0 && player.swingProgress == 0 && !player.isSwingInProgress) {
            fireEvent(EventName.AttackReady, createEvent(EventName.AttackReady));
        }
        lastSwingProgress = player.swingProgress;
        boolean rain = MinecraftClient.getInstance().world.isRaining();
        boolean thunder = MinecraftClient.getInstance().world.isThundering();
        if (rain != wasRaining || wasThundering != thunder) {
            wasRaining = rain;
            wasThundering = thunder;
            LuaTable e = createEvent(EventName.Weather);
            String stat;
            if (rain && thunder) {
                stat = "thunder";
            } else if (rain && !thunder) {
                stat = "rain";
            } else if (!rain && thunder) {
                stat = "only thunder";
            } else {
                stat = "clear";
            }

            e.set(3, stat);
            fireEvent(EventName.Weather, e);
        }
        if (lastHeldItem == null || lastHeldItem.isEmpty()) { //skip on pickup or create or w/e
            resetItemDurablitity();
            lastHeldItem = player.getMainHandStack();
        }
        int currentDura = player.getMainHandStack().getMaxDamage() - player.getMainHandStack().getDamage();
        if (currentDura != lastItemDurablity && !player.getMainHandStack().isEmpty()) {
            LuaTable e = createEvent(EventName.ItemDurability);
            e.set(3, Utils.itemStackToLuatable(player.getMainHandStack()));
            e.set(4, LuaValue.valueOf(currentDura - lastItemDurablity));
            fireEvent(EventName.ItemDurability, e);
            resetItemDurablitity();
        }

        int thisXP = player.totalExperience;
        int thisXPLevel = player.experienceLevel;
        if (thisXP != lastXP || thisXPLevel != this.lastXPLevel) {
            LuaTable e = createEvent(EventName.XP);
            e.set(3, LuaValue.valueOf(thisXP));
            e.set(4, LuaValue.valueOf(thisXPLevel));
            e.set(5, LuaValue.valueOf(thisXP - lastXP));
            e.set(6, LuaValue.valueOf(thisXPLevel - lastXPLevel));
            fireEvent(EventName.XP, e);
            lastXP = thisXP;
            lastXPLevel = thisXPLevel;
        }

        if (lastPlayerList == null) {
            lastPlayerList = new ConcurrentHashMap<>();
            populatePlayerList(lastPlayerList);
        }
        nowPlayerList.clear();
        populatePlayerList(nowPlayerList);
        //System.out.println(nowPlayerList);
        for (UUID uuid : nowPlayerList.keySet()) {
            if (!lastPlayerList.containsKey(uuid)) {
                //player joined
                String val = nowPlayerList.get(uuid);
                if (val == null) {
                    continue;
                }
                LuaTable e = createEvent(EventName.PlayerJoin);
                e.set(3, val);
                fireEvent(EventName.PlayerJoin, e);
                lastPlayerList.put(uuid, val);
            }
        }
        for (UUID uuid : lastPlayerList.keySet()) {
            //	System.out.println("Checking: "+s);
            if (!nowPlayerList.containsKey(uuid)) {
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
            MinecraftClient mc = MinecraftClient.getInstance();
            if (titlesTimer == null) { //special thanks to "MCP Mapping Viewer" by bspkrs
                titlesTimer = ObfuscationReflectionHelper.findField(IngameGui.class, "field_175195_w"); //checked for 1.14.3
                titleDisplayTime = ObfuscationReflectionHelper.findField(IngameGui.class, "field_175192_A");
                titleFadeInTime = ObfuscationReflectionHelper.findField(IngameGui.class, "field_175199_z");
                titleFadeOutTime = ObfuscationReflectionHelper.findField(IngameGui.class, "field_175193_B");
                title = ObfuscationReflectionHelper.findField(IngameGui.class, "field_175201_x");
                subtitle = ObfuscationReflectionHelper.findField(IngameGui.class, "field_175200_y");
                actionbarTimer = ObfuscationReflectionHelper.findField(IngameGui.class, "field_73845_h");
                actionbarText = ObfuscationReflectionHelper.findField(IngameGui.class, "field_73838_g");
                isColorized = ObfuscationReflectionHelper.findField(IngameGui.class, "field_73844_j");
            }
            IngameGui gui = mc.ingameGUI;
            {
                int disp = titleDisplayTime.getInt(gui);
                int fadeIn = titleFadeInTime.getInt(gui);
                int fadeOut = titleFadeOutTime.getInt(gui);
                int timer = titlesTimer.getInt(gui);
                if (timer == (fadeIn + fadeOut + disp) - 1) {
                    //AdvancedMacros.logFunc.call("&d&BDEBUG:&7 A new title has been displayed!");
                    String titleText, subtitleText;
                    titleText = Utils.fromMinecraftColorCodes((String) title.get(gui));
                    subtitleText = Utils.fromMinecraftColorCodes((String) subtitle.get(gui));

                    if (titleText.endsWith("&f")) {
                        titleText = titleText.substring(0, titleText.length() - 2);
                    }
                    if (subtitleText.endsWith("&f")) {
                        subtitleText = subtitleText.substring(0, subtitleText.length() - 2);
                    }

                    LuaTable event = createEvent(EventName.Title);
                    event.set(3, LuaValue.valueOf(titleText));
                    event.set(4, LuaValue.valueOf(subtitleText));
                    event.set(5, disp);
                    event.set(6, fadeIn);
                    event.set(7, fadeOut);
                    fireEvent(EventName.Title, event);
                }
            }
            {
                int timer = actionbarTimer.getInt(gui);
                if (timer == 59) { //hardcoded value from the class is 60, we see it one tick later
                    //AdvancedMacros.logFunc.call("&d&BDEBUG:&7 A new actionbar has been displayed!");
                    LuaTable event = createEvent(EventName.Actionbar);
                    String text = Utils.fromMinecraftColorCodes((String) actionbarText.get(gui));
                    if (text.endsWith("&f")) {
                        text = text.substring(0, text.length() - 2);
                    }
                    event.set(3, LuaValue.valueOf(text));
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
    public void onArrowFired(ArrowLooseEvent event) {//CONFIRMED MP

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
    //		PlayerEntity player = MinecraftClient.getInstance().player;
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
        e.set(5, event.getHand().equals(Hand.MAIN_HAND) ? "main hand" : "off hand");
        if (event.getFace() != null) {
            e.set(6, LuaValue.valueOf(event.getFace().getName()));
        }
        fireEvent(EventName.EntityInteract, e);
    }

    @SubscribeEvent
    public void onBlockInteract(PlayerInteractEvent event) {

        if (event.getFace() == null) {
            return;
        }
        LuaTable e = createEvent(EventName.BlockInteract);
        e.set(3, Utils.blockPosToTable(event.getPos()));
        e.set(4, Utils.itemStackToLuatable(event.getItemStack()));
        e.set(5, event.getHand().equals(Hand.MAIN_HAND) ? "main hand" : "off hand");
        if (event.getFace() != null) {
            e.set(6, LuaValue.valueOf(event.getFace().getName()));
        }
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
        for (int x = 1; x <= size; x++) {
            LuaTable m = new LuaTable();
            matrix.set(x, m);
            for (int y = 1; y <= size; y++) {
                m.set(y, Utils.itemStackToLuatable(event.getInventory().getStackInSlot((x - 1) + (y - 1) * size)));
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
    public void onJoinedWorld(PlayerEvent.PlayerLoggedInEvent event) { //EntityJoinWorldEvent, GatherLoginPayloadsEvent, PlayerLoggedInEvent

        TaskDispatcher.delayTask(() -> {
            OpenChangeLog.openChangeLog(false);
        }, 1500);

        MinecraftClient mc = MinecraftClient.getInstance();
        LuaTable e = createEvent(EventName.JoinWorld);
        if (mc.getConnection() != null && mc.getConnection().getNetworkManager() != null) {
            e.set(3, NetworkHooks.getConnectionType(() -> mc.getConnection().getNetworkManager()).name()); //yeilded modded
        } else {
            e.set(3, ConnectionType.MODDED.name());
        }
        e.set(4, mc.isSingleplayer() ? "SP" : "MP");
        if (MinecraftClient.getInstance().getCurrentServerData() != null) {
            ServerData sd = MinecraftClient.getInstance().getCurrentServerData();
            if (sd != null) {
                e.set(5, sd.serverName == null ? LuaValue.FALSE : LuaValue.valueOf(sd.serverName));
                e.set(6, sd.serverMOTD == null ? LuaValue.FALSE : LuaValue.valueOf(sd.serverMOTD));
                e.set(7, sd.serverIP == null ? LuaValue.FALSE : LuaValue.valueOf(sd.serverIP));
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
    public void onWorldSaved(SaveToFile event) {
        fireEvent(EventName.WorldSaved, createEvent(EventName.WorldSaved));
    }

    private boolean startupHasFired = false;

    @SubscribeEvent
    public void onGameStart(GuiScreenEvent.InitGuiEvent.Post sEvent) {
        if (sEvent.getGui().getClass().equals(MainMenuScreen.class)) {
            if (startupHasFired) {
                return;
            }
            //TaskDispatcher.addTask(()->{
            fireEvent(EventName.Startup, ForgeEventHandler.createEvent(EventName.Startup));
            //});
            startupHasFired = true;
        }
    }

    @SubscribeEvent
    public void onUseItem(LivingEntityUseItemEvent event) {//CONFIRMED MP
        //lik srsly
        if (!event.getEntity().equals(MinecraftClient.getInstance().player)) {
            return;
        }
        int useItemFrequency = 1;
        try {
            useItemFrequency = Utils.tableFromProp(Settings.settings, "events.useItemFrequency", LuaValue.valueOf(20)).checkint();
        } catch (Exception e) {
            Settings.settings.get("events").set("useItemFrequency", LuaValue.valueOf(20));
            useItemFrequency = 20;
        }
        if (event.getDuration() % useItemFrequency != 0 && event.getClass().getSimpleName().toLowerCase().equals("tick")) {
            return;
        }
        LuaTable e = createEvent(EventName.UseItem);
        e.set(3, Utils.itemStackToLuatable(event.getItem()));
        e.set(4, LuaValue.valueOf(event.getDuration()));
        e.set(5, event.getClass().getSimpleName().toLowerCase());
        fireEvent(EventName.UseItem, e);
    }*/

    public void onGuiOpened(Screen sGui) {
        if (sGui == null) {
            AdvancedMacros.EVENT_HANDLER.fireEvent(EventName.GUIClosed, createEvent(EventName.GUIClosed));
        } else {
            String name = "";
            if (sGui instanceof MacroMenuGui) {
                name = "AdvancedMacros:BindingsMenu";
            } else if (sGui instanceof ScriptBrowser2) {
                name = "AdvancedMacros:ScriptBrowser";
            } else if (sGui instanceof RunningScriptsGui) {
                name = "AdvancedMacros:RunningScripts";
            } else if (sGui instanceof EditorGUI) {
                name = "AdvancedMacros:Editor";
            } else if (sGui instanceof AbstractInventoryScreen) {
                name = "inventory";
            } else if (sGui instanceof EnchantmentScreen) {
                name = "enchantment table";
            } else if (sGui instanceof MerchantScreen) {
                name = "villager";
            } else if (sGui instanceof AnvilScreen) {
                name = "anvil";
            } else if (sGui instanceof BeaconScreen) {
                name = "beacon";
            } else if (sGui instanceof BrewingStandScreen) {
                name = "brewing stand";
            } else if (sGui instanceof GenericContainerScreen) {
                name = "chest";
            } else if (sGui instanceof CraftingScreen) {
                name = "crafting table";
            } else if (sGui instanceof Generic3x3ContainerScreen) {
                name = "dispenser";
            } else if (sGui instanceof HopperScreen) {
                name = "hopper";
            } else if (sGui instanceof FurnaceScreen) {
                name = "furnace";
            } else if (sGui instanceof HorseScreen) {
                name = "horse inventory";
            } else if (sGui instanceof ShulkerBoxScreen) {
                name = "shulker box";
            } else if (sGui instanceof SignEditScreen) {
                name = "sign";
            } else if (sGui instanceof BookEditScreen) {
                name = "book";
            } else if (sGui instanceof AbstractCommandBlockScreen) {
                name = "command block";
            } else {
                name = sGui.getClass().getSimpleName();
            }
            //TODO: Add more gui names

            final String fName = name;
            final LuaValue controls = GuiControls.load(sGui);

            if (sGui instanceof GenericContainerScreen) {
                Thread test = new Thread(() -> {
                    GenericContainerScreen gCon = (GenericContainerScreen) sGui;
                    List<ItemStack> stacks = gCon.getScreenHandler().getStacks();
                    LuaTable e = createEvent(EventName.ContainerOpen);

                    LuaTable ctrl;
                    if (controls.isnil()) {
                        ctrl = new LuaTable();
                    } else {
                        ctrl = controls.checktable();
                    }

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
                    //System.out.println(MinecraftClient.getInstance().ingameGUI.getClass());

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

    public final SoundInstanceListener SOUND_LISTENER = new SoundListener();

    public class SoundListener implements SoundInstanceListener {

        @Override
        public void onSoundPlayed(SoundInstance sound, WeightedSoundSet soundSet, float range) {
            LuaTable event = createEvent(EventName.Sound);
            event.set(3, sound.getX() + " " + sound.getY() + " " + sound.getZ());

            LuaTable details = new LuaTable();

            try {
                details.set("pitch", sound.getPitch());
            } catch (NullPointerException e) {
                details.set("pitch", 1);
            }
            try {
                details.set("volume", sound.getVolume());
            } catch (NullPointerException e) {
                details.set("volume", 1);
            }
            try {
                details.set("pos", Utils.posToTable(sound.getX(), sound.getY(), sound.getZ()));
            } catch (NullPointerException ignored) {

            }
            try {
                details.set("category", sound.getCategory().getName().toLowerCase());
            } catch (NullPointerException ignored) {

            }
            event.set(4, details);

            LuaTable controls = new LuaTable();
            controls.set("isPlaying", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaValue.valueOf(MinecraftClient.getInstance().getSoundManager().isPlaying(sound));
                }
            });
            controls.set("stop", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    MinecraftClient.getInstance().getSoundManager().stop(sound);
                    return NONE;
                }
            });
            event.set(5, controls);
            fireEvent(EventName.Sound, event);

        }

    }

    public void onEntityRenderPre(MatrixStack matrixStack, LivingEntity entity) {
        RenderFlags r = entityRenderFlags.get(entity);
        if (r == null) {
            return;
        }
        //event.getRenderer().setRenderOutlines(true);
        boolean flag = false;
        if (entity instanceof PlayerEntity p) {
            flag = p.isSneaking() | wasSneaking;
            wasSneaking = p.isSneaking();
        }
        ((IEntity) entity).am_setFlag(6, r.glow);
        r.reset();
        if (r.xray) {
            RenderSystem.disableDepthTest();
        }
    }

    public void onEntityRenderPost(MatrixStack matrixStack, LivingEntity entity) {
        RenderFlags r = entityRenderFlags.get(entity);
        if (r == null || !r.xray) {
            return;
        }
        RenderSystem.enableDepthTest();
    }

/*

    private static Object messageCounterLock = new Object();
    private static long messageIndex = 0;
    private static long nextMessageToAddToChat = 0;

    @SubscribeEvent
    public void onChat(final ClientChatReceivedEvent sEvent) {//TODO out going chat msg filter
        final ClientChatReceivedEvent event = sEvent; //arg not final because it's acquired thru reflection
        final long thisMessageIndex;
        synchronized (messageCounterLock) {
            thisMessageIndex = messageIndex++;
        }

        final LinkedList<String> toRun = AdvancedMacros.macroMenuGui.getMatchingScripts(false, EventName.ChatFilter.name(), false);
        JavaThread t = new JavaThread(() -> {

            LuaTable e = createEvent(EventName.Chat);
            LuaTable e2 = createEvent(EventName.ChatFilter);
            String unformated = event.getMessage().getString();

            Pair<String, LuaTable> pair = Utils.codedFromTextComponent(event.getMessage());//fromMinecraftColorCodes(event.getMessage().getFormattedText());
            String formated = pair.a;

            //formated = formated.substring(0, formated.length()-2);//gets rid of last &f that does nothing for us
            //System.out.println(sEvent.getMessage().getSiblings());
            //TODO simplfy formating
            LuaTable actions = pair.b;

            e.set(3, formated);
            e2.set(3, formated);
            e.set(4, unformated);
            e2.set(4, unformated);
            e.set(5, actions);
            e2.set(5, actions);

            for (String script : toRun) {
                if (script == null) {
                    continue;
                }
                File f = new File(AdvancedMacros.MACROS_FOLDER, script);
                if (f.exists() && f.isFile()) {
                    try {
                        FileReader fr = new FileReader(f);
                        Thread.currentThread().setName("ChatFilter - " + script);
                        LuaValue function = AdvancedMacros.globals.load(fr, f.getAbsolutePath());
                        Varargs ret = function.invoke(e2.unpack());
                        if (!ret.toboolean(1)) {
                            return;
                        }
                        e2 = createEvent(EventName.ChatFilter);
                        for (int i = 1; i <= ret.narg(); i++) {
                            e2.set(2 + i, ret.arg(i));
                        }
                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    } catch (LuaError le) {
                        Utils.logError(le);
                    }
                }
            }
            if (toRun.size() > 0) {
                for (int i = 0; i < e2.length(); i++) {
                    e.set(6 + i, e2.get(3 + i));
                }
            } else {
                LuaTable toUnpack = e.get(5).checktable();
                for (int i = 1; i <= Math.max(toUnpack.length(), 2); i++) {
                    e2.set(3 + i, toUnpack.get(i));
                }
            }

            LuaValue timeoutProp = Settings.settings.get("chatFilterTimeout");
            if (timeoutProp.isnil()) {
                Settings.settings.set("chatFilterTimeout", 3000);
            }
            long timeout = System.currentTimeMillis() + timeoutProp.optlong(3000);
            while (true) {
                long current;
                synchronized (messageCounterLock) {
                    if (nextMessageToAddToChat >= thisMessageIndex) {
                        break;
                    }
                }
                if (System.currentTimeMillis() >= timeout) {
                    break;
                }
                try {
                    Thread.sleep(50);
                } catch (Exception ex) {
                }
            }

            if (e2.get(3).toboolean()) {
                //AdvancedMacros.logFunc.invoke(e2.unpack().subargs(3));
                Pair<Text, Varargs> text = Utils.toTextComponent(e2.unpack().arg(3).checkjstring(), e2.unpack().subargs(4), true);
                ClientChatReceivedEvent ccre = new ClientChatReceivedEvent(sEvent.getType(), text.a);
                repostForgeEvent(ccre);
            }
            fireEvent(EventName.Chat, e);
            synchronized (messageCounterLock) {
                nextMessageToAddToChat = Math.max(thisMessageIndex + 1, nextMessageToAddToChat);
            }
        });
        if (toRun.size() > 0) {
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
                if (script == null) {
                    return;
                }
                File f = new File(AdvancedMacros.MACROS_FOLDER, script);
                if (f.exists() && f.isFile()) {
                    try {
                        FileReader fr = new FileReader(f);
                        Thread.currentThread().setName("ChatSendFilter - " + script);
                        LuaValue function = AdvancedMacros.globals.load(fr, f.getAbsolutePath());
                        Varargs ret = function.invoke(e.unpack());
                        if (!ret.toboolean(1)) {
                            return;
                        }
                        e = createEvent(EventName.ChatSendFilter);
                        for (int i = 1; i <= ret.narg(); i++) {
                            e.set(2 + i, ret.arg(i));
                        }
                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    } catch (LuaError le) {
                        Utils.logError(le);
                    }
                }
            }
            forceSendMsg(e.get(3).tojstring(), true);
        });
        thread.start();
        event.setCanceled(true);
        MinecraftClient.getInstance().ingameGUI.getChatGUI().addToSentMessages(event.getOriginalMessage());
    }

    //this is from the Screen class, skips the forge event though
    private void forceSendMsg(String msg, boolean addToChat) {
        //msg = net.minecraftforge.event.ForgeEventFactory.onClientSendMessage(msg);
        if (msg.isEmpty()) {
            return;
        }
        if (addToChat) {
            MinecraftClient.getInstance().ingameGUI.getChatGUI().addToSentMessages(msg);
        }
        //if (net.minecraftforge.client.ClientCommandHandler.instance.executeCommand(mc.player, msg) != 0) return; //Forge: TODo Client command re-write

        MinecraftClient.getInstance().player.sendChatMessage(msg);
    }

    @SubscribeEvent
    public void onItemPickup(EntityItemPickupEvent ipe) { //check net.minecraftforge.event.ForgeEventFactory onItemPickup
        LuaTable e = createEvent(EventName.ItemPickup);
        e.set(3, Utils.codedFromTextComponent(ipe.getPlayer().getName()).a);
        e.set(4, Utils.itemStackToLuatable(ipe.getItem().getItem()));
        fireEvent(EventName.ItemPickup, e);
    }

 */

    private void resetItemDurablitity() {
        ItemStack i = MinecraftClient.getInstance().player.getMainHandStack();
        lastItemDurablity = i.getMaxDamage() - i.getDamage();
    }

    private void resetItemDurability() {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return;
        }
        lastAir = player.getAir() / 3;
        lastHealth = (int) player.getHealth();
        lastHunger = player.getHungerManager().getFoodLevel();
        lastSaturation = player.getHungerManager().getSaturationLevel();
        //lastDimension = player.dimension;
        resetItemDurablitity();

        Iterator<ItemStack> armor = player.getItemsEquipped().iterator();
        boolean armorChanged = false;
        int[] armorDurability = new int[4];
        for (int i = 0; i < armorDurability.length; i++) {
            ItemStack next = armor.next();
            int temp = next.getMaxDamage() - next.getDamage();
            if (temp != lastArmourDurability[i]) {
                armorChanged = true;
            }
            armorDurability[i] = temp;
        }
        System.arraycopy(armorDurability, 0, lastArmourDurability, 0, armorDurability.length);
        lastXP = player.totalExperience;
        lastXPLevel = player.experienceLevel;
    }

    public void onLastWorldRender(MatrixStack matrixStack) {
        Camera renderInfo = MinecraftClient.getInstance().gameRenderer.getCamera();
        Vec3d projectedView = renderInfo.getPos();

        matrixStack.push();

        RenderSystem.enableCull();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

        synchronized (worldHudItems) {
            for (WorldHudItem worldHudItem : worldHudItems) {
                matrixStack.push();
                if (worldHudItem.getDrawType().isXRAY()) {
                    RenderSystem.disableDepthTest();
                } else {
                    RenderSystem.enableDepthTest();
                }
                worldHudItem.apply3dRotation(matrixStack, projectedView.x, projectedView.y, projectedView.z);
                RenderSystem.setShaderColor(1, 1, 1, worldHudItem.getOpacity());
                worldHudItem.render(matrixStack);
                matrixStack.pop();
            }
        }
        matrixStack.pop();
        RenderSystem.disableBlend();//F1 is black otherwise
    }

    public void afterOverlay(DrawContext drawContext) {
        float p = MinecraftClient.getInstance().getTickDelta();
        //Entity player = MinecraftClient.getInstance().player;

        RenderSystem.disableBlend();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        //TODO 1.19 Update: RenderSystem.disableAlphaTest();
        RenderSystem.bindTexture(0);

        synchronized (hud2DItems) {
            for (Hud2DItem hudItem : hud2DItems) {
                RenderSystem.setShaderColor(1, 1, 1, hudItem.getOpacity() / 255f);
                hudItem.render(drawContext, p);
            }
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        //TODO 1.19 Update: RenderSystem.disableAlphaTest();
        //TODO 1.19 Update: RenderSystem.enableAlphaTest();
    }

    public static float accuPlayerX(float pTick, Entity e) {
        return (float) (e.getX() * pTick + e.lastRenderX * (1 - pTick));
    }

    public static float accuPlayerY(float pTick, Entity e) {
        return (float) (e.getY() * pTick + e.lastRenderY * (1 - pTick));
    }

    public static float accuPlayerZ(float pTick, Entity e) {
        return (float) (e.getZ() * pTick + e.lastRenderZ * (1 - pTick));
    }

    public static double accuPlayerYaw(float pTick, Entity e) {
        return e.getYaw() * pTick + e.prevYaw * (1 - pTick);
    }

    public static double accuPlayerPitch(float pTick, Entity e) {
        return e.getPitch() * pTick + e.prevPitch * (1 - pTick);
    }

    public static LuaTable createEvent(String eventName) {
        LuaTable t = new LuaTable();
        t.set(1, "event");
        t.set(2, eventName);
        return t;
    }

    public static LuaTable createEvent(EventName eventName) {
        return createEvent(eventName.name());
    }

    public void fireEvent(EventName event, LuaTable args) {
        if (AdvancedMacros.macroMenuGui == null) {
            return;
        }
        AdvancedMacros.macroMenuGui.fireEvent(false, event.name(), args.unpack(), false, null);
    }

    public void fireEvent(String eventString, LuaTable args) {
        if (AdvancedMacros.macroMenuGui == null) {
            return;
        }
        AdvancedMacros.macroMenuGui.fireEvent(false, eventString, args.unpack(), false, null);
    }

    public void fireEvent(EventName event, LuaTable args, LuaDebug.OnScriptFinish onScriptFinish) {
        if (AdvancedMacros.macroMenuGui == null) {
            return;
        }
        AdvancedMacros.macroMenuGui.fireEvent(false, event.name(), args.unpack(), false, onScriptFinish);
    }

    public boolean eventExists(EventName eName) {
        if (AdvancedMacros.macroMenuGui == null) {
            return false;
        }
        return AdvancedMacros.macroMenuGui.doesEventExist(eName.name());
    }

    public static void showMenu() {
        showMenu(AdvancedMacros.macroMenuGui.getGui());
    }

    public static void closeMenu() {
        MinecraftClient.getInstance().setScreen(null);
    }

    public static void showMenu(Gui gui) {
        if (gui == null) {
            showMenu();
        }
        AdvancedMacros.lastGui = gui;//the one to return to on open, not prev gui
        MinecraftClient.getInstance().setScreen(gui);
        gui.onOpen();
    }

    public static void showMenu(Gui gui, Gui prevGui) {
        MinecraftClient.getInstance().setScreen(gui);
        AdvancedMacros.prevGui = prevGui;
        AdvancedMacros.lastGui = gui;
        gui.onOpen();
    }

    public static void showPrevMenu() {
        if (AdvancedMacros.prevGui == null) {
            AdvancedMacros.prevGui = AdvancedMacros.macroMenuGui.getGui();
        }
        showMenu(AdvancedMacros.prevGui);
        AdvancedMacros.prevGui = null;
    }

    public void addWorldHudItem(WorldHudItem whi) {
        synchronized (worldHudItems) {
            worldHudItems.add(whi);
        }
    }

    public void removeWorldHudItem(WorldHudItem whi) {
        synchronized (worldHudItems) {
            worldHudItems.remove(whi);
        }
    }

    public void clearWorldHud() {
        synchronized (worldHudItems) {
            while (!worldHudItems.isEmpty()) {
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
            while (!hud2DItems.isEmpty()) {
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
        PlayerEntity player = MinecraftClient.getInstance().player;
        look = new Look(player.getHeadYaw(), player.getPitch(), sYaw, sPitch, time);

    }

    private class Look {

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

            PlayerEntity player = MinecraftClient.getInstance().player;
            player.setYaw(fixYaw(player.getYaw()));
            player.prevYaw = fixYaw(player.prevYaw);

            double b = toYaw - fromYaw;
            double a = 360 - b;
            a %= 360;
            boolean flag = Math.abs(b) > 180;
            if (flag) {//better to turn other way
                float amount = (float) (360 * Math.signum(b));
                this.fromYaw += amount;
                player.setYaw(player.getYaw() + amount);
                player.prevYaw += amount;
                //System.out.println("Reverse spin");
            } else if (fromYaw % 360 == toYaw % 360) { //same do nothing really
                fromYaw = toYaw;
            }
            //AdvancedMacros.logFunc.call(String.format("%f, %f -> %f, %f (&%s%f, %f&f)", this.fromYaw, fromPitch, toYaw, toPitch, flag?"a":"c" , a, b));
            start = System.currentTimeMillis();
            this.time = time;
        }

        private float fixYaw(float yaw) {
            return (yaw + 540) % 360 - 180;
        }

        public void look() {
            if (System.currentTimeMillis() <= time + start) {
                PlayerEntity player = MinecraftClient.getInstance().player;

                player.setYaw(interpolate(fromYaw, toYaw));
                player.setPitch(interpolate(fromPitch, toPitch));
            }
        }

        private float interpolate(float f, float t) {
            float x = System.currentTimeMillis() - start;
            float u = (f - t) / 2;
            return u * MathHelper.cos((float) ((x * Math.PI) / time)) - u + f;
        }

    }

    private class HeldKeybinds {

        InputUtil.Key input;
        long releaseTime;
        boolean done = false; //removal flag

        public HeldKeybinds(InputUtil.Key input, long releaseTime) {
            super();
            this.input = input;
            this.releaseTime = releaseTime;
        }

    }

    private void populatePlayerList(ConcurrentHashMap<UUID, String> map) {
        MinecraftClient mc = MinecraftClient.getInstance();
        for (PlayerListEntry playerInfo : mc.getNetworkHandler().getPlayerList()) {
            UUID uuid = playerInfo.getProfile().getId();
            String name = Utils.codedFromTextComponent(mc.inGameHud.getPlayerListHud().getPlayerName(playerInfo)).a;
            if (name != null) {
                String formated = name
                        .replaceAll("&", "&&")
                        .replaceAll("\u00A7", "&")
                        .replaceAll("&k", "&O") //Obfuscated
                        .replaceAll("&l", "&B") //Bold
                        .replaceAll("&m", "&S") //Strikethru
                        .replaceAll("&o", "&I") //Italics
                        .replaceAll("&r", "&f")   //reset (to white in this case)
                        .replaceAll("&[^&]", "").replaceAll("&&", "&");
                map.put(uuid, formated);
            }
        }
    }

    //private LinkedList<HeldKeybinds> heldKeybinds = new LinkedList<>();
    ConcurrentHashMap<InputUtil.Key, HeldKeybinds> keyBindReleaseMap = new ConcurrentHashMap<>();

    public void releaseKeybindAt(InputUtil.Key input, long l) {
        //heldKeybinds.add(new HeldKeybinds(keycode, l));
        keyBindReleaseMap.put(input, new HeldKeybinds(input, l));
    }

    public static class RenderFlags {

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
