package com.theincgi.advancedMacros.lua.functions;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.lua.LuaDebug;
import com.theincgi.advancedMacros.lua.LuaFunctions.Log;
import com.theincgi.advancedMacros.misc.Utils;

public class FileSystem extends LuaTable{
	public FileSystem() {
		set("open", new Open());
		set("exists", new Exists());
		set("copy", new Copy());
		set("delete", new Delete());
		set("rename", new Rename());
		set("mkDir", new MkDir());
		set("mkDirs", new MkDirs());
		set("isDir", new IsDir());
		set("list", new List());
		set("getMacrosAddress", new ZeroArgFunction() {
			@Override public LuaValue call() {
				return LuaValue.valueOf(AdvancedMacros.macrosFolder.toString());
			}
		});
	}

	private static class Open extends TwoArgFunction{
		@Override
		public LuaValue call(LuaValue arg0, LuaValue arg1) {
			LuaTable controls = new ClosingLuaTable("file");
			String mode = arg1.checkjstring();
			//assertAddress(arg0);

			File file = Utils.parseFileLocation(arg0);

			if(!mode.equals("r")) {
				file.getParentFile().mkdirs();
			}

			if(mode.equals("r")) {
				try {
					FileInputStream fis = new FileInputStream(file);
					Object syncLock = new Object();
					controls.set("readByte", new ReadByte(fis, syncLock));
					controls.set("readLine", new Read(fis, syncLock, true));
					controls.set("read", new Read(fis, syncLock, false));
					controls.set("readAll", new ReadAll(syncLock, fis));
					controls.set("available", new Available(syncLock, fis));
					controls.set("close", new Close(syncLock, fis));
				} catch (FileNotFoundException e) {
					throw new LuaError("FileNotFoundException: ("+e.getMessage()+")");
				}
			}else if(mode.equals("raf")){
				try {
					RandomAccessFile raf = new RandomAccessFile(file, "rw");
					RAFFuncs rafFuncs = new RAFFuncs(raf);
					controls = rafFuncs.getControls();
				} catch (FileNotFoundException e) {
					throw new LuaError("FileNotFoundException: ("+e.getMessage()+")");
				}
			}else if(mode.equals("w")||mode.equals("a")) {
				try {
					FileOutputStream fos = new FileOutputStream(file, mode.equals("a"));
					Object syncLock = new Object();
					controls.set("write",     new Write(fos,syncLock));    //"Blah"
					controls.set("writeLine", new WriteLine(fos,syncLock));//"Blah"+\n
					controls.set("writeByte", new WriteByte(fos,syncLock));//0b11000101
					controls.set("close", new Close(syncLock, fos));
				} catch (FileNotFoundException e) {
					throw new LuaError("FileNotFoundException: ("+e.getMessage()+")");
				}
			}else {
				throw new LuaError("Unsupported mode '"+mode+"'. Try 'r', 'w', 'a' or 'raf'");
			}
			return controls;

		}

	}
	//TODO move syncLock to the open class so we dont keep so many unnecessary copies


	public static class ClosingLuaTable extends LuaTable{
		String traceback;
		String objName;
		public ClosingLuaTable(String objectName) {
			super();
			try {
			objName = objectName;
//			LuaValue v = AdvancedMacros.globals.debuglib.get("getinfo").call(valueOf(1), valueOf("Sl")); 
			LuaValue v = Utils.getDebugStacktrace();
			int line = -1;
			String name = "?";
			if(!v.isnil()) {
				line = v.get("currentline").toint();
				name = v.get("short_src").tojstring();
			}
			traceback = String.format("[%s]:%s", name, line==-1?"?":String.valueOf(line));
			}catch (Throwable e) {
				System.err.println("Notice:");
				e.printStackTrace();
				traceback = "[?]:?";
			}
		}

		public ClosingLuaTable(int narray, int nhash) {
			super(narray, nhash);
		}

		public ClosingLuaTable(LuaValue[] named, LuaValue[] unnamed, Varargs lastarg) {
			super(named, unnamed, lastarg);
		}

		public ClosingLuaTable(Varargs varargs, int firstarg) {
			super(varargs, firstarg);
		}

		public ClosingLuaTable(Varargs varargs) {
			super(varargs);
		}

		@Override
		protected void finalize() throws Throwable {
			LuaValue v = this.get("close");
			if(v instanceof Close) {
				Close c = (Close) v;
				if(!c.hasClosed && this.get("close").isfunction()) {
					AdvancedMacros.logFunc.call(LuaValue.valueOf("&6Warning: "+objName+" was not closed in '"+traceback+"' &7closing now..."));
					c.close();
				}

			}
			super.finalize();
		}
	}

	private static class ReadAll extends ZeroArgFunction{
		Object syncLock;
		FileInputStream fis;
		public ReadAll(Object syncLock, FileInputStream fis) {
			super();
			this.syncLock = syncLock;
			this.fis = fis;
		}
		@Override
		public LuaValue call() {
			synchronized (syncLock) {
				try {
					int avail=fis.available();
					byte[] b = new byte[avail];
					fis.read(b);
					return LuaValue.valueOf(new String(b));
				} catch (IOException e) {
					throw new LuaError("IOExeception: ("+e.getMessage()+")");
				}
			}
		}
	}

	private static class Read extends ZeroArgFunction{
		FileInputStream fis;
		Object syncLock;
		boolean lineMode;
		public Read(FileInputStream fis, Object syncLock, boolean isLineMode) {
			super();
			this.fis = fis;
			this.syncLock = syncLock;
			lineMode = isLineMode;
		}
		@Override
		public LuaValue call() {
			String s = "";
			synchronized (syncLock) {
				try {
					char sChar = ' ';
					s += readDelims();
					while(fis.available()>0) {
						sChar = (char) fis.read();
						if(isDelim(sChar)) {
							break;
						}
						s+=sChar;
					}
					return LuaValue.valueOf(s);
				} catch (IOException e) {
					throw new LuaError("IOExeception: ("+e.getMessage()+")");
				}
			}
		}
		private boolean isDelim(char sChar) {
			if(lineMode)
				return sChar=='\n'||sChar==10||sChar==13;
			return sChar==' '||sChar=='\n'||sChar==10||sChar==13;
		}
		private char readDelims() throws IOException {
			char sChar;
			do {
				sChar = (char) fis.read();
			} while (isDelim(sChar));
			return sChar;
		}
	}

	private static class ReadByte extends ZeroArgFunction{
		FileInputStream fis;
		Object syncLock;
		public ReadByte(FileInputStream fis, Object syncLock) {
			super();
			this.fis = fis;
			this.syncLock = syncLock;
		}
		@Override
		public LuaValue call() {
			synchronized (syncLock) {
				try {
					return LuaValue.valueOf(fis.read());
				} catch (IOException e) {
					throw new LuaError("IOExeception: ("+e.getMessage()+")");
				}
			}
		}
	}

	private static class Available extends ZeroArgFunction{
		private Object syncLock;
		private FileInputStream fis;
		public Available(Object syncLock, FileInputStream fis) {
			super();
			this.syncLock = syncLock;
			this.fis = fis;
		}
		@Override
		public LuaValue call() {
			synchronized (syncLock) {

				try {
					return LuaValue.valueOf(fis.available());
				} catch (IOException e) {
					throw new LuaError("IOExeception: ("+e.getMessage()+")");
				}

			}
		}
	}


	private static class Close extends ZeroArgFunction{
		private Object syncLock;
		private Closeable[] closeables;
		protected boolean hasClosed = false;
		public Close(Object syncLock, Closeable...closeables) {
			this.syncLock = syncLock;
			this.closeables = closeables;
		}
		@Override
		public LuaValue call() {

			close();

			return LuaValue.NONE;
		}
		public void close() {
			synchronized(syncLock) {
				for(int i = 0; i<closeables.length; i++) {
					try {
						if(closeables[i] instanceof Flushable) {
							((Flushable)closeables[i]).flush();
						}
						closeables[i].close();
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
				hasClosed = true;
			}
		}
	}

	private static class Write extends OneArgFunction{
		private FileOutputStream fos;
		private Object syncLock;

		public Write(FileOutputStream fos, Object syncLock) {
			super();
			this.fos = fos;
			this.syncLock = syncLock;
		}

		@Override
		public LuaValue call(LuaValue arg0) {
			synchronized(syncLock) {
				try {
					fos.write(arg0.tojstring().getBytes());
				} catch (IOException e) {
					throw new LuaError("IOExeception: ("+e.getMessage()+")");
				}
			}
			return LuaValue.NONE;
		}
	}
	private static class WriteLine extends OneArgFunction{
		private FileOutputStream fos;
		private Object syncLock;

		public WriteLine(FileOutputStream fos, Object syncLock) {
			super();
			this.fos = fos;
			this.syncLock = syncLock;
		}

		@Override
		public LuaValue call(LuaValue arg0) {
			synchronized(syncLock) {
				try {
					fos.write((arg0.tojstring()+"\n").getBytes());
				} catch (IOException e) {
					throw new LuaError("IOExeception: ("+e.getMessage()+")");
				}
			}
			return LuaValue.NONE;
		}
	}
	private static class WriteByte extends OneArgFunction{
		private FileOutputStream fos;
		private Object syncLock;


		public WriteByte(FileOutputStream fos, Object syncLock) {
			super();
			this.fos = fos;
			this.syncLock = syncLock;
		}


		@Override
		public LuaValue call(LuaValue arg0) {
			try {
				synchronized(syncLock) {
					fos.write(arg0.checkint());
				}
			} catch (IOException e) {
				throw new LuaError("IOExeception: ("+e.getMessage()+")");
			}
			return LuaValue.NONE;
		}
	}

	private static class Exists extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg0) {
			File f = Utils.parseFileLocation(arg0);
			return LuaValue.valueOf( f.exists() );
		}
	}
	private static class Copy extends TwoArgFunction{
		@Override
		public LuaValue call(LuaValue arg0, LuaValue arg1) {
			File from = Utils.parseFileLocation(arg0);
			File to   = Utils.parseFileLocation(arg1);

			try {
				if(Files.isSameFile(from.toPath(), to.toPath())){
					throw new LuaError("Source and destination can not be the same.");
				}

				Files.copy(from.toPath(), to.toPath());
				return LuaValue.TRUE;
			} catch (IOException e) {
				throw new LuaError("IOExeption occurred trying to copy ("+e.getMessage()+")");
			}
		}
	}
	private static class Delete extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg0) {
			File file = Utils.parseFileLocation(arg0);
			return LuaValue.valueOf( file.delete() );
		}
	}
	private static class Rename extends TwoArgFunction{
		@Override
		public LuaValue call(LuaValue arg0, LuaValue arg1) {
			File from = Utils.parseFileLocation(arg0);
			File to   = Utils.parseFileLocation(arg1);
			return LuaValue.valueOf(from.renameTo(to));
		}
	}
	private static class MkDir extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg0) {
			File f = Utils.parseFileLocation(arg0);
			return LuaValue.valueOf(f.mkdir());
		}
	}
	private static class MkDirs extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg0) {
			File f = Utils.parseFileLocation(arg0);
			return LuaValue.valueOf(f.mkdirs());
		}
	}
	private static class IsDir extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg0) {
			File f = Utils.parseFileLocation(arg0);
			return LuaValue.valueOf(f.isDirectory());
		}
	}//TODO Check list("") or list()
	private static class List extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg0) {
			if(arg0.isnil()) {
				arg0 = LuaValue.valueOf("");
			}
			LuaTable t = new LuaTable();
			File f = Utils.parseFileLocation(arg0);
			if(!f.isDirectory()){
				return LuaValue.FALSE;
			}
			int i = 1;
			for(String s : f.list()) {
				t.set(i++, s);
			}
			return t;
		}
	}

	
	//	private static void assertAddress(LuaValue arg) {
	//		if(!isValidAddress(arg.checkjstring())){
	//			throw new LuaError("File may not be accessed, move/copy into the advanced macros folder to use this file.");
	//		}
	//		return;
	//	}
	//	/**
	//	 * @param path the path inside the root folder of {@link AdvancedMacros}*/
	//	public static boolean isValidAddress(String path) {
	//		try {
	//			File f = new File(AdvancedMacros.macrosRootFolder,path);
	//			return f.getCanonicalPath().startsWith(AdvancedMacros.macrosRootFolder.getCanonicalPath());
	//		} catch (IOException e) {
	//			return false;
	//		}
	//	}
}