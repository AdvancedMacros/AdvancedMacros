package com.theincgi.advancedmacros.misc;

public class SimpleListenableFuture<T> {
	
	T result;
	boolean done;
	Throwable t;
	
	public SimpleListenableFuture() {
	}
	
	public void setResult(T result) {
		synchronized (this) {
			done = true;
			this.result = result;
			notifyAll();
		}
	}
	
	public T waitForResult() throws InterruptedException {
		synchronized (this) {
			if(!done)
				this.wait();
		}
		if(t != null)
			throw new RuntimeException(t);
		return result;
	}
}
