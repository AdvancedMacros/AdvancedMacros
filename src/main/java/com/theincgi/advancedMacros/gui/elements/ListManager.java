package com.theincgi.advancedMacros.gui.elements;

import java.util.Iterator;
import java.util.LinkedList;

import com.theincgi.advancedMacros.gui.Color;
import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.gui.Gui.InputSubscriber;
import com.theincgi.advancedMacros.gui.elements.GuiScrollBar.Orientation;
import com.theincgi.advancedMacros.misc.Property;
import com.theincgi.advancedMacros.misc.PropertyPalette;
import com.theincgi.advancedMacros.misc.Settings;
import com.theincgi.advancedMacros.misc.Utils;

public class ListManager implements InputSubscriber, Drawable, Moveable{
	private LinkedList<Moveable> items= new LinkedList<>();
	Moveable hand = null;
	private int spacing = 6;
	private int x,y,wid,hei;
	GuiScrollBar scrollBar;
//	Property colorBGFill;
//	Property colorBGFrame;
	private static final String DEFAULT_PROP = "colors.list";
	PropertyPalette propertyPalette;
	public ListManager(int x, int y, int wid, int hei, String...propPath) {
		this(x, y, wid, hei, propPath.length==0? new PropertyPalette() : new PropertyPalette(propPath, Settings.settings));
	}
	
	public ListManager(int x, int y, int wid, int hei, PropertyPalette propPalette) {
		this.propertyPalette = propPalette;
		//widgetPropTableName = widgetPropTableName==null?DEFAULT_PROP:widgetPropTableName;
//		scrollBar = new GuiScrollBar(wID, x+wid-7, y, 7, hei, Orientation.UPDOWN, widgetPropTableName);
		scrollBar = new GuiScrollBar( x+wid-7, y, 7, hei, Orientation.UPDOWN, propPalette.propertyPaletteOf("scrollBar"));
		
		propPalette.addColorIfNil(Color.BLACK, "colors", "backgroundFill");
		propPalette.addColorIfNil(Color.WHITE, "colors", "backgroundFrame");
		
//		colorBGFill = new Property(widgetPropTableName+".BGFill", Color.BLACK.toLuaValue(), "listBGFill", wID);
//		colorBGFrame = new Property(widgetPropTableName+".BGFrame", Color.WHITE.toLuaValue(), "listBGFrame", wID);

		updateBounds(x, y, wid, hei);
		scrollBar.setItemCount(1);
	}

	public void updateBounds(int x, int y, int wid, int hei){
		this.x = x;
		this.y = y;
		this.wid = wid;
		this.hei = hei;
		scrollBar.reposition(x+wid-7,y, 7, hei);
		scrollBar.setVisibleItems(hei);

	}


	/**call this to pull the item out of the list and put it in the hand<br>
	 * only one item may be in the hand, be sure to call place before using grab a second time
	 * */
	public void grab(int item){
		if(hand==null){
			synchronized (this) {
				hand = items.remove(item);
			}
		}
	}
	public void grab(Moveable item){
		if(hand==null){
			synchronized (this) {
				if(items.remove(item)){
					hand = item;
				}
			}
		}
	}
	public void place(int slot){
		if(hand!=null){
			synchronized (this) {
				items.add(slot, hand);
				hand = null;
			}
		}
	}
	
	int bonusSpace = 0;
	public void setBonusSpace(int bonusSpace) {
		this.bonusSpace = bonusSpace;
		scrollBar.setItemCount(getTotalHeight()+bonusSpace);
	}
	public void add(Moveable m){
		synchronized (this) {
			items.add(m);
		}
		scrollBar.setItemCount(getTotalHeight()+bonusSpace);
	}
	public Moveable getItem(int index){
		synchronized (this) {
			return items.get(index);
		}
	}

	
	public void setScrollSpeed(double speed) {
		scrollBar.setScrollSpeed(speed);
	}



	@Override
	public boolean onScroll(Gui gui, int i) {
		if(!isVisible)return false;
		if(scrollBar.onScroll(gui, i))return true;
		synchronized(this){
			for (Moveable moveable : items) {
				if(moveable instanceof InputSubscriber){
					if(((InputSubscriber) moveable).onScroll(gui, i)) return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean onMouseClick(Gui gui, int x, int y, int buttonNum) {
		if(scrollBar.onMouseClick(gui, x, y, buttonNum))return true;
		//System.out.println("CLICK ListManager");
		synchronized(this){
			for (Moveable moveable : items) {
				if(moveable instanceof InputSubscriber){
					if(((InputSubscriber) moveable).onMouseClick(gui, x, y, buttonNum))
						return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onMouseRelease(Gui gui, int x, int y, int state) {
		if(scrollBar.onMouseRelease(gui, x, y, state))return true;
		if(hand!=null){
			place(getHoverSlot(y));
		}
		synchronized(this){
			for (Moveable moveable : items) {
				if(moveable instanceof InputSubscriber){
					if(((InputSubscriber) moveable).onMouseRelease(gui, x, y, state))
						return true;
				}
			}
		}
		return false;
	}

	private int hoverslot = 0;
	private boolean forceFrame = false;
	private int getHoverSlot(int y) {
		return hoverslot;
	}

	@Override
	public boolean onMouseClickMove(Gui gui, int x, int y, int buttonNum, long timeSinceClick) {
		if(scrollBar.onMouseClickMove(gui, x, y, buttonNum, timeSinceClick))return true;
		synchronized(this){
			for (Moveable moveable : items) {
				if(moveable instanceof InputSubscriber){
					if(((InputSubscriber) moveable).onMouseClickMove(gui, x, y, buttonNum, timeSinceClick))
						return true;
				}
			}
		}
		return false;
	}



	@Override
	public boolean onKeyPressed(Gui gui, char typedChar, int keyCode) {
		if(scrollBar.onKeyPressed(gui, typedChar, keyCode))return true;
		synchronized(this){
			for (Moveable moveable : items) {
				if(moveable instanceof InputSubscriber){
					if(((InputSubscriber) moveable).onKeyPressed(gui, typedChar, keyCode))
						return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKeyRepeat(Gui gui, char typedChar, int keyCode, int repeatMod) {
		if(scrollBar.onKeyRepeat(gui, typedChar, keyCode, repeatMod))return true;
		synchronized(this){
			for (Moveable moveable : items) {
				if(moveable instanceof InputSubscriber){
					if(((InputSubscriber) moveable).onKeyRepeat(gui, typedChar, keyCode, repeatMod))
						return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKeyRelease(Gui gui, char typedChar, int keyCode) {
		if(scrollBar.onKeyRelease(gui, typedChar, keyCode))return true;
		synchronized(this){
			for (Moveable moveable : items) {
				if(moveable instanceof InputSubscriber){
					if(((InputSubscriber) moveable).onKeyRelease(gui, typedChar, keyCode))
						return true;
				}
			}
		}
		return false;
	}

	public int getTotalHeight(){
		int h = 0;
		synchronized(this){
			for (Moveable moveable : items) { 
				h+=moveable.getItemHeight()+spacing;
			}
			return h+(hand==null?0:hand.getItemHeight());
		}
	}
	
	boolean modeFullBox = false;
	public void setModeFullBox(boolean modeFullBox) {
		this.modeFullBox = modeFullBox;
	}
	
	@Override
	public void onDraw(Gui gui, int mouseX, int mouseY, float partialTicks) {
		if(!isVisible){return;}

		int frame = propertyPalette.getColor("colors", "backgroundFrame").toInt(); 
		int fill = propertyPalette.getColor("colors", "backgroundFill").toInt();
		synchronized(this){
			//System.out.println(String.format("Items: %s, Hei is %s for drawing", items.size(), hei));
			
			if(drawBG){
				//System.out.println(("Box: HEI "+ Math.min(hei, getTotalHeight())));
				gui.drawBoxedRectangle(x, y, wid-scrollBar.getItemWidth(), modeFullBox? hei:Math.min(hei, getTotalHeight()), frame, fill);
			}
			Iterator<Moveable> moveables = items.iterator();
			int heightUsed = 0;
			double sPos = scrollBar.getOffset();
			hoverslot = items.size();
			while(moveables.hasNext()){
				Moveable m = moveables.next();
				int normalY = (int) (y-sPos + heightUsed);
				if(hand!=null){
					if(mouseY<normalY+m.getItemHeight()/2){
						normalY+=m.getItemHeight();
						hoverslot--;
					}
				}
				m.setPos(x,normalY);
				if(normalY < y || normalY+m.getItemHeight() > y+hei){
					m.setVisible(false);
					//System.out.println(y + " " +hei);
				}else{
					m.setVisible(true);
				}
				heightUsed += m.getItemHeight()+spacing;
			}

			for (Moveable moveable : items) {
				if(moveable instanceof Drawable){
					moveable.setWidth(wid-scrollBar.getItemWidth()-scrollGap);
					((Drawable) moveable).onDraw(gui, mouseX, mouseY, partialTicks);
				}
			}
			scrollBar.setHeight(hei);
			scrollBar.setVisible(alwaysShowScroll || heightUsed>=hei);
			scrollBar.setPos(x+wid-scrollBar.getItemWidth(), y);
			scrollBar.onDraw(gui, mouseX, mouseY, partialTicks);
			if(hand!=null){
				hand.setPos(mouseX-hand.getItemWidth()+6, mouseY);
				hand.setWidth(wid-scrollBar.getItemHeight());
				if(hand instanceof Drawable){
					((Drawable) hand).onDraw(gui, mouseX, mouseY, partialTicks);
				}
			}
			if(forceFrame) {
				gui.drawVerticalLine(getX(), getY(), getY()+getItemHeight(), frame);
				gui.drawHorizontalLine(getX(), getX()+getItemWidth()-scrollBar.getItemWidth()-1, getY(), frame);
				gui.drawHorizontalLine(getX(), getX()+getItemWidth()-scrollBar.getItemWidth()-1, getY()+getItemHeight(), frame);
			}
		}
	}
	
	/**Draw a frame outline over all the elements*/
	public void setForceFrame(boolean forceFrame) {
		this.forceFrame = forceFrame;
	}
	
	@Override
	public void setPos(int x, int y) {
		this.x = x;
		this.y = y;
		scrollBar.setPos(x, y+wid-scrollBar.getItemWidth());
		updateBounds(x, y, wid, hei);
	}

	private boolean isVisible=true;

	@Override
	public void setVisible(boolean b) {
		scrollBar.setVisible(b);
		synchronized(this){
			for (Moveable moveable : items) {
				moveable.setVisible(false);
			}
		}
		isVisible = b;
	}

	@Override
	public int getItemHeight() {
		return hei;
	}

	@Override
	public int getItemWidth() { //TODO can this be changed to just be getWidth.. was there a point?
		return wid;
	}

	public int find(GuiButton sButton) {
		int i = 0;
		synchronized(this){
			for (Moveable moveable : items) {
				if(moveable.equals(sButton)) return i;
				i++;
			}
		}
		return -1;
	}

	public void clear() {
		synchronized(this){
			items.clear();
		}
	}

	public void remove(Object obj) {
		synchronized(this){
			items.remove(obj);
		}
	}
	public void remove(int index) {
		synchronized(this){
			items.remove(index);
		}
	}

	public void setSpacing(int i) {
		spacing = i;
	}

	public boolean isVisible() {
		return isVisible;
	}
	@Override
	public int getX() {
		return x;
	}
	@Override
	public int getY() {
		return y;
	}

	@Override
	public void setWidth(int i) {
		this.wid = i;
	}

	@Override
	public void setHeight(int i) {
		updateBounds(x, y, wid, i);
		//System.out.println("Height is finaly "+hei);
	}
	public void setScrollbarWidth(int wid){
		scrollBar.setWidth(wid);
	}
	private int scrollGap=0;
	public void setScrollbarGap(int wid){
		scrollGap = wid;
	}
	private boolean drawBG =true;
	public void setDrawBG(boolean drawBG) {
		this.drawBG = drawBG;
	}
	private boolean alwaysShowScroll = false;
	public void setAlwaysShowScroll(boolean alwaysShowScroll) {
		this.alwaysShowScroll = alwaysShowScroll;
	}

	public LinkedList<Moveable> getItems() {
		synchronized(this){
			return (LinkedList<Moveable>) items.clone();
		}
	}

	public void scrollTop() {
		//TODO make it so it scrolls to top, for scriptBrowser2
	}
}