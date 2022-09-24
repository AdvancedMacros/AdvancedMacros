package com.theincgi.advancedMacros.lua;

import java.net.URI;

import org.json.JSONException;
import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaFunction;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.lua.util.LuaJson;
import com.theincgi.advancedMacros.misc.Pair;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter.Listener;

import org.json.JSONArray;
import org.json.JSONObject;

public class LuaSocketIO {
	
	private Socket socket;
	
	public LuaSocketIO( String host ) {
		build( host );
	}
	
	/**Constructor for controls on a new object for lua*/
	public static class NewLuaSocketIO extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue host) {
			if(!host.checkjstring().matches("^[^:]+://.+")) 
				throw new LuaError("url must contain protocol ('http://', 'https://', ws://...)");
			LuaSocketIO lsio = new LuaSocketIO( host.checkjstring() );
			return lsio.getFunctions();
		}
	}
	
	public void build(String url) {
		URI uri = URI.create(url);
		
		IO.Options options = IO.Options.builder()
			//
			.build();
		
		this.socket = IO.socket(uri, options);
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
		/**Returns a function to stop the listener*/
		funcs.set("on", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue eventName, LuaValue callback) {
				Pair<Listener, LuaFunction> f = off(eventName.checkjstring(),ackOfFunc( callback.checkfunction() ));
				socket.on(eventName.checkjstring(), f.a);
				return f.b;
			}
		});
		funcs.set("onAnyIncoming", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue callback) {
				Pair<Listener, LuaFunction> f = offIncoming(ackOfFunc( callback.checkfunction() ));
				socket.onAnyIncoming( f.a );
				return f.b;
			}
		});
		funcs.set("onAnyOutgoing", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue callback) {
				Pair<Listener, LuaFunction> f = offOutgoing(ackOfFunc( callback.checkfunction() ));
				socket.onAnyOutgoing( f.a );
				return f.b;
			}
		});
		
		funcs.set("once", new TwoArgFunction() {
			@Override 
			public LuaValue call(LuaValue eventName, LuaValue callback) {
				Pair<Listener, LuaFunction> f = off( eventName.checkjstring(), ackOfFunc( callback.checkfunction() ));
				socket.once(eventName.checkjstring(), ackOfFunc( callback.checkfunction() )::call );
				return f.b;
			}
		});
		
		funcs.set("connect", new OneArgFunction() {
			@Override
			public LuaValue call( LuaValue timeout ) {
				try {
					socket.connect();
				}catch(Exception e) {
					throw new LuaError(e);
				}
				long start = System.currentTimeMillis();
				while( !socket.connected() && System.currentTimeMillis() - start <= timeout.optdouble(3000) ) {
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {break;}
				}
				return LuaValue.valueOf(socket.connected());
			}
		});
		funcs.set("disconnect", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				socket.disconnect();
				return NONE;
			}
		});
		funcs.set("isConnected", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(socket.connected());
			}
		});
		funcs.set("isActive", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(socket.isActive());
			}
		});
		funcs.set("EVENT_CONNECT",       Socket.EVENT_CONNECT      );
		funcs.set("EVENT_DISCONNECT",    Socket.EVENT_DISCONNECT   );
		funcs.set("EVENT_CONNECT_ERROR", Socket.EVENT_CONNECT_ERROR);
		
		return funcs;
	}
	
	protected Pair<Listener, LuaFunction> off( String eventName, Ack ack ) {
		Listener listener = ack::call;
		return new Pair<Listener, LuaFunction>( listener, new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				socket.off(eventName, listener);
				return NONE;
			}
		});
	} 
	
	protected Pair<Listener, LuaFunction> offIncoming( Ack ack ) {
		Listener listener = ack::call;
		return new Pair<Listener, LuaFunction>( listener, new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				socket.offAnyIncoming( listener );
				return NONE;
			}
		});
	} 
	
	protected Pair<Listener, LuaFunction> offOutgoing( Ack ack ) {
		Listener listener = ack::call;
		return new Pair<Listener, LuaFunction>( listener, new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				socket.offAnyOutgoing( listener );
				return NONE;
			}
		});
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
					if( o instanceof Boolean )
						params.set(params.length()+1, LuaValue.valueOf((Boolean)o));
					if( o instanceof String )
						params.set(params.length()+1, LuaValue.valueOf((String)o));
				}
				f.invoke( params.unpack() ); 
			} catch (JSONException e) {
				throw new LuaError( e );
			}
		};
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
