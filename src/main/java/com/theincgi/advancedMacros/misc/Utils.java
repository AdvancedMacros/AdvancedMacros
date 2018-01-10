package com.theincgi.advancedMacros.misc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import javax.annotation.Nullable;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.gui.Color;
import com.theincgi.advancedMacros.lua.LuaValTexture;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.InventoryPlayer;
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
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextComponent.Serializer;
import net.minecraftforge.common.util.Constants;
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

	public static Color parseColor(Varargs v){
		int a=255,r,g,b;
		switch (v.narg()) {

		case 1:
			LuaValue val = v.arg1();
			if(val.isint()){
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
	public static void logError(LuaError le){
		AdvancedMacros.logFunc.call("&4"+le.toString());
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

	public static LuaTable entityToTable(Entity entity) {
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

		t.set("isInvisible", LuaValue.valueOf(entity.isInvisible()));
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
}