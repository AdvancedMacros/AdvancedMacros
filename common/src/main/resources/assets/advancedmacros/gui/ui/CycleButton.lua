local utils = advancedMacros.utils
local EventChannel = require"ui/EventChannel"

local Element = require"ui/Element"
local CycleButton = newClass("ui.CycleButton", Element)

function CycleButton:new( ... )
  local obj = CycleButton._new( self, ... )

  local args = utils.kwargs({
    { options = "table", nil}, -- table of groups or images
    { labels = {"nil","table"}}, 
    { tooltips = {"nil","table"}, nil, "toolTips"}, --TODO
    { optionMode = "string" }, -- "groups" or "images" 
    { selected = {"number"}, 1, "selection" },
    { hoverTint = {"table","number"}, 0x55000000, "hoverColor", "tint" },
    { frame = "number", 0 },
    { onChange = {"nil", "function"}},
    { backgroundColor = {"table", "number"}, 0 }, --transparent
  },...)
  
  assert( #args.options > 0                , "must contain at least one option" )
  assert( args.options[ args.selected ], "selected index ("..tostring(args.selected)..") out of bounds" )

  obj.options = args.options
  obj.labels = args.labels
  obj.tooltips = args.tooltips
  obj.selected = args.selected
  obj.optionMode = args.optionMode
  obj.events.change = EventChannel:new{}

  if args.onChange then
    obj.events.change:addListener( args.onChange )
  end

  obj.elements = {
    cycleButton = {}
  }
  
  if args.backgroundColor > 0 then
    obj.elements.cycleButton.background = obj.screen.newRectangle(
      0, 0, obj.width, obj.height
    )
    obj.elements.cycleButton.background.setColor( args.backgroundColor )
    obj.elements.cycleButton.background.setParent( obj.group )
  end

  obj:_setupOptionMode( args.frame > 0, args.hoverTint )

  obj.elements.cycleButton.hitbox.setOnMouseClick( function( x, y, button )
    if not obj:isVisible() then
      return false --event not consumed
    end
    local newSelection = obj.selected % #obj.options + 1
    obj:setSelection( newSelection )
    return true --event consumed
  end)

  if args.frame > 0 then
    obj.elements.cycleButton.frame = obj.screen.newBox( 0, 0, obj.width, obj.height )
    obj.elements.cycleButton.frame.setColor(0xFFFFFFFF)
    obj.elements.cycleButton.frame.setParent(obj.group)
  end

  if self == CycleButton then
    obj:_postConstruct()
  end

  return obj
end

function CycleButton:_setupOptionMode( hasFrame, hoverTint )
  local a = hasFrame and 1 or 0
  local s = hasFrame and -2 or 0
  if self.optionMode  == "images" then
    self.elements.cycleButton.hitbox = self.screen.newImage( self:getSelection(), a, a, s + self.width, s + self.height )

  elseif self.optionMode == "groups" then
    self.elements.cycleButton.hitbox = self.screen.newRectangle( 0, 0, self.width, self.height )
    self.elements.cycleButton.hitbox.setZ( 5 )
    
    for self in ipairs( self.options ) do
      self.setVisible( self == self.selected )
      self.setPos( 0, 0 )
      self.setParent( self.group )
    end
  else
    error("Unexpected option mode '"..self.optionMode.."'")
  end
  
  self.elements.cycleButton.hitbox.setParent( self.group )
  self.elements.cycleButton.hitbox.setHoverTint( hoverTint )
end

function CycleButton:getSelection()
  return self.options[ self.selected ]
end

function CycleButton:getSelectionLabel()
  if not self.labels then return nil end
  return self.labels[ self.selected ]
end

function CycleButton:setSelectionByLabel( label )
  local index = utils.findInTable( label, self.labels )
  assert( index, "No such label `"..tostring(label).."`")
  self:setSelection( index )
end

function CycleButton:setSelection( selection )
  assert( self.options[ selection ], "selected index out of bounds" )
  
  if self.optionMode == "images" then
    self.selected = selection
    self.elements.cycleButton.hitbox.setImage( self:getSelection() )

  elseif self.optionMode == "groups" then
    self.options[ self.selected ].setVisible( false )
    self.selected = selection
    self.options[ selection ].setVisible( true )
  
  else
    error("Unexpected option mode '"..self.optionMode.."'")
  end

  self.events.change:notify( self, self.options[ self.selected ], self.selected, self.labels and self.labels[ self.selected ] )
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
  CycleButton[ func ] = function( self, ... )
    return self.group[ func ]( ... )
  end
end

for _,func in ipairs{
  "setOnMouseEnter",
  "setOnMouseExit",
  "isHover",
  "setHoverTint",
  "getHoverTint",
  "setOnScroll",
} do
  CycleButton[ func ] = function( self, ... )
    return self.elements.cycleButton.hitbox[ func ]( ... )
  end
end

return CycleButton