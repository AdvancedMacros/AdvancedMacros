package com.theincgi.advancedmacros.misc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.theincgi.advancedmacros.AdvancedMacros;
import com.theincgi.advancedmacros.misc.Permissions.Permission;

public class Workspace {
	
	private static final Map<String, Workspace> workspaces = new ConcurrentHashMap<>();
	
	static {
		synchronized (workspaces) {
			for(var name : Workspace.listWorkspaceNames()) {
				try {
					workspaces.put(name, Workspace.load(name));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private String name;
	private final String path;
	private boolean valid = true;
	private boolean caseSensitiveModules = true;
	
	public Permissions permissions;
	
	public static final Workspace INTERNAL = new Workspace("internal", "resource:", true);
	public static final Workspace DEFAULT = new Workspace(AdvancedMacros.DEFAULT_WORKSPACE_NAME, AdvancedMacros.MACROS_FOLDER.getAbsolutePath(), true);
	
	static {
		workspaces.put(INTERNAL.name, INTERNAL);
		workspaces.put(DEFAULT.name, DEFAULT);
		INTERNAL.caseSensitiveModules = false;
	}
	
	private Workspace(String name, String path) {
		this(name, path, false);
	}
	
	private Workspace(String name, String path, boolean allPermissions) {
		super();
		this.name = name;
		this.path = path;
		permissions = new Permissions(allPermissions);
		if(!getConfigFile().exists() && !isReservedWorkspace())
			try {
				save();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	public String getName() {
		return name;
	}
	
	public String getPath() {
		return path;
	}
	
	public boolean containsFile(File file) throws IOException {
		File base = new File(path).getCanonicalFile();
        File child = file.getCanonicalFile();

        File current = child;
        while (current != null) {
            if (base.equals(current)) {
                return true;
            }
            current = current.getParentFile();
        }

        return false;
				
	}
	
	public static Workspace load(String name) throws IOException {
		return load(new File(AdvancedMacros.WORKSPACES_FOLDER, name+".json"));
	}
	
	public static Workspace load(File file) throws IOException {
		if(!file.exists())
			throw new FileNotFoundException(file.toString());
		String contents = new String(Files.readAllBytes(file.toPath()));
		var json = JsonParser.parseString(contents).getAsJsonObject();
		var name = json.get("name").getAsString();
		var path = json.get("path").getAsString();
		var workspace = new Workspace(name, path);
		workspace.permissions.load(json.get("permissions").getAsJsonArray());
		return workspace;
	}
	
	public static ArrayList<String> listWorkspaceNames() {
		var out = new ArrayList<String>();
		for(var file : AdvancedMacros.WORKSPACES_FOLDER.list()) {
			if(file.endsWith(".json"))
				out.add(file.substring(0, file.length()-5));
		}
		return out;
	}

	public void save() throws FileNotFoundException, IOException {
		if(name.equals(AdvancedMacros.DEFAULT_WORKSPACE_NAME) ||  
		   name.equals(AdvancedMacros.INTERNAL_WORKSPACE_NAME)) return;
		
		checkValid();
		File file = getConfigFile();
		JsonObject obj = new JsonObject();
		obj.add("name", new JsonPrimitive(name));
		obj.add("path", new JsonPrimitive(path));
		obj.add("permissions", permissions.toJson());
		
		try(FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(obj.toString().getBytes());
		}
	}

	private synchronized void checkValid() {
		if(name.equals(AdvancedMacros.DEFAULT_WORKSPACE_NAME) ||  
				   name.equals(AdvancedMacros.INTERNAL_WORKSPACE_NAME)) return;
		if(!valid) throw new IllegalStateException("attempt to use/modify deleted resourced");
	}
	
	public synchronized void rename(String newName) throws FileNotFoundException, IOException {
		checkValid();
		var newFile = new File(AdvancedMacros.WORKSPACES_FOLDER, newName+".json");
		
		if(newFile.exists())
			throw new IllegalStateException("Can not rename workspace '%s' to '%s', file already exists".formatted(name, newName));
		
		workspaces.remove(name);
		getConfigFile().delete();
		name = newName;
		save();
		workspaces.put(name, this);
	}
	
	public synchronized void delete() {
		if(name.equals(AdvancedMacros.DEFAULT_WORKSPACE_NAME) ||  
				   name.equals(AdvancedMacros.INTERNAL_WORKSPACE_NAME)) throw new IllegalStateException("Attempt to delete required workspace");
		checkValid();
		synchronized (workspaces) {
			workspaces.remove(name);
			getConfigFile().delete();
			valid = false;
		}
	}
	
	public boolean isReservedWorkspace() {
		return name.equals(AdvancedMacros.DEFAULT_WORKSPACE_NAME) ||
				name.equals(AdvancedMacros.INTERNAL_WORKSPACE_NAME);
	}
	
	public synchronized File getConfigFile() {
		checkValid();
		return new File(AdvancedMacros.WORKSPACES_FOLDER, name+".json");
	}
	
	public synchronized LuaValue toLuaValue() {
		LuaTable controls = new LuaTable();
		controls.set("getName", new ZeroArgFunction() {
			@Override public LuaValue call() {
				if(!valid) throw new LuaError("Attempt to use/modify deleted workspace");
				return valueOf(Workspace.this.name);
			}
		});
		controls.set("getPath", new ZeroArgFunction() {
			@Override public LuaValue call() {
				if(!valid) throw new LuaError("Attempt to use/modify deleted workspace");
				return valueOf(path);
			}
		});
		controls.set("getPermissions", new ZeroArgFunction() {
			@Override public LuaValue call() {
				if(!valid) throw new LuaError("Attempt to use/modify deleted workspace");
				return permissions.toLuaTable();
			}
		});
		controls.set("hasPermission", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				if(args.narg() == 0)
					throw new LuaError("Missing args String, <String>");
				if(args.narg() == 1)
					return LuaValue.valueOf(permissions.hasPermission(args.checkjstring(1)));
				return LuaValue.valueOf(permissions.hasPermission(args.checkjstring(1), args.checkjstring(2)));
			}
		});
		controls.set("rename", new OneArgFunction() {
			@Override public LuaValue call(LuaValue arg) {
				if(!valid) throw new LuaError("Attempt to use/modify deleted workspace");
				if(isReservedWorkspace()) throw new LuaError("Attempt to modify reserved workspace");
				Permissions.check(Permission.MODIFY_WORKSPACE);
				
				try {
					rename(arg.checkjstring());
				} catch(IllegalStateException | IOException e) {
					throw new LuaError(e.getMessage());
				}
				return NONE;
			}
		});
		controls.set("delete", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				if(isReservedWorkspace()) throw new LuaError("Attempt to delete reserved workspace");
				Permissions.check(Permission.MODIFY_WORKSPACE);
				if(valid)
					delete();
				return NONE;
			}
		});
		controls.set("grant", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				var key = arg.checkjstring();
				if(isReservedWorkspace())
					throw new LuaError("Attempt to modify permissions of reserved workspace '%s'".formatted(name));
				Permissions.check(Permission.MODIFY_PERMISSIONS);
				Permissions.check(key);
				permissions.grant(key);
				try {
					save();
				} catch (IOException e) {
					e.printStackTrace(); //TODO Save failed err
				}
				return NONE;
			}
		});
		controls.set("revoke", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				var key = arg.checkjstring();
				if(isReservedWorkspace())
					throw new LuaError("Attempt to modify permissions of reserved workspace '%s'".formatted(name));
				Permissions.check(Permission.MODIFY_PERMISSIONS);
				permissions.revoke(key);
				try {
					save();
				} catch (IOException e) {
					e.printStackTrace(); //TODO Save failed err
				}
				return NONE;
			}
		});
		controls.set("isReserved", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return valueOf(isReservedWorkspace());
			}
		});
		controls.set("wrap", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				Permissions.check(Permission.RUN_WORKSPACE, name);
				return new WrappedTask(arg, Workspace.this);
			}
		});
		controls.set("setModuleCaseSensitivity", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				if(isReservedWorkspace())
					throw new LuaError("attempt to modify case sensitivity of reserved workspace '%s'".formatted(name));
				caseSensitiveModules = arg.checkboolean();
				return NONE;
			}
		});
		controls.set("isModuleCaseSensitive", new ZeroArgFunction() { //added as a helper for development. workspace can be set to the repo's resource folder so reloading isn't needed
			@Override
			public LuaValue call() {
				return valueOf(caseSensitiveModules);
			}
		});
		
		return AdvancedMacros.workspaceLuaClass.get("new").call(AdvancedMacros.workspaceLuaClass, controls);
		
		
	}
	
	public void addDefaultPermissions() {
		var add = new Permission[] {
			Permission.NARRATE,
			Permission.TOAST_NOTIFICATION,
			Permission.TOAST_ACTION_BAR,
			Permission.TOAST_TITLE,
		};
		var currentWorkspace = Utils.currentWorkspace();
		for(var p : add) {
			if(currentWorkspace == null || currentWorkspace.permissions.hasPermission(p))
				permissions.grant(p);
		}
	}
	
	public static Workspace get(String name) {
		return workspaces.get(name);
	}
	
	public static Stream<Workspace> workspacesWithFile(File file) {
		return workspaces.values().stream().filter(w->{
			try {
				return w.containsFile(file);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		});
	} 
	
	public static class CreateWorkspace extends TwoArgFunction {
		@Override
		public LuaValue call(LuaValue name, LuaValue file) {
			Permissions.check(Permission.MODIFY_WORKSPACE);
			String path;
			if(file.isstring())
				path = file.checkjstring();
			else if(LuaClassUtils.instanceOf(file, "File"))
				path = file.get("getPath").call(file).checkjstring();
			else
				throw new LuaError("Expected arg 2 to be string or class:File");
			
			try {
				path = new File(path).getCanonicalPath();
			} catch (IOException e) {
				throw new LuaError("IOException: "+e.getMessage());
			}
			
			var jName = name.checkjstring();
			
			synchronized (workspaces) {
				if(workspaces.containsKey(jName))
					throw new LuaError("Workspace with name '%s' already exists".formatted(jName));
				var workspace = new Workspace(jName, path);
				workspaces.put(jName, workspace);
				workspace.addDefaultPermissions();
				try {
					workspace.save();
				} catch (IOException e) {
					throw new LuaError("IOException: "+e.getMessage());
				}
			}
			return NONE;
		}
	}
	
	public static class GetCurrentWorkspace extends ZeroArgFunction {
    	@Override
    	public LuaValue call() {
    		return Utils.currentWorkspace().toLuaValue();
    	}
    }
	
	public static class ListWorkspaces extends ZeroArgFunction {
		@Override
		public LuaValue call() {
			LuaTable tbl = new LuaTable();
			
			for(var w : workspaces.entrySet())
				tbl.set(w.getKey(), w.getValue().toLuaValue());
			
			return tbl;
		}
	}
	
	public static class GetWorkspace extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue arg) {
			var workspace = workspaces.get(arg.checkjstring());
			if(workspace != null)
				return workspace.toLuaValue();
			return NIL;
		}
	}
	
	/**
	 * Wraps a function so when it's called it always runs in the workspace the wrapper was run from 
	 * */
	public static class WrapTask extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue arg) {
			return new WrappedTask(arg);
		}
	}
	
	public static class WrappedTask extends VarArgFunction {
			private final Workspace workspace;
			private final LuaValue callable;
			
			public WrappedTask(LuaValue callable) {
				this(callable, Utils.currentWorkspace());
			}
			
			public WrappedTask(LuaValue callable, Workspace workspace) {
				this.workspace = workspace;
				this.callable = callable;
			}
			
			@Override
			public Varargs invoke(Varargs args) {
				try(var reset = Utils.tempSetCurrentWorkspace(workspace)) {
					return callable.invoke(args);
				}
			}
		}
}
