package com.theincgi.advancedMacros.lua.functions.entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.google.common.util.concurrent.ListenableFuture;
import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.event.TaskDispatcher;

import net.minecraft.entity.Entity;

public class GetEntityList extends ZeroArgFunction {
	private Object syncLock = new Object();
	@Override
	public LuaValue call() {
		synchronized (syncLock) {
			final 
			ListenableFuture<LuaTable> f = TaskDispatcher.addTask(new Callable<LuaTable>() {
				@Override
				public LuaTable call() {
					LuaTable table = new LuaTable();
					HashMap<Integer, Boolean> map=new HashMap<>();
					int i = 1;
					
					Iterator<Entity> entities = AdvancedMacros.getMinecraft().world.getAllEntities().iterator();//loadedEntityList;
					for(Entity e = entities.next(); entities.hasNext(); e = entities.next()) {
						if(map.containsKey(e.getEntityId())) {continue;}
						map.put(e.getEntityId(), true);
						LuaTable dat = new LuaTable();
						dat.set("name",  e.getName().getUnformattedComponentText());
						dat.set("id",  e.getEntityId());
						dat.set("class", e.getClass().getName());
						table.set(i++, dat);
					}
					return table;
				}
			});
			while(!f.isDone()) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e1) {
					throw new LuaError("Thread interrupted");
				}
			}
			
			try {
				return f.get();
			} catch (InterruptedException | ExecutionException e) {
				throw new LuaError(e);
			}
		}
	}

}