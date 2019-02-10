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
  
  "&b&BChange Log: &7version 3.2.5", 
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.2.5", 
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 3.2.5", 
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
function onScroll(...)
  local pos = s.getScrollPos()
  group.setPos(0, pos*-12)
end
s.setOnMouseDrag( onScroll )
s.setOnScroll( onScroll )

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
