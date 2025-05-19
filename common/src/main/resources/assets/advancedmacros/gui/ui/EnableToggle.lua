local utils = advancedMacros.utils
local misc = require"misc"
local Element = require"ui/Element"

local EnableToggle = newClass("ui/EnableToggle", Element)

EnableToggle.static = {
  tiles = {},
  defaultDisabledColor  = { 1, 0, 0, 1 }, --Red
  defaultEnabledColor   = { 0, 1, 0, 1 }, --Green
  defaultGroupDisabledColor = { .25, .25, .25, 1 } --Dark Gray, colored frame used
}

local function generateBubble( self, size, borderThickness, color, frame )
  local key = ("%.3f:%.3f:%.3f:%.3f:%.3f:%.3f:%.3f:%.3f:%.3f:%.3f"):format( size, borderThickness, color[1], color[2], color[3], color[4], frame[1], frame[2], frame[3], frame[4] )
  --log(key)
  if EnableToggle.static.tiles[ key ] then
    return EnableToggle.static.tiles[ key ]
  end

  local resScale = misc.resScale( self.screen )
  size = size*resScale
  borderThickness = borderThickness*resScale

  local img = image.new( size, size )
  local g = img.graphics

  g.setColor( frame )
  g.fillOval( 1, 1, size, size )
  g.setColor( color )
  g.fillOval( 1 + borderThickness, 1 + borderThickness, size - borderThickness*2, size - borderThickness*2 )
  img.update()

  EnableToggle.static.tiles[ key ] = img
  return img
end

function EnableToggle:new( ... )
  local obj = EnableToggle._new( self, ... )
  
  local args = utils.kwargs({
    { enabled = "boolean", true, "isEnabled" },
    { parentEnabled = "boolean", true },
    { borderThickness = "number", 1 },
    { onToggle = {"nil", "function"}, nil, "onEnableChanged" }, --( self, isEnabled )
    { enableColor = {"nil", "table"} },
    { disableColor = {"nil", "table"} },
    { groupDisabledColor = {"nil", "table"} }
  }, ...)
  local mSize = math.min( obj.width, obj.height )
  obj.width, obj.height = mSize, mSize

  obj.borderThickness = args.borderThickness
  obj.parentEnabled    = args.parentEnabled
  obj.enableColor     = args.enableColor
  obj.disableColor    = args.disableColor
  obj.groupDisabledColor   = args.groupDisabledColor

  obj.elements.enableToggle = {
    button = obj.screen.newImage( nil, 0, 0, mSize, mSize )
  }

  obj:setEnabled( args.enabled )
  obj.onToggle = args.onToggle
  
  for i,e in pairs( obj.elements.enableToggle ) do
    e.setParent( obj.group )
  end

  -- obj.elements.enableToggle.button.setHoverTint(0x77000000)
  obj.elements.enableToggle.button.setOnMouseClick( function( x, y, button )
    if not obj:isVisible() then return false end
    obj:setEnabled( not obj.enabled )
    return true --event consumed
  end)
  obj.elements.enableToggle.button.setOnMouseEnter( function() obj:updateImage() end)
  obj.elements.enableToggle.button.setOnMouseExit(  function() obj:updateImage() end)
  
  if self == EnableToggle then
    obj:_postConstruct()
  end

  return obj
end

function EnableToggle:setEnabled( state )
  self.enabled = state
  self:updateImage()
end

function EnableToggle:isEnabled()
  -- log("&aEnableToggle: &NSELF&f &NPARENT",self.enabled, self.parentEnabled)
  return self.enabled and self.parentEnabled
end

function EnableToggle:setParentEnabled( state )
  assert(type(state)=="boolean", "boolean expected")
  self.parentEnabled = state
  self:updateImage()
end

function EnableToggle:updateImage()
  local color, frameColor
  
  if self.enabled then
    color = self.enabledColor or EnableToggle.static.defaultEnabledColor
    frameColor = misc.trimColor(color)
  else
    color = self.disabledColor or EnableToggle.static.defaultDisabledColor
    frameColor = misc.trimColor(color)
  end
  
  if not self.parentEnabled then
    color = self.groupDisabledColor or EnableToggle.static.defaultGroupDisabledColor
  end

  --log(self.elements.enableToggle.button.isHover())
  if self.elements.enableToggle.button.isHover() then
    color = misc.darkenColor( color )
  end
  --log("&aColor: ", color)

  self.elements.enableToggle.button.setImage( generateBubble( 
    self,
    math.min(self.width, self.height), 
    self.borderThickness, 
    color,
    frameColor
  ))
  if self.onToggle then
    self:onToggle( self.enabled, self.groupEnabled )
  end
end

--expose group functions as card functions
for _,func in ipairs{
  "setVisible",
  "isVisible",
  "move",
  "setPos",
  "getPos",
  "getX",
  "getY",
  "setParent",
  "addSubGroup",
  "getChildren"
} do
  EnableToggle[ func ] = function( self, ... )
    return self.group[ func ]( ... )
  end
end

return EnableToggle