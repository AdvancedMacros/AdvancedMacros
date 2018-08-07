package com.theincgi.advancedMacros.lua;

import org.luaj.vm2_v3_0_1.LuaValue;

import com.theincgi.advancedMacros.misc.Settings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
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
		if(dTex==null){return;}
		dTex.deleteGlTexture();
		dTex = null;
	}

	@Override
	public String typename() {
		return TYPE_NAMES[type()];
	}
	public ResourceLocation getResourceLocation() {
		return r;
	}
	public void bindTexture() {
		if(r!=null)
			Minecraft.getMinecraft().getTextureManager().bindTexture(r);
		else
			GlStateManager.bindTexture(dTex.getGlTextureId());
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