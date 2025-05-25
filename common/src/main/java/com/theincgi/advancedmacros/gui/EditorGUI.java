package com.theincgi.advancedmacros.gui;

import com.theincgi.advancedmacros.AdvancedMacros;
import com.theincgi.advancedmacros.event.CombinedEventHandler;
import com.theincgi.advancedmacros.gui.elements.ColorTextArea;
import com.theincgi.advancedmacros.gui.elements.GuiButton;
import com.theincgi.advancedmacros.gui.elements.OnClickHandler;
import com.theincgi.advancedmacros.gui2.ScriptBrowser2;
import com.theincgi.advancedmacros.misc.PropertyPalette;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.luaj.vm2_v3_0_1.LuaValue;

public class EditorGUI extends Gui {

    ColorTextArea cta = new ColorTextArea(this, "editor");
    TextFieldWidget gtf;
    GuiButton save, exit, quickRun;

    //TODO autosave option property for run

    //TODO Help bar
    public void postInit() {
        gtf.setFocused(false);
    }

    public EditorGUI() {
        addInputSubscriber(cta);
        gtf = new TextFieldWidget(getFontRend(), 6, 2, width / 3, 20, Text.literal(""));
        gtf.setFocusUnlocked(true);

        {
            PropertyPalette p = new PropertyPalette(new String[]{"editor"});
            p.addColor(Color.TEXT_8, "saveButton", "colors", "frame");
            p.addColor(Color.TEXT_8, "exitButton", "colors", "frame");
            p.addColor(Color.TEXT_8, "runButton", "colors", "frame");
        }
        cta.setFocused(true);
        save = new GuiButton(7 + width / 3, 2, width / 4, 10, LuaValue.NIL, LuaValue.valueOf("Save"), "editor", "saveButton");
        //save 	= new GuiButton(new WidgetID(301), 7+width/3, 				2	, width/4, 10, LuaValue.NIL, LuaValue.valueOf("Save"), "editor.save", Color.BLACK, Color.TEXT_8, Color.WHITE);
        exit = new GuiButton(7 + width / 3, 12, width / 4, 10, LuaValue.NIL, LuaValue.valueOf("Exit"), "editor", "exitButton");
        //exit 	= new GuiButton(new WidgetID(302), 7+width/3, 				12	, width/4, 10, LuaValue.NIL, LuaValue.valueOf("Exit"), "editor.exit", Color.BLACK, Color.TEXT_8, Color.WHITE);
        quickRun = new GuiButton(save.getX() + save.getWid(), 12, width / 8, 10, LuaValue.NIL, LuaValue.valueOf("Run"), "editor", "runButton");
        //quickRun= new GuiButton(new WidgetID(303), save.getX()+save.getWid(),12	, width/8, 10, LuaValue.NIL, LuaValue.valueOf("Run"), "editor.run", Color.BLACK, Color.TEXT_8, Color.WHITE);
        addInputSubscriber(save);
        addInputSubscriber(exit);
        addInputSubscriber(quickRun);
        save.setOnClick(new OnClickHandler() {
            @Override
            public void onClick(int button, GuiButton sButton) {
                cta.save();
                updateKeywords();
            }
        });
        exit.setOnClick((button, sButton) -> {
            if (cta.isNeedsSave() && !ColorTextArea.isCTRLDown()) {
                //not gonna do anything
            } else {
                //either overridden for exit or saved, so all good
                CombinedEventHandler.showPrevMenu();
            }
        });
        quickRun.setOnClick(new OnClickHandler() {//TODO shift to save and run? or run with args?
            @Override
            public void onClick(int button, GuiButton sButton) {
                save.getOnClickHandler().onClick(button, save);
                CombinedEventHandler.closeMenu();
                AdvancedMacros.runScript(ScriptBrowser2.getScriptPath(cta.getScriptFile()));
            }
        });

        //		cta.setOnNeedsSaveChanged(new Runnable() {
        //			@Override
        //			public void run() {
        //				if(cta.isNeedsSave())
        //					exit.setFill(Color.TEXT_4);
        //				else
        //					exit.setFill(Color.TEXT_2);
        //			}
        //		});
    }

    public ColorTextArea getCta() {
        return cta;
    }

    @Override
    public Text getTitle() {
        return Text.literal("Script Editor");
    }

    public void runScriptFromEditor() {
        quickRun.getOnClickHandler().onClick(0, save);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float partialTicks) {
        if (cta.isNeedsSave()) {
            if (ColorTextArea.isCTRLDown()) {
                exit.setFill(Color.TEXT_6);//Orange
            } else {
                exit.setFill(Color.TEXT_4);//Red
            }
        } else {
            exit.setFill(Color.TEXT_2);//Green
        }
        super.render(drawContext, mouseX, mouseY, partialTicks);
        gtf.render(drawContext, mouseX, mouseY, partialTicks);
        save.onDraw(drawContext, this, mouseX, mouseY, partialTicks);
        exit.onDraw(drawContext, this, mouseX, mouseY, partialTicks);
        quickRun.onDraw(drawContext, this, mouseX, mouseY, partialTicks);
        cta.onDraw(drawContext, this, mouseX, mouseY, partialTicks);
        drawContext.drawText(this.getFontRend(), String.format("%3d, %4d", cta.getCursor().getX() + 1, cta.getCursor().getY() + 1), (int) (8 + width * 7 / 8f), 5, Color.WHITE.toInt(), false);
    }

    public void updateKeywords() {
        ColorTextArea.updateKeywords();
    }

    @Override
    public void onOpen() {
        super.onOpen();
        cta.setFocused(true);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (super.mouseClicked(mouseX, mouseY, mouseButton)) {
            return true;
        }
        gtf.mouseClicked(mouseX, mouseY, mouseButton);
        cta.setFocused(!gtf.isFocused());
        return true;

    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (super.charTyped(typedChar, keyCode)) {
            return true;
        }
        if (gtf.isFocused()) {
            return gtf.charTyped(typedChar, keyCode);
            //TODO what to do if the text box is updated here
        }
        return false;
    }

    @Override
    public void resize(MinecraftClient mc, int width, int height) {
        super.resize(mc, width, height);
        gtf.setWidth(width / 3);
        save.setWidth(width / 4);
        exit.setWidth(width / 4);
        quickRun.setWidth(width / 8);
        save.setPos(7 + width / 3, 2);
        exit.setPos(7 + width / 3, 12);
        quickRun.setPos(save.getX() + save.getWid(), 12);
        cta.setPos(5, gtf.getY() + gtf.getHeight() + 1);
        cta.resize(width - 10, height - 6 - gtf.getHeight());

    }

    public void openScript(String sScript) {
        cta.openScript(sScript);
        gtf.setText(sScript);
        gtf.setCursor(0, false);
    }

}
