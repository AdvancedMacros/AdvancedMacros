package com.theincgi.advancedmacros.misc;

import java.util.ArrayList;

public class Permissions {
	
	public boolean all;
	public boolean luaJava;
	public boolean readAnywhere;
	public boolean writeAnywhere;
	ArrayList<String> readWorkspace;
	ArrayList<String> writeWorkspace;
	ArrayList<String> runWorkspace;
	public boolean debug;
	public boolean playerControl;
	public boolean speak;
	public boolean hud2D;
	public boolean hud3D;
	public boolean http;
	public boolean viewSettings;
	public boolean saveSettings;
	public boolean viewClipboard;
	public boolean setClipboard;
	public boolean inventoryInteraction;
	public boolean narrate;
	public boolean manageScripts;
	public boolean toast;
}
