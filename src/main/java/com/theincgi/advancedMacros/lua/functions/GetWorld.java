package com.theincgi.advancedMacros.lua.functions;

import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;

import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraftforge.fml.network.NetworkHooks;

public class GetWorld extends ZeroArgFunction {

	@Override
	public LuaValue call() {
		try {
			World world = AdvancedMacros.getMinecraft().player.getEntityWorld();
			return worldToTable(world);
		}catch(NullPointerException npe) {
			return LuaValue.FALSE;
		}
		
	}

	private static void set(LuaTable t, String string, WorldBorder worldBorder) {
		if(worldBorder==null) {
			t.set(string, LuaValue.FALSE);
			return;
		}
		LuaTable b = new LuaTable();
		b.set("centerX", worldBorder.getCenterX());
		b.set("centerZ", worldBorder.getCenterZ());
		b.set("warningDist", worldBorder.getWarningDistance());
		b.set("dmgPerBlock", worldBorder.getDamagePerBlock());
		b.set("dmgBuffer", worldBorder.getDamageBuffer());
		b.set("radius", worldBorder.getDiameter()/2);
		b.set("size", worldBorder.getSize());
		t.set(string, b);
	}

	private static void set(LuaTable t, String string, long seed) {
		t.set(string, seed);
	}
	private static void set(LuaTable t, String string, BlockPos spawnPoint) {
		LuaTable p = new LuaTable();
		p.set(1, LuaValue.valueOf(spawnPoint.getX()));
		p.set(2, LuaValue.valueOf(spawnPoint.getY()));
		p.set(3, LuaValue.valueOf(spawnPoint.getZ()));
		t.set(string, p);
	}
	private static void set(LuaTable t, String string, boolean difficultyLocked) {
		t.set(string, LuaValue.valueOf(difficultyLocked));
	}
	private static void set(LuaTable t, String string, String name) {
		t.set(string, name);
	}
	private static void set(LuaTable t, String string, int arg) {
		t.set(string, LuaValue.valueOf(arg));
	}
	
	public static LuaTable worldToTable(World world) {
		LuaTable t = new LuaTable();
		set(t, "isRemote", world.isRemote);
		set(t, "isDaytime",world.isDaytime());
		set(t, "worldTime", world.getGameTime()); //TESTME
		{
			String stat;
			if(world.isRaining() && world.isThundering()){
				stat = "thunder";
			}else if(world.isRaining() && !world.isThundering()){
				stat = "rain";
			}else if(!world.isRaining() && world.isThundering()){
				stat = "only thunder";
			}else{
				stat = "clear";
			}
			set(t, "weather", stat);
		}
		set(t, "moonPhase", world.getMoonPhase());
		set(t, "clearWeatherTime", world.getWorldInfo().getClearWeatherTime());
		set(t, "rainTime", world.getWorldInfo().getRainTime());
		set(t, "gameType", world.getWorldInfo().getGameType().getName());
		set(t, "seed", world.getSeed());
		set(t, "difficulty", world.getDifficulty().name().toLowerCase());
		
		set(t, "name", world.getWorldInfo().getWorldName());
		set(t, "spawn", world.getSpawnPoint());
		set(t, "border", world.getWorldBorder());
		set(t, "isHardcore", world.getWorldInfo().isHardcore());
		set(t, "isDifficultyLocked", world.getWorldInfo().isDifficultyLocked());
		set(t, "isSinglePlayer", AdvancedMacros.getMinecraft().isSingleplayer());
		IntegratedServer is = AdvancedMacros.getMinecraft().getIntegratedServer();
		set(t, "isLanHost", is!=null && is.getPublic());
		set(t, "connectionType", NetworkHooks.getConnectionType(()->AdvancedMacros.getMinecraft().getConnection().getNetworkManager()).name() ); //yeilded modded
		if(AdvancedMacros.getMinecraft().getCurrentServerData()!=null){
			ServerData sd = AdvancedMacros.getMinecraft().getCurrentServerData();
			if(sd!=null) {
				t.set("serverName", sd.serverName==null? LuaValue.FALSE    : LuaValue.valueOf(sd.serverName));
				t.set("messageOfTheDay", sd.serverMOTD==null? LuaValue.FALSE    : LuaValue.valueOf(sd.serverMOTD));
				t.set("ip", sd.serverIP==null?   LuaValue.FALSE    : LuaValue.valueOf(sd.serverIP));
			}
		}
		return t;
	}
}