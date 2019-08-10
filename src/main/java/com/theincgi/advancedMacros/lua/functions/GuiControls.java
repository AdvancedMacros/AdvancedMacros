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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.AbstractCommandBlockScreen;
import net.minecraft.client.gui.screen.CommandBlockScreen;
import net.minecraft.client.gui.screen.EditBookScreen;
import net.minecraft.client.gui.screen.EditSignScreen;
import net.minecraft.client.gui.screen.EnchantmentScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.AnvilScreen;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.MerchantScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.EnchantmentContainer;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.tileentity.CommandBlockLogic;
import net.minecraft.tileentity.CommandBlockTileEntity;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class GuiControls {



	public static LuaValue load(Screen gCon) {
		LuaTable controls = new LuaTable();
		if(gCon instanceof AnvilScreen) {
			AnvilScreen gr = (AnvilScreen) gCon;
			for (RepairOp r : RepairOp.values()) {
				controls.set(r.name(), new CallableTable(r.getDocLocation(), new DoRepair(r, gr)));
			}
		}else if(gCon instanceof MerchantScreen) {
			MerchantScreen gm = (MerchantScreen) gCon;
			for (TradeOp r : TradeOp.values()) {
				controls.set(r.name(), new CallableTable(r.getDocLocation(), new DoTrade(r, gm)));
			}

		}else if(gCon instanceof EnchantmentScreen) {
			EnchantmentScreen ge = (EnchantmentScreen) gCon;
			for (EnchantOp r : EnchantOp.values()) {
				controls.set(r.name(), new CallableTable(r.getDocLocation(), new DoEnchant(r, ge)));
			}
		}else if(gCon instanceof EditSignScreen) {
			EditSignScreen es = (EditSignScreen) gCon;
			for (SignOp r : SignOp.values()) {
				controls.set(r.name(), new CallableTable(r.getDocLocation(), new DoSign(r, es)));
			}
		}else if(gCon instanceof EditBookScreen) {
			EditBookScreen bk = (EditBookScreen) gCon;
			for (BookOp r : BookOp.values()) {
				controls.set(r.name(), new CallableTable(r.getDocLocation(), new DoBook(r, bk)));
			} 
			//TODO read book screen thing
		}else if(gCon instanceof CommandBlockScreen) {
			CommandBlockScreen cb = (CommandBlockScreen) gCon;
			for (CommandOp r : CommandOp.values()) {
				controls.set(r.name(), new CallableTable(r.getDocLocation(), new DoCommand(r, cb)));
			}
		}else if(gCon instanceof ChestScreen) {
			ChestScreen gc = (ChestScreen) gCon;
			for(ChestOp op : ChestOp.values()) {
				controls.set(op.name(), new CallableTable(op.getDocLocation(), new DoChestOp(op, gc)));
			}
		}//else if(gCon instanceof ReadBookScreen) {
//			ReadBookScreen rbs = (ReadBookScreen) gCon;
//			for(ReadBookOp op : ReadBookOp.values()) {
//				controls.set(op.name(),  new CallableTable(op.getDocLocation(), new DoReadBook(op, rbs)));
//			}
//		}
		Screen whenOpened = gCon;
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
		AnvilScreen gr;
		Method renameItem = ObfuscationReflectionHelper.findMethod(AnvilScreen.class, /*"renameItem",*/ "func_214075_a", new Class[] {}); //TESTME is this the right func? should be
		
		//Field anvil = ReflectionHelper.findField(AnvilScreen.class, "anvil", "w", "field_147092_v");
		Field nameField = ObfuscationReflectionHelper.findField(AnvilScreen.class, "field_147091_w");
		Field maxCost   = ObfuscationReflectionHelper.findField(RepairContainer.class, "field_82854_e");
		public DoRepair(RepairOp op, AnvilScreen gr) {
			super();
			this.op = op;
			this.gr = gr;
		}

		@Override
		public Varargs invoke(Varargs args) {
			switch (op) {
			case getCost:{
				try {
					maxCost.setAccessible(true);
					RepairContainer cr = (RepairContainer) gr.getContainer();// anvil.get(gr);
					return valueOf(((IntReferenceHolder)maxCost.get(cr)).get());
				} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
					throw new LuaError(e);
				}
			}
			case getName:
				try {
					nameField.setAccessible(true);
					TextFieldWidget gtf = (TextFieldWidget) nameField.get(gr);
					return valueOf(gtf.getText());
				} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
					throw new LuaError(e);
				}
			case setName:
				try {
					nameField.setAccessible(true);
					TextFieldWidget gtf = (TextFieldWidget) nameField.get(gr);
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
		MerchantScreen gm;
		public DoTrade(TradeOp op, MerchantScreen gm) {
			super();
			this.op = op;
			this.gm = gm;
		}
		@Override
		public Varargs invoke(Varargs args) {
			switch (op) {
			case getTrades:{
				LuaTable trades = new LuaTable();
				MerchantOffers offers = gm.getContainer().func_217051_h();
				for(int i = 0; i<offers.size(); i++) {
					MerchantOffer mr = offers.get(i);
					LuaTable t = new LuaTable();
					LuaTable inputs = new LuaTable();
					t.set("input", inputs);
					inputs.set(1,   Utils.itemStackToLuatable(mr.func_222218_a()       	)); //first stack
					inputs.set(2,   Utils.itemStackToLuatable(mr.func_222202_c() 		)); //second stack
					t.set("output", Utils.itemStackToLuatable(mr.func_222200_d()      	)); //item sold
					t.set("uses", mr.func_222213_g() 									 ); //uses remaining
					trades.set(i+1, t);
				}
				return trades;
			}
			case getType:
				return valueOf(gm.getTitle().getUnformattedComponentText());
			default:
				throw new LuaError("Unimplemented function '"+op.name()+"'");
			}
		}
	}
	private static class DoEnchant extends VarArgFunction {
		EnchantOp op;
		EnchantmentScreen ge;

		public DoEnchant(EnchantOp op, EnchantmentScreen ge) {
			super();
			this.op = op;
			this.ge = ge;
		}
		@Override
		public Varargs invoke(Varargs args) {
				EnchantmentContainer ce = ge.getContainer();
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
		}
	}
	private static class DoSign extends VarArgFunction {
		SignOp op;
		EditSignScreen es;
		Field tileSign = ObfuscationReflectionHelper.findField(EditSignScreen.class, "field_146848_f");

		public DoSign(SignOp op, EditSignScreen es) {
			super();
			this.op = op;
			this.es = es;
		}
		@Override
		public Varargs invoke(Varargs args) {
			try {
				SignTileEntity ts = (SignTileEntity) tileSign.get(es);
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
					//TESTME es.mc is gone! prob ok tho if(es.mc==null) es.mc = AdvancedMacros.getMinecraft();
					ts.markDirty();
					AdvancedMacros.getMinecraft().displayGuiScreen(null);
					return NONE;
				case setLine:
					ts.signText[args.checkint(1)] = new StringTextComponent(args.optjstring(2, ""));
					return NONE;
					//				case setFormatedLine:
					//					ts.signText[args.checkint(1)] = new TextComponentString(Utils.toMinecraftColorCodes(args.checkjstring(2)));
					//					return NONE; //lost when sent to server
					//case setFormatedLines:
				case setLines:{
					if(args.arg1().istable()) {
						LuaTable t = args.checktable(1);
						for(int i=1; i<=ts.signText.length; i++) {
							ts.signText[i-1] = new StringTextComponent(t.get(i).optjstring(""));
						}
					}else {
						for(int i = 1; i<=ts.signText.length; i++)
							ts.signText[i-1] = new StringTextComponent(args.optjstring(i, ""));
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
	//nbt is good enough for now
//	private static class DoReadBook extends VarArgFunction {
//		ReadBookOp op;
//		ReadBookScreen rbs;
//		public DoReadBook(ReadBookOp op, ReadBookScreen rbs) {
//			this.op = op;
//			this.rbs = rbs;
//		}
//		
//		@Override
//		public Varargs invoke() {
//			switch (op) {
//			case currentPage:
//				
//			case getAuthor:
//			case getText:
//			case getTitle:
//			case gotoPage:
//			case pageCount:
//			default:
//				break;
//			}
//		}
//	}
	
	//TODO readbookscreen
	private static class DoBook extends VarArgFunction {
		BookOp op;
		EditBookScreen book;
		Method insert;
		Method setTxt;
		Method getTxt;
		Method updateButtons;
		Method sendBook;
		Method addPage;
		Method getPageCount;
		
		Field  currPage  = ObfuscationReflectionHelper.findField(EditBookScreen.class, "field_214237_f");//"currPage",       "y", "field_146484_x");
		Field  isMod     = ObfuscationReflectionHelper.findField(EditBookScreen.class, "field_214234_c");//"bookIsModified", "s", "field_146481_r");
		//true because ReadBookScreen is for signed now //Field  isUnsigned= ObfuscationReflectionHelper.findField(EditBookScreen.class, "bookIsUnsigned", "i", "field_146475_i");
		Field  bookTitle = ObfuscationReflectionHelper.findField(EditBookScreen.class, "field_214239_h");//"bookTitle",      "A", "field_146482_z");
		Field  bookObj   = ObfuscationReflectionHelper.findField(EditBookScreen.class, "field_214233_b");//"book",           "h", "field_146474_h");
		
		//title
		//sign
		//save
		//setText
		//getText
		//updateButtons
		//addPage
		//currentPage
		//numPages
		//isMod aka is dirty
		
		
		//setPages
		//getPages
		
		
		//		Field  gettingSigned = 
		//				           ReflectionHelper.findField(GuiScreenBook.class, "bookGettingSigned", "t", "field_146480_s");

		public DoBook(BookOp op, EditBookScreen book) {
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

			getPageCount.setAccessible(true);
			currPage.setAccessible(true);
			isMod.setAccessible(true);
			//isUnsigned.setAccessible(true);
			bookTitle.setAccessible(true);
			//			gettingSigned.setAccessible(true);
		}
		private void loadReflects() {
			getPageCount 	= ObfuscationReflectionHelper.findMethod(EditBookScreen.class, "func_214199_a");  // formerly: "bookTotalPages", "x", "field_146476_w");
			//insert 			= ObfuscationReflectionHelper.findMethod(EditBookScreen.class, "pageInsertIntoCurrent", 		"func_146459_b", String.class);
			setTxt 			= ObfuscationReflectionHelper.findMethod(EditBookScreen.class, "func_214217_j");//"pageSetCurrent", 		  	"func_146457_a", String.class);
			getTxt 			= ObfuscationReflectionHelper.findMethod(EditBookScreen.class, "func_214193_h");//"pageGetCurrent", 		  	"func_146456_p");
			updateButtons = 
					 ObfuscationReflectionHelper.findMethod(EditBookScreen.class, "func_214229_d");//"updateButtons", 				"func_146464_h");
			sendBook=ObfuscationReflectionHelper.findMethod(EditBookScreen.class, "func_214198_a");//"sendBookToServer", 			"func_146462_a", boolean.class);
			addPage =ObfuscationReflectionHelper.findMethod(EditBookScreen.class, "func_214215_f");//"addNewPage", 				"func_146461_i");
		}
		private void markDirty() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			isMod.set(book, true);
			updateButtons.invoke(book);
		}
		@Override
		public Varargs invoke(Varargs args) {
			try { //WrittenBookItem; ClientPlayerEntity
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
//				case getTitle:
//					return valueOf((String)bookTitle.get(book));

				case isSigned:
					return FALSE;

				case getText:
					return LuaValue.valueOf((String)getTxt.invoke(book));

				case setText:
					setTxt.invoke(book, "");
					System.out.println(insert.getParameterTypes());
					insert.invoke(book, args.checkjstring(1));      //insert is used to trim the text in a lazy way, setText doesnt
					markDirty();
					return NONE;

				case getPages:{
					int p = currPage.getInt(book);
					LuaTable out = new LuaTable();
					int pages = (int) getPageCount.invoke(book);
					for(int i =0; i<pages; i++) {
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
							int tP = (int) getPageCount.invoke(book);
							if(cP < tP -1) {
								currPage.set(book, cP+1);
							}else{
								addPage.invoke(book);
								cP = currPage.getInt(book);
								tP = (int) getPageCount.invoke(book);
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
					int old = (int) getPageCount.invoke(book);
					addPage.invoke(book);
					return valueOf((int) getPageCount.invoke(book) != old);
				}
				case nextPage:{
					int cP = currPage.getInt(book);
					int tP = (int) getPageCount.invoke(book);
					if(cP < tP -1) {
						currPage.set(book, cP+1);
					}else {
						addPage.invoke(book);
						cP = currPage.getInt(book);
						tP = (int) getPageCount.invoke(book);
						if(cP < tP -1) 
							currPage.set(book, cP+1);
						else
							return FALSE;
					}
					return TRUE;
				}
				case prevPage:{
					int cP = currPage.getInt(book);
					int tP = (int) getPageCount.invoke(book);
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
					currPage.setInt(book, Math.min(Math.max(0, args.checkint(1)-1), (int) getPageCount.invoke(book)));
					return NONE;
				}
				case pageCount:{
					return valueOf((int) getPageCount.invoke(book));
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
		CommandBlockScreen cb;
		Field tecb = ObfuscationReflectionHelper.findField(CommandBlockScreen.class, "field_184078_g");
		Field tf   = ObfuscationReflectionHelper.findField(AbstractCommandBlockScreen.class, "field_195237_a");
		Field conditional = ObfuscationReflectionHelper.findField(CommandBlockScreen.class, "field_184084_y");
		Field automatic   = ObfuscationReflectionHelper.findField(CommandBlockScreen.class, "field_184085_z");
		Field mode        = ObfuscationReflectionHelper.findField(CommandBlockScreen.class, "field_184082_w");
		Field output      = ObfuscationReflectionHelper.findField(AbstractCommandBlockScreen.class, "field_195239_f");
		Field track       = ObfuscationReflectionHelper.findField(CommandBlockScreen.class, "field_195242_i");
		
		Method sendUpdatePacket;

		//updateGui is public
//		Method updateMode = ObfuscationReflectionHelper.findMethod(CommandBlockScreen.class, "updateMode", "func_184073_g");
//		//Method updateGui = ReflectionHelper.findMethod(GuiCommandBlock.class, "updateGui", "a");
//		Method updateTrack=ObfuscationReflectionHelper.findMethod(CommandBlockScreen.class, "updateCmdOutput", "func_175388_a");
//		Method updateConditional=
//				ObfuscationReflectionHelper.findMethod(CommandBlockScreen.class, "updateConditional", "func_184077_i");
//		Method updateAutomatic = ObfuscationReflectionHelper.findMethod(CommandBlockScreen.class, "updateAutoExec", "func_184076_j");
		TextFieldWidget text;
		public DoCommand(CommandOp op, CommandBlockScreen cb) {
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
			sendUpdatePacket = ObfuscationReflectionHelper.findMethod(CommandBlockScreen.class, "func_195235_a");
			sendUpdatePacket.setAccessible(true);
			//cb.initGui(); //TESTME command blocks
			cb.updateGui();
			try {
				long timeout = System.currentTimeMillis()+400;
				while(text==null) {
					text = (TextFieldWidget) tf.get(cb);
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
				text = (TextFieldWidget) tf.get(cb);
				switch (op) {
				case isConditional:
					return valueOf(conditional.getBoolean(cb));
				case getMode:
					switch ((CommandBlockTileEntity.Mode) mode.get(cb)) {
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
						mode.set(cb, CommandBlockTileEntity.Mode.AUTO);
						break;
					case "impulse":
						mode.set(cb, CommandBlockTileEntity.Mode.REDSTONE);
						break;
					case "chain":
						mode.set(cb, CommandBlockTileEntity.Mode.SEQUENCE);
						break;
					}
					cb.updateGui();
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
					mode.set(cb, CommandBlockTileEntity.Mode.SEQUENCE);
					cb.updateGui();
					return NONE;
				case setConditional:
					conditional.setBoolean(cb, args.optboolean(1, true));
					cb.updateGui();
					return NONE;
				case setImpulse:
					mode.set(cb, CommandBlockTileEntity.Mode.REDSTONE);
					cb.updateGui();
					break;
				case setNeedsRedstone:
					automatic.setBoolean(cb, !args.optboolean(1, true));
					cb.updateGui();
					return NONE;
				case setRepeat:
					mode.set(cb, CommandBlockTileEntity.Mode.AUTO);
					cb.updateGui();
					return NONE;
				case setTrackOutput:
					track.setBoolean(cb, args.optboolean(1, true));
					cb.updateGui();
					return NONE;
				case getOutput:
					return valueOf(((TextFieldWidget)output.get(cb)).getText());
				default:
					throw new LuaError("Unimplemented function '"+op.name()+"'");
				}
			} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
				throw new LuaError(e);
			}
			return NONE;
		}
		private void done() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			CommandBlockTileEntity block = (CommandBlockTileEntity) tecb.get(cb);
			CommandBlockLogic commandblockbaselogic = block.getCommandBlockLogic();
			sendUpdatePacket.invoke(cb, commandblockbaselogic);
			if (!commandblockbaselogic.shouldTrackOutput())
			{
				commandblockbaselogic.setLastOutput((ITextComponent)null);
			}

			AdvancedMacros.getMinecraft().displayGuiScreen((Screen)null);
		}
	}
	private static class DoChestOp extends VarArgFunction {
		ChestScreen gc;
		ChestOp op;
		private static Field playerInv = ObfuscationReflectionHelper.findField(ContainerScreen.class, "field_213127_e");
		//private static Field title = ObfuscationReflectionHelper.findField(ChestScreen.class, "lowerChestInventory", "x", "field_147015_w");
		static {
			playerInv.setAccessible(true);
		}
		public DoChestOp(ChestOp op, ChestScreen gc) {
			this.op = op;
			this.gc = gc;
		}
		@Override
		public Varargs invoke(Varargs args) {
			try {
				switch (op) {
				case getLowerLabel: 
					return valueOf( ((PlayerInventory)playerInv.get(gc)).getDisplayName().getUnformattedComponentText() );
				case getUpperLabel:
					return valueOf(	gc.getTitle().getUnformattedComponentText() );
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
			return NONE;
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
	
//	private static enum ReadBookOp{
//		getTitle,
//		getAuthor,
//		getText,
//		gotoPage,
//		currentPage,
//		pageCount;
//		public String[] getDocLocation() {
//			return new String[] {"guiEvent#readBook", name()};
//		}
//	}
	private static enum BookOp {
		setText,
		getText,
		getPages,
		setPages,
		//		setTitle,
		//getAuthor,
		addPage,
		nextPage,
		prevPage,
		save,
		//getTitle,
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
	private static enum ChestOp{
		getUpperLabel,
		getLowerLabel;

		public String[] getDocLocation() {
			return new String[] {"guiEvent#chest", name()};
		}
	}
}
