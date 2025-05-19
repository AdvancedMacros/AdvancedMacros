local utils = advancedMacros.utils
local misc = require"misc"
local Element = require"ui/Element"
local ContextMenuItem = require"ui/ContextMenuItem"
local DelayedTask = require"DelayedTask"

local ContextMenu = newClass("ui/ContextMenu")

local dummyFunction = function() end

function ContextMenu:new( ... )
  local obj = ContextMenu._new( self, ... )
  
  local args = utils.kwargs({
    { parent = {"nil","table"} }, --parent gui should be passed unless it's a submenu
    { options = "table", nil, "layout" }, --array of ContextMenuItem
    { bgColor = "table", { .2, .2, .2, 1 }, "color" },
    { defaultOnSelect = {"function"}, dummyFunction, "onSelect" },
    { root = {"nil","class:ui/ContextMenu"}}, --first layer context menu
  }, ...)
  
  
  obj.screen = gui.new()
  obj.group = obj.screen.newGroup()
  -- obj.group.setZ( 20 ) --doesn't do move op, applies transform to all children when drawing
  if args.parent then
    obj.screen.setParentGui( args.parent )
  end
  
  obj.root = args.root
  obj.bgColor = args.bgColor

  if args"options" == "layout" then
    obj.options = obj:_buildOptions( args.layout, args.defaultOnSelect )
  else
    obj.options = args.options
  end

  obj.optWidth = 0
  obj.height = 1 + #obj.options
  obj.hasSubMenus = false
  for i, opt in ipairs( obj.options ) do
    obj.optWidth = math.max(obj.optWidth, opt:getWidth())
    obj.height = obj.height + opt:getHeight()
    obj.hasSubMenus = obj.hasSubMenus or opt.hasSubMenu
    
    opt:setOnMouseEnter(function() obj:onMouseEnterItem( opt ) end)
    opt:setOnMouseExit(function() obj:onMouseExitItem( opt ) end)
    opt.rootMenu = obj.root or obj
    opt.menu = obj
  end

  obj.optWidth = obj.optWidth + ( obj.hasSubMenus and 12 or 0 )

  for i, opt in ipairs( obj.options ) do
    opt:setWidth( obj.optWidth )
  end

  obj.background = obj.screen.newRectangle( 1, 1, obj.optWidth+8, 20 ) --placeholder height
  obj.frame = obj.screen.newBox( 0, 0, obj.optWidth+10, 22 ) --placeholder height
  obj.scrollbar = obj.screen.newScrollBar( obj.optWidth+10, 0, 5, 22 ) --placeholder height
  
  obj.frame.setColor(0xFFFFFFFF)

  obj.background.setParent( obj.group )
  obj.frame.setParent( obj.group )
  obj.scrollbar.setParent( obj.group )

  obj.delayedClose = {}

  if args.parent then
    obj.screen.setOnMouseClick(function()
      args.parent.open() 
      return true
    end)
  end

  return obj
end

function ContextMenu:onMouseEnterItem( option )
  option.enterTime = os.clock()
  if option.subMenu then
    -- if self.activeMenu and self.activeMenu ~= option then --instant swap
    --   if self.delayedClose[ option ] then
    --     self.delayedClose[ option ]:cancel()
    --     self.delayedClose[ option ] = nil
    --   end
    --   self.activeMenu:close()
    -- end
    self.activeMenu = option.subMenu
    local x, y = option:getPos()
    local w, h = self:getWidth()
    
    option.subMenu:open(x + w, y) --TODO position fully to left or right side
  end
end

function ContextMenu:onMouseExitItem( option )
  if option == self.activeMenu then
    if not self.delayedClose[ option ] then
      self.delayedClose[ option ] = DelayedTask:new({
        task = function() 
          option.subMenu:close()
        end,
        seconds = 1
      }):start()
    end
  end
end

function ContextMenu:findMenuItem( keyName, keyValue )
  for i, opt in ipairs( self.options ) do
    if opt[keyName] == keyValue then
      return opt
    end
  end
end

function ContextMenu:open(x, y)
  local mouseX, mouseY = self.screen.getMousePos()
  x, y = x or mouseX, y or mouseY
  local sw, sh = self.screen.getSize()
  local maxHeight = math.floor(sh * 3 / 4)
  
  if maxHeight >= self.height then
    maxHeight = self.height
    self.scrollbar.setVisible( false )
  else
    self.scrollbar.setVisible( true )
  end

  self.background.setHeight( maxHeight )
  self.frame.setHeight( maxHeight + 2 )
  self.scrollbar.setHeight( maxHeight + 2 )
  self.group.setPos( x, y )
  self.group.setVisible( true )

  self:_positionItems()

  self.screen.open()
end

function ContextMenu:close()
  self.group.setVisible( false )
  if self.activeMenu then
    self.activeMenu:close()
  end
  log(utils.keys(self))
  local root = self.root or self
  if root.screen.getParentGui() then --gui
    root.screen.getParentGui().open()
  end
end

function ContextMenu:_positionItems()
  local x, y = self.group.getPos()
  x, y = x+3, y+4 --margins
  for i, opt in ipairs( self.options ) do
    opt:setPos( x+3, y )
    y = y + opt:getHeight()
  end
end

function ContextMenu:_buildOptions( layout, defaultOnSelect )
  local options = {}
  local callback = function( item )
    -- toast("Missing", item:getLabel())
  end

  for i, info in ipairs( layout ) do
    local args = {
      screen = self.screen,
      icon = info.icon,
      label = info.label,
      height = info.height,
    }

    if info.menu then
      args.subMenu = ContextMenu:new{
        parent = self.screen,
        layout = info.menu,
        bgColor = self.bgColor,
        defaultOnSelect = defaultOnSelect,
        root = self
      }
    else
      args.onClick = info.onClick or defaultOnSelect or callback
    end

    local item = ContextMenuItem:new(args)
    info.contextMenuItem = item
    table.insert( options, item )
  end
  return options
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
  ContextMenu[ func ] = function( self, ... )
    return self.group[ func ]( ... )
  end
end

for _,func in ipairs{
  "getWidth",
  "getHeight",
  "getSize",
} do
  ContextMenu[ func ] = function( self, ... )
    return self.background[ func ]( ... )
  end
end

return ContextMenu