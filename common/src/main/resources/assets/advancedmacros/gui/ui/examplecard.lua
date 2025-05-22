--for color picker, this is just a card with the color option removed
local utils = advancedMacros.utils

local Card = require"ui/Card"
local ExampleCard = newClass("ui/ExampleCard", Card)

function ExampleCard:new( ... )
  local obj = ExampleCard._new( self, ... )
  
  if self == ExampleCard then
    obj:_postConstruct()
  end

  return obj
end

--@Override
function ExampleCard:getContextMenuLayout()
  local layout =  ExampleCard:super().getContextMenuLayout( self )
  return utils.filter(layout, function(k,v)
    return v.label ~= "Color..."
  end)
end

return ExampleCard