package com.theincgi.advancedMacros.lua.functions;

import java.util.HashMap;
import java.util.List;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

import com.google.common.util.concurrent.ListenableFuture;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

public class GetEntityList extends ZeroArgFunction {
	private Object syncLock = new Object();
	@Override
	public LuaValue call() {
		synchronized (syncLock) {
			final LuaTable table = new LuaTable();
			ListenableFuture<Object> f = Minecraft.getMinecraft().addScheduledTask(new Runnable() {
				@Override
				public void run() {
					HashMap<Integer, Boolean> map=new HashMap<>();
					int i = 1;
					List<Entity> entities = Minecraft.getMinecraft().world.loadedEntityList;
					for(int j = 0; j<entities.size(); j++) {
						Entity e = entities.get(j);
						if(map.containsKey(e.getEntityId())) {continue;}
						map.put(e.getEntityId(), true);
						LuaTable dat = new LuaTable();
						dat.set("name",  e.getName());
						dat.set("id",  e.getEntityId());
						dat.set("class", e.getClass().getName());
						table.set(i++, dat);
					}
				}
			});
			while(!f.isDone()) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e1) {
					throw new LuaError("Thread interrupted");
				}
			}
			
			return table;
		}
	}

}