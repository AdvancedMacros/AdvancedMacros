package com.theincgi.advancedmacros.lua;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theincgi.advancedmacros.event.TaskDispatcher;
import com.theincgi.advancedmacros.misc.Settings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import org.luaj.vm2_v3_0_1.LuaValue;

public class LuaValTexture extends LuaValue {

    Identifier r;
    NativeImageBackedTexture dTex;
    private float u1, v1, u2 = 1, v2 = 1;
    String name;

    /**
     * @param name - used by Settings.fromDynamic(...), should be unique, file name is a suggestion
     */
    public LuaValTexture(String name, NativeImageBackedTexture dTex) {
        super();
        this.dTex = dTex;
        this.name = name;
        r = Settings.fromDynamic(name, dTex);
    }

    public LuaValTexture(NativeImageBackedTexture dTex) {
        super();
        this.dTex = dTex;
    }

    public LuaValTexture(String name, Identifier r) {
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

    public void update() {
        if (dTex == null) {
            return;
        }
        dTex.upload();
    }

    public void deleteTex() {
        TaskDispatcher.addTask(() -> {
            if (dTex == null) {
                return;
            }
            //TODO keep an eye out for decomp src to see if this is needed here or if it happens in delete dTex.getTextureData().close();
            dTex.close();
            dTex = null;
        });
    }

    @Deprecated
    public void setBlockResource() {
//        this.r = PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
    }

    @Override
    public String typename() {
        return TYPE_NAMES[type()];
    }

    public Identifier getResourceLocation() {
        return r;
    }

    public void bindTexture() {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, r);
    }

    public NativeImageBackedTexture getDynamicTexture() {
        return dTex;
    }

    public float uMin() {
        return u1;
    }

    public float uMax() {
        return u2;
    }

    public float vMin() {
        return v1;
    }

    public float vMax() {
        return v2;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (dTex != null) {
            System.out.println("Texture ID " + dTex.getGlId() + " is being removed for object finalization.");
            deleteTex();
        }

    }

}
