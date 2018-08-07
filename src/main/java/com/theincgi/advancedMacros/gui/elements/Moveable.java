package com.theincgi.advancedMacros.gui.elements;

public interface Moveable {
	void setPos(int x, int y);
	void setX(int x);
	void setY(int y);
	/**should not interact with input events when hidden*/
	void setVisible(boolean b);

	/**How tall is your item? allows us to calculate space needed*/
	int getItemHeight();

	/**How wide is your item?*/
	int getItemWidth();
	
	void setWidth(int i);
	void setHeight(int i);
	
	int getX();
	int getY();
	
}