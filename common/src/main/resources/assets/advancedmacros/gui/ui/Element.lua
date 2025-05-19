local utils = advancedMacros.utils
local EventChannel = require"ui/EventChannel"

local Element = newClass"ui/Element"

function Element:new( ... )
  local obj = Element._new( self )
  
  local args = utils.kwargs({
    { screen = "table", nil, "gui" },
    { parent = {"nil","table","userdata"}}, --might just be userdata only?
    { x = "number", 0 },
    { y = "number", 0 },
    { width = "number", nil, "wid", "w" },
    { height = "number", nil, "hei", "h" },
    { visible = "boolean", true},
  }, ...)

  obj.screen = args.screen
  obj.group = obj.screen.newGroup()
  obj.x = args.x
  obj.y = args.y
  obj.width = args.width
  obj.height = args.height
  obj.events = {
    resize = EventChannel:new{},
    reposition = EventChannel:new{},
    rmb = EventChannel:new{},         --TODO implemment listener in other ui elements
  }
  
  if args.parent then
    obj.group.setParent( args.parent )
  end

  obj.elements = {}

  obj.group.setVisible( args.visible )
  
  if self == Element then
    self:_postConstruct()
  end
  return obj
end

function Element:setParent( group )
  self.group.setParent( group )
end

function Element:getWidth()
  return self.width
end

function Element:getHeight()
  return self.height
end

function Element:getSize()
  return self:getWidth(), self:getHeight()
end

function Element:setX( x, notify )
  self.group.setX( x )
  if notify == false then return end
  self.events.reposition:notify( self, self.group.getPos() )
end

function Element:setY( y, notify )
  self.group.setY( y )
  if notify == false then return end
  self.events.reposition:notify( self, self.group.getPos() )
end

function Element:setPos( x, y )
  self:setX( x, false )
  self:setY( y, false )
  self.events.reposition:notify( self, self.group.getPos() )
end

function Element:move( dx, dy )
  local x, y = self.group.getPos()
  self:setPos( x + dx, y + dy )
end

function Element:setWidth( width, notify )
  self.width = width
  if notify == false then return end
  self.events.resize:notify( self, self.width, self.height )
end

function Element:setHeight( width, notify )
  self.width = width
  if notify == false then return end
  self.events.resize:notify( self, self.width, self.height )
end

function Element:setSize( width, height )
  self:setWidth( width )
  self:setHeight( height )
  self.events.resize:notify( self, self.width, self.height )
end

function Element:notifyResize()
  self.events.resize:notify( self, self.width, self.height )
end

--expose group functions as card functions
for _,func in ipairs{
  "setVisible",
  "isVisible",
  "getPos",
  "getX",
  "getY",
  "getZ",
  "setZ",
  "setParent",
  "addSubGroup",
  "getChildren"
} do
  Element[ func ] = function( self, ... )
    assert(self, "self invalid, check caller")
    assert(self.group, "group not found, called with : ?")
    assert(self.group[ func ], "Cant find "..func )
    return self.group[ func ]( ... )
  end
end

function Element:_postConstruct()
  self.group.setPos( self.x, self.y )
  self.x, self.y = nil, nil
end

function Element:remove()
  --all children should be attached to this group
  self.group.remove()
end

return Element