package com.theincgi.advancedMacros.gui.elements;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.lwjgl.input.Keyboard;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.ForgeEventHandler;
import com.theincgi.advancedMacros.gui.Color;
import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.gui.Gui.InputSubscriber;
import com.theincgi.advancedMacros.gui.elements.GuiScrollBar.Orientation;
import com.theincgi.advancedMacros.gui2.ScriptBrowser2;
import com.theincgi.advancedMacros.misc.Property;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class ColorTextArea implements Drawable, InputSubscriber, Moveable{
	//DoubleLinkedList<DoubleLinkedList<Unit>> text = new DoubleLinkedList<>();
	/**y, x*/
	char[][] visChars;
	/**y, x*/
	Color [][] cols;
	/**y, x*/
	boolean[][] quote;
	/**y, x*/
	String[][] formating;
	boolean[][] selection;
	//Changed from linked list becuase more reading will be done for rendering
	ArrayList<String> lines;
	/**where typing happens*/
	Point cursor = new Point(0, 0);
	private Point hoverCursor = new Point(0,0);
	private long hoverTime = 0;
	private String hoverWord = null;
	//int cursorX, cursorY;
	/**Where you prob want the cursor when you hit up or down*/
	int prefX;
	/**where the top left corner of the view is*/
	int viewX,   viewY;
	private Point selectionStart, selectionEnd;
	private Point lastSelectionPoint;
	private boolean needsSaveFlag=false;
	private boolean isEditable = true;
	//	private BufferedImage  bufferedImage;
	//	private CustomTexture ctaTexture;
	//private DynamicTexture texture;
	//private ResourceLocation rLocation;
	
	public boolean doSyntaxHighlighting = true;

	public final HashMap<String, Boolean> keywords = new HashMap<>();

	Property bgColor, frameColor, defaultTextColor, keywordColor, commentColor, variableColor,tableColor,
	stringBoxFill, stringBoxFrame, functionColor, selectionFillColor, selectionFrameColor;
	Property tabCount;
	Property cursorOnColor/*,cursorOffColor*/;
	final int charWid = 7;
	final int charHei = 12;

	private int x, y, wid, hei;

	GuiScrollBar hBar, vBar;

	//dynamic texture draw line nums? (reuse texture, make via buffImg)
	//buffer image powered text?
	//pro, any font, any size
	//     can have decimal scrolling
	//con, gotta get the non scaled window size
	//     non monospaced font may look weird when it's forced to be
	private Gui g;
	private boolean isVisible = true;
	private boolean textChanged = true;
	private boolean resized;
	private static final String propAddress = "colors.cta.";
	private static WidgetID wID;
	public ColorTextArea(WidgetID wID, Gui g) {	
		this.wID = wID;
																					 /*AAA, RRR, GGG, BBB*/
		bgColor 		 = new Property(propAddress+"textFill", 	       new Color(	  128, 128, 128).toLuaValue(), "textFill", 			wID);
		frameColor 		 = new Property(propAddress+"frame", 		       new Color(	  255, 255, 255).toLuaValue(), "frame", 			wID);
		defaultTextColor = new Property(propAddress+"plainText", 	       new Color(  	    0,   0,   0).toLuaValue(), "plainText", 		wID);
		keywordColor 	 = new Property(propAddress+"keyword", 		       new Color(  	    0,  80, 100).toLuaValue(), "keyword", 			wID);
		commentColor 	 = new Property(propAddress+"comment", 		       new Color(  	    0,  85,   0).toLuaValue(), "comment", 			wID);
		variableColor 	 = new Property(propAddress+"variable", 	       new Color(       0, 109,   9).toLuaValue(), "variable", 			wID);
		stringBoxFill 	 = new Property(propAddress+"stringBoxFill",       new Color( 60, 255, 128,  64).toLuaValue(), "stringBoxFill", 	wID);
		stringBoxFrame 	 = new Property(propAddress+"stringBoxFrame",      new Color(     255, 128,  64).toLuaValue(), "stringBoxFrame", 	wID);
		functionColor 	 = new Property(propAddress+"function", 	       new Color(     128, 255, 255).toLuaValue(), "function", 			wID);
		tableColor       = new Property(propAddress+"table",  	           new Color(     120, 250, 100).toLuaValue(), "table", 			wID); 	
		cursorOnColor    = new Property(propAddress+"cursorOn", 	 	   new Color(      91, 182, 255).toLuaValue(), "cursorOn",			wID);
		selectionFrameColor=new Property(propAddress+"selectionFrame",     new Color(       0, 132,  79).toLuaValue(), "selectionColor" , 	wID);
		selectionFillColor=new Property(propAddress+"selectionFill",       new Color(      57, 206, 146).toLuaValue(), "selectionFrame", 	wID);
		//cursorOffColor   = new Property(propAddress+"cursorOff", 	 new Color(     178, 208, 232).toLuaValue(), "cursorOff", 		wID);
		tabCount         = new Property(propAddress+"tabCount", LuaValue.valueOf(2), "tabCount", wID);
		this.g= g;

		hBar = new GuiScrollBar(wID, 0, 0, 0, 0, Orientation.LEFTRIGHT, "colors.cta.scrollbar");
		vBar = new GuiScrollBar(wID, 0, 0, 0, 0, Orientation.UPDOWN,    "colors.cta.scrollbar");
		lines = new ArrayList<String>(){
			@Override
			public String get(int index) {
				if(index<0 || index>=size()){return "";} //no null pointer for when view is outside the text
				return super.get(index);
			}
		};

		keywords.put("and", true);
		keywords.put("break", true);
		keywords.put("do", true);
		keywords.put("else", true);
		keywords.put("elseif", true);
		keywords.put("end", true);
		keywords.put("false", true);
		keywords.put("for", true);
		keywords.put("function", true);
		keywords.put("if", true);
		keywords.put("in", true);
		keywords.put("local", true);
		keywords.put("nil", true);
		keywords.put("not", true);
		keywords.put("or", true);
		keywords.put("repeat", true);
		keywords.put("return", true);
		keywords.put("then", true);
		keywords.put("true", true);
		keywords.put("until", true);
		keywords.put("while", true);

		updateKeywords();

		//		lines.add("hello = \"world\"");
		//		lines.add("say('Shello')");
		//		lines.add("--comment!");
		//		lines.add("say('1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890')");
		//		lines.add("if(hello=='blah')then");
		//		lines.add("  hello = math.floor(3.2)");
		//		lines.add("end");
		lines.add("");
	}

	//	private class Unit{
	//		Color color;
	//		char c;
	//	}

	public static WidgetID getWidgetID() {
		return wID;
	}

	@Override
	public void onDraw(Gui g, int mouseX, int mouseY, float partialTicks) {
		if(wid==0 || hei==0 || !isVisible){return;}
		
		GlStateManager.pushAttrib();
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		//update text color
		//update bounds on scrollbar n stuff
		//drawbg
		//drawstringbox gb
		//draw text
		//draw stringbox outline
		//draw cursor /w blink
		//System.out.println("DRAW?");
		
		//System.out.println("DRAW");
		viewX = (int) hBar.getOffset();
		viewY = (int) vBar.getOffset();
		g.drawBoxedRectangle(x, y, wid-7, hei-7, Utils.parseColor(frameColor.getPropValue()).toInt(), Utils.parseColor(bgColor.getPropValue()).toInt());
		hBar.setWid(7);
		vBar.setWid(7);
		hBar.setLen(wid-7);
		vBar.setLen(hei-7);
		g.drawBoxedRectangle(x+wid-7, y+hei-7, 7, 7, Utils.parseColor(frameColor.getPropValue()).toInt(), Utils.parseColor(bgColor.getPropValue()).toInt());
		hBar.onDraw(g, mouseX, mouseY, partialTicks);
		vBar.onDraw(g, mouseX, mouseY, partialTicks);
		if(textChanged){
			clearGrids();
			loadText();
			if( doSyntaxHighlighting ) 
				solveTextColor();	
			solveSelectionGrid();
			updateScrollbarContent(false);
		}
		drawSelectionBG(g);
		drawStringBoxes(g);
		drawCursor(g);
		drawText(g);
		drawStringBoxFrame(g);
		drawSelectionBox(g);//it moves!
		offerTooltip(g, mouseX, mouseY);
		textChanged = false;
		resized = false;
		//System.out.println("Debug active");
		GlStateManager.popAttrib();
	}

	private void offerTooltip(Gui g, int mx, int my) {
		Point over = cursorOver(mx,my);
		//System.out.println(over);
		//System.out.println(System.currentTimeMillis()-hoverTime);
		if(hoverCursor.equals(over)) {
			//System.out.println("EEQ");
			if(System.currentTimeMillis()-hoverTime>1500) {
				AdvancedMacros.getDocumentationManager().tooltip(g, hoverWord, mx, my, g.width, g.height);
			}
		}else {
			hoverCursor.set(over);
			hoverTime=System.currentTimeMillis();
		}
	}

	private void solveSelectionGrid() {
		if(selectionStart!=null && selectionEnd==null){
			try{
				selection[selectionStart.getY()-viewY][selectionStart.getX()-viewX] = true;
			}catch(Exception e){}
		}else if(selectionStart!=null && selectionEnd!=null){
			{
				if(compare(selectionStart, selectionEnd)==-1){
					//selectionEnd.addX();
					//selectionStart.addX(1);
					Point temp = selectionStart;
					selectionStart = selectionEnd;
					selectionEnd=temp;
				}else{
					//selectionEnd.addX(1);
				}
				if(selectionStart.equals(selectionEnd)){
					selectionStart=selectionEnd=null;
					return;
				}
				int uX, uY=selectionStart.getY();
				while(uY<=selectionEnd.getY()){
					if(uY==selectionStart.getY())
						uX=selectionStart.getX();
					else
						uX=0;
					int end;
					if(uY==selectionEnd.getY())
						end = selectionEnd.getX();
					else
						end = lines.get(uY).length();
					for (; uX < end; uX++) {
						try{
							//if((uX-viewX>=0 && uX-viewX<selection[uY].length && uY-viewY>=0 && uY-viewY<selection.length)){
							//System.out.printf("Selected %d, %d\n", uY-viewY, uX-viewX);
							selection[uY-viewY][uX-viewX] = true;
							//}
						}catch(IndexOutOfBoundsException ioobe){}
					}
					uY++;
				}
			}
		}
	}

	private void drawSelectionBG(Gui g) {
		int sCol = Utils.parseColor(selectionFillColor.getPropValue()).toInt();
		for (int dy = 0; dy < selection.length; dy++) {
			for (int dx = 0; dx < selection[dy].length; dx++) {
				if(selection[dy][dx]){
					int left = x+dx*charWid+2, top = y+dy*charHei+2;
					int right= left+charWid, bottom = top+charHei;
					net.minecraft.client.gui.Gui.drawRect(left, top, right, bottom, sCol);
				}
			}
		}
	}
	private int blinkOffset = 0;
	private void drawCursor(Gui g) {
		if(!focused){return;}
		int col;
		if((System.currentTimeMillis()-blinkOffset)/500%2==0){
			col = Utils.parseColor(cursorOnColor.getPropValue()).toInt();
		}else{
			//col = Utils.parseColor(cursorOffColor.getPropValue()).toInt();
			return;
		}
		if(cursor.getX()-viewX<0 || cursor.getX()-viewX>visChars[0].length || cursor.getY()-viewY<0 || cursor.getY()-viewY>visChars.length){return;}//offscreen
		int left,right,top,bottom;
		left = x+(cursor.getX()-viewX)*charWid+2;
		right = left+charWid;
		top = y+(cursor.getY()-viewY)*charHei+1;
		bottom = top+charHei;
		net.minecraft.client.gui.Gui.drawRect(left, top, right, bottom, col);
	}

	/**load text from lines*/
	private void loadText(){
		for (int y = 0; y < visChars.length; y++) {
			String line = lines.get(viewY+y);
			//System.out.println(line);
			for(int x = 0; x < visChars[y].length && (x+viewX)<line.length(); x++){
				//System.out.println(line.charAt(x+viewX));
				visChars[y][x] = line.charAt(x+viewX);
			}
		}
	}

	//	private void wthUpdate() {
	//		bufferedImage.getGraphics().setColor(java.awt.Color.red);
	//		bufferedImage.getGraphics().drawOval(0, 0, 10, 10);
	//		int[] dat = texture.getTextureData();
	//		for (int y = 0; y < bufferedImage.getHeight(); y++) {
	//			for (int x = 0; x < bufferedImage.getWidth(); x++) {
	//				dat[y*bufferedImage.getWidth()+x] = 0xff-bufferedImage.getRGB(x, y);
	//			}
	//		}
	//	}

	private void clearGrids(){
		Color plain = Utils.parseColor(defaultTextColor.getPropValue());
		for(int y = 0; y<visChars.length; y++){
			for(int x = 0; x<visChars[y].length;x++){
				visChars[y][x] = ' ';
				quote[y][x]=false;
				formating[y][x]="";
				selection[y][x]=false;
				if(cols[y][x]!=null) //reduced object creation, reduced garbage collection
					cols[y][x].setFrom(plain);
				else
					cols[y][x]=plain.copy();
			}
		}
	}


	private void drawStringBoxes(Gui g) {
		if( !doSyntaxHighlighting ) return;
		int boxFillColor = Utils.parseColor(stringBoxFill.getPropValue()).toInt();
		for (int dy = 0; dy < quote.length; dy++) {
			boolean isDrawing = false;
			for (int dx = 0; dx < quote[dy].length; dx++) {
				//				if(quote!=isDrawing){
				////					isDrawing = quote[dy][dx];
				////					if(isDrawing){//starting
				////						
				////					}else{//ending
				////						
				////					}
				//				}
				if(quote[dy][dx]||isDrawing){
					int left = x+dx*charWid+1, top = y+dy*charHei+1;
					int right= left+charWid, bottom = top+charHei;
					net.minecraft.client.gui.Gui.drawRect(left, top, right, bottom, boxFillColor);
				}
				isDrawing = quote[dy][dx];
			}
		}
	}
	private void drawStringBoxFrame(Gui g){
		if( !doSyntaxHighlighting ) return;
		int boxFrameColor = Utils.parseColor(stringBoxFrame.getPropValue()).toInt();
		for (int dy = 0; dy < quote.length; dy++) {
			boolean isDrawing = false;
			for (int dx = 0; dx < quote[dy].length; dx++) {
				int left = x+dx*charWid+1, top = y+dy*charHei+1;
				int right= left+charWid+1, bottom = top+charHei;
				if(quote[dy][dx]!=isDrawing){

					if(quote[dy][dx]){//starting
						if(!(dx==0 && viewX>0)||(viewX==0 && dx==0))//only draw when it doesnt go off edge of screen
							g.drawVerticalLine(left, top, bottom, boxFrameColor);
					}else{//ending
						//if(!(dx==quote[dy].length-1 && viewX))
						g.drawVerticalLine(right, top, bottom, boxFrameColor);
					}
				}
				if(quote[dy][dx]||isDrawing){
					g.drawHorizontalLine(left, right, top, boxFrameColor);
					g.drawHorizontalLine(left, right, bottom, boxFrameColor);
				}
				isDrawing = quote[dy][dx];
			}
		}
	}
	//		Graphics gfx = bufferedImage.getGraphics();
	//		gfx.setColor(Utils.parseColor(stringBoxFill.getPropValue()).toAWTColor());
	//		for(int dY = 0; dY<quote.length; dY++){
	//			boolean isDrawing = false;
	//			for(int dX = 0; dX<quote[dY].length; dX++){
	//				if(isDrawing){
	//					gfx.fillRect((int)(Math.floor(viewX-1)-dX), (int)(Math.floor(viewY-1)-dY), getCharWid(), getCharHei());
	//				}
	//			}
	//		}

	private void drawSelectionBox(Gui g){
		for (int y = 0; y < selection.length; y++) {
			for (int x = 0; x < selection[y].length; x++) {
				if(selection[y][x]){
					//int left = x+dx*charWid+1, top = y+dy*charHei+1;
					//int right= left+charWid, bottom = top+charHei;
					int px = this.x + x*charWid+2;
					int py = this.y + y*charHei+2;
					if(y<selection.length-1){//space to check down
						if(!selection[y+1][x])
							drawBlinkyLine(g, BlinkyLineDirection.LEFT, px, py);
					}
					if(y>0){
						if(!selection[y-1][x])
							drawBlinkyLine(g, BlinkyLineDirection.RIGHT, px, py);
					}
					if(x< selection[y].length-1){
						if(!selection[y][x+1])
							drawBlinkyLine(g, BlinkyLineDirection.DOWN, px, py);
					}
					if(x>0){
						if(!selection[y][x-1])
							drawBlinkyLine(g, BlinkyLineDirection.UP, px, py);

					}
				}
			}
		}
	}

	private void drawText(Gui g){
		//visChars[3][3]='A';
		//cols[3][3]=new Color(0,0,255);
		for (int y = 0; y < visChars.length; y++) {
			for (int x = 0; x < visChars[y].length; x++) {
				//				if(g.getFontRend()==null){
				//					System.out.println("G was null");
				//				}else if(visChars==null){
				//					System.out.println("visChars null");
				//				}else if(cols[y][x]==null){
				//					System.out.println("colors null");
				//				}
				int offset = (charWid - g.getFontRend().getCharWidth(visChars[y][x]))/2;

				String frmt = formating[y][x];
				g.getFontRend().drawString(frmt+visChars[y][x], this.x+charWid*x+3+offset, this.y+charHei*y+4, cols[y][x].toInt());
			}
		}

		//		Graphics gfx = bufferedImage.getGraphics();
		//		gfx.setColor(Utils.parseColor(stringBoxFill.getPropValue()).toAWTColor());
		//		for(int dY = 0; dY<visChars.length; dY++){
		//			boolean isDrawing = false;
		//			for(int dX = 0; dX<visChars[dY].length; dX++){
		//				if(isDrawing){
		//					gfx.drawString(visChars[dY][dX]+"", (int)(Math.floor(viewX-1)-dX), (int)(Math.floor(viewY-1)-dY));
		//					//gfx.fillRect((int)(Math.floor(viewX-1)-dX), (int)(Math.floor(viewY-1)-dY), getCharWid(), getCharHei());
		//				}
		//			}
		//		}
	}

	public static HashMap<String,Object> tables, functions, variables;
	/**This will get a new list of keywords based on the AdvancedMacros.globals*/
	public static void updateKeywords(){
		tables = getVariableList(LuaValue.TTABLE);
		functions = getVariableList(AdvancedMacros.globals.checktable(), LuaValue.TFUNCTION, true, "", new HashMap<LuaTable, Boolean>());
		//functions = getVariableList(LuaValue.TFUNCTION); //TODO check this
		variables = getVariableList(LuaValue.TNUMBER);
		variables.putAll(getVariableList(LuaValue.TSTRING));
		variables.putAll(getVariableList(LuaValue.TBOOLEAN));
	}
	public static HashMap<String, Object> getFunctionsMap() {
		return functions;
	}

	private static final String quoteRegEx = "(\"[^\"]*\")|('[^\']*')|(\\[\\[.*?\\]\\])";
	private static final String variableRegEx = "([_a-zA-Z]+[_a-zA-Z0-9]*(\\[[0-9]+\\])?(\\.{1})?)+";
	private Pattern variablePattern = Pattern.compile(variableRegEx);


	private class Q{int start, end; public Q(int a, int b){start = a;end = b;}}
	private void solveTextColor(){
		Color color_table   = Utils.parseColor(tableColor.getPropValue()).copy();
		Color color_funct   = Utils.parseColor(functionColor.getPropValue()).copy();
		Color color_var     = Utils.parseColor(variableColor.getPropValue()).copy();
		Color color_str     = Utils.parseColor(stringBoxFill.getPropValue()).copy();
		Color color_plain   = Utils.parseColor(defaultTextColor.getPropValue()).copy();
		Color color_comment = Utils.parseColor(commentColor.getPropValue()).copy();
		Color color_keyword = Utils.parseColor(keywordColor.getPropValue()).copy();
		
		hoverWord = null;
		
		for(int sLine = viewY, dY = 0; dY<visChars.length; sLine++, dY++){
			//sLine, line # in lines
			//dY draw Y, where in the visChars and cols this goes
			String line = lines.get(sLine);
			String tmp = "";
			int stage = 0;
			boolean commentMode = false;
			int tmpStart = 0;
			boolean isQuote = false;
			String quoteChar = "";
			Color quoteColor = Color.BLACK;
			String sFormat = "";
			Color sCol = color_plain.copy();

			Matcher matcher = variablePattern.matcher(line);

			while(matcher.find()){
				String m = matcher.group();

				int start = matcher.start();
				int end   = matcher.end();
				
				if(sLine==hoverCursor.getY()) {
					if(start<=hoverCursor.getX() && hoverCursor.getX()<end)
						hoverWord = line.substring(start, end);
				}
				
				//System.out.printf("Found: %s [%4d - %4d]\n", m, start,end);
				if(keywords.containsKey(m)){
					//System.out.println("KEYWORD");
					sCol = color_keyword;
				}else if(tables.containsKey(m)){
					//System.out.println("TABLES");
					sCol = color_table;
				}else if(functions.containsKey(m)){
					//System.out.println("FUNCTION");
					sCol = color_funct;
				}else if(variables.containsKey(m)){
					//System.out.println("VARIABLE");
					sCol = color_var;
				}else{
					sCol = color_plain;
				}
				//System.out.println("View X "+viewX);
				for(int dX = start; dX<end; dX++){
					//System.out.printf("[%3d:%3d] ",dX, dY);
					if(dX-viewX>=0 && dX-viewX<cols[dY].length)
						cols[dY][dX-viewX].setFrom(sCol);
				}
				//System.out.println();
			}
			Pattern p = Pattern.compile(quoteRegEx);
			Matcher quoteFind = p.matcher(line);

			ArrayList<Q> quoteSpots = new ArrayList<>(); //for checking if a comment outside a quote or not even if off screen

			/*\u00a7
			 *  OBFUSCATED("OBFUSCATED", 'k', true),
    			BOLD("BOLD", 'l', true),
    			STRIKETHROUGH("STRIKETHROUGH", 'm', true),
    			UNDERLINE("UNDERLINE", 'n', true),
    			ITALIC("ITALIC", 'o', true),
    			RESET("RESET", 'r', -1);
			 * */
			while(quoteFind.find()){
				String m = quoteFind.group();
				int start = quoteFind.start();
				int end = quoteFind.end()-1;
				//sFormat = "";
				//System.out.printf("Found: %s [%4d - %4d]\n", m, start,end);
				quoteSpots.add(new Q(start, end));
				Color textCode = color_plain;
				for(int x = start; x<end; x++){
					char c = line.charAt(x); //char at point in quote/line
					if(x-viewX>=0 && x-viewX<quote[dY].length){ //on screen in width
						quote[dY][x-viewX]=true;
					}
					if(c=='&' && x<line.length()-1 && line.charAt(x+1)=='&') {//example "blah&&blah"
						
						x++; 
						if(x-viewX>=0 && x-viewX<quote[dY].length){ //on screen in width
							quote[dY][x-viewX]=true;
						}
						try{
							cols[dY][x-viewX].setFrom(textCode);
							formating[dY][x-viewX]=sFormat;
						}catch (ArrayIndexOutOfBoundsException e) {
							//TODO stop being lazy
						}
						continue;
					}
					if(c=='&' && x<line.length()-1 && Utils.isTextColorCode(line.charAt(x+1))){//if char is an & and has space to check next char, and is a color code
						textCode = Utils.getTextCodeColor(line.charAt(x+1));
						textCode = textCode==null?color_plain:textCode;
						sFormat = "";
					}
					if(c=='&' && x<line.length()-1 && Utils.isTextStyleCode(line.charAt(x+1))){
						switch (line.charAt(x+1)) {
						case 'B':
							sFormat +="\u00A7l";
							break;
						case 'I':
							sFormat+="\u00A7o";
							break;
						case 'O':
							sFormat+="\u00A7k";
							break;
						case 'S':
							sFormat+="\u00A7m";
							break;
						case 'U':
							sFormat+="\u00A7n";
							break;
						default:
							break;
						}

					}
					if((line.charAt(end-1)==']' && x<end-1 )||(line.charAt(end-1)!=']' && x<end)){
						if(x==start || (x==start+1 && c=='[' && line.charAt(start)=='[')) {continue;}
						try{
							cols[dY][x-viewX].setFrom(textCode);
							formating[dY][x-viewX]=sFormat;
						}catch (ArrayIndexOutOfBoundsException e) {
							//TODO stop being lazy
						}
					}
				}
			}
			int w=-1;
			findingComments:
				while((w=line.indexOf("--", w+1) )!=-1){
					if(w>viewX+quote[0].length){break; /*off right side of screen, doesnt matter*/}
					for (Q q : quoteSpots) {
						if(q.start<=w && w<q.end){
							continue findingComments;
						}
					}
					for(int x=w-viewX; x<cols[0].length; x++){
						if(x-viewX>=0){
							cols[dY][x].setFrom(color_comment);
						}
					}
				}

			//tables
			//variables
			//functions
			//keywords
			//string
			//comment
			//blockComment?
			textChanged=false;
		}

		//		for(int y = 0; y<quote.length; y++){
		//			boolean isQuote=false;
		//			//char quote = ' ';
		//			String line = lines.get(y+viewY);
		//			Color activeColor = null;
		//			for (int x = 0; x < quote[y].length; x++) {
		//				if(line.charAt(x+viewX)=='&' && x+viewX+1<line.length() && Utils.isTextCode(line.charAt(x+viewX+1))){
		//					activeColor = Utils.getTextCodeColor(line.charAt(x+viewX+1));
		//				}
		//				if(quote[y][x] && )
		//			}
		//		}
	}


	//	private 

	@Override
	public boolean onScroll(Gui gui, int i) {
		if(focused){
			if(!isShiftDown()){
				vBar.onScroll(gui, i);
			}else{
				hBar.onScroll(gui, i);
			}
			textChanged=true;
		}
		return focused;
	}

	private void resetBlinkOffset(){
		blinkOffset = (int) (System.currentTimeMillis()%1000);
	}

	@Override
	public boolean onMouseClick(Gui gui, int x, int y, int buttonNum) {
		if(hBar.onMouseClick(gui, x, y, buttonNum)){return textChanged=true;}if(vBar.onMouseClick(gui, x, y, buttonNum)){return textChanged=true;}
		if(x>this.x && x<this.x+this.wid && y> this.y && y<this.y+this.wid){
			textChanged = true;
			x -=this.x+3;
			y -=this.y+4;
			x /= charWid;
			y /= charHei;
			cursor.setY( Math.min(y+viewY, lines.size()-1));
			cursor.setX(prefX =Math.min(lines.get(cursor.getY()).length(), x+viewX));
			resetBlinkOffset();

			if(isShiftDown()){//set one of the selection spotts

				if(isCTRLDown()){//TODO document this
					selectionStart=new Point(0, cursor.getY());
					selectionEnd= new Point(lines.get(cursor.getY()).length(), cursor.getY());

				}else{
					Point nP = new Point(cursor.getX(), cursor.getY());

					if(selectionStart==null && selectionEnd==null){
						selectionStart = cursor.getLastPoint();
						selectionEnd = nP;
					}else{
						if(lastSelectionPoint==selectionStart){
							selectionStart=nP;
						}else{
							selectionEnd = nP;
						}
					}
					if(selectionStart!=null && selectionEnd!=null){
						if(compare(selectionStart, selectionEnd)==-1){
							//selectionEnd.addX();
							selectionStart.addX(1);
							Point temp = selectionStart;
							selectionStart = selectionEnd;
							selectionEnd=temp;
						}else{
							selectionEnd.addX(1);
						}
					}
					if(selectionEnd.x>lines.get(selectionEnd.getY()).length()){
						selectionEnd.addX(-1);
					}
					lastSelectionPoint=nP;

				}
				//System.out.println(selectionStart + " " + selectionEnd);
			}else{
				selectionStart=null;
				selectionEnd=null;
			}
			return true;
		}else{
			selectionStart=selectionEnd=null;
			return false;
		}
	}


	/**if p1 is before p2 then positve negitive if reveresed*/
	private static int compare(Point p1, Point p2){
		if(p1.getY() < p2.getY()){
			return 1;
		}else if(p1.getY() > p2.getY()){
			return -1;
		}else{
			if(p1.getX()<p2.getX()){
				return 1;
			}else if(p1.getX()>p2.getX()){
				return -1;
			}else{
				return 0;
			}
		}
	}

	public int countIndent(){
		String line = lines.get(cursor.getY());
		for(int i = 0; i<line.length(); i++){
			if(line.charAt(i)!=' '){
				return i;
			}
		}
		return cursor.getX();
	}

	private static String repeat(String string, int i) {
		String out ="";
		for(int m = 0; m<i; m++){
			out+=string;
		}
		return out;
	}

	@Override
	public boolean onMouseRelease(Gui gui, int x, int y, int state) {
		if(hBar.onMouseRelease(gui, x, y, state)){return textChanged=true;}
		if(vBar.onMouseRelease(gui, x, y, state)){return textChanged=true;}
		return false;
	}

	@Override
	public boolean onMouseClickMove(Gui gui, int x, int y, int buttonNum, long timeSinceClick) {
		if(hBar.onMouseClickMove(gui, x, y, buttonNum, timeSinceClick)){return textChanged=true;}
		if(vBar.onMouseClickMove(gui, x, y, buttonNum, timeSinceClick)){return textChanged=true;}
		return false;
	}

	@Override
	public boolean onKeyPressed(Gui gui, char typedChar, int keyCode) { //TODO any navKey + SHIFT use lastxy and current xy to make selection/expand
		//TODO snap to view if offscreen or arrow keys push offscreen
		updateScrollbarContent(true);
		if(!isCTRLDown() && isEditKey(typedChar, keyCode) && selectionStart!=null && selectionEnd!=null && isEditable){
			lines.set(selectionStart.getY(),
					lines.get(selectionStart.getY()).substring(0,selectionStart.getX())+ //begining to selection on first line
					lines.get(selectionEnd.getY()).substring(selectionEnd.getX())); //end of selection to end of line on last line
			if(selectionStart.getY()!=selectionEnd.getY()){
				lines.set(selectionEnd.getY(), 
						lines.get(selectionEnd.getY()).substring(selectionEnd.getX())
						);
				for(int g = (selectionStart.getY()+1); g<=selectionEnd.getY(); g++){
					lines.remove((selectionStart.getY()+1)); //not line g, because the array also shrinks in size at the same time
				}
			}
			cursor.set(selectionStart);
			//selectionStart.setCursorTo();
			selectionStart=null;
			selectionEnd=null;
			textChanged = true;
			if(keyCode==Keyboard.KEY_DELETE || Keyboard.KEY_BACK==keyCode){
				return true;
			}
			setNeedsSaveFlag(true);
		}
		if(!focused){return false;}
		switch (keyCode) {
		case Keyboard.KEY_RETURN:{
			if(!isEditable){return false;}
			String tmp = lines.get(cursor.getY()).substring(cursor.getX());
			lines.set(cursor.getY(), lines.get(cursor.getY()).substring(0,cursor.getX()));
			cursor.setX(countIndent());
			lines.add(cursor.getY()+1, repeat(" ",cursor.getX())+tmp);
			cursor.addY(1);
			prefX = cursor.getX();
			setNeedsSaveFlag(true);
			break;
		}
		case Keyboard.KEY_BACK:
			if(!isEditable){return false;}
			if(cursor.getX()>0){
				lines.set(cursor.getY(), lines.get(cursor.getY()).substring(0, cursor.getX()-1)+lines.get(cursor.getY()).substring(cursor.getX()));
				cursor.addX(-1);
				prefX = cursor.getX();
			}else if(cursor.getY()>0){
				int oldLen = lines.get(cursor.getY()-1).length();
				lines.set(cursor.getY()-1, lines.get(cursor.getY()-1)+lines.get(cursor.getY()));
				cursor.addY(-1);
				cursor.setX(oldLen);
				prefX = cursor.getX();
				lines.remove(cursor.getY()+1);
			}
			setNeedsSaveFlag(true);
			break;
		case Keyboard.KEY_UP:
			if(cursor.getY()>0){
				cursor.addY(-1);
				cursor.setX(Math.min(lines.get(cursor.getY()).length(), prefX));
			}
			if(selectionEnd!=null && isShiftDown())
				selectionEnd.set(cursor);
			break;
		case Keyboard.KEY_DOWN:
			if(cursor.getY()<lines.size()-1){
				cursor.addY(1);
				cursor.setX(Math.min(lines.get(cursor.getY()).length(), prefX));
			}
			if(selectionEnd!=null && isShiftDown())
				selectionEnd.set(cursor);
			break;
		case Keyboard.KEY_LEFT:
			if(cursor.getX()>0){
				cursor.addX(-1);
			}else if(cursor.getY()>0){
				cursor.addY(-1);
				cursor.setX(lines.get(cursor.getY()).length());
			}
			prefX = cursor.getX();
			if(selectionEnd!=null && isShiftDown())
				selectionEnd.set(cursor);
			break;
		case Keyboard.KEY_RIGHT:
			if(cursor.getX()<lines.get(cursor.getY()).length()){
				cursor.addX(1);
			}else if(cursor.getY()<lines.size()-1){
				cursor.addY(1);
				cursor.setX(0);
			}
			prefX = cursor.getX();
			if(selectionEnd!=null && isShiftDown())
				selectionEnd.set(cursor);
			break;
		case Keyboard.KEY_DELETE:{
			if(!isEditable){return false;}
			String line = lines.get(cursor.getY());
			if(cursor.getX()<line.length()){
				lines.set(cursor.getY(), line.substring(0, cursor.getX()) + line.substring(cursor.getX()+1));
			}else if(cursor.getY()<lines.size()-1){
				lines.set(cursor.getY(), line+lines.get(cursor.getY()+1));
				lines.remove(cursor.getY()+1);
			}
			setNeedsSaveFlag(true);
			break;
		}
		case Keyboard.KEY_PRIOR:
			vBar.focusToItem(cursor.getY()-visChars.length/2);
			break;
		case Keyboard.KEY_NEXT:
			vBar.focusToItem(cursor.getY()+visChars.length/2);
			break;
		case Keyboard.KEY_TAB:
			if(!isEditable){return false;}
			for(int i = 0; i<tabCount.getPropValue().checkint(); i++)
				onKeyPressed(gui, ' ', Keyboard.KEY_SPACE);
			setNeedsSaveFlag(true);
			break;
		case Keyboard.KEY_APPS:

			break;
		case Keyboard.KEY_PAUSE:
			break;
		case Keyboard.KEY_HOME:
			cursor.setX(prefX=0);
			updateScrollbarContent(true);
			break;
		case Keyboard.KEY_END:
			cursor.setX(prefX=lines.get(cursor.getY()).length());
			updateScrollbarContent(true);
			break;
		default:
			if(isCTRLDown()){
				//System.out.println("CONTROLL IS PRESS");
				if(keyCode==Keyboard.KEY_A) {
					selectionStart = new Point(0, 0);
					selectionEnd = new Point(lines.get(lines.size()-1).length(), lines.size()-1);
				}else	if(keyCode==Keyboard.KEY_S){
					//System.out.println("Save>");
					if(!isEditable){return false;}
					save();
				}else if(keyCode==Keyboard.KEY_C){
					try{
						String toClipboard = "";
						for(int sl =  selectionStart.getY(); sl<=selectionEnd.getY(); sl++){
							int start,end;
							start = ((sl==selectionStart.getY())?selectionStart.getX():0);
							end = ((sl==selectionEnd.getY())?selectionEnd.getX():lines.get(sl).length());
							toClipboard+=lines.get(sl).substring(start, end) + (sl!=selectionEnd.getY()?"\n":"");
						}
						StringSelection ss = new StringSelection(toClipboard);
						System.out.println("Copied: '"+toClipboard+"'");
						Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, ss);
					}catch (Exception e) {
						e.printStackTrace();
					}
				}else if(keyCode==Keyboard.KEY_X){
					if(!isEditable){return false;}
					try{
						String toClipboard = "";
						for(int sl = selectionStart.getY(); sl<=selectionEnd.getY(); sl++){
							int start,end;
							start = ((sl==selectionStart.getY())?selectionStart.getX():0);
							end = ((sl==selectionEnd.getY())?selectionEnd.getX():lines.get(sl).length());
							//System.out.printf("START %d, END %d\n",start, end);
							toClipboard+=lines.get(sl).substring(start, Math.min(end,lines.get(sl).length()));
							toClipboard+=(sl!=selectionEnd.getY()?"\n":"");

						}
						lines.set(selectionStart.getY(),
								lines.get(selectionStart.getY()).substring(0,selectionStart.getX())+ //begining to selection on first line
								lines.get(selectionEnd.getY()).substring(selectionEnd.getX())); //end of selection to end of line on last line
						if(selectionStart.getY()!=selectionEnd.getY()){
							lines.set(selectionEnd.getY(), 
									lines.get(selectionEnd.getY()).substring(selectionEnd.getX())
									);
							for(int g = (selectionStart.getY()+1); g<=selectionEnd.getY(); g++){
								lines.remove((selectionStart.getY()+1)); //not line g, because the array also shrinks in size at the same time
							}
						}
						cursor.set(selectionStart);
						//selectionStart.setCursorTo();
						selectionStart=null;
						selectionEnd=null;
						StringSelection ss = new StringSelection(toClipboard);
						Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, ss);
					}catch (Exception e) {
						e.printStackTrace();
					}
					setNeedsSaveFlag(true);
				}else if(keyCode==Keyboard.KEY_V && isEditable){
					if(!isEditable){return false;}
					//System.out.println("paste");
					try {
						String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
						Scanner s = new Scanner(data);
						boolean multiline = false;
						String lineStart = lines.get(cursor.getY()).substring(0, cursor.getX()), lineEnd = lines.get(cursor.getY()).substring(cursor.getX());
						while(s.hasNextLine()){
							if(multiline==false)
								lines.set(cursor.getY(), lineStart+s.nextLine());
							else{
								cursor.addY(1);
								lines.add(cursor.getY(), s.nextLine());
							}
							multiline = true;
						}
						lines.set(cursor.getY(), lines.get(cursor.getY())+lineEnd);
						cursor.setX(lines.get(cursor.getY()).length()-lineEnd.length());
						s.close();
					} catch (Exception e) {
					}
					setNeedsSaveFlag(true);
				}else if(keyCode==Keyboard.KEY_W && isEditable){
					//TODO exit editor with CTRL+W
					System.out.println("Exit");
				}else if(keyCode==Keyboard.KEY_G && isEditable){
					//TODO goto line popup
					//TODO clickable errors that jump to line number
					System.out.println("Goto line");
				}else if(keyCode==Keyboard.KEY_R && isEditable) {
					save();
					ForgeEventHandler.closeMenu();
					AdvancedMacros.runScript(ScriptBrowser2.getScriptPath(getScriptFile()));
				}else if(keyCode==Keyboard.KEY_SPACE && isEditable){
					if(!isEditable){return false;}
					//TODO autocomplete!
					System.out.println("Autocomplete");
				}
			}
			if(32<=typedChar && typedChar<=126){
				if(!isEditable){return false;}
				lines.set(cursor.getY(), lines.get(cursor.getY()).substring(0, cursor.getX())+typedChar+lines.get(cursor.getY()).substring(cursor.getX()));
				cursor.addX(1);
				setNeedsSaveFlag(true);
			}
			break;
		}
		textChanged = true;
		resetBlinkOffset();
		return isVisible && focused;
	}
	private boolean isEditKey(char typedChar, int keyCode) {
		@SuppressWarnings("deprecation")
		int[] blacklist = new int[]{Keyboard.KEY_APPS, Keyboard.KEY_DOWN, Keyboard.KEY_UP, Keyboard.KEY_LEFT, Keyboard.KEY_RIGHT,
				Keyboard.KEY_PRIOR, Keyboard.KEY_NEXT, Keyboard.KEY_LSHIFT, Keyboard.KEY_RSHIFT,
				Keyboard.KEY_LCONTROL, Keyboard.KEY_RCONTROL, Keyboard.KEY_HOME, Keyboard.KEY_END,
				Keyboard.KEY_F1,Keyboard.KEY_F2,Keyboard.KEY_F3,Keyboard.KEY_F4,Keyboard.KEY_F5,Keyboard.KEY_F6,
				Keyboard.KEY_F7,Keyboard.KEY_F8,Keyboard.KEY_F9,Keyboard.KEY_F10,Keyboard.KEY_F11,Keyboard.KEY_F12,
				Keyboard.KEY_ESCAPE, Keyboard.KEY_LMENU, Keyboard.KEY_RMENU,
				Keyboard.KEY_LMETA, Keyboard.KEY_RMETA, Keyboard.KEY_LWIN, Keyboard.KEY_RWIN, Keyboard.KEY_CAPITAL,
				Keyboard.KEY_SCROLL, Keyboard.KEY_PAUSE};
		if(typedChar==0)
			return false;
		for (int i : blacklist) {
			if(keyCode==i)
				return false;
		}
		return true;
	}

	public void save() {
		System.out.println("Saving...");
		try {
			PrintWriter pw = new PrintWriter(getScriptFile());
			for (String string : lines) {
				pw.println(string);
			}
			pw.close();
			System.out.println("Saved");
		} catch (IOException e) {
			e.printStackTrace(); //TODO warn user
		}
		setNeedsSaveFlag(false);
	}
	
	public File getScriptFile() {
		return new File(AdvancedMacros.macrosFolder,scriptName);
	}
	
	public void setText(String text) {
		lines.clear();
		cursor.set(0, 0);
		Scanner s = new Scanner(text);
		while(s.hasNextLine())
			lines.add(s.nextLine());
		setNeedsSaveFlag(true);
	}
	public String getText() {
		StringJoiner out = new StringJoiner("\n");
		lines.iterator().forEachRemaining(s->{
			out.add(s);
		});
		return out.toString();
	}
	
	public static boolean isCTRLDown(){
		return Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
	}
	public static boolean isShiftDown(){
		return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT); 
	}
	@Override
	public boolean onKeyRepeat(Gui gui, char typedChar, int keyCode, int repeatMod) {
		if(focused && repeatMod%5==0 && !isCTRLDown()){
			onKeyPressed(gui, typedChar, keyCode);
			return true;
		}
		return false;
	}

	@Override
	public boolean onKeyRelease(Gui gui, char typedChar, int keyCode) {
		return false;
	}

	@Override
	public void setPos(int x, int y) {
		this.x = x;
		this.y = y;
		hBar.setPos(x, y+hei-7);
		vBar.setPos(x+wid-7, y);
	}

	@Override
	public void setVisible(boolean b) {
		isVisible = b;
	}

	@Override
	public int getItemHeight() {
		return hei;
	}

	@Override
	public int getItemWidth() {
		return wid;
	}

	@Override
	public void setWidth(int i) {
		resize(i, y);
	}
	@Override
	public void setHeight(int i) {
		resize(x, i);
	}
	public void resize(int newWid, int newHei){
		if(newWid==0 || newHei==0 || g.width==0 || g.height==0 || Minecraft.getMinecraft().displayWidth==0 || Minecraft.getMinecraft().displayHeight==0)return;
		if(wid!=newWid || hei!=newHei){ //something changed
			////			if(texture!=null){
			////				texture.deleteGlTexture();
			////			}
			wid = newWid;
			hei = newHei;
			//			//System.out.println(g);
			//			bufferedImage = new BufferedImage((int) (Minecraft.getMinecraft().displayWidth  * (newWid/(float)g.width)), 
			//					(int) (Minecraft.getMinecraft().displayHeight * (newHei/(float)g.height)), 
			//					BufferedImage.TYPE_INT_ARGB);
			//			bufferedImage.getGraphics().setColor(java.awt.Color.red);
			//			bufferedImage.getGraphics().drawLine(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
			////			texture = new DynamicTexture(bufferedImage);
			////			rLocation = Settings.fromDynamic("CTAContent", texture);
			//			ctaTexture = new CustomTexture(bufferedImage);
			int charX = (wid-7)/(charWid) ;
			int charY = (hei-7)/(charHei) ; 
			hBar.setPos(x, y+hei-7);
			vBar.setPos(x+wid-7, y);
			hBar.setLen(wid-7);
			vBar.setLen(hei-7);
			hBar.setWid(7);
			vBar.setWid(7);



			cols = new Color[charY][charX];
			visChars = new char[charY][charX];
			quote = new boolean[charY][charX];
			formating = new String[charY][charX];
			selection = new boolean[charY][charX];
			Color plain = Utils.parseColor(defaultTextColor.getPropValue());
			for(int y = 0; y<charY;y++){
				for(int x=0; x<charX;x++){ //TODO make bufferes have +1 on both sides so string can draw offscreen boxes
					cols[y][x]=plain.copy();
					visChars[y][x]=' ';
					quote[y][x]=false;
					formating[y][x] = "";
				}
			}
			updateScrollbarContent(false);
			resized = true;
			textChanged = true;
		}
	}

	private void updateScrollbarContent(boolean typed) {
		vBar.setItemCount(getLineCount());
		hBar.setItemCount(getWidestLine());
		vBar.setVisibleItems(Math.max(1,visChars.length-4));
		hBar.setVisibleItems(Math.max(1,visChars[0].length-12));
		if (typed) {
			if(cursor.getX()<viewX+3 || cursor.getX()>viewX+visChars[0].length-4)
				hBar.focusToItem(cursor.getX());
			if(cursor.getY()<viewY || cursor.getY()>viewY+visChars.length)
				vBar.focusToItem(cursor.getY());

		}
	}

	@Override
	public int getX() {
		return x;
	}
	@Override
	public int getY() {
		return y;
	}
	public int getLineCount(){
		return lines.size();
	}
	public int getWidestLine(){
		int m = 0;
		for (String line : lines) {
			m=Math.max(line.length(), m);
		}
		return m;
	}


	public static HashMap<String, Object> getVariableList(int luaValType){
		return getVariableList(AdvancedMacros.globals.checktable(), luaValType, true, "", new HashMap<LuaTable, Boolean>());
	}
	/**use AdvancedMacros.globals.getraw("_G");<br>
	 * for tmp, start with "" <br>
	 * */
	public static HashMap<String, Object> getVariableList(LuaTable t, int luaValType, boolean checkTables, String tmp, HashMap<LuaTable, Boolean> added){
		//if(luaValType==LuaValue.TINT){return new LinkedList<>();}
		added.put(t, true);
		HashMap<String,Object> vars = new HashMap<>();
		if(tmp.equals("package.loaded")){tmp = "";}
		/**the key name in this table*/
		LuaValue sKey = t.next(LuaValue.NIL).arg1();
		LuaValue sVal = t.get(sKey);
		while(!sKey.isnil()){ //while there is a key in next()
			if(sVal.type()==luaValType){ //if the value matches the type we want
				vars.put(tmp+(tmp.length()>0?".":"") + sKey.tojstring(), sVal); //add it to output
				//System.out.println(tmp+(tmp.length()>0?".":"") + sKey.tojstring());
			}
			if(checkTables && sVal.istable() && !added.getOrDefault(sVal, false)){
				String tmp2 = "";
				if(sKey.isint()){
//					if(tmp.length()==0){
//						tmp2 = "_G["+sVal.tojstring()+"].";
//					}else{
//						tmp2 = tmp+"["+sVal.tojstring()+"].";
//					}
				}else{
					tmp2 = tmp+(tmp.length()>0?".":"") + sKey.tojstring();
				}
				if(!sKey.isint())
					vars.putAll(getVariableList(sVal.checktable(), luaValType, checkTables, tmp2, added));
			}
			sKey = t.next(sKey).arg1();
			sVal = t.get(sKey);
		}

		return vars;
	}

	private String scriptName;

	public void openScript(String scriptName) {
		this.scriptName = scriptName;
		lines.clear();
		File sScript = new File(AdvancedMacros.macrosFolder, scriptName);
		if(sScript.exists()){
			try {
				Scanner s = new Scanner(sScript);
				while(s.hasNextLine()){
					lines.add(s.nextLine());
				}
				s.close();
			} catch (FileNotFoundException e) {
				lines.add("");
			}
			if(lines.size()==0){
				lines.add("");
			}
		}else{
			lines.add("");
		}
		cursor.set(0, 0);
		setNeedsSaveFlag(false);
		updateKeywords();//cause this seems like a reasonable time to do it
		textChanged = true;
	}

	public void setEditable(boolean isEditable) {
		this.isEditable = isEditable;
	}

	boolean focused;
	public void setFocused(boolean b) {
		focused = b;
	}

	private void drawBlinkyLine(Gui g, BlinkyLineDirection bld, int x, int y){
		int sCol = Utils.parseColor(selectionFrameColor.getPropValue()).toInt();
		int i = (int) (System.currentTimeMillis()/250%6);
		if(bld.isUp() || bld.isLeft()){i=6-i;}
		//		boolean a = ((int)(i/3))==(bld.isLeft()||bld.isRight()?x:y)%3;
		//		boolean b = i%2==0 && a;
		//		boolean c = i%2==1 && a;
		boolean b = (bld.isLeft()||bld.isRight())?x*2%6!=i:y*2%6!=i;
		boolean c = (bld.isLeft()||bld.isRight())?(x*2+1)%6!=i:(x*2+1)%6!=i;
		if (bld.isRight()) {
			if(b)
				g.drawHorizontalLine(x, x+charWid/2, y, sCol);
			if(c)
				g.drawHorizontalLine(x+charWid/2, x+charWid, y, sCol);
		}
		if(bld.isLeft()){
			if(b)
				g.drawHorizontalLine(x, x+charWid/2, y+charHei, sCol);
			if(c)
				g.drawHorizontalLine(x+charWid/2, x+charWid, y+charHei, sCol);
		}
		if(bld.isDown()){
			if(b)
				g.drawVerticalLine(x+charWid, y, y+charHei/2, sCol);
			if(c)
				g.drawVerticalLine(x+charWid, y+charHei/2, y+charHei, sCol);
		}
		if(bld.isUp()){
			if(b)
				g.drawVerticalLine(x, y, y+charHei/2, sCol);
			if(c)
				g.drawVerticalLine(x, y+charHei/2, y+charHei, sCol);
		}
	}

	private enum BlinkyLineDirection{
		UP,
		DOWN,
		LEFT,
		RIGHT;
		boolean isUp(){
			return this.equals(UP);
		}
		boolean isDown(){
			return this.equals(DOWN);
		}
		boolean isLeft(){
			return this.equals(LEFT);
		}
		boolean isRight(){
			return this.equals(RIGHT);
		}
	}


	public class Point{
		private int x, y;
		private int lastX, lastY;
		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
		public Point getLastPoint() {
			return new Point(lastX, lastY);
		}
		public void set(Point sP) {
			set(sP.x, sP.y);
		}
		public void addX(int i){
			x+=i;
		}
		public void addY(int i) {
			y+=i;
		}
		public void set(int x, int y){
			setX(x);
			setY(y);
		}
		public int getX() {
			return x;
		}
		public void setX(int x) {
			lastX = this.x;
			this.x = x;
		}
		public int getY() {
			return y;
		}
		public void setY(int y) {
			lastY=this.y;
			this.y = y;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Point other = (Point) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}
		private ColorTextArea getOuterType() {
			return ColorTextArea.this;
		}
		@Override
		public String toString() {
			return "Point [x=" + x + ", y=" + y + ", lastX=" + lastX + ", lastY=" + lastY + "]";
		}
		
	}
	public Point getCursor() {
		return cursor;
	}
	//	private int getType(String s){
	//		if(s.indexOf("(")>=0){
	//			s = s.substring(0, s.indexOf("("));
	//		}
	//		AdvancedMacros.globals.load
	//	}

	public boolean isNeedsSave() {
		return needsSaveFlag;
	}

	public void setNeedsSaveFlag(boolean needsSaveFlag) {
		this.needsSaveFlag = needsSaveFlag;
		//		if(needsSaveChanged!=null)
		//			needsSaveChanged.run();
	}
	
	/**recently added util to tell you where the cursor is over in char coords instead of pixels*/
	public Point cursorOver(int x, int y) {
		x -=this.x+3;
		y -=this.y+4;
		x /= charWid;
		y /= charHei;
		return new Point(Math.min(lines.get(cursor.getY()).length(), x+viewX),  Math.min(y+viewY, lines.size()-1));
	}
//	public String wordAt(Point p) {
//		try {
//			String line = lines.get(p.getY());
//			if(line.charAt(p.getX())==' ') {return null;}
//			int start = 0, end = 0;
//			int di = 1, dj=-1;
//			for(int i = p.getX(), j=p.getX(); ;i+=di,j-=dj) {
//				if(i<0) {i=0; di=0;}
//				if(j>=line.length()) {j=line.length(); dj=0;}
//				if(di==0 && dj==0) {start = i; end = j; break;}
//				
//				char a = line.charAt(i), b = line.charAt(j);
//				if(a==' ') {di=0;}
//				if(b==' ') {dj=0;}
//			}
//			//System.out.println("Word is "+line.substring(start,end));
//			return line.substring(start, end);
//		}catch (Exception e) {
//			return null;
//		}
//	}

	public boolean isFoucused() {
		return focused;
	}
	public boolean isEditable() {
		return isEditable;
	}
}