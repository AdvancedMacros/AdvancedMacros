package com.theincgi.advancedMacros.asm;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

/*
 * Don't let any access transformer stuff accidentally modify our classes. A list of package prefixes for FML to ignore
 */
@TransformerExclusions({ "com.theincgi.advancedMacros.asm" })
@MCVersion(value = "1.12.2-14.23.1.2587")

public class AdvancedMacrosCorePlugin implements IFMLLoadingPlugin{
	
	@Override
	public String[] getASMTransformerClass() {
		return new String[] { "com.theincgi.advancedMacros.asm.ChatLinesEditThing" };
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

}
