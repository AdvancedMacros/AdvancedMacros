package com.theincgi.advancedMacros.lua.functions;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

import com.theincgi.advancedMacros.AdvancedMacros;
import com.theincgi.advancedMacros.misc.CallableTable;
import com.theincgi.advancedMacros.misc.Utils;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiCommandBlock;
import net.minecraft.client.gui.GuiEnchantment;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class GuiControls {



	public static LuaValue load(Gui gCon) {
		LuaTable controls = new LuaTable();
		if(gCon instanceof GuiRepair) {
			GuiRepair gr = (GuiRepair) gCon;
			for (RepairOp r : RepairOp.values()) {
				controls.set(r.name(), new CallableTable(r.getDocLocation(), new DoRepair(r, gr)));
			}
		}else if(gCon instanceof GuiMerchant) {
			GuiMerchant gm = (GuiMerchant) gCon;
			for (TradeOp r : TradeOp.values()) {
				controls.set(r.name(), new CallableTable(r.getDocLocation(), new DoTrade(r, gm)));
			}

		}else if(gCon instanceof GuiEnchantment) {
			GuiEnchantment ge = (GuiEnchantment) gCon;
			for (EnchantOp r : EnchantOp.values()) {
				controls.set(r.name(), new CallableTable(r.getDocLocation(), new DoEnchant(r, ge)));
			}
		}else if(gCon instanceof GuiEditSign) {
			GuiEditSign es = (GuiEditSign) gCon;
			for (SignOp r : SignOp.values()) {
				controls.set(r.name(), new CallableTable(r.getDocLocation(), new DoSign(r, es)));
			}
		}else if(gCon instanceof GuiScreenBook) {
			GuiScreenBook bk = (GuiScreenBook) gCon;
			for (BookOp r : BookOp.values()) {
				controls.set(r.name(), new CallableTable(r.getDocLocation(), new DoBook(r, bk)));
			}
		}else if(gCon instanceof GuiCommandBlock) {
			GuiCommandBlock cb = (GuiCommandBlock) gCon;
			for (CommandOp r : CommandOp.values()) {
				controls.set(r.name(), new CallableTable(r.getDocLocation(), new DoCommand(r, cb)));
			}
		}
		Gui whenOpened = gCon;
		controls.set("isOpen", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return valueOf(whenOpened==AdvancedMacros.getMinecraft().currentScreen);
			}
		});
		controls.set("close", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				AdvancedMacros.getMinecraft().displayGuiScreen(null);
				return NONE;
			}
		});


		return controls;
	}

	private static class DoRepair extends VarArgFunction {
		RepairOp op;
		GuiRepair gr;
		Method renameItem = ReflectionHelper.findMethod(GuiRepair.class, "renameItem", "func_147090_g", new Class[] {});
		
		Field anvil = ReflectionHelper.findField(GuiRepair.class, "anvil", "w", "field_147092_v");
		Field nameField = ReflectionHelper.findField(GuiRepair.class, "nameField", "x", "field_147091_w");
		public DoRepair(RepairOp op, GuiRepair gr) {
			super();
			this.op = op;
			this.gr = gr;
		}

		@Override
		public Varargs invoke(Varargs args) {
			switch (op) {
			case getCost:{
				try {
					anvil.setAccessible(true);
					ContainerRepair cr = (ContainerRepair) anvil.get(gr);
					return valueOf(cr.maximumCost);
				} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
					throw new LuaError(e);
				}
			}
			case getName:
				try {
					nameField.setAccessible(true);
					GuiTextField gtf = (GuiTextField) nameField.get(gr);
					return valueOf(gtf.getText());
				} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
					throw new LuaError(e);
				}
			case setName:
				try {
					nameField.setAccessible(true);
					GuiTextField gtf = (GuiTextField) nameField.get(gr);
					gtf.setText(args.arg1().checkjstring());
					renameItem.invoke(gr, new Object[] {});
					return NONE;
				} catch (SecurityException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
					throw new LuaError(e);
				}
			default:
				throw new LuaError("Unimplemented function '"+op.name()+"'");
			}
		}
	} 
	private static class DoTrade extends VarArgFunction {
		TradeOp op;
		GuiMerchant gm;
		public DoTrade(TradeOp op, GuiMerchant gm) {
			super();
			this.op = op;
			this.gm = gm;
		}
		@Override
		public Varargs invoke(Varargs args) {
			switch (op) {
			case getTrades:{
				LuaTable trades = new LuaTable();
				MerchantRecipeList mrl = gm.getMerchant().getRecipes(AdvancedMacros.getMinecraft().player);
				for(int i = 0; i<mrl.size(); i++) {
					MerchantRecipe mr = mrl.get(i);
					LuaTable t = new LuaTable();
					LuaTable inputs = new LuaTable();
					t.set("input", inputs);
					inputs.set(1,   Utils.itemStackToLuatable(mr.getItemToBuy()       ));
					inputs.set(2,   Utils.itemStackToLuatable(mr.getSecondItemToBuy() ));
					t.set("output", Utils.itemStackToLuatable(mr.getItemToSell()      ));
					t.set("isEnabled", !mr.isRecipeDisabled());
					trades.set(i+1, t);
				}
				return trades;
			}
			case getType:
				return valueOf(gm.getMerchant().getDisplayName().getUnformattedText());
			default:
				throw new LuaError("Unimplemented function '"+op.name()+"'");
			}
		}
	}
	private static class DoEnchant extends VarArgFunction {
		EnchantOp op;
		GuiEnchantment ge;
		Field container = ReflectionHelper.findField(GuiEnchantment.class, "container", "H", "field_147075_G");

		public DoEnchant(EnchantOp op, GuiEnchantment ge) {
			super();
			this.op = op;
			this.ge = ge;
		}
		@Override
		public Varargs invoke(Varargs args) {
			try {
				container.setAccessible(true);
				ContainerEnchantment ce = (ContainerEnchantment) container.get(ge);
				switch (op) {
				case getOptions:{
					LuaTable out = new LuaTable();
					for(int i = 1; i<=3; i++) {
						LuaTable o = new LuaTable();
						Enchantment e = Enchantment.getEnchantmentByID(ce.enchantClue[i-1]);
						if(e == null)continue;
						o.set("hint", e.getName());
						o.set("lvl", ce.enchantLevels[i-1]);
						out.set(i, o);
					}
					return out;
				}
				case pickOption:
					Minecraft mc = AdvancedMacros.getMinecraft();
					int arg = args.checkint(1);
					if(arg < 1 || arg > 3) throw new LuaError("argument out of range 1-3 ("+arg+")");
					if(ce.enchantItem(mc.player, arg-1))
						mc.playerController.sendEnchantPacket(ce.windowId, arg-1);
					return NONE;
				default:
					throw new LuaError("Unimplemented function '"+op.name()+"'");
				}
			} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
				throw new LuaError(e);
			}
		}
	}
	private static class DoSign extends VarArgFunction {
		SignOp op;
		GuiEditSign es;
		Field tileSign = ReflectionHelper.findField(GuiEditSign.class, "tileSign", "a", "field_146848_f");

		public DoSign(SignOp op, GuiEditSign es) {
			super();
			this.op = op;
			this.es = es;
		}
		@Override
		public Varargs invoke(Varargs args) {
			try {
				TileEntitySign ts = (TileEntitySign) tileSign.get(es);
				switch (op) {
				case getLines:{
					LuaTable lines = new LuaTable();
					for (int i = 0; i < ts.signText.length; i++) {
						lines.set(i+1, ts.signText[i].getUnformattedComponentText());
					}
					return lines.unpack();
				}
				//				case getFormatedLines:{
				//					LuaTable lines = new LuaTable();
				//					for (int i = 0; i < ts.signText.length; i++) {
				//						lines.set(i+1, Utils.fromMinecraftColorCodes(ts.signText[i].getFormattedText()));
				//					}
				//					return lines;
				//				}
				case done:
					if(es.mc==null) es.mc = AdvancedMacros.getMinecraft();
					ts.markDirty();
					AdvancedMacros.getMinecraft().displayGuiScreen(null);
					return NONE;
				case setLine:
					ts.signText[args.checkint(1)] = new TextComponentString(args.optjstring(2, ""));
					return NONE;
					//				case setFormatedLine:
					//					ts.signText[args.checkint(1)] = new TextComponentString(Utils.toMinecraftColorCodes(args.checkjstring(2)));
					//					return NONE; //lost when sent to server
					//case setFormatedLines:
				case setLines:{
					if(args.arg1().istable()) {
						LuaTable t = args.checktable(1);
						for(int i=1; i<=ts.signText.length; i++) {
							ts.signText[i-1] = new TextComponentString(t.get(i).optjstring(""));
						}
					}else {
						for(int i = 1; i<=ts.signText.length; i++)
							ts.signText[i-1] = new TextComponentString(args.optjstring(i, ""));
					}
					return NONE;
				}


				default:
					throw new LuaError("Unimplemented function '"+op.name()+"'");
				}
			} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
				throw new LuaError(e);
			}
		}
	}
	private static class DoBook extends VarArgFunction {
		BookOp op;
		GuiScreenBook book;
		Method insert;
		Method setTxt;
		Method getTxt;
		Method updateButtons;
		Method sendBook;
		Method addPage;

		Field  pages     = ReflectionHelper.findField(GuiScreenBook.class, "bookTotalPages", "x", "field_146476_w");
		Field  currPage  = ReflectionHelper.findField(GuiScreenBook.class, "currPage",       "y", "field_146484_x");
		Field  isMod     = ReflectionHelper.findField(GuiScreenBook.class, "bookIsModified", "s", "field_146481_r");
		Field  isUnsigned= ReflectionHelper.findField(GuiScreenBook.class, "bookIsUnsigned", "i", "field_146475_i");
		Field  bookTitle = ReflectionHelper.findField(GuiScreenBook.class, "bookTitle",      "A", "field_146482_z");
		Field  bookObj   = ReflectionHelper.findField(GuiScreenBook.class, "book",           "h", "field_146474_h");
		//		Field  gettingSigned = 
		//				           ReflectionHelper.findField(GuiScreenBook.class, "bookGettingSigned", "t", "field_146480_s");

		public DoBook(BookOp op, GuiScreenBook book) {
			super();
			//System.out.println(Arrays.toString(GuiScreenBook.class.getDeclaredMethods()));
			//System.out.println(Arrays.toString(GuiScreenBook.class.getMethods()));
			loadReflects();
			this.op = op;
			this.book = book;
			insert.setAccessible(true);
			setTxt.setAccessible(true);
			getTxt.setAccessible(true);
			updateButtons.setAccessible(true);
			sendBook.setAccessible(true);
			addPage.setAccessible(true);

			pages.setAccessible(true);
			currPage.setAccessible(true);
			isMod.setAccessible(true);
			isUnsigned.setAccessible(true);
			bookTitle.setAccessible(true);
			//			gettingSigned.setAccessible(true);
		}
		private void loadReflects() {
			insert = ReflectionHelper.findMethod(GuiScreenBook.class, "pageInsertIntoCurrent", 		"func_146459_b", String.class);
			setTxt = ReflectionHelper.findMethod(GuiScreenBook.class, "pageSetCurrent", 		  	"func_146457_a", String.class);
			getTxt = ReflectionHelper.findMethod(GuiScreenBook.class, "pageGetCurrent", 		  	"func_146456_p");
			updateButtons = 
					ReflectionHelper.findMethod(GuiScreenBook.class, "updateButtons", 				"func_146464_h");
			sendBook=ReflectionHelper.findMethod(GuiScreenBook.class, "sendBookToServer", 			"func_146462_a", boolean.class);
			addPage =ReflectionHelper.findMethod(GuiScreenBook.class, "addNewPage", 				"func_146461_i");
		}
		private void markDirty() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			isMod.set(book, true);
			updateButtons.invoke(book);
		}
		@Override
		public Varargs invoke(Varargs args) {
			try {
				switch (op) {
				case sign:
					String newTitle = args.checkjstring(1);
					newTitle = newTitle.substring(0, Math.min(newTitle.length(), 16));
					bookTitle.set(book, newTitle);
					markDirty();
					sendBook.invoke(book, true);
					AdvancedMacros.getMinecraft().displayGuiScreen(null);
					break;
				case save:
					sendBook.invoke(book, false);
					break;
				case getTitle:
					return valueOf((String)bookTitle.get(book));

				case isSigned:
					return valueOf(!isUnsigned.getBoolean(book));

				case getText:
					return LuaValue.valueOf((String)getTxt.invoke(book));

				case setText:
					setTxt.invoke(book, "");
					System.out.println(insert.getParameterTypes());
					insert.invoke(book, args.checkjstring(1));
					markDirty();
					return NONE;

				case getPages:{
					int p = currPage.getInt(book);
					LuaTable out = new LuaTable();
					for(int i =0; i<pages.getInt(book); i++) {
						currPage.setInt(book, i);
						out.set(i+1, (String)getTxt.invoke(book));
					}
					currPage.setInt(book, p);
					return out;
				}
				case setPages:{
					int p = currPage.getInt(book);
					LuaTable in = args.checktable(1);
					currPage.setInt(book, 0);
					for(int i =0; i< in.length(); i++) {
						if(i>0) {
							int cP = currPage.getInt(book);
							int tP = pages.getInt(book);
							if(cP < tP -1) {
								currPage.set(book, cP+1);
							}else if(isUnsigned.getBoolean(book)) {
								addPage.invoke(book);
								cP = currPage.getInt(book);
								tP = pages.getInt(book);
								if(cP < tP -1) 
									currPage.set(book, cP+1);
								else
									return FALSE;
							}
						}
						setTxt.invoke(book, "");
						insert.invoke(book, in.get(i+1).checkjstring());
					}
					currPage.setInt(book, p);
					markDirty();
					return NONE;
				}
				case addPage:{
					int old = pages.getInt(book);
					addPage.invoke(book);
					return valueOf(pages.getInt(book) != old);
				}
				case nextPage:{
					int cP = currPage.getInt(book);
					int tP = pages.getInt(book);
					if(cP < tP -1) {
						currPage.set(book, cP+1);
					}else if(isUnsigned.getBoolean(book)) {
						addPage.invoke(book);
						cP = currPage.getInt(book);
						tP = pages.getInt(book);
						if(cP < tP -1) 
							currPage.set(book, cP+1);
						else
							return FALSE;
					}
					return TRUE;
				}
				case prevPage:{
					int cP = currPage.getInt(book);
					int tP = pages.getInt(book);
					if(cP > 0)
						currPage.setInt(book, cP-1);
					else
						return FALSE;
					return TRUE;
				}
				case currentPage:{
					return valueOf(currPage.getInt(book)+1);
				}
				case gotoPage:{
					currPage.setInt(book, Math.min(Math.max(0, args.checkint(1)-1), pages.getInt(book)));
					return NONE;
				}
				case pageCount:{
					return valueOf(pages.getInt(book));
				}
				case getAuthor:{
					ItemStack bookData = (ItemStack) bookObj.get(book);
					return valueOf(bookData.getTagCompound().getString("author"));
				}
				default:
					throw new LuaError("Unimplemented function '"+op.name()+"'");
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new LuaError(e);
			}
			return NONE;
		}
	}
	private static class DoCommand extends VarArgFunction {
		CommandOp op;
		GuiCommandBlock cb;
		Field tecb = ReflectionHelper.findField(GuiCommandBlock.class, "commandBlock",     "g", "field_184078_g");
		Field tf   = ReflectionHelper.findField(GuiCommandBlock.class, "commandTextField", "a", "field_146485_f");
		Field conditional = ReflectionHelper.findField(GuiCommandBlock.class, "conditional", "z", "field_184084_y");
		Field automatic   = ReflectionHelper.findField(GuiCommandBlock.class, "automatic", "A", "field_184085_z");
		Field mode        = ReflectionHelper.findField(GuiCommandBlock.class, "commandBlockMode", "x", "field_184082_w");
		Field output      = ReflectionHelper.findField(GuiCommandBlock.class, "previousOutputTextField", "f", "field_146486_g");
		Field track       = ReflectionHelper.findField(GuiCommandBlock.class, "trackOutput", "w", "field_175389_t");

		Method updateMode = ReflectionHelper.findMethod(GuiCommandBlock.class, "updateMode", "func_184073_g");
		//Method updateGui = ReflectionHelper.findMethod(GuiCommandBlock.class, "updateGui", "a");
		Method updateTrack=ReflectionHelper.findMethod(GuiCommandBlock.class, "updateCmdOutput", "func_175388_a");
		Method updateConditional=
				            ReflectionHelper.findMethod(GuiCommandBlock.class, "updateConditional", "func_184077_i");
		Method updateAutomatic = ReflectionHelper.findMethod(GuiCommandBlock.class, "updateAutoExec", "func_184076_j");
		GuiTextField text;
		public DoCommand(CommandOp op, GuiCommandBlock cb) {
			super();
			this.op = op;
			this.cb = cb;
			tecb.setAccessible(true);
			tf.setAccessible(true);
			conditional.setAccessible(true);
			automatic.setAccessible(true);
			mode.setAccessible(true);
			output.setAccessible(true);
			track.setAccessible(true);
			cb.initGui();
			cb.updateGui();
			try {
				long timeout = System.currentTimeMillis()+400;
				while(text==null) {
					text = (GuiTextField) tf.get(cb);
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {break;}
					if(timeout < System.currentTimeMillis())
						break;
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new LuaError( e );
			}
			//cb.updateGui();
		}
		@Override
		public Varargs invoke(Varargs args) {
			try {
				//TileEntityCommandBlock block = (TileEntityCommandBlock) tecb.get(cb);
				text = (GuiTextField) tf.get(cb);
				switch (op) {
				case isConditional:
					return valueOf(conditional.getBoolean(cb));
				case getMode:
					switch ((TileEntityCommandBlock.Mode) mode.get(cb)) {
					case AUTO:
						return valueOf("repeat");
					case REDSTONE:
						return valueOf("impulse");
					case SEQUENCE:
						return valueOf("chain");
					}
					throw new LuaError("Unknown mode...");
				case getText:
					return valueOf(text.getText());
				case setMode:
					switch (args.checkjstring(1)) {
					case "repeat":
						mode.set(cb, TileEntityCommandBlock.Mode.AUTO);
						break;
					case "impulse":
						mode.set(cb, TileEntityCommandBlock.Mode.REDSTONE);
						break;
					case "chain":
						mode.set(cb, TileEntityCommandBlock.Mode.SEQUENCE);
						break;
					}
					updateMode.invoke(cb);
					return NONE;
				case setText:
					text.setText(args.checkjstring(1));
					return NONE;
				case done:
					done();
					return NONE;
				case isNeedsRedstone:
					return valueOf(automatic.getBoolean(cb));
				case isTrackOutput:
					return valueOf(track.getBoolean(cb));
				case setChain:
					mode.set(cb, TileEntityCommandBlock.Mode.SEQUENCE);
					updateMode.invoke(cb);
					return NONE;
				case setConditional:
					conditional.setBoolean(cb, args.optboolean(1, true));
					updateConditional.invoke(cb);
					return NONE;
				case setImpulse:
					mode.set(cb, TileEntityCommandBlock.Mode.REDSTONE);
					updateMode.invoke(cb);
					break;
				case setNeedsRedstone:
					automatic.setBoolean(cb, !args.optboolean(1, true));
					updateAutomatic.invoke(cb);
					return NONE;
				case setRepeat:
					mode.set(cb, TileEntityCommandBlock.Mode.AUTO);
					updateMode.invoke(cb);
					return NONE;
				case setTrackOutput:
					track.setBoolean(cb, args.optboolean(1, true));
					updateTrack.invoke(cb);
					return NONE;
				case getOutput:
					return valueOf(((GuiTextField)output.get(cb)).getText());
				default:
					throw new LuaError("Unimplemented function '"+op.name()+"'");
				}
			} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
				throw new LuaError(e);
			}
			return NONE;
		}
		private void done() throws IllegalArgumentException, IllegalAccessException {
			TileEntityCommandBlock block = (TileEntityCommandBlock) tecb.get(cb);
			Minecraft mc = AdvancedMacros.getMinecraft();
			
			PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
			CommandBlockBaseLogic commandblockbaselogic = block.getCommandBlockLogic();
			commandblockbaselogic.fillInInfo(packetbuffer);
			packetbuffer.writeString(text.getText());
			packetbuffer.writeBoolean(commandblockbaselogic.shouldTrackOutput());
			packetbuffer.writeString(((TileEntityCommandBlock.Mode) mode.get(cb)).name());
			packetbuffer.writeBoolean(this.conditional.getBoolean(cb));
			packetbuffer.writeBoolean(this.automatic.getBoolean(cb));
			mc.getConnection().sendPacket(new CPacketCustomPayload("MC|AutoCmd", packetbuffer));

			if (!commandblockbaselogic.shouldTrackOutput())
			{
				commandblockbaselogic.setLastOutput((ITextComponent)null);
			}

			mc.displayGuiScreen((GuiScreen)null);
		}
	}
	private static enum RepairOp {
		setName,
		getName,
		getCost;

		public String[] getDocLocation() {
			return new String[] {"guiEvent#anvil", name()};
		}
	}
	private static enum TradeOp {
		getTrades,
		getType;
		public String[] getDocLocation() {
			return new String[] {"guiEvent#villager", name()};
		}
	}
	private static enum EnchantOp {
		pickOption,
		getOptions;
		public String[] getDocLocation() {
			return new String[] {"guiEvent#enchant", name()};
		}
	}
	private static enum SignOp {
		setLine,
		setLines,
		done,
		getLines;
		public String[] getDocLocation() {
			return new String[] {"guiEvent#sign", name()};
		}
	}
	private static enum BookOp {
		setText,
		getText,
		getPages,
		setPages,
		//		setTitle,
		getAuthor,
		addPage,
		nextPage,
		prevPage,
		save,
		getTitle,
		sign,
		currentPage,
		pageCount,
		isSigned, gotoPage;
		public String[] getDocLocation() {
			return new String[] {"guiEvent#book", name()};
		}
	}
	private static enum CommandOp {
		getText,
		setText,
		getMode,
		setImpulse,
		setChain,
		setRepeat,
		setConditional,
		getOutput,
		isConditional,
		setNeedsRedstone,
		isNeedsRedstone,
		setTrackOutput,
		isTrackOutput,
		done,
		setMode;
		public String[] getDocLocation() {
			return new String[] {"guiEvent#commandBlock", name()};
		}
	}
}
