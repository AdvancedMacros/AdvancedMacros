package com.theincgi.advancedMacros.misc;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaFunction;
import org.luaj.vm2_v3_0_1.LuaNumber;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.google.common.util.concurrent.ListenableFuture;
import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.event.ForgeEventHandler;
import com.theincgi.advancedMacros.gui.Color;
import com.theincgi.advancedMacros.lua.LuaValTexture;
import com.theincgi.advancedMacros.lua.util.BufferedImageControls;
import com.theincgi.advancedMacros.lua.util.ContainerControls;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextComponent.Serializer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.oredict.OreDictionary;

public class Utils {


	public static String LuaTableToString(LuaTable sTable){
		return luaTableToString(sTable, new HashMap<LuaTable, Boolean>(), 1, sTable.tojstring()+" {\n");
	}
	private static String luaTableToString(LuaTable t, HashMap<LuaTable, Boolean> printed, int indent, String out){
		//		for(int i = 0; i<indent;i++){
		//			out+="  ";
		//		}
		//		out+=t.tojstring()+" {\n";
		printed.put(t, true);
		for(LuaValue key = t.next(LuaValue.NIL).arg1(); !key.isnil(); key = t.next(key).arg1()){
			for(int i = 0; i<indent;i++){
				out+="  ";
			}
			out+="["+key.tojstring()+"] = ";
			LuaValue val = t.get(key);
			if(val.istable()){
				if(printed.containsKey(val.checktable())){
					out += "[recursive table]\n";
				}else{
					out+=val.tojstring()+" {\n";
					out=luaTableToString(val.checktable(), printed, indent+1, out);
				}
			}else{
				if(val.isstring()){
					out += "\""+val.tojstring()+"\"\n";
				}else{
					out += val.tojstring()+"\n";
				}
			}
		}
		for(int i = 0; i<indent-1;i++){
			out+="  ";
		}
		out+="}\n";
		return out;
	}
	
	public static Color parseColor(LuaValue v) {
		return parseColor(LuaValue.varargsOf(new LuaValue[] {v}));
	} 
	public static Color parseColor(Varargs v, boolean use255Space) {
		if(use255Space)
			return parseColor(v);
		else {
			float a=1,r,g,b;
			switch (v.narg()) {

			case 1:
				LuaValue val = v.arg1();
				if(val.isnumber()){
					return new Color(val.checkint());
				}else if(val.istable()){
					if(val.get("r").isint()&&val.get("g").isint()&&val.get("b").isint()){
						r = (float) val.get("r").checkdouble();
						g = (float) val.get("g").checkdouble();
						b = (float) val.get("b").checkdouble();
						if(val.get("a").isint()){
							a = val.get("a").checkint();
						}
						return new Color(a, r, g, b);
					}else{
						return parseColor(val.checktable().unpack(), use255Space);
					}
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
	private static Color parseColor(Varargs v){
		int a=255,r,g,b;
		switch (v.narg()) {

		case 1:
			LuaValue val = v.arg1();
			if(val.isnumber()){
				return new Color(val.checkint());
			}else if(val.istable()){
				if(val.get("r").isint()&&val.get("g").isint()&&val.get("b").isint()){
					r = val.get("r").checkint();
					g = val.get("g").checkint();
					b = val.get("b").checkint();
					if(val.get("a").isint()){
						a = val.get("a").checkint();
					}
					return new Color(a, r, g, b);
				}else{
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
	/**Generates needed tables if they dont exist<br>
	 * will 
	 * @throws LuaError if something exists and is not a table for one or if it contains spaces<br>
	 * */
	public static LuaValue tableFromProp(LuaTable sTable, String propKey, LuaValue defaultVal){
		if(propKey.contains(" ")||propKey.contains("_")){
			throw new LuaError("Prop key can not have spaces or underscores");
		}
		Scanner s = new Scanner(propKey);
		s.useDelimiter("\\.");
		LuaValue v = LuaValue.NIL;
		while(s.hasNext()){
			String key = s.next();
			//System.out.println("Key> "+key);
			try{
				v = sTable.get(Integer.parseInt(key));
			}catch (Exception e) {
				v = sTable.get(key);
			}

			if(v.isnil()){
				if(s.hasNext())
					sTable.set(key, v = new LuaTable());
				else
					sTable.set(key, v = defaultVal);
			}
			if(s.hasNext()){
				sTable = v.checktable();
			}
		}
		s.close();
		return v;
	}

	public static LuaValTexture checkTexture(LuaValue v){
		if(v.getClass().equals(LuaValTexture.class)){
			return (LuaValTexture)v;
		}
		return null;
	}
	private static LuaValue createJumpToAction(String file, int lineNum) {
		LuaTable table = new LuaTable();
		table.set("hover", "&b&BClick&f to jump to\nline &a&B"+lineNum+"&f in editor");
		table.set("click", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				//TODO check for unsaved changes first or use tab'd editor instead
				AdvancedMacros.editorGUI.openScript(file);
				AdvancedMacros.editorGUI.getCta().jumpToLine(0, lineNum-1);
				ForgeEventHandler.showMenu(AdvancedMacros.editorGUI, AdvancedMacros.macroMenuGui.getGui());
				return null;
			}
		});
		return table;
	}

	@Deprecated
	public static void logError2(Throwable le){
		final String pattern = "((?:[a-zA-Z_0-9./\\\\]+)+):(\\d+)";
		if(le instanceof LuaError) {
			String errText = le.getLocalizedMessage();

			Pattern pat = Pattern.compile(pattern);
			Matcher m = pat.matcher( errText );
			StringBuilder output = new StringBuilder("&c");
			int i = 0;
			LuaTable actions = new LuaTable();
			int actNum = 2; //1 reserved for output string
			while(m.find()) {
				int s = m.start(), e = m.end();
				String fileName = m.group(1);
				int lineNum = Integer.parseInt(m.group(2));
				actions.set(actNum++, createJumpToAction(fileName, lineNum));
				if(i<s){
					output.append("&c");
					output.append(errText.substring(i, s));
				}
				output.append("&U&F");
				output.append(fileName)
				.append(":")
				.append(lineNum);
				i = e;
			}
			if(i < errText.length())
				output.append("&c").append(errText.substring(i));
			actions.set(1, output.toString().replaceAll("\t", "  "));
			//			AdvancedMacros.logFunc.call("&c"+le.toString());			
			AdvancedMacros.logFunc.invoke(actions.unpack());			
		}else {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			le.printStackTrace(pw);
			AdvancedMacros.logFunc.call("&c"+sw.toString());
		}
	}

	public static void logError(Throwable le) {  //FIXME launch matrex vector test from tools
		if(le instanceof LuaError) {
			String errText = le.getLocalizedMessage().replace("\t","  ").replace("\n\n", "\n");
			StringBuilder output = new StringBuilder();
			int start = 0, end = 0;
			LuaTable actions = new LuaTable();
			int actNum = 2; //1 reserved for output string
			int amRootLength = AdvancedMacros.macrosFolder.getAbsolutePath().length()+1;
			boolean valid = true;
			while(end < errText.length()) {
				//matches _: where _ is a letter
				if((start+1 < errText.length()) && Character.isLetter(errText.charAt(start)) && errText.charAt(start+1)==':') {
					end = start+2;
					while(end+1<errText.length() && errText.charAt(end)!='\n' && !(errText.charAt(end)==':' && Character.isDigit(errText.charAt(end+1))))
						end++;
					String fileName = errText.substring(start, end);
					if(errText.charAt(end)=='\n') {
						output.append(errText.substring(start, end+1));
						start = end++;
					}else {
						start = ++end;
						while(end<errText.length() && Character.isDigit(errText.charAt(end)))
							end++;
						if(start==end) {
							output.append(fileName);
						}else {
							int line = Integer.parseInt(errText.substring(start,end));
							start = end;
							File tmp = new File(fileName);
							if(tmp.exists() && tmp.getAbsolutePath().contains(AdvancedMacros.macrosFolder.getAbsolutePath())) {
								fileName = fileName.substring(amRootLength);
								output.append("&4&F")
								.append(fileName)
								.append(':')
								.append(line)
								.append("&c");
								actions.set(actNum++, createJumpToAction(fileName, line));
							}else {
								output.append("&4")
								.append(fileName)
								.append(':')
								.append(line)
								.append("&c");
							}
							
							
						}
					}
				}else {
					//if(errText.charAt(start)!='\n')
						output.append(errText.charAt(start));
					start++;
					end++;
				}
			}
			if(start!=end) {
				output.append("&b")
				.append(errText.substring(start,end));
			}
			actions.set(1, output.toString());
			AdvancedMacros.logFunc.invoke(actions.unpack());
		}else{
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			le.printStackTrace(pw);
			AdvancedMacros.logFunc.call("&c"+sw.toString());
		}
	}

	public static String normalizeText(String keyName) {
		return keyName.charAt(0)+(keyName.substring(1).toLowerCase());
	}
	/**true if not nil*/
	public static boolean checkKey(LuaTable t, String k){
		return !t.get(k).isnil();
	}
	public static boolean isTextColorCode(char charAt) {
		return Character.isDigit(charAt)||('a'<=charAt && charAt<='f');
	}
	public static boolean isTextStyleCode(char c) {
		return c=='B'||c=='I'||c=='O'||c=='S'||c=='U';
	}
	private static final Color[] textCodes={Color.TEXT_0,
			Color.TEXT_1,Color.TEXT_2,Color.TEXT_3,Color.TEXT_4,
			Color.TEXT_5,Color.TEXT_6,Color.TEXT_7,Color.TEXT_8,
			Color.TEXT_9,Color.TEXT_a,Color.TEXT_b,Color.TEXT_c,Color.TEXT_d,Color.TEXT_e,Color.TEXT_f};
	public static Color getTextCodeColor(char sChar){
		if(!isTextColorCode(sChar)){return null;}
		if(Character.isDigit(sChar)){
			return textCodes[sChar-'0'].copy();
		}else{
			return textCodes[10+sChar-'a'].copy();
		}
	}

	public static class TimeFormat{
		public int seconds, mins, hours, days, millis;
	}
	public static TimeFormat formatTime(double time) {
		TimeFormat f = new TimeFormat();
		f.millis = (int) ((time%1)*100);
		time-= ((int) (time%1));
		time-= (f.seconds = (int) (time%60));
		time/=60;
		time-= (f.mins = (int) (time%(60)));
		time/=60;
		time-= (f.hours= (int) (time%24));
		time/=24;
		f.days = (int) time;
		return f;
	}
	public static LuaValue itemStackToLuatable(ItemStack stack) {
		if(stack.isEmpty()){return LuaValue.FALSE;}
		LuaTable table = new LuaTable();
		table.set("name", stack.getDisplayName()==null?LuaValue.NIL:LuaValue.valueOf(stack.getDisplayName()));
		table.set("id", stack.getItem().getRegistryName().toString());
		table.set("dmg", stack.getItemDamage());
		table.set("maxDmg", stack.getMaxDamage());
		table.set("amount", stack.getCount());
		table.set("repairCost", stack.getRepairCost());
		table.set("enchants", NBTUtils.fromTagList(stack.getEnchantmentTagList()));
		table.set("nbt", NBTUtils.fromCompound(stack.serializeNBT()));
		return table;
	}
	public static LuaTable blockToTable(IBlockState blockState, @Nullable TileEntity te) {
		Block block = blockState.getBlock();
		LuaTable out = new LuaTable();
		out.set("id", block.getRegistryName().toString());
		out.set("name", block.getLocalizedName());
		out.set("dmg", block.getMetaFromState(blockState));
		if(te != null) {
			out.set("nbt", Utils.NBTUtils.fromCompound(te.serializeNBT()));
		}
		String tool = block.getHarvestTool(blockState);
		if(tool!=null)
			out.set("harvestTool", tool);

		return out;
	}
	public static boolean itemsEqual(ItemStack sourceStack, ItemStack sinkStack) {
		if(sourceStack.isEmpty()&&sinkStack.isEmpty())return true;
		if(!sourceStack.getItem().equals(sinkStack.getItem())) return false;
		if(sourceStack.getItemDamage()==OreDictionary.WILDCARD_VALUE || sinkStack.getItemDamage()==OreDictionary.WILDCARD_VALUE) return true;
		return sourceStack.getItemDamage()==sinkStack.getItemDamage();
	}
	public static LuaValue inventoryToTable(InventoryPlayer inventory, boolean collapseEmpty) {
		LuaTable t = new LuaTable();
		for(int i = 0; i<inventory.getSizeInventory(); i++) {
			LuaValue stack = itemStackToLuatable(inventory.getStackInSlot(i));
			if(collapseEmpty && stack.isboolean() && stack.checkboolean()==false)continue;
			t.set(i, stack);
		}
		t.set("mouse", itemStackToLuatable(inventory.getItemStack()));
		return t;
	}
	public static LuaValue effectToTable(PotionEffect pe) {
		LuaTable table = new LuaTable();
		table.set("id", pe.getEffectName());
		table.set("strength", pe.getAmplifier());
		table.set("duration", pe.getDuration());
		table.set("showsParticles", LuaValue.valueOf(pe.doesShowParticles()));
		table.set("isAmbient", LuaValue.valueOf(pe.getIsAmbient()));
		return table;
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
		if(entity==null) return LuaValue.FALSE;
		LuaTable t = new LuaTable();
		t.set("name", entity.getName());
		t.set("class", entity.getClass().getName());
		//t.set("inventory", Utils.inventoryToTable(entity.inventory, !(entity instanceof EntityPlayerSP)));
		{
			LuaTable pos = new LuaTable();
			pos.set(1, LuaValue.valueOf(entity.posX));
			pos.set(2, LuaValue.valueOf(entity.posY));
			pos.set(3, LuaValue.valueOf(entity.posZ));
			t.set("pos", pos);
		}
		t.set("dimension", LuaValue.valueOf(entity.dimension));
		t.set("pitch", entity.rotationPitch);
		t.set("yaw", entity.rotationYaw);
		t.set("fallDist", entity.fallDistance);
		t.set("height", entity.height);
		t.set("width", entity.width);
		t.set("hurtResTime", entity.hurtResistantTime);
		//t.set("isAirborne", LuaValue.valueOf(player.isAirBorne));
		t.set("isCollidedHorz", LuaValue.valueOf(entity.collidedHorizontally));
		t.set("isCollidedVert", LuaValue.valueOf(entity.collidedVertically));
		//t.set("swingProgress", LuaValue.valueOf(entity.swingProgress));
		//t.set("maxHurtResTime", LuaValue.valueOf(entity.maxHurtResistantTime));
		t.set("isNoClip", LuaValue.valueOf(entity.noClip));
		t.set("onGround", LuaValue.valueOf(entity.onGround));
		t.set("isInvulnerable", LuaValue.valueOf(entity.getIsInvulnerable()));
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
		t.set("team", entity.getTeam()==null?"none":entity.getTeam().getName());
		{
			LuaTable velocity = new LuaTable();
			Entity e = entity.getLowestRidingEntity();
			velocity.set(1, LuaValue.valueOf(e.motionX));
			velocity.set(2, LuaValue.valueOf(e.motionY));
			velocity.set(3, LuaValue.valueOf(e.motionZ));
			t.set("velocity", velocity);
		}
		//t.set("luck", entity.getLuck());
		if(entity instanceof EntityLiving) {
			EntityLiving living = (EntityLiving) entity;
			t.set("health", living.getHealth());
			t.set("isOnLadder", LuaValue.valueOf(living.isOnLadder()));
			{
				LuaTable effects = new LuaTable();
				int i = 1;
				for(PotionEffect pe : living.getActivePotionEffects()) {
					effects.set(i++, Utils.effectToTable(pe));
				}
				t.set("potionEffects", effects);
			}
		}
		//t.set("hunger", entity.getFoodStats().getFoodLevel());
		t.set("air", entity.getAir());

		//t.set("isSneaking", LuaValue.valueOf(entity.isSneaking()));

		t.set("isInWater", LuaValue.valueOf(entity.isInWater()));
		t.set("isInLava", LuaValue.valueOf(entity.isInLava()));
		t.set("immuneToFire", LuaValue.valueOf(entity.isImmuneToFire()));
		t.set("isImmuneToExplosion", LuaValue.valueOf(entity.isImmuneToExplosions()));
		t.set("isOnFire", LuaValue.valueOf(entity.isBurning()));
		t.set("isSprinting", LuaValue.valueOf(entity.isSprinting()));
		t.set("entityRiding", Utils.entityToTable(entity.getRidingEntity()));
		t.set("isInvisible", LuaValue.valueOf(entity.isInvisible()));
		try{
			t.set("nbt", NBTUtils.fromCompound(entity.serializeNBT()));
		}catch (NullPointerException e) {
			try {
				NBTTagCompound ret = new NBTTagCompound();
				entity.writeToNBT(ret);
				t.set("nbt", NBTUtils.fromCompound(ret));
			
			}catch(Exception ex) {
				ex.printStackTrace();
				t.set("nbt", LuaValue.FALSE);
			}
		}
		t.set("uuid", LuaValue.valueOf(entity.getUniqueID().toString()));
		{
			RayTraceResult rtr = entity.rayTrace(8, 0);
			if(rtr!=null) {
				BlockPos lookingAt = rtr.getBlockPos();
				if(lookingAt!=null) {
					LuaTable look = new LuaTable();
					look.set(1, LuaValue.valueOf(lookingAt.getX()));
					look.set(2, LuaValue.valueOf(lookingAt.getY()));
					look.set(3, LuaValue.valueOf(lookingAt.getZ()));
					t.set("lookingAt", look);
				}
			}
		}
		return t;
	}

	public static ITextComponent luaTableToComponentJson(LuaTable table){
		String msg = "[\"\","; //["",

		if(table.length()==0) {
			msg+=parseTableToComJson(table)+"]";
		}else {
			for(int i = 1; i<=table.length(); i++) {
				LuaTable t = table.get(i).checktable();
				msg+=parseTableToComJson(t);
				if(i<table.length())
					msg+=",";
			}
		}
		System.out.println(msg+"]");
		return Serializer.jsonToComponent(msg+"]");
		//

	}
	private static String parseTableToComJson(LuaTable table) {
		String msg="";
		if(table.get("text").isnil()) {
			throw new LuaError("No text property found");
		}
		msg+="{\"text\":\""+table.get("text").tojstring();
		if(!table.get("color").isnil()) {
			String code = table.get("color").checkjstring();
			if(hasColorCode(code)!=null) {
				msg+="\",\"color\":\""+hasColorCode(code)+"\"";
			}
			if(code.contains("B")) {
				msg+=",\"bold\":true";
			}
			if(code.contains("S")) {
				msg+=",\"strikethrough\":true";
			}
			if(code.contains("O")) {
				msg+=",\"obfuscated\":true";
			}
			if(code.contains("U")) {
				msg+=",\"underlined\":true";
			}
			if(code.contains("I")) {
				msg+=",\"italic\":true";
			}
		}
		if(!table.get("click").isnil()) {
			LuaTable clickDetails = table.get("click").checktable();
			if(clickDetails.get("type").isnil()) {
				throw new LuaError("No click type set [sTable.type = url/suggestCommand/runCommand]");
			}
			if(clickDetails.get("value").isnil()) {
				throw new LuaError("No 'value' set");
			}
			String actType = clickDetails.get("type").checkjstring();
			switch(clickDetails.get("type").checkjstring()) {
			case "url":
			case "open_url":
				actType="open_url";
				break;
			case "suggestCommand":
			case "suggest_command":
				actType="suggest_command";
				break;
			case "runCommand":
			case "run_command":
				actType="run_command";
				break;
				//				case "suggestText":
				//				case "insertion":
				//					actType="insertion";
				//					break;
			default:
				throw new LuaError("Invalid click type ("+clickDetails.get("type").tojstring()+") use: [url/suggestCommand/runCommand]");
			}
			msg+=",\"clickEvent\":{\"action\":\"" + actType + "\",\"value\":\""+ clickDetails.get("value").checkjstring() +"\"}";
		}
		if(!table.get("tooltip").isnil()) {
			msg+=",\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + table.get("tooltip") + "\"}";
		}
		msg+="}";
		return msg;
	}
	public static String hasColorCode(String code) {
		if(code.contains("0"))
			return "black";
		if(code.contains("1"))
			return "dark_blue";
		if(code.contains("2"))
			return "dark_green";
		if(code.contains("3"))
			return "dark_aqua";
		if(code.contains("4"))
			return "dark_red";
		if(code.contains("5"))
			return "dark_purple";
		if(code.contains("6"))
			return "gold";
		if(code.contains("7"))
			return "light_gray";
		if(code.contains("8"))
			return "gray";
		if(code.contains("9"))
			return "blue";
		if(code.contains("a"))
			return "green";
		if(code.contains("b"))
			return "aqua";
		if(code.contains("c"))
			return "red";
		if(code.contains("d"))
			return "light_purple";
		if(code.contains("e"))
			return "yellow";
		if(code.contains("f"))
			return "white";
		return null;
	}

	public static class NBTUtils{
		private static LuaTable fromCompound(NBTTagCompound comp) {
			LuaTable out = new LuaTable();
			for(String k : comp.getKeySet())
				out.set(k, fromBase(comp.getTag(k)));
			return out;
		}
		private static LuaValue fromBase(NBTBase tag) {
			LuaValue thisTag;
			if(tag instanceof NBTTagByte){
				thisTag = LuaValue.valueOf(((NBTTagByte) tag).getByte());
			}else if(tag instanceof NBTTagShort){
				thisTag = LuaValue.valueOf(((NBTTagShort) tag).getShort());
			}else if(tag instanceof NBTTagInt){
				thisTag = LuaValue.valueOf(((NBTTagInt) tag).getInt());
			}else if(tag instanceof NBTTagLong){
				thisTag = LuaValue.valueOf(((NBTTagLong) tag).getLong());
			}else if(tag instanceof NBTTagFloat){
				thisTag = LuaValue.valueOf(((NBTTagFloat) tag).getFloat());
			}else if(tag instanceof NBTTagDouble){
				thisTag = LuaValue.valueOf(((NBTTagDouble) tag).getDouble());
			}else if(tag instanceof NBTTagByteArray){
				byte[] bytes = ((NBTTagByteArray) tag).getByteArray();
				thisTag = new LuaTable();
				for(int j = 0; j<bytes.length; j++){
					thisTag.set(j+1, LuaValue.valueOf(bytes[j]));
				}
			}else if(tag instanceof NBTTagString){
				thisTag = LuaValue.valueOf(((NBTTagString) tag).getString());
			}else if(tag instanceof NBTTagList){
				thisTag = fromTagList((NBTTagList) tag);
			}else if(tag instanceof NBTTagCompound){
				thisTag = fromCompound((NBTTagCompound) tag);
			}else if(tag instanceof NBTTagIntArray){
				int[] ints = ((NBTTagIntArray) tag).getIntArray();
				thisTag = new LuaTable();
				for(int j = 0; j<ints.length; j++){
					thisTag.set(j+1, LuaValue.valueOf(ints[j]));
				}
			}else{
				thisTag = LuaValue.NIL;
			}
			return thisTag;
		}
		private static LuaTable fromTagList(NBTTagList list) {
			if(list==null) {
				//System.err.println("Warning: Utils.fromTagList list was null");
				return new LuaTable();
			}
			LuaTable table = new LuaTable();
			Iterator<NBTBase> iter = list.iterator();
			while(iter.hasNext()) {
				NBTBase tag = iter.next();
				table.set(table.length()+1, fromBase(tag));
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

	public static Varargs pinvoke(LuaFunction func, LuaValue...luaValues) {
		try {
			return func.invoke(luaValues);
		}catch (Throwable e) {
			logError(toLuaError(e));
			return null;
		}
	}
	/**nah, its invoke, but it has one return*/
	public static LuaValue pcall(LuaFunction func, LuaValue...luaValues) {
		try {
			return func.invoke(luaValues).arg1();
		}catch (Throwable e) {
			logError(toLuaError(e));
			return LuaValue.NIL;
		}
	}
	public static Varargs pinvoke(LuaFunction func, Varargs luaValues) {
		try {
			return func.invoke(luaValues);
		}catch (Throwable e) {
			logError(toLuaError(e));
			return null;
		}
	}
	/**nah, its invoke, but it has one return*/
	public static LuaValue pcall(LuaFunction func, Varargs luaValues) {
		try {
			return func.invoke(luaValues).arg1();
		}catch (Throwable e) {
			logError(toLuaError(e));
			return LuaValue.NIL;
		}
	}
	public static LuaValue toTable(Set<String> keySet) {
		LuaTable t = new LuaTable();
		for(String s : keySet)
			t.set(t.length()+1, s);
		return t;
	}

	public static void debugPrint( LuaTable t ) {
		System.out.println( LuaTableToString(t) );
	}


	public static File parseFileLocation(LuaValue arg0) {
		return parseFileLocation(arg0.isnil()?"":arg0.tojstring(), 1);
	}
	public static File parseFileLocation(LuaValue arg0, LuaValue level) {
		return parseFileLocation(arg0.isnil()?"":arg0.tojstring(), level.optint(1));
	}
	public static File parseFileLocation(String arg, int level) {
		return parseFileLocation(Thread.currentThread(), arg, level);
	}
	public static File parseFileLocation(Thread caller, String arg, int level) {
		if(arg==null)
			arg = "";

		File file = null;
		if(arg.startsWith("/") || arg.startsWith("\\"))
			file = new File(arg.substring(1));
		else if(arg.startsWith("~"))
			file = new File(AdvancedMacros.macrosRootFolder, arg.substring(1));
		else {
			LuaValue v = Utils.getDebugStacktrace(caller, level);
			if(v.isnil())
				throw new LuaError("Unable to get local path of file");
			String m = v.get("short_src").tojstring();
			m = m.substring(0, Math.max(0, Math.max(m.lastIndexOf("\\"),m.lastIndexOf("/"))));
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
		if(v instanceof LuaValTexture){
			lvt = (LuaValTexture) v;
		}if(v instanceof BufferedImageControls) {
			//if(((BufferedImageControls) v).getLuaValTexture() == null) throw new LuaError("Texture not created");
			lvt = ((BufferedImageControls) v).getLuaValTexture();
		}else if(v.isstring()){
			lvt = Utils.checkTexture(Settings.getTextureID(v.checkjstring()));
			//		}else if(v.isnil()){
			//			lvt = null;
			if(lvt==null)
				return def;
		}else {
			lvt = def;
		}
		return lvt;
	}

	public static char mcSelectCode = '\u00A7';
	public static String toMinecraftColorCodes(String text) {
		char sel = '\u00A7';
		String reset = sel+"r";
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
		.replaceAll("&U",         sel + "n")
		.replaceAll("&B",         sel + "l")
		.replaceAll("&O",         sel + "k")
		.replaceAll("&S",         sel + "m")
		.replaceAll("&I",         sel + "o")
		.replaceAll("&&", "&")
		;
	}
	public static String fromMinecraftColorCodes(String text) {
		return text
				.replaceAll("&", "&&")
				.replaceAll("\u00A7", "&")
				.replaceAll("&k", "&O") //Obfuscated
				.replaceAll("&l", "&B") //Bold
				.replaceAll("&m", "&S") //Strikethru
				.replaceAll("&o", "&I") //Italics
				.replaceAll("&r", "&f")  //reset (to white in this case)
				.replaceAll("&n", "&U")  //Underline
				;
	}
	/**
	 * @param allowFunctions defaults true
	 * */
	public static Pair<ITextComponent, Varargs> toTextComponent(String codedText, Varargs args, boolean allowHover) {
		return toTextComponent(codedText, args, allowHover, true);
	}

	public static Pair<ITextComponent, Varargs> toTextComponent(String codedText, Varargs args, boolean allowHover, boolean allowFunctions) {
		if(args == null) args = new LuaTable().unpack();
		ITextComponent out = new TextComponentString("");
		StringBuilder temp = new StringBuilder();
		Boolean bold = null, italics = null, obfusc = null, strike = null, underline = null;
		TextFormatting color = null;
		Style pStyle = out.getStyle();
		pStyle.setBold(false);
		pStyle.setItalic(false);
		pStyle.setObfuscated(false);
		pStyle.setStrikethrough(false);
		pStyle.setUnderlined(false);
		//lua text component ce? click event?
		boolean ltcce = false;
		int argNum = 1;
		ClickEvent clickEvent = null;
		HoverEvent hoverEvent = null;
		for(int i = 0; i<codedText.length(); i++) {
			char c = codedText.charAt(i);
			if(c!='&')
				temp.append(c);
			else {
				if(i<codedText.length()-1) {
					char next = codedText.charAt(i+1);
					if(next == '&') {
						temp.append(next);
						i++;
					}else if(isTextColorCode(next) || isTextStyleCode(next) || isSpecialCode(next) || next == '&') {
						i++;
						if(temp.length() > 0) {
							ITextComponent component = ltcce&&allowFunctions? new LuaTextComponent(temp.toString(), args.arg(argNum++), allowHover) : new TextComponentString(temp.toString());
							Style style = component.getStyle();
							style.setBold(bold);
							style.setItalic(italics);
							style.setObfuscated(obfusc);
							style.setStrikethrough(strike);
							style.setUnderlined(underline);
							style.setColor(color);
							style.setParentStyle(pStyle);
							if(clickEvent != null)
								style.setClickEvent(clickEvent);
							out.appendSibling(component);
							if (hoverEvent != null) 
								style.setHoverEvent(hoverEvent);
							bold = italics = obfusc = strike = underline = null;
							//color = null;
							ltcce = false;
							clickEvent = null;
							hoverEvent = null;
							temp = new StringBuilder();
							pStyle = component.getStyle();
						}
						if (isTextColorCode(next)) {
							color = getTextFormatingColor(next);
							bold = italics = obfusc = strike = underline = false;
						}/*else if(next == 'x') { //custom color
							color = parseColor(args.arg(argNum++));
							bold = italics = obfusc = strike = underline = false;
						}*/else if(isTextStyleCode(next)) {
							switch (next) {
							case 'B':
								bold = true;
								break;
							case 'I':
								italics = true;
								break;
							case 'O':
								obfusc = true;
								break;
							case 'S':
								strike = true;
								break;
							case 'U':
								underline = true;
								break;
							default:
								break;
							}
						}else if(next == 'F') { //Function/table
							ltcce = true;
						}else if(next == 'R') { //execute
							String cText, hText;
							if(args.arg(argNum).istable() && !args.arg(argNum).get("click").isnil())
								cText = args.arg(argNum).get("click").tojstring();
							else
								cText = args.arg(argNum).tojstring();


							if(args.arg(argNum).istable() && !args.arg(argNum).get("hover").isnil())
								hText = args.arg(argNum).get("hover").tojstring();
							else
								hText = "Run: &b"+cText;
							argNum++;
							clickEvent = new ClickEvent(Action.RUN_COMMAND, cText);
							hoverEvent = new HoverEvent(net.minecraft.util.text.event.HoverEvent.Action.SHOW_TEXT, toTextComponent(hText, null, false, false).a);
						}else if(next == 'T') { //type (suggest)
							String cText, hText;
							if(args.arg(argNum).istable() && !args.arg(argNum).get("click").isnil())
								cText = args.arg(argNum).get("click").tojstring();
							else
								cText = args.arg(argNum).tojstring();


							if(args.arg(argNum).istable() && !args.arg(argNum).get("hover").isnil())
								hText = args.arg(argNum).get("hover").tojstring();
							else
								hText = "Type: &b"+cText;
							argNum++;
							clickEvent = new ClickEvent(Action.SUGGEST_COMMAND, cText);
							hoverEvent = new HoverEvent(net.minecraft.util.text.event.HoverEvent.Action.SHOW_TEXT, toTextComponent(hText, null, false, false).a);
						}else if(next == 'L') { //Link
							String cText, hText;
							if(args.arg(argNum).istable() && !args.arg(argNum).get("click").isnil())
								cText = args.arg(argNum).get("click").tojstring();
							else
								cText = args.arg(argNum).tojstring();


							if(args.arg(argNum).istable() && !args.arg(argNum).get("hover").isnil())
								hText = args.arg(argNum).get("hover").tojstring();
							else
								hText = "URL: &b&U"+cText;
							argNum++;
							clickEvent = new ClickEvent(Action.OPEN_URL, cText);
							hoverEvent = new HoverEvent(net.minecraft.util.text.event.HoverEvent.Action.SHOW_TEXT, toTextComponent(hText, null, false, false).a);
						}else if(next == 'N') {
							String hText;

							if(args.arg(argNum).istable() && !args.arg(argNum).get("hover").isnil())
								hText = args.arg(argNum).get("hover").tojstring();
							else
								hText = args.arg(argNum).tojstring();
							argNum++;

							hoverEvent = new HoverEvent(net.minecraft.util.text.event.HoverEvent.Action.SHOW_TEXT, toTextComponent(hText, null, false, false).a);
						}
					}
				}
			}
		}
		if(temp.length() > 0) {
			ITextComponent component = ltcce&&allowFunctions? new LuaTextComponent(temp.toString(), args.arg(argNum++), allowHover) : new TextComponentString(temp.toString());
			Style style = component.getStyle();
			style.setBold(bold);
			style.setItalic(italics);
			style.setObfuscated(obfusc);
			style.setStrikethrough(strike);
			style.setUnderlined(underline);
			style.setColor(color);
			style.setParentStyle(pStyle);
			if(clickEvent != null)
				style.setClickEvent(clickEvent);
			out.appendSibling(component);
			if (hoverEvent != null) 
				style.setHoverEvent(hoverEvent);
			temp = new StringBuilder();
			pStyle = component.getStyle();
		}
		return new Pair<ITextComponent, Varargs>(out, args.subargs(argNum));
	}

	public static Pair<String, LuaTable> codedFromTextComponent(ITextComponent message) {
		return codedFromTextComponent(message, true);
	}
	public static Pair<String, LuaTable> codedFromTextComponent(ITextComponent message, boolean includeActions) {
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
		for(ITextComponent com : message ) {
			if(com.getUnformattedComponentText().isEmpty()) continue;
			Style s = com.getStyle();
			LuaTable action = null;
			String formating = "&f"+fromMinecraftColorCodes(s.getFormattingCode().toString());
			if(s.getClickEvent()!=null && includeActions) {
				action = new LuaTable();
				action.set("click", s.getClickEvent().getValue());
				switch (s.getClickEvent().getAction()) {
				case OPEN_URL:
					formating +="&L";
					break;
				case RUN_COMMAND:
					formating += "&R";
					break;
				case SUGGEST_COMMAND:
					formating += "&T";
					break;
				default:
					action = null;
				}
			}
			if(s.getHoverEvent()!=null && includeActions) {
				switch (s.getHoverEvent().getAction()) {
				case SHOW_TEXT:
					if(action==null)
						formating+="&N";
					action = (action==null)?new LuaTable() : action;
					action.set("hover", codedFromTextComponent(s.getHoverEvent().getValue(), false).a);
					break;
				default:
				}
			}
			if(action!=null)
				actions.set(actionNum++, action);
			out.append(formating);
			out.append(com.getUnformattedComponentText().replace("&", "&&"));
		}
		return new Pair<String, LuaTable>(out.toString(), actions);
	}
	private static boolean isSpecialCode(char c) {
		return "FRTLN".indexOf(c) >= 0; //Function, Execute, Type, Url
	}
	private static TextFormatting getTextFormatingColor(char c) {
		//		 BLACK("BLACK", '0', 0),
		//		    DARK_BLUE("DARK_BLUE", '1', 1),
		//		    DARK_GREEN("DARK_GREEN", '2', 2),
		//		    DARK_AQUA("DARK_AQUA", '3', 3),
		//		    DARK_RED("DARK_RED", '4', 4),
		//		    DARK_PURPLE("DARK_PURPLE", '5', 5),
		//		    GOLD("GOLD", '6', 6),
		//		    GRAY("GRAY", '7', 7),
		//		    DARK_GRAY("DARK_GRAY", '8', 8),
		//		    BLUE("BLUE", '9', 9),
		//		    GREEN("GREEN", 'a', 10),
		//		    AQUA("AQUA", 'b', 11),
		//		    RED("RED", 'c', 12),
		//		    LIGHT_PURPLE("LIGHT_PURPLE", 'd', 13),
		//		    YELLOW("YELLOW", 'e', 14),
		//		    WHITE("WHITE", 'f', 15),
		switch (c) {
		case '0': return TextFormatting.BLACK;
		case '1': return TextFormatting.DARK_BLUE;
		case '2': return TextFormatting.DARK_GREEN;
		case '3': return TextFormatting.DARK_AQUA;
		case '4': return TextFormatting.DARK_RED;
		case '5': return TextFormatting.DARK_PURPLE;
		case '6': return TextFormatting.GOLD;
		case '7': return TextFormatting.GRAY;
		case '8': return TextFormatting.DARK_GRAY;
		case '9': return TextFormatting.BLUE;
		case 'a': return TextFormatting.GREEN;
		case 'b': return TextFormatting.AQUA;
		case 'c': return TextFormatting.RED;
		case 'd': return TextFormatting.LIGHT_PURPLE;
		case 'e': return TextFormatting.YELLOW;
		case 'f': return TextFormatting.WHITE;
		default:
			return null;
		}
	}
	
//	//why is there also runOnMCAndWait...
//	@Deprecated
//	public static void runOnMCThreadAndWait(Runnable r){
//		if(AdvancedMacros.getMinecraftThread() == Thread.currentThread()) {
//			r.run();
//			return;
//		}
//		ListenableFuture<Object> f = AdvancedMacros.getMinecraft().addScheduledTask(r);
//		while(!f.isDone()) try{Thread.sleep(5);}catch (InterruptedException ie) {return;}
//	}
	public static LuaValue toTable(Container container) {
		return toTable(container, false);
	}
	public static LuaValue toTable(Container container, boolean isReady) {
		LuaTable out = new LuaTable();
		LuaTable slots = new LuaTable();
		for(int i = 0; i<container.inventoryItemStacks.size(); i++) {
			slots.set(i, itemStackToLuatable(container.inventoryItemStacks.get(i)));
		}
		out.set("slots", slots);
		out.set("controls", new ContainerControls(container));
		out.set("isReady", LuaValue.valueOf(isReady));
		return out;
	}
	/**
	 * @param if not optional LuaError may be thrown, null in pair if unable to make a vector otherwise.
	 * @param isAngular defines if this vector uses yaw,pitch or x,y,z to define it.
	 * */
	public static Pair<Vec3d, Varargs> consumeVector(Varargs args, boolean optional, boolean isAngular) {
		if(args.istable(1) && !args.arg1().get(1).istable()) { //is table, but does not contain table
			Pair<Vec3d, Varargs> p = consumeVector(args.checktable(1).unpack(), optional, isAngular);
			p.b = args.subargs(2);
			return p;
		}else if(args.isnumber(1) && args.isnumber(2) && args.isnumber(3) && !isAngular) {
			Vec3d v = new Vec3d(args.checkdouble(1), args.checkdouble(2), args.checkdouble(3));
			return new Pair<Vec3d, Varargs>(v, args.subargs(4));
		}else if(args.isnumber(1) && args.isnumber(2) && isAngular) {
			float yaw = (float) args.checkdouble(1), pitch = (float) args.checkdouble(2);
			//yaw = Math.toRadians(yaw);
			//			pitch = Math.toRadians(pitch);
			////			double x = Math.cos(yaw), z = Math.sin(yaw);
			////			double y = -Math.sin(pitch);
			////			Vec3d v = new Vec3d(x, y, z);
			//			Entity
			//			v = v.rotateYaw((float) Math.toRadians(-90));
			//			v = v.normalize();
			float f = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
			float f1 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
			float f2 = -MathHelper.cos(-pitch * 0.017453292F);
			float f3 = MathHelper.sin(-pitch * 0.017453292F);
			Vec3d v = new Vec3d((f1 * f2), f3, (f * f2));
			return new Pair<Vec3d, Varargs>(v, args.subargs(3));
		}
		if(!optional)
			throw new LuaError(isAngular?"Invalid direction, must be {yaw, pitch} or yaw, pitch":"Invalid vector, must be {x,y,z} or x,y,z");
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
				.call(AdvancedMacros.globals.getLuaThread(caller),LuaValue.valueOf(level),
						LuaValue.valueOf("Sl"));
	}
	public static LuaValue rayTraceResultToLuaValue(RayTraceResult rtr) {
		Minecraft mc = AdvancedMacros.getMinecraft();
		if(rtr==null) return LuaValue.FALSE;
		LuaTable result = new LuaTable();

		switch (rtr.typeOfHit) {
		case MISS:
			return LuaValue.FALSE;
		case ENTITY:
			result.set("entity", Utils.entityToTable(rtr.entityHit));
			break;
		case BLOCK:
			BlockPos pos = rtr.getBlockPos();
			result.set("pos", Utils.blockPosToTable(pos));
			IBlockState ibs = mc.world.getBlockState(pos);
			TileEntity te = mc.world.getTileEntity(pos);
			result.set("block", Utils.blockToTable(ibs, te));
		default:
			break;
		}
		LuaTable vec3d = new LuaTable();
		vec3d.set(1, rtr.hitVec.x);
		vec3d.set(2, rtr.hitVec.y);
		vec3d.set(3, rtr.hitVec.z);
		result.set("vec", vec3d);
		if( rtr.sideHit != null)
			result.set("side", rtr.sideHit.name().toLowerCase());
		result.set("subHit", rtr.subHit);
		return result;
	}
	
	
	/**Returns null when done if already on MC thread*/
	public static Object runOnMCAndWait(Runnable r) {
		if(AdvancedMacros.getMinecraftThread() == Thread.currentThread()) {
			r.run();
			return null;
		}
		ListenableFuture<Object> a = AdvancedMacros.getMinecraft().addScheduledTask(r);
		while(!a.isDone())
			try {Thread.sleep(1);}catch (Exception e) {break;}
		try {
			return a.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static <T> T  runOnMCAndWait(Callable<T> c) {
		if(AdvancedMacros.getMinecraftThread() == Thread.currentThread()) {
			try {
				return c.call();
			} catch (InterruptedException | ExecutionException | ClassCastException e) {
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				Utils.logError(e);
			}
		}
		ListenableFuture<T> a = AdvancedMacros.getMinecraft().addScheduledTask(c);
		while(!a.isDone())
			try {Thread.sleep(1);}catch (Exception e) {break;}
		try {
			return (T) a.get();
		} catch (InterruptedException | ExecutionException | ClassCastException e) {
			e.printStackTrace();
			return null;
		}
	}
	public static void waitTick() {
		int t = AdvancedMacros.forgeEventHandler.getSTick();
		while(t==AdvancedMacros.forgeEventHandler.getSTick()){
			try {
				Thread.sleep(5); //tick should be 20, lil bit faster this way
			} catch (InterruptedException e) {} 
		}
	}
	public static double clamp(double min, double value, double max) {
		return Math.max(min, Math.min(max, value));
	}
	public static int clamp(int min, int value, int max) {
		return Math.max(min, Math.min(max, value));
	}
	public static LuaValue parseColor(MapColor mapColor) {
		return new Color(mapColor.colorValue | 0xFF000000).toLuaValue(false);
	}
	public static Varargs varargs(LuaValue...args) {
		return LuaValue.varargsOf(args);
	}
}