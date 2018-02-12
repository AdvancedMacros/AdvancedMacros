package com.theincgi.advancedMacros.lua.functions;

import java.io.IOException;
import java.io.RandomAccessFile;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

public class RAFFuncs {
	private RandomAccessFile raf;
	private LuaTable controls = new LuaTable();
	public RAFFuncs(RandomAccessFile raf) {
		this.raf = raf;
		controls.set("seek", 		new Seek());
		controls.set("getLength", 	new GetLength());
		controls.set("setLength", 	new SetLength());
		controls.set("pos", 		new GetPos());
		
		controls.set("readByte", 	new ReadByte());
		controls.set("readUByte", 	new ReadUByte());
		controls.set("readChar", 	new ReadChar());
		controls.set("readUTF", 	new ReadUTF());
		controls.set("readBoolean", new ReadBoolean());
		controls.set("readDouble", 	new ReadDouble());
		controls.set("readFloat", 	new ReadFloat());
		controls.set("readLong", 	new ReadLong());
		controls.set("readShort", 	new ReadShort());
		controls.set("readUShort", 	new ReadUShort());
		controls.set("readLine",    new ReadLine());
		controls.set("readInt", 	new ReadInt());
		controls.set("writeString", new WriteString());
		controls.set("writeBoolean",new WriteBoolean());
		controls.set("writeByte", 	new WriteByte());
		controls.set("writeDouble", new WriteDouble());
		controls.set("writeFloat", 	new WriteFloat());
		controls.set("writeInt", 	new WriteInt());
		controls.set("writeLong", 	new WriteLong());
		controls.set("writeShort", 	new WriteShort());
		controls.set("writeUTF", 	new WriteUTF());
		controls.set("force", 		new Force());//channel
		//controls.set("map", 		new Map());//channel
		controls.set("skip", 		new Skip());
		controls.set("close", 		new Close());
	}
	public LuaTable getControls() {
		return controls;
	}
	
	private class Seek extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg0) {
			try {
				raf.seek(arg0.checklong());
			} catch (IOException e) {
				throw new LuaError("IOException: ("+e.getMessage()+")");
			}
			return LuaValue.NONE;
		}
	}
	private class GetLength extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			try {
				return LuaValue.valueOf(raf.length());
			} catch (IOException e) {
				throw new LuaError("IOException: ("+e.getMessage()+")");
			}
		}
	}
	private class SetLength extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg0) {
			try {
				raf.setLength(arg0.checklong());
			} catch (IOException e) {
				throw new LuaError("IOException: ("+e.getMessage()+")");
			}
			return LuaValue.NONE;
		}
	}
	private class GetPos extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			try {
				return LuaValue.valueOf(raf.getFilePointer());
			} catch (IOException e) {
				throw new LuaError("IOException: ("+e.getMessage()+")");
			}
		}
	}
	
	private class ReadByte extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			try {
				return LuaValue.valueOf(raf.readByte());
			} catch (IOException e) {
				throw new LuaError("IOException: ("+e.getMessage()+")");
			}
		}
	}
	private class ReadInt extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			try {
				return LuaValue.valueOf(raf.readInt());
			} catch (IOException e) {
				throw new LuaError("IOException: ("+e.getMessage()+")");
			}
		}
	}
	private class ReadBoolean extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			try {
				return LuaValue.valueOf(raf.readBoolean());
			} catch (IOException e) {
				throw new LuaError("IOException: ("+e.getMessage()+")");
			}
		}
	}
	private class ReadDouble extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			try {
				return LuaValue.valueOf(raf.readDouble());
			} catch (IOException e) {
				throw new LuaError("IOException: ("+e.getMessage()+")");
			}
		}
	}
	private class ReadLine extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			try {
				return LuaValue.valueOf(raf.readLine());
			} catch (IOException e) {
				throw new LuaError("IOException: ("+e.getMessage()+")");
			}
		}
	}
	private class ReadUTF extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			try {
				return LuaValue.valueOf(raf.readUTF());
			} catch (IOException e) {
				throw new LuaError("IOException: ("+e.getMessage()+")");
			}
		}
	}
	private class ReadUByte extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			try {
				return LuaValue.valueOf(raf.readUnsignedByte());
			} catch (IOException e) {
				throw new LuaError("IOException: ("+e.getMessage()+")");
			}
		}
	}
	private class ReadShort extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			try {
				return LuaValue.valueOf(raf.readShort());
			} catch (IOException e) {
				throw new LuaError("IOException: ("+e.getMessage()+")");
			}
		}
	}
	private class ReadUShort extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			try {
				return LuaValue.valueOf(raf.readUnsignedShort());
			} catch (IOException e) {
				throw new LuaError("IOException: ("+e.getMessage()+")");
			}
		}
	}
	private class ReadFloat extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			try {
				return LuaValue.valueOf(raf.readFloat());
			} catch (IOException e) {
				throw new LuaError("IOException: ("+e.getMessage()+")");
			}
		}
	}
	private class ReadChar extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			try {
				return LuaValue.valueOf(raf.readChar());
			} catch (IOException e) {
				throw new LuaError("IOException: ("+e.getMessage()+")");
			}
		}
	}
	private class ReadLong extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			try {
				return LuaValue.valueOf(raf.readLong());
			} catch (IOException e) {
				throw new LuaError("IOException: ("+e.getMessage()+")");
			}
		}
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//~~~~~~~~~            Write Section            ~~~~~~~~~~~~~~~~~~~
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	private class WriteString extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg0) {
			try{
				raf.writeBytes(arg0.checkjstring());
			}catch(IOException e) {throw new LuaError("IOException: ("+e.getMessage()+")");}
			return LuaValue.NONE;
		}
	}
	private class WriteBoolean extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg0) {
			try{
				raf.writeBoolean(arg0.checkboolean());
			}catch(IOException e) {throw new LuaError("IOException: ("+e.getMessage()+")");}
			return LuaValue.NONE;
		}
	}
	private class WriteByte extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg0) {
			try{
				raf.writeByte(arg0.checkint());
			}catch(IOException e) {throw new LuaError("IOException: ("+e.getMessage()+")");}
			return LuaValue.NONE;
		}
	}
	
	private class WriteDouble extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg0) {
			try{
				raf.writeDouble(arg0.checkdouble());
			}catch(IOException e) {throw new LuaError("IOException: ("+e.getMessage()+")");}
			return LuaValue.NONE;
		}
	}
	private class WriteFloat extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg0) {
			try{
				raf.writeFloat((float) arg0.checkdouble());
			}catch(IOException e) {throw new LuaError("IOException: ("+e.getMessage()+")");}
			return LuaValue.NONE;
		}
	}
	private class WriteInt extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg0) {
			try{
				raf.writeInt(arg0.checkint());
			}catch(IOException e) {throw new LuaError("IOException: ("+e.getMessage()+")");}
			return LuaValue.NONE;
		}
	}private class WriteLong extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg0) {
			try{
				raf.writeLong(arg0.checklong());
			}catch(IOException e) {throw new LuaError("IOException: ("+e.getMessage()+")");}
			return LuaValue.NONE;
		}
	}
	private class WriteShort extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg0) {
			try{
				raf.writeShort(arg0.checkint());
			}catch(IOException e) {throw new LuaError("IOException: ("+e.getMessage()+")");}
			return LuaValue.NONE;
		}
	}
	private class WriteUTF extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg0) {
			try{
				raf.writeUTF(arg0.checkjstring());
			}catch(IOException e) {throw new LuaError("IOException: ("+e.getMessage()+")");}
			return LuaValue.NONE;
		}
	}
	
	
	private class Force extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg0) {
			try {
				raf.getChannel().force(arg0.checkboolean());
			} catch (IOException e) {
				throw new LuaError("IOException: ("+e.getMessage()+")");
			}
			return LuaValue.NONE;
		}
	}
	
	private class Skip extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg0) {
			try {
				return LuaValue.valueOf(raf.skipBytes(arg0.checkint()));
			} catch (IOException e) {
				throw new LuaError("IOException: ("+e.getMessage()+")");
			}
			//return LuaValue.NONE;
		}
	}
	private class Close extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			try {
				raf.close();
			} catch (IOException e) {
				throw new LuaError("IOException: ("+e.getMessage()+")");
			}
			return LuaValue.NONE;
		}
	}
	
	
}