--check if the gui should show and set the default value if missing
local settings = getSettings()
settings.changeLog = settings.changeLog or {}
local cl = settings.changeLog
cl.showOnStartup = cl.showOnStartup==nil or cl.showOnStartup
if not cl.showOnStartup then return end
cl.shownVersions = cl.shownVersions or {}
if cl.shownVersions[_MOD_VERSION] then return end

-- CHANGE LOG -------------------------------------------------------------------
local changeLog = {
  "&eShow on startup? ", --do not remove
  "&b&BChange Log: &7version ".._MOD_VERSION, --do not remove
  ""
}

---------------------------------------------------------------------------------

local CHECK = "resource:greencheck.png"
local X     = "resource:redx.png"
g = gui.new()

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
  log( "New width: "..w)
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
g.open()
