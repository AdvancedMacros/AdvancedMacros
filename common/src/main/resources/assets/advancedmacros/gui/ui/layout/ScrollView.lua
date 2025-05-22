--doesn't use recyclable cells
local utils = advancedMacros.utils

local Element = require"ui/Element"
local ScrollView = newClass("ui/layout/ScrollView", Element)

ScrollView.static = {
  scrollModes = {
    SHOW = "show",
    HIDE = "hide",
    AUTO = "auto",
  }
}

function ScrollView:new( ... )
  local obj = ScrollView._new( self, ... )
  
  local args = utils.kwargs({
    { backgroundColor = {"number","table"}, 0x44000000, "background", "bg" },
    { frameThickness = "number", 0, "frame" },
    { horizontalScrollMode = "string", ScrollView.static.scrollModes.AUTO, "hBarMode", "hBar", "horizontal" },
    { verticalScrollMode   = "string", ScrollView.static.scrollModes.AUTO, "vBarMode", "vBar", "vertical" },
  }, ...)

  obj.frameThickness = args.frameThickness
  obj.contentGroup = obj.screen.newGroup()
  obj.contentWidth = obj.width
  obj.contentHeight = obj.height
  
  obj.elements.background = obj.screen.newRectangle(args.frameThickness, args.frameThickness,obj.width-args.frameThickness*2,obj.height-args.frameThickness*2)
  obj.elements.frame = obj.screen.newBox(0,0,obj.width,obj.height, args.frameThickness)
  
  local barLengthOffset = -1
  
  obj.elements.vScrollBar = obj.screen.newScrollBar(obj.width - 8, 0, 7, obj.height + barLengthOffset)
  obj.elements.hScrollBar = obj.screen.newScrollBar(0, obj.height-8, obj.width + barLengthOffset, 7, "h") --"h" also works
  obj.elements.bothBlock = obj.screen.newRectangle(obj.width + barLengthOffset, obj.height + barLengthOffset, -barLengthOffset, -barLengthOffset)
  
  obj.elements.background.setColor( args.backgroundColor )
  obj.elements.frame.setColor(0xFFFFFFFF)
  obj.elements.bothBlock.setColor( {100/255, 100/255, 1})
  
  obj.elements.background.setParent( obj.group )
  obj.elements.frame.setParent( obj.group )
  obj.elements.bothBlock.setParent( obj.group )
  obj.elements.vScrollBar.setParent( obj.group )
  obj.elements.hScrollBar.setParent( obj.group )
  obj.contentGroup.setParent( obj.group )
  
  obj.children = {}

  obj.events.resize:addListener(obj.onResize)
  obj.elements.vScrollBar.setOnMouseDrag(function() obj:onScroll() end)
  obj.elements.hScrollBar.setOnMouseDrag(function() obj:onScroll() end)
  --TODO scroll wheel event on bg with shift check

  obj:updateContentBounds()
  obj:onResize()

  if self == ScrollView then
    obj:_postConstruct()
  end

  return obj
end

function ScrollView:add( child )
  table.insert( self.children, child )
  if isClass( child ) then
    child:setParent( self.contentGroup )
  else
    child.setParent( self.contentGroup )
  end
  if child.events and child.events.resize then
    child.events.resize:addListener(function() self:updateContentBounds() end, self)
  end
end

function ScrollView:remove( child )
  table.remove( self.children, child )
end

function ScrollView:updateContentBounds()
  local minX, minY, maxX, maxY = 0, 0, self.width, self.height
  local contentX, contentY = self.contentGroup.getPos()
  for index, child in ipairs( self.children ) do
    local cx, cy = child:getPos()
    local cw, ch = child:getSize()
    cw, ch = cw or 0, ch or 0
    
    minX, minY = math.min(minX, cx - contentX), math.min(minY, cy - contentY)
    maxX, maxY = math.max(maxX, cx - contentX + cw), math.max(maxY, cy - contentY + ch)
  end
  self.contentWidth = maxX - minX
  self.contentHeight = maxY - minY

  local hVis, vVis
  if self.horizontalScrollMode == ScrollView.static.scrollModes.HIDE then
    hVis = false
  elseif self.horizontalScrollMode == ScrollView.static.scrollModes.SHOW then
    hVis = true
  else
    hVis = self.contentWidth > self.width
  end

  if self.verticalScrollMode == ScrollView.static.scrollModes.HIDE then
    vVis = false
  elseif self.verticalScrollMode == ScrollView.static.scrollModes.SHOW then
    vVis = true
  else
    vVis = self.contentHeight > self.height
  end

  self.viewportWidth = self.width - (vVis and 8 or 0)
  self.viewportHeight = self.height - (hVis and 8 or 0)
  local lenOffset = hVis and vVis and -8 or -1
  local widOffset = hVis and vVis and -8 or 0 --AM bug related

  self.elements.hScrollBar.setWidth( self.width + widOffset )
  self.elements.vScrollBar.setHeight( self.height + lenOffset )
  self.elements.bothBlock.setVisible( hVis and vVis )
  
  self.elements.hScrollBar.setMaxItems( self.contentWidth )
  self.elements.hScrollBar.setVisibleItems( self.viewportWidth )
  self.elements.vScrollBar.setMaxItems( self.contentHeight )
  self.elements.vScrollBar.setVisibleItems( self.viewportHeight )

  self.elements.hScrollBar.setVisible( hVis )
  self.elements.vScrollBar.setVisible( vVis )
end

function ScrollView:onScroll()
  local hScroll = self.elements.hScrollBar.getScrollPos()
  local vScroll = self.elements.vScrollBar.getScrollPos()
  local x, y = self:getPos()
  self.contentGroup.setPos( x - hScroll, y - vScroll )
end

function ScrollView:getViewportWidth()
  return self.viewportWidth
end

function ScrollView:getViewPortHeight()
  return self.viewportHeight  
end

function ScrollView:getViewportSize()
  return self:getViewPortWidth(), self:getViewportHeight()
end

function ScrollView:getContentWidth()
  return self.contentWidth
end

function ScrollView:getContentHeight()
  return self.contentHeight
end

function ScrollView:getContentSize()
  return self:getContentWidth(), self:getContentHeight()
  
end

function ScrollView:onResize(w, h)
  --update clipping area
  self.group.setScissor(0,0,self.width, self.height)

  self.elements.background.setWidth(self.width - self.frameThickness*2)
  self.elements.frame.setWidth(self.width)
  self.elements.vScrollBar.setX( self:getY() + self.width - 8 )
  self.elements.hScrollBar.setWidth( self.width - 1 )

  self.elements.background.setHeight(self.height - self.frameThickness*2)
  self.elements.frame.setHeight(self.height)
  self.elements.hScrollBar.setY(self:getY() + self.height - 8)
  self.elements.vScrollBar.setHeight(self.height - 1)
end

return ScrollView