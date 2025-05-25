package com.theincgi.advancedmacros.event.handlers;

import static com.theincgi.advancedmacros.guiScripts.BindingsMenu.getBindingsMenu;

import com.theincgi.advancedmacros.AdvancedMacros;
import com.theincgi.advancedmacros.event.CombinedEventHandler;
import com.theincgi.advancedmacros.event.EventHandler;
import com.theincgi.advancedmacros.event.TaskDispatcher;
import com.theincgi.advancedmacros.event.events.KeyEvent;
import com.theincgi.advancedmacros.gui.elements.ColorTextArea;
import com.theincgi.advancedmacros.lua.LuaDebug;
import com.theincgi.advancedmacros.misc.HIDUtils;
import com.theincgi.advancedmacros.misc.Workspace;

import net.minecraft.client.MinecraftClient;

public class OnKey extends EventHandler<KeyEvent> {
	
	@Override
	public String getEventValue(KeyEvent event) {
		return event.getValue();
	}
	
	@Override
	public void onEvent(KeyEvent event) {
		if (AdvancedMacros.modKeybind.isPressed()) {
            if (ColorTextArea.isCTRLDown()) {
                if (ColorTextArea.isShiftDown()) {
                    AdvancedMacros.stopAll();
                } else {
                    MinecraftClient.getInstance().setScreen(AdvancedMacros.runningScriptsGui);
                }
            } else if (ColorTextArea.isShiftDown()) {
                CombinedEventHandler.showMenu(AdvancedMacros.scriptBrowser2, AdvancedMacros.macroMenuGui.getGui());
                
            } else if (HIDUtils.Keyboard.isAlt()) {
                LuaDebug.LuaThread thread = new LuaDebug.LuaThread(AdvancedMacros.repl, "REPL");
                thread.workspace = Workspace.DEFAULT;
                thread.start();
            } else {
                if (AdvancedMacros.lastGui != null) {
                    TaskDispatcher.delayTask(() -> {
                        AdvancedMacros.lastGui.showGui();
                    }, 85);
                } else {
//                    MacroMenuGui.showMenu();
                	AdvancedMacros.bindingsMenu.openBindingsMenu();
                }
            }
            return;
        }
		
		getBindingsMenu().triggerEvent(KEY_TYPE, getEventValue(event), event.createArgsTable());
	}
	
}
