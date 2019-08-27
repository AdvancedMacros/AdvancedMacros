package com.theincgi.advancedMacros.gui;

import java.util.concurrent.ConcurrentHashMap;

import org.luaj.vm2_v3_0_1.LuaValue;

import com.theincgi.advancedMacros.gui.elements.Drawable;
import com.theincgi.advancedMacros.gui.elements.GuiButton;
import com.theincgi.advancedMacros.gui.elements.ListManager;
import com.theincgi.advancedMacros.gui.elements.Moveable;
import com.theincgi.advancedMacros.gui.elements.OnClickHandler;
import com.theincgi.advancedMacros.lua.LuaDebug;
import com.theincgi.advancedMacros.lua.LuaDebug.Status;
import com.theincgi.advancedMacros.lua.LuaDebug.StatusListener;
import com.theincgi.advancedMacros.misc.PropertyPalette;
import com.theincgi.advancedMacros.misc.Utils;
import com.theincgi.advancedMacros.misc.Utils.TimeFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class RunningScriptsGui extends Gui{
	ListManager listManager;
	private LuaDebug luaDebug;
	ConcurrentHashMap<Thread, Script> scripts = new ConcurrentHashMap<>(10);
	public RunningScriptsGui(LuaDebug luaDebug) {
		this.luaDebug = luaDebug;
		listManager = new ListManager(5, 17, width-10, height-22, /*new WidgetID(700), "colors.runningScripts"*/ new PropertyPalette());
		listManager.setDrawBG(false);
		addInputSubscriber(listManager);
		luaDebug.addStatusListener(new StatusListener() {
			@Override
			public void onStatus(Thread sThread, Status status) {
				switch (status) {
				case DONE:
					listManager.remove(scripts.remove(sThread));
					break;
				case PAUSED:{
					Script s = scripts.get(sThread);
					if(s!=null){
						s.statusChanged(status);
					}
					break;
				}
				case RUNNING:{
					Script s = scripts.get(sThread);
					if(s==null){
						s = new Script(sThread);
						scripts.put(sThread, s);
						listManager.add(s);
					}
					s.statusChanged(status);
					break;
				}
				case CRASH:
				case STOPPED:
				{
					Script s = scripts.get(sThread);
					if(s!=null){
						listManager.remove(scripts.remove(sThread));
					}
				}
				default:
					break;
				}
			}
		});
	}
	
	@Override
	public ITextComponent getTitle() {
		return new StringTextComponent("Running Scripts");
	}
	
	@Override
	public void resize(Minecraft mc, int width, int height) {
		super.resize(mc, width, height);
		listManager.setWidth(width-10);
		listManager.setHeight(height-22);
	}
	
	final int WHITE = Color.WHITE.toInt();
	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		super.render(mouseX, mouseY, partialTicks);
		this.getFontRend().drawString("Running Scripts:", 5, 3, WHITE);
		listManager.onDraw(this, mouseX, mouseY, partialTicks);
	}
	
	private class Script implements Drawable, Moveable, InputSubscriber{
		Thread thread;
		GuiButton stop;
		int labelColor = Status.NEW.getStatusColor().toInt();
		private int x, y;
		boolean isVisible;
		public Script(final Thread thread) {
			this.thread = thread;
			
			//stop = new GuiButton(new WidgetID(701), 0, 0, 20, 12, LuaValue.NIL, LuaValue.valueOf("[X]"), "colors.runningScript", 
			//		Color.BLACK, Color.WHITE, Color.TEXT_c);
			stop = new GuiButton(0, 0, 20, 12, LuaValue.NIL, LuaValue.valueOf("[X]"), "runningScript","stopButton");
			stop.setOnClick(new OnClickHandler() {
				@Override
				public void onClick(int button, GuiButton sButton) {
					luaDebug.stop(thread);
				}
			});
		}

		public void statusChanged(Status status) {
			labelColor = status.getStatusColor().toInt();
		}


		@Override
		public void setPos(int x, int y) {
			this.x = x;
			this.y = y;
			stop.setPos(x, y);
		}
		
		@Override
		public void setX(int x) {
			setPos(x, y);
		}
		
		@Override
		public void setY(int y) {
			setPos(x, y);
		}
		@Override
		public void setVisible(boolean b) {
			this.isVisible = b;
		}

		@Override
		public int getItemHeight() {
			return stop.getHei();
		}

		@Override
		public int getItemWidth() {
			return RunningScriptsGui.this.width-15;
		}

		@Override
		public void setWidth(int i) {
		}

		@Override
		public void setHeight(int i) {
		}

		@Override
		public int getX() {
			return this.x;
		}

		@Override
		public int getY() {
			return this.y;
		}

		@Override
		public void onDraw(Gui g, int mouseX, int mouseY, float partialTicks) {
			stop.onDraw(g, mouseX, mouseY, partialTicks);
			int dY = getY()+2;
			int m = g.drawMonospaceString("[", (int) (getX()+stop.getWid()*1.5), dY, Color.WHITE.toInt());
			double uptime = luaDebug.getUptime(thread);
			TimeFormat timeFormat = Utils.formatTime(uptime);
			m = g.drawMonospaceString(
					String.format("%02d:%02d:%02d.%02d", 
							timeFormat.days*24 + timeFormat.hours,
							timeFormat.mins, timeFormat.seconds, timeFormat.millis), m, dY, Color.TEXT_7.toInt());
			m = g.drawMonospaceString("] ", m, dY, Color.WHITE.toInt());
			String label = luaDebug.getLabel(thread);
			label = label==null? "?" : label;
			g.drawMonospaceString(label, m, dY, labelColor);
		}

		@Override
		public boolean onScroll(Gui gui, double i) {
			return false;
		}

		@Override
		public boolean onMouseClick(Gui gui, double x, double y, int buttonNum) {
			return stop.onMouseClick(gui, x, y, buttonNum);
		}

		@Override
		public boolean onMouseRelease(Gui gui, double x, double y, int state) {
			return stop.onMouseRelease(gui, x, y, state);
		}

		@Override
		public boolean onMouseClickMove(Gui gui, double x, double y, int buttonNum, double q, double r) {
			return false;
		}

		@Override
		public boolean onCharTyped(Gui gui, char typedChar, int mods) {
			return false;
		}
		@Override
		public boolean onKeyPressed(Gui gui, int keyCode, int scanCode, int modifiers) {
			return false;
		}

		@Override
		public boolean onKeyRelease(Gui gui, int keyCode, int scanCode, int modifiers) {
			return false;
		}
		@Override
		public boolean onKeyRepeat(Gui gui, int keyCode, int scanCode, int modifiers, int n) {
			return false;
		}
		
	}
}