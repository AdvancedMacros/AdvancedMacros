package com.theincgi.advancedMacros.lua.functions.os;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

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
		StringSelection ss = new StringSelection(toClipboard);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, ss);
	}
	public static String getClipboard() {
		try {
			return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
