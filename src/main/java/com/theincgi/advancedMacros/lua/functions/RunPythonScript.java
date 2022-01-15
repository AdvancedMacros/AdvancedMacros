package com.theincgi.advancedMacros.lua.functions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;









/*
 * CODE CREDIT: 
 * Code modified from "How to Run a Shell Command in Java" by baeldung 
 * https://www.baeldung.com/run-shell-command-in-java
 * 
 * and "How to Call Python From Java" by baeldung
 * https://www.baeldung.com/java-working-with-python
 */

public class RunPythonScript extends TwoArgFunction {
	
	static String homeDirectory = System.getProperty("user.home");
	static Process process;

	public static String[] runPythonScript(String pythonPath, LuaValue arguments) throws IOException {
		
		
		  //convert our parameters to a single list of arguments
		  String[] args = new String[arguments.length()+2];
		  
		  args[0] = "python";
		  args[1] = pythonPath;
		  
		  for (int i = 2; i < args.length; i++) {
			  args[i] = arguments.tojstring(i-2);
		  }
		  
		 
		  return runPythonScript(args); //run the script with our args
	}
		  
	public static String[] runPythonScript(String[] args) throws IOException {
		  //create the processbuilder with our arguments
        ProcessBuilder pb = new ProcessBuilder(args);
        Process p = pb.start();
        
        ArrayList<String> output = new ArrayList<String>();
        
        //read the output
        BufferedReader bfr = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = "";
        System.out.println("Running Python starts: " + line);
        line = bfr.readLine();
        output.add(line);
        System.out.println("First Line: " + line);
        while ((line = bfr.readLine()) != null) {
            System.out.println("Python Output: " + line);
            output.add(line);
        }
        return output.toArray(new String[0]);
	}
	
	/**
	 * {@summary runs a python script at the home directory + the passed filePath}
	 */
	@Override
	public LuaValue call(LuaValue filePath, LuaValue args) {
	
		System.out.println("running pyScript");
		//runPythonScript(homeDirectory + "\\\\AppData\\\\Roaming\\\\.minecraft\\\\mods\\\\shock.py");
		
		String[] output = {};
		try {
			output = runPythonScript(homeDirectory + filePath.tojstring(), args);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		LuaValue luaOut = LuaValue.valueOf("");
		for (String s : output) {
			luaOut.add(LuaValue.valueOf(s));
			
		}
		
		return luaOut;
	}
	
	
}

