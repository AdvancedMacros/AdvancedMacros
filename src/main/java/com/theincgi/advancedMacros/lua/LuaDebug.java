package com.theincgi.advancedMacros.lua;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaFunction;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.DebugLib;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.gui.Color;
import com.theincgi.advancedMacros.lua.util.LuaMutex;
import com.theincgi.advancedMacros.misc.Utils;

public class LuaDebug extends DebugLib{
	private static final HashMap<Thread, LuaThread> threads = new HashMap<>();

	private static LinkedList<StatusListener> statusListeners = new LinkedList<>();
	public abstract static class StatusListener{
		public abstract void onStatus(final Thread sThread, Status status);
	}
	public void addStatusListener(StatusListener sl){
		synchronized (statusListeners) {
			statusListeners.add(sl);
		}
	}
	public void removeStatusListener(StatusListener sl){
		synchronized (statusListeners) {
			statusListeners.remove(sl);
		}
	}
	private static void notifyStatusListeners(Thread sThread, Status status){
		synchronized (statusListeners) {
			Iterator<StatusListener> listeners = statusListeners.iterator();
			while(listeners.hasNext()){
				listeners.next().onStatus(sThread, status);
			}
		}
	}
	@Override
	public void onInstruction(int arg0, Varargs arg1, int arg2) {
		if(threads.containsKey(Thread.currentThread())){
			LuaThread lt = threads.get(Thread.currentThread());
			switch (lt.status) {
			case RUNNING:
				super.onInstruction(arg0, arg1, arg2);
				break;
			case PAUSED:{
				try {
					while(lt.status.equals(Status.PAUSED))
						Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return;
			}
			case STOPPED:
				synchronized (threads) {
					threads.remove(Thread.currentThread());
				}
				throw new LuaError("Script was stopped");
			default:
				break;
			}
		}
	}



	//	public Set<Thread> getThreads(){
	//		return threads.keySet();
	//	}
	public static double getUptime(Thread thread){
		LuaThread lt = threads.get(thread);
		if(lt==null){return Double.NaN;}
		return lt.getUpTime();
	}
	public static String getLabel(Thread thread){
		LuaThread lt = threads.get(thread);
		if(lt==null){return "";}
		return lt.label;
	}
	public static LuaValue getLuaStatus(Thread thread){
		LuaThread lt = threads.get(thread);
		if(lt==null){return LuaValue.NIL;}
		return LuaValue.valueOf(lt.status.toString().toLowerCase());
	}
	public static Status getStatus(Thread thread){
		LuaThread lt = threads.get(thread);
		if(lt==null){return null;}
		return lt.status;
	}
	public static void stop(Thread thread) {
		LuaThread lt = threads.get(thread);
		if(lt==null){return;}
		lt.stop();
	}

	public abstract static class OnScriptFinish{
		abstract public void onFinish(Varargs v);
	}
	public static class LuaThread{
		LuaFunction sFunc;
		Varargs varagrs;
		protected long launchTime;
		protected Status status = Status.NEW;
		private String label;
		protected Thread thread;

		private LuaThread() {}

		public LuaThread(LuaValue sFunc, String label) {
			this(sFunc, new LuaTable(), label);
		}
		public LuaThread(LuaValue sFunc, Varargs varagrs, String label) {
			if(sFunc.istable() && !sFunc.getmetatable().isnil() && !sFunc.getmetatable().get("__call").isnil())
				this.sFunc = sFunc.getmetatable().get("__call").checkfunction();
			else
				this.sFunc = sFunc.checkfunction();
			this.label = label;
			this.varagrs = varagrs;
		}

		public static LuaThread getCurrent() {
			return threads.get(Thread.currentThread());
		}

		public Thread getThread() {
			return thread;
		}

		protected void register(Thread t){
			synchronized (threads) {
				threads.put(t, this);
			}
		}
		/**returns a LuaTable for controlling this thread*/
		public LuaTable start(){
			return start(null);
		}
		/**returns a LuaTable for controlling this thread*/
		public LuaTable start(final OnScriptFinish onScriptFinish){ //TODO needs lua func to make new thread
			synchronized (this) {
				if(status.equals(Status.NEW)){
					thread = new Thread(new Runnable() {
						@Override
						public void run() {
							try{
								register(thread);
								status = Status.RUNNING;
								notifyStatusListeners(thread, Status.RUNNING);
								launchTime = System.currentTimeMillis();
								org.luaj.vm2_v3_0_1.LuaThread luaThread = new org.luaj.vm2_v3_0_1.LuaThread(AdvancedMacros.globals, sFunc);
								AdvancedMacros.globals.setCurrentLuaThread(luaThread);
								//luaThread.state.args = varagrs;
								//luaThread.state.run()
								Varargs retValues = sFunc.invoke(varagrs);//luaThread.resume(varagrs);//sFunc.invoke(varagrs);
								if(onScriptFinish!=null){onScriptFinish.onFinish(retValues);}
								status = Status.DONE;
								notifyStatusListeners(thread, Status.DONE);
							}catch (Throwable e) {
								status = Status.CRASH;
								notifyStatusListeners(thread, Status.CRASH);
								e.printStackTrace();
								Utils.logError(e);
							}
							LuaMutex.cleanup();
						}
					});
					thread.setName(sFunc.tojstring());
					thread.start();

					return ThreadControls.getControls(this);
				}else{
					throw new LuaError("Attempt to start a thread in state '"+status+"'");
				}
			}
		}
		public void stop(){
			status = Status.STOPPED;
			notifyStatusListeners(thread, Status.STOPPED);
			thread.interrupt();
		}
		public double getUpTime(){
			return ((int)(System.currentTimeMillis()-launchTime)/10f)/100f;
		}


	}
	/**Added for the ChatSendFilter which will go through multiple filters, this occurs in a runnable<br>
	 * This will allow it to show up inside the running scripts list and be cancel-able*/
	public static class JavaThread extends LuaThread{
		Runnable r;
		public JavaThread(Runnable r) {
			this.r = r;
		}

		@Override
		public LuaTable start(OnScriptFinish unused) {
			if(status.equals(Status.NEW)){
				thread = new Thread(new Runnable() {
					@Override
					public void run() {
						try{
							register(thread);
							status = Status.RUNNING;
							notifyStatusListeners(thread, Status.RUNNING);
							launchTime = System.currentTimeMillis();
							AdvancedMacros.globals.setCurrentLuaThread(new org.luaj.vm2_v3_0_1.LuaThread(AdvancedMacros.globals));
							r.run();

							status = Status.DONE;
							notifyStatusListeners(thread, Status.DONE);
						}catch (Throwable e) {
							status = Status.CRASH;
							notifyStatusListeners(thread, Status.CRASH);
							e.printStackTrace();
							Utils.logError(e);
						}
					}
				});

				thread.start();

				return ThreadControls.getControls(this);
			}else{
				throw new LuaError("Attempt to start a thread in state '"+status+"'");
			}
		}

		public void setName(String s) {
			thread.setName(s);
		}
	}
	public static class GetCurrent extends ZeroArgFunction{
		@Override
		public LuaValue call() {
			return ThreadControls.getControls(LuaThread.getCurrent());
		}
	}
	public static enum Status{
		/**BLUE  */ NEW,
		/**GREEN */ RUNNING,
		/**YELLOW*/ PAUSED,
		/**N/A   */ STOPPED,
		/**N/A   */ CRASH,
		/**N/A   */ DONE;
		@Override
		public String toString() {
			return name().toLowerCase();
		}
		public Color getStatusColor(){
			switch (this) {
			case NEW:
				return Color.TEXT_b;
			case RUNNING:
				return Color.TEXT_a;
			case PAUSED:
				return Color.TEXT_e;
			default:
				return Color.WHITE;
			}
		}
	}

	public static class ThreadControls extends LuaTable{
		static final WeakHashMap<LuaThread, ThreadControls> controlLookup = new WeakHashMap<>();
		LuaThread t;

		public static ThreadControls getControls(LuaThread t) {
			if(t==null) throw new NullPointerException("LuaThread is null");
			return controlLookup.computeIfAbsent(t, (key)->{
				return new ThreadControls(t);
			});
		}

		private ThreadControls(LuaThread t) {
			super();
			this.t = t;
			set("start", new Start());
			set("stop", new Stop());
			set("getStatus", new GetStatus());
			set("pause", new Pause());
			set("unpause", new Unpause());
			set("getID", new GetID());
			set("getLabel", new GetLabel());
			set("getUptime", new GetUptime());
			controlLookup.put(t, this);
		}
		class Start extends ZeroArgFunction{
			@Override
			public LuaValue call() {
				t.start();
				return LuaValue.NIL;
			}}
		class Stop extends ZeroArgFunction{
			@Override
			public LuaValue call() {
				t.stop();
				return LuaValue.NIL;
			}
		}
		class GetID extends ZeroArgFunction{
			@Override
			public LuaValue call() {
				return valueOf(t.thread.getId());
			}
		}
		class GetStatus extends ZeroArgFunction{
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(t.status.toString());
			}
		}
		class Pause extends ZeroArgFunction{
			@Override
			public LuaValue call() {
				if(t.status.equals(Status.RUNNING)){
					t.status = Status.PAUSED;
					notifyStatusListeners(t.thread, Status.PAUSED);
					return LuaValue.NONE;
				}
				throw new LuaError("Attempt to pause a thread in state '"+t.status+"'");
			}
		}
		class Unpause extends ZeroArgFunction{
			@Override
			public LuaValue call() {
				if(t.status.equals(Status.PAUSED)){
					t.status = Status.RUNNING;
					notifyStatusListeners(t.thread, Status.RUNNING);
					return LuaValue.NONE;
				}
				throw new LuaError("Attempt to unpause a thread in state '"+t.status+"'");
			}
		}
		class GetUptime extends ZeroArgFunction {
			@Override
			public LuaValue call() {
				return valueOf(getUptime(t.thread));
			}
		}
		class GetLabel extends ZeroArgFunction{
			@Override
			public LuaValue call() {
				return valueOf(t.label);
			}
		}

		public LuaThread getThread() {
			return t;
		}
	}



	public void stopAll() { //FIXME concurrent mod
		synchronized (threads) {
			for (LuaThread t : threads.values()) {
				t.stop();
			}
		}
	}

	public static class GetRunningScripts extends ZeroArgFunction {
		@Override
		public LuaValue call() {
			LuaTable scripts = new LuaTable();
			synchronized (threads) {
				for (Entry<Thread, LuaThread> e : threads.entrySet()) {
					if(e.getValue().status == Status.RUNNING || e.getValue().status==Status.PAUSED)
						scripts.set(valueOf(e.getKey().getId()), ThreadControls.getControls(e.getValue()));
				}
			}
			return scripts;
		}
	}

}