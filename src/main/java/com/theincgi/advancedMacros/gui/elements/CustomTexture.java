package com.theincgi.advancedMacros.gui.elements;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;


//TODO check usage, LuaTexVal tends to get used, dont recall this as much
public class CustomTexture{
	BufferedImage bufferedImage;
	ByteBuffer buffer;
	private int textureID;
	public CustomTexture(BufferedImage buffImg) {
		this.bufferedImage = buffImg;
		buffer = ByteBuffer.allocateDirect(buffImg.getWidth()*buffImg.getHeight() * 4);
		//textureID = GL11.glGenTextures();
	}
	public void update(){
		buffer.clear();
		int[] pixels = new int[bufferedImage.getWidth()*bufferedImage.getHeight()];
		for (int y = 0; y < bufferedImage.getHeight(); y++) {
			for (int x = 0; x < bufferedImage.getWidth(); x++) {
				int pixel = pixels[y * bufferedImage.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red component
                buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green component
                buffer.put((byte) (pixel & 0xFF));               // Blue component
                buffer.put((byte) ((pixel >> 24) & 0xFF));    // Alpha component. Only for RGBA
			}
		}
		buffer.flip();
		
		
	}
	
	private void bindTex(){
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, bufferedImage.getWidth(), bufferedImage.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE	, buffer);
	}
	public void draw(int x, int y, int wid, int hei){
		GlStateManager.pushTextureAttributes();
//		GlStateManager.enableLighting();
//		//GlStateManager.disableDepth();
		bindTex();
		int z = 0;
		int uMin, uMax, vMin, vMax;
		uMin = vMin = 1;
		uMax = vMax = 0;
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(x, y, z).tex(uMin, vMin).endVertex();
		buffer.pos(x, y+hei, z).tex(uMin, vMax).endVertex();
		buffer.pos(x+hei, y+hei, z).tex(uMax, vMax).endVertex();
		buffer.pos(x+hei, y, z).tex(uMax, vMin).endVertex();
		Tessellator.getInstance().draw();
		unbindTex();
		GlStateManager.popAttributes();
	}
	private void unbindTex(){
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}
}