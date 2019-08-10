package com.theincgi.advancedMacros.event;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.util.concurrent.ListenableFuture;
import com.theincgi.advancedMacros.AdvancedMacros;

public class TaskDispatcher {
	private static ConcurrentLinkedQueue<CallableTask<?>> callableTasks = new ConcurrentLinkedQueue<>();
	private static ConcurrentLinkedQueue<RunnableTask> runnableTasks = new ConcurrentLinkedQueue<>();
	private static ArrayList<DelayedTask<?>>  delayedTasks  = new ArrayList<>();
	
	private TaskDispatcher() {}
	
	private static class DelayedTask<Z>{
		private Object task; public DelayedTask(Callable<Z> t, long exTime) {
			task = t; executionTime=exTime+System.currentTimeMillis();} public DelayedTask(Runnable t, long exTime) {task = t;executionTime=exTime+System.currentTimeMillis();}
		private long executionTime;
		@SuppressWarnings("unchecked") public void addTask() {
			if(task instanceof Runnable) TaskDispatcher.addTask((Runnable)    task);
			else 						 TaskDispatcher.addTask((Callable<Z>) task);}
		public long getExecutionTime() {return executionTime;}
	}
	
	public static <T> ListenableFuture<T> delayTask( Callable<T> c, long millis ){
		DelayedTask<T> dt = new DelayedTask<>(c, millis);
		CallableTask<T> task = new CallableTask<>(c);
		
		insertSorted(delayedTasks, dt);
		
		return task.future;
	}
	public static <T> ListenableFuture<Void> delayTask( Runnable c, long millis ){
		DelayedTask<T> dt = new DelayedTask<>(c, millis);
		RunnableTask task = new RunnableTask(c);
		
		insertSorted(delayedTasks, dt);
		
		return task.future;
	}
	
	private static <Z> void insertSorted(ArrayList<DelayedTask<?>> list, DelayedTask<Z> task) {
		synchronized (delayedTasks) {
			int cursor = 0;
			while(cursor < list.size() && list.get(cursor).executionTime > task.executionTime) cursor++;
			list.add(cursor, task);
		}
		
	}

	public static <T> ListenableFuture<T> addTask( Callable<T> c ){
		if(AdvancedMacros.getMinecraftThread() == Thread.currentThread()) {
			CallableTask<T> tmp = new CallableTask<>(c);
			tmp.call();
			return tmp.future;
		}
		CallableTask<T> task = new CallableTask<>(c);
		callableTasks.add(task);
		return task.future;
	}
	public static ListenableFuture<Void> addTask( Runnable r ){
		if(AdvancedMacros.getMinecraftThread() == Thread.currentThread()) {
			RunnableTask tmp = new RunnableTask(r);
			tmp.call();
			return tmp.future;
		}
		RunnableTask task = new RunnableTask(r);
		runnableTasks.add(task);
		return task.future;
	}
	
	public static void runTasks() {
		synchronized (delayedTasks) {
			long now = System.currentTimeMillis();
			while(delayedTasks.size() > 0 && delayedTasks.get(0).executionTime < now)
					delayedTasks.remove(0).addTask();
		}
		
		int cTasks = callableTasks.size();
		int rTasks = runnableTasks.size();
		try {
			for(int i = 0; i<cTasks; i++) 
				callableTasks.remove().call();
			}catch (NoSuchElementException e) {}
		try {
			for(int i = 0; i<rTasks; i++) 
				runnableTasks.remove().call();
		}catch (NoSuchElementException e) {}
		if(cTasks+rTasks>0) System.out.println("Tasks dispatched: "+(cTasks+rTasks));
	}
	
	
	private static class CallableTask<U> {
		Callable<U> c;
		LF future;
		boolean isDone = false;
		boolean isCancelled = false;
		private U result;
		private final LinkedList<Runnable> listeners = new LinkedList<>();
		private final LinkedList<Executor> executors = new LinkedList<>();
		private Throwable err = null;
		private class LF implements ListenableFuture<U> {
			

			public LF() {
			}

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				synchronized(listeners) {
					return isCancelled = isCancelled || callableTasks.remove(this);
				}
			}

			@Override
			public boolean isCancelled() {
				return isCancelled;
			}

			@Override
			public boolean isDone() {
				return isDone;
			}

			@Override
			public U get() throws InterruptedException, ExecutionException {
				if(err!=null) throw new ExecutionException(err);
				return result;
			}

			@Override
			public U get(long timeout, TimeUnit unit)
					throws InterruptedException, ExecutionException, TimeoutException {
				long millis = unit.convert(timeout, TimeUnit.MILLISECONDS);
				long deadline = System.currentTimeMillis()+millis;
				while(!isDone && System.currentTimeMillis() < deadline && err==null) {
					Thread.sleep(20);
				}
				if(err!=null) throw new ExecutionException(err);
				if(isDone)
				return result;
				throw new TimeoutException();
			}

			@Override
			public void addListener(Runnable listener, Executor executor) {
				synchronized (listeners) {
					if(isDone)
						executor.execute(listener);
					else if(!isCancelled) {
						listeners.add(listener);
						executors.add(executor);
					}
				}
			}
			
			protected void markDone(U result) {
				synchronized (listeners) {
					CallableTask.this.result = result;
					isDone = true;
					while(!listeners.isEmpty()) {
						executors.removeFirst().execute(listeners.removeFirst());
					}
				}
			}
			
		}
		
		public CallableTask(Callable<U> c) {
			this.c = c;
			future = new LF();
		}
		
		protected void call() {
			try {
				future.markDone( c.call() );
			} catch (Exception e) {
				err = e;
			}
		}
	}
	
	private static class RunnableTask {
		Runnable r;
		LF future;
		boolean isDone = false;
		boolean isCancelled = false;
		private final LinkedList<Runnable> listeners = new LinkedList<>();
		private final LinkedList<Executor> executors = new LinkedList<>();
		private Throwable err = null;
		private class LF implements ListenableFuture<Void> {
			

			public LF() {
			}

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				synchronized(listeners) {
					return isCancelled = isCancelled || callableTasks.remove(this);
				}
			}

			@Override
			public boolean isCancelled() {
				return isCancelled;
			}

			@Override
			public boolean isDone() {
				return isDone;
			}

			@Override
			public Void get() throws InterruptedException, ExecutionException {
				if(err!=null) throw new ExecutionException(err);
				return null;
			}

			@Override
			public Void get(long timeout, TimeUnit unit)
					throws InterruptedException, ExecutionException, TimeoutException {
				long millis = unit.convert(timeout, TimeUnit.MILLISECONDS);
				long deadline = System.currentTimeMillis()+millis;
				while(!isDone && System.currentTimeMillis() < deadline && err==null) {
					Thread.sleep(20);
				}
				if(err!=null) throw new ExecutionException(err);
				if(isDone)
				return null;
				throw new TimeoutException();
			}

			@Override
			public void addListener(Runnable listener, Executor executor) {
				synchronized (listeners) {
					if(isDone)
						executor.execute(listener);
					else if(!isCancelled) {
						listeners.add(listener);
						executors.add(executor);
					}
				}
			}
			
			protected void markDone() {
				synchronized (listeners) {
					isDone = true;
					while(!listeners.isEmpty()) {
						executors.removeFirst().execute(listeners.removeFirst());
					}
				}
			}
		}
		
		public RunnableTask(Runnable r) {
			this.r = r;
			future = new LF();
		}
		protected void call() {
			try {
				r.run();
				future.markDone();
			} catch (Exception e) {
				err = e;
			}
		}
	}
	
}
