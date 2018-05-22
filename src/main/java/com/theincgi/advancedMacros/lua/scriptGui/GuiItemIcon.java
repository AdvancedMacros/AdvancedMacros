package com.theincgi.advancedMacros.lua.scriptGui;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.TwoArgFunction;

import com.theincgi.advancedMacros.gui.Gui;
import com.theincgi.advancedMacros.gui.elements.GuiRect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class GuiItemIcon extends ScriptGuiElement{
	static RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
	static FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

	ItemStack itemStack;

	public GuiItemIcon(Gui gui, Group parent) {
		super(gui, parent);
		itemStack = ItemStack.EMPTY;
		this.set("setItem", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue item, LuaValue quantity) {
				if(item.isnil()) {
					throw new LuaError("Item cannot be nil");
				}
				setStack(item.checkjstring());
				if(!quantity.isnil()) {
					itemStack.setCount(quantity.checkint());
				}
				return NONE;
			}
		});
		this.set("setCount", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				itemStack.setCount(arg.checkint());
				return NONE;
			}
		});
		wid = hei = 16;
	}

	public void setStack(String text) {
		//Item i = Item.getByNameOrId(text);
		if(text==null) {
			itemStack = ItemStack.EMPTY;
			return;
		}
		if(!text.contains(":")) {
			text = "minecraft:"+text;
		}
		int indx = text.lastIndexOf(":");
		String end = text.substring(indx+1);
		try {
			int dmg = Integer.parseInt(end);
			itemStack = new ItemStack(Item.getByNameOrId(text.substring(0, indx)));
			itemStack.setItemDamage(dmg);
		}catch (NumberFormatException e) {
			itemStack = new ItemStack(Item.getByNameOrId(text));
		}

	}

	@Override
	public void onDraw(Gui g, int mouseX, int mouseY, float partialTicks) {
		super.onDraw(g, mouseX, mouseY, partialTicks);
		RenderHelper.disableStandardItemLighting();
		RenderHelper.enableGUIStandardItemLighting();
		itemRender.zLevel = z;

		itemRender.renderItemIntoGUI(itemStack, (int)x, (int)y);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.bindTexture(0);
		
		if(GuiRect.isInBounds(mouseX, mouseY, (int)x, (int)y, (int)wid, (int)hei))
			GuiRectangle.drawRectangle(x, y, wid, hei, getHoverTint(), z+150);
	}

	@Override
	public int getItemHeight() {
		// TODO Auto-generated method stub
		return (int)wid;
	}

	@Override
	public int getItemWidth() {
		// TODO Auto-generated method stub
		return (int)hei;
	}

	@Override
	public void setWidth(int i) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHeight(int i) {
		// TODO Auto-generated method stub

	}

	public void setCount(int optint) {
		itemStack.setCount(1);
	}







}
