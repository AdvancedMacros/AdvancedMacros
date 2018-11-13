--check if the gui should show and set the default value if missing
local settings = getSettings()
settings.changeLog = settings.changeLog or {}
local cl = settings.changeLog
cl.showOnStartup = cl.showOnStartup==nil or cl.showOnStartup
if not cl.showOnStartup then return end
cl.shownVersions = cl.shownVersions or {}
if cl.shownVersions[_MOD_VERSION] then return end

local DIVIDER =
"--------------------------------------------------------------------------------"
-- CHANGE LOG -------------------------------------------------------------------
local changeLog = {
  "&eShow on startup? ", --do not remove
  "&f&BOffical discord: &7https://discord.gg/ga9Npym",
  "&fA clickable link can be found in the Mods menu in this mod's description",
  "&7"..DIVIDER, --keep
  "&b&BChange Log: &7version ".._MOD_VERSION, --do not remove
  
  "&f - &eOnCraft&f event doesn't output to the console debug info anymore",
  "&f - &bgetRecipes&f(name,[dmg]) added",
  "&f - Inventory actions are now more reliable &7(syncronized with mc thread)",
  "&f - &bwaitTick&f() has been sycronized for better accuracy",
  "&f - Internal change: Changed script chunk names",
  "&f   to use full path name instead of script name.",
  "&f - Fixed &bgetHeldKeys&f()",
  
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 6.0.5",
  
  "&f - Filtered chat message exist in the sent history now so you can use arrow keys",
  "&f   to see them",
  "&f - player yaw and pitch in getPlayer will now match the F3 menu values",
  
  "&b&BChange Log: &7version 6.0.4",
  
  "&f - typo fix setSide -> setSize in hud3dPane",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 6.0.2, 6.0.3",
  
  "&f - Whoops, fixed an optimization for hud blocks that broke them",
  "&f   &bTip: &7Scroll past this version like it never happened....",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 6.0.2",
  
  "&f - Added details to the sound event, pitch, volume, location",
  "&f - Added Pane to hud3D",
  "&f   allows for width and length controls so you can make all",
  "&f   of your favorite rectangular prisms",
  "&f   valid sides are xy, xz, yz, xy+, xy-, xz+, xz-, yz+, yz-",
  "&f - Added details to the AABB (axis aligned bounding box)",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 6.0.1",
  "&f - Bug fix where client would crash if exiting script gui run",
  "&f   from the editor's '&6Run&f' button",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 6.0.0",
  "&f - Files across all functions are now accessed the same way",
  "&f   Files starting with ~ are in the macros root folder",
  "&f   Files starting with / start at the hard drive letter given",
  "&f   Files with no prefix will access files in their own folder",
  "&f   &7Tip: \\ also works instead of /",
  "&f - You can now click on your errors to go to them in the editor",
  "&f - Fixed a bug where regular chat messages were getting",
  "&f   & formated",
  "&f   &7Note: when clicking the error, it will open the gui",
  "&f   &7  **Unsaved changes will not be saved currently**",
  "&f - &aFile IO&f tweaks:",
  "&f    - &6*all&f, &6*number &fand &6*line&f all work like &6*a&f, &6*n&f and &6*l&f &7(previously",
  "&f      &7wasn't available as valid format)",
  "&f    - Files will be localized to the folder the calling script is in",
  "&f      &7(Same rules as all file access ~ and / prefixes are also applied)",
  "&f - Added a function &bresolve&f(&7name&8<, &7level&8>&f) which can be used to",
  "&f   find the path a script is located in relative to its stacktrace level",
  "&f - You can now press &aENTER&f on popup prompts to press &bOK",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.8.1",
  "&f - Fixed some formating issues with player names or other",
  "&f   &bTranslatableTextComponent&fs",
  "&f - &b&&N&f was added for text with no click action (hover only)",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.8.0",
  "&f - Log now allows for & codes &b&&L&f, &b&&T&f, &b&&R&f, &b&&F&f ",
  "&f    - &b&&L &fcreate a link",
  "&f    - &b&&T &ftype text into the chat text field (Suggest Command)",
  "&f    - &b&&F &fFunction - can be a table, function or string",
  "&f            if string, used as hover text",
  "&f            if function, used as click event",
  "&f            if the table has hover or click values they will be used",
  "&f            table can also use meta for __tostring and __call",
  "&f    - &b&&R &fRun - makes the player say something on click",
  "&f   Provide each arg for the new & codes after your formated string",
  "&f   &a&BExamples:",
  "&f      &blog&f('Hello &b&&b&U&&U&&Lworld&f&&f!', {click='&6http://www.google.com&f'})",  
  "&f      &blog&f('Hello &b&&b&U&&U&&Fworld&f&&f!', {click=someFunction, hover = '&6Click me!&f'})",  
  "&f - Tired of chatFilter making links unclickable in chat? Well no more!",
  "&f   ChatFilter events now pass a table containing all of the actions used in the chat msg",
  "&f   Args for the event are now: formatedText, unformatedText, actions",
  "&f   &7&BTip: &7for the last chat filter return your formated text and unpack the actions table",
  "&f - fixed &&&& in gui's",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.7.0",
  "&f - &bopenInventory() &fis now functional",
  "&f   &7Tip: Add a delay when pulling items from the crafing output",
  "&f - &asomeTable&f[123] will now highlight the table correctly when",
  "&f   no space is between the table and []",
  "&f - &bGUIOpened &fand &bContainerOpen &fevents now pass the args",
  "&f   &aguiControls&f, &2guiName",
  "&f   Multipe Minecraft GUI names have been simplified",
  "&f   Gui controls are available for:",
  "&f    - Inventories / containers",
  "&f    - Enchantment tables",
  "&f    - Anvils",
  "&f    - Signs",
  "&f    - Villager trades",
  "&f    - Books",
  "&f    - Command blocks",
  "&f - Slot mappings have been added to the &bopenInventory&f()",
  "&f   controls. They include:",
  "&f    - The player's inventory",
  "&f    - Beacons",
  "&f    - Brewing stands",
  "&f    - Chests",
  "&f    - Shulker boxes",
  "&f    - Crafting tables",
  "&f    - Dispensers / Droppers",
  "&f    - Furnaces",
  "&f    - Hoppers",
  "&f    - Anvils",
  "&f    - Enchantment tables",
  "&f    - Villager trades",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.6.0",
  "&f - Fixed &blook&f. Smooth, no unneeded spinning",
  "&f - Caught error from &bFileSystem.open &f when preparing a stacktrace",
  "&f   for unclosed files",
  "&f - Added &brayTrace&f(&7<yaw, pitch>, <from xyz>, <dist>, <includeLiquid>&f)",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.5.5",
  "&f - Fixed &brun &fnot working with args",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.5.4",
  "&f - Scripts missing names will show ? in the running script gui",
  "&f - hud3D block wont cause an error when an unknown texture name is given",
  "&f - getBoundingBox now returns a second value for blocks indicating",
  "&f   if the block is solid (collidable)",
  "&f - Patch for LuaJ's DebugLib where it fail's to get a function name",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.5.3",
  "&f - Fixed a bug where default color settings wouldn't be saved during init",
  "&f   You should be able to see the profile text in fresh installs now.",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.5.2",
  "&f - Fixed Settings serialization bug with re-used/recursive tables",
  "&f   that caused the file to become un-usable",
   
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.5.1",
  "&f - Fixed null pointer from &bgetBoundingBox()",
  "&f - Fixed &bgetBlockList()",
  "&f - Added some Minecraft settings under &bgetSettings().minecraft",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.5.0",
  "&f - Fixed a startup error that crashed for fresh installs. Welcome!",
  "&f - ChatFilter now works like ChatSendFilter so they will be chained.",
  "&f   if the last ChatFilter returns nothing it will cancel the message",
  "&f - &bgetBoundingBox(x, y, z)&f or &b(entityID)&f returns a bounding box.",
  "&f   &7Used by minecraft to check for collisions.",
    
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.4.3",
  "&f - Fixed &bfilesystem &fbug with close warning",
  "&f   Also, if the line number cant be found (-1)",
  "&f   it should show &6'?' &finstead now",
  "&f - Fixed a bug where you sneak and stop glowing",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.4.2",
  "&f - &b&Sfilesystem &f&Sclose warning shows when a file isn't closed",
  "&f   It also includes a stack trace. This triggers when garbage collection",
  "&f   occurs in java",
  "&f   &7Edit: this version had a bug where it always showed the warning",
  "&f - &bgetPlayer() &fnow includes the entityID property",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.4.1",
  "&f - Fixed a potential crash from the ingame editor",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.4.0",
  "&f - New event! &bChatSendFilter",
  "&f   if the script called returns &enil &for &efalse&f then the message",
  "&f   will not send. If a string is returned that will be sent instead.",
  "&f   In the event of multiple bindings, the output of the first will be",
  "&f   passed on to the next",
  "&f - &cErrors &f will now show a full stack trace if they originate from",
  "&f   &eAdvancedMacros",
  "&f - Added &3documentation &f to &bhud3D &fand functions from its elements",
  "&f - Hud3D's block now includes a tint color &7(can set per side)",
  "&f - &afilesystem &fnow allows for relitive and external paths",
  "&f   &7(you can access outside the AdvancedMacros folder)",
  "&f   Paths starting with &6'/' &for &6'\\' &fwill use a full address (from drive letter)",
  "&f   Paths can use &6'../' &fto navigate back a folder",
  "&f - Logging functions with built in documentation should show correctly now",
  "&f   Logging just the function will provide full detail",
  "&f   Only the 'definition' will show if it's in a table",
  "&f - Fixed hud2D's miss-named document files",
  "&f - Added function &bhighlightEntity &fAllows you to make an entity render",
  "&f   like the &exray &fmode for hud3D or with the &eglow &feffect",
  "&f - &blistTextures() &f has been fixed",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.3.0",
  "&f - Color space is now 0 <-> 1 (not 255)",
  "&f - A script included in the startup should have updated your color settings",
  "&f   to the 0 <-> 1 format",
  "&f - When colors are returned from functions they will now return in the",
  "&f   format {r, g, b, a} &7(instead of {r=r, g=g, b=b, a=a})",
  "&f - Added &bgetBlockList() &freturns a list of minecraft block ID's",
  "&f - Scripts fired from the editor now has the correct argument",
  "&f - &bgetSettings().chatMaxLines&f will now change the maximum number of",
  "&f   lines in your chat. Useful for large logs like &bgetPlayer()",
  "&f - Attempted to implement more events, currently",
  "&f   many only trigger inside the server.... working on it",
  "&f - The XP event now detects changes to levels",
  "&f - Arrow fired has more detail",
  "&f - &bgetSettings().events.potionStatusFrequency &fwill control how often",
  "&f   the event is called, (default 20, fastest = 1)",
  "&f - &bgetSettings().events.useItemFrequency &fwill control how often",
  "&f   the UseItem event will trigger.",
  "&f   It will always trigger on start and finish",
  "&f - Fixed a bug with checking held keys and thread syncronization",
  "&f - Removed a debug output from the script browser that may have spamed",
  "&f   the console",
  "&f - &bhud3D.newBlock().changeTexture &fnow allows for a second argument",
  "&f   this will set the texture for one side.",
  "&f   Valid sides are:",
  "&f     - \"up\" or \"top\"",
  "&f     - \"down\" or \"bottom\"",
  "&f     - \"north\"",
  "&f     - \"west\"",
  "&f     - \"east\"",
  "&f     - \"south\"",
  "&f - Tooltips will now show from tables with the meta table containing:",
  "&f   - luaFunction = true",
  "&f   - definition=\"someFunction(a, b, c)\"",
  "&f   - tooltip={\"line 1\", \"line 2\"....} or \"single line\"",
  "&f   - luaDoc={\"line 1\", \"line 2\"....} or \"single line\"",
  "&f     this will be used for an in game help page later",
  "&f   - types={{'string', 'number', 'opt_number'}, {'string', 'string'}}",
  "&f     if someFunction('blah', 4 [,10]) and someFunction('a', 'b')",
  "&f     both work, this will be used for autocomplete in the future",
  "&f   - It should also have a value at __call",
  "&f - &bgetPlayer() &f now includes a value &aentityRiding &f",
  "&f   containing entity info about whatever the player is on",
  "&f   this chagne also effects &bgetEntityData()",
  "&f - Tooltips added for a bunch of common &bgui &ffunctions",
  "&f - A placeholder for container controls has been made",
  "&f   this will become available on inventories from the ContainerOpen",
  "&f   event",
  "&f - image.graphics.drawImage now allows additional arguments for",
  "&f   source location and scaling",
  "&f   drawImage(img, x, y, wid, hei, srcX, srcY, srcWid, srcHei)",
  "&f - now built on a newer version of forge",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.2.0",
  "&f - Due to a typo &bhud3D.block &fwoudln't change its texture,",
  "&f   this is fixed now.",
  "&f - if you have a hud block and do &bsetTexture&f, you can now include",
  "&f   a second argument containing:",
  "&f   &eup&f/&etop&f/&edown&f/&ebottom&f/&enorth&f/&ewest&f/&eeast&f/&esouth",
  "&f   it will set the texture for",
  "&f   only that side",
  
  "&7"..DIVIDER,
  
  "&b&BChange Log: &7version 5.1.0",
  "&f - Textures are now simpler to use.", 
  "&f   the &bimg.texture.create() &fand &bimg.texture.remove()",
  "&f   in images have been removed, and &bimg.texture.update()",
  "&f   is now &bimg.update()",
  "",
  "&f - If the player hasn't loaded yet, either in a menu or while",
  "&f   joining the world it will", 
  "&f   return &enil &finstead of causing a &cNullPointerException&f now",
  "",
  "&f - Added &bthread.new() &fand &bthread.current()",
  "&f   &bthread.new() &fwill allow you to create a thread without starting it",
  "&f   &bthread.current() &fwill return controls for the current thread",
  "&f   thread controls now contains &bgetID() &ffor comparison"
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
while( getPlayer() == nil)do 
  sleep(1000) 
end
sleep(2000)
g.open()
