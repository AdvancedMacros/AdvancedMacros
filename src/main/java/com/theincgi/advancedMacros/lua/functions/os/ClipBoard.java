package com.theincgi.advancedMacros.lua.functions.os;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;
import org.lwjgl.glfw.GLFW;

import com.theincgi.advancedMacros.AdvancedMacros;

public class ClipBoard {
	public static class GetClipboard extends ZeroArgFunction {
		@Override
		public LuaValue call() {
			return valueOf(getClipboard());
		}
	}
	public static class SetClipboard extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue arg) {
			setClipboard(arg.tojstring());
			return NONE;
		}
	}
	
	public static void setClipboard(String toClipboard) {
		GLFW.glfwSetClipboardString(AdvancedMacros.getMinecraft().getMainWindow().getHandle(), toClipboard);
	}
	public static String getClipboard() {
		try {
			return GLFW.glfwGetClipboardString(AdvancedMacros.getMinecraft().getMainWindow().getHandle());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
