package com.theincgi.advancedmacros.lua;

import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;


public class LuaText extends LuaTable {

        private final Text message;

    public Text getMessage() {
        return message;
    }

    private LuaValue getData(Text message) {
        Optional<LuaValue>  data =  message.visit( (Style style, String text )-> {
            LuaTable luaStyle = new LuaTable();
            LuaTable luaData = new LuaTable();
            luaData.set("text",text);
            luaData.set("internal",style.toString());
            luaData.set("styles", luaStyle);
            luaData.set("color", style.getColor() != null ? LuaValue.valueOf(style.getColor().getName()) : LuaValue.valueOf(false));
            luaStyle.set("bold", style.isBold());
            luaStyle.set("italic", style.isItalic());
            luaStyle.set("underlined", style.isUnderlined());
            luaStyle.set("strikethrough", style.isStrikethrough());
            luaStyle.set("obfuscated", style.isObfuscated());

            return Optional.of(luaData);
        }, message.getStyle());
        return data.orElseGet(() -> LuaValue.valueOf(false));
    }

    public LuaValue getTextData(Text message) {
        return getTextData(message, new LuaTable());
    }

    private  LuaValue getTextData(Text message, LuaTable nodeData) {
        List<Text> siblings = message.getSiblings();
        nodeData.set(nodeData.length() + 1, getData(message));
        for (Text sibling : siblings) {
            getTextData(sibling, nodeData);
        }
        return nodeData;
    }

    public LuaText(Text message) {
            this.message = message;

            this.set("getData", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return getTextData(message);
                }
            });
        }



        @Override
        public int type() {
            return LuaValue.TUSERDATA;
        }

        @Override
        public String typename() {
            return LuaValue.TYPE_NAMES[type()];
        }
}
