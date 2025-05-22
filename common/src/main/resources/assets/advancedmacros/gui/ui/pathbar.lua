--For file browser
local utils = advancedMacros.utils
local EventChannel = require"ui/EventChannel"

local Element = require"ui/Element"
local PathBar = newClass("ui/PathBar", Element)

function PathBar:new( ... )
  local obj = PathBar._new( self, ... )
  
  -- local args = utils.kwargs({
  -- }, ...)

  obj.events.pathChanged = EventChannel:new{}

  obj.elements.pathBar = {}
  obj.elements.pathBar.path = {}
  obj.elements.pathBar.background = obj.screen.newRectangle( 0, 0, obj.width, obj.height )

  obj.elements.pathBar.background.setColor( 0x44000000 )
  obj.elements.pathBar.background.setParent( obj.group )

  if self == PathBar then
    obj:_postConstruct()
  end
  
  return obj
end

function PathBar:setPath( path )
  if path == nil then error("Missing arg", 2) end
  if path == self.path then return end
  self.path = path
  local parts = {}
  for x in path:gmatch"[^/\\]+" do
    table.insert( parts, x )
    table.insert( parts, "/" )
  end
  table.remove( parts, #parts )
  -- log(parts)

  local buttons = self.elements.pathBar.path
  --hide extra
  for i = #buttons, #parts+1, -1 do
    buttons[i].bg.setVisible(false)
    buttons[i].text.setVisible(false)
    buttons[i] = nil
  end

  --add new
  for i = #buttons+1, #parts do
    buttons[i] = {
      text = self.screen.newText("",0,0,self:getHeight()-2),
      bg = self.screen.newRectangle(0,0,0,self:getHeight()-2),
    }
    buttons[i].bg.setWidth(buttons[i].text.getWidth())
    buttons[i].bg.setParent( self.group )
    buttons[i].bg.setColor( 0 )
    buttons[i].text.setZ(1)
    buttons[i].text.setParent( self.group )
  end

  --udpate all
  local fullPath = ""
  local x = 0
  for i = 1, #buttons do
    local isPathSep = parts[i] == "/"
    buttons[i].text.setText( parts[i] )
    buttons[i].text.setVisible(true)
    buttons[i].bg.setVisible(true)
    buttons[i].bg.setWidth(buttons[i].text.getWidth()+1)
    fullPath = fullPath .. parts[i]
    if isPathSep then
      buttons[i].bg.setHoverTint()
      buttons[i].bg.setOnMouseClick()
    else
      local clickPath = fullPath
      buttons[i].bg.setOnMouseClick(function() log(clickPath) self:setPath( clickPath ) end)
      buttons[i].bg.setHoverTint( 0x44FFFFFF )
    end
    buttons[i].bg.setX( x )
    buttons[i].text.setX( x )
    x = x + buttons[i].text.getWidth() + 2
  end

  --show last part only for long paths
  local over = self:getWidth() - x + 2
  local shift
  if over > 0 then
    for i = 1, #buttons do
      local b = buttons[i]
      local x, w = b.text.getX(), b.text.getWidth()
      if shift then
        b.bg.setX( self.group.getX() + x + shift )
        b.text.setX( self.group.getX() + x + shift )
      elseif x + w > over then
        buttons[i].text.setVisible(false)
        buttons[i].bg.setVisible(false)
        over = over - w - 2
      else
        shift = -x
        b.bg.setX( self.group.getX() )
        b.text.setX( self.group.getX() )
      end
    end
  end
  self.events.pathChanged:notify( path )
end

return PathBar