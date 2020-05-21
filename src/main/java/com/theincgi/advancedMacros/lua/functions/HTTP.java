package com.theincgi.advancedMacros.lua.functions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

public class HTTP extends OneArgFunction{
	//
	@Override
		public LuaValue call(LuaValue arg) {
			try {
				LuaTable sets = arg.checktable();
				
				URL url = new URL(sets.get("url").checkjstring());
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				
				conn.setRequestMethod(sets.get("requestMethod").optjstring("GET"));
				if(sets.get("requestProperties").istable()) {
					LuaTable t = sets.get("requestProperties").checktable();
					for(LuaValue v : t.keys()) {
						conn.setRequestProperty(v.checkjstring(), t.checkjstring());
					}
				}
				conn.setConnectTimeout(sets.get("timeout").optint(10) * 1000); //in seconds, default 10s
				
				conn.setDoOutput(sets.get("doOutput").optboolean(false));
				conn.setInstanceFollowRedirects(sets.get("followRedirects").optboolean(conn.getFollowRedirects()));
				
				
				
				return new LuaConnection(conn);
				
			} catch (MalformedURLException e) {
				throw new LuaError("MalformedURLException occurred");
			} catch (IOException e) {
				throw new LuaError("IOException occurred");
			}
		}
	
	
//	•GET 
//	•POST 
//	•HEAD 
//	•OPTIONS 
//	•PUT 
//	•DELETE 
//	•TRACE 

	public static class LuaConnection extends LuaTable{
		HttpURLConnection conn;

		public LuaConnection(HttpURLConnection conn) throws IOException {
			this.conn = conn;
			this.set("input", new LuaInputStream(conn.getInputStream()));
			if(conn.getDoOutput())
				this.set("output", new LuaOutputStream(conn.getOutputStream()));
			if(conn.getErrorStream()!=null)
				this.set("err", new LuaInputStream(conn.getErrorStream()));
			
			this.set("getURL", new ZeroArgFunction() {@Override	public LuaValue call() {return LuaValue.valueOf(conn.getURL().toString());}} );
			this.set("getResponseCode", new ZeroArgFunction() {@Override	public LuaValue call() {try {return LuaValue.valueOf(conn.getResponseCode());}catch (IOException e) {return LuaValue.NIL;}}} );
			this.set("getContentType", new ZeroArgFunction() {
				@Override
				public LuaValue call() {
					return LuaValue.valueOf(conn.getContentType());
				}
			});
			this.set("getContentEncoding", new ZeroArgFunction() {
				@Override
				public LuaValue call() {
					return LuaValue.valueOf(conn.getContentEncoding());
				}
			});
			this.set("getContentLength", new ZeroArgFunction() {
				@Override
				public LuaValue call() {
					return LuaValue.valueOf(conn.getContentLengthLong());
				}
			});
			this.set("getFollowRedirects", new ZeroArgFunction() {
				@Override
				public LuaValue call() {
					return LuaValue.valueOf(conn.getFollowRedirects());
				}
			});
			this.set("disconnect", new ZeroArgFunction() {
				@Override
				public LuaValue call() {
					conn.disconnect();
					return LuaValue.NONE;
				}
			});
		}
		
		@Override
		protected void finalize() throws Throwable {
			try{
				conn.disconnect();
			}catch(Exception e) {}
		}
		
	}
	
	public static class LuaInputStream extends LuaTable{
		InputStream in;
		Scanner scanner;
		public LuaInputStream(InputStream in) {
			this.in = in;
			scanner = new Scanner(in);
			
			this.set("readByte", new ZeroArgFunction() {
				@Override
				public LuaValue call() {
					try {
						return LuaValue.valueOf(in.read());
					} catch (IOException e) {
						return LuaValue.valueOf(-1);
					}
				}
			});
			this.set("readChar", new ZeroArgFunction() {
				@Override
				public LuaValue call() {
					try {
						int b = in.read();
						if(b>=0)
							return LuaValue.valueOf(String.valueOf((char)b));
						return FALSE;
					} catch (IOException e) {
						return LuaValue.NIL;
					}
				}
			});
			this.set("available", new ZeroArgFunction() {
				@Override
				public LuaValue call() {
					try {
						return LuaValue.valueOf(in.available());
					} catch (IOException e) {
						return LuaValue.valueOf(0);
					}
				}
			});
			this.set("readLine", new ZeroArgFunction() {
				@Override
				public LuaValue call() {
					if(scanner.hasNextLine())
						return LuaValue.valueOf(scanner.nextLine());
					return LuaValue.NIL;
				}
			});
			this.set("close", new ZeroArgFunction() {
				@Override
				public LuaValue call() {
					scanner.close();
					try {
						in.close();
					} catch (IOException e) {}
					return LuaValue.NONE;
				}
			});
		}
		
	}
	
	public static class LuaOutputStream extends LuaTable{
		OutputStream out;

		public LuaOutputStream(OutputStream out) {
			this.out = out;
			this.set("writeByte", new OneArgFunction() {
				@Override
				public LuaValue call(LuaValue arg) {
					try {
						out.write(arg.checkint());
					} catch (IOException e) {
						throw new LuaError("IOException occurred");
					}
					return LuaValue.NONE;
				}
			});
			this.set("writeBytes", new VarArgFunction() {
				@Override
				public Varargs invoke(Varargs args) {
					if(args.arg1().istable()) {
						args = args.arg1().checktable().unpack();
					}
					for (int i = 0; i < args.narg(); i++) {
						try {
							out.write(args.arg(i).checkint());
						} catch (IOException e) {
							throw new LuaError("IOException occurred");
						}
					}
					return LuaValue.NONE;
				}
			});
			this.set("write", new OneArgFunction() {
				@Override
				public LuaValue call(LuaValue arg) {
					try {
						out.write(arg.checkjstring().getBytes());
					} catch (IOException e) {
						throw new LuaError("IOException occurred");
					}
					return LuaValue.NONE;
				}
			});
			this.set("writeLine", new OneArgFunction() {
				@Override
				public LuaValue call(LuaValue arg) {
					try {
						out.write((arg.checkjstring()+"\n").getBytes());
					} catch (IOException e) {
						throw new LuaError("IOException occurred");
					}
					return LuaValue.NONE;
				}
			});
			this.set("flush", new OneArgFunction() {
				@Override
				public LuaValue call(LuaValue arg) {
					try {
						out.flush();
					} catch (IOException e) {
						throw new LuaError("IOException occurred");
					}
					return LuaValue.NONE;
				}
			});
			this.set("close", new OneArgFunction() {
				@Override
				public LuaValue call(LuaValue arg) {
					try {
						out.close();
					} catch (IOException e) {
						throw new LuaError("IOException occurred");
					}
					return LuaValue.NONE;
				}
			});
			
		}
		
	}
}
