package com.theincgi.advancedMacros.lua.functions;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.google.common.io.Files;
import com.theincgi.advancedMacros.AdvancedMacros;

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
	}

	private static class Open extends TwoArgFunction{
		@Override
		public LuaValue call(LuaValue arg0, LuaValue arg1) {
			LuaTable controls;
			String mode = arg1.checkjstring();
			assertAddress(arg0);
			if(arg0.tojstring().isEmpty()) {throw new LuaError("No File name given");}
			File file = new File(AdvancedMacros.macrosRootFolder, arg0.checkjstring());

			if(mode.equals("r")) {
				try {
					FileInputStream fis = new FileInputStream(file);
					Object syncLock = new Object();
					controls = new LuaTable();
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
					controls = new LuaTable();
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
		public Close(Object syncLock, Closeable...closeables) {
			this.syncLock = syncLock;
			this.closeables = closeables;
		}
		@Override
		public LuaValue call() {
			synchronized(syncLock) {
			for(int i = 0; i<closeables.length; i++) {
				try {
					if(closeables[i] instanceof Flushable) {
						((Flushable)closeables[i]).flush();
					}
					closeables[i].close();
				}catch (Exception e) {

				}
			}
			}
			return LuaValue.NONE;
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
			assertAddress(arg0);
			return LuaValue.valueOf(new File(AdvancedMacros.macrosRootFolder, arg0.checkjstring()).exists());
		}
	}
	private static class Copy extends TwoArgFunction{
		@Override
		public LuaValue call(LuaValue arg0, LuaValue arg1) {
			assertAddress(arg0);
			assertAddress(arg1);
			if(arg0.eq(arg1).checkboolean()){
				throw new LuaError("Source and destination can not be the same.");
			}
			File from = new File(AdvancedMacros.macrosRootFolder, arg0.checkjstring());
			File to  = new File(AdvancedMacros.macrosRootFolder, arg1.checkjstring());
			try {
				Files.copy(from, to);
				return LuaValue.TRUE;
			} catch (IOException e) {
				throw new LuaError("IOExeption occurred trying to copy ("+e.getMessage()+")");
			}
		}
	}
	private static class Delete extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg0) {
			assertAddress(arg0);
			return LuaValue.valueOf(new File(AdvancedMacros.macrosRootFolder, arg0.checkjstring()).delete());
		}
	}
	private static class Rename extends TwoArgFunction{
		@Override
		public LuaValue call(LuaValue arg0, LuaValue arg1) {
			assertAddress(arg0);
			assertAddress(arg1);
			return LuaValue.valueOf(new File(AdvancedMacros.macrosRootFolder, arg0.checkjstring()).renameTo(new File(AdvancedMacros.macrosRootFolder, arg1.checkjstring())));
		}
	}
	private static class MkDir extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg0) {
			assertAddress(arg0);
			File f = new File(AdvancedMacros.macrosRootFolder, arg0.checkjstring());
			return LuaValue.valueOf(f.mkdir());
		}
	}
	private static class MkDirs extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg0) {
			assertAddress(arg0);
			File f = new File(AdvancedMacros.macrosRootFolder, arg0.checkjstring());
			return LuaValue.valueOf(f.mkdirs());
		}
	}
	private static class IsDir extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg0) {
			assertAddress(arg0);
			return LuaValue.valueOf(new File(AdvancedMacros.macrosRootFolder, arg0.checkjstring()).isDirectory());
		}
	}//TODO Check list("") or list()
	private static class List extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg0) {
			if(arg0.isnil()) {
				arg0 = LuaValue.valueOf("");
			}
			assertAddress(arg0);
			LuaTable t = new LuaTable();
			File f = new File(AdvancedMacros.macrosRootFolder, arg0.checkjstring());
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
	private static void assertAddress(LuaValue arg) {
		if(!isValidAddress(arg.checkjstring())){
			throw new LuaError("File may not be accessed, move/copy into the macros folder to use this file.");
		}
		return;
	}
	private static boolean isValidAddress(String path) {
		try {
			File f = new File(AdvancedMacros.macrosRootFolder,path);
			return f.getCanonicalPath().startsWith(AdvancedMacros.macrosRootFolder.getCanonicalPath());
		} catch (IOException e) {
			return false;
		}
	}
}