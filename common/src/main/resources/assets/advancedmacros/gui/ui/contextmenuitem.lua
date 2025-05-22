local utils = advancedMacros.utils

local ContextMenuItem = newClass("ui/ContextMenuItem")

function ContextMenuItem:new( ... )
  local obj = ContextMenuItem._new( self, ... )
  
  local args = utils.kwargs({
    {screen = "table"},
    {icon = {"nil","string","table"}, nil, "image"},
    {label = "string"},
    {height = "number", 12},
    {onClick = "function", nil, "callback", "onSelect", {"subMenu", "class:ui/ContextMenu"}},
    {z = "number", 1}
  }, ...)

  obj.screen = args.screen
  obj.group = obj.screen.newGroup()
  obj.height = args.height

  if args"onClick" == "subMenu" then
    obj.subMenu = args.subMenu
  else
    obj.onClick = args.onClick
  end

  obj.elements = { 
    contextMenuItem = {
      background = obj.screen.newRectangle( 0, 0, 0, obj.height ),
      label = obj.screen.newText( args.label, obj.height + 2, 0, obj.height ),
    }
  }

  obj.elements.contextMenuItem.background.setHoverTint(0x550080FF)

  if args.icon then
    obj.elements.contextMenuItem.icon = obj.screen.newImage( args.icon, 0, 0, obj.height, obj.height )
  end
  local cmi = obj.elements.contextMenuItem

  obj.width = cmi.label.getX() + cmi.label.getWidth()

  if obj.subMenu then
    cmi.subMenuIndicator = obj.screen.newText( ">", 0, 0, obj.height )
    cmi.subMenuIndicator.setX( obj.width + 2 )
    cmi.subMenuIndicator.setParent( obj.group )
    obj.width = obj.width + 4 + cmi.subMenuIndicator.getWidth()
  end

  obj.minWidth = obj.width

  if not obj.subMenu then
    obj:setOnMouseClick( function()
      obj.rootMenu:close()
      obj:onClick()
      return true
    end )
  end


  for i, e in pairs(cmi) do
    e.setParent( obj.group )
    e.setZ( args.z )
  end
  
  return obj
end

function ContextMenuItem:getLabel()
  return self.elements.contextMenuItem.label.getText()
end

function ContextMenuItem:getWidth()
  return self.width
end

function ContextMenuItem:getHeight()
  return self.height
end

function ContextMenuItem:setWidth( w )
  self.width = math.max( self.minWidth, w )
  local smi = self.elements.contextMenuItem.subMenuIndicator
  self.elements.contextMenuItem.background.setWidth( self.width )
  if not smi then return end
  smi.setX( self.width - 2 - smi.getWidth() )
end

function ContextMenuItem:getSubMenu()
  return self.subMenu
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
  ContextMenuItem[ func ] = function( self, ... )
    return self.group[ func ]( ... )
  end
end

for _,func in ipairs{
  "setOnMouseClick",
  "setOnMouseEnter",
  "setOnMouseExit",
} do
  ContextMenuItem[ func ] = function( self, ... )
    return self.elements.contextMenuItem.background[ func ]( ... )
  end
end

return ContextMenuItem