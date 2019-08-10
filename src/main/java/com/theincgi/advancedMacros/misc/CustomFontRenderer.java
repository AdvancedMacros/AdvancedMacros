package com.theincgi.advancedMacros.misc;

import com.mojang.blaze3d.platform.GlStateManager;
import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.gui.Color;
import com.theincgi.advancedMacros.lua.LuaValTexture;
import com.theincgi.advancedMacros.misc.Matrix.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class CustomFontRenderer {
	static LuaValTexture consolas = Utils.checkTexture(Settings.getTextureID("resource:consolas.png"));
	static LuaValTexture consolas_bold = Utils.checkTexture(Settings.getTextureID("resource:consolas_bold.png"));
	static LuaValTexture consolas_italics = Utils.checkTexture(Settings.getTextureID("resource:consolas_italic.png"));
	static LuaValTexture consolas_bold_italics = Utils.checkTexture(Settings.getTextureID("resource:consolas_bold_italic.png"));
	Minecraft mc = AdvancedMacros.getMinecraft();
	private UVPair uvPair = new UVPair();
	//private ResourceLocation ascii = new ResourceLocation("textures/font/ascii.png");
	static final int charWid = 13,charHei = 29, vGap=1, imgWid=256, imgHei=256;
	public CustomFontRenderer() {
	}

	
	//TODO get char measurements for user
	
	public float textSize3d = .25f; //.25 of a block as the height
	public float textSize2d = 12; //12 pixels tall
	/**Call during the 3d drawing, angles are in degrees*/
	public void renderText(double playerX, double playerY, double playerZ, double x, double y, double z, float yaw, float pitch, float roll, String text, float opacity) {
		boolean bold = false, italics = false;
		double tx = x - playerX;
		double ty = y - playerY;
		double tz = z - playerZ;
		float ratio = charWid/(float)charHei;
		Matrix upVect = Matrix.vector(0, textSize3d, 0);
		Matrix rightVect = Matrix.vector(textSize3d*ratio, 0 , 0);
		Matrix offset = Matrix.vector(0, 0, 0);
		//yaw pitch roll y, x, z
		//reverse order
		
		//pitch
		{
			float p = (float) Math.toRadians(pitch);

			rightVect = rightVect.rotate(Axis.X, p);
			upVect = upVect.rotate(Axis.X, p);
		}
		//roll

				{
					float r = (float) Math.toRadians(roll);
					rightVect = rightVect.rotate(Axis.Z, r);
					upVect = upVect.rotate(Axis.Z, r);
				}
		//yaw
		{
			float ya = (float) Math.toRadians(yaw);
			rightVect = rightVect.rotate(Axis.Y, ya);
			upVect = upVect.rotate(Axis.Y, ya);
		}
		double dx=tx,dy=ty,dz=tz;
		int lineNum = 0;
		mc.getTextureManager().bindTexture(consolas.getResourceLocation());
		//System.out.printf("TX: %5.2f TY: %5.2f TZ: %5.2f   UP: %s RIGHT: %s\n",tx,ty,tz,upVect,rightVect);
		for(int i = 0; i<text.length(); i++) {
			char c = text.charAt(i);
			//System.out.println(c);
			if(c=='\n') {
				dx=tx;
				dy=ty;
				dz=tz;
				lineNum++;
				Matrix down = upVect.scalar(-lineNum);
				dx+=down.vectorX();
				dy+=down.vectorY();
				dz+=down.vectorZ();
			}else if(c=='&' && i<text.length()-1 && (Utils.isTextColorCode(text.charAt(i+1))||Utils.isTextStyleCode(text.charAt(i+1)))) {
				if(Utils.isTextColorCode(text.charAt(i+1)))
					mc.getTextureManager().bindTexture(consolas.getResourceLocation());
				char code = text.charAt(i+1);
				//System.out.println(code);
				switch (code) {
				case '0':
					italics=false;
					bold=false;
					setColor(Color.TEXT_0, opacity);
					break;
				case '1':
					italics=false;
					bold=false;
					setColor(Color.TEXT_1, opacity);
					break;
				case '2':
					italics=false;
					bold=false;
					setColor(Color.TEXT_2, opacity);
					break;
				case '3':
					italics=false;
					bold=false;
					setColor(Color.TEXT_3, opacity);
					break;
				case '4':
					italics=false;
					bold=false;
					setColor(Color.TEXT_4, opacity);
					break;
				case '5':
					italics=false;
					bold=false;
					setColor(Color.TEXT_5, opacity);
					break;
				case '6':
					italics=false;
					bold=false;
					setColor(Color.TEXT_6, opacity);
					break;
				case '7':
					italics=false;
					bold=false;
					setColor(Color.TEXT_7, opacity);
					break;
				case '8':
					italics=false;
					bold=false;
					setColor(Color.TEXT_8, opacity);
					break;
				case '9':
					italics=false;
					bold=false;
					setColor(Color.TEXT_9, opacity);
					break;
				case 'a':
					italics=false;
					bold=false;
					setColor(Color.TEXT_a, opacity);
					break;
				case 'b':
					italics=false;
					bold=false;
					setColor(Color.TEXT_b, opacity);
					break;
				case 'c':
					italics=false;
					bold=false;
					setColor(Color.TEXT_c, opacity);
					break;
				case 'd':
					italics=false;
					bold=false;
					setColor(Color.TEXT_d, opacity);
					break;
				case 'e':
					italics=false;
					bold=false;
					setColor(Color.TEXT_e, opacity);
					break;
				case 'f':
					italics=false;
					bold=false;
					setColor(Color.TEXT_f, opacity);
					break;
				case 'B':
					if(italics)
						mc.getTextureManager().bindTexture(consolas_bold_italics.getResourceLocation());
					else
						mc.getTextureManager().bindTexture(consolas_bold.getResourceLocation());
					bold=true;
					break;
				case 'I':
					if(bold)
						mc.getTextureManager().bindTexture(consolas_bold_italics.getResourceLocation());
					else
						mc.getTextureManager().bindTexture(consolas_italics.getResourceLocation());
					italics=true;
					break;
				}
				i++;
			}else {
				drawChar3D(dx,dy,dz , upVect, rightVect, c);
				dx-=rightVect.vectorX();
				dy-=rightVect.vectorY();
				dz-=rightVect.vectorZ();
			}
		}
	}
	private void setColor(Color color, float opaicty) {
		GlStateManager.color4f(color.getR()/255f, color.getG()/255f, color.getB()/255f, opaicty);
	}
	public void renderText(double x, double y, float z, String text, float opacity, float textSize2d) {
		boolean bold = false, italics = false;
		double tx = x;
		double ty = y;
		
		float ratio = charWid/(float)charHei;
		Matrix upVect = Matrix.vector(0, -textSize2d, 0);
		Matrix rightVect = Matrix.vector(-textSize2d*ratio, 0 , 0);
		Matrix offset = Matrix.vector(0, 0, 0);
		
		double dx=tx,dy=ty;
		int lineNum = 0;
		mc.getTextureManager().bindTexture(consolas.getResourceLocation());
		//System.out.printf("TX: %5.2f TY: %5.2f TZ: %5.2f   UP: %s RIGHT: %s\n",tx,ty,tz,upVect,rightVect);
		for(int i = 0; i<text.length(); i++) {
			char c = text.charAt(i);
			//System.out.println(c);
			if(c=='\n') {
				dx=tx;
				dy=ty;
				lineNum++;
				Matrix down = upVect.scalar(-lineNum);
				dx+=down.vectorX();
				dy+=down.vectorY();
			}else if(c=='&' && i<text.length()-1 && ( text.charAt(i+1)=='&' || Utils.isTextColorCode(text.charAt(i+1)) || Utils.isTextStyleCode(text.charAt(i+1)))) {
				if(Utils.isTextColorCode(text.charAt(i+1))) //reset to normal text
					mc.getTextureManager().bindTexture(consolas.getResourceLocation());
				char code = text.charAt(i+1);
				//System.out.println(code);
				switch (code) {
				case '0':
					italics=false;
					bold=false;
					setColor(Color.TEXT_0, opacity);
					break;
				case '1':
					italics=false;
					bold=false;
					setColor(Color.TEXT_1, opacity);
					break;
				case '2':
					italics=false;
					bold=false;
					setColor(Color.TEXT_2, opacity);
					break;
				case '3':
					italics=false;
					bold=false;
					setColor(Color.TEXT_3, opacity);
					break;
				case '4':
					italics=false;
					bold=false;
					setColor(Color.TEXT_4, opacity);
					break;
				case '5':
					italics=false;
					bold=false;
					setColor(Color.TEXT_5, opacity);
					break;
				case '6':
					italics=false;
					bold=false;
					setColor(Color.TEXT_6, opacity);
					break;
				case '7':
					italics=false;
					bold=false;
					setColor(Color.TEXT_7, opacity);
					break;
				case '8':
					italics=false;
					bold=false;
					setColor(Color.TEXT_8, opacity);
					break;
				case '9':
					italics=false;
					bold=false;
					setColor(Color.TEXT_9, opacity);
					break;
				case 'a':
					italics=false;
					bold=false;
					setColor(Color.TEXT_a, opacity);
					break;
				case 'b':
					italics=false;
					bold=false;
					setColor(Color.TEXT_b, opacity);
					break;
				case 'c':
					italics=false;
					bold=false;
					setColor(Color.TEXT_c, opacity);
					break;
				case 'd':
					italics=false;
					bold=false;
					setColor(Color.TEXT_d, opacity);
					break;
				case 'e':
					italics=false;
					bold=false;
					setColor(Color.TEXT_e, opacity);
					break;
				case 'f':
					italics=false;
					bold=false;
					setColor(Color.TEXT_f, opacity);
					break;
				case 'B':
					if(italics)
						mc.getTextureManager().bindTexture(consolas_bold_italics.getResourceLocation());
					else
						mc.getTextureManager().bindTexture(consolas_bold.getResourceLocation());
					bold=true;
					break;
				case 'I':
					if(bold)
						mc.getTextureManager().bindTexture(consolas_bold_italics.getResourceLocation());
					else
						mc.getTextureManager().bindTexture(consolas_italics.getResourceLocation());
					italics=true;
					break;
				case '&':
					drawChar2D(dx, dy, z, c, textSize2d*ratio, textSize2d);
					dx-=rightVect.vectorX();
					dy-=rightVect.vectorY();
					break;
				}
				i++;
			}else {
				drawChar2D(dx, dy, z, c, textSize2d*ratio, textSize2d);
				dx-=rightVect.vectorX();
				dy-=rightVect.vectorY();
			}
		}
	}
	private void drawChar2D(double x, double y, float z, char c, float wide, float tall) {
		loadUV(c, uvPair);
		
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(x    	, y     	, z     	).tex(uvPair.umin, uvPair.vmin).endVertex(); //bottom left
		buffer.pos(x	    , y+tall	, z 		).tex(uvPair.umin, uvPair.vmax).endVertex(); //top left
		buffer.pos(x+wide	, y+tall	, z			).tex(uvPair.umax, uvPair.vmax).endVertex(); //top right
		buffer.pos(x+wide	, y     	, z			).tex(uvPair.umax, uvPair.vmin).endVertex(); //bottom right
		Tessellator.getInstance().draw();
	}

	private void drawChar3D(double x, double y, double z, Matrix upVect, Matrix rightVect, char c) {

		loadUV(c, uvPair);
		double 	   ux = upVect.vectorX(),
				uy = upVect.vectorY(),
				uz = upVect.vectorZ(),
				rx = rightVect.vectorX(),
				ry = rightVect.vectorY(),
				rz = rightVect.vectorZ();
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(x    	, y     	, z     	).tex(uvPair.umax, uvPair.vmax).endVertex(); //bottom left
		buffer.pos(x+ux     , y+uy		, z+uz 		).tex(uvPair.umax, uvPair.vmin).endVertex(); //top left
		buffer.pos(x+ux+rx	, y+uy+ry	, z+uz+rz	).tex(uvPair.umin, uvPair.vmin).endVertex(); //top right
		buffer.pos(x+rx		, y+ry     	, z+rz		).tex(uvPair.umin, uvPair.vmax).endVertex(); //bottom right
		Tessellator.getInstance().draw();
	}

	private static void loadUV(char c, UVPair uv) {
		int s = spot(c);
		int x = s%18;
		int y = s/18;
		uv.umin = x*14;
		uv.vmin = y*(charHei+1)+5;
		uv.umax = uv.umin+charWid;
		uv.vmax = uv.vmin+charHei+5;

		uv.umin /= imgWid;
		uv.umax /= imgWid;
		uv.vmin /= imgHei;
		uv.vmax /= imgHei;
	}
	private static int spot(char c) {
		if(c>=32 && c<=126) 
			return c-32;
		switch (c) {
		case 0xB0://degree sign
			return 95;
		case 0x2022://Bullet Point
			return 96;
		case 0x2122://TM
			return 97;
		case 0x2190://left
			return 98;
		case 0x2191://up
			return 99;
		case 0x2192://right
			return 100;
		case 0x2193://down
			return 101;
		case 0x2194://horz
			return 102;
		case 0x2195://vert
			return 103;
		case 0x2211://sum
			return 104;
		case 0x2206://delta
			return 105;
		case 0x221a://sqrt
			return 106;
		case 0x2248://aprox
			return 107;
		case 0x2260://neq
			return 108;
		case 0x2591://light shade
			return 109;
		case 0x2592://med shade
			return 110;
		case 0x2593://dark shade
			return 111;
		case 0x250c://L Bar Top left
			return 112;
		case 0x2502://Bar Vert
			return 113;
		case 0x2510://L bar topRight
			return 114;
		case 0x2514://L bar bottmLeft
			return 115;
		case 0x2518://L bar bottomRight
			return 116;
		case 0x251c://T bar left
			return 117;
		case 0x2524://T bar right
			return 118;
		case 0x252c://T bar up
			return 119;
		case 0x2534://T bar down
			return 120;
		case 0x253c://+ bar
			return 121;
		case 0x2588://solid box
			return 122;
		case 0x25b2://Tri up
			return 123;
			//case 0x25b6://tri right
			//	return 124;
		case 0x25bc://tri down
			return 124;
			//case 0x25c0://tri left
			//	return 126;
		case 0x2640://female
			return 125;
		case 0x2642://male
			return 126;
		case 0x2660://spade
			return 127;
		case 0x2663://club
			return 128;
		case 0x2665://heart
			return 129;
		case 0x2666://diamond
			return 130;
		case 0x266a://note
			return 131;
			//case 0x266c://note2
			//	return 134;
		default:
			return 0;
		}
	}


	class UVPair{
		float umin,umax,vmin,vmax;
	}

	private static final float ratio = charWid/(float)charHei;
//	public float getTextRatio() { //size*ratio = scaledWidth
//		return ratio;
//	}
	public static float measureWidth(String s, float currentSize) {
		int max = 0;
		int sLine = 0;
		for(int i=0; i<s.length(); i++) {
			char c = s.charAt(i);
			if(c=='\n') {
				max = Math.max(sLine, max);
				sLine = 0;
				continue;
			}
			if(c=='&' && i<s.length()-1) {
				char nc = s.charAt(i+1);
				if(Utils.isTextStyleCode(nc) || Utils.isTextColorCode(nc)) {
					i++;
					continue;
				}else if(nc=='&') {
					i++;//skip next
					sLine++;
					continue;
				}
			}
			
			sLine++;
			
		}
		max = Math.max(sLine, max);
		return max*(currentSize * ratio);
	}
	public static float measureHeight(String s, float size) {
		int lines = 1;
		for(int i=0; i<s.length(); i++) {
			if(s.charAt(i)=='\n')
				lines++;
		}
		return lines*size;
	}
}
