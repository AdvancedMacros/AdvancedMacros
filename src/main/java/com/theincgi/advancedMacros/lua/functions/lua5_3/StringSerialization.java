package com.theincgi.advancedMacros.lua.functions.lua5_3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.StringJoiner;

import org.luaj.vm2_v3_0_1.Globals;
import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaString;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.ThreeArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import com.google.common.primitives.UnsignedInteger;

import io.netty.buffer.ByteBuf;

public class StringSerialization {
	private StringSerialization() {}

	/**@param t Lua table to load method into, should be string table*/
	public static void load(LuaTable t) {
		t.set("pack", new StringPack());
		t.set("unpack", new StringUnpack());
	}

	public static class StringPack extends VarArgFunction {
		@Override
		public Varargs invoke(Varargs args) {
			String pattern = args.checkjstring(1);
			//StringBuilder out = new StringBuilder();
			ByteArrayStack out = new ByteArrayStack();
			long sizeArg = 0; //long so it can fit unsigned int data correctly
			int argNum = 2;

			int maxAlignment = 1;

			ByteBuffer temp = ByteBuffer.allocate(16);
			for(int i = 0; i<pattern.length(); i++) {
				char c = pattern.charAt(i);
				PackFormat format = PackFormat.fromChar(c);
				if(format.acceptsNumberArg()) {
					sizeArg = 0;
					for(int u = i+1; u<pattern.length(); u++, i++) {
						if(Character.isDigit(pattern.charAt(u)))
							sizeArg = (sizeArg*10) + pattern.charAt(u)-'0';
						else
							break;
					}
					if(sizeArg > format.maxNumArgValue()) {
						throw new LuaError("integer size ("+sizeArg+") out of limits [1,16]");
					}
					if(sizeArg > Math.pow(2, 32))
						throw new LuaError("integer overflow ("+sizeArg+") out of limits [1, 4294967296]");
					if(sizeArg <= 0)
						throw new LuaError("integer size must be greater than 1");
				}

				switch (format) {

				case NATIVE_ENDIAN:
					temp.order(ByteOrder.nativeOrder());
					break;
				case BIG_ENDIAN:
					temp.order(ByteOrder.BIG_ENDIAN);
					break;
				case LITTLE_ENDIAN:
					temp.order(ByteOrder.LITTLE_ENDIAN);
					break;

				case DOUBLE:{
					if(!args.arg(argNum).isnumber())
						throwArgErr("pack", argNum, "number expected got "+args.arg(argNum).typename(), c);
					temp.putDouble(args.checkdouble(argNum++));
					append(out, temp, Double.BYTES);
					break;
				}
				case FLOAT:
					if(!args.arg(argNum).isnumber())
						throwArgErr("pack", argNum, "number expected, got "+args.arg(argNum).typename(), c);
					temp.putFloat((float) args.checkdouble(argNum++));
					append(out, temp, Float.BYTES);
					break;
				case FIXED_SIZE_STRING:{
					String str = args.checkjstring(argNum++);
					if(sizeArg>Integer.MAX_VALUE)
						throwArgErr("pack", argNum-1, "oversized string", c);
					if(str.length() > sizeArg)
						throwArgErr("pack", argNum-1, "string longer than given size", c);
					out.append(str);
					out.appendZeros((int) (sizeArg-str.length()));
					//for(int i2 = str.length(); i2<sizeArg; i2++)

					break;
				}
				case LENGTH_LABELD_STRING:{
					String str = args.arg(argNum++).checkjstring();
					temp.putInt(str.length());
					append(out, temp, Integer.BYTES);
					out.append(str);
					break;
				}
				case LUA_INTEGER: //should be long
				case LUA_NUMBER:  //should be double
				case LUA_UNSIGNED://...
					throwArgErr("pack", argNum, "Unimplemented", c);
				case MAXIMUM_ALIGNMENT_TO_N:
					maxAlignment = (int) sizeArg;
					break;
				case ONE_BYTE_PADDING:
					out.add(new byte[1]);
					break;
				case SIGNED_BYTE:{
					LuaValue arg = args.arg(argNum++);
					if(!arg.isnumber())
						throwArgErr("pack", argNum-1, "number expected, got "+arg.typename(), c);
					if(!arg.isint())
						throwArgErr("pack", argNum-1, "number has no integer representation", c);
					int value = arg.checkint();
					if(value > 128 || value < -127)
						throwArgErr("pack", argNum-1, "integer overflow", c);
					out.add(new byte[] {(byte) (value&0xFF)}); //tested: (int)((((char)(-45 & 0xFF))+"").charAt(0)) == 211, can be converted back at unpack, bytes are correct
					break;
				}
				case UNSIGNED_BYTE:{
					LuaValue arg = args.arg(argNum++);
					if(!arg.isnumber())
						throwArgErr("pack", argNum-1, "number expected, got "+arg.typename(), c);
					if(!arg.isint())
						throwArgErr("pack", argNum-1, "number has no integer representation", c);
					int value = arg.checkint();
					if(value > 255 || value < 0)
						throwArgErr("pack", argNum-1, "integer overflow", c);
					out.add(new byte[] {(byte) (value&0xFF)});

					break;
				}
				case SIZE_T:
					sizeArg = Integer.BYTES;
				case UNSIGNED_INT_WITH_N_BYTES:{
					LuaValue arg = args.arg(argNum++);
					if(!arg.isnumber())
						throwArgErr("pack", argNum-1, "number expected, got "+arg.typename(), c);
					if(!arg.islong())
						throwArgErr("pack", argNum-1, "number has no integer representation", c);
					long l = arg.checklong();
					double max = Math.pow(2, sizeArg*8);
					double min = 0;
					if(l > max || l < min) 
						throwArgErr("pack", argNum-1, "integer overflow", c);
					temp.putLong(l);
					append(out, temp, (int) sizeArg);
					break;
				}
				case SIGNED_INT_WITH_N_BYTES:{ //capped at 8 bytes of info, rest is padding, padding included in number (endian, pading first/last)
					LuaValue arg = args.arg(argNum++);
					if(!arg.isnumber())
						throwArgErr("pack", argNum-1, "number expected, got "+arg.typename(), c);
					if(!arg.islong())
						throwArgErr("pack", argNum-1, "number has no integer representation", c);
					long l = arg.checklong();
					double max = Math.pow(2, sizeArg*8-1);
					double min = -max+1;
					if(l > max || l < min) 
						throwArgErr("pack", argNum-1, "integer overflow", c);
					temp.putLong(l);
					append(out, temp, (int) sizeArg);
					break;
				}
				case SIGNED_LONG:{ //capped at 8 bytes of info, rest is padding, padding included in number (endian, pading first/last)
					LuaValue arg = args.arg(argNum++);
					if(!arg.isnumber())
						throwArgErr("pack", argNum-1, "number expected, got "+arg.typename(), c);
					if(!arg.islong())
						throwArgErr("pack", argNum-1, "number has no integer representation", c);
					long l = arg.checklong();
					long max = Long.MAX_VALUE;
					long min = Long.MIN_VALUE;
					if(l > max || l < min) 
						throwArgErr("pack", argNum-1, "integer overflow", c);
					temp.putLong(l);
					append(out, temp, Long.BYTES);
					break;
				}
				case SIGNED_SHORT:{
					LuaValue arg = args.arg(argNum++);
					if(!arg.isnumber())
						throwArgErr("pack", argNum-1, "number expected, got "+arg.typename(), c);
					if(!arg.islong())
						throwArgErr("pack", argNum-1, "number has no integer representation", c);
					int s = arg.checkint();
					if(s > Short.MAX_VALUE || s < Short.MIN_VALUE)
						throwArgErr("pack", argNum-1, "integer overflow", c);
					temp.putShort((short)(s&0xFFFF));
					append(out, temp, Short.BYTES);
					break;
				}
				case UNSIGNED_SHORT:{
					LuaValue arg = args.arg(argNum++);
					if(!arg.isnumber())
						throwArgErr("pack", argNum-1, "number expected, got "+arg.typename(), c);
					if(!arg.islong())
						throwArgErr("pack", argNum-1, "number has no integer representation", c);
					int s = arg.checkint();
					if(s > Math.pow(2, Short.SIZE) || s < 0)
						throwArgErr("pack", argNum, "integer overflow", c);
					temp.putShort((short)(s&0xFFFF));
					append(out, temp, Short.BYTES);
					break;
				}

				case UNSIGNED_LONG:
					throwArgErr("pack", argNum, "unsigned long unsupported", c);
					break;
				case XOP:{
					PackFormat arg = PackFormat.fromChar(pattern.charAt(++i));
					pad(out, arg.getAlignmentSize(), maxAlignment);
				}
				case ZERO_TERMINATED_STRING:{
					String temp2 = args.arg(argNum++).checkjstring();
					if(temp2.indexOf((char)(0))>=0) throwArgErr("pack", argNum-1, "String contains char 0", c);
					out.append(temp2);
					out.appendZeros(1);
				}
				case NONE:
					break;
				default:
					throw new LuaError("invalid format code '"+c+"'");
				}

			}//this is the end of the for loop itterating the pattern length
			return LuaValue.valueOf(out.asString());
		}//end of invoke
	}

	public static void throwArgErr(String method, int argNum, String reason, char pf) {
		throw new LuaError("bad argument #"+argNum+" to '"+method+"' ("+reason+") ["+pf+"]");
	}

	private static void pad(ByteArrayStack out, int alignment, int maxAlignment) {
		int m = Math.min(alignment, maxAlignment);
		if( m > 0 && ((m & (m - 1)) == 0) ) {
			int bytesNeeded = m - (out.getBytes() % m);
			out.add(new byte[bytesNeeded]);
			//			while(out.length() % m != 0)
			//				out.append((char) 0);
		}else
			throw new RuntimeException("An invalid padding was entered ("+alignment+")");
	}

	/**flips, and appends each byte as a char in builder for n bytes*/
	private static void append(ByteArrayStack out, ByteBuffer buf, int byteCount) {
		buf.flip();
		byte[] chunk = new byte[byteCount];
		out.add(chunk);
		if(buf.order().equals(ByteOrder.BIG_ENDIAN))
			buf.position(buf.limit()-byteCount);
		for(int i = 0; i<byteCount; i++)
			chunk[i] = (buf.get());
		buf.clear();
	}

	public static class StringUnpack extends VarArgFunction {

		@Override
		public Varargs invoke(Varargs args) {
			String format = args.checkjstring(1);
			byte[] str = args.checkjstring(2).getBytes();
			int startPos = args.optint(3, 1) - 1;
			ByteBuffer buffer = ByteBuffer.allocate(16);

			LuaTable output = new LuaTable();
			int nOut = 1;
			int sizeArg = 0;
			int maxAlignment = 1;

			for(int sPos = startPos, fPos = 0; fPos<format.length(); fPos++) {
				char c = format.charAt(fPos);
				PackFormat f = PackFormat.fromChar(c);

				if(f.acceptsNumberArg()) {
					sizeArg = 0;
					for(int u = fPos+1; u<format.length(); u++, fPos++) {
						if(Character.isDigit(format.charAt(u)))
							sizeArg = (sizeArg*10) + format.charAt(u)-'0';
						else
							break;
					}
					if(sizeArg > f.maxNumArgValue()) {
						throw new LuaError("integer size ("+sizeArg+") out of limits [1,16]");
					}
					if(sizeArg > Math.pow(2, 32))
						throw new LuaError("integer overflow ("+sizeArg+") out of limits [1, 4294967296]");
					if(sizeArg <= 0)
						throw new LuaError("integer size must be greater than 1");
				}


				switch (f) {
				case NATIVE_ENDIAN:
					buffer.order(ByteOrder.nativeOrder());
					break;
				case BIG_ENDIAN:
					buffer.order(ByteOrder.BIG_ENDIAN);
					break;
				case LITTLE_ENDIAN:
					buffer.order(ByteOrder.LITTLE_ENDIAN);
					break;
				case LUA_NUMBER:  //should be double
				case DOUBLE:
					buffer.clear();
					buffer.put(str, sPos, Double.BYTES);
					sPos+=Double.BYTES;
					buffer.flip();
					output.set(nOut++, LuaValue.valueOf(buffer.getDouble()));

					break;
				case FIXED_SIZE_STRING:{
					String m = new String(str, sPos, str.length-sPos).substring(0, sizeArg);
					sPos+=m.getBytes().length;
					output.set(nOut++, LuaValue.valueOf(m));
					break;
				}
				case FLOAT:
					buffer.clear();
					buffer.put(str, sPos, Float.BYTES);
					sPos+=Float.BYTES;
					buffer.flip();
					output.set(nOut++, LuaValue.valueOf(buffer.getFloat()));

					break;

				case ZERO_TERMINATED_STRING:{
					String temp = new String(str, sPos, str.length-sPos);
					temp = temp.substring(0, temp.indexOf(0));
					sPos+=temp.getBytes().length+1;
					output.set(nOut++, LuaValue.valueOf(temp));
					break;
				}
				case LENGTH_LABELD_STRING:{
					buffer.clear();
					buffer.put(str, sPos, Integer.BYTES);
					sPos+=Integer.BYTES;
					buffer.flip();
					int len = buffer.getInt();
					String m = new String(str, sPos, str.length-sPos).substring(0, len);
					sPos+=m.getBytes().length;
					output.set(nOut++, LuaValue.valueOf(m));
					break;
				}

				//				case LUA_INTEGER:
				//					buffer.clear();
				//					buffer.put(str, sPos, Integer.BYTES);
				//					sPos+=Integer.BYTES;
				//					buffer.flip();
				//					output.set(nOut++, LuaValue.valueOf(buffer.getInt()));
				//					break;


				case LUA_UNSIGNED://...
					break;
				case MAXIMUM_ALIGNMENT_TO_N:
					maxAlignment = sizeArg;
					break;
				case NONE:
					break;
				case ONE_BYTE_PADDING:
					sPos++;
					break;

				case SIGNED_BYTE:
				case SIGNED_SHORT:
				case LUA_INTEGER: //should be long
				case SIGNED_LONG:
					switch (f) { //falls through to signed_int_with_n_bytes
					case SIGNED_BYTE:
						sizeArg = 1;
						break;
					case LUA_INTEGER:
					case SIGNED_LONG:
						sizeArg = Long.BYTES;
						break;
					case SIGNED_SHORT:
						sizeArg = Short.BYTES;
					default: break;
					}
				case SIGNED_INT_WITH_N_BYTES:{
					buffer.clear();
					byte[] t = null;
					if(sizeArg!=Long.BYTES) {
						t = new byte[Long.BYTES-sizeArg];
						if((str[sPos] & 0x80) > 0)
							Arrays.fill(t, (byte)-1);
						if(buffer.order().equals(ByteOrder.BIG_ENDIAN))
							buffer.put(t);
					}
					buffer.put(str, sPos, sizeArg);
					if(buffer.order().equals(ByteOrder.LITTLE_ENDIAN) && t!=null)
						buffer.put(t);
					sPos+=sizeArg;
					buffer.flip();
					//buffer.limit(8);//FIXME move bytes?
					output.set(nOut++, LuaValue.valueOf(buffer.getLong()));
					break;
				}


				case UNSIGNED_BYTE:
					output.set(nOut++, LuaValue.valueOf( str[sPos++]&0xFF ));
					break;
				case SIZE_T:
				case UNSIGNED_LONG:
				case UNSIGNED_SHORT:
					switch (f) {
					case SIZE_T:
						sizeArg = Integer.BYTES;
						break;
					case UNSIGNED_SHORT:
						sizeArg = Short.BYTES;
						break;
					case UNSIGNED_LONG:
						throwArgErr("unpack", fPos, "Unsupported operation for 'unsigned long'", c);  
					default:
						break;
					}
				case UNSIGNED_INT_WITH_N_BYTES:
					if(sizeArg==Long.BYTES) throwArgErr("unpack", fPos, "byte overflow", c);
					long mask = (long)(Math.pow(2, 8*sizeArg))-1;
					buffer.clear();
					//FIXME respect endian
					if(buffer.order().equals(ByteOrder.BIG_ENDIAN))
						buffer.put(new byte[Long.BYTES-sizeArg]);
					buffer.put(str, sPos, sizeArg);
					if(buffer.order().equals(ByteOrder.LITTLE_ENDIAN))
						buffer.put(new byte[Long.BYTES-sizeArg]);
					sPos+=sizeArg;
					buffer.flip();

					output.set(nOut++, LuaValue.valueOf(  buffer.getLong()&mask  ));
					break;
				case XOP:
					PackFormat argf = PackFormat.fromChar(format.charAt(++fPos));
					int m = Math.min(argf.getAlignmentSize(), maxAlignment);
					if( m > 0 && ((m & (m - 1)) == 0) ) 
						sPos+= (m - sPos%m);
					//while(sPos++ % m != 0);	
					else
						throw new RuntimeException("An invalid padding was entered ("+sizeArg+")");



				default:
					break;
				}
			}
			return output.unpack();
		}

	}
	
	public static class PackSize extends OneArgFunction{
		@Override
		public LuaValue call(LuaValue arg) {
			String format = arg.checkjstring();
			//byte[] str = args.checkjstring(2).getBytes();
			//int startPos = args.optint(3, 1) - 1;
			//ByteBuffer buffer = ByteBuffer.allocate(16);

			//LuaTable output = new LuaTable();
			//int nOut = 1;
			int sizeArg = 0;
			int maxAlignment = 1;

			int sum = 0;
			for(int fPos = 0; fPos<format.length(); fPos++) {
				char c = format.charAt(fPos);
				PackFormat f = PackFormat.fromChar(c);

				if(f.acceptsNumberArg()) {
					sizeArg = 0;
					for(int u = fPos+1; u<format.length(); u++, fPos++) {
						if(Character.isDigit(format.charAt(u)))
							sizeArg = (sizeArg*10) + format.charAt(u)-'0';
						else
							break;
					}
					if(sizeArg > f.maxNumArgValue()) {
						throw new LuaError("integer size ("+sizeArg+") out of limits [1,16]");
					}
					if(sizeArg > Math.pow(2, 32))
						throw new LuaError("integer overflow ("+sizeArg+") out of limits [1, 4294967296]");
					if(sizeArg <= 0)
						throw new LuaError("integer size must be greater than 1");
				}else {
					sizeArg = 0;
				}
				
				if(f.equals(PackFormat.XOP)) {
					PackFormat argf = PackFormat.fromChar(format.charAt(++fPos));
					int m = Math.min(argf.getAlignmentSize(), maxAlignment);
					if( m > 0 && ((m & (m - 1)) == 0) ) 
						sum+= (m - sum%m);
					//while(sPos++ % m != 0);	
					else
						throw new RuntimeException("An invalid padding was entered ("+sizeArg+")");

				}else 
					sum += f.getByteCount(sizeArg);
			}
			return LuaValue.valueOf(sum);
		}
	}
	
	public static enum PackFormat {
		LITTLE_ENDIAN,
		BIG_ENDIAN,
		NATIVE_ENDIAN,
		MAXIMUM_ALIGNMENT_TO_N,
		SIGNED_BYTE,
		UNSIGNED_BYTE,
		SIGNED_SHORT,
		UNSIGNED_SHORT,
		SIGNED_LONG,
		UNSIGNED_LONG,
		LUA_INTEGER,
		LUA_UNSIGNED, //INT?
		SIZE_T, //?
		SIGNED_INT_WITH_N_BYTES, //DEFAULTS TO NATIVE SIZE
		UNSIGNED_INT_WITH_N_BYTES, //DEFAULTS TO NATIVE SIZE
		FLOAT,
		DOUBLE,
		LUA_NUMBER,
		FIXED_SIZE_STRING,
		ZERO_TERMINATED_STRING,
		LENGTH_LABELD_STRING, //UNSIGNED INT FOR SIZE, THEN THE STRING
		ONE_BYTE_PADDING,
		XOP, //"an empty item that aligns according to option op (which is otherwise ignored)"
		NONE;

		public boolean acceptsNumberArg() {
			switch (this) {
			case MAXIMUM_ALIGNMENT_TO_N:
			case SIGNED_INT_WITH_N_BYTES:
			case UNSIGNED_INT_WITH_N_BYTES:
			case FIXED_SIZE_STRING:
				return true;
			default:
				return false;
			}
		}
		
		/**
		 * Similar to getAlignmentSize, but will throw LuaError for variable length options
		 * */
		public int getByteCount(int sizeArg) {
			switch (this) {
			case BIG_ENDIAN: 			return 0;
			case DOUBLE: 				return Double.BYTES;
			case FIXED_SIZE_STRING:		return sizeArg;
			case FLOAT:					return Float.BYTES;
			case LENGTH_LABELD_STRING:	throw new LuaError("Unable to return pack size with option 's'");
			case LITTLE_ENDIAN:			return 0;
			case LUA_INTEGER:			return Double.BYTES; //TODO check this
			case LUA_NUMBER:			return Long.BYTES; //TODO check this
			case LUA_UNSIGNED:			return Double.BYTES; //TODO check this
			case MAXIMUM_ALIGNMENT_TO_N:return 0;
			case NATIVE_ENDIAN:			return 0;
			case NONE:					return 0;
			case ONE_BYTE_PADDING:		return 1;
			case SIGNED_BYTE:			return 1;
			case SIGNED_INT_WITH_N_BYTES:return sizeArg;
			case SIGNED_LONG:			return Long.BYTES;
			case SIGNED_SHORT:			return Short.BYTES;
			case SIZE_T:				return Integer.BYTES;
			case UNSIGNED_BYTE:			return Byte.BYTES;
			case UNSIGNED_INT_WITH_N_BYTES:return sizeArg;
			case UNSIGNED_LONG:			return Long.BYTES;
			case UNSIGNED_SHORT:		return Short.BYTES;
			case XOP:					throw new RuntimeException("Xop lenght must be calculated");
			case ZERO_TERMINATED_STRING:throw new LuaError("Unable to return pack size with option 'z'");
			default:
				throw new LuaError("Type "+this.name()+" doesn't have a registered byte size available, this is a bug");
			}
		}

		public int getAlignmentSize() {
			switch (this) {
			case FLOAT:
				return Float.BYTES;
			case DOUBLE:
			case LUA_NUMBER://TODO Check this
			case LUA_UNSIGNED://TODO Check this
				return Double.BYTES;
			case SIZE_T:
			case LENGTH_LABELD_STRING:
				return Integer.BYTES;
			case SIGNED_LONG:
			case UNSIGNED_LONG:
			case LUA_INTEGER:
				return Long.BYTES;
			case SIGNED_BYTE:
			case UNSIGNED_BYTE:
				return Byte.BYTES;
			case SIGNED_SHORT:
			case UNSIGNED_SHORT:
				return Short.BYTES;
			default:
				return 1;
			}
		}

		public int maxNumArgValue() {
			if(!this.acceptsNumberArg()) return 0;
			if(!this.equals(LENGTH_LABELD_STRING)) return 16;
			return Integer.MAX_VALUE;
		}

		public static PackFormat fromChar(char c) {
			switch (c) {
			case '<': return LITTLE_ENDIAN;
			case '>': return BIG_ENDIAN;
			case '=': return NATIVE_ENDIAN;
			case '!': return MAXIMUM_ALIGNMENT_TO_N;
			case 'b': return SIGNED_BYTE;
			case 'B': return UNSIGNED_BYTE;
			case 'h': return SIGNED_SHORT;
			case 'H': return UNSIGNED_SHORT;
			case 'l': return SIGNED_LONG;
			case 'L': return UNSIGNED_LONG;
			case 'j': return LUA_INTEGER;
			case 'J': return LUA_UNSIGNED;
			case 'T': return SIZE_T;
			case 'i': return SIGNED_INT_WITH_N_BYTES;
			case 'I': return UNSIGNED_INT_WITH_N_BYTES;
			case 'f': return FLOAT;
			case 'd': return DOUBLE;
			case 'n': return LUA_NUMBER;
			case 'c': return FIXED_SIZE_STRING;
			case 'z': return ZERO_TERMINATED_STRING;
			case 's': return LENGTH_LABELD_STRING;
			case 'x': return ONE_BYTE_PADDING;
			case 'X': return XOP;
			case ' ': return NONE;
			default: return null;
			}
		}
	}

	public static class ByteArrayStack {
		Node head, tail;
		int bytes = 0;
		int items = 0;
		private class Node {
			Node next;
			byte[] chunk;
			public Node(byte[] chunk) {
				this.chunk = chunk;
			}
			private void setNext(Node next) {
				this.next = next;
			}
			private Node getNext() {
				return next;
			}
			@Override
			public String toString() {
				return Arrays.toString(chunk);
			}
		}
		public int getBytes() {
			return bytes;
		}
		public int getItems() {
			return items;
		}

		public void add(byte[] chunk) {
			Node n = new Node(chunk);
			if(head==null || tail==null)
				head = tail = n;
			else 
				tail = (tail.next = n);

			items++;
			bytes+=chunk.length;
		}

		public void appendZeros(int l) {
			byte[] z = new byte[l];
			add(z);
		}

		public void append(String str) {
			add(str.getBytes());
		}

		public byte[] toCompleteArray() {
			byte[] full = new byte[bytes];
			int destPos = 0;



			for(Node n = head; n!=null; destPos+=n.chunk.length, n=n.next) {
				System.arraycopy(n.chunk, 0, full, destPos, n.chunk.length);
			}
			return full;
		}

		@Override
		public String toString() {
			StringJoiner sj = new StringJoiner(",", "<", ">");
			for(Node n = head; n!=null; n = n.next)
				sj.add(n.toString());
			return sj.toString();
		}

		/**
		 * Not to be confused with {@link Object#toString}<br>
		 * used to turn the complete byte array into a string
		 * */
		public String asString() {
			return new String(toCompleteArray());
		}
	}
}
