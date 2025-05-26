package com.theincgi.advancedmacros.event;

import static com.theincgi.advancedmacros.guiScripts.BindingsMenu.getBindingsMenu;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.luaj.vm2_v3_0_1.Varargs;

import com.theincgi.advancedmacros.lua.LuaDebug.JavaThread;
import com.theincgi.advancedmacros.lua.LuaDebug.OnScriptFinish;
import com.theincgi.advancedmacros.misc.Settings;
import com.theincgi.advancedmacros.misc.Utils;

abstract public class FilterEvent<T extends Event> extends EventHandler<T> {
	
	private static boolean ENABLE_FILTER_EVENTS = false; //toggle for dev
	
	//used to maintain order of events
	protected ConcurrentLinkedQueue<EventProcessor> events = new ConcurrentLinkedQueue<>();
	protected Thread workerThread;
	
	public FilterEvent() {
		workerThread = new Thread(this::handleCompletions, "event queue manager");
	}
	
	public boolean hasBindings(T event) {
		return getBindingsMenu().hasBindingsForEvent(EVENT_TYPE, getEventValue(event), false);
	}
	
	public boolean isCanceled(Varargs result) {
		return !result.toboolean(1);
	}
	
	public void onEvent(T event) {
		if(hasBindings(event) && event.isCancelable() && ENABLE_FILTER_EVENTS) {
			event.cancel();
			
			try {
				new EventProcessor(event) {
					Varargs task() {
						var eventArgs = event.createArgsTable();					
						try {
							return getBindingsMenu().triggerEvent(EVENT_TYPE, getEventValue(event), eventArgs).waitForResult();
						} catch (InterruptedException e) {
							e.printStackTrace();
							return eventArgs;
						}
					}
				};
			} catch(Exception e) {
				Utils.logError(e);
			}
		} else {
			getBindingsMenu().triggerEvent(EVENT_TYPE, getUnfilteredEventName(), event.createArgsTable());
		}
	}
	
	public void triggerBindingsForUnfilteredListeners(T event, EventProcessor processor) {
		var args = event.createArgsTable();
		
		if(processor.result != null) {
			for(int i = 1, j = args.length()+1; i <= processor.result.narg(); i++, j++) {
				args.set(j, processor.result.arg(i));
			}
		}
		
		new JavaThread(()->{
			getBindingsMenu().triggerEvent(EVENT_TYPE, getUnfilteredEventName(), args);			
		}).start();
	}
	
	private void handleCompletions() {
		while(true) {
			synchronized (events) {
				if(events.isEmpty())
					try {
						events.wait(); //will receive notification on task completion
					} catch (InterruptedException e) {
						e.printStackTrace();
						continue;
					}
				var first = events.poll();
				synchronized (first) {
					if(!first.done)
						try {
							first.wait(EventProcessor.getTimeoutDuration());
						} catch (InterruptedException e) {
							e.printStackTrace();
							continue;
						}
					if(first.canceled)
						continue;
					if(!first.done) {
						first.killTask();
						continue;
					}
					TaskDispatcher.addTask(()->{
						fireEvent(first);						
					});
				}
			}
		}
	}
	
	/**Re-queue event in a way that won't also retrigger the event handler*/
	abstract public void fireEvent(FilterEvent<T>.EventProcessor event);
	
	abstract public String getUnfilteredEventName();
	
	public abstract class EventProcessor {
		private JavaThread thread;
		public boolean canceled = false;
		public T event;
		public boolean done = false;
		public Varargs result;
		
		public EventProcessor(T event) {
			this.event = event;
			this.thread = new JavaThread(this::task);
			events.add(this);
			this.thread.start(new OnScriptFinish() {
				@Override public void onFinish(Varargs v) {
					onResult(v);
				}
			});
		}
		
		abstract Varargs task();
		
		public static long getTimeoutDuration() {
			var settings = Settings.settings;
			if(settings.get("eventTimeout").isnil())
	        	settings.set("eventTimeout", 3000);
			return Settings.settings.get("eventTimeout").checklong();
		}
		
		public void killTask() {
			thread.stop();
		}
		
		private void onResult(Varargs v) {
			canceled = isCanceled(v);
			
			synchronized (this) {
				done = true;
				notifyAll(); //notify completions manager thread
			}
		}
		
	}
}
