package com.theincgi.advancedMacros.misc;

import java.awt.AWTException;
import java.awt.Robot;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.lwjgl.glfw.GLFW;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.event.TaskDispatcher;
import com.theincgi.advancedMacros.misc.HIDUtils.Keyboard;

import net.minecraft.client.MainWindow;

public class HIDUtils {
	
	private static final long MC_WINDOW = AdvancedMacros.getMinecraft().getMainWindow().getHandle();
	
	
	public static class Keyboard {
		private final static ConcurrentHashMap<Integer, String> LOOKUP_BY_ID = new ConcurrentHashMap<>();
		private final static ConcurrentHashMap<String, Integer> LOOKUP_BY_NAME = new ConcurrentHashMap<>();
		private static final String KEY_PREFIX = "GLFW_KEY_" ;
		
		

		public static final int UNKNOWN_KEY_CODE = -1;
		public static final String UNKNOWN_KEY_NAME = "UNKNOWN";
		static {
			Field[] fields = GLFW.class.getDeclaredFields();
			for(Field f : fields) {
				String name = f.getName();
				if( name .startsWith(KEY_PREFIX) ) {
					name = name.substring(KEY_PREFIX.length());
					
					try {
						int code = f.getInt(null);
						if(name.startsWith("KP_")) {
							String name_new = "NUMPAD_"+name.substring(3);
							String name_old = "NUMPAD"+name.substring(3);
							
							LOOKUP_BY_ID.put(code, name_new);
							LOOKUP_BY_NAME.put(name_new, code);
							LOOKUP_BY_NAME.put(name_old, code);
						}else {
							LOOKUP_BY_ID.put(code, name);
							LOOKUP_BY_NAME.put(name, code);
						}
					} catch (IllegalArgumentException | IllegalAccessException e) {
					}
				}
			}
			
		}

		public static int codeOf(String name) {
			return LOOKUP_BY_NAME.getOrDefault(name, UNKNOWN_KEY_CODE);
		}
		public static String nameOf(int code) {
			return LOOKUP_BY_ID.getOrDefault(code, UNKNOWN_KEY_NAME);
		}


		public static boolean isDown(int code) {
			return GLFW.glfwGetKey(MC_WINDOW, code) != GLFW.GLFW_RELEASE;
		}
		
		public static LuaTable getStateTable() { return getStateTable(null); }
		public static LuaTable getStateTable(LuaTable out) {
			out = out==null? new LuaTable() : out;
			for (int code : LOOKUP_BY_ID.keySet()) {
				out.set(nameOf(code), isDown(code));
			}
			return out;
		}
		public static LuaValue modifiersToLuaTable(int modifiers) {
			LuaTable out = new LuaTable();
			out.set("shift", (modifiers & GLFW.GLFW_MOD_SHIFT) > 0);
			out.set("control", (modifiers & GLFW.GLFW_MOD_CONTROL) > 0);
			out.set("alt", (modifiers & GLFW.GLFW_MOD_ALT) > 0);
			out.set("capsLock", (modifiers & GLFW.GLFW_MOD_CAPS_LOCK) > 0);
			out.set("numLock", (modifiers & GLFW.GLFW_MOD_NUM_LOCK) > 0);
			out.set("super", (modifiers & GLFW.GLFW_MOD_SUPER) > 0);
			return out;
		}
		public static boolean isAlt() {
			return Keyboard.isDown(GLFW.GLFW_KEY_LEFT_ALT) || Keyboard.isDown(GLFW.GLFW_KEY_RIGHT_ALT);
		}
		public static boolean isCTRLDown(){
			return Keyboard.isDown(GLFW.GLFW_KEY_LEFT_CONTROL) || Keyboard.isDown(GLFW.GLFW_KEY_RIGHT_CONTROL);
		}
		public static boolean isShiftDown(){
			return Keyboard.isDown(GLFW.GLFW_KEY_LEFT_SHIFT) || Keyboard.isDown(GLFW.GLFW_KEY_RIGHT_SHIFT); 
		}
	}
	
	public static class Mouse {
		public static final int UNKNOWN_MOUSE_BUTTON = -1;
		private final static String MOUSE_PREFIX = "MOUSE:";
		
		public static String nameOf(int buttonNumber) {
			switch (buttonNumber) {
			case 0: return "LMB";
			case 1: return "RMB";
			case 2: return "MMB";
			default:
				return MOUSE_PREFIX+(buttonNumber+1);
			}
		}
		public static int codeOf(String name) {
			switch (name) {
			case "LMB": return 0;
			case "RMB": return 1;
			case "MMB": return 2;

			default:
				try {
					return Integer.parseInt(name.substring(MOUSE_PREFIX.length()));
				}catch (Exception e) {
					return UNKNOWN_MOUSE_BUTTON;
				}
			}
		}
		
		public static boolean isDown(int code) {
			return GLFW.glfwGetMouseButton(MC_WINDOW, code) != GLFW.GLFW_RELEASE;
		}
		public static LuaTable getStateTable() { return getStateTable(null); }
		public static LuaTable getStateTable(LuaTable out) {
			out = out==null? new LuaTable() : out;
			for( int i = 0; i <= 8; i++) {
				out.set(nameOf(i), isDown(i));
			}
			return out;
		}
		
		@SuppressWarnings("resource")
		public static void setGrabbed(boolean b) {
			if(b)
				AdvancedMacros.getMinecraft().mouseHelper.grabMouse();
			if(b)
				AdvancedMacros.getMinecraft().mouseHelper.ungrabMouse();
		}
		
		public static int getButtonCount() {
			return 8;
		}
		public static void setCursorPosition(int x, int y) {
			try {
				Robot r = new Robot();
				MainWindow m = AdvancedMacros.getMinecraft().getMainWindow();
				//TESTME Add difference from width vs framebuffer?
				r.mouseMove(m.getWindowX()+x, m.getWindowY()+y);
			} catch (AWTException e) {
				e.printStackTrace();
			}
		}
	}
	
	//TODO joystick and gamepad support!
	public static class Joystick {
		public static final String JOYSTICK_PREFIX = "JOYSTICK_";
		public static final int UNKNOWN_JOYSTICK_BUTTON = -1;
		
		public static String nameOf(int code) {
			return JOYSTICK_PREFIX+(code+1);
		}
		public static int codeOf(String name) {
			try {
				return Integer.parseInt(name.substring(JOYSTICK_PREFIX.length()));
			}catch (Exception e) {
				return UNKNOWN_JOYSTICK_BUTTON;
			}
		}
		public static boolean isDown(int jID, int code) {
			return GLFW.glfwGetJoystickButtons(jID).get(code) == GLFW.GLFW_PRESS;
		}
		public static String getGUID(int jID) {
			return GLFW.glfwGetJoystickName(jID);
		}
		public static HatDir getHat(int jID, int hatNum) {
			ByteBuffer states = GLFW.glfwGetJoystickHats(jID);
			if(states == null) return null;
			return HatDir.fromGLFW(states.get(hatNum));
		}
		public static float getAxis(int jID, int axisNum) {
			FloatBuffer states = GLFW.glfwGetJoystickAxes(jID);
			if(states == null) return Float.NaN;
			return states.get(axisNum);
		}
		public static LuaTable getStateTable() { return getStateTable(null); }
		public static LuaTable getStateTable(LuaTable out) {
			int jID = 0;
			out = out == null ? new LuaTable() : out;
			while(GLFW.glfwJoystickPresent(jID)) {
				if(GLFW.glfwJoystickIsGamepad(jID)) { jID++; continue;}
				
				LuaTable j = new LuaTable();
				ByteBuffer buttons = GLFW.glfwGetJoystickButtons(jID);
				ByteBuffer hats = GLFW.glfwGetJoystickHats(jID);
				FloatBuffer axis = GLFW.glfwGetJoystickAxes(jID);
				String name = GLFW.glfwGetJoystickName(jID);
				String guid = GLFW.glfwGetJoystickGUID(jID);
				
				LuaTable buttonTable = new LuaTable();
				j.set("buttons", buttonTable);
				for(int i = 0; i<buttons.limit(); i++) {
					buttonTable.set(i+1, buttons.get(i)==GLFW.GLFW_PRESS);
				}
				
				LuaTable hatsTable = new LuaTable();
				j.set("hats", hatsTable);
				for(int i = 0; i<hats.limit(); i++) {
					hatsTable.set(i+1, HatDir.fromGLFW(hats.get(i)).asTable());
				}
				
				LuaTable axisTable = new LuaTable();
				j.set("axis", axisTable);
				for(int i = 0; i<axis.limit(); i++) {
					axisTable.set(i+1, axis.get(i));
				}
				
				j.set("name", name);
				j.set("guid", guid);
				out.set(jID+1, j);
				jID++;
			}
			return out;
		}
		
		
	}
	
	public static class GamePad {
		public static final String JOYSTICK_PREFIX = "GAMEPAD_";
		public static final int UNKNOWN_GAMEPAD_BUTTON = -1;
		public static String nameOf(int code) {
			switch (code) {
			case 0: return "A";
			case 1: return "B";
			case 2: return "X";
			case 3: return "Y";
			case 4: return "LB";
			case 5: return "RB";
			case 6: return "BACK";
			case 7: return "START";
			case 8: return "GUIDE";
			case 9: return "LEFT_THUMB";
			case 10: return "RIGHT_THUMB";
			case 11: return "D_UP";
			case 12: return "D_RIGHT";
			case 13: return "D_DOWN";
			case 14: return "D_LEFT";
			
			default:
				return "UNKNOWN:"+code;
			}
		}
		public static String axisNameOf(int code) {
			switch(code) {
			case 0: return "LEFT_X";
			case 1: return "LEFT_Y";
			case 2: return "RIGHT_X";
			case 3: return "RIGHT_Y";
			case 4: return "LEFT_TRIGGER";
			case 5: return "RIGHT_TRIGGER";
			default:
			return "UNKNOWN:"+code;
			}
		}
		//public static int codeOf(String name) {}
		public static boolean isDown(int jID, int code) {
			ByteBuffer state = GLFW.glfwGetJoystickButtons(jID);
			if(state == null) return false;
			return state.get(code) == GLFW.GLFW_PRESS;
		}
		public static LuaTable getStateTable() { return getStateTable(null); }
		public static LuaTable getStateTable(LuaTable out) {
			
			int jID = 0;
			out = out == null ? new LuaTable() : out;
			while(GLFW.glfwJoystickPresent(jID)) {
				if(!GLFW.glfwJoystickIsGamepad(jID)) {jID++; continue;}
				
				LuaTable j = new LuaTable();
				ByteBuffer buttons = GLFW.glfwGetJoystickButtons(jID);
				ByteBuffer hats = GLFW.glfwGetJoystickHats(jID);
				FloatBuffer axis = GLFW.glfwGetJoystickAxes(jID);
				String name = GLFW.glfwGetJoystickName(jID);
				String guid = GLFW.glfwGetJoystickGUID(jID);
				
				LuaTable buttonTable = new LuaTable();
				j.set("buttons", buttonTable);
				for(int i = 0; i<buttons.limit(); i++) {
					buttonTable.set(nameOf(i), buttons.get(i)==GLFW.GLFW_PRESS);
				}
				
				LuaTable hatsTable = new LuaTable();
				j.set("hats", hatsTable);
				for(int i = 0; i<hats.limit(); i++) {
					hatsTable.set(i+1, HatDir.fromGLFW(hats.get(i)).asTable());
				}
				
				LuaTable axisTable = new LuaTable();
				j.set("axis", axisTable);
				for(int i = 0; i<axis.limit(); i++) {
					axisTable.set(axisNameOf(i), axis.get(i));
				}
				
				j.set("name", name);
				j.set("guid", guid);
				out.set(jID+1, j);
				jID++;
			}
			return out;
			
		}
	}
	
	
	
	public static enum HatDir{
		UP, RIGHT, DOWN, LEFT, UP_RIGHT, DOWN_RIGHT, DOWN_LEFT, UP_LEFT, NONE;
		
		public boolean isUp() {
			return this.equals(UP) || this.equals(UP_LEFT) || this.equals(UP_RIGHT);
		}
		public boolean isDown() {
			return this.equals(DOWN) || this.equals(DOWN_LEFT) || this.equals(DOWN_RIGHT);
		}
		public boolean isLeft() {
			return this.equals(LEFT) || this.equals(DOWN_LEFT) || this.equals(UP_LEFT);
		}
		public boolean isRight() {
			return this.equals(RIGHT) || this.equals(DOWN_RIGHT) || this.equals(UP_RIGHT);
		}
		public LuaTable asTable() {
			LuaTable out = new LuaTable();
			out.set("left", this.isLeft());
			out.set("right", this.isRight());
			out.set("up", this.isUp());
			out.set("down", this.isDown());
			return out;
		}
		
		public static HatDir fromGLFW(int state) {
			switch (state) {
			case GLFW.GLFW_HAT_LEFT:
				return LEFT;
			case GLFW.GLFW_HAT_RIGHT:
				return RIGHT;
			case GLFW.GLFW_HAT_UP:
				return UP;
			case GLFW.GLFW_HAT_DOWN:
				return DOWN;
			case GLFW.GLFW_HAT_LEFT_UP:
				return UP_LEFT;
			case GLFW.GLFW_HAT_LEFT_DOWN:
				return DOWN_LEFT;
			case GLFW.GLFW_HAT_RIGHT_UP:
				return UP_RIGHT;
			case GLFW.GLFW_HAT_RIGHT_DOWN:
				return DOWN_RIGHT;
			default:
				return NONE;
			}
		}
	}
}