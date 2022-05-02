package com.theincgi.advancedMacros.lua.util;

import java.util.LinkedList;
import java.util.WeakHashMap;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import com.theincgi.advancedMacros.misc.CallableTable;

public class LuaMutex extends CallableTable{
	private static WeakHashMap<String, Long> mutex = new WeakHashMap<>();
	private static WeakHashMap<Long, LinkedList<String>> reverse = new WeakHashMap<>();
	private static String[] DOC_LOCATION = new String[] {"newMutex"};
	public LuaMutex() {
		super(DOC_LOCATION, new OneArgFunction() {

			@Override
			public LuaValue call(LuaValue arg) {
				return new Mutex(arg.checkjstring());
			}
		});
	}

	private static void sleep() {
		try {
			synchronized (mutex) {
				mutex.wait();
			}
		}catch (Exception e) {}
	}
	
	private static void register(String key) {
		mutex.put(key, Thread.currentThread().getId());
		LinkedList<String> held = reverse.computeIfAbsent(Thread.currentThread().getId(), (k)->{return new LinkedList<>();});
		held.add(key);
	}
	private static void unregister(String key) {
		mutex.remove(key);
		LinkedList<String> held = reverse.get(Thread.currentThread().getId());
		if (held != null)
			held.remove(key);
		mutex.notifyAll();
	}
	
	public static void cleanup() {
		long id = Thread.currentThread().getId();
		cleanup(id);
	}
	public static void cleanup(long id) {
		LinkedList<String> held = reverse.get(id);
		if (held != null) {
			for (String key : held) {
				unregister(key);
			}
		}
	}

	public static boolean lock(String key, long timeout) {
		long end = System.currentTimeMillis()+timeout;
		do {
			synchronized (mutex) {
				if(!mutex.containsKey(key)) {
					register(key);
					return true;
				}else if(mutex.get(key) == Thread.currentThread().getId()){
					return true; //already locked
				}
			}
			sleep();
		}while(timeout==-1 || System.currentTimeMillis() < timeout);
		return false;
	}

	public static boolean tryLock(String key) {
		synchronized (mutex) {
			if(mutex.containsKey(key)) {
				return mutex.get(key)==Thread.currentThread().getId();
			}
			register(key);
		}
		return true;
	}
	public static void unlock(String key) {
		synchronized (mutex) {
			if(mutex.containsKey(key)) {
				if(mutex.get(key) == Thread.currentThread().getId()) {
					unregister(key);
				}else {
					throw new LuaError("Attempt to unlock a mutex locked on another thread");
				}
			}//else already unlocked
		}
	}

	private static class Mutex extends LuaTable {
		String key;

		public Mutex(String key) {
			this.key = key;
			for (Op op : Op.values()) {
				this.set(op.name(), new CallableTable(op.docLocation(), new DoOp(op)));
			}
		}

		private class DoOp extends VarArgFunction {
			Op op;
			public DoOp(Op op) {
				this.op = op;
			}

			@Override
			public Varargs invoke(Varargs args) {
				switch (op) {
				case getKey:{
					return valueOf(key);
				}
				case lock:
					return valueOf(lock(key, args.arg1().optint(1, -1)));
				case tryLock:
					return valueOf(tryLock(key));
				case unlock:
					unlock(key);
					return NONE;
				default:
					break;
				}
				return NONE;
			}
		}


		private static enum Op {
			lock,
			tryLock,
			unlock,
			getKey;

			String[] docLocation() {
				return new String[] {"newMutex()", this.name()};
			}
		}
	}
}
