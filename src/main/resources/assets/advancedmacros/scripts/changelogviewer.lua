local args = {...}
--check if the gui should show and set the default value if missing
local settings = getSettings()
settings.changeLog = settings.changeLog or {}
local cl = settings.changeLog
cl.showOnStartup = cl.showOnStartup==nil or cl.showOnStartup
if not cl.showOnStartup then return end
cl.shownVersions = cl.shownVersions or {}
if cl.shownVersions[_MOD_VERSION] and args[1]~="force" then return end

local DIVIDER =
"--------------------------------------------------------------------------------"
-- CHANGE LOG -------------------------------------------------------------------
local changeLog = {
  "&eShow on startup? ", --do not remove
  "&BOffical discord: &7https://discord.gg/ga9Npym",
  "A clickable link can be found in the Mods menu in this mod's description",
  "&7"..DIVIDER, --keep
  "&b&BChange Log: &7version ".._MOD_VERSION, --do not remove
  "&bFeatures:",
  " - &blog&f now has an &e&&&f tag &6&&*&f for opening files &7(like screenshots)",
  "   the extra arg used may be a &6string&f &Bor&f &atable&f with fields &6click&f and",
  "   &doptionally &6hover",
  " - added &bnarrate&f( &6string: text&f<,&5boolean: interrupt&f >)",
  " - Added &aplayerDetails&f, this breaks out all of the fields from &bgetPlayer&f()",
  "   into separate functions. The following run on the &eMC thread&f by default:",
  "   - &bplayerDetails.getPotionEffects&f()",
  " - You can now use &battack&f(&6\"break\"&f) to stop attacking once a block is broken",
  "   &a&BTip: &fit returns a function which will wait until the block is done breaking.",
  "       if you don't need to do anything else while breaking you can do",
  "       &battack&f(&6\"break\"&f)()",
  " - A bunch of new &amath&f functions!",
  "   - &bmath.clamp&f( &7x&f, &7low&f, &7high&f ) &7makes sure x is in some range",
  "   - &amath.easings&f are a set of math functions which follow these rules:",
  "     - all functions take at least these args ( &bx&7<,&ba&7<,&bb&7>&7)",
  "       &bx&f is the input, &ba&f and &bb&f are the low and high parts of the",
  "       output range &7(they default to &d0&f and &d1&f)",
  "     - &bx&f is &ealways clamped&f between &d0&f and &d1",
  "     - the output value when &bx&f=<&d0&f will always be &da",
  "     - the output value when &bx&f=>&d1&f will always be &db",
  "   - &e&BAn interactive demo can be launched with: &bmath.easings.demo.show&f()",
  "   - &bmath.easings.bezier&f can take &d2&f extra args ( &7x<,a<,b<,&ei&7<,&ej&7>>>>&f )",
  "     see how &ei&f&&&ej&f change the curve with the link from",
  "     &amath.easings.demo.links.bezier &7(not a url)",
  "   - &bmath.easings.bounce&f &fcan take &d12&f extra args and &eeaseIn&f/&eeaseOut&f versions &d6&f extra ",
  "     &e&BIn a nutshell&f it lets you control the timing and height of each bounce,",
  "     check out the &Binteractive demo&f at the link from",
  "     &a&Bmath.easings.demo.links.bounce &7(not a url)",
  "     &7math.easings.bounce is a combination of the easeIn and easeOut parts",
  "   - there are additional functions in &amath.easings.easeIn&f and &amath.easings.easeOut",
  "   - But what are they for? They're used with animations to produce more natural results",
  "     there are plans to use these with &blook&f in a future update.",
  "&aBug Fixes:",
  " - &ahud2D&f's &btext&f elements now respect font size controls",
  " - &brunOnMC&f now throws &cerrors&f that occured in the minecraft thread",
  " - &dREPL&f doesn't loose char typed on text box focus",
  " - Fixed some issues where &bwaitTick&f could be called from the minecraft thread",
  "   &bfreezing&f the game",
  " - Built in waitTick commands were removed from &bopenInventory&f's controls since",
  "   it runs on the minecraft thread",
  " - Fixed an issue where &bopenInventory&f()&b.swap&f(&7a&f, &7b&f) would check the",
  "   wrong slots for items to determine if clicks were needed &7(off by one)",
  " - Things like &battack&f(&d0&f) work correctly now",
  "&eAdditional Notes:",
  " - &eChatFilter&f event's will not create new threads if they are all disabled",
  " - &dREPL&f is now version 1.2",
  " - &b&BgetPlayer&f&B() now runs on the &e&BMC thread.",
  " - You can launch &bmath.easings.demo.show&f() from &dREPL",
  
     
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 7.9.0",
  "&bFeatures:",
  " - A built in &eREPL&f has been added. It can be accessed with &eALT&f + the mod's keybinding.",
  "&aBug Fixes:",
  " - Removed a console spam from script elements",
  " - Gui text uses correct width and height for clicking",
  "&eAdditional Notes:",
  " - Added resource images for &eglobe.png&f and &esandbox.png&f",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 7.8.0",
  "&bFeatures:",
  " - New function &brunOnMC&f(&7function <, args...>&f)",
  " - Added &bgetMap&f() to &bopenInventory&f()'s controlls to match &61.14.4&f's functionality",
  " - Added &aluajava&f's buffs to this version.",
  " - Added &bluajava.findMethodByDescription&f(&7class, name, desc&f)",
  "   from 1.14.4's script functions",
  "&dImprovments:",
  " - &bwaitTick&f no longer uses very small sleep increments, provides better accuracy",
  " - Profiles in bindings menu shows more options before needing to scroll",
  "&aBug Fixes:",
  " - &a__GAME_VERSION&f will now be more consistent reading '&61.12.2&f', '&61.14.4&f', etc",
  " - Written books now give the correct &dtitle&f instead of empty strings.",
  "   &7Previously it would only give the title when you were about to sign the book.",
  " - HTTP requests &breadChar&f() returns &eFALSE&f when there are no more chars",
  "   left in the stream now.",
  " - Fixed crafting table mapping having an inconsistant name for &ecraftingOut&7",
  " - Fixes for &bhttpRequest&fs for &drequestProperties&f, and non-success-codes",
  "&eAdditional Notes:",
  " - &bwaitTick&f() will do nothing on the MC thread.",
  "   This is to prevent the game from locking up",
  " - &cYou should not use sleep on the MC thread.",
  " - There are no thread controls for the MC thread",
  " - &brunOnMC&f will return any values returned by the function provided",
  " - &3Special thanks to anyone who contributed to this version's updates :)",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 7.7.7b",
  "&aBug Fix:",
  " - &bgetSound&f was missing from the globals table since 7.3.0!",
  " - missing comma in this change log broke this change log",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 7.7.6",
  " - &aBug Fix:",
  " - &aBug Fix:",
  "  - Fixed mapping for crafting tables output slot",
  "  - Added missing mapping for &edouble chest",
  
      
    "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 7.7.5",
  " - &aBug Fix:",
  "  - Fixed an issue where the chat filter was causing messages to show up",
  "    in the incorrect order.",
  "    As a side effect of this fix ChatFilters taking longer than 3 seconds are",
  "    not guaranteed to be in order (to make sure other messages still get through)",
  "    The timeout value is configurable with &bgetSettings&f()&b.chatFilterTimeout",
  "    &7(measured in milliseconds)",
    
    "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 7.7.4",
  
  " - &aBug Fix:",
  "   - &ahud3D&f elements were using the same default color",
  "     causing opacity to change for all elements with the default color",
  "   - &ahud2D&f text's opacity now transitions smoothly",
   
    "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 7.7.3",
  " - Colors can now accept functions, they will be called when they are parsed",
  " - Added a setting for enabling access to non public fields and methods with luajava",
  "   &7(getSettings().luajava.allowPrivateAccess)",
  " - Player NBT is now accessable through &bgetPlayer&f()",
   
    "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 7.7.2",
  " - &aBug fix:",
  "  - Forwarded forge event's canceled property is now checked correctly",
   
    "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 7.7.1",
  " - &aBug Fixes:",
  "  - Actual fix for the forge event thing &7(whoops)",
  
   
    "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 7.7.0",
  " - &bFeatures:",
  "  - Added chest label info to gui controls for the GuiOpened and ContainerOpened",
  "    events",
  "  - &ahud2D&f's rectangle now has a &bsetSize&f function",
  " - &aBug Fixes:",
  "  - Fixed certain server messages for actionbar text would get added to the chat",
  "    &7...frequently",
  "  - Chat filters now forward the forge event for ClientChatReceivedEvent",
  "    Any event listeners fired after filtering will receive the filtered text component",
  
   
    "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 7.6.0",
  " - &bFeatures:",
  "  - Added new &ahud3D&f element '&bmesh&f' created by",
  "    &bhud3D.newMesh( <x,y,z> )",
  "    The &adata&f table in the controls is what defines the mesh's shape",
  "    and render attibutes",
  "    Valid modes are: '&astrip&f', '&afan&f', '&aquads&f', '&atriangles&f', and '&aquadStrip&f'",
  "    Valid cull sides are: &efalse&f, '&afront&f', '&aback&f', '&afront and back&f'",
  
    "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 7.5.0",
  " - &aBug Fixes:",
  "   - Fixed unimplemented rotation in &ahud3D&f's &bpane",
  " - &bFeatures:",
  "  - Added rotation for all &ahud2D&f elements",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 7.4.1",
  " - &aBug Fixes:",
  "   - Fixed an issue where &bmidi&f lib could not handle events due to a null pointer",
  "   - Fixed an issue where some classes where unavailable to the &aluajava&f lib",
  "     &7(Related to the class loader that was previously being used)",
  "   - Removed console spam from midi events",
  " - &bFeatures:",
  "   - Midi files are accessed like rest of mod",
  "   - Midi files provide &bgetTicksPerMinute &7does not seem to be accurate though...",
  
  " - &aBug fix:",
  "   - Sound event's pitch and volume previously returned a value of 1 even if it wasn't",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 7.4.1",
  " - &aBug fix:",
  "   - Sound event now provides controls as 5th argument (previously missing)",
   
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 7.4.0",
  " - &bFeatures:",
  "   - New &devents&f:",
  "     - Title",
  "         &7> &e\"event\"",
  "         &7> &e\"Title\"",
  "         &7> &etitle text",
  "         &7> &esubtitle text",
  "         &7> &edisplay time",
  "         &7> &efade in time",
  "         &7> &efade out time",
  "     - Actionbar",
    "         &7> &e\"event\"",
    "         &7> &e\"Actionbar\"",
    "         &7> &etext",
    "         &7> &eis colorized",
  "   - New &bfunctions&f:",
  "     - &bhud2D.title&f( &7String:title, String:subtitle,", 
  "            &7Number:displayTicks, Number:fadeInTicks, Number:fadeOutTicks &f)",
  "       &3&BNote: &3Everything after title is optional",
  "     - &bhud2D.actionbar&f(&7 String:text <, Boolean:colorize> &f)",
  "         &7( Displays text over the hotbar )",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 7.3.0",
  " - &aBug Fixes:",
  "   - Fixed an issue where drawImage was using the incorrect src width and height",
  "   - &eArrowFired&f event now provideds all details",
  "   - http request's readChar no longer returns a number",
  "   - Minecraft text fields now move correctly with groups",
  "   - Minecraft text fields now return the correct x,y coords",
  "   - Script scrollbars now move correctly with groups",
  "   - Script scrollbars now return the correct x,y coords",
  "   - images &bupdate&f() function no longer blocks as it was possible to freeze",
  "     your game in certain cases while using a mutex",
  "   - Clipping fixed in image graphics",
  "   - Fixed a bug for &&F text and tables which can be called",
  "   - Fixed a bug where missing NBT information in some entities causes a crash",
  "   - Fixed missing/incorrect mapping for brewing stand in &bopenInventory()",
  "   - Gui's no longer crash with concurrent modification errors if you add gui elements in",
  "     gui events",
  "   - Removed redundant &bremove&f definition from script elements",
  "   - Script scrollbar's frame now fits in there width and height",
  "   - Removed an 'unimplemented' message from gui",
  " - &bFeatures:",
  "   - &bgetFonts&f and &bmeasureString&f are now available directly from the",
  "     &aimage&f library",
  "   - &ahud3D&f cubes can now rotate!",
  "   - added getSound to replace playSound",
  "      - getSound returns a table of controls, files follow the same rules as the",
  "        rest of the mod &7(unlike playSound)",
  "   - Script gui's Minecraft Text Field can now hide its background so it looks like",
  "     the text field from the chat",
  "   - Individual script elements each have a &bremove&f and &bunremove&f function now",
  " - &dMisc:",
  "   - advLog is now marked depreciated and may be removed in a future version",
  "   - playSound is now marked depreciated and may be removed in a future version",
  "   - Slight optimization when getting the position of another player using",
  "     &bgetPlayerPos",
  "   - http request now auto disconnects if the user forgets to",
  "   - Image graphics function &bdrawPolyline&f renamed to &bdrawPath&f",
  "   - Script scrollbars color properties have been renamed from &a.color.colors&f to ",
  "     &a.properties.colors",
  "   - The games current version can now be found as &e__GAME_VERSION",

  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 7.2.1",
  "&aBug fix:",
  " - Fixed a bug where events that attempted to serialized NBT data of certain",
  "   types of entities caused a crash",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 7.2.0",
  " &BFeatures:",
  " - Gui ScrollBar now has &bgetMaxItems&f and &bgetVisibleItems&f",
  " - &aimage.graphics&f now has rotate, scale, and string measurment functions",
  " - All gui elements now have the &bgetSize&f function",
  " - Added controlls to get/set the scrolling speed for a ScrollBar",
  " - ScrollBars now have more control on if it should receive a scroll event",
  "   in the onScroll event use &b.scroll&f(&7amount&f) to pass the event",
  "   to the scroll bar. &7This was changed to allow multiple scrollbars to be used",
  "   &7in one gui.",
  " - Gui's can now provide the mouse's position in the gui",
  " - Gui can now set if the game should be paused in single player while open",
  " - Can now set if the default background should be drawn",
  " - Can now manualy grab/ungrab the mouse while in a gui",
  " - Gui groups now have getX and getY",
  " - Added &d__class&f attribute to several control tables to make checking",
  "   what the table was generated from",
  " &aFixes",
  " - Removed redundant code from ScrollBar",
  " - Gui events now catch errors from called functions",
  " - Gui releases keystates when opening &7(previous left as being held when it was not)",
  " - Gui events no longer require that the return type *must* be boolean or nil",
  " - The image graphics function for clearRect now clears to a transparent image",
  " - Removed a &cNullPointerException&f from gui elements setting their size",
  " - Parent gui's no longer steal the child gui's events",
  "   &7events are passed in this order:",
  "   &7current gui's elements -> current gui -> parent gui's elements -> parent gui ...",
  " - Setting graphics fonts doesn't tell you that something is unimplemented anymore",
  " - Fixed require",
  " &eOther:",
  " - Updated this gui to use the new scrollbar mechanic",
  " - Changed access to the Minecraft instance to the AdvancedMacros class",
  "   to reduce future number of code updates when updating to 1.13.2",
  
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 7.1.0",
  " - The &adebug&f library has been repaired so that:",
  "   - &bgetinfo&f doesn't give you the stack trace of another script that's",
  "     also running at the same time",
  "   - Debug hooks are now per thread",
  "   - &bgetinfo&f now also allows you to pass in the &athread&f from &bthread.new&f (or",
  "     &brunThread&f) as an optional first arg.",
  "     &7*it is recommended that you pause the script first*",
  " - The &eCHANGELOG&f can now be viewed from the bindings menu",
  " - Fixed an issue where &bthread.new&f would only let you start",
  "   one thread before telling you all your new threads are already done",
  " - Fixed an issue where GuiImage and GuiRectangle were causing some render issues",
  "   with elements drawn after them",
  " - You can now get and set clipboard contents with:",
  "   - &bos.getClipboard&f() and",
  "   - &bos.setClipboard&f(&7 string &f)",
  " - Fixed &bisKeyDown&f() so it should provide correct results even after a gui",
  "   has been opened",
  " - NBT tag data is now available on entities",
  " - Older changelogs have been added in so you can view all of them in one place",
  " - &7Internal:",
  "   - &7Running scripts now uses a ConcurrentHashMap",
  "   - &7Syncronized thread access in LuaDebug class to prevent a ConcurrentModificationException",
  " - &bgetPlayer&f().&atarget&f now tells you what you are looking at, (block or entity)",

  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 7.0.0",
  
  " &cMajor:",
  " - the return value of &bgetBlockList&f() has been changed",
  "   to a string key, each value has a table with a key of the dmg and",
  "   value of item details",
  " &aMinor:",
  " - Depreciating &bdestroy&f() from &ahud&f items",
  "   &7disableDraw will safely remove your hud elements and is ",
  "   &7less likely to cause issues if it is accidently removed via",
  "   &7a clearAll. Destroy's functionality is now that of disableDraw",
  "   &7(removes itself from list of elements to be drawn)",
  " - &bgetBlock&f() now returns &amapColor&f with the block details",
  " - Item stack info now shows NBT tag data",
  " - Added &bmath.map&f() &7used to map a value from one range to another",
  "   &7math.map(x, inMin, inMax, outMin, outMax)",
  " &aFixes:",
  " - Fixed an issue with the drawImage (from image.graphics)",
  "   drawing incorrectly when given a set width and height for the destination",
  
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 6.5.0",
  
  " - Fixed &blog&f not showing quotes around string keys in tables",
  " - Fixed &blog&f showing &crecursive&f tables exactly one extra time",
  " - Added &bgetLabel&f() to threads so you can see how its name would look in ",
  "   the running scripts gui (CTRL + A.M. Keybind)",
  " - Added &bgetUptime&f() to threads so you can see how long it has been running",
  " - Added &bthread.listRunning&f() to list all the scripts that are either",
  "   Paused or Running",
  " - Added &badvancedMacros.openChangeLog&f() so you can open this change log again",
  " - implemented the &d__pairs&f and &d__ipairs&f meta events",
  " - Fine-tuned actions (&bforward&f, &bback&f, etc)",
  " - Gui groups can now return a list of their child elements",
  " - Fixed a missing &c,&f in the change log for version &76.4.0&f that broke the change log",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 6.4.0",
  
  " - added &bgetChunkUpdateCount&f() &7(info from the debug screen)",
    
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 6.3.0",
  
  " - Added &bconnect&f and &bdisconnect&f for quickly quickly switching and leaving",
  "   a server",
  " - Fixed a bug where copying and pasting a file with no extension caused the game",
  "   to freeze",
  " - OpenInventory now has &bgetTotalSlots&f()",
  " - Fixed NullPointerException from gui's &bgetParentGui&f() when none is set",
  "   now returns nil as expected",
  " - Fixed a coloring issue with gui text where the text's color would",
  "   change depending on what was drawn before it (default text color wasn't set)",
  " - Added &bgetFps&f()",
  " - Added a large number of settings controls in &bgetSettings&f()&a.minecraft",
   
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 6.2.0",
  
  " - Added mutexes for thread safety",
  "   &bnewMutex( String:key )",
  "   &b.lock(<timeout>)&f - waits for the mutex to become unlocked, optional timeout",
  "   &b.tryLock()&f - returns false if the mutex is locked, locks otherwise",
  "   &b.unlock()&f - unlocks the mutex",
  "   Additionaly, mutex will automaticly unlock if the thread that locked it dies",
  "   (including if errors stop the script)",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 6.0.5",
  
  " - &eOnCraft&f event doesn't output to the console debug info anymore",
  " - &bgetRecipes&f(name,[dmg]) added",
  " - Inventory actions are now more reliable &7(syncronized with mc thread)",
  " - &bwaitTick&f() has been sycronized for better accuracy",
  " - Internal change: Changed script chunk names",
  "   to use full path name instead of script name.",
  " - Fixed &bgetHeldKeys&f()",
  " - Error logs have been reformated sligtly",
  "   Clicking errors will now scroll the text editor to that line",
  "   &7(not just the cursor anymore)",
  " - Added &bmath.sign()",
  
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 6.0.5",
  
  " - Filtered chat message exist in the sent history now so you can use arrow keys",
  "   to see them",
  " - player yaw and pitch in getPlayer will now match the F3 menu values",
  
  "&b&BChange Log: &7version 6.0.4",
  
  " - typo fix setSide -> setSize in hud3dPane",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 6.0.2, 6.0.3",
  
  " - Whoops, fixed an optimization for hud blocks that broke them",
  "   &bTip: &7Scroll past this version like it never happened....",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 6.0.2",
  
  " - Added details to the sound event, pitch, volume, location",
  " - Added Pane to hud3D",
  "   allows for width and length controls so you can make all",
  "   of your favorite rectangular prisms",
  "   valid sides are xy, xz, yz, xy+, xy-, xz+, xz-, yz+, yz-",
  " - Added details to the AABB (axis aligned bounding box)",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 6.0.1",
  " - Bug fix where client would crash if exiting script gui run",
  "   from the editor's '&6Run&f' button",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 6.0.0",
  " - Files across all functions are now accessed the same way",
  "   Files starting with ~ are in the macros root folder",
  "   Files starting with / start at the hard drive letter given",
  "   Files with no prefix will access files in their own folder",
  "   &7Tip: \\ also works instead of /",
  " - You can now click on your errors to go to them in the editor",
  " - Fixed a bug where regular chat messages were getting",
  "   & formated",
  "   &7Note: when clicking the error, it will open the gui",
  "   &7  **Unsaved changes will not be saved currently**",
  " - &aFile IO&f tweaks:",
  "    - &6*all&f, &6*number &fand &6*line&f all work like &6*a&f, &6*n&f and &6*l&f &7(previously",
  "      &7wasn't available as valid format)",
  "    - Files will be localized to the folder the calling script is in",
  "      &7(Same rules as all file access ~ and / prefixes are also applied)",
  " - Added a function &bresolve&f(&7name&8<, &7level&8>&f) which can be used to",
  "   find the path a script is located in relative to its stacktrace level",
  " - You can now press &aENTER&f on popup prompts to press &bOK",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.8.1",
  " - Fixed some formating issues with player names or other",
  "   &bTranslatableTextComponent&fs",
  " - &b&&N&f was added for text with no click action (hover only)",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.8.0",
  " - Log now allows for & codes &b&&L&f, &b&&T&f, &b&&R&f, &b&&F&f ",
  "    - &b&&L &fcreate a link",
  "    - &b&&T &ftype text into the chat text field (Suggest Command)",
  "    - &b&&F &fFunction - can be a table, function or string",
  "            if string, used as hover text",
  "            if function, used as click event",
  "            if the table has hover or click values they will be used",
  "            table can also use meta for __tostring and __call",
  "    - &b&&R &fRun - makes the player say something on click",
  "   Provide each arg for the new & codes after your formated string",
  "   &a&BExamples:",
  "      &blog&f('Hello &b&&b&U&&U&&Lworld&f&&f!', {click='&6http://www.google.com&f'})",  
  "      &blog&f('Hello &b&&b&U&&U&&Fworld&f&&f!', {click=someFunction, hover = '&6Click me!&f'})",  
  " - Tired of chatFilter making links unclickable in chat? Well no more!",
  "   ChatFilter events now pass a table containing all of the actions used in the chat msg",
  "   Args for the event are now: formatedText, unformatedText, actions",
  "   &7&BTip: &7for the last chat filter return your formated text and unpack the actions table",
  " - fixed &&&& in gui's",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.7.0",
  " - &bopenInventory() &fis now functional",
  "   &7Tip: Add a delay when pulling items from the crafing output",
  " - &asomeTable&f[123] will now highlight the table correctly when",
  "   no space is between the table and []",
  " - &bGUIOpened &fand &bContainerOpen &fevents now pass the args",
  "   &aguiControls&f, &2guiName",
  "   Multipe Minecraft GUI names have been simplified",
  "   Gui controls are available for:",
  "    - Inventories / containers",
  "    - Enchantment tables",
  "    - Anvils",
  "    - Signs",
  "    - Villager trades",
  "    - Books",
  "    - Command blocks",
  " - Slot mappings have been added to the &bopenInventory&f()",
  "   controls. They include:",
  "    - The player's inventory",
  "    - Beacons",
  "    - Brewing stands",
  "    - Chests",
  "    - Shulker boxes",
  "    - Crafting tables",
  "    - Dispensers / Droppers",
  "    - Furnaces",
  "    - Hoppers",
  "    - Anvils",
  "    - Enchantment tables",
  "    - Villager trades",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.6.0",
  " - Fixed &blook&f. Smooth, no unneeded spinning",
  " - Caught error from &bFileSystem.open &f when preparing a stacktrace",
  "   for unclosed files",
  " - Added &brayTrace&f(&7<yaw, pitch>, <from xyz>, <dist>, <includeLiquid>&f)",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.5.5",
  " - Fixed &brun &fnot working with args",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.5.4",
  " - Scripts missing names will show ? in the running script gui",
  " - hud3D block wont cause an error when an unknown texture name is given",
  " - getBoundingBox now returns a second value for blocks indicating",
  "   if the block is solid (collidable)",
  " - Patch for LuaJ's DebugLib where it fail's to get a function name",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.5.3",
  " - Fixed a bug where default color settings wouldn't be saved during init",
  "   You should be able to see the profile text in fresh installs now.",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.5.2",
  " - Fixed Settings serialization bug with re-used/recursive tables",
  "   that caused the file to become un-usable",
   
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.5.1",
  " - Fixed null pointer from &bgetBoundingBox()",
  " - Fixed &bgetBlockList()",
  " - Added some Minecraft settings under &bgetSettings().minecraft",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.5.0",
  " - Fixed a startup error that crashed for fresh installs. Welcome!",
  " - ChatFilter now works like ChatSendFilter so they will be chained.",
  "   if the last ChatFilter returns nothing it will cancel the message",
  " - &bgetBoundingBox(x, y, z)&f or &b(entityID)&f returns a bounding box.",
  "   &7Used by minecraft to check for collisions.",
    
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.4.3",
  " - Fixed &bfilesystem &fbug with close warning",
  "   Also, if the line number cant be found (-1)",
  "   it should show &6'?' &finstead now",
  " - Fixed a bug where you sneak and stop glowing",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.4.2",
  " - &b&Sfilesystem &f&Sclose warning shows when a file isn't closed",
  "   It also includes a stack trace. This triggers when garbage collection",
  "   occurs in java",
  "   &7Edit: this version had a bug where it always showed the warning",
  " - &bgetPlayer() &fnow includes the entityID property",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.4.1",
  " - Fixed a potential crash from the ingame editor",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.4.0",
  " - New event! &bChatSendFilter",
  "   if the script called returns &enil &for &efalse&f then the message",
  "   will not send. If a string is returned that will be sent instead.",
  "   In the event of multiple bindings, the output of the first will be",
  "   passed on to the next",
  " - &cErrors &f will now show a full stack trace if they originate from",
  "   &eAdvancedMacros",
  " - Added &3documentation &f to &bhud3D &fand functions from its elements",
  " - Hud3D's block now includes a tint color &7(can set per side)",
  " - &afilesystem &fnow allows for relitive and external paths",
  "   &7(you can access outside the AdvancedMacros folder)",
  "   Paths starting with &6'/' &for &6'\\' &fwill use a full address (from drive letter)",
  "   Paths can use &6'../' &fto navigate back a folder",
  " - Logging functions with built in documentation should show correctly now",
  "   Logging just the function will provide full detail",
  "   Only the 'definition' will show if it's in a table",
  " - Fixed hud2D's miss-named document files",
  " - Added function &bhighlightEntity &fAllows you to make an entity render",
  "   like the &exray &fmode for hud3D or with the &eglow &feffect",
  " - &blistTextures() &f has been fixed",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.3.0",
  " - Color space is now 0 <-> 1 (not 255)",
  " - A script included in the startup should have updated your color settings",
  "   to the 0 <-> 1 format",
  " - When colors are returned from functions they will now return in the",
  "   format {r, g, b, a} &7(instead of {r=r, g=g, b=b, a=a})",
  " - Added &bgetBlockList() &freturns a list of minecraft block ID's",
  " - Scripts fired from the editor now has the correct argument",
  " - &bgetSettings().chatMaxLines&f will now change the maximum number of",
  "   lines in your chat. Useful for large logs like &bgetPlayer()",
  " - Attempted to implement more events, currently",
  "   many only trigger inside the server.... working on it",
  " - The XP event now detects changes to levels",
  " - Arrow fired has more detail",
  " - &bgetSettings().events.potionStatusFrequency &fwill control how often",
  "   the event is called, (default 20, fastest = 1)",
  " - &bgetSettings().events.useItemFrequency &fwill control how often",
  "   the UseItem event will trigger.",
  "   It will always trigger on start and finish",
  " - Fixed a bug with checking held keys and thread syncronization",
  " - Removed a debug output from the script browser that may have spamed",
  "   the console",
  " - &bhud3D.newBlock().changeTexture &fnow allows for a second argument",
  "   this will set the texture for one side.",
  "   Valid sides are:",
  "     - \"up\" or \"top\"",
  "     - \"down\" or \"bottom\"",
  "     - \"north\"",
  "     - \"west\"",
  "     - \"east\"",
  "     - \"south\"",
  " - Tooltips will now show from tables with the meta table containing:",
  "   - luaFunction = true",
  "   - definition=\"someFunction(a, b, c)\"",
  "   - tooltip={\"line 1\", \"line 2\"....} or \"single line\"",
  "   - luaDoc={\"line 1\", \"line 2\"....} or \"single line\"",
  "     this will be used for an in game help page later",
  "   - types={{'string', 'number', 'opt_number'}, {'string', 'string'}}",
  "     if someFunction('blah', 4 [,10]) and someFunction('a', 'b')",
  "     both work, this will be used for autocomplete in the future",
  "   - It should also have a value at __call",
  " - &bgetPlayer() &f now includes a value &aentityRiding &f",
  "   containing entity info about whatever the player is on",
  "   this chagne also effects &bgetEntityData()",
  " - Tooltips added for a bunch of common &bgui &ffunctions",
  " - A placeholder for container controls has been made",
  "   this will become available on inventories from the ContainerOpen",
  "   event",
  " - image.graphics.drawImage now allows additional arguments for",
  "   source location and scaling",
  "   drawImage(img, x, y, wid, hei, srcX, srcY, srcWid, srcHei)",
  " - now built on a newer version of forge",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.2.0",
  " - Due to a typo &bhud3D.block &fwoudln't change its texture,",
  "   this is fixed now.",
  " - if you have a hud block and do &bsetTexture&f, you can now include",
  "   a second argument containing:",
  "   &eup&f/&etop&f/&edown&f/&ebottom&f/&enorth&f/&ewest&f/&eeast&f/&esouth",
  "   it will set the texture for",
  "   only that side",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.1.0",
  " - Textures are now simpler to use.", 
  "   the &bimg.texture.create() &fand &bimg.texture.remove()",
  "   in images have been removed, and &bimg.texture.update()",
  "   is now &bimg.update()",
  "",
  " - If the player hasn't loaded yet, either in a menu or while",
  "   joining the world it will", 
  "   return &enil &finstead of causing a &cNullPointerException&f now",
  "",
  " - Added &bthread.new() &fand &bthread.current()",
  "   &bthread.new() &fwill allow you to create a thread without starting it",
  "   &bthread.current() &fwill return controls for the current thread",
  "   thread controls now contains &bgetID() &ffor comparison",
  
  -------------------------------------------------------------------
  ----retro active change log
   
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.0.0",
  " - Mod now shows logo in the mods menu",
  " - &a_MOD_VERSION&f now shows the current version of Advanced Macros",
  " - ScrollBar has been added to the Gui library",
  " - MinecraftTextField has been added to the Gui library",
  " - Fixed prompt choice causing errors on selection",
  " - &chus2D, hud3D and gui now use .new____ instead of .add_____",
  " - gui's now allow for an onResizeEvent",
  " - gui boxes with larger thickness now render their bottom right corrners correctly",
  " - gui images now show hovertint correctly",
  " - hud2D images, hud3D blocks, and gui images now allow for images from the image",
  "   library to be used as textures",
  " - all graphics functions for images should be accessable now",
  " - all this, in the ingame change log (reading it right now!)",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 4.1.0", 
  " - &ahud2D&f text now uses the minecraft font",
  " - &7(hud2D's text measurement maybe slightly off still, this is being fixed)",
  " - Added &bimage.getFormats&f()",
  "   lists supported image types for reading and writing",
  " - fixed an issue with plugin loading and global resource location stuff",
  " - Added image graphics tools for drawing",
  " - Fixed a bug where &bstring.format&f(\"%.3f\", number) would provide the incorrect output",
  " - Setup for further documentation of functions that are generated at runtime",
  "   &7 like setPixel from image.new()",
  " - Future documentation will include name, definition, tooltip, and accepted param types",
  "   as a part of the functions data (as a table that can be called)",
  " - Added some setup for coremod features to change the maximum number of lines the chat may",
  "   hold &7(current limit is 100)",
  "   The setting can be seen at &bgetSettings().chatMaxLines",
  " - The text editor now recognizes functions with &d:&f in them instead of just &d.&f",
  "   so class functions will show their tooltips",
  " - The text editors vertical scrollbar is now clickable again",
  " - Fixed an error where getPos and getRot in hud3D items would get stuck in recursion",
  " - log now shows metatables in purple",
  " - when logging an empty table it will put {} on one line instead of different lines",
  " - errors thrown by threads now provide details in the chat",
  " - image.load should now show a tooltip",
  " - fixed a bug where hud2D rectangles alpha/opacity didn't function as expected",
  " - comment based documentation for tooltips now includes up to 10 lines instead of 5",
  " - getPlayer().health and .hunger now provide more useful values (applies math.ceil op)",
  " - new icons created by &dEmex&f for the bindings menu",
  " - &7Offical discord server created",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 4.0.0", 
  " - Color themes / settings have simplified format",
  " - The widget nonsense has been banished",
  " - Added &bgetHeldKeys&f()",
  " - Create and load images with:",
  "   - &bimage.new&f(&7 width, height &f)",
  "   - &bimage.load&f( &7file &f)",
  " - Gui Text Areas now have color theme settings",
  "   &7full text color control in a future update",
  " - &aBug fixes:",
  "   - Syncronization issue with script gui's you can now add elements with",
  "     the gui open",
  "   - Removed debug output from the prompt gui",
  " - &aNote&f:",
  "   - &7 - The simplified color settings will allow for more gui elements to be easily included with color settings in future updates",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.14.3", 
  " - The bug where you open a script gui, and you cant see your mouse",
  "   (unless you move your mouse outside the window first) should now be fixed",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.14.2", 
  " - Fixed Text area crashes when typing and created with no default text or \"\"",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.14.0", 
  " - Root folder is now configurable",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.13.1", 
  " - Bug fix: Vertical scrollbars correctly check their lower bounds",
  " - Bug fix: Text areas now auto focus when clicked on",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.13.0", 
  " - Added &bgetScreen()",
  "   returns a table with some image controls, the image is a screenshot taken",
  "   at the time getScreen() was called you can view/edit data and quickly save",
  "   to file. (looks like it takes a little less than 15-20ms on average from testing)",
  " - &bgui.addTextArea&f( &7<x, y, width, height, default text> &f)",
  "   Adds the same type of text editor found in the script editor",
  "   syntax highlighting can be disabled, keywords can be replaced",
  "   Currently lacks proper controls for individual color themes",
  "",
  "  &aNote:&7 The delay in getScreen() is mainly from the synchronizing with the OpenGL",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.12.0", 
  " - &aFixed:",
  "   - Previously unable to change profile bindings for loaded profile via getSettings()",
  "   - Reduced amount of saving done by bindings gui",
  " - &bFeatures:",
  "   - Special jars put in the macros/libs folder will be loaded as lua libraries. Resources for creating custom libraries will be made available.",
  "   - getJarLibLoaders() will return a table of loaded jar libs names paired with their loader functions",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.11.6", 
  " - New Features:",
  "   - &bhus2D.addItem&f(&7 item, x, y &f) &7all args are optional",
  "   - &bg.addItem&f( &7item, x, y &f) &7where g is a gui instance, args optional",
  "   - &bmidi.getKeyboard&f( &7filter, onEvent &f) &7--allows connection to midi keyboards",
  "   - &bmidi.stopAll() &7--stops/closes all midi connections",
  " - Changed:",
  "   - prompt will wait for you to exit the running scripts gui so you can stop a script that keeps",
  "     prompting without restarting",
  "   - Gui elements have a default hover color of CLEAR now, shouldn't cause",
  "     &cNullPointerException&f crashes anymore",
  " - Notes: ",
  "   - Item format for addItem allows:",
  "     - 'minecraft:wool' &7--no damage value",
  "     - 'minecraft:wool:wool' &7--damage value of 5",
  "     - 'wool' &7--puts \"minecraft:\" infront *only if there is no ':' anywhere in the string for now",
  "     - Currently item counts will not render on the item",
  "     - Also, Items with durability do not render the usage/damage bar.",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.11.5", 
  " - Sound events now also give a control table with",
  "   &bisPlaying&f() and &bstop&f()",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.11.4", 
  " - Issue #17 Request for on sound/music event",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.11.3", 
  " - Implemented events:",
  "   - GuiOpen",
  "   - GuiClose",
  "   - ProfileChange",
  "   - Startup &7- fixed",
  " - Fixed transparency in hud2D/gui(?)",
  " - gui image has default image now",
  " - guis can be named (for gui events)",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.11.2", 
  " - Fixed an issue with Hud2D items and transparency",
  " - Rectangles now have transparency working",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.11.1", 
  " - Hud text no longer has a default value of &cnull",
  " - Hud text now has a default text size of 12",
  " - Text can now be measured with &b.geWidth&f() on 2D text elements",
  " - &aHud2D&f's items now have a function &b.isDrawing&f()",
  " - &aHud2D&f's add____ now includes optional arguments",
  "   - &baddRectangle&f(&7 x, y, width, height &f)",
  "   - &baddBox&f(&7 x, y, width, height, thickness &f)",
  "   - &baddImage&f(&7 texture, x, y, width, height &f)",
  "   - &baddText&f(&7 text, x, y, size &f)",
  "   - Values &ddefault&f to:",
  "     - &9x &f= &d0",
  "     - &9y &f= &d0",
  "     - &9width &f= &d0",
  "     - &9height &f= &d0",
  "     - &9thickness &f= &d0",
  "     - &9size &f= &d12",
  "     - &9text &f= &d\"\"",
  "     - &9texture &f= &dnil",
  " - New: Cusotm Gui! ( &bgui.new&f() )",
  "  - &baddRectangle&f()",
  "  - &baddBox&f()",
  "  - &baddGroup&f()",
  "  - &baddText&f()",
  "  - &baddImage&f()",
  "  - &open&f()",
  "  - &bclose&f()",
  "  - &bsetParentGui&f()",
  "  - &bgetSize&f()",
  "  - Groups:",
  "    - Manage multiple elements at the same time",
  "    - &bsetVisible&f( &7boolean &f)", 
  "    - &bisVisible&f()", 
  "    - &bmove&f(&7 x, y &f) --relative movement", 
  "    - &bsetPos&f( &7x, y &f)", 
  "    - &bgetPos&f()",
  "    - &bsetParent( &7parent &f)",
  "    - &baddSubGroup&f()",
  "  - Any event is consumed if you return true in the handler function (gui not bindings)",
  "  - Gui Events: &7( the letter F here is a function that accepts some arguments )",
  "    - &bsetOnMouseEnter&f()",
  "    - &bsetOnMouseExit&f()",
  "    - &bsetOnScroll&f( &7F(amount) &f)",
  "    - &bsetOnMouseClick&f( &7F(x, y, buttonNum) &f) &7--LMB=0, RMB=1, MMB = 2",
  "    - &bsetOnMouseRelease&f( &7F(x, y, buttonNum) &f)",
  "    - &bsetOnMouseDrag&f( &7F(x, y, buttonNum, time) &f)",
  "    - &bsetOnKeyPressed&f( &7F(typedChar, keyCode) &f)",
  "    - &bsetOnKeyReleased&f( &7F(typedChar, keyCode) &f)",
  "    - &bsetOnKeyRepeated&f( &7F(typedChar, keycode, mod) &f) &7mod can be used with % to reduce usage",
  "  - Other Gui Element Functions:",
  "    - &bisHover&f()",
  "    - &bremove&f()",
  "    - &bsetVisible&f( &7boolean&f )",
  "    - &bisVisible&f()",
  "    - &bsetX&f( &7x &f)",
  "    - &bsetY&f( &7y &f)",
  "    - &bgetX&f()",
  "    - &bgetY()",
  "    - &bsetPos(&7 x, y &f)",
  "    - &bgetPos&f()",
  "    - &bsetZ&f( &7height &f)",
  "    - &bgetZ&f()",
  "    - &bgetPos&f()",
  "    - &bsetOpacity&f( &7opacity &f)",
  "    - &bgetOpacity&f()",
  "    - &bgetWidth&f()",
  "    - &bsetHoverTint&f( &7color &f)",
  "    - &bgetHoverTint&f()",
  "    - &bsetParent&f( &7parent &f)",
  "    - Some elements may include &bsetColor&f( &7color &f) and &bgetColor&f()",
  " - &aBug Fixes:",
  "    - &aHud2D&f's items can not be added to the drawing list multiple times anymore",
  "    - All errors now use the same color code for logging",
  "    - &aHud2D&f's image doesn't have a null texture by default anymore",
  "    - 2D text with \\n doesn't go up anymore",
  " - &eSyntax Changes:",
  "    - text &bsetSize &7-> &bsetTextSize",
  "    - &bgetSize &7-> &bgetTextSize",
  "    - &bmeasureWidth &7-> &bgetWidth",
  "    - &bmeasureHeight &7-> &bgetHeight",
  "    - &bmeasure &7-> &bgetSize",
  
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.10.3", 
  " - Added Z property to 2D hud items, can be used for controling what elements are on top/behind each other ",
  "   &7(+ is top - is bottom, any number is fine)",
  " - Added String measurement tools for text",
  " - Fixed some bugs:",
  "   - opacity with 2D rectangles",
  "   - Text size not being used",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.10.2", 
  " - Removed mining fatigue from mod*",
  "",
  "   &7* The mod is as fast as it was before 3.10.0",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.10.1", 
  " - Profile delete operations are saved.",
  " - Deleted bindings (and other settings) are saved before switching profiles",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.10.0", 
  " - &aFixed:",
  "   - Bug where setting a color for something with the 0x######## format would always set it to black.",
  "   - Bug where changing the column count for the script browser would result in a crash.",
  "   - Changing a texture on a HUD item after it's created to an unloaded/new texture now works.",
  " - &bAdded:",
  "   - &bhttpRequest&f(&7Table: args&f)",
  "   - &bhud2D.clearAll&f()",
  "   - &bhud2D.getSize&f()",
  "   - &bhud2D.addRectangle&f()",
  "   - &bhud2D.addBox&f()",
  "   - &bhud2D.addText&f()",
  "   - &bhud2D.addImage&f()",
  "   - &bhud2D.addBlock&f()",
  "   - &bhud2D.addText&f()",
  "   - &bhud2D.clearAll&f()",
  "   - Tooltips for all new functions",
  " - Additional details available from curse forge...",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.9.3", 
  " - Added advancement toast messages.",
  "   &7(bread not included)",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.9.2",
  " - Fixed errors from scripts not showing in chat",
  "  &7(seems the \ from the folder was messing with json formating)", 
  
   
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.9.1", 
  " - This update may fix issues where color settings with overlapping names caused issues",
  " - Some property names have been changed",
   
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.9.0", 
  " - Tab size property for text editor",
  " - Dont need to press CTRL for deleting scripts",
  " - Added text color controls GuiButton",
  " - Added Choice Prompt",
  " - getPlayer has mainHand, offHand and invSlot now",
  " - Script browser and selection now uses a folder system.",
  " - added new bugs",
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.8.2",
  " - Fixed a compatibility issue with computerCraft.",
  "   As a side effect you no longer need to download the luaj-jse jar as it is included in the mod files.", 
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.8.1",
  " - Fixed a compatibility issue with InventoryTweaks.", 
  "   &7ComputerCraft still has compatibility issues",
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.8.0",
  " - &cUpdated for use on Minecraft 1.12.2",
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.7.1",
  " - Removed a line of code from testing that spams your console", 
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.7.0",
  " - Now easier to set textures for holoblocks using vanilla textures.",
  " - New function listTextures will return a list of textures register to the TextureMap.",
  ' - Using .changeTexture on a holoblock with "block:" and one of the items from the ',
  "   listTextures table will set the UV to that blocks spot on the blocks texture.", 
  '   &7ex: hb.changeTexture("block:minecraft:blocks/redstone_block")',
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.6.0",
  " - New function runThread(function/file, args...)", 
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.5.2", 
  " - Fixed a bug that showed up all over the place (related to converting NBT data to tables) [Caused game crash]",
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.5.1",
  " - Fixed a bug that made it impossible to join a multiplayer world.", 
  " - implemented PlayerJoin event",
  " - Implemented PlayerLeave event",
  " - getBlock now gives NBT data as available.",
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.4.0", 
  " - getEntityData now yields false if there is not entity with a matching id",
  " - getEntityList no longer has duplicates",
  " - playSound has volume control now closes",
  " - call has been changed to run",
  " - pcall has been changed to pRun",
  "   &7native pcall should now be accessible",
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.3.2",
  " - New event type ChatFilter works as Chat used to. The Chat event will not block messages, ChatFilter will.",
  "- Fixed: Unable to get player velocity when riding something like a minecart", 
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.3.1",
  " - Added velocity to getEntity", 
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.3.0",
  ' - Edited Tool tip for look and look at to include "Number:" for time',
  ' - Function "stopAllScripts" added to kill all the scripts instantly (tooltip included)',
  ' - Actions like forward, back, left, use.. etc when passing a time of 0 will now instantly stop the action',
  
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.2.5",
  " - Fixed a bug related to getPlayerList",
  "   &7(Removed &f from start of every name from getPlayerList())",
  
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.2.4",
  " - getPlayerList now returns names with their color codes in & format",
  "   &7Tip: string.gsub can be used to filter them out",
  
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.2.3",
  "Fixed getPlayerList() returning an empty table",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.2.2",
  " - Fixed:",
  "   - New profiles could not be created",
  '   - Script list would not update (now updates when "Scripts" is clicked from the main menu)',
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.2.1",
  
  "Initial release. Welcome!"
}

---------------------------------------------------------------------------------

local CHECK = "resource:greencheck.png"
local X     = "resource:redx.png"
local g = gui.new()

local s = g.newScrollBar()
s.setOrientation("vertical")
s.setY(5)
s.setWidth(7)
s.setMaxItems( #changeLog )

local group = g.newGroup()

local tmp = g.newRectangle(94, 10, 13, 13)
tmp.setColor( 0xFF000000 )
tmp.setParent( group )

local button = g.newImage(cl.showOnStartup and CHECK or X  , 94, 10, 12, 12)
button.setHoverTint(0x55555555)
button.setParent( group )
button.setOnMouseClick(function (...)
  local bool = not cl.showOnStartup
  cl.showOnStartup = bool
  button.setImage( bool and CHECK or X )
end)

tmp = g.newBox(93, 9, 14, 14, 1)
tmp.setColor( 0xFFAAAAAA )
tmp.setParent( group )
--scrollbar setup

s.setOnMouseDrag( function(...)
  local pos = s.getScrollPos()
  group.setPos(0, pos*-12)
end )
s.setOnScroll( function (amount)
  s.scroll(amount )
  local pos = s.getScrollPos()
  group.setPos(0, pos*-12)
end )

--generate text elements
for i, text in pairs(changeLog) do
  local t = g.newText(text, 5, i*12)
  t.setParent(group)
end

function resize(w, h)
  s.setHeight(h - 10)
  s.setX( w-5-s.getWidth() )
  s.setVisibleItems( math.min(math.floor((h-10)/12), #changeLog)  )
end
g.setOnClose( function(...)
  cl.shownVersions[_MOD_VERSION] = true
  getSettings().save() 
end)
g.setOnResize( resize )
resize( g.getSize() )
if(getPlayer() == nil)then
  while( getPlayer() == nil)do 
   sleep(1000) 
  end
  sleep(2000)
end
g.open()
