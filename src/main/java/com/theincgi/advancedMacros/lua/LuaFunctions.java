package com.theincgi.advancedMacros.lua;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedList;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import com.theincgi.advancedMacros.misc.CallableTable;
import com.theincgi.advancedMacros.misc.Pair;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextComponent.Serializer;

public class LuaFunctions {
	public static Dictionary<Character, String> chatColors = new Hashtable<>();
	
	static{
		//static init for chatColors
		chatColors.put('0', "black");
		chatColors.put('1', "dark_blue");
		chatColors.put('2', "dark_green");
		chatColors.put('3', "dark_aqua");
		chatColors.put('4', "dark_red");
		chatColors.put('5', "dark_purple");
		chatColors.put('6', "gold");
		chatColors.put('7', "gray");
		chatColors.put('8', "dark_gray");
		chatColors.put('9', "blue");
		chatColors.put('a', "green");
		chatColors.put('b', "aqua");
		chatColors.put('c', "red");
		chatColors.put('d', "light_purple");
		chatColors.put('e', "yellow");
		chatColors.put('f', "white");
	}
	
	public static class Sleep extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue time) {
			try {
				Thread.sleep(time.checklong());
			} catch (InterruptedException e) {
			}
			return LuaValue.NONE;
		}
	}
	
	public static class Say extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg) {
			Minecraft.getMinecraft().player.sendChatMessage(arg.tojstring());
			return LuaValue.NONE;
		}
	}
	
	public static class Log extends VarArgFunction{ // ... this should really be a ONE arg function... but it will work this way too
		
		final char 	OBFUSCATE 	= 'O',   //&_ escape char definitions
					ITALICS 	= 'I',
					UNDERLINE	= 'U',
					BOLD 		= 'B',
					STRIKETHRU 	= 'S';
		boolean obfuscate 	= false,   //text modes
				italics 	= false,
				underline 	= false,
				bold 		= false,
				strikethru 	= false;
		
		final String textHeader 		= "{\"text\":\"";        		//looks like ---> {"text":"
		final String singalQuote 		= "\"";                  		//looks like ---> "    //used to close segements in an easier way to read then "\""
		final String BOLD_SEGMENT   	= ",\"bold\":true";   			//looks like ---> ,"bold":true
		final String ITALIC_SEGMENT   	= ",\"italic\":true";   		//looks like ---> ,"italic":true
		final String UNDERLINE_SEGMENT  = ",\"underlined\":true";   	//looks like ---> ,"underlined":true
		final String STRIKETHRU_SEGMENT = ",\"strikethrough\":true";    //looks like ---> ,"strkethrough":true
		final String OBFUSCATE_SEGMENT  = ",\"obfuscated\":true";   	//looks like ---> ,"obfuscated":true
		final String colorHeader    	= ",\"color\":\""; 				//looks like ---> ,"color":"        //closes text section and starts color
		String fragment;
		String activeColor = "white";
		String parsed;        								//looks like ---> [""
		
		private void appendSegment(){
			parsed += ","+textHeader+fragment+singalQuote;       //text
			if(bold			){ parsed += BOLD_SEGMENT; 		 }   //bold
			if(italics		){ parsed += ITALIC_SEGMENT; 	 }   //italics
			if(underline	){ parsed += UNDERLINE_SEGMENT;  }   //underline
			if(strikethru	){ parsed += STRIKETHRU_SEGMENT; }   //strikethru
			if(obfuscate	){ parsed += OBFUSCATE_SEGMENT;  }   //obfuscate
			parsed += colorHeader+activeColor+singalQuote+"}";   //color
			fragment = "";
		}
		private void resetFormat(){
			//reset values
			bold		= false;
			italics		= false;
			underline	= false;
			strikethru 	= false;
			obfuscate 	= false;
		}
		@Override
		public Varargs invoke(Varargs arg0) {
			try {
				Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(formatString(arg0));
			}catch (Throwable e) {
				//prob tried to log without chat
				e.printStackTrace();
			}
			return LuaValue.NONE;
		}
		public synchronized ITextComponent formatString(Varargs arg0){ //TODO make it so hovering a callable table shows the tooltip
			String toParse;
			ITextComponent out = null;
			for (int i = 1;arg0.narg() > 0; i++) {
//				if(i!=1) {
//					out.appendText(" ");
//				}
				LuaValue arg = arg0.arg1();
				Pair<ITextComponent, Varargs> pair;
				
				if(arg.istable())
					toParse = formatTableForLog(arg.checktable());
				else
					toParse = arg.tojstring();
				
				pair = Utils.toTextComponent(toParse, arg0.subargs(2), true);
				arg0 = pair.b;
				if(out==null)
					out = pair.a;
				else
					out.appendSibling(pair.a);
				
				
			}
			return out;
//			
//			if(arg0.arg(1).istable()){
//				toParse = formatTableForLog(arg0.arg(1).checktable());
//				toParse = "&f"+toParse.replace("\"", "\\\"");
//			}else{
//				toParse = arg0.tojstring().replace("\\", "\\\\");
//				toParse = "&f"+toParse.replace("\"", "\\\"");
//			}
//			toParse+="&f";
//			fragment = "";
//			parsed = "[\"\"";  
//			/*json formating example
//			 * /tellraw @p 
//			 * ["",
//			 * {"text":"Text 1"},
//			 * {"text":"Text 2","color":"blue"},
//			 * {"text":"Text 3","color":"red","bold":true},
//			 * {"text":"this you can hover on","hoverEvent":{
//			 * 		"action":"show_text","value":{
//			 * 			"text":"","extra":[
//			 * 				{"text":"Hover text in color","color":"white"},
//			 * 				{"text":"With a second line in blue","color":"blue"}
//			 * 			]
//			 * 		}
//			 * },
//			 * "color":"none","bold":false}
//			 * ]
//			 */
//			//System.out.println("PARSING DEBUG: "+chatColors.size());
//			
//			//example &O WOW &a&U WOW &I WOW
//			
//			for(int i = 0; i<toParse.length(); i++){ 						//char by char
//				if(toParse.charAt(i)=='&'){									//color format escape char
//					if(i!=toParse.length()-1){								//not a & at the end of a string
//						char next = toParse.charAt(i+1);
//						if(('0'<=next && next <= '9') || ('a'<=next && next <= 'f') ){
//							if(fragment.length()>0){						//if fragment contains text, add to parsed
//								appendSegment();
//								resetFormat();
//							}
//							activeColor = chatColors.get(toParse.charAt(i+1));
//							//System.out.println("PARSE: ACTIVE COLOR IS "+toParse.charAt(i+1) +" which is " +activeColor);
//						}else if(next=='&'){
//							fragment+="&";
//						}else if(next==OBFUSCATE){
//							if(fragment.length()>0){						//if fragment contains text, add to parsed
//								appendSegment();
//							}
//							obfuscate = true;
//						}else if(next==BOLD){
//							if(fragment.length()>0){						//if fragment contains text, add to parsed
//								appendSegment();
//							}
//							bold = true;
//						}else if(next==ITALICS){
//							if(fragment.length()>0){						//if fragment contains text, add to parsed
//								appendSegment();
//							}
//							italics = true;
//						}else if(next==STRIKETHRU){
//							if(fragment.length()>0){						//if fragment contains text, add to parsed
//								appendSegment();
//							}
//							strikethru = true;
//						}else if(next==UNDERLINE){
//							if(fragment.length()>0){						//if fragment contains text, add to parsed
//								appendSegment();
//							}
//							underline = true;
//						}else{ //invalid formating, we'll just print it out as is
//							fragment+="&"+toParse.charAt(i+1);
//						}
//						i++; continue; //skip next char after the &, it was already handled
//					}
//				}//no else, lets the at end fall in as well
//				fragment+=toParse.charAt(i);
//			}
//			//this code was coppied from the loop
//			if(fragment.length()>0){						//if fragment contains text, add to parsed
//				parsed += ","+textHeader+fragment+singalQuote;       //text
//				if(bold			){ parsed += BOLD_SEGMENT; 		 }   //bold
//				if(italics		){ parsed += ITALIC_SEGMENT; 	 }   //italics
//				if(underline	){ parsed += UNDERLINE_SEGMENT;  }   //underline
//				if(strikethru	){ parsed += STRIKETHRU_SEGMENT; }   //strikethru
//				if(obfuscate	){ parsed += OBFUSCATE_SEGMENT;  }   //obfuscate
//				parsed += colorHeader+activeColor+singalQuote+"}";   //color
//				//dont even bother reseting values here, no need
//			}
//			parsed +="]"; //close it all up
//			new ITextComponent.Serializer();
//			//System.out.println("DEBUG PARSED JSON VALUE:\n\t"+parsed);
//			return Serializer.jsonToComponent(parsed);
		}
	}
	
	
	
	public static String formatTableForLog(LuaTable t){
		LinkedList<LuaTable> l = new LinkedList<>();
		String f = "&e"+t.tojstring()+" &f{\n";
		f+= formatTableForLog(t, l, 2);
		f+="&f}";
		return f;
	} 
	
	public static boolean tableContainsKeys(LuaTable table) {
		return !table.next(LuaValue.NIL).arg1().isnil();
	}
	
	private static String formatTableForLog(LuaTable t, LinkedList<LuaTable> antiR, int indent){ //TODO optamize with StringBuilder
		String s = "";
		for(LuaValue k : t.keys()){
			LuaValue v = t.get(k);
			if(v.istable()){
				if(antiR.indexOf(v)>=0){
					//repeat subTable
					s+=rep(" ",indent)+"&f[&c"+escAND(k.tojstring())+"&f] = <&4RECURSIVE&f> &e"+escAND(v.tojstring())+"&f{&4...&f}\n";
				}else{
					LuaTable vTab = v.checktable();
					if(vTab.getmetatable()!=null && vTab.getmetatable().istable() && vTab.getmetatable().get(CallableTable.LUA_FUNCTION_KEY).optboolean(false)) {
						s+=rep(" ",indent)+"&f[&c"+escAND(k.tojstring())+"&f] = &b"+escAND((v.tojstring()));
						if(tableContainsKeys(vTab)){
							s+=" &f{\n";
							s+=formatTableForLog(vTab, antiR, indent+2);
							s+=rep(" ",indent)+"&f}\n";
						}else {
							s+="&f\n";
						}
					}else {
						antiR.add(vTab);
						s+=rep(" ",indent)+"&f[&c"+escAND(k.tojstring())+"&f] = &e"+escAND(v.tojstring()); //TODO remove \n if no keys of any type
						if(tableContainsKeys(vTab)) {
							s+=" &f{\n";
							s+=formatTableForLog(vTab, antiR, indent+2);
							s+=rep(" ",indent)+"&f}\n";
						}else {
							s+=" &f{}\n";
						}
					}
					
					
				}
			}else{
				s+=rep(" ",indent)+"&f[&c"+escAND(k.tojstring())+"&f] = &b";
				if(v.typename().equals("string")){
					s+="&f\"&b"+escAND(v.tojstring())+"&f\""; //added &b to fix color formating in these
					//added .replaceAll so that way color formating doesnt trigger inside the table print
				}else{
					s+=v.isuserdata()?"&d"+escAND(v.tojstring()):escAND(v.tojstring());
				}
				s+="\n";
			}
		}
		if(t.getmetatable()!=null && t.getmetatable().istable()) {
			antiR.add(t.getmetatable().checktable());
			s+=rep(" ",indent)+"&f[&dmetatable&f] = &d"+t.getmetatable().tojstring()+" &f{\n";
			s+=formatTableForLog(t.getmetatable().checktable(), antiR, indent+4);
			s+=rep(" ",indent)+"&f}\n";
		}
		return s;
	}
	private static String escAND(String s) {
		return s.replace("&",  "&&");
	}
	public static String rep(String s, int t){
		String m = "";
		for(int i = 0; i<t; i++){
			m+=s;
		}
		return m;
	}
	
	public static class Debug extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg0) {
			System.out.println("LUA DEBUG: "+arg0.tojstring());
			return LuaValue.NONE;
		}
		
	}
	
	
}