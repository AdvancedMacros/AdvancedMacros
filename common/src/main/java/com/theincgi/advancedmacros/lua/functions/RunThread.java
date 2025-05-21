package com.theincgi.advancedmacros.lua.functions;

import com.theincgi.advancedmacros.AdvancedMacros;
import com.theincgi.advancedmacros.lua.LuaDebug;
import com.theincgi.advancedmacros.lua.LuaDebug.LuaThread;
import com.theincgi.advancedmacros.misc.LuaClassUtils;
import com.theincgi.advancedmacros.misc.Permissions;
import com.theincgi.advancedmacros.misc.Utils;
import com.theincgi.advancedmacros.misc.Workspace;
import com.theincgi.advancedmacros.misc.Permissions.Permission;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class RunThread extends VarArgFunction {

    @Override
    public Varargs invoke(Varargs args) {
        try {
            LuaValue function = null;
            
            if(args.arg1().isstring() || LuaClassUtils.instanceOf(args.arg1(), "File")) {
                File f = Utils.parseFileLocation(args.arg1()); //new File(AdvancedMacros.macrosFolder, args.arg1().tojstring());
                
                Permissions.checkRunFile(f);
                
                FileReader fr = new FileReader(f);
                function = AdvancedMacros.globals.load(fr, f.getAbsolutePath());
                
            } else if(args.arg1().isfunction()) {
                function = args.arg1();
            
            } else {
                throw new LuaError("Arg 1 must be string, function or class:File");
            }
            LuaDebug.LuaThread thread = new LuaThread(function, args.subargs(2), args.arg1().tojstring());
            return thread.start();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new LuaError("No such file: " + args.arg1().tojstring());
        } catch (IOException e) {
        	 e.printStackTrace();
             throw new LuaError(e);
		}
    }

}
