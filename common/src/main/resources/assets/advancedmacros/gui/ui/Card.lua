local utils = advancedMacros.utils
local misc = require"misc"
local Element = require"ui/Element"
local ContextMenu = require"ui/ContextMenu"
local EnableToggle = require"ui/EnableToggle"
local CycleButton = require"ui/CycleButton"
local EventChannel = require"ui/EventChannel"

local Card = newClass("ui.Card", Element)

Card.static = {
  tiles = {},
  disabledColor = {.4, .4, .4, 1}
}

-- local TOP_COLOR = {.8, .8, .8, 1}
-- local TOP_FRAME_COLOR = misc.trimColor( TOP_COLOR )
--store defaults true, set false if testing colors to not save a bunch of junk
local function generateCornerTile( cx, cy, r, color, frameColor, borderThickness, store )
  local key = ("CORNER_%.3f:%.3f:%.3f:%.3f:%.3f:%.3f"):format( cx, cy, r, table.unpack(color) )
  if Card.static.tiles[ key ] then
    return Card.static.tiles[ key ]
  end

  local img = image.new( r, r )
  local g = img.graphics
  g.setColor( frameColor )
  g.fillOval( cx-r, cy-r, r*2, r*2 )
  g.setColor( color )
  g.fillOval( cx-r+borderThickness, cy-r+borderThickness, r*2 - borderThickness*2, r*2-borderThickness*2 )
  if store ~= false then
    Card.static.tiles[ key ] = img
  end
  img.update()
  return img
end
local function generateCollapsedTile( isLeft, r, color, frameColor, borderThickness )
  local key = ("COLLAPSED_%s:%.3f:%.3f:%.3f:%.3f:%.3f"):format( isLeft and "left" or "right", r, table.unpack(color) )
  if Card.static.tiles[ key ] then
    return Card.static.tiles[ key ]
  end


  local img = image.new( r*2, r*2 )
  local g = img.graphics
  g.setColor( frameColor )
  g.fillOval( 1, 1, r*2, r*2 )
  g.setColor( color )
  g.fillOval( 1+borderThickness, 1+borderThickness, r*2 - borderThickness*2, r*2-borderThickness*2 )
  
  if isLeft then
    g.setColor( frameColor )
    g.fillRect( r, 1, r, borderThickness )
    g.fillRect( r, r*2-borderThickness, r, borderThickness )
    g.setColor( color )
    g.fillRect( r, borderThickness + 1, r, r*2 - borderThickness * 2)
  else
    g.setColor( frameColor )
    g.fillRect( 1, 1, r, borderThickness )
    g.fillRect( 1, r*2-borderThickness, r, borderThickness )
    g.setColor( color )
    g.fillRect( 1, borderThickness + 1, r, r*2 - borderThickness * 2)
  end

  Card.static.tiles[ key ] = img
  img.update()
  return img
end

function Card:new( ... )
  local obj = Card._new( self, ... )
  
  local args = utils.kwargs({
    {collapsable = "boolean", true, "isCollapsable"},
    {closeable = "boolean", true, "isCloseable"},
    {enabled = "boolean", true, "isEnabled"},
    {enableable = "boolean", true, "disableable"},
    {parentEnabled = "boolean", true, "parentIsEnabled", "isParentEnabled"},
    {label = "string", "", "title"},
    {labelHeight = {"nil","number"}, 14}, --GUI pixels, defaults to radius, can't be less than radius
    {radius = "number", 12}, --GUI pixels
    {borderThickness = "number", 1},
    {color  = "table"},
    {collapsed = "boolean", false, "isCollapsed"},
    {showDivider = "boolean", true },
    {useColorCache = "boolean", true },
    {enableContext = "boolean", true, "enableContextMenu" },
  },...)

  obj.isCollapsable = args.collapsable
  obj.label = args.label
  obj.labelHeight = args.labelHeight and math.max( args.radius, args.labelHeight ) or args.radius
  obj.radius = args.radius
  obj.borderThickness = args.borderThickness
  obj.color = args.color
  obj.enabledColor = args.color
  obj.collapsed = args.collapsed and obj.isCollapsable
  obj.useColorCache = args.useColorCache
  
  obj.head = obj.screen.newGroup()
  obj.head.setParent( obj.group )
  obj.body = obj.screen.newGroup()
  obj.body.setParent( obj.group )
  obj.body.setY( obj.labelHeight )
  
  obj.tiles = {} --images
  obj.elements.cardHead = {} --gui components
  obj.elements.cardBody = {} --gui components

  -- obj.elements.card.BB = obj.screen.newBox( 0, 0, obj.width, obj.height, 1)

  local gap = -(obj.labelHeight - obj.radius)
  local bodyHeight = obj:getBodyHeight()
  obj.elements.cardHead.topLeftBG      = obj.screen.newImage( nil,                       0,  0, args.radius, args.radius )
  obj.elements.cardHead.topRightBG     = obj.screen.newImage( nil, obj.width - args.radius,  0, args.radius, args.radius )
  obj.elements.cardBody.bottomLeftBG   = obj.screen.newImage( nil,                       0, bodyHeight - args.radius, args.radius, args.radius )
  obj.elements.cardBody.bottomRightBG  = obj.screen.newImage( nil, obj.width - args.radius, bodyHeight - args.radius, args.radius, args.radius )
  obj.elements.cardHead.topFrame       = obj.screen.newRectangle( obj.radius, 0, obj.width - obj.radius*2, obj.borderThickness )
  obj.elements.cardHead.top            = obj.screen.newRectangle( obj.radius, obj.borderThickness, obj.width - obj.radius*2, obj.radius - obj.borderThickness )
  obj.elements.cardBody.middle         = obj.screen.newRectangle( obj.borderThickness, gap, obj.width - obj.borderThickness*2, obj.height - obj.radius*2)
  obj.elements.cardBody.leftFrame      = obj.screen.newRectangle( 0, gap, obj.borderThickness, obj.height - obj.radius * 2)
  obj.elements.cardBody.rightFrame     = obj.screen.newRectangle( obj.width - obj.borderThickness, gap, obj.borderThickness, obj.height - obj.radius * 2)
  obj.elements.cardBody.bottom         = obj.screen.newRectangle( obj.radius, bodyHeight - obj.radius, obj.width - obj.radius * 2, obj.radius - obj.borderThickness )
  obj.elements.cardBody.bottomFrame    = obj.screen.newRectangle( obj.radius, bodyHeight - obj.borderThickness, obj.width - obj.radius*2, obj.borderThickness )
  obj.elements.cardBody.divider        = obj.screen.newRectangle( obj.borderThickness, 0, obj.width - obj.borderThickness*2, obj.borderThickness )
  obj.elements.cardHead.label          = obj.screen.newText( args.label, obj.radius / 2 + 10, obj.labelHeight - 12, 12)
  
  obj.elements.cardBody.divider.setZ(1)
  obj.elements.cardBody.divider.setVisible( args.showDivider )

  for name, e in pairs( obj.elements.cardHead ) do
    e.setParent( obj.head )
    e.setOnMouseClick( function(x, y, b) obj:onCardClick(x, y, b, e) end )
  end
  
  for name, e in pairs( obj.elements.cardBody ) do
    e.setParent( obj.body )
    e.setOnMouseClick( function(x, y, b) obj:onCardClick(x, y, b, e) end )
  end

  obj.elements.cardHead.remove = obj.screen.newImage("resource:remove.png", obj.width - obj.radius, obj.labelHeight / 2 - 3, 6, 6 )
  obj.elements.cardHead.remove.setParent( obj.head )
  obj.elements.cardHead.remove.setZ( 2 )
  obj.elements.cardHead.remove.setHoverTint( 0x40000000 )
  obj.elements.cardHead.remove.setVisible( args.closeable )
  obj.events.close = EventChannel:new{}
  obj.elements.cardHead.remove.setOnMouseClick(function() obj.events.close:notify( obj ) end)
  
  if obj.isCollapsable then
    obj.elements.cardHead.topLeftBG_collapsed      = obj.screen.newImage( nil,                            0,                        0, obj.labelHeight, obj.labelHeight )
    obj.elements.cardHead.topRightBG_collapsed     = obj.screen.newImage( nil, obj.width - obj.labelHeight,                        0, obj.labelHeight, obj.labelHeight )
    obj.elements.cardHead.bottomFrame_collapsed    = obj.screen.newRectangle( obj.labelHeight, obj.labelHeight - obj.borderThickness, obj.width - obj.labelHeight*2, obj.borderThickness)
    obj.elements.cardHead.topLeftBG_collapsed.setParent( obj.head )
    obj.elements.cardHead.topRightBG_collapsed.setParent( obj.head )
    obj.elements.cardHead.bottomFrame_collapsed.setParent( obj.head )
    
    obj.events.collapse = EventChannel:new{}

    obj.elements.cardHead.collapseButton = CycleButton:new{
      screen = obj.screen,
      parent = obj.group,
      x = obj.elements.cardHead.remove.getX() - 8,
      y = obj.elements.cardHead.remove.getY(),
      width = 6,
      height = 6,
      optionMode = "images",
      options = {
        "resource:whitedowntri.png",
        "resource:blackdowntri.png"
      }, onChange = function( button, element, index )
        obj:setCollapsed( index == 2 )
      end,
    }
  end

  obj.events.enableChanged = EventChannel:new{}
  obj.elements.cardHead.enableButton = EnableToggle:new{
    screen = obj.screen,
    parent = obj.group,
    x = obj.radius / 2 ,
    y = obj.labelHeight / 2 - 4,
    width = 8,
    height = 8,
    enabled = args.enabled or not args.enableable,
    parentEnabled = args.parentEnabled,
    onToggle = function(button, state) obj:_onEnableChaged() end
  }
  obj.elements.cardHead.enableButton:setVisible( args.enableable )

  obj:updateTiles()

  if args.enableContext then
    obj:buildContextMenu()
  end

  if self == Card then
    obj:_postConstruct()
  end
  return obj
end

function Card:setCollapsed( collapse )
  self.collapsed = collapse
  self.events.collapse:notify( self, collapse )
  self:updateTiles()
  self.events.resize:notify( self, self.width, self:getHeight() )
end

function Card:onCardClick( x, y, button, element )
  if button == misc.RMB and self.menu then
    self.menu:open( self.screen.getMousePos() )
  end
end

function Card:resizeCard()
  --obj.elements.cardHead.topLeftBG --no change, ever
  local x, y = self:getPos()
  self.group.setPos(0,0)
  self.elements.cardHead.topRightBG.setPos(self.width - self.radius,  0)
  self.elements.cardHead.topRightBG.setSize(self.radius, self.radius)

  self.elements.cardBody.bottomLeftBG.setPos(  0, self.height - self.radius )
  self.elements.cardBody.bottomLeftBG.setSize( self.radius, self.radius )

  self.elements.cardBody.bottomRightBG.setPos(  self.width - self.radius, self.height - self.radius )
  self.elements.cardBody.bottomRightBG.setSize( self.radius, self.radius )

  self.elements.cardHead.topFrame.setPos( self.radius, 0 )
  self.elements.cardHead.topFrame.setSize( self.width - self.radius*2, self.borderThickness )

  self.elements.cardHead.top.setPos( self.radius, self.borderThickness )
  self.elements.cardHead.top.setSize( self.width - self.radius*2, self.radius - self.borderThickness )

  self.elements.cardBody.middle.setPos( self.borderThickness, self.radius )
  self.elements.cardBody.middle.setSize( self.width - self.borderThickness*2, self.height - self.radius*2)

  self.elements.cardBody.leftFrame.setPos( 0, self.radius )
  self.elements.cardBody.leftFrame.setSize( self.borderThickness, self.height - self.radius * 2 )

  self.elements.cardBody.rightFrame.setPos( self.width - self.borderThickness, self.radius )
  self.elements.cardBody.rightFrame.setSize( self.borderThickness, self.height - self.radius * 2 )

  self.elements.cardBody.bottom.setPos( self.radius, self.height - self.radius )
  self.elements.cardBody.bottom.setSize( self.width - self.radius * 2, self.radius - self.borderThickness )

  self.elements.cardBody.bottomFrame.setPos( self.radius, self.height - self.borderThickness )
  self.elements.cardBody.bottomFrame.setSize( self.width - self.radius*2, self.borderThickness )

  self.elements.cardBody.divider.setPos( self.borderThickness, self.labelHeight )
  self.elements.cardBody.divider.setSize( self.width - self.borderThickness*2, self.borderThickness )

  self.elements.cardHead.label.setPos( self.radius / 2 + 10, self.labelHeight - 12 )


  self.elements.cardHead.remove.setPos( self.width - self.radius, self.labelHeight / 2 - 3 )
  
  if self.isCollapsable then
    -- self.elements.cardHead.topLeftBG_collapsed.setPos(    0,                        0 ) -- no change
    self.elements.cardHead.topLeftBG_collapsed.setSize(  self.labelHeight, self.labelHeight )

    self.elements.cardHead.topRightBG_collapsed.setPos(  self.width - self.labelHeight,                        0 )
    self.elements.cardHead.topRightBG_collapsed.setSize(  self.labelHeight, self.labelHeight )

    self.elements.cardHead.bottomFrame_collapsed.setPos( self.labelHeight, self.labelHeight - self.borderThickness)
    self.elements.cardHead.bottomFrame_collapsed.setSize(  self.width - self.labelHeight*2, self.borderThickness)


    self.elements.cardHead.collapseButton:setPos(
      self.elements.cardHead.remove.getX() - 8,
      self.elements.cardHead.remove.getY()
    )
  end
  self.group.setPos(x,y)
  self.events.resize:notify( self, self:getWidth(), self:getHeight() )
end

function Card:setColor( color )
  self.enabledColor = color
  self:updateTiles()
end

function Card:getColor()
  return self.enabledColor
end

function Card:updateTiles()
    local scale = misc.resScale( self.screen )
    local pixelRadius = self.radius * scale
    local pixelRadius2 = self.labelHeight * scale
    local boarder = self.borderThickness * scale

    self.body.setVisible( not self.collapsed )
    
    self.color = self:isEnabled() and self.enabledColor or Card.static.disabledColor

    local frameColor = misc.trimColor( self.color )

    self.tiles.topLeft = generateCornerTile( pixelRadius, pixelRadius, pixelRadius, self.color, frameColor, boarder+1, self.useColorCache )
    self.tiles.topRight = generateCornerTile( 1, pixelRadius, pixelRadius, self.color, frameColor, boarder+1, self.useColorCache )
    self.tiles.bottomLeft = generateCornerTile( pixelRadius, 1,  pixelRadius, self.color, frameColor, boarder+1, self.useColorCache )
    self.tiles.bottomRight = generateCornerTile( 1, 1,  pixelRadius, self.color, frameColor, boarder+1, self.useColorCache )
    self.tiles.collapseLeft = generateCollapsedTile( true, pixelRadius2, self.color, frameColor, boarder+2, self.useColorCache )
    self.tiles.collapseRight = generateCollapsedTile( false, pixelRadius2, self.color, frameColor, boarder+2, self.useColorCache )
    
    self.elements.cardHead.topLeftBG.setImage( self.tiles.topLeft )    
    self.elements.cardHead.topRightBG.setImage( self.tiles.topRight )   
    
    if self.isCollapsable then
      self.elements.cardHead.topLeftBG_collapsed.setImage( self.tiles.collapseLeft )
      self.elements.cardHead.topRightBG_collapsed.setImage( self.tiles.collapseRight )

      self.elements.cardHead.bottomFrame_collapsed.setColor( frameColor )
      
      self.elements.cardHead.topLeftBG_collapsed.setVisible( self.collapsed )
      self.elements.cardHead.topRightBG_collapsed.setVisible( self.collapsed )
      self.elements.cardHead.bottomFrame_collapsed.setVisible( self.collapsed )

      self.elements.cardHead.top.setHeight( (self.collapsed and self.labelHeight or self.radius) - self.borderThickness)
    end

    self.elements.cardBody.bottomLeftBG.setImage( self.tiles.bottomLeft ) 
    self.elements.cardBody.bottomRightBG.setImage( self.tiles.bottomRight )

    self.elements.cardHead.topFrame.setColor( frameColor )
    self.elements.cardHead.top.setColor( self.color )
    self.elements.cardBody.middle.setColor( self.color )
    self.elements.cardBody.leftFrame.setColor( frameColor )
    self.elements.cardBody.rightFrame.setColor( frameColor )
    self.elements.cardBody.bottom.setColor( self.color )
    self.elements.cardBody.bottomFrame.setColor( frameColor )
    

    self.elements.cardBody.divider.setColor({1,1,1,1})

    self.elements.cardHead.topLeftBG.setVisible( not self.collapsed )
    self.elements.cardHead.topRightBG.setVisible( not self.collapsed )
    

end

function Card:getHeight()
  return self.collapsed and self.labelHeight or self.height
end

function Card:getBodyHeight()
  return self.height - self.labelHeight
end

function Card:getLabelHeight()
  return self.labelHeight
end

function Card:getBodyX()
  return self.body.getX()
end

function Card:getBodyY()
  return self.body.getY()
end

function Card:getBodyPos()
  return self:getBodyX(), self:getBodyY()
end

function Card:setWidth( newWidth, _notify )
  self.width = newWidth
  if _notify ~= false then
    self:resizeCard()
  end
end

function Card:setHeight( newHeight, _notify )
  self.height = newHeight
  if _notify ~= false then
    self:resizeCard()
  end
end

function Card:setSize( width, height )
  self:setWidth( width, false )
  self:setHeight( height, false )
  self:resizeCard()
end

function Card:setEnabled( state )
  assert(type(state)=="boolean", "boolean expected")
  self.elements.cardHead.enableButton:setEnabled( state )
end

function Card:setParentEnabled( state )
  assert(type(state)=="boolean", "boolean expected")
  self.elements.cardHead.enableButton:setParentEnabled( state )
end

function Card:isEnabled()
  return self.elements.cardHead.enableButton:isEnabled()
end

function Card:_onEnableChaged()
  self.events.enableChanged:notify( self, self:isEnabled() )
  self:updateTiles()
end

function Card:getLabel()
  return self.elements.cardHead.label.getText()
end

function Card:getContextMenuLayout()
  return {
    {
      label = "Color...",
      onClick = function() self:promptColorChange() end
    },{
      label = "Rename...",
      onClick = function() self:rename() end
    }
  }
end

function Card:promptColorChange()
  local ColorPicker = require"ui/ColorPicker"
  local picker = ColorPicker:new{
    parentGui = self.screen,
    color = self.enabledColor,
    onSelect = function(picker, color)
      picker:close()
      self:setColor( color )
    end
  }
  picker:open()
end

function Card:rename( name )
  if not name then
    require("ui/TextPrompt"):new({
      screen = self.screen,
      prompt = "Enter new name",
      default = self:getLabel(),
      callback = function( result ) self:rename( result ) end
    }):open()
    return
  end
  self.elements.cardHead.label.setText( name )
end
Card.setLabel = Card.rename --alias

function Card:onContextMenuSelect( contextItem )
  toast("TODO", contextItem:getLabel())
end

function Card:buildContextMenu()
  self.menuLayout = self:getContextMenuLayout()
  self.menu = ContextMenu:new{
    parent = self.screen,
    layout = self.menuLayout,
    onSelect = function( contextItem ) 
      self:onContextMenuSelect( contextItem )
    end,
  }
end

return Card