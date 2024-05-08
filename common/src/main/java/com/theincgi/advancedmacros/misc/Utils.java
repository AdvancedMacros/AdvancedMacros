package com.theincgi.advancedmacros.misc;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.theincgi.advancedmacros.AdvancedMacros;
import com.theincgi.advancedmacros.event.EventHandler;
import com.theincgi.advancedmacros.gui.Color;
import com.theincgi.advancedmacros.lua.LuaValTexture;
import com.theincgi.advancedmacros.lua.util.BufferedImageControls;
import com.theincgi.advancedmacros.lua.util.ContainerControls;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.command.argument.ItemStringReader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaFunction;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static String LuaTableToString(LuaTable sTable) {
        return luaTableToString(sTable, new HashMap<LuaTable, Boolean>(), 1, sTable.tojstring() + " {\n");
    }

    private static String luaTableToString(LuaTable t, HashMap<LuaTable, Boolean> printed, int indent, String out) {
        //		for(int i = 0; i<indent;i++){
        //			out+="  ";
        //		}
        //		out+=t.tojstring()+" {\n";
        printed.put(t, true);
        for (LuaValue key = t.next(LuaValue.NIL).arg1(); !key.isnil(); key = t.next(key).arg1()) {
            for (int i = 0; i < indent; i++) {
                out += "  ";
            }
            out += "[" + key.tojstring() + "] = ";
            LuaValue val = t.get(key);
            if (val.istable()) {
                if (printed.containsKey(val.checktable())) {
                    out += "[recursive table]\n";
                } else {
                    out += val.tojstring() + " {\n";
                    out = luaTableToString(val.checktable(), printed, indent + 1, out);
                }
            } else {
                if (val.isstring()) {
                    out += "\"" + val.tojstring() + "\"\n";
                } else {
                    out += val.tojstring() + "\n";
                }
            }
        }
        for (int i = 0; i < indent - 1; i++) {
            out += "  ";
        }
        out += "}\n";
        return out;
    }

    public static Color parseColor(LuaValue v) {
        return parseColor(LuaValue.varargsOf(new LuaValue[]{v}));
    }

    public static Color parseColor(Varargs v, boolean use255Space) {
        if (use255Space) {
            return parseColor(v);
        } else {
            float a = 1, r, g, b;
            switch (v.narg()) {

                case 1:
                    LuaValue val = v.arg1();
                    if (val.isnumber()) {
                        return new Color(val.checkint());
                    } else if (val.istable()) {
                        if (val.get("r").isint() && val.get("g").isint() && val.get("b").isint()) {
                            r = (float) val.get("r").checkdouble();
                            g = (float) val.get("g").checkdouble();
                            b = (float) val.get("b").checkdouble();
                            if (val.get("a").isint()) {
                                a = val.get("a").checkint();
                            }
                            return new Color(a, r, g, b);
                        } else {
                            return parseColor(val.checktable().unpack(), use255Space);
                        }
                    } else if (val.isfunction()) {
                        return parseColor(val.call(), use255Space);
                    }
                    break;
                case 4:

                    a = (float) v.arg(4).checkdouble();
                case 3:
                    r = (float) v.arg(1).checkdouble();
                    g = (float) v.arg(2).checkdouble();
                    b = (float) v.arg(3).checkdouble();
                    return new Color(a, r, g, b);
            }
            return new Color(0xFF000000);
        }
    }

    private static Color parseColor(Varargs v) {
        int a = 255, r, g, b;
        switch (v.narg()) {

            case 1:
                LuaValue val = v.arg1();
                if (val.isnumber()) {
                    return new Color(val.checkint());
                } else if (val.istable()) {
                    if (val.get("r").isint() && val.get("g").isint() && val.get("b").isint()) {
                        r = val.get("r").checkint();
                        g = val.get("g").checkint();
                        b = val.get("b").checkint();
                        if (val.get("a").isint()) {
                            a = val.get("a").checkint();
                        }
                        return new Color(a, r, g, b);
                    } else {
                        return parseColor(val.checktable().unpack());
                    }
                }
                break;
            case 4:

                a = v.arg(4).checkint();
            case 3:
                r = v.arg(1).checkint();
                g = v.arg(2).checkint();
                b = v.arg(3).checkint();
                return new Color(a, r, g, b);
        }
        return new Color(0xFF000000);
    }

    /**
     * Generates needed tables if they dont exist<br> will
     *
     * @throws LuaError if something exists and is not a table for one or if it contains spaces<br>
     */
    public static LuaValue tableFromProp(LuaTable sTable, String propKey, LuaValue defaultVal) {
        if (propKey.contains(" ") || propKey.contains("_")) {
            throw new LuaError("Prop key can not have spaces or underscores");
        }
        Scanner s = new Scanner(propKey);
        s.useDelimiter("\\.");
        LuaValue v = LuaValue.NIL;
        while (s.hasNext()) {
            String key = s.next();
            //System.out.println("Key> "+key);
            try {
                v = sTable.get(Integer.parseInt(key));
            } catch (Exception e) {
                v = sTable.get(key);
            }

            if (v.isnil()) {
                if (s.hasNext()) {
                    sTable.set(key, v = new LuaTable());
                } else {
                    sTable.set(key, v = defaultVal);
                }
            }
            if (s.hasNext()) {
                sTable = v.checktable();
            }
        }
        s.close();
        return v;
    }

    public static LuaValTexture checkTexture(LuaValue v) {
        if (v.getClass().equals(LuaValTexture.class)) {
            return (LuaValTexture) v;
        }
        return null;
    }

    private static LuaValue createJumpToAction(String file, int lineNum) {
        LuaTable table = new LuaTable();
        table.set("hover", "&b&BClick&f to jump to\nline &a&B" + lineNum + "&f in editor");
        table.set("click", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                //TODO check for unsaved changes first or use tab'd editor instead
                AdvancedMacros.editorGUI.openScript(file);
                AdvancedMacros.editorGUI.getCta().jumpToLine(0, lineNum - 1);
                EventHandler.showMenu(AdvancedMacros.editorGUI, AdvancedMacros.macroMenuGui.getGui());
                return null;
            }
        });
        return table;
    }

    @Deprecated
    public static void logError2(Throwable le) {
        final String pattern = "((?:[a-zA-Z_0-9./\\\\]+)+):(\\d+)";
        if (le instanceof LuaError) {
            String errText = le.getLocalizedMessage();

            Pattern pat = Pattern.compile(pattern);
            Matcher m = pat.matcher(errText);
            StringBuilder output = new StringBuilder("&c");
            int i = 0;
            LuaTable actions = new LuaTable();
            int actNum = 2; //1 reserved for output string
            while (m.find()) {
                int s = m.start(), e = m.end();
                String fileName = m.group(1);
                int lineNum = Integer.parseInt(m.group(2));
                actions.set(actNum++, createJumpToAction(fileName, lineNum));
                if (i < s) {
                    output.append("&c");
                    output.append(errText.substring(i, s));
                }
                output.append("&U&F");
                output.append(fileName)
                        .append(":")
                        .append(lineNum);
                i = e;
            }
            if (i < errText.length()) {
                output.append("&c").append(errText.substring(i));
            }
            actions.set(1, output.toString().replaceAll("\t", "  "));
            //			AdvancedMacros.logFunc.call("&c"+le.toString());
            AdvancedMacros.logFunc.invoke(actions.unpack());
        } else {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            le.printStackTrace(pw);
            AdvancedMacros.logFunc.call("&c" + sw.toString());
        }
    }

    public static void logError(Throwable le) {  //FIXME launch matrex vector test from tools
        if (le instanceof LuaError) {
            String errText = le.getLocalizedMessage().replace("\t", "  ").replace("\n\n", "\n");
            StringBuilder output = new StringBuilder();
            int start = 0, end = 0;
            LuaTable actions = new LuaTable();
            int actNum = 2; //1 reserved for output string
            int amRootLength = AdvancedMacros.MACROS_FOLDER.getAbsolutePath().length() + 1;
            boolean valid = true;
            while (end < errText.length()) {
                //matches _: where _ is a letter
                if ((start + 1 < errText.length()) && Character.isLetter(errText.charAt(start)) && errText.charAt(start + 1) == ':') {
                    end = start + 2;
                    while (end + 1 < errText.length() && errText.charAt(end) != '\n' && !(errText.charAt(end) == ':' && Character.isDigit(errText.charAt(end + 1)))) {
                        end++;
                    }
                    String fileName = errText.substring(start, end);
                    if (errText.charAt(end) == '\n') {
                        output.append(errText.substring(start, end + 1));
                        start = end++;
                    } else {
                        start = ++end;
                        while (end < errText.length() && Character.isDigit(errText.charAt(end))) {
                            end++;
                        }
                        if (start == end) {
                            output.append(fileName);
                        } else {
                            int line = Integer.parseInt(errText.substring(start, end));
                            start = end;
                            File tmp = new File(fileName);
                            if (tmp.exists() && tmp.getAbsolutePath().contains(AdvancedMacros.MACROS_FOLDER.getAbsolutePath())) {
                                fileName = fileName.substring(amRootLength);
                                output.append("&4&F")
                                        .append(fileName)
                                        .append(':')
                                        .append(line)
                                        .append("&c");
                                actions.set(actNum++, createJumpToAction(fileName, line));
                            } else {
                                output.append("&4")
                                        .append(fileName)
                                        .append(':')
                                        .append(line)
                                        .append("&c");
                            }

                        }
                    }
                } else {
                    //if(errText.charAt(start)!='\n')
                    output.append(errText.charAt(start));
                    start++;
                    end++;
                }
            }
            if (start != end) {
                output.append("&b")
                        .append(errText.substring(start, end));
            }
            actions.set(1, output.toString());
            AdvancedMacros.logFunc.invoke(actions.unpack());
        } else {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            le.printStackTrace(pw);
            AdvancedMacros.logFunc.call("&c" + sw.toString());
        }
    }

    public static String normalizeText(String keyName) {
        return keyName.charAt(0) + (keyName.substring(1).toLowerCase());
    }

    /**
     * true if not nil
     */
    public static boolean checkKey(LuaTable t, String k) {
        return !t.get(k).isnil();
    }

    public static boolean isTextColorCode(char charAt) {
        return Character.isDigit(charAt) || ('a' <= charAt && charAt <= 'f');
    }

    public static boolean isTextStyleCode(char c) {
        return c == 'B' || c == 'I' || c == 'O' || c == 'S' || c == 'U';
    }

    private static final Color[] textCodes = {Color.TEXT_0,
            Color.TEXT_1, Color.TEXT_2, Color.TEXT_3, Color.TEXT_4,
            Color.TEXT_5, Color.TEXT_6, Color.TEXT_7, Color.TEXT_8,
            Color.TEXT_9, Color.TEXT_a, Color.TEXT_b, Color.TEXT_c, Color.TEXT_d, Color.TEXT_e, Color.TEXT_f};

    public static Color getTextCodeColor(char sChar) {
        if (!isTextColorCode(sChar)) {
            return null;
        }
        if (Character.isDigit(sChar)) {
            return textCodes[sChar - '0'].copy();
        } else {
            return textCodes[10 + sChar - 'a'].copy();
        }
    }

    public static class TimeFormat {

        public int seconds, mins, hours, days, millis;

    }

    public static TimeFormat formatTime(double time) {
        TimeFormat f = new TimeFormat();
        f.millis = (int) ((time % 1) * 100);
        time -= ((int) (time % 1));
        time -= (f.seconds = (int) (time % 60));
        time /= 60;
        time -= (f.mins = (int) (time % (60)));
        time /= 60;
        time -= (f.hours = (int) (time % 24));
        time /= 24;
        f.days = (int) time;
        return f;
    }

    public static LuaValue itemStackToLuatable(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return LuaValue.FALSE;
        }
        LuaTable table = new LuaTable();
        table.set("name", stack.getName() == null ? LuaValue.NIL : LuaValue.valueOf(stack.getName().getString()));
        table.set("id", Registries.ITEM.getId(stack.getItem()).toString());
        table.set("dmg", stack.getDamage());
        table.set("maxDmg", stack.getMaxDamage());
        table.set("amount", stack.getCount());
        table.set("repairCost", stack.getRepairCost());
        table.set("enchants", NBTUtils.fromTagList(stack.getEnchantments()));
        NbtCompound compound = new NbtCompound();
        stack.writeNbt(compound);
        table.set("nbt", NBTUtils.fromCompound(compound));
        LuaTable ctabs = new LuaTable();
        table.set("tabs", ctabs);
        int k = 1;
/*        for (ItemGroup tab : stack.getItem().getCreativeTabs()) {
            if (tab != null) {
                LuaTable cat = new LuaTable();
                cat.set("label", tab.getTabLabel());
                cat.set("icon", tab.getTabsImage().toString());
                ctabs.set(k++, cat);
            }
        }*/
        return table;
    }

    public static LuaTable blockToTable(BlockState blockState, @Nullable BlockEntity te) {
        Block block = blockState.getBlock();
        LuaTable out = new LuaTable();
        out.set("id", Registries.BLOCK.getId(block).toString());
        out.set("name", block.getName().getString());
        // looks like they don't have dmg values for blocks anymore? out.set("dmg", block.getMetaFromState(blockState));
        if (te != null) {
            out.set("nbt", Utils.NBTUtils.fromCompound(te.createNbtWithId()));
        }
/*        ToolType tool = block.getHarvestTool(blockState);
        if (tool != null) {
            out.set("harvestTool", tool.getName().toLowerCase());
        }*/

        return out;
    }

    public static boolean itemsEqual(ItemStack sourceStack, ItemStack sinkStack) {
        return ItemStack.areItemsEqual(sourceStack, sinkStack) && ItemStack.areEqual(sourceStack, sinkStack);
    }

    public static LuaValue inventoryToTable(PlayerInventory inventory, boolean collapseEmpty) {
        LuaTable t = new LuaTable();
        for (int i = 0; i < inventory.size(); i++) {
            LuaValue stack = itemStackToLuatable(inventory.getStack(i));
            if (collapseEmpty && stack.isboolean() && !stack.checkboolean()) {
                continue;
            }
            t.set(i, stack);
        }
        t.set("mouse", itemStackToLuatable(MinecraftClient.getInstance().player.currentScreenHandler.getCursorStack()));
        return t;
    }

    public static LuaValue effectToTable(StatusEffectInstance pe) {
        LuaTable table = new LuaTable();
        table.set("id", Registries.STATUS_EFFECT.getId(pe.getEffectType()).toString());
        table.set("strength", pe.getAmplifier());
        table.set("duration", pe.getDuration());
        table.set("showsParticles", LuaValue.valueOf(pe.shouldShowParticles()));
        table.set("isAmbient", LuaValue.valueOf(pe.isAmbient()));
        return table;
    }

    public static AbstractClientPlayerEntity findPlayerByName(String toFind) {
        return MinecraftClient.getInstance().world.getPlayers().stream()
                .filter(player -> player.getName().getString().equals(toFind))
                .findFirst().orElse(null);
    }

    public static LuaTable blockPosToTable(BlockPos pos) {
        LuaTable t = new LuaTable();
        t.set(1, LuaValue.valueOf(pos.getX()));
        t.set(2, LuaValue.valueOf(pos.getY()));
        t.set(3, LuaValue.valueOf(pos.getZ()));
        return t;
    }

    public static LuaTable posToTable(double x, double y, double z) {
        LuaTable t = new LuaTable();
        t.set(1, LuaValue.valueOf(x));
        t.set(2, LuaValue.valueOf(y));
        t.set(3, LuaValue.valueOf(z));
        return t;
    }

    public static LuaValue entityToTable(Entity entity) {
        if (entity == null) {
            return LuaValue.FALSE;
        }
        LuaTable t = new LuaTable();
        t.set("name", entity.getName().getString());
        t.set("class", entity.getClass().getName());
        //t.set("inventory", Utils.inventoryToTable(entity.inventory, !(entity instanceof EntityPlayerSP)));
        {
            LuaTable pos = new LuaTable();
            pos.set(1, LuaValue.valueOf(entity.getX()));
            pos.set(2, LuaValue.valueOf(entity.getY()));
            pos.set(3, LuaValue.valueOf(entity.getZ()));
            t.set("pos", pos);
        }
        t.set("dimension", toTable(entity.getWorld().getDimension()));
        t.set("pitch", entity.getPitch());
        t.set("yaw", entity.getYaw());
        t.set("fallDist", entity.fallDistance);
        t.set("height", entity.getHeight());
        t.set("width", entity.getWidth());
        t.set("hurtResTime", entity.timeUntilRegen);
        //t.set("isAirborne", LuaValue.valueOf(player.isAirBorne));
        t.set("isCollidedHorz", LuaValue.valueOf(entity.horizontalCollision));
        t.set("isCollidedVert", LuaValue.valueOf(entity.verticalCollision));
        //t.set("swingProgress", LuaValue.valueOf(entity.swingProgress));
        //t.set("maxHurtResTime", LuaValue.valueOf(entity.maxHurtResistantTime));
        t.set("isNoClip", LuaValue.valueOf(entity.noClip));
        t.set("onGround", LuaValue.valueOf(entity.isOnGround()));
        t.set("isInvulnerable", LuaValue.valueOf(entity.isInvulnerable()));
        //		{
        //			LuaTable pos = new LuaTable();
        //			BlockPos p = entity.getBedLocation();
        //			if(p!=null) {
        //				pos.set(1, LuaValue.valueOf(p.getX()));
        //				pos.set(2, LuaValue.valueOf(p.getY()));
        //				pos.set(3, LuaValue.valueOf(p.getZ()));
        //				t.set("bedLocation", pos);
        //			}
        //		}
        t.set("team", entity.getScoreboardTeam() == null ? "none" : entity.getScoreboardTeam().getName());
        {
            LuaTable velocity = new LuaTable();
            Vec3d motion = entity.getVelocity();
            velocity.set(1, LuaValue.valueOf(motion.x));
            velocity.set(2, LuaValue.valueOf(motion.y));
            velocity.set(3, LuaValue.valueOf(motion.z));
            t.set("velocity", velocity);
        }
        //t.set("luck", entity.getLuck());
        if (entity instanceof LivingEntity living) {
            t.set("health", living.getHealth());
            t.set("isOnLadder", LuaValue.valueOf(living.isHoldingOntoLadder()));
            {
                LuaTable effects = new LuaTable();
                int i = 1;
                for (StatusEffectInstance pe : living.getStatusEffects()) {
                    effects.set(i++, Utils.effectToTable(pe));
                }
                t.set("potionEffects", effects);
            }
        }
        //t.set("hunger", entity.getFoodStats().getFoodLevel());
        t.set("air", entity.getAir());

        //t.set("isSneaking", LuaValue.valueOf(entity.isSneaking()));

        t.set("isInWater", LuaValue.valueOf(entity.isTouchingWater()));
        t.set("isInLava", LuaValue.valueOf(entity.isInLava()));
        t.set("immuneToFire", LuaValue.valueOf(entity.isFireImmune()));
        t.set("isOnFire", LuaValue.valueOf(entity.isOnFire()));
        t.set("isSprinting", LuaValue.valueOf(entity.isSprinting()));
        t.set("entityRiding", Utils.entityToTable(entity.getControllingPassenger()));
        t.set("isInvisible", LuaValue.valueOf(entity.isInvisible()));
        NbtCompound compound = new NbtCompound();
        entity.writeNbt(compound);
        t.set("nbt", NBTUtils.fromCompound(compound));
        t.set("uuid", LuaValue.valueOf(entity.getUuid().toString()));
        HitResult rtr = entity.raycast(8, MinecraftClient.getInstance().getTickDelta(), false);
        if (rtr != null && rtr instanceof BlockHitResult brtr) {
            BlockPos lookingAt = brtr.getBlockPos();
            if (lookingAt != null) {
                LuaTable look = new LuaTable();
                look.set(1, LuaValue.valueOf(lookingAt.getX()));
                look.set(2, LuaValue.valueOf(lookingAt.getY()));
                look.set(3, LuaValue.valueOf(lookingAt.getZ()));
                t.set("lookingAt", look);
            }
        }
        return t;
    }

    public static LuaTable toTable(DimensionType dt) {
        if (dt == null) {
            throw new NullPointerException("DimensionType provided is null");
        }
        LuaTable out = new LuaTable();
        out.set("isVanilla", dt.natural());
/*        out.set("id", dt.getId());
        out.set("name", dt.equals(DimensionType.OVERWORLD) ?
                "overworld" :
                dt.equals(DimensionType.THE_END) ?
                        "end" :
                        dt.equals(DimensionType.THE_NETHER) ? "nether" : "unknown");  //TESTME check if name is reasonable too*/
        return out;
    }

    public static Text luaTableToComponentJson(LuaTable table) {
        String msg = "[\"\","; //["",

        if (table.length() == 0) {
            msg += parseTableToComJson(table) + "]";
        } else {
            for (int i = 1; i <= table.length(); i++) {
                LuaTable t = table.get(i).checktable();
                msg += parseTableToComJson(t);
                if (i < table.length()) {
                    msg += ",";
                }
            }
        }
        System.out.println(msg + "]");
        return Text.Serialization.fromJson(msg + "]");
    }

    private static String parseTableToComJson(LuaTable table) {
        String msg = "";
        if (table.get("text").isnil()) {
            throw new LuaError("No text property found");
        }
        msg += "{\"text\":\"" + table.get("text").tojstring();
        if (!table.get("color").isnil()) {
            String code = table.get("color").checkjstring();
            if (hasColorCode(code) != null) {
                msg += "\",\"color\":\"" + hasColorCode(code) + "\"";
            }
            if (code.contains("B")) {
                msg += ",\"bold\":true";
            }
            if (code.contains("S")) {
                msg += ",\"strikethrough\":true";
            }
            if (code.contains("O")) {
                msg += ",\"obfuscated\":true";
            }
            if (code.contains("U")) {
                msg += ",\"underlined\":true";
            }
            if (code.contains("I")) {
                msg += ",\"italic\":true";
            }
        }
        if (!table.get("click").isnil()) {
            LuaTable clickDetails = table.get("click").checktable();
            if (clickDetails.get("type").isnil()) {
                throw new LuaError("No click type set [sTable.type = url/suggestCommand/runCommand]");
            }
            if (clickDetails.get("value").isnil()) {
                throw new LuaError("No 'value' set");
            }
            clickDetails.get("type").checkjstring();
            String actType = switch (clickDetails.get("type").checkjstring()) {
                case "url", "open_url" -> "open_url";
                case "suggestCommand", "suggest_command" -> "suggest_command";
                case "runCommand", "run_command" -> "run_command";
                //				case "suggestText":
                //				case "insertion":
                //					actType="insertion";
                //					break;
                default ->
                        throw new LuaError("Invalid click type (" + clickDetails.get("type").tojstring() + ") use: [url/suggestCommand/runCommand]");
            };
            msg += ",\"clickEvent\":{\"action\":\"" + actType + "\",\"value\":\"" + clickDetails.get("value").checkjstring() + "\"}";
        }
        if (!table.get("tooltip").isnil()) {
            msg += ",\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + table.get("tooltip") + "\"}";
        }
        msg += "}";
        return msg;
    }

    public static String hasColorCode(String code) {
        if (code.contains("0")) {
            return "black";
        } else if (code.contains("1")) {
            return "dark_blue";
        } else if (code.contains("2")) {
            return "dark_green";
        } else if (code.contains("3")) {
            return "dark_aqua";
        } else if (code.contains("4")) {
            return "dark_red";
        } else if (code.contains("5")) {
            return "dark_purple";
        } else if (code.contains("6")) {
            return "gold";
        } else if (code.contains("7")) {
            return "light_gray";
        } else if (code.contains("8")) {
            return "gray";
        } else if (code.contains("9")) {
            return "blue";
        } else if (code.contains("a")) {
            return "green";
        } else if (code.contains("b")) {
            return "aqua";
        } else if (code.contains("c")) {
            return "red";
        } else if (code.contains("d")) {
            return "light_purple";
        } else if (code.contains("e")) {
            return "yellow";
        } else if (code.contains("f")) {
            return "white";
        }
        return null;
    }

    public static class NBTUtils {

        public static LuaTable fromCompound(NbtCompound comp) {
            LuaTable out = new LuaTable();
            for (String k : comp.getKeys()) {
                out.set(k, fromBase(comp.get(k)));
            }
            return out;
        }

        public static LuaValue fromBase(NbtElement tag) {
            LuaValue thisTag;
            if (tag instanceof NbtByte nbtByte) {
                thisTag = LuaValue.valueOf(nbtByte.byteValue());
            } else if (tag instanceof NbtShort nbtShort) {
                thisTag = LuaValue.valueOf(nbtShort.shortValue());
            } else if (tag instanceof NbtInt nbtInt) {
                thisTag = LuaValue.valueOf(nbtInt.intValue());
            } else if (tag instanceof NbtLong nbtLong) {
                thisTag = LuaValue.valueOf(nbtLong.longValue());
            } else if (tag instanceof NbtFloat nbtFloat) {
                thisTag = LuaValue.valueOf(nbtFloat.floatValue());
            } else if (tag instanceof NbtDouble nbtDouble) {
                thisTag = LuaValue.valueOf(nbtDouble.doubleValue());
            } else if (tag instanceof NbtByteArray nbtByteArray) {
                byte[] bytes = nbtByteArray.getByteArray();
                thisTag = new LuaTable();
                for (int j = 0; j < bytes.length; j++) {
                    thisTag.set(j + 1, LuaValue.valueOf(bytes[j]));
                }
            } else if (tag instanceof NbtString nbtString) {
                thisTag = LuaValue.valueOf(nbtString.asString());
            } else if (tag instanceof NbtList nbtList) {
                thisTag = fromTagList(nbtList);
            } else if (tag instanceof NbtCompound nbtCompound) {
                thisTag = fromCompound(nbtCompound);
            } else if (tag instanceof NbtIntArray nbtIntArray) {
                int[] ints = nbtIntArray.getIntArray();
                thisTag = new LuaTable();
                for (int j = 0; j < ints.length; j++) {
                    thisTag.set(j + 1, LuaValue.valueOf(ints[j]));
                }
            } else {
                thisTag = LuaValue.NIL;
            }
            return thisTag;
        }

        public static LuaTable fromTagList(NbtList list) {
            if (list == null) {
                //System.err.println("Warning: Utils.fromTagList list was null");
                return new LuaTable();
            }
            LuaTable table = new LuaTable();
            for (NbtElement tag : list) {
                table.set(table.length() + 1, fromBase(tag));
            }
            return table;
        }
        //		private static LuaValue compoundToTable(NBTTagCompound tileData) {
        //			LuaTable t = new LuaTable();
        //			for(String k : tileData.getKeySet()) {
        //				t.set(k, valueFromCompound(tileData, k));
        //			}
        //			return t;
        //		}
        //
        //		private static LuaTable NBT2Table(NBTTagList nbt) {
        //			if(nbt == null){
        //				return new LuaTable();
        //			}
        //			LuaTable tags = new LuaTable();
        //			for(int i = 0; i < nbt.tagCount(); i++){
        //				NBTBase b = nbt.get(i);
        //				tags.set(i+1, NBTBase2LuaValue(b));
        //			}
        //			return tags;
        //		}
        //
        //		private static LuaValue NBTBase2LuaValue(NBTBase b) {
        //			LuaValue thisTag;
        //			if(b instanceof NBTTagByte){
        //				thisTag = LuaValue.valueOf(((NBTTagByte) b).getByte());
        //			}else if(b instanceof NBTTagShort){
        //				thisTag = LuaValue.valueOf(((NBTTagShort) b).getShort());
        //			}else if(b instanceof NBTTagInt){
        //				thisTag = LuaValue.valueOf(((NBTTagInt) b).getInt());
        //			}else if(b instanceof NBTTagLong){
        //				thisTag = LuaValue.valueOf(((NBTTagLong) b).getLong());
        //			}else if(b instanceof NBTTagFloat){
        //				thisTag = LuaValue.valueOf(((NBTTagFloat) b).getFloat());
        //			}else if(b instanceof NBTTagDouble){
        //				thisTag = LuaValue.valueOf(((NBTTagDouble) b).getDouble());
        //			}else if(b instanceof NBTTagByteArray){
        //				byte[] bytes = ((NBTTagByteArray) b).getByteArray();
        //				thisTag = new LuaTable();
        //				for(int j = 0; j<bytes.length; j++){
        //					thisTag.set(j+1, LuaValue.valueOf(bytes[j]));
        //				}
        //			}else if(b instanceof NBTTagString){
        //				thisTag = LuaValue.valueOf(((NBTTagString) b).getString());
        //			}else if(b instanceof NBTTagList){
        //				thisTag = NBT2Table((NBTTagList) b);
        //			}else if(b instanceof NBTTagCompound){
        //				thisTag = new LuaTable();
        //				Set<String> keys = ((NBTTagCompound) b).getKeySet();
        //				for (String key : keys) {
        //					thisTag.set(key, valueFromCompound((NBTTagCompound) b, key));
        //				}
        //			}else if(b instanceof NBTTagIntArray){
        //				int[] ints = ((NBTTagIntArray) b).getIntArray();
        //				thisTag = new LuaTable();
        //				for(int j = 0; j<ints.length; j++){
        //					thisTag.set(j+1, LuaValue.valueOf(ints[j]));
        //				}
        //			}else{
        //				thisTag = LuaValue.NIL;
        //			}
        //			return thisTag;
        //		}
        //
        //		private static LuaValue valueFromCompound(NBTTagCompound b, String key) {
        //			switch (b.getTagId(key)) {
        //			case Constants.NBT.TAG_BYTE: //byte
        //				return LuaValue.valueOf(b.getByte(key));
        //			case Constants.NBT.TAG_SHORT: //short
        //				return LuaValue.valueOf(b.getShort(key));
        //			case Constants.NBT.TAG_INT: //int
        //				return LuaValue.valueOf(b.getInteger(key));
        //			case Constants.NBT.TAG_LONG: //long
        //				return LuaValue.valueOf(b.getLong(key));
        //			case Constants.NBT.TAG_FLOAT: //float
        //				return LuaValue.valueOf(b.getFloat(key));
        //			case Constants.NBT.TAG_DOUBLE: //double
        //				return LuaValue.valueOf(b.getDouble(key));
        //			case Constants.NBT.TAG_BYTE_ARRAY:{ //byte array
        //				LuaTable t = new LuaTable();
        //				byte[] bArry = b.getByteArray(key);
        //				for (int i = 0; i < bArry.length; i++) {
        //					t.set(i+1, LuaValue.valueOf(bArry[i]));
        //				}
        //				return t;
        //			}
        //			case Constants.NBT.TAG_STRING: //string
        //				return LuaValue.valueOf(b.getString(key));
        //			case Constants.NBT.TAG_LIST:{ //tagList
        //					NBTTagList list = (NBTTagList) b.getTag(key);
        //					Iterator<NBTBase> iter = list.iterator();
        //					LuaTable list = new LuaTable();
        //					while(iter.hasNext()) {
        //						NBTBase tag = iter.next();
        //						list.set(list.length()+1, NBTUtils.NBTBase2LuaValue(tag));
        //					}
        //			}
        //			case 10://compound
        //				LuaTable sTable = new LuaTable();
        //				for(String sKey : b.getCompoundTag(key).getKeySet()){
        //					sTable.set(sKey, valueFromCompound(b, sKey));
        //				}
        //				return sTable;
        //			case 11://int array
        //				LuaTable t2 = new LuaTable();
        //				byte[] iArry = b.getByteArray(key);
        //				for (int i = 0; i < iArry.length; i++) {
        //					t2.set(i+1, LuaValue.valueOf(iArry[i]));
        //				}
        //				return t2;
        //
        //
        //			default:
        //				return LuaValue.NIL;
        //			}
        //		}

    }

    public static LuaError toLuaError(Throwable t) {
        return new LuaError(t);
    }

    public static Varargs pinvoke(LuaFunction func, LuaValue... luaValues) {
        try {
            return func.invoke(luaValues);
        } catch (Throwable e) {
            logError(toLuaError(e));
            return null;
        }
    }

    /**
     * nah, its invoke, but it has one return
     */
    public static LuaValue pcall(LuaFunction func, LuaValue... luaValues) {
        try {
            return func.invoke(luaValues).arg1();
        } catch (Throwable e) {
            logError(toLuaError(e));
            return LuaValue.NIL;
        }
    }

    public static Varargs pinvoke(LuaFunction func, Varargs luaValues) {
        try {
            return func.invoke(luaValues);
        } catch (Throwable e) {
            logError(toLuaError(e));
            return null;
        }
    }

    /**
     * nah, its invoke, but it has one return
     */
    public static LuaValue pcall(LuaFunction func, Varargs luaValues) {
        try {

            if (AdvancedMacros.globals.getCurrentLuaThread() == null) {
                org.luaj.vm2_v3_0_1.LuaThread luaThread = new org.luaj.vm2_v3_0_1.LuaThread(AdvancedMacros.globals, func);
                AdvancedMacros.globals.setCurrentLuaThread(luaThread);
            }
            return func.invoke(luaValues).arg1();
        } catch (Throwable e) {
            logError(toLuaError(e));
            return LuaValue.NIL;
        }
    }

    public static Varargs pcallVarArgs(LuaFunction func, Varargs luaValues) {
        try {

            if (AdvancedMacros.globals.getCurrentLuaThread() == null) {
                org.luaj.vm2_v3_0_1.LuaThread luaThread = new org.luaj.vm2_v3_0_1.LuaThread(AdvancedMacros.globals, func);
                AdvancedMacros.globals.setCurrentLuaThread(luaThread);
            }
            return func.invoke(luaValues);
        } catch (Throwable e) {
            logError(toLuaError(e));
            return LuaValue.NIL;
        }
    }

    public static LuaValue toTable(Set<String> keySet) {
        LuaTable t = new LuaTable();
        for (String s : keySet) {
            t.set(t.length() + 1, s);
        }
        return t;
    }

    public static void debugPrint(LuaTable t) {
        System.out.println(LuaTableToString(t));
    }

    public static File parseFileLocation(LuaValue arg0) {
        return parseFileLocation(arg0.isnil() ? "" : arg0.tojstring(), 1);
    }

    public static File parseFileLocation(LuaValue arg0, LuaValue level) {
        return parseFileLocation(arg0.isnil() ? "" : arg0.tojstring(), level.optint(1));
    }

    public static File parseFileLocation(String arg, int level) {
        return parseFileLocation(Thread.currentThread(), arg, level);
    }

    public static File parseFileLocation(Thread caller, String arg, int level) {
        if (arg == null) {
            arg = "";
        }

        File file = null;
        if (arg.startsWith("/") || arg.startsWith("\\")) {
            file = new File(arg.substring(1));
        } else if (arg.startsWith("~")) {
            file = new File(AdvancedMacros.MACROS_ROOT_FOLDER, arg.substring(1));
        } else {
            LuaValue v = Utils.getDebugStacktrace(caller, level);
            if (v.isnil()) {
                throw new LuaError("Unable to get local path of file");
            }
            String m = v.get("short_src").tojstring();
            m = m.substring(0, Math.max(0, Math.max(m.lastIndexOf("\\"), m.lastIndexOf("/"))));
            File path = new File(m);
            file = new File(path, arg);
        }
        return file;
    }

    public static LuaValTexture parseTexture(LuaValue v) {
        return parseTexture(v, Utils.checkTexture(Settings.getTextureID("resource:holoblock.png")));
    }

    public static LuaValTexture parseTexture(LuaValue v, LuaValTexture def) {
        LuaValTexture lvt;
        if (v instanceof LuaValTexture) {
            lvt = (LuaValTexture) v;
        }
        if (v instanceof BufferedImageControls) {
            //if(((BufferedImageControls) v).getLuaValTexture() == null) throw new LuaError("Texture not created");
            lvt = ((BufferedImageControls) v).getLuaValTexture();
        } else if (v.isstring()) {
            lvt = Utils.checkTexture(Settings.getTextureID(v.checkjstring()));
            //		}else if(v.isnil()){
            //			lvt = null;
            if (lvt == null) {
                return def;
            }
        } else {
            lvt = def;
        }
        return lvt;
    }

    public static char mcSelectCode = '§';

    public static String toMinecraftColorCodes(String text) {
        char sel = '§';
        String reset = "";
        return reset + sel + "f" +
                text.replaceAll("&0", reset + sel + "0")
                        .replaceAll("&1", reset + sel + "1")
                        .replaceAll("&2", reset + sel + "2")
                        .replaceAll("&3", reset + sel + "3")
                        .replaceAll("&4", reset + sel + "4")
                        .replaceAll("&5", reset + sel + "5")
                        .replaceAll("&6", reset + sel + "6")
                        .replaceAll("&7", reset + sel + "7")
                        .replaceAll("&8", reset + sel + "8")
                        .replaceAll("&9", reset + sel + "9")
                        .replaceAll("&a", reset + sel + "a")
                        .replaceAll("&b", reset + sel + "b")
                        .replaceAll("&c", reset + sel + "c")
                        .replaceAll("&d", reset + sel + "d")
                        .replaceAll("&e", reset + sel + "e")
                        .replaceAll("&f", reset + sel + "f")
                        .replaceAll("&U", sel + "n")
                        .replaceAll("&B", sel + "l")
                        .replaceAll("&O", sel + "k")
                        .replaceAll("&S", sel + "m")
                        .replaceAll("&I", sel + "o")
                        .replaceAll("&&", "&")
                ;
    }

    public static String fromMinecraftColorCodes(String text) {
        return text
                .replaceAll("§", "&&")
                .replaceAll("&", "&")
                .replaceAll("§k", "&O") //Obfuscated
                .replaceAll("§l", "&B") //Bold
                .replaceAll("§m", "&S") //Strikethru
                .replaceAll("§o", "&I") //Italics
                .replaceAll("§r", "&f")  //reset (to white in this case)
                .replaceAll("§n", "&U")  //Underline
                ;
    }

    public static Pair<MutableText, Varargs> toTextComponent(String codedText, Varargs args, boolean allowHover) {
        return toTextComponent(codedText, args, allowHover, true);
    }

    //TODO 1.19 Update: Fix the style with functions
    public static Pair<MutableText, Varargs> toTextComponent(String codedText, Varargs args, boolean allowHover, boolean allowFunctions) {
        if (args == null) {
            args = new LuaTable().unpack();
        }
        MutableText out = Text.literal("");
        StringBuilder temp = new StringBuilder();
        Boolean bold = null, italics = null, obfusc = null, strike = null, underline = null;
        Formatting color = null;
        Style pStyle = out.getStyle();
        pStyle.withBold(false);
        pStyle.withItalic(false);
        pStyle.withObfuscated(false);
        pStyle.withStrikethrough(false);
        pStyle.withUnderline(false);
        out.setStyle(pStyle);
        //lua text component ce? click event?
        boolean ltcce = false;
        int argNum = 1;
        ClickEvent clickEvent = null;
        HoverEvent hoverEvent = null;
        for (int i = 0; i < codedText.length(); i++) {
            char c = codedText.charAt(i);
            if (c != '&') {
                temp.append(c);
            } else {
                if (i == codedText.length()) {
                  break;
                }
                char next = codedText.charAt(i + 1);
                if (next == '&') {
                  temp.append('&');
                } else {
                  temp.append('§');
                  temp.append(next);
                }
                i++;
            }
        }
        if (temp.length() > 0) {
            Text component = ltcce && allowFunctions ? new LuaTextComponent(temp.toString(), args.arg(argNum++), allowHover) : Text.literal(temp.toString());
            Style style = component.getStyle();
            style.withBold(bold);
            style.withItalic(italics);
            style.withObfuscated(obfusc);
            style.withStrikethrough(strike);
            style.withUnderline(underline);
            //style.withColor(color);
            style.withParent(pStyle);
            if (clickEvent != null) {
                style.withClickEvent(clickEvent);
            }
            out.append(component);
            if (hoverEvent != null) {
                style.withHoverEvent(hoverEvent);
            }
            temp = new StringBuilder();
            pStyle = component.getStyle();
        }

        return new Pair<>(out, args.subargs(argNum));
    }

    public static Pair<String, LuaTable> codedFromTextComponent(Text message) {
        return codedFromTextComponent(message, true);
    }

    public static Pair<String, LuaTable> codedFromTextComponent(Text message, boolean includeActions) {
        StringBuilder out = new StringBuilder();
        //		if(message instanceof TextComponentTranslation) {
        //			TextComponentTranslation tct = (TextComponentTranslation) message;
        //			System.out.println(tct.getUnformattedComponentText());
        //			System.out.println(Arrays.toString(tct.getFormatArgs()));
        //			tct.getFormattedText();
        //		}

        //msg = message.getSiblings().size()==0?message.getUnformattedText():message.getUnformattedComponentText();
        //if(message.getSiblings().size()==0)
        //			System.out.println(message.getClass()); //FIXME remove when done debugging

        LuaTable actions = new LuaTable();
        int actionNum = 1;
        for (Text com : message.getSiblings()) {
            if (com.getString().isEmpty()) {
                continue;
            }
            Style s = com.getStyle();
            LuaTable action = null;
            String formating = "&f" + fromMinecraftColorCodes(getFormattingCode(s));
            if (s.getClickEvent() != null && includeActions) {
                action = new LuaTable();
                action.set("click", s.getClickEvent().getValue());
                switch (s.getClickEvent().getAction()) {
                    case OPEN_URL -> formating += "&L";
                    case RUN_COMMAND -> formating += "&R";
                    case SUGGEST_COMMAND -> formating += "&T";
                    case OPEN_FILE -> formating += "&*";
                    default -> action = null;
                }
            }
            if (s.getHoverEvent() != null && includeActions) {
                if (HoverEvent.Action.SHOW_TEXT.equals(s.getHoverEvent().getAction())) {
                    if (action == null) {
                        formating += "&N";
                    }
                    action = (action == null) ? new LuaTable() : action;
                    action.set("hover", codedFromTextComponent((Text) s.getHoverEvent().getValue(s.getHoverEvent().getAction()), false).a);
                }
            }
            if (action != null) {
                actions.set(actionNum++, action);
            }
            out.append(formating);
            out.append(com.getString().replace("&", "&&"));
        }
        return new Pair<String, LuaTable>(out.toString(), actions);
    }

    public static String getFormattingCode(Style style) {
        if (style.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        if (style.getColor() != null) {
            builder.append(style.getColor());
        }
        if (style.isBold()) {
            builder.append(Formatting.BOLD);
        }
        if (style.isItalic()) {
            builder.append(Formatting.ITALIC);
        }
        if (style.isUnderlined()) {
            builder.append(Formatting.UNDERLINE);
        }
        if (style.isObfuscated()) {
            builder.append(Formatting.OBFUSCATED);
        }
        if (style.isStrikethrough()) {
            builder.append(Formatting.STRIKETHROUGH);
        }

        return builder.toString();
    }

    private static boolean isSpecialCode(char c) {
        return "FRTLN*".indexOf(c) >= 0; //Function, Execute, Type, Url
    }

    private static Formatting getTextFormatingColor(char c) {
        return switch (c) {
            case '0' -> Formatting.BLACK;
            case '1' -> Formatting.DARK_BLUE;
            case '2' -> Formatting.DARK_GREEN;
            case '3' -> Formatting.DARK_AQUA;
            case '4' -> Formatting.DARK_RED;
            case '5' -> Formatting.DARK_PURPLE;
            case '6' -> Formatting.GOLD;
            case '7' -> Formatting.GRAY;
            case '8' -> Formatting.DARK_GRAY;
            case '9' -> Formatting.BLUE;
            case 'a' -> Formatting.GREEN;
            case 'b' -> Formatting.AQUA;
            case 'c' -> Formatting.RED;
            case 'd' -> Formatting.LIGHT_PURPLE;
            case 'e' -> Formatting.YELLOW;
            case 'f' -> Formatting.WHITE;
            default -> null;
        };
    }

    //	//why is there also runOnMCAndWait...
    //	@Deprecated
    //	public static void runOnMCThreadAndWait(Runnable r){
    //		if(AdvancedMacros.getMinecraftThread() == Thread.currentThread()) {
    //			r.run();
    //			return;
    //		}
    //		ListenableFuture<Object> f = MinecraftClient.getInstance().addScheduledTask(r);
    //		while(!f.isDone()) try{Thread.sleep(5);}catch (InterruptedException ie) {return;}
    //	}
    public static LuaValue toTable(Inventory container) {
        return toTable(container, false);
    }

    public static LuaValue toTable(Inventory container, boolean isReady) {
        LuaTable out = new LuaTable();
        LuaTable slots = new LuaTable();
        for (int i = 0; i < container.size(); i++) {
            slots.set(i, itemStackToLuatable(container.getStack(i)));
        }
        out.set("slots", slots);
        out.set("controls", new ContainerControls(container));
        out.set("isReady", LuaValue.valueOf(isReady));
        return out;
    }

    public static Pair<Vec3d, Varargs> consumeVector(Varargs args, boolean optional, boolean isAngular) {
        if (args.istable(1) && !args.arg1().get(1).istable()) { //is table, but does not contain table
            Pair<Vec3d, Varargs> p = consumeVector(args.checktable(1).unpack(), optional, isAngular);
            p.b = args.subargs(2);
            return p;
        } else if (args.isnumber(1) && args.isnumber(2) && args.isnumber(3) && !isAngular) {
            Vec3d v = new Vec3d(args.checkdouble(1), args.checkdouble(2), args.checkdouble(3));
            return new Pair<Vec3d, Varargs>(v, args.subargs(4));
        } else if (args.isnumber(1) && args.isnumber(2) && isAngular) {
            float yaw = (float) args.checkdouble(1), pitch = (float) args.checkdouble(2);
            //yaw = Math.toRadians(yaw);
            //			pitch = Math.toRadians(pitch);
            ////			double x = Math.cos(yaw), z = Math.sin(yaw);
            ////			double y = -Math.sin(pitch);
            ////			Vec3d v = new Vec3d(x, y, z);
            //			Entity
            //			v = v.rotateYaw((float) Math.toRadians(-90));
            //			v = v.normalize();
            float f = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
            float f1 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
            float f2 = -MathHelper.cos(-pitch * 0.017453292F);
            float f3 = MathHelper.sin(-pitch * 0.017453292F);
            Vec3d v = new Vec3d((f1 * f2), f3, (f * f2));
            return new Pair<Vec3d, Varargs>(v, args.subargs(3));
        }
        if (!optional) {
            throw new LuaError(isAngular ? "Invalid direction, must be {yaw, pitch} or yaw, pitch" : "Invalid vector, must be {x,y,z} or x,y,z");
        }
        return new Pair<Vec3d, Varargs>(null, args);
    }

    public static LuaValue getDebugStacktrace() {
        return getDebugStacktrace(1);
    }

    public static LuaValue getDebugStacktrace(int level) {
        return getDebugStacktrace(Thread.currentThread(), level);
    }

    public static LuaValue getDebugStacktrace(Thread caller, int level) {
        //LuaValue v = AdvancedMacros.globals.debuglib.get("getinfo").call(valueOf(1), valueOf("Sl"));
        return AdvancedMacros.debugTable.get("getinfo")
                .call(AdvancedMacros.globals.getLuaThread(caller), LuaValue.valueOf(level),
                        LuaValue.valueOf("Sl"));
    }

    public static LuaValue rayTraceResultToLuaValue(HitResult rtr) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (rtr == null) {
            return LuaValue.FALSE;
        }
        LuaTable result = new LuaTable();

        switch (rtr.getType()) {
            case MISS:
                return LuaValue.FALSE;
            case ENTITY:
                if (rtr instanceof EntityHitResult ertr) {
                    result.set("entity", Utils.entityToTable(ertr.getEntity()));

                }
                break;
            case BLOCK:
                if (rtr instanceof BlockHitResult brtr) {
                    BlockPos pos = brtr.getBlockPos();
                    result.set("side", brtr.getSide().name().toLowerCase());
                    result.set("pos", Utils.blockPosToTable(pos));
                    BlockState ibs = mc.world.getBlockState(pos);
                    BlockEntity te = mc.world.getBlockEntity(pos);
                    result.set("block", Utils.blockToTable(ibs, te));
                }
                break;
            default:
                break;
        }
        LuaTable vec3d = new LuaTable();
        vec3d.set(1, rtr.getPos().x);
        vec3d.set(2, rtr.getPos().y);
        vec3d.set(3, rtr.getPos().z);
        result.set("vec", vec3d);
        //result.set("subHit", rtr.subHit);
        return result;
    }

    //	/**Returns null when done if already on MC thread*/
    //	public static Object runOnMCAndWait(Runnable r) {
    //		if(AdvancedMacros.getMinecraftThread() == Thread.currentThread()) {
    //			r.run();
    //			return null;
    //		}
    //		ListenableFuture<Object> a = MinecraftClient.getInstance().addScheduledTask(r);
    //		while(!a.isDone())
    //			try {Thread.sleep(1);}catch (Exception e) {break;}
    //		try {
    //			return a.get();
    //		} catch (InterruptedException | ExecutionException e) {
    //			e.printStackTrace();
    //			return null;
    //		}
    //	}
    //
    //	public static <T> T  runOnMCAndWait(Callable<T> c) {
    //		if(AdvancedMacros.getMinecraftThread() == Thread.currentThread()) {
    //			try {
    //				return c.call();
    //			} catch (InterruptedException | ExecutionException | ClassCastException e) {
    //				e.printStackTrace();
    //				return null;
    //			} catch (Exception e) {
    //				Utils.logError(e);
    //			}
    //		}
    //		ListenableFuture<T> a = MinecraftClient.getInstance().addScheduledTask(c);
    //		while(!a.isDone())
    //			try {Thread.sleep(1);}catch (Exception e) {break;}
    //		try {
    //			return (T) a.get();
    //		} catch (InterruptedException | ExecutionException | ClassCastException e) {
    //			e.printStackTrace();
    //			return null;
    //		}
    //	}
    //
    //	public static void runOnMCLater(Runnable r) {
    //		//TODO
    //	}

    public static void waitTick() {
        if (AdvancedMacros.getMinecraftThread().equals(Thread.currentThread())) {
            return;
        }
        int t = AdvancedMacros.EVENT_HANDLER.getSTick();
        while (t == AdvancedMacros.EVENT_HANDLER.getSTick()) {
            try {
                synchronized (AdvancedMacros.EVENT_HANDLER.getTickLock()) {
                    AdvancedMacros.EVENT_HANDLER.getTickLock().wait();
                }
            } catch (InterruptedException e) {
            }
        }
    }

    public static double clamp(double min, double value, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int clamp(int min, int value, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static LuaValue parseColor(MapColor mapColor) {
        return new Color(mapColor.color | 0xFF000000).toLuaValue(false);
    }

    public static Varargs varargs(LuaValue... args) {
        return LuaValue.varargsOf(args);
    }

    public static LuaTable toTable(Vec3d motion) {
        LuaTable out = new LuaTable();
        out.set(1, motion.x);
        out.set(2, motion.y);
        out.set(3, motion.z);
        return out;
    }

    public static Item itemFromName(String name) {
        try {
            return new ItemStack(ItemStringReader.item(Registries.ITEM.getReadOnlyWrapper(), new StringReader(name)).item()).getItem();
        } catch (CommandSyntaxException e) {
            return null;
        }
    }

    public static ItemStack itemStackFromName(String name) {
        return itemStackFromName(name, 1);
    }

    public static ItemStack itemStackFromName(String name, int qty) {
        Item i = itemFromName(name);
        if (i == null) {
            return null;
        }
        return new ItemStack(i, qty);
    }

    public static BufferedImage nativeImageToBufferedImage(NativeImage ni) {
        BufferedImage img = new BufferedImage(ni.getWidth(), ni.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getHeight(); x++) {
                img.setRGB(x, y, nativeARGBFlip(ni.getColor(x, y)));
            }
        }
        return img;
    }

    public static void updateNativeImage(BufferedImage img, NativeImage dest) {
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                dest.setColor(x, y, nativeARGBFlip(img.getRGB(x, y)));
            }
        }
    }

    /**
     * Native image has their "RGBA" as ABGR (RGBA backwards) this Flips the B and the R in the int
     * (second and 4th bytes)
     */ //nice and compact
    private static int nativeARGBFlip(int color) {
        return ((color & 0x00_00_00_FF) << 16) | ((color & (0x00_FF_00_00)) >> 16) | (color & 0xFF_00_FF_00); //
    }
	/*private static int nativeARGBFlip(int color) {
												//X Y Z W
		int tmp =  color 		& 0xFF;         //0 0 0 W
		color   =  color 		&~0xFF;         //X Y Z 0
		color   = (color >> 16) & 0xFF | color; //0 0 0 Y -> X Y Z Y
		color   =  color		&~(0xFF << 16); //X 0 Z Y
		color  |=  tmp << 16;                   //X W Z Y
		return color;
	}*/
}
