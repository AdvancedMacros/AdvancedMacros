package com.theincgi.advancedMacros.lua;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

import org.luaj.vm2_v3_0_1.LuaClosure;
import org.luaj.vm2_v3_0_1.LuaFunction;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.gui.Color;
import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.misc.CallableTable;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class DocumentationManager {
	private final int textColor = Color.WHITE.toInt();
	private final int tooltipFrame = 0xFF_82_c6_f4;
	private final int tooltipFill = 0xc0_22_66_c4; //aa_rr_gg_bb
	public void tooltip(Gui g, String fName, int x, int y, int screenWid, int screenHei) {
		//System.out.println("ToolTipping");
		LinkedList<String> text = getLine1(fName);
		//System.out.println(fName+" "+text);
		if(text==null) return;
		int wid = 0;
		for(int l = 0; l<text.size(); l++) {
			wid = Math.max(wid, g.getFontRend().getStringWidth(text.get(l)));
		}
		int hei = text.size()*12;
		int prefX = Math.min(x+2, screenWid-wid-5);
		int prefY = Math.min(y+10, screenHei-hei-5);
		g.drawBoxedRectangle(prefX, prefY, wid+4, hei+4, tooltipFrame, tooltipFill);
		for(int l = 0; l<text.size(); l++)
			g.getFontRend().drawString(text.get(l), prefX+2, prefY+4+(l*12), textColor);
	}

	private String lastRequestL1 = null; //repeated method call indexing
	private LinkedList<String> lastResponseL1 = null;
	private LinkedList<String> getLine1(String fName) {
		String withCase = fName;
		if(fName==null)
			return null;
		fName = fName.toLowerCase();
		if(lastRequestL1!=null && lastRequestL1.equals(fName)) {
			//System.out.println("Dup");
			return lastResponseL1;
		}

		//System.out.println("New Q "+fName);
		lastRequestL1=fName;
		InputStream in = null;

		LinkedList<String> commentDoc = checkForCommentDocumentation(withCase);
		if(commentDoc!=null) return lastResponseL1 = commentDoc;
		try {
			in = new FileInputStream(new File(AdvancedMacros.customDocsFolder, fName+".txt"));
		} catch (FileNotFoundException e) {//this is fine, no custom doc made
			//System.out.println("No custom doc");
		}
		if(in==null) {
			try {
				LinkedList<String> lines = checkForTableDoc(withCase);
				if(lines!=null)
					return lastResponseL1 = lines;
			} catch (Exception e) {
				e.printStackTrace();
				//return last
			}
		}
		if(in==null) {
			try {
				in = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(AdvancedMacros.MODID, "docs/"+fName+".txt")).getInputStream();
			} catch (IOException e) {
				//return lastResponseL1 = null; set in the last one
			}
		}
		if(in==null) {/*System.out.println("No resource! ("+fName+")");*/return lastResponseL1 = null;}
		LinkedList<String> lines = new LinkedList<>();
		Scanner scany = new Scanner(in);
		do {
			if(scany.hasNextLine()) {
				String blah = scany.nextLine();
				if(!blah.isEmpty())
					lines.add(blah);
			}else {break;}
		}while(lines.size() < 3);
		scany.close();
		return lastResponseL1=lines;
	}
	private LinkedList<String> checkForTableDoc(String fName) {
		LinkedList<String> out = new LinkedList<>();
		HashMap<String, Object> map = AdvancedMacros.editorGUI.getCta().getTablesMap();
		
		LuaTable t = (LuaTable) map.get(fName);
		if(t==null) return null;
		if(t.getmetatable() == null) return null;
		t = t.getmetatable().checktable();
		if(t.get(CallableTable.LUA_FUNCTION_KEY).optboolean(false)) {
			out.add(t.get(CallableTable.DEFINITION).checkjstring());
			LuaValue temp = t.get(CallableTable.TOOLTIP);
			if(temp.istable()) {
				LuaTable tooltip = t.get(CallableTable.TOOLTIP).checktable();
				for(int i = 1; i<=tooltip.length(); i++) {
					out.add(tooltip.get(i).checkjstring());
				}
			}else {
				out.add(temp.checkjstring());
			}
			return out;
		}
		return null;
	}
	private LinkedList<String> checkForCommentDocumentation(String fName) {
		LuaFunction f = (LuaFunction) AdvancedMacros.editorGUI.getCta().getFunctionsMap().get(fName);
		if(f!=null && f.isclosure()) {
			LuaClosure c = f.checkclosure();
			String sLine;
			try(
					FileInputStream fis = new FileInputStream(new File(AdvancedMacros.macrosFolder, c.p.source.tojstring()));
					Scanner scan = new Scanner(fis)
					) {
				int lineNum = 1;
				for(int i=0; i<c.p.linedefined-11; i++, lineNum++)
					scan.nextLine();
				LinkedList<String> lines = new LinkedList<>();
				while(lineNum!=c.p.linedefined) {
					if((sLine=scan.nextLine()).trim().startsWith("--")) {
						lines.add(sLine.substring(2));
					}else{
						lines.clear();
					}
					lineNum++;
				}
				if(lines.size()>0)
					return lines;

			}catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(c.p.toString());
		}

		return null;
	}





}
