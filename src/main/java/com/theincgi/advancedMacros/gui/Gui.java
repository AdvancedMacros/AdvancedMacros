package com.theincgi.advancedMacros.gui;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.gui.elements.Drawable;

import net.minecraft.client.MainWindow;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.INestedGuiEventHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class Gui extends Screen implements INestedGuiEventHandler {
	FontRenderer fontRend = AdvancedMacros.getMinecraft().fontRenderer;
	//private LinkedList<KeyTime> heldKeys = new LinkedList<>();
	private HashMap<Integer, Long> keyRepeatDelay = new HashMap<>();
	private LinkedList<InputSubscriber> inputSubscribers = new LinkedList<>();
	/**The next key or mouse event will be sent to this before anything else*/
	public InputSubscriber nextKeyListen = null;
	private LinkedList<Drawable> drawables = new LinkedList<>();
	public volatile Drawable drawLast = null;
	private Object focusItem = null;
	private int lastResWidth, lastResHeight;
	
	/**Strictly key typed and mouse clicked events atm*/
	public InputSubscriber firstSubsciber;

	private Queue<InputSubscriber> inputSubscriberToAdd = new LinkedList<>(), inputSubscribersToRemove = new LinkedList<>();
	private Queue<Drawable> drawableToAdd = new LinkedList<>(), drawableToRemove = new LinkedList<>();

	private int repeatMod = 0;
	private boolean drawDefaultBackground = true;

	
	public Gui() {
		super(null); //text component title
		super.minecraft = AdvancedMacros.getMinecraft();
	}

	public void drawHorizontalLine(int startX, int endX, int y, int color) {
		super.hLine(startX, endX, y, color);
	}
	public void drawVerticalLine(int x, int startY, int endY, int color) {
		super.vLine(x, startY, endY, color);
	}
	public static void drawBoxedRectangle(int x,int y, int w, int h,int boarderW,int frame, int fill){
		fill(x, 	     y, 	     x+w+1, 		   y+h+1, 		   frame);
		fill(x+boarderW, y+boarderW, x+w-boarderW+1,   y+h-boarderW+1, fill);
	}
	public void drawBoxedRectangle(int x,int y, int w, int h,int frame, int fill){
		fill(x, y, x+w+1,   y+h+1, fill);
		drawHollowRect(x, y, w, h, frame);
	}
	private void drawHollowRect(int x,int y,int w,int h,int col){
		drawHorizontalLine(x, x+w, y, col);
		drawHorizontalLine(x, x+w, y+h, col);
		drawVerticalLine(x, y, y+h, col);
		drawVerticalLine(x+w, y, y+h, col);
	}

	/**returns next x to use in this for multiColoring*/
	public int drawMonospaceString(String str, int x, int y, int color){
		FontRenderer fr = getFontRend();
		int cWid = (int) ((8f/12) * fr.FONT_HEIGHT);
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			float offset = cWid/2 - fr.getCharWidth(c)/2;
			fr.drawString(c+"", x+offset+cWid*i, y, color);
		}
		return cWid*str.length()+x;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		keyRepeatDelay.put(scanCode, System.currentTimeMillis());
		if(nextKeyListen!=null && nextKeyListen.onKeyPressed(this, keyCode, scanCode, modifiers)){nextKeyListen = null; return true;}
		if(firstSubsciber!=null && firstSubsciber.onKeyPressed(this, keyCode, scanCode, modifiers)){return true;}
		if(super.keyPressed(keyCode,scanCode, modifiers)) return true;
		//heldKeys.add(new KeyTime(keyCode, typedChar));
		synchronized (inputSubscribers) {
			for (InputSubscriber inputSubscriber : inputSubscribers) {
				if(inputSubscriber.onKeyPressed(this, keyCode, scanCode, modifiers)) return true;
			}
		}
		return false;
	}
	@Override
	public boolean charTyped(char typedChar, int modifiers) {
		if(nextKeyListen!=null && nextKeyListen.onCharTyped(this, typedChar, modifiers)){nextKeyListen = null; return true;}
		if(firstSubsciber!=null && firstSubsciber.onCharTyped(this, typedChar, modifiers)){return true;}
		if(super.charTyped(typedChar, modifiers)) return true;
		//heldKeys.add(new KeyTime(keyCode, typedChar));
		synchronized (inputSubscribers) {
			for (InputSubscriber inputSubscriber : inputSubscribers) {
				if(inputSubscriber.onCharTyped(this, typedChar, modifiers)) return true;
			}
		}
		return false;
	}
	
	
	/**fires after key has been held in for a time
	 * mod will always be positive
	 * <br><b>Tip</b>: Use mod to reduce key repeat speed.
	 * <blockquote><br> if(mod%5==0){...} </code></blockquote>>*/
	public boolean onKeyRepeated(Gui gui, int keyCode, int scanCode, int mods, int n){
		if(!keyRepeatDelay.containsKey(scanCode)) return false; //safety check
		if(keyRepeatDelay.get(scanCode) + 300 > System.currentTimeMillis()) return false; //wait a little while before key spaming
		//TODO customize key repeat delay
		if(firstSubsciber!=null && firstSubsciber.onKeyRepeat(gui, keyCode, scanCode, mods, n)){return true;}
		synchronized (inputSubscribers) {
			for (InputSubscriber inputSubscriber : inputSubscribers) {
				if(inputSubscriber.onKeyRepeat(gui, keyCode, scanCode, mods, n)) return true;
			}
		}
		return false;
	}

	@Override
	public boolean keyReleased(int p_223281_1_, int p_223281_2_, int p_223281_3_) {
		return onKeyRelease(this, p_223281_1_, p_223281_2_, p_223281_3_);
	}
	
	/**very overridable, this is called after input subscribers have not claimed this event*/
	public boolean onKeyRelease(Gui gui, int keyCode, int scanCode, int modifiers) {
		if(firstSubsciber!=null && firstSubsciber.onKeyRelease(this, keyCode, scanCode, modifiers)){return true;}
		synchronized (inputSubscribers) {
			for (InputSubscriber inputSubscriber : inputSubscribers) {
				if(inputSubscriber.onKeyRelease(this, keyCode, scanCode, modifiers)) return true;
			}
		}
		return false;
	}

	private MainWindow mainWindow;
	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		if(mainWindow == null)
			mainWindow = AdvancedMacros.getMinecraft().mainWindow;
		if(mainWindow.getScaledWidth()!=lastResWidth || mainWindow.getScaledHeight()!=lastResHeight) {
			lastResWidth = mainWindow.getScaledWidth();
			lastResHeight = mainWindow.getScaledHeight();
			resize(AdvancedMacros.getMinecraft(), lastResWidth, lastResHeight);
		}
		
		synchronized (inputSubscribers) {
			synchronized (inputSubscriberToAdd) {
				while(!inputSubscriberToAdd.isEmpty()) {
					inputSubscribers.add( inputSubscriberToAdd.poll() );
				}
			}
			synchronized (inputSubscribersToRemove) {
				while(!inputSubscribersToRemove.isEmpty()) {
					inputSubscribers.remove( inputSubscribersToRemove.poll() );
				}
			}
			
		}
		synchronized (drawables) {
			synchronized (drawableToAdd) {
				while(!drawableToAdd.isEmpty()) {
					drawables.add( drawableToAdd.poll() );
				}
			}
			synchronized (drawableToRemove) {
				while(!drawableToRemove.isEmpty()) {
					drawables.remove( drawableToRemove.poll() );
				}
			}
			
		}


		if(drawDefaultBackground)
			renderBackground(); //prev default bg

		if(AdvancedMacros.getMinecraft().currentScreen == this) { //do not steal the child gui's events!
//			int i = Mouse.getDWheel();
//			if(i!=0)
//				mouseScroll((int) Math.signum(i));
//			Stack<KeyTime> killList = new Stack<>();
//			for(KeyTime k:heldKeys){
//				k.fireKeyRepeat();
//				if(k.dead){
//					killList.push(k);
//				}
//			}
//			for (KeyTime keyTime : killList) {
//				boolean flag = false;
//				synchronized (inputSubscribers) {
//					for (InputSubscriber inputSubscriber : inputSubscribers) {
//						if(inputSubscriber.onKeyRelease(this, keyTime.key, keyTime.keyCode)) {
//							flag = true;
//							break;
//						}
//					}
//				}
//				if(!flag)
//					onKeyRelease(this, keyTime.key, keyTime.keyCode);
//				heldKeys.remove(keyTime);
//			}
		}

		synchronized (drawables) {
			for (Drawable drawable : drawables) {
				GlStateManager.pushTextureAttributes();
				//GlStateManager.enableAlpha();
				//GlStateManager.disableBlend();
				//GlStateManager.enableColorMaterial();


				drawable.onDraw(this, mouseX, mouseY, partialTicks);
				GlStateManager.popAttributes();
			}
		}
		if(drawLast!=null){
			drawLast.onDraw(this, mouseX, mouseY, partialTicks);
		}
	}
	//	public static ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/hopper.png");
	public void drawImage(ResourceLocation texture, int x, int y, int wid, int hei, float uMin, float vMin, float uMax, float vMax){


		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		//GlStateManager.pushMatrix();
		//GlStateManager.pushAttrib();

		AdvancedMacros.getMinecraft().getTextureManager().bindTexture(texture);

		//GlStateManager.enableBlend();
		//GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		//GlStateManager.color(1F, 1F, 1F, 1F);
		GlStateManager.enableAlphaTest();
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(x, y, 0).tex(uMin, vMin).endVertex();
		buffer.pos(x, y+hei, 0).tex(uMin, vMax).endVertex();
		buffer.pos(x+wid, y+hei, 0).tex(uMax, vMax).endVertex();
		buffer.pos(x+wid, y, 0).tex(uMax, vMin).endVertex();
		Tessellator.getInstance().draw();


		GL11.glPopAttrib();

		//AdvancedMacros.getMinecraft().getTextureManager().
		//GlStateManager.popMatrix();
		//GlStateManager.popAttrib();
	}






	public static void drawPixel(int x, int y, int color){
		fill(x, y, x+1, y+1, color);
	}

	@Override
	public boolean mouseScrolled(double x, double y, double i){
		if(firstSubsciber!=null && firstSubsciber.onScroll(this, i)) return true;
		synchronized (inputSubscribers) {
			for (InputSubscriber inputSubscriber : inputSubscribers) {
				if(inputSubscriber.onScroll(this, i)) return true;
			}

		}
		return false;
	}

//	private class KeyTime{
//		int keyCode;
//		char key;
//		long timeSig;
//		static final int validationTime = 500;
//		static final int repeatDelay = 10;
//		boolean dead = false;
//		public KeyTime(int keyCode, char key) {
//			super();
//			this.keyCode = keyCode;
//			this.key = key;
//			this.timeSig = System.currentTimeMillis();
//		}
//
//		public boolean isValid(){
//			if(!Keyboard.isDown(keyCode)){
//				dead=true;
//				return false;}
//			return System.currentTimeMillis()-timeSig>validationTime;
//		}
//
//		public void fireKeyRepeat(){
//			if(isValid()){
//				timeSig=System.currentTimeMillis()-validationTime+repeatDelay;
//				keyRepeated(key, keyCode,repeatMod);
//				repeatMod = Math.max(0, repeatMod+1);//must always be positve that all
//			}
//		}
//	}

	public FontRenderer getFontRend() {
		return fontRend;
	}
	@Override
	public void drawCenteredString(FontRenderer fontRendererIn, String text, int x, int y, int color) {
		int wid = fontRend.getStringWidth(text);
		fontRendererIn.drawString(text, x-wid/2, y-fontRend.FONT_HEIGHT/2, color);//, false);
	}


	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		if(nextKeyListen!=null && nextKeyListen.onMouseClick(this, mouseX, mouseY, mouseButton)){nextKeyListen = null; return true;}
		if(firstSubsciber!=null && firstSubsciber.onMouseClick(this, mouseX, mouseY, mouseButton)){return true;}
		

		//System.out.println("CLICK 1");
		synchronized (inputSubscribers) { 
			for (InputSubscriber inputSubscriber : inputSubscribers) {
				if(inputSubscriber.onMouseClick(this, mouseX, mouseY, mouseButton))
					return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int state) {
		if(firstSubsciber!=null && firstSubsciber.onMouseRelease(this, mouseX, mouseY, state)){return true;}
		synchronized (inputSubscribers) {
			for (InputSubscriber inputSubscriber : inputSubscribers) {
				if(inputSubscriber.onMouseRelease(this, mouseX, mouseY, state))
					return true;
			}
		}
		return super.mouseReleased(mouseX, mouseY, state);
	}
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double q, double r) {
		
		if(firstSubsciber!=null && firstSubsciber.onMouseClickMove(this, mouseX, mouseY, clickedMouseButton, q, r)) {return true;}
		synchronized (inputSubscribers) {
			for (InputSubscriber inputSubscriber : inputSubscribers) {
				if(inputSubscriber.onMouseClickMove(this, mouseX, mouseY, clickedMouseButton, q, r))
					return true;
			}
		}
		return super.mouseDragged(mouseX, mouseY, clickedMouseButton, q, r);
	}

	public static interface Focusable{
		public boolean isFocused();
		public void setFocused(boolean f);
	}
	public static interface InputSubscriber{ 
		/**@param i scroll amount*/
		public boolean onScroll(Gui gui, double i);

		public boolean onMouseClick(Gui gui, double x, double y, int buttonNum);
		public boolean onMouseRelease(Gui gui, double x, double y, int state);
		public boolean onMouseClickMove(Gui gui, double x, double y, int buttonNum, double q, double r);
		
		public boolean onCharTyped(Gui gui, char typedChar, int mods);
		/**aka keyTyped*/
		public boolean onKeyPressed(Gui gui, int keyCode, int scanCode, int modifiers);
		/**@param typedChar char value of typed key<br>
		 * @param keycode   number for key, typed char cant  have [up arrow] for example<br>
		 * @param repeatMod for reducing the number of repeat events, you can use % on this and pick say 1 in 3 events to use*/
		public boolean onKeyRepeat(Gui gui, int keyCode, int scanCode, int modifiers, int n);
		public boolean onKeyRelease(Gui gui, int keyCode, int scanCode, int modifiers);

	}

	public void showGui(){
		//AdvancedMacros.lastGui = this;
		AdvancedMacros.getMinecraft().displayGuiScreen(this);
	}
	//something to call each time you switch back
	public void onOpen(){

	}

	public Object getFocusItem() {
		//System.out.println("Foooocas "+focusItem);
		return focusItem;
	}
	public void setFocusItem(Object focusItem) {
		this.focusItem = focusItem;
		//System.out.println("FOCUS: >> "+focusItem);
	}
	public int getUnscaledWindowWidth(){
		return AdvancedMacros.getMinecraft().mainWindow.getWidth();
	}
	public int getUnscaledWindowHeight(){
		return AdvancedMacros.getMinecraft().mainWindow.getHeight();
	}

	public void setDrawDefaultBackground(boolean drawDefaultBackground) {
		this.drawDefaultBackground = drawDefaultBackground;
	}
	public boolean getDrawDefaultBackground() {
		return drawDefaultBackground;
	}



	public void addDrawable(Drawable d) {
		synchronized (drawableToAdd) {
			drawableToAdd.add(d);
		}
	}
	public void addInputSubscriber(InputSubscriber i) {
		synchronized (inputSubscriberToAdd) {
			inputSubscriberToAdd.add(i);
		}
	}
	public void removeDrawables(Drawable d) {
		synchronized (drawableToRemove) {
			drawableToRemove.add(d);
		}
	}
	public void removeInputSubscriber(InputSubscriber i) {
		synchronized (inputSubscribersToRemove) {
			inputSubscribersToRemove.add(i);
		}
	}
	public void clearDrawables() {
		synchronized (drawableToRemove) {
			drawableToRemove.addAll(drawables);
		}
	}
	public void clearInputSubscribers() {
		synchronized (inputSubscribersToRemove) {
			inputSubscribersToRemove.addAll(inputSubscribers);
		}
	}
	/**
	 * Synchronize usage on linked list!
	 * do not use to add or remove elements directly
	 * */
	protected LinkedList<InputSubscriber> getInputSubscribers() {
		return inputSubscribers;
	}

	public boolean onScroll(Gui gui, double i) {
		return false;
	}
}