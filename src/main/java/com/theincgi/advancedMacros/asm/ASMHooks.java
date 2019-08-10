package com.theincgi.advancedMacros.asm;

import org.luaj.vm2_v3_0_1.LuaValue;

import com.theincgi.advancedMacros.misc.LuaTextComponentClickEvent;
import com.theincgi.advancedMacros.misc.Settings;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.util.text.event.ClickEvent;

//If you are reading this there are a couple of files for Core mod features
//This one, 
//src/main/resources/<your amazing transformer script>.js   (does the transforming)
//src/main/resources/META-INF/coremods.json                 (says what script is used to transform with [the .js script])
//src/main/resources/META-INF/accesstransformer.cfg         (says what is going to be transformed)
public class ASMHooks {
	
	public static int getChatBufferSize() {
		try {
			return Utils.tableFromProp(Settings.settings, "chat.maxLines", LuaValue.valueOf(100)).checkint();
		}catch (Throwable e) {
			e.printStackTrace();
			return 100;
		}
	}
	
	public static boolean tryLuaTextComponent(Object obj) {
		if(obj instanceof ClickEvent) {
			ClickEvent ce = (ClickEvent)obj;
			if (ce instanceof LuaTextComponentClickEvent) {
				LuaTextComponentClickEvent ltcce = (LuaTextComponentClickEvent) ce;
				try {
					ltcce.click();
				}catch(Throwable t) {
					throw new RuntimeException("com.theincgi.advancedMacros.asm.ASMHooks#clickLuaTextComponent had an exception:", t);
				}
				return true;
			}
			
		}
		
		return false;
	}

}
