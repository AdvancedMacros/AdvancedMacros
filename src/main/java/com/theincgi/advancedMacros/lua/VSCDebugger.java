package com.theincgi.advancedMacros.lua;

import java.net.URI;

import org.luaj.vm2_v3_0_1.LuaTable;

import io.socket.client.IO;
import io.socket.client.Socket;

public class VSCDebugger {
	
	private Socket socket;
	
	public VSCDebugger() {
	}

	public static void openConnectionGui() {
		
	}
	
	//server must already be running in editor
	public void connect( int port ) {
		URI uri = URI.create("http://localhost:" + port);
		
		IO.Options options = IO.Options.builder()
			//
			.build();
		
		this.socket = IO.socket(uri, options);
	}
	
	public LuaTable generateFunctions() {
		LuaTable out = new LuaTable();
		
		return out;
	}
	
	public void emit() {
		socket.
	}
	
}
