package com.theincgi.advancedmacros.mixin.events;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.theincgi.advancedmacros.event.Event;
import com.theincgi.advancedmacros.misc.Utils;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;

@Mixin(ChatHud.class)
public class MixinChatHud {


    @Inject(at = @At("HEAD"), method = "addMessage(Lnet/minecraft/text/Text;)V", cancellable = true)
    public void am_onOnChatMessage(Text message, CallbackInfo ci) {
//    	EventHandlers.onChat.onEvent(new ChatEvent(message, ci));
    	
//        if (AdvancedMacros.EVENT_HANDLER.eventExists(CombinedEventHandler.EventName.ChatFilter)) {
//            AdvancedMacros.EVENT_HANDLER.onChatEvent(message, CombinedEventHandler.EventName.ChatFilter);
//            ci.cancel();
//            return;
//        }
//        OnChat.onChat.onEvent(message, CombinedEventHandler.EventName.Chat);
    }
    
    public class ChatEvent extends Event {
    	
    	Text message;
    	
    	public ChatEvent(Text message, CallbackInfo ci) {
    		super(ci);
    		this.message = message;
		}
    	
    	@Override
    	public LuaTable createArgsTable() {
    		var args = new LuaTable();
    		
    		var unformatted = message.getString();
    		var pair = Utils.codedFromTextComponent(message);
    		String formatted = pair.a;
    		LuaTable actions = pair.b;
    		
    		args.set(1, formatted);
    		args.set(2, unformatted);
    		args.set(3, actions);
    		
    		return args;
    	}
    	
    }
}