package com.theincgi.advancedMacros.mixin;

import com.theincgi.advancedMacros.misc.Settings;
import com.theincgi.advancedMacros.misc.Utils;
import net.minecraft.client.gui.GuiNewChat;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(GuiNewChat.class)
public class MixinChatLinesLimit {

    @ModifyConstant(
        method = "setChatLine(Lnet/minecraft/util/text/ITextComponent;IIZ)V",
        constant = @Constant(intValue = 100)
    )
    public int setChatLines(int lines) {
        return getMaxLineCount();
    }

    public int getMaxLineCount() {
        try {
			return Utils.tableFromProp(Settings.settings, "chat.maxLines", LuaValue.valueOf(100)).checkint();
		}catch (Exception | Error e) {
			return 100;
		}
    }
}
