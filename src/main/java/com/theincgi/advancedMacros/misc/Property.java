package com.theincgi.advancedMacros.misc;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import com.theincgi.advancedMacros.gui.elements.WidgetID;

public class Property {
	//in settings widgetSettings.w###.colors_standardButton_*** where *** is either value or name will save the custom value for a widget if changed from default 
	private static final String propLocation = "widgetSettings";
	private String defaultPropPointer; //ie "colors.standardButton"
	private LuaValue defaultPropValue; //ie {r,g,b}
	private String propName; //ie fillColor -> widgetSettings.w###.fillColor = {rgb}
	WidgetID wID;
	
	
	
	/**@param defaultPropPointer where the default value for this should point to is<br>
	 * @param defaultPropValue   what this should be if value is nil<br>
	 * @param propName ex: fillColor will create widgetSettings.w15.PROPNAME = defValue, grouping recommended (color.fill)<br>
	 * @param wID the widget ID object<br>
	 * */
	public Property(String defaultPropPointer, LuaValue defaultPropValue, String propName, WidgetID wID) {
		this.defaultPropPointer = defaultPropPointer;
		this.defaultPropValue = defaultPropValue;
		this.propName = propName;
		this.wID = wID;
		getPropName();
		getPropValue();
	}
	public String getPropName(){ //from widgetSettings.w###.color = colors.standardButton, yeilds defaultPropPointer if fails
		return Utils.tableFromProp(Settings.settings, 
				propLocation+"."+wID.getID()+"."+propName
				, LuaValue.valueOf(defaultPropPointer)).checkjstring();
	}
	public LuaValue getPropValue(){ //gets frame={r,g,b}... from the getPropName address, yeilds default prop value if nil
		//System.out.println("Got from "+propName);
		return Utils.tableFromProp(Settings.settings, getPropName(), defaultPropValue);
	}
	public void setPropName(String s){
		setPropName(LuaValue.valueOf(s));
	}
	public void setPropName(LuaValue s){
		LuaTable table = Utils.tableFromProp(Settings.settings, 
				propLocation+"."+wID.getID(),
				new LuaTable() //this is if widgetSettings.w### is nill
				).checktable();
		table.set(propName, s);
	}
	/**sets the value at wherever getPropName points to*/
	public void setPropValue(LuaValue v){
		String prop = getPropName(); //ie widgetSettings.w32.sound = "blah.wav"
		String prop1 = prop.substring(0,prop.lastIndexOf(".")); //gets the part that is widgetSettings.w32 so we can set in next setep
		String prop2 = prop.substring(prop.lastIndexOf(".")+1); //gets the part that is the prop name, this may have been changed to not include propName
		LuaTable t = Utils.tableFromProp(Settings.settings, prop1, new LuaTable()).checktable();
		t.set(prop2, v);
	}
	public void reset(){
		setPropName(defaultPropPointer);
		setPropValue(defaultPropValue);
	}
}