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
import net.minecraft.client.settings.AmbientOcclusionStatus;
import net.minecraft.client.settings.CloudOption;
import net.minecraft.client.settings.ParticleStatus;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.util.HandSide;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.Difficulty;

public class MinecraftSettings extends LuaTable {
	public MinecraftSettings() {
		for (OpCode code : OpCode.values()) {
			set(code.name(), new DoOp(code)); //TODO document me
		}
	}
	
	private static final String[] GUI_SCALE = new String[] {"auto", "small", "normal", "large"};
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
				return valueOf(mc.gameSettings.fov);
			case getRenderDistance:
				return valueOf(mc.gameSettings.renderDistanceChunks);
			case getSkinCustomization:{
				Set<PlayerModelPart> s = mc.gameSettings.getModelParts();
				if(args.arg1().isnil()) {
					LuaTable out = new LuaTable();
					for (PlayerModelPart part : PlayerModelPart.values()) {
						out.set(part.name().toLowerCase().replace('_', ' '), false);
					}
					for (PlayerModelPart part : s) {
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
					return valueOf(s.contains(PlayerModelPart.valueOf(key)));
				}
			}
			case getVolume:
				return valueOf(mc.gameSettings.getSoundLevel(SoundCategory.valueOf(args.checkjstring(1).toUpperCase())));
			case isFullscreen:
				return valueOf(mc.mainWindow.isFullscreen());
			case setFov:
				mc.gameSettings.fov = (float) args.checkdouble(1);
				return NONE;
			case setFullscreen:
				if(mc.mainWindow.isFullscreen() != args.optboolean(1, true))
					mc.mainWindow.toggleFullscreen();
				return NONE;
			case setRenderDistance:
				mc.gameSettings.renderDistanceChunks = Math.max(2, Math.min(args.checkint(1), 32));
				return NONE;
			case setVolume:
				mc.gameSettings.setSoundLevel(SoundCategory.valueOf(args.checkjstring(1).toUpperCase()), (float) args.checkdouble(2));
				return NONE;
			case getMaxFps:
				return valueOf(mc.gameSettings.framerateLimit);
			case setMaxFps:
				mc.gameSettings.framerateLimit = Math.max(1, args.checkint(1));
				return NONE;
			case getSmoothLighting:{
				name = mc.gameSettings.ambientOcclusionStatus.name().toLowerCase();
				return valueOf(name);
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
				return valueOf(mc.gameSettings.cloudOption.name().toLowerCase());
			case getDifficulty:
				return valueOf(mc.gameSettings.difficulty.name().toLowerCase());
			case getGuiScale:
				return valueOf(mc.gameSettings.guiScale);
			case getLanguage:
				return valueOf(mc.gameSettings.language);
			case getLanguages:{
				LuaTable langs = new LuaTable();
				for(Language l : mc.getLanguageManager().getLanguages()) {
					langs.set(langs.length()+1, l.getCode());
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
				return valueOf(mc.gameSettings.particles.name().toLowerCase());
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
				return valueOf(mc.gameSettings.vsync);
				
				
			case setAdvancedItemTooltips:
				mc.gameSettings.advancedItemTooltips = args.arg1().checkboolean();
				return NONE;
			case setSmoothLighting:{
				AmbientOcclusionStatus status = AmbientOcclusionStatus.valueOf(args.arg1().checkjstring().toUpperCase());
				if(status == null) throw new LuaError("Invalid option '"+args.arg1().checkjstring()+"'");
				mc.gameSettings.ambientOcclusionStatus = status;
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
					CloudOption co = CloudOption.valueOf( args.arg1().checkjstring().toUpperCase() );
					if(co == null) throw new LuaError("Invalid option '"+args.arg1().checkjstring()+"'");
					mc.gameSettings.cloudOption = co;
				return NONE;
			case setDifficulty:
				mc.gameSettings.difficulty = args.arg1().isnumber()? Difficulty.byId(args.arg1().checkint()) : Difficulty.valueOf(args.arg1().checkjstring().toUpperCase());
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
				//@Deprecated
			case setHeldItemTooltips: 
				mc.gameSettings.heldItemTooltips = args.checkboolean(1);
				return NONE;
			case setInvertMouse:
				mc.gameSettings.invertMouse = args.checkboolean(1);
				return NONE;
			case setLanguage: 
				for(Language l : mc.getLanguageManager().getLanguages()) {
					if(l.getCode().toLowerCase() == args.checkjstring(1).toLowerCase()) {
						mc.gameSettings.language = l.getCode();
						return NONE;
					}
				}
				throw new LuaError("Invalid languge code ("+args.checkjstring(1)+")");
			case setMainHandSide:
				mc.gameSettings.mainHand = HandSide.valueOf(args.checkjstring(1).toUpperCase());
				return NONE;
			case setMipmapLevels:
				mc.gameSettings.mipmapLevels = Utils.clamp(0, args.checkint(1), 4);
				return NONE;
			case setMouseSensitivity:
				mc.gameSettings.mouseSensitivity = (float) Utils.clamp(0.0, args.checkdouble(1), 1.0);
				return NONE;
			case setParticleLevel:
				ParticleStatus s = ParticleStatus.valueOf(args.checkjstring(1).toLowerCase());
				if(s==null) throw new LuaError("Invalid particle level '"+args.checkjstring(1)+"'");
				mc.gameSettings.particles = s;
					return NONE;
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
				mc.gameSettings.vsync = args.checkboolean(1);
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
