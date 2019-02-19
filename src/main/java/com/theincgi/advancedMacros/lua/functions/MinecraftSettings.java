package com.theincgi.advancedMacros.lua.functions;

import java.util.Set;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.misc.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.Language;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.EnumDifficulty;

public class MinecraftSettings extends LuaTable {
	public MinecraftSettings() {
		for (OpCode code : OpCode.values()) {
			set(code.name(), new DoOp(code)); //TODO document me
		}
	}
	
	private static final String[] CLOUDS_MODE = new String[] {"off","fast","fancy"};
	private static final String[] GUI_SCALE = new String[] {"auto", "small", "normal", "large"};
	private static final String[] PARTICLE_MODE = new String[] {"minimal","decresed","all"};
	private static final String[] PERSPECTIVE = new String[] {"first","front","back"};
	
	private static class DoOp extends VarArgFunction {
		OpCode code;
		public DoOp(OpCode code) {
			this.code = code;
		}
		@Override
		public Varargs invoke(Varargs args) {
			Minecraft mc = AdvancedMacros.getMinecraft();
			switch (code) {
			case getFov:
				return valueOf(mc.gameSettings.fovSetting);
			case getRenderDistance:
				return valueOf(mc.gameSettings.renderDistanceChunks);
			case getSkinCustomization:{
				Set<EnumPlayerModelParts> s = mc.gameSettings.getModelParts();
				if(args.arg1().isnil()) {
					LuaTable out = new LuaTable();
					for (EnumPlayerModelParts part : EnumPlayerModelParts.values()) {
						out.set(part.name().toLowerCase().replace('_', ' '), false);
					}
					for (EnumPlayerModelParts part : s) {
						out.set(part.name().toLowerCase().replace('_', ' '), true);
					}
				}else {
					String key = args.checkjstring(1);
					if(key.equals("helmet"))
						key = "hat";
					else if(key.equals("jacket"))
						key = "chest";
					key = key.toUpperCase();
					key = key.replace(' ', '_');
					return valueOf(s.contains(EnumPlayerModelParts.valueOf(key)));
				}
			}
			case getVolume:
				return valueOf(mc.gameSettings.getSoundLevel(SoundCategory.getByName(args.checkjstring(1).toUpperCase())));
			case isFullscreen:
				return valueOf(mc.isFullScreen());
			case setFov:
				mc.gameSettings.fovSetting = (float) args.checkdouble(1);
				return NONE;
			case setFullscreen:
				if(mc.isFullScreen() != args.optboolean(1, true))
					mc.toggleFullscreen();
				return NONE;
			case setRenderDistance:
				mc.gameSettings.renderDistanceChunks = Math.max(2, Math.min(args.checkint(1), 32));
				return NONE;
			case setVolume:
				mc.gameSettings.setSoundLevel(SoundCategory.getByName(args.checkjstring(1)), (float) args.checkdouble(2));
				return NONE;
			case getMaxFps:
				return valueOf(mc.gameSettings.limitFramerate);
			case setMaxFps:
				mc.gameSettings.limitFramerate = Math.max(1, args.checkint(1));
				return NONE;
			case getSmoothLighting:{
				String mode = "INVALID";
				switch(mc.gameSettings.ambientOcclusion){
				case 0: mode = "off";    break;
				case 1: mode = "minimum";break;
				case 2: mode = "maximum";break;
				}
				return valueOf(mode);
			}
			case getChatHeightFocused:
				return valueOf(mc.gameSettings.chatHeightFocused);
			case getChatHeightUnfocused:
				return valueOf(mc.gameSettings.chatHeightUnfocused);
			case getChatOpacity:
				return valueOf(mc.gameSettings.chatOpacity);
			case getChatScale:
				return valueOf(mc.gameSettings.chatScale);
			case getChatWidth:
				return valueOf(mc.gameSettings.chatWidth);
			case getCloudsMode:
				return valueOf(CLOUDS_MODE[mc.gameSettings.clouds]);
			case getDifficulty:
				return valueOf(mc.gameSettings.difficulty.name().toLowerCase());
			case getGuiScale:
				return valueOf(mc.gameSettings.guiScale);
			case getLanguage:
				return valueOf(mc.gameSettings.language);
			case getLanguages:{
				LuaTable langs = new LuaTable();
				for(Language l : mc.getLanguageManager().getLanguages()) {
					langs.set(langs.length()+1, l.getLanguageCode());
				}
			}
			case getLastServer:
				return valueOf(mc.gameSettings.lastServer);
			case getMainHandSide:
				return valueOf(mc.gameSettings.mainHand.name().toLowerCase());
			case getMipmapLevels:
				return valueOf(mc.gameSettings.mipmapLevels);
			case getMouseSensitivity:
				return valueOf(mc.gameSettings.mouseSensitivity);
			case getParticleLevel:
				return valueOf(PARTICLE_MODE[mc.gameSettings.particleSetting]);
			case getPerspective:
				return valueOf(PERSPECTIVE[mc.gameSettings.thirdPersonView]);
				
				
			case isAdvancedItemTooltips:
				return valueOf(mc.gameSettings.advancedItemTooltips);
			case isAutoJump:
				return valueOf(mc.gameSettings.autoJump);
			case isEntityShadows:
				return valueOf(mc.gameSettings.entityShadows);
			case isFancyGraphics:
				return valueOf(mc.gameSettings.fancyGraphics);
			case isHeldItemTooltips:
				return valueOf(mc.gameSettings.heldItemTooltips);
			case isInvertMouse:
				return valueOf(mc.gameSettings.invertMouse);
			case isPauseOnLostFocus:
				return valueOf(mc.gameSettings.pauseOnLostFocus);
			case isSmoothCamera:
				return valueOf(mc.gameSettings.smoothCamera);
			case isTouchscreenMode:
				return valueOf(mc.gameSettings.touchscreen);
			case isViewBobbing:
				return valueOf(mc.gameSettings.viewBobbing);
			case isVsync:
				return valueOf(mc.gameSettings.enableVsync);
				
				
			case setAdvancedItemTooltips:
				mc.gameSettings.advancedItemTooltips = args.arg1().checkboolean();
				return NONE;
			case setSmoothLighting:{
				switch (args.arg1().checkjstring().toLowerCase()) {
				case "off":
					mc.gameSettings.ambientOcclusion = 0; break;
				case "min":
				case "minimum":
					mc.gameSettings.ambientOcclusion = 1; break;
				case "max":
				case "maximum":
					mc.gameSettings.ambientOcclusion = 2; break;
				}
				return NONE;
			}
			case setAutoJump:
				mc.gameSettings.autoJump = args.arg1().checkboolean();
				return NONE;
				
			case setChatHeightFocused:
				mc.gameSettings.chatHeightFocused = (float) Utils.clamp(0.0, args.arg1().checkdouble(), 1.0);
				return NONE;
				
			case setChatHeightUnfocused:
				mc.gameSettings.chatHeightUnfocused = (float) Utils.clamp(0.0, args.arg1().checkdouble(), 1.0);
				return NONE;
				
			case setChatOpacity:
				mc.gameSettings.chatOpacity = (float) Utils.clamp(0, args.arg1().checkdouble(), 1);
				return NONE;
				
			case setChatScale:
				mc.gameSettings.chatScale = (float) Utils.clamp(0, args.arg1().checkdouble(), 1);
				return NONE;
				
			case setChatWidth:
				mc.gameSettings.chatWidth = (float) Utils.clamp(0, args.arg1().checkdouble(), 1);
				return NONE;
				
			case setCloudsMode:
				switch(args.arg1().checkjstring().toLowerCase()) {
				case "off":
					mc.gameSettings.clouds = 0; break;
				case "fast":
					mc.gameSettings.clouds = 1; break;
				case "fancy":
					mc.gameSettings.clouds = 2; break;
				default:
					throw new LuaError("Invalid mode [off/fast/fancy]");
				}
				return NONE;
			case setDifficulty:
				mc.gameSettings.difficulty = args.arg1().isnumber()? EnumDifficulty.byId(args.arg1().checkint()) : EnumDifficulty.valueOf(args.arg1().checkjstring().toUpperCase());
				return NONE;
			case setEntityShadows:
				mc.gameSettings.entityShadows = args.arg1().checkboolean();
				return NONE;
			case setFancyGraphics:
				mc.gameSettings.fancyGraphics = args.checkboolean(1);
				return NONE;
			case setGuiScale:
				switch (args.checkjstring(1).toLowerCase()) {
				case "auto":
					mc.gameSettings.guiScale = 0; break;
				case "small":
					mc.gameSettings.guiScale = 1; break;
				case "normal":
					mc.gameSettings.guiScale = 2; break;
				case "large":
					mc.gameSettings.guiScale = 3; break;
				default:
					throw new LuaError("Invalid scale [auto/small/normal/large]");
				}
				return NONE;
			case setHeldItemTooltips:
				mc.gameSettings.advancedItemTooltips = args.checkboolean(1);
				return NONE;
			case setInvertMouse:
				mc.gameSettings.invertMouse = args.checkboolean(1);
				return NONE;
			case setLanguage: 
				for(Language l : mc.getLanguageManager().getLanguages()) {
					if(l.getLanguageCode().toLowerCase() == args.checkjstring(1).toLowerCase()) {
						mc.gameSettings.language = l.getLanguageCode();
						return NONE;
					}
				}
				throw new LuaError("Invalid languge code ("+args.checkjstring(1)+")");
			case setMainHandSide:
				mc.gameSettings.mainHand = EnumHandSide.valueOf(args.checkjstring(1).toUpperCase());
				return NONE;
			case setMipmapLevels:
				mc.gameSettings.mipmapLevels = Utils.clamp(0, args.checkint(1), 4);
				return NONE;
			case setMouseSensitivity:
				mc.gameSettings.mouseSensitivity = (float) Utils.clamp(0.0, args.checkdouble(1), 1.0);
				return NONE;
			case setParticleLevel:
				switch (args.checkjstring(1).toLowerCase()) {
				case "min":
				case "minimal":
				case "minimum":
					mc.gameSettings.particleSetting = 0;
					return NONE;
				case "decresed":
				case "some":
					mc.gameSettings.particleSetting = 1;
					return NONE;
				case "max":
				case "all":
				case "maximum":
					mc.gameSettings.particleSetting = 2;
					return NONE;
				default:
					throw new LuaError("Unknown particle level '"+args.checkstring(1)+"' (use min/some/max or minimal/decresed/all)");
				}
			case setPauseOnLostFocus:
				mc.gameSettings.pauseOnLostFocus = args.checkboolean(1);
				return NONE;
			case setSmoothCamera:
				mc.gameSettings.smoothCamera = args.checkboolean(1);
				return NONE;
			case setPerspective:
				switch (args.checkjstring(1).toLowerCase()) {
				case "first":
					mc.gameSettings.thirdPersonView = 0;
					break;
				case "front":
					mc.gameSettings.thirdPersonView = 1;
					break;
				case "back":
					mc.gameSettings.thirdPersonView = 2;
					break;

				default:
					throw new LuaError("Unknown perspective '"+args.checkjstring(1)+"' (use first/front/back)");
				}
				return NONE;
			case setTouchscreenMode:
				mc.gameSettings.touchscreen = args.checkboolean(1);
				return NONE;
			case setViewBobbing:
				mc.gameSettings.viewBobbing = args.checkboolean(1);
				return NONE;
			case setVsync:
				mc.gameSettings.enableVsync = args.checkboolean(1);
				return NONE;
			default:
				throw new LuaError("Undefined op "+code.name());
			}
		}
	}

	private enum OpCode {
		getFov,
		setFov,
		getVolume,
		setVolume,
		setRenderDistance,
		getRenderDistance,
		setFullscreen,
		isFullscreen,
		getSkinCustomization,
		getMaxFps,
		setMaxFps,
		setAdvancedItemTooltips,
		isAdvancedItemTooltips,
		getSmoothLighting,
		setSmoothLighting,
		setAutoJump,
		isAutoJump,
		getChatOpacity,
		setChatOpacity,
		getChatScale,
		setChatScale,
		getChatHeightFocused,
		setChatHeightFocused,
		getChatHeightUnfocused,
		setChatHeightUnfocused,
		setChatWidth,
		getChatWidth,
		setCloudsMode,
		getCloudsMode,
		setDifficulty,
		getDifficulty,
		setVsync,
		isVsync,
		setEntityShadows,
		isEntityShadows,
		setFancyGraphics,
		isFancyGraphics,
		setGuiScale,
		getGuiScale,
		setHeldItemTooltips,
		isHeldItemTooltips,
		setInvertMouse,
		isInvertMouse,
		getLanguage,
		getLanguages,
		setLanguage,
		getLastServer,
		getMainHandSide,
		setMainHandSide,
		getMipmapLevels,
		setMipmapLevels,
		getMouseSensitivity,
		setMouseSensitivity,
		setParticleLevel,
		getParticleLevel,
		setPauseOnLostFocus,
		isPauseOnLostFocus,
		setSmoothCamera,
		isSmoothCamera,
		setPerspective,
		getPerspective,
		setTouchscreenMode,
		isTouchscreenMode,
		setViewBobbing,
		isViewBobbing,
		
	}
}
