local utils = advancedMacros.utils

local EventChannel = require"ui/EventChannel"
local ListView = require"ui/layout/ListView"
local FramedRectangle = require"ui/FramedRectangle"

local Element = require"ui/Element"
local ComboBox = newClass("ui/ComboBox", Element)

function ComboBox:new( ... )
  local obj = ComboBox._new( self, ... )
  
  local args = utils.kwargs({
    { popupMaxHeight = {"nil", "number"} },
    { frameThickness = "number", 1 },
    { frameColor = {"number", "table"}, 0xFFFFFFFF },
    { backgroundColor = {"number", "table"}, 0xFF000000 },
    { cellClass = "table" },
    { cellArgs = {"nil", "table"} },
    { viewTransformer = {"nil", "function"}, nil, "cellTransformer" }, --function(model) return copyOfModel end
  }, ...)

  obj.popupMaxHeight = args.popupMaxHeight
  obj.focusScreen = gui.new()
  obj.focusScreen.setParentGui( obj.screen )
  obj.cellClass = args.cellClass
  obj.viewTransformer = args.viewTransformer

  obj.events.optionUnselected = EventChannel:new{}
  obj.events.optionSelected = EventChannel:new{}

  obj.elements.widget = {}
  obj.elements.widget.box = FramedRectangle:new{
    screen = obj.screen,
    x = 0,
    y = 0,
    width = obj.width,
    height = obj.height,
    frameThickness = args.frameThickness,
    backgroundColor = args.backgroundColor,
    frameColor = args.frameColor,
    clipping = true,
  }
  local interiorWidth, interiorHeight = obj.elements.widget.box:getInteriorSize()
  
  obj.cellArgs = {
    screen = obj.screen,
    x = args.frameThickness,
    y = args.frameThickness,
    width = interiorWidth,
    height = interiorHeight,
    hoverTint = 0xFFFFFFFF,
  }
  for k,v in pairs( args.cellArgs ) do
    obj.cellArgs[k] = v
  end
  obj.elements.widget.selected = obj.cellClass:new( obj.cellArgs )
  local buttonSize = interiorHeight
  obj.elements.widget.triangleButton = obj.screen.newImage( "resource:whitedowntri.png", obj.width - args.frameThickness - buttonSize, args.frameThickness, buttonSize, buttonSize ) 

  local screenWidth, screenHeight = obj.screen.getSize()
  obj.elements.popup = {}
  obj.elements.popupGroup = obj.focusScreen.newGroup( obj.x, obj.y + obj.height )
  obj.elements.popup.listView = ListView:new{
    screen = obj.focusScreen,
    x = 0,
    y = 0,
    width = obj.width,
    height = obj.popupMaxHeight or (screenHeight / 3 - 4),
    cellClass = obj.cellClass,
    cellArgs = obj.cellArgs,
  }

  obj.elements.widget.box:setParent( obj.group )
  obj.elements.widget.selected:setParent( obj.elements.widget.box.interiorGroup )
  obj.elements.widget.triangleButton.setParent( obj.group )

  obj.elements.popup.listView:setParent( obj.elements.popupGroup )

  obj.elements.widget.box:setHoverTint( 0x44FFFFFF )
  obj.elements.widget.triangleButton.setHoverTint( 0x44FFFFFF )

  obj.elements.widget.box:setOnMouseClick(function(x,y,b) 
    if b == utils.LMB then 
      obj:open() 
      return true
    elseif b == utils.RMB then
      obj.events.rmb:notify(obj, x, y)
    end
  end)
  obj.elements.popup.listView.events.cellClicked:addListener(function(x,y,b,model) obj:setSelection( model ) end)
  obj.focusScreen.setOnMouseClick(function(x,y,b) obj:close() end)

  if self == ComboBox then
    obj:_postConstruct()
  end

  return obj
end

function ComboBox:setOptions( options )
  if self.selected and not utils.inTable( self.selected, options ) then
    self:setSelection( nil )
  end
  self.elements.popup.listView:setData( options )
end

function ComboBox:getOptions()
  return self.elements.popup.listView:getData()
end

function ComboBox:setSelectionByIndex( index )
  self:setSelection( self.elements.widget.listView.data[ index ] )
end

function ComboBox:setSelection( model )
  if self.selection then
    self.events.optionUnselected:notify( self, self.selection )
  end
  local previous = self.selection
  self.selection = model
  self.events.optionSelected:notify( self, model, previous )
  local modified = self.viewTransformer and model ~= nil and self.viewTransformer( model ) or model
  self.elements.widget.selected:applyModel( modified )
  if self:isOpen() then
    self:close()
  end
end

function ComboBox:getSelection()
  return self.selection
end

function ComboBox:updateOptionsLayout()
  local screenWidth, screenHeight = self.focusScreen.getSize()
  local listItemsHeight = self.elements.popup.listView:getListHeight()
  local maxHeight = self.popupMaxHeight or math.huge
  local widgetX, widgetY = self.group.getPos()
  local spaceAbove = widgetY
  local spaceBelow = screenHeight - widgetY - self.height
  local prefHeight = screenHeight / 3 
  
  local listHeight = math.min( maxHeight, listItemsHeight )

  self.elements.popupGroup.setX( self:getX() )
  self.elements.popup.listView:setWidth( self.width )
  if spaceBelow < prefHeight and listHeight >= prefHeight then --near bottom of screen and won't fit prefHeight 
    self.elements.popup.listView:setHeight( math.min(spaceAbove-4, listHeight) )
    self.elements.popupGroup.setY( widgetY - self.elements.popup.listView:getHeight() )
  else
    self.elements.popup.listView:setHeight( math.min(spaceBelow-4, listHeight ) )
    self.elements.popupGroup.setY( widgetY + self.height )
  end
end

function ComboBox:open()
  self:updateOptionsLayout()
  self.focusScreen.open()
end

function ComboBox:close()
  self.screen.open() --parent
end

function ComboBox:isOpen()
  return self.focusScreen.isOpen()
end

return ComboBox