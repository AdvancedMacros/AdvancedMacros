package com.theincgi.advancedMacros.gui;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Stack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.theincgi.advancedMacros.gui.elements.Drawable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class Gui extends net.minecraft.client.gui.GuiScreen{
	FontRenderer fontRend = Minecraft.getMinecraft().fontRenderer;
	private LinkedList<KeyTime> heldKeys = new LinkedList<>();
	public LinkedList<InputSubscriber> inputSubscribers = new LinkedList<>();
	/**The next key or mouse event will be sent to this before anything else*/
	public InputSubscriber nextKeyListen = null;
	private LinkedList<Drawable> drawables = new LinkedList<>();
	public volatile Drawable drawLast = null;
	private Focusable focusItem = null;
	/**Strictly key typed and mouse clicked events atm*/
	public InputSubscriber firstSubsciber;

	private int repeatMod = 0;
	private boolean drawDefaultBackground = true;

	public Gui() {
		super.mc = Minecraft.getMinecraft();
	}

	@Override
	public void drawHorizontalLine(int startX, int endX, int y, int color) {
		super.drawHorizontalLine(startX, endX, y, color);
	}
	@Override
	public void drawVerticalLine(int x, int startY, int endY, int color) {
		super.drawVerticalLine(x, startY, endY, color);
	}
	public static void drawBoxedRectangle(int x,int y, int w, int h,int boarderW,int frame, int fill){
		drawRect(x, 	     y, 	     x+w+1, 		   y+h+1, 		   frame);
		drawRect(x+boarderW, y+boarderW, x+w-boarderW+1,   y+h-boarderW+1, fill);
	}
	public void drawBoxedRectangle(int x,int y, int w, int h,int frame, int fill){
		drawRect(x, y, x+w+1,   y+h+1, fill);
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
			int offset = cWid/2 - fr.getCharWidth(c)/2;
			fr.drawString(c+"", x+offset+cWid*i, y, color);
		}
		return cWid*str.length()+x;
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if(nextKeyListen!=null && nextKeyListen.onKeyPressed(this, typedChar, keyCode)){nextKeyListen = null; return;}
		if(firstSubsciber!=null && firstSubsciber.onKeyPressed(this, typedChar, keyCode)){return;}
		super.keyTyped(typedChar, keyCode);
		heldKeys.add(new KeyTime(keyCode, typedChar));
		for (InputSubscriber inputSubscriber : inputSubscribers) {
			if(inputSubscriber.onKeyPressed(this, typedChar, keyCode)) break;
		}
	}
	/**fires after key has been held in for a time
	 * mod will always be positive
	 * <br><b>Tip</b>: Use mod to reduce key repeat speed.
	 * <blockquote><br> if(mod%5==0){...} </code></blockquote>>*/
	public void keyRepeated(char typedChar, int keyCode, int mod){
		if(firstSubsciber!=null && firstSubsciber.onKeyRepeat(this, typedChar, keyCode, mod)){return;}
		for (InputSubscriber inputSubscriber : inputSubscribers) {
			if(inputSubscriber.onKeyRepeat(this, typedChar, keyCode, mod)) break;
		}
	}
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if(drawDefaultBackground)
			drawDefaultBackground();

		int i = Mouse.getDWheel();
		mouseScroll((int) Math.signum(i));
		Stack<KeyTime> killList = new Stack<>();
		for(KeyTime k:heldKeys){
			k.fireKeyRepeat();
			if(k.dead){
				killList.push(k);
			}
		}
		for (KeyTime keyTime : killList) {
			for (InputSubscriber inputSubscriber : inputSubscribers) {
				if(inputSubscriber.onKeyRelease(this, keyTime.key, keyTime.keyCode)) break;
			}
			heldKeys.remove(keyTime);
		}

		synchronized (drawables) {
			for (Drawable drawable : drawables) {
				GlStateManager.pushAttrib();
				//GlStateManager.enableAlpha();
				//GlStateManager.disableBlend();
				//GlStateManager.enableColorMaterial();


				drawable.onDraw(this, mouseX, mouseY, partialTicks);
				GlStateManager.popAttrib();
			}
		}
		if(drawLast!=null){
			drawLast.onDraw(this, mouseX, mouseY, partialTicks);
		}
	}
	//	public static ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/hopper.png");
	public void drawImage(ResourceLocation texture, int x, int y, int wid, int hei, float uMin, float vMin, float uMax, float vMax){

		//GlStateManager.pushMatrix();
		//GlStateManager.pushAttrib();

		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);


		//GlStateManager.enableBlend();
		//GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		//GlStateManager.color(1F, 1F, 1F, 1F);
		GlStateManager.enableAlpha();
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(x, y, 0).tex(uMin, vMin).endVertex();
		buffer.pos(x, y+hei, 0).tex(uMin, vMax).endVertex();
		buffer.pos(x+wid, y+hei, 0).tex(uMax, vMax).endVertex();
		buffer.pos(x+wid, y, 0).tex(uMax, vMin).endVertex();
		Tessellator.getInstance().draw();

		//Minecraft.getMinecraft().getTextureManager().
		//GlStateManager.popMatrix();
		//GlStateManager.popAttrib();
	}






	public static void drawPixel(int x, int y, int color){
		drawRect(x, y, x+1, y+1, color);
	}

	//Called by drawScreen, gets overridden by gui
	public void mouseScroll(int i){
		if(firstSubsciber!=null && firstSubsciber.onScroll(this, i)) return;
		synchronized (drawables) {

			for (InputSubscriber inputSubscriber : inputSubscribers) {
				if(inputSubscriber.onScroll(this, i)) break;
			}

		}
	}

	private class KeyTime{
		int keyCode;
		char key;
		long timeSig;
		static final int validationTime = 500;
		static final int repeatDelay = 10;
		boolean dead = false;
		public KeyTime(int keyCode, char key) {
			super();
			this.keyCode = keyCode;
			this.key = key;
			this.timeSig = System.currentTimeMillis();
		}

		public boolean isValid(){
			if(!Keyboard.isKeyDown(keyCode)){
				dead=true;
				return false;}
			return System.currentTimeMillis()-timeSig>validationTime;
		}

		public void fireKeyRepeat(){
			if(isValid()){
				timeSig=System.currentTimeMillis()-validationTime+repeatDelay;
				keyRepeated(key, keyCode,repeatMod);
				repeatMod = Math.max(0, repeatMod+1);//must always be positve that all
			}
		}
	}

	public FontRenderer getFontRend() {
		return fontRend;
	}
	@Override
	public void drawCenteredString(FontRenderer fontRendererIn, String text, int x, int y, int color) {
		int wid = fontRend.getStringWidth(text);
		fontRendererIn.drawString(text, x-wid/2, y-fontRend.FONT_HEIGHT/2, color, false);
	}


	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if(nextKeyListen!=null && nextKeyListen.onMouseClick(this, mouseX, mouseY, mouseButton)){nextKeyListen = null; return;}
		if(firstSubsciber!=null && firstSubsciber.onMouseClick(this, mouseX, mouseY, mouseButton)){return;}
		super.mouseClicked(mouseX, mouseY, mouseButton);
		//System.out.println("CLICK 1");
		for (InputSubscriber inputSubscriber : inputSubscribers) {
			if(inputSubscriber.onMouseClick(this, mouseX, mouseY, mouseButton))
				break;
		}
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		if(firstSubsciber!=null && firstSubsciber.onMouseRelease(this, mouseX, mouseY, state)){return;}
		for (InputSubscriber inputSubscriber : inputSubscribers) {
			if(inputSubscriber.onMouseRelease(this, mouseX, mouseY, state))
				break;
		}
	}
	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
		if(firstSubsciber!=null && firstSubsciber.onMouseClickMove(this, mouseX, mouseY, clickedMouseButton, timeSinceLastClick)) {return;}
		for (InputSubscriber inputSubscriber : inputSubscribers) {
			if(inputSubscriber.onMouseClickMove(this, mouseX, mouseY, clickedMouseButton, timeSinceLastClick))
				break;
		}
	}

	public static interface Focusable{
		public boolean isFocused();
		public void setFocused(boolean f);
	}
	public static interface InputSubscriber{ 
		/**@param i scroll amount*/
		public boolean onScroll(Gui gui, int i);

		public boolean onMouseClick(Gui gui, int x, int y, int buttonNum);
		public boolean onMouseRelease(Gui gui, int x, int y, int state);
		public boolean onMouseClickMove(Gui gui, int x, int y, int buttonNum, long timeSinceClick);
		/**aka keyTyped*/
		public boolean onKeyPressed(Gui gui, char typedChar, int keyCode);
		/**@param typedChar char value of typed key<br>
		 * @param keycode   number for key, typed char cant  have [up arrow] for example<br>
		 * @param repeatMod for reducing the number of repeat events, you can use % on this and pick say 1 in 3 events to use*/
		public boolean onKeyRepeat(Gui gui, char typedChar, int keyCode, int repeatMod);
		public boolean onKeyRelease(Gui gui, char typedChar, int keyCode);

	}

	public void showGui(){
		//AdvancedMacros.lastGui = this;
		Minecraft.getMinecraft().displayGuiScreen(this);
	}
	//something to call each time you switch back
	public void onOpen(){

	}

	public Focusable getFocusItem() {
		//System.out.println("Foooocas "+focusItem);
		return focusItem;
	}
	public void setFocusItem(Focusable focusItem) {
		this.focusItem = focusItem;
		//System.out.println("FOCUS: >> "+focusItem);
	}
	public int getUnscaledWindowWidth(){
		return Minecraft.getMinecraft().displayWidth;
	}
	public int getUnscaledWindowHeight(){
		return Minecraft.getMinecraft().displayHeight;
	}

	public void setDrawDefaultBackground(boolean drawDefaultBackground) {
		this.drawDefaultBackground = drawDefaultBackground;
	}
	public boolean getDrawDefaultBackground() {
		return drawDefaultBackground;
	}

	public void addDrawable(Drawable d) {
		synchronized (drawables) {
			drawables.add(d);
		}
	}
	public void removeDrawables(Drawable d) {
		synchronized (drawables) {
			drawables.remove(d);
		}
	}
	public void clearDrawables() {
		synchronized (drawables) {
			drawables.clear();
		}
	}
}