--should have made this one sooner...
local utils = advancedMacros.utils

local Element = require"ui/Element"
local FramedRectangle = newClass("ui/FramedRectangle", Element)

function FramedRectangle:new( ... )
  local obj = FramedRectangle._new( self, ... )
  
  local args = utils.kwargs({
    { frameThickness = "number", 1 },
    { frameColor = {"number", "table"}, 0xFFFFFFFF },
    { backgroundColor = {"number", "table"}, 0xFF000000 },
    { clipping = "boolean", false, "enableClipping", "scissor", "enableScissor" }
  }, ...)

  obj.frameThickness = args.frameThickness
  obj.clipping = args.clipping

  obj.elements.background = obj.screen.newRectangle( 
    args.frameThickness, 
    args.frameThickness, 
    obj.width - args.frameThickness * 2, 
    obj.height - args.frameThickness * 2
  )
  obj.elements.frame = obj.screen.newBox( 0, 0, obj.width, obj.height )
  obj.interiorGroup = obj.screen.newGroup( args.frameThickness, args.frameThickness )

  obj.elements.background.setColor( args.backgroundColor )
  obj.elements.frame.setColor( args.frameColor )

  obj.elements.background.setParent( obj.group )
  obj.elements.frame.setParent( obj.group )
  obj.interiorGroup.setParent( obj.group )
  
  if self == FramedRectangle then
    obj:_postConstruct()
  end

  return obj
end

function FramedRectangle:setWidth( width, ... )
  self.elements.background.setWidth( width - self.frameThickness * 2 )
  self.elements.frame.setWidth( width )
  if self.clipping then
    self.interiorGroup.setScissor( self:getInteriorSize() )
  end
  FramedRectangle:super().setWidth( self, width, ... )
end

function FramedRectangle:setHeight( height, ... )
  self.elements.background.setHeight( height - self.frameThickness * 2 )
  self.elements.frame.setHeight( height )
  if self.clipping then
    self.interiorGroup.setScissor( self:getInteriorSize() )
  end
  FramedRectangle:super().setHeight( self, height, ... )
end

function FramedRectangle:getInteriorWidth()
  return self.width - self.frameThickness * 2
end

function FramedRectangle:getInteriorHeight()
  return self.height - self.frameThickness * 2
end

function FramedRectangle:getInteriorSize()
  return self:getInteriorWidth(), self:getInteriorHeight()
end

--expose group functions as card functions
for _,func in ipairs{
  "setOnMouseClick",
  "setOnMouseEnter",
  "setOnMouseExit",
  "setOnMouseRelease",
  "setOnMouseDrag",
  "setOnScroll",
  "setOnKeyPressed",
  "setOnKeyReleased",
  "setOnKeyRepeated",
  "setOnCharTyped",
  "isHover",
  "setHoverTint",
  "getHoverTint",
} do
  FramedRectangle[ func ] = function( self, ... )
    assert(self, "self invalid, check caller")
    assert(self.elements.background, "group not found, called with : ?")
    assert(self.elements.background[ func ], "Cant find "..func )
    return self.elements.background[ func ]( ... )
  end
end

return FramedRectangle