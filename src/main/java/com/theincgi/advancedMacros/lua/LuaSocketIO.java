package com.theincgi.advancedMacros.lua;

import java.net.URI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaFunction;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import com.theincgi.advancedMacros.lua.util.LuaJson;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;

public class LuaSocketIO {
	
	private Socket socket;
	
	public LuaSocketIO() {
	}
	
	public static class Connect extends OneArgFunction {
		
	}
	
	public LuaTable getFunctions() {
		LuaTable funcs = new LuaTable();
		
		funcs.set("emit", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				String event = args.checkjstring(1);
				
				LuaValue value = args.arg(2).checknotnil();
				
				Object payload = objOfVal( value );
				Ack ack = null;
				
				
				if( args.narg() == 3 ) {
					;
					ack = ackOfFunc( args.checkfunction( 3 ) );
				}
				emit( event, payload, ack );
				return NONE;
			}
		});
		funcs.set("send", new VarArgFunction() {
			@Override
			public Varargs invoke( Varargs args ) {
				Object[] objs = new Object[args.narg()];
				for (int i = 0; i < objs.length; i++) {
					objs[i] = objOfVal( args.arg(i+1) );
				}
				socket.send(objs);
				return NONE;
			}
		});
		funcs.set("on", new VarArgFunction() {
		});
		funcs.set("once", new VarArgFunction() {
		});
		funcs.set("", new VarArgFunction() {
		});
		
		return funcs;
	}
	
	protected Object objOfVal( LuaValue value ) {
		try {
			if( value.isuserdata() ) 
				return value.checkuserdata();
			else if( value.istable() ) 
				return LuaJson.auto( value.checktable() );
			else if( value.isboolean() )
				return value.checkboolean();
			else if( value.isint() )
				return value.checkint();
			else if( value.islong() )
				return value.checklong();
			else if( value.isnumber() )
				return value.checkdouble();
			else if( value.isstring() )
				return value.checkjstring();
			throw new LuaError("Can not send type '"+value.typename()+"'");
		} catch (JSONException e) {
			throw new LuaError( e );
		}
	}
	
	protected Ack ackOfFunc( LuaFunction f ) {
		return (aaa) -> {
			try {
				LuaTable params = new LuaTable();
				for(Object o : aaa) {
					if( o instanceof JSONArray )
						params.set(params.length()+1, LuaJson.table((JSONArray)o));
					if( o instanceof JSONObject )
						params.set(params.length()+1, LuaJson.table((JSONObject)o));
					if( o instanceof Integer )
						params.set(params.length()+1, LuaValue.valueOf((Integer)o));
					if( o instanceof Long )
						params.set(params.length()+1, LuaValue.valueOf((Long)o));
					if( o instanceof Double )
						params.set(params.length()+1, LuaValue.valueOf((Double)o));
					if( o instanceof String )
						params.set(params.length()+1, LuaValue.valueOf((String)o));
					if( o instanceof Boolean )
						params.set(params.length()+1, LuaValue.valueOf((Boolean)o));
				}
				f.invoke( params.unpack() ); 
			} catch (JSONException e) {
				throw new LuaError( e );
			}
		};
	}
	
	//server must already be running in editor
	public void connect( String url ) {
		URI uri = URI.create(url);
		
		IO.Options options = IO.Options.builder()
			//
			.build();
		
		this.socket = IO.socket(uri, options);
	}
	
	public LuaTable generateFunctions() {
		LuaTable out = new LuaTable();
		
		return out;
	}
	
	public void send( Object... objs) {
		socket.send( objs );
	}
	
	public void emit( String event, Object obj, Ack ack ) {
		if(ack != null)
			socket.emit(event, obj, ack);
		else
			socket.emit(event, obj);
	}
	
}
