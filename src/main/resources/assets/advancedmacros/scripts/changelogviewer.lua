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
  "&b&BChange Log: &7version ".._MOD_VERSION, --do not remove
  
  "&f - Fixed &bfilesystem &fclose warning showing when a file DOES close",
  "&f   It also includes a stack trace. This triggers when garbage collection",
  "&f   occurs in java",
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
