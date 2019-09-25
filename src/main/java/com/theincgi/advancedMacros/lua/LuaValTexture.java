package com.theincgi.advancedMacros.lua;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.event.TaskDispatcher;
import com.theincgi.advancedMacros.misc.Settings;

import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.ResourceLocation;

public class LuaValTexture extends LuaValue{
	ResourceLocation r;
	DynamicTexture dTex;
	private float u1,v1, u2=1,v2=1;
	String name;
	/**@param name - used by Settings.fromDynamic(...), should be unique, file name is a suggestion*/
	public LuaValTexture(String name, DynamicTexture dTex) {
		super();
		this.dTex = dTex;
		this.name = name;
		r = Settings.fromDynamic(name, dTex);
	}
	public LuaValTexture(DynamicTexture dTex) {
		super();
		this.dTex = dTex;
	}
	public LuaValTexture(String name, ResourceLocation r) {
		super();
		this.name = name;
		this.r = r;
	}
	@Override
	public int type() {
		return LuaValue.TUSERDATA;
		
	}
	
	public void setUV(float u1, float v1, float u2, float v2) {
		this.u1 = u1;
		this.u2 = u2;
		this.v1 = v1;
		this.v2 = v2;
	}
	
	public void update(){
		if(dTex==null){return;}
		dTex.updateDynamicTexture();
	}
	
	public void deleteTex(){
		TaskDispatcher.addTask(()->{
			if(dTex==null){return;}
			//TODO keep an eye out for decomp src to see if this is needed here or if it happens in delete dTex.getTextureData().close();
			dTex.deleteGlTexture();
			dTex = null;
		});
	}

	public void setBlockResource() {
		this.r = AtlasTexture.LOCATION_BLOCKS_TEXTURE;
	}
	
	@Override
	public String typename() {
		return TYPE_NAMES[type()];
	}
	public ResourceLocation getResourceLocation() {
		return r;
	}
	public void bindTexture() {
		if(r!=null) {
			AdvancedMacros.getMinecraft().getTextureManager().bindTexture(r);
		}else {
			//GL11.glBindTexture(GL11.GL_TEXTURE_2D, dTex.getGlTextureId()); //GLStatemanager was spamming errors.... "OpenGL debug message, id=1281, source=API, type=ERROR, severity=HIGH, message=Error has been generated. GL error GL_INVALID_VALUE in (null): (ID: 173538523) Generic error"
			GlStateManager.bindTexture(dTex.getGlTextureId());  //
		}
	}
	public DynamicTexture getDynamicTexture() {
		return dTex;
	}
	public float uMin() {
		return u1;
	}
	public float uMax() {
		return u2;
	}
	public float vMin() {return v1;}
	public float vMax() {return v2;}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if(dTex!=null) {
			System.out.println("Texture ID " + dTex.getGlTextureId() + " is being removed for object finalization.");
			deleteTex();
		}
			
	}
	
}