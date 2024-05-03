package com.theincgi.advancedmacros.lua.functions;
import com.theincgi.advancedmacros.AdvancedMacros;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ThreeArgFunction;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

public class GetBlockName extends ThreeArgFunction{
    @Override
    public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
        int x = (arg1.checkint()), y = arg2.checkint(), z = (arg3.checkint());
        BlockPos pos = new BlockPos(x,y,z);

        if (AdvancedMacros.getMinecraft().world == null) return LuaValue.valueOf("");
        Chunk chunk = AdvancedMacros.getMinecraft().world.getChunk(pos);

        if(chunk == null ) return LuaValue.valueOf("");

        Block block = chunk.getBlockState(pos).getBlock();
        return LuaValue.valueOf(block.getName().getString());
    }
}
