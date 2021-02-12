package com.theincgi.advancedMacros.lua.functions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;







/*
 * CODE CREDIT: 
 * Code modified from "How to Run a Shell Command in Java" by baeldung 
 * https://www.baeldung.com/run-shell-command-in-java
 * 
 * and "How to Call Python From Java" by baeldung
 * https://www.baeldung.com/java-working-with-python
 */

public class RunPythonScript extends OneArgFunction {
	
	static String homeDirectory = System.getProperty("user.home");
	static Process process;

	
	public static void runPythonScript(String pythonPath) throws IOException {
	
          //String pythonExe = "C:/Users/AppData/Local/Continuum/Anaconda/python.exe";
          ProcessBuilder pb = new ProcessBuilder("python", pythonPath);
          Process p = pb.start();
          
          BufferedReader bfr = new BufferedReader(new InputStreamReader(p.getInputStream()));
          String line = "";
          System.out.println("Running Python starts: " + line);
          line = bfr.readLine();
          System.out.println("First Line: " + line);
          while ((line = bfr.readLine()) != null) {
              System.out.println("Python Output: " + line);
          }
	}
	
	/**
	 * {@summary runs a python script at the home directory + the passed filePath}
	 */
	@Override
	public LuaValue call(LuaValue valueL) {
	
		System.out.println("running pyScript");
		//runPythonScript(homeDirectory + "\\\\AppData\\\\Roaming\\\\.minecraft\\\\mods\\\\shock.py");
		
		try {
			runPythonScript(homeDirectory + valueL.tojstring());
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		
		return valueL;
	}
	
	
}

