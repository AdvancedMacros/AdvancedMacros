function initializeCoreMod() {
	//"borrowed" some code for ref
	Opcodes = Java.type("org.objectweb.asm.Opcodes");
	// ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
	
	InsnList = Java.type("org.objectweb.asm.tree.InsnList");
	LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
	AbstractInsnNode = Java.type("org.objectweb.asm.tree.AbstractInsnNode");
	InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
	IntInsnNode  = Java.type("org.objectweb.asm.tree.IntInsnNode");
	MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
	VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
	JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
	LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
	ACC_PUBLIC = Opcodes.ACC_PUBLIC;

	INVOKESTATIC = Opcodes.INVOKESTATIC;
	INVOKEVIRTUAL = Opcodes.INVOKEVIRTUAL;

	ALOAD = Opcodes.ALOAD;
	ILOAD = Opcodes.ILOAD;
	BIPUSH = Opcodes.BIPUSH
	FLOAD = Opcodes.FLOAD;
	DLOAD = Opcodes.DLOAD;
	ISTORE = Opcodes.ISTORE;
	RETURN = Opcodes.RETURN;
	ARETURN = Opcodes.ARETURN;
	IRETURN = Opcodes.IRETURN;
	DRETURN = Opcodes.DRETURN;
	NEW = Opcodes.NEW;
	ACONST_NULL = Opcodes.ACONST_NULL;
	ICONST_0 = Opcodes.ICONST_0;
	
	IFEQ = Opcodes.IFEQ;
	IFNE = Opcodes.IFNE;
	IF_ACMPEQ = Opcodes.IF_ACMPEQ;
	
	GETFIELD = Opcodes.GETFIELD;
	GETSTATIC = Opcodes.GETSTATIC;

	GOTO = Opcodes.GOTO;

	LABEL = AbstractInsnNode.LABEL;
	METHOD_INSN = AbstractInsnNode.METHOD_INSN;
	
	log("INITIALIZING CORE MOD")
	
	return {
					"NewChatGui#setChatLine":{
								"target": {
										"type": "METHOD",
										"class": "net.minecraft.client.gui.NewChatGui",
										"methodName": "func_146237_a",
										"methodDesc" : "(Lnet/minecraft/util/text/ITextComponent;IIZ)V"  //IMPORTANT (probably) / not . for packages here
								},
								"transformer": function(methodNode) {         //noticed that this is the method node, not the class node, yay, no more itterating for the correct method
										var instructions = methodNode.instructions;
										log("NewChatGui#setChatLine - transformer");
										injectChatLimit( instructions ); //line 146 in NewChatGui and 153
										return methodNode; //also important
								}
					},
					"Screen#handleComponentClicked":{
									"target":{
											"type":"METHOD",
											"class":"net.minecraft.client.gui.screen.Screen",
											"methodName":"handleComponentClicked",
											"methodDesc":"(Lnet/minecraft/util/text/ITextComponent;)Z"
									},
									"transformer": function(methodNode) {
											var instructions = methodNode.instructions;
											log("Screen#handleComponentClicked - transformer");
											injectLuaTextComponentCaseForClickEvent( instructions );
											return methodNode;
									}
					}
					
	}
}

function log( message ) {
	print("###[AM-CORE-MOD] "+message);
}

function injectChatLimit( instructions ) {

	//    BIPUSH 100     - is the instruction that puts the 100 into the stack for comparison
	//                            this is the hard coded limit we will be replacing
	//                            by removing this instruction and putting a MethodInsnNode that calls
	//                            com.example.examplemod.ASMHooks#getLimit() [static]
    //                            the chat message count will be compared with it's result instead of the hard coded constant 153
	var opsRemaining = 2;            //gonna exit out as soon as two spots have been edited to exit quickly as we can
	var iArr = instructions.toArray();
	var iLen = instructions.size();
	//log("Type of iArr: "+typeof(iArr));
	//log( Object.keys( iArr ) );
	var found = false;
	
	for(i = 0; i < iLen; i++ ) {  //for(AbstractInsnNode instr : instructions)
		var instr = instructions.get( i );
		if( instr.getOpcode() === BIPUSH ){ //located the BIPUSH instruction, but we want to make sure it's the correct one, so we check that it's loading 100
			if(instr.operand == 100){
				var target = instr;  //we will be removing this later (this is the BIPUSH 100 instruction)
				
				var toInject = new InsnList();
				
				found = true;
				toInject.add(
					new MethodInsnNode(
					INVOKESTATIC, "com/theincgi/advancedMacros/asm/ASMHooks", "getChatBufferSize", "()I", false
					) //generated that by writing the line:
					//ASMHooks.getChatBufferSize(); and using the ASMify button in the bytecode view in eclipse with this plugins
					//http://andrei.gmxhome.de/bytecode/index.html
					//really handy
				);
				
				// //only had the one instruction, so a list is kinda overkill, but that's ok
				
				instructions.insertBefore(target, toInject); //you can check out these methods from the org.objectweb.asm javadocs btw
				instructions.remove(target); //as promised
				if( --opsRemaining === 0 ) {
					log("AdvancedMacros: max chat lines is now editable :)"); //some good news
					return;
				}
			}
		}
	}
	log("AdvancedMacros: unable to enable the chat lines limit edit");
}


function injectLuaTextComponentCaseForClickEvent( instructions ){
		/*
		 public boolean handleComponentClicked(ITextComponent p_handleComponentClicked_1_) 
			....
			} else if (clickevent.getAction() == ClickEvent.Action.SUGGEST_COMMAND) {
               this.insertText(clickevent.getValue(), true);
            } else if (clickevent.getAction() == ClickEvent.Action.RUN_COMMAND) {
               this.sendMessage(clickevent.getValue(), false);
   ***   }	else if( ASMHooks.tryLuaTextComponent( p_handleComponentClicked_1_ ) )	
            } else {  <- L33
               LOGGER.error("Don't know how to handle {}", (Object)clickevent);
            } <-L24
		*/
		//above is the effective edit to the code, 
		//the *** are the lines being added in
		//Here is the un-edited byte code
		/*
		   GOTO L24
	   L31
		LINENUMBER 293 L31
	   FRAME SAME
		ALOAD 2: clickevent
		INVOKEVIRTUAL ClickEvent.getAction() : ClickEvent$Action
		GETSTATIC ClickEvent$Action.RUN_COMMAND : ClickEvent$Action        
		IF_ACMPNE L33                
	   L34
		LINENUMBER 294 L34           
		ALOAD 0: this
		ALOAD 2: clickevent
		INVOKEVIRTUAL ClickEvent.getValue() : String
		ICONST_0
		INVOKEVIRTUAL Screen.sendMessage(String, boolean) : void         <-Will be looking for this call
		GOTO L24
	   L33                                           <-Inject if(isTextCompCheck) {clickTextComp()}else{
		LINENUMBER 296 L33
	   FRAME SAME
		GETSTATIC Screen.LOGGER : Logger
		LDC "Don't know how to handle {}"
		ALOAD 2: clickevent
		INVOKEINTERFACE Logger.error(String, Object) : void
	   L24
		*/
		log("Locating INVOKEVIRTUAL Screen.SendMessage");
		var iArr = instructions.toArray();
		var iLen = instructions.size();
		var target;
		for(i = 0; i < iLen; i++ ) {
			var instr = instructions.get( i );
			if( instr.getOpcode() === INVOKEVIRTUAL ) {
				if( instr.name === "sendMessage" && 
				   instr.desc  === "(Ljava/lang/String;Z)V" ) {
					   log("Located injection point");
					   target = instr;
					   break;
				   }
			}
		}
		target = target.getNext(); //should be GOTO L24 next
		if(!target.getOpcode() === GOTO){log("Mistake locating injection point, aborting"); return;}
		var endOfIfElse = target.label; //save for later
		target = target.getNext(); //should be Lable 33
		if(!target.getOpcode() === LABEL){log("Mistake locating injection point, aborting"); return;}
		target = target.getNext(); //the LineNumberNode
		
		//code will be injected after this label
		
		log("DEBUG: " + target.getNext());
		
		
		var myLabel = new LabelNode();
		var toInject = new InsnList();
		//if( ASMHooks.isLuaTextComponent ( obj ) )
		//obj needs to be on the stack, then call static func, then the ifstatment with IFEQ
		
		
		toInject.add(new VarInsnNode(ALOAD, 2) ); //clickEvent
		toInject.add(new MethodInsnNode(
			INVOKESTATIC,
			"com/theincgi/advancedMacros/asm/ASMHooks",
			"tryLuaTextComponent",
			"(Ljava/lang/Object;)Z",
			false
		));
		toInject.add(new JumpInsnNode(IFEQ, myLabel)) //jumps if false, continues if true
		
		toInject.add(new JumpInsnNode(GOTO, endOfIfElse));
		toInject.add(myLabel);
		
		
		instructions.insert( target, toInject );
		log("AdvancedMacros LuaTextComponents should now be clickable");
}