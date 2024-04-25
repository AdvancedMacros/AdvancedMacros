package com.theincgi.advancedmacros.hud.hud3D;

import com.theincgi.advancedmacros.AdvancedMacros;
import com.theincgi.advancedmacros.gui.Color;
import com.theincgi.advancedmacros.lua.LuaValTexture;
import com.theincgi.advancedmacros.misc.CallableTable;
import com.theincgi.advancedmacros.misc.Settings;
import com.theincgi.advancedmacros.misc.Utils;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.ThreeArgFunction;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

public class HoloBlock extends WorldHudItem {

    LuaValTexture texture;
    LuaValTexture textureUp;
    LuaValTexture textureDown;
    LuaValTexture textureNorth;
    LuaValTexture textureEast;
    LuaValTexture textureSouth;
    LuaValTexture textureWest;

    Color colorUp, colorDown, colorNorth,
            colorWest, colorEast, colorSouth;

    float uMin, uMax, vMin, vMax, width;

    public HoloBlock() {
        this("resource:holoblock");
    }

    public HoloBlock(String resourceName) {
        this(resourceName, 0, 0, 1, 1);
    }

    public HoloBlock(String resourceName, float uMin, float vMin, float uMax, float vMax) {
        this(Utils.checkTexture(Settings.getTextureID(resourceName)), uMin, vMin, uMax, vMax);
    }

    public HoloBlock(LuaValTexture texture) {
        this(texture, 0, 0, 1, 1);
    }

    public HoloBlock(LuaValTexture texture, float uMin, float vMin, float uMax, float vMax) {
        this.texture = texture;
        setUV(uMin, vMin, uMax, vMax);
        width = 1;

    }

    /**
     * flip'd with 1-# for the way I like it, 0,0 in top left
     */
    public void setUV(float uMin, float vMin, float uMax, float vMax) {
        this.uMin = uMin;
        this.uMax = uMax;
        this.vMin = vMin;
        this.vMax = vMax;
    }

    @Override
    public void render(MatrixStack ms) {
        //TODO dont render if player is facing other way
        if (texture != null) {
            texture.bindTexture();

            Matrix4f t = ms.peek().getPositionMatrix();
            color.apply();

            if (texture != null || textureNorth != null) {
                bindOrBind(textureNorth, texture);
                drawSideFace(t, 0, 0, 0, width, width, 0); //draw front
            }
            if (texture != null || textureEast != null) {
                bindOrBind(textureEast, texture);
                drawSideFace(t, -width, 0, 0, 0, width, width); //draw front
            }
            if (texture != null || textureSouth != null) {
                bindOrBind(textureSouth, texture);
                drawSideFace(t, -width, 0, -width, -width, width, 0);
            }
            if (texture != null || textureWest != null) {
                bindOrBind(textureWest, texture);
                drawSideFace(t, 0, 0, -width, 0, width, -width);
            }
            if (texture != null || textureUp != null) {
                bindOrBind(textureUp, texture);
                drawTopFace(t, 0, -width, 0, width, width);
            }
            if (texture != null || textureDown != null) {
                bindOrBind(textureDown, texture);
                drawBottomFace(t, 0, 0, 0, width, width);
            }

            //			RenderSystem.popMatrix();//protection for not messing up matrix
            //RenderSystem.popAttrib();
        }
    }

    private void bindOrBind(LuaValTexture pref, LuaValTexture def) {
        if (pref != null) {
            pref.bindTexture();
            if (pref != null) {
                setUV(pref.uMin(), pref.vMin(), pref.uMax(), pref.vMax());
            }
            return;
        }
        if (def != null) {
            def.bindTexture();
            setUV(def.uMin(), def.vMin(), def.uMax(), def.vMax());
        }
    }

    //north face
    private void drawSideFace(Matrix4f trf, float dx, float dy, float dz, float xWid, float yHei, float zWid) {
        float x = this.x - dx;
        float y = this.y - dy;
        float z = this.z - dz;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(trf, x, y, z).texture(uMax, vMax).next();
        buffer.vertex(trf, x, y + yHei, z).texture(uMax, vMin).next();
        buffer.vertex(trf, x + xWid, y + yHei, z + zWid).texture(uMin, vMin).next();
        buffer.vertex(trf, x + xWid, y, z + zWid).texture(uMin, vMax).next();
        tessellator.draw();
    }

    private void drawTopFace(Matrix4f trf, float dx, float dy, float dz, float wid, float len) {
        float x = this.x - dx;
        float y = this.y - dy;
        float z = this.z - dz;

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(trf, x, y, z).texture(uMin, vMin).next();
        buffer.vertex(trf, x, y, z + len).texture(uMin, vMax).next();
        buffer.vertex(trf, x + wid, y, z + len).texture(uMax, vMax).next();
        buffer.vertex(trf, x + wid, y, z).texture(uMax, vMin).next();
        Tessellator.getInstance().draw();
    }

    private void drawBottomFace(Matrix4f transform, float dx, float dy, float dz, float wid, float len) {
        float x = this.x - dx;
        float y = this.y - dy;
        float z = this.z - dz;

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(transform, x + wid, y, z).texture(uMin, vMin).next();
        buffer.vertex(transform, x + wid, y, z + len).texture(uMin, vMax).next();
        buffer.vertex(transform, x, y, z + len).texture(uMax, vMax).next();
        buffer.vertex(transform, x, y, z).texture(uMax, vMin).next();
        Tessellator.getInstance().draw();
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setTexture(LuaValTexture v) {
        texture = v;
    }

    /**
     * do not give null type
     */
    @Override
    public void setDrawType(DrawType drawType) {
        if (drawType == null) {
            throw new NullPointerException("Draw type may not be null");
        }
        this.drawType = drawType;
    }

    public enum DrawType {
        XRAY,
        NO_XRAY;

        public boolean isXRAY() {
            switch (this) {
                case XRAY:
                    return true;
                default:
                    return false;
            }
        }
    }

    @Override
    public void loadControls(LuaValue t) {
        super.loadControls(t);
        t.set("setWidth", new CallableTable(new String[]{"hud3D", "newBlock()", "setWidth"}, new SetWidth()));
        t.set("changeTexture", new CallableTable(new String[]{"hud3D", "newBlock()", "changeTexture"}, new ChangeTexture()));
        t.set("setUV", new CallableTable(new String[]{"hud3D", "newBlock()", "setUV"}, new SetUV()));
        t.set("overlay", new CallableTable(new String[]{"hud3D", "newBlock()", "overlay"}, new Overlay()));
        t.set("setColor", new CallableTable(new String[]{"hud3D", "newBlock()", "setColor"}, new setColorSide()));
        t.set("getColor", new CallableTable(new String[]{"hud3D", "newBlock()", "getColor"}, new GetColorSide()));
    }

    private class setColorSide extends TwoArgFunction {

        @Override
        public LuaValue call(LuaValue color, LuaValue optSide) {
            Color c = Utils.parseColor(color, AdvancedMacros.COLOR_SPACE_IS_255);
            if (optSide.isstring()) {
                switch (optSide.checkjstring().toLowerCase()) {
                    case "up":
                    case "top":
                        colorUp = c;
                        return NONE;
                    case "north":
                        colorNorth = c;
                        return NONE;
                    case "west":
                        colorWest = c;
                        return NONE;
                    case "east":
                        colorEast = c;
                        return NONE;
                    case "south":
                        colorSouth = c;
                        return NONE;
                    case "down":
                    case "bottom":
                        colorDown = c;
                        return NONE;
                    default:
                        throw new LuaError("Undefined side '" + optSide.tojstring() + "'");
                }
            }
            HoloBlock.this.color = c;
            return NONE;
        }

    }

    private class GetColorSide extends OneArgFunction {

        @Override
        public LuaValue call(LuaValue optside) {
            boolean use = AdvancedMacros.COLOR_SPACE_IS_255;
            if (optside.isstring()) {
                switch (optside.checkjstring().toLowerCase()) {
                    case "up":
                    case "top":
                        return colorUp.toLuaValue(use);
                    case "north":
                        return colorNorth.toLuaValue(use);
                    case "west":
                        return colorWest.toLuaValue(use);
                    case "east":
                        return colorEast.toLuaValue(use);
                    case "south":
                        return colorSouth.toLuaValue(use);
                    case "down":
                    case "bottom":
                        return colorDown.toLuaValue(use);
                    default:
                        throw new LuaError("Undefined side '" + optside.tojstring() + "'");
                }
            }
            return color.toLuaValue(use);
        }

    }

    private class SetWidth extends OneArgFunction {

        @Override
        public LuaValue call(LuaValue arg) {
            setWidth((float) arg.checkdouble());
            return LuaValue.NONE;
        }

    }

    private class ChangeTexture extends TwoArgFunction {

        @Override
        public LuaValue call(LuaValue arg, LuaValue optSide) {
            LuaValTexture tmp = Utils.parseTexture(arg);
            if (optSide.isnil()) {
                texture = tmp;
            } else {
                switch (optSide.tojstring().toLowerCase()) {
                    case "up":
                    case "top":
                        textureUp = tmp;
                        break;
                    case "north":
                        textureNorth = tmp;
                        break;
                    case "west":
                        textureWest = tmp;
                        break;
                    case "east":
                        textureEast = tmp;
                        break;
                    case "south":
                        textureSouth = tmp;
                        break;
                    case "down":
                    case "bottom":
                        textureDown = tmp;
                        break;

                    default:
                        throw new LuaError("Unknown side '" + optSide.tojstring() + "'");
                }
            }
            //			setTexture(tex);
            //			if(arg instanceof LuaValTexture)
            //				setTexture(tex = Utils.checkTexture(arg));
            //			else
            //				setTexture(tex = Utils.checkTexture(Settings.getTextureID(arg.checkjstring())));

            return LuaValue.NONE;
        }

    }

    private class SetUV extends VarArgFunction {

        @Override
        public Varargs invoke(Varargs args) {
            if (args.narg() < 4) {
                throw new LuaError("Not enough args (uMin, vMin, uMax, vMax)");
            }
            setUV((float) args.arg(1).checkdouble(), (float) args.arg(2).checkdouble(), (float) args.arg(3).checkdouble(), (float) args.arg(4).checkdouble());
            return LuaValue.NONE;
        }

    }

    private class Overlay extends ThreeArgFunction {

        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
            setPos((float) (arg1.optdouble(x) - .0005), (float) (arg2.optdouble(y) - .0005), (float) (arg3.optdouble(z) - .0005));
            width = 1.001f;
            return LuaValue.NONE;
        }

    }

    @Override
    public String toString() {
        return "HoloBlock [width=" + width + ", color=" + color + ", drawType=" + drawType + ", x=" + x + ", y=" + y
                + ", z=" + z + ", isDrawing()=" + isDrawing() + "]";
    }

}
