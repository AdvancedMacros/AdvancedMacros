package com.theincgi.advancedmacros.lua.functions;

import com.theincgi.advancedmacros.AdvancedMacros;
import com.theincgi.advancedmacros.misc.Permissions;
import com.theincgi.advancedmacros.misc.Utils;
import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import java.io.File;
import java.io.FileReader;

public class Run extends VarArgFunction {

    @Override
    public Varargs invoke(Varargs arg0) {
        try {
            File f = Utils.parseFileLocation(arg0.arg1());
            
            Permissions.checkRunFile(f);
            
            //			File f = new File(AdvancedMacros.macrosFolder, arg0.arg1().tojstring());
            FileReader fr = new FileReader(f);
            LuaValue function = AdvancedMacros.globals.load(fr, f.getAbsolutePath());
            Varargs args = function.invoke(arg0.subargs(2));
            return args;
        } catch (Exception e) {
            throw new LuaError(e);
        }
    }

}
