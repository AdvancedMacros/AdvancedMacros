local utils = advancedMacros.utils

local ViewCell = require"ui/layout/ViewCell"
local StringListCell = newClass("ui/cells/StringListCell", ViewCell)

function StringListCell:new( ... )
  local obj = StringListCell._new( self, ... )

  local args = utils.kwargs({
    { hoverTint = {"nil","number","table"} },
    { prefix = {"nil","string"} },
    { margin = "number", 2}
  }, ...)

  obj.prefix = args.prefix
  obj.elements.background = obj.screen.newRectangle( 0, 0, obj.width, obj.height)
  obj.elements.text = obj.screen.newText("", args.margin, 0, obj.height)
  
  obj.elements.background.setColor( 0 )

  obj.elements.background.setParent( obj.group )
  obj.elements.text.setParent( obj.group )
  obj.elements.text.setZ(1)

  obj.group.setScissor( obj.width, obj.height )

  if args.hoverTint then
    obj.elements.background.setHoverTint( args.hoverTint )
  end
  
  obj.elements.background.setOnMouseClick(function(x,y,b) obj.events.mouseClicked:notify(x,y,b, obj.model) return true end)

  return obj
end

function StringListCell:onModelChange()
  local text = self.model.text
  local bgColor = self.model.backgroundColor
  self.elements.text.setText( (self.prefix or "") .. (self.model.prefix or "").. text )
  self.elements.background.setColor( bgColor or 0 )
end

function StringListCell:setWidth( width, ... )
  self.elements.background.setWidth( width )
  self.group.setScissor( self.width, self.height )
  StringListCell:super().setWidth( self, width, ... )
end

return StringListCell