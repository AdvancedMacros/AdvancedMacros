local utils = advancedMacros.utils
local EventChannel = require"ui/EventChannel"

local Element = require"ui/Element"
local ViewCell = newClass("ui/layout/ViewCell", Element)

function ViewCell:new( ... )
  local obj = ViewCell._new( self, ... )
  
  obj.events.modelChanged = EventChannel:new{}
  obj.events.mouseClicked = EventChannel:new{}

  return obj
end

--"final"
function ViewCell:applyModel( model )
  -- if model == self.model then return end
  self.model = model
  if model == nil then
    self:setVisible( false )
  else
    self:setVisible( true )
    self:onModelChange()
  end
  self.events.modelChanged:notify( self, model )
end

--override this one
function ViewCell:onModelChange()
end

return ViewCell