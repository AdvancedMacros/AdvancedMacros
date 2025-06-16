local utils = advancedMacros.utils
local Element = require"ui/Element"
local Flow = newClass("ui/layout/Flow", Element)

function Flow:new( ... )
  local obj = Flow._new( self, ... )
  
  local args = utils.kwargs({
    {hAlign = "string", "center", "horizontalAlignment"}, --left center right
    {vAlign = "string", "top", "verticalAlignment"}, --top center bottom
    {hGap = "number", 1, "hgap","horizontalGap","horizontalSpacing","hSpacing"},
    {vGap = "number", 1, "vgap","verticalGap", "verticalSpacing","vSpacing"},
    {hbox = "boolean", false, "vbox", "hBox", "vBox"},
    {debug = "boolean", true, "showBounds"},
  }, ...)

  obj.vAlign = args.vAlign
  obj.hAlign = args.hAlign
  obj.hGap = args.hGap
  obj.vGap = args.vGap
  obj.elements.debug = {
    bounds = obj.screen.newBox(0,0,obj.width,obj.height, 1)
  }
  obj.elements.debug.bounds.setVisible( args.debug )
  obj.elements.debug.bounds.setColor( 0x40FFFFFF)
  obj.elements.debug.bounds.setZ( 1 )
  obj.elements.debug.bounds.setParent(obj.group)
  obj.children = {}
  
  if args.hbox then
    obj.mode = args("hbox"):lower() --kwargs name used
  end

  if self == Flow then
    obj:_postConstruct()
  end
  
  return obj
end

function Flow:add( element )
  table.insert(self.children, element)
  if isClass( element ) then
    element.events.resize:addListener( function() self:arrange() end )
    element:setParent( self.group )
  else
    element.setParent( self.group )
  end
  self:arrange()
end

function Flow:onResize( width, height )
  Flow:super().onResize( self, width, height )
  self.elements.debug.bounds.setSize( width, height )
  self:arrange()
end

function Flow:remove( element )
  for i, child in ipairs(self.children) do
    if child == element then
      table.remove(self.children, i)
      self:arrange()
      return
    end
  end
end

function Flow:_allignRow( start, stop, width, height )
  --log("&aAlign: &f&Nstart&f, &Nstop&f, &Nwidth&f, &Nheight", start, stop, width, height)
  local dw = self.width - width
  local dx = 0
  if self.hAlign == "center" then
    dx = dw /2
  elseif self.hAlign == "right" then
    dx = dw
  elseif self.hAlign ~= "left" then
    error("Invalid horizontal alignment "..tostring(self.vAlign))
  end

  for i = start, stop do
    local element = self.children[ i ]
    local dh = height - element:getHeight()
    local dy = 0
    if self.vAlign == "center" then
      dy = dh /2
    elseif self.vAlign == "bottom" then
      dy = dh
    elseif self.vAlign ~= "top" then
      error("Invalid vertical alignment "..tostring(self.vAlign))
    end
    local ex, ey = element:getPos()
    if isClass( element ) then
      element:setPos( ex + dx, ey + dy )
    else
      element.setPos( ex + dx, ey + dy )
    end
  end
end

function Flow:arrange()
  local rowWidth, rowHeight = 0, 0
  local itemsInRow = 0
  local X, Y = self.group.getPos()
  local x, y = X, Y
  local WIDTH = self.width
  local mode = self.mode --nil, "hbox" or "vbox"
  local maxRowWidth = 0
  -- log("&eArrange with mode: ", mode)
  if self.scrollbar and self.scrollbar.isVisible() then
    WIDTH = WIDTH - self.scrollbar.getWidth()             --TODO content size?
  end

  for index, element in ipairs( self.children ) do
    local eWidth = element:getWidth()
    if itemsInRow > 0 then
      if mode == "vbox" or (x + eWidth > WIDTH and mode ~= "hbox") then
        --apply allignment to row excluding this element
        self:_allignRow( index-itemsInRow, index-1, rowWidth - self.hGap, rowHeight )
        x, y = X, y + rowHeight + self.vGap
        maxRowWidth = math.max( maxRowWidth, rowWidth - self.hGap )
        rowWidth, rowHeight = 0, 0
        itemsInRow = 0
      end
    end
    rowWidth = rowWidth + self.hGap
    if isClass( element ) then
      element:setPos( x, y )
    else
      element.setPos( x, y )
    end
    rowWidth = rowWidth + eWidth
    x = x + eWidth + self.hGap
    rowHeight = math.max( rowHeight, element:getHeight() )
    itemsInRow = itemsInRow + 1
  end

  local widthChanged = false
  local reArrange = false
  maxRowWidth = math.max( maxRowWidth, rowWidth - self.hGap )
  if mode == "hbox" then
    self.width = rowWidth
    self.elements.debug.bounds.setWidth( self.width )
    widthChanged = self.width ~= rowWidth
  elseif mode == "vbox" then
    reArrange = self.width ~= maxRowWidth
    self.width = maxRowWidth
    self.elements.debug.bounds.setWidth( self.width )
    widthChanged = reArrange
  end
  self:_allignRow( #self.children-itemsInRow+1, #self.children, rowWidth - self.hGap, rowHeight )
  local newHeight = y + rowHeight - Y

  if newHeight ~= self.height or widthChanged then
    self.height = newHeight
    self.elements.debug.bounds.setHeight( self.height )
    self:notifyResize()
  end

  if reArrange then
    self:arrange()
  end
end

return Flow