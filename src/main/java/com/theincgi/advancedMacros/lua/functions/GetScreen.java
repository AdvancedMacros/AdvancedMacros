package com.theincgi.advancedMacros.lua.functions;

import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.google.common.util.concurrent.ListenableFuture;
import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.event.TaskDispatcher;
import com.theincgi.advancedMacros.lua.util.BufferedImageControls;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.ScreenShotHelper;

public class GetScreen extends ZeroArgFunction{
	
	
	@Override
	public LuaValue call() {
		Minecraft mc = AdvancedMacros.getMinecraft();	
		
		ListenableFuture<BufferedImage> futureImage = TaskDispatcher.addTask(new Callable<BufferedImage>() {
			@Override
			public BufferedImage call() throws Exception {
				NativeImage ni = ScreenShotHelper.createScreenshot(mc.getMainWindow().getFramebufferWidth(), mc.getMainWindow().getFramebufferHeight(), mc.getFramebuffer());
				
				return Utils.nativeImageToBufferedImage(ni);
			}
		});
		
		try {
			return new BufferedImageControls(futureImage.get());
		} catch (InterruptedException | ExecutionException e) {
			throw new LuaError("Error occurred getting the screenshot");
		}
	}
}
