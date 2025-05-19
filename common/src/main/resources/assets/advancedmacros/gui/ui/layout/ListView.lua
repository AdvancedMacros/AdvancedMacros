local utils = advancedMacros.utils

local EventChannel = require"ui/EventChannel"

local Element = require"ui/Element"
local ListView = newClass("ui/layout/ListView", Element)

function ListView:new( ... )
  local obj = ListView._new( self, ... )
  
  local args = utils.kwargs({
    { cellClass = "table" },
    { cellArgs = {"nil", "table"} },
    { rowHeight = "number", 10 },
    { gap = "number", 2, "vgap", "vGap" },
  }, ...)

  obj.cellClass = args.cellClass
  obj.rowHeight = args.rowHeight
  obj.gap = args.gap
  obj.cells = {}
  obj.data = {}
  obj.cellArgs = args.cellArgs
  obj.listHeight = 0

  obj.events.cellClicked = EventChannel:new{}

  obj.elements.listView = {
    background = obj.screen.newRectangle( 0, 0, obj.width - 6, obj.height ),
    scrollbar = obj.screen.newScrollBar( obj.width - 6, 0, 5, obj.height-1 )
  }
  
  obj.elements.listView.scrollbar.setMaxItems( obj.height )
  obj.elements.listView.scrollbar.setVisibleItems( obj.height )
  obj.elements.listView.background.setColor( 0x44000000 )

  obj.elements.listView.background.setParent( obj.group )
  obj.elements.listView.scrollbar.setParent( obj.group )

  obj.elements.listView.background.setOnScroll( function(d)
    obj.elements.listView.scrollbar.scroll( d )
    obj:updateCells() 
  end)
  obj.elements.listView.scrollbar.setOnScroll(function(d)
    obj.elements.listView.scrollbar.scroll( d )
    obj:updateCells()
  end)
  obj.elements.listView.scrollbar.setOnMouseDrag(function() obj:updateCells() end)

  obj:setHeight( obj.height )
  
  if self == ListView then
    obj:_postConstruct()
  end

  return obj
end

function ListView:setData( data )
  self.data = data
  local n = #self.data
  local totalHeight = n * self.rowHeight + (n-1) * self.gap
  self.listHeight = totalHeight
  self.elements.listView.scrollbar.setMaxItems( totalHeight )
  self:updateCells()
end

function ListView:getData()
  return self.data
end

function ListView:getViewRange()
  local bar = self.elements.listView.scrollbar
  local pixelStart = bar.getScrollPos()
  -- local pixelEnd = pixelStart + bar.getVisibleItems() - 1 
  local itemStart = 1 + math.floor(pixelStart / (self.rowHeight + self.gap))
  return itemStart, itemStart + #self.cells - 1
end

function ListView:updateCells()
  local start, stop = self:getViewRange()
  local barPos = self.elements.listView.scrollbar.getScrollPos()
  for i = start, stop do
    local model = self.data[ i ]
    local cell = self.cells[ (i-1) % #self.cells + 1 ]
    if model then
      local fakeY = ( i-1 ) * (self.rowHeight + self.gap)
      cell:applyModel( model )
      cell:setY( self:getY() + fakeY - barPos )
      cell:setVisible( 
        (self:getY() <= cell:getY() + self.rowHeight) and 
        (cell:getY()) <= (self:getY() + self:getHeight() ))
    else
      cell:setVisible( false )
    end
  end
end

function ListView:setWidth( width, ... )
  for i, cell in ipairs( self.cells ) do
    cell:setWidth( width - 6 )
  end
  self.elements.listView.scrollbar.setX( self:getX() + width - 6 )
  self:updateCells()
  ListView:super().setWidth( self, width, ... )
end

function ListView:setHeight( height )
  local cellsNeeded = 2 + math.ceil( height / (self.rowHeight + self.gap) )
  for i = #self.cells, cellsNeeded + 1, -1 do
    self.cells[i]:setVisible( false )
  end
  local args = {
    screen = self.screen,
    parent = self.group,
    x = 0,
    y = 0,
    width = self.width - self.elements.listView.scrollbar.getWidth(),
    height = self.rowHeight,
  }
  if self.cellArgs then
    for k,v in pairs(self.cellArgs) do
      args[k] = v
    end
  end

  for i = #self.cells + 1, cellsNeeded do
    self.cells[ i ] = self.cellClass:new( args )
    self.cells[i]:setZ( 1 )
    self.cells[i].events.mouseClicked:addListener(function(x,y,b,model) self.events.cellClicked:notify(x,y,b,model) end)
  end
  for i = 1, cellsNeeded do
    self.cells[i]:setVisible( true )
  end
  self.elements.listView.scrollbar.setVisibleItems( height )
  self.elements.listView.scrollbar.setHeight( height )
  self.elements.listView.background.setHeight( height )
  self.group.setScissor( self.width, height )
  ListView:super().setHeight( self, height )
end

function ListView:getListHeight()
  return self.listHeight
end

return ListView