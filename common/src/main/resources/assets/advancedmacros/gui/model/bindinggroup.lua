local utils = advancedMacros.utils
local BindingGroup = newClass"model/BindingGroup"

function BindingGroup:new( ... )
  local obj = BindingGroup._new( self, ... )
  
  local args = utils.kwargs({
    { color = {"nil", "number", "table"} },
    { label = "string" },
    { enabled = "boolean", true },
    { children = "table"},
  }, ...)

  obj.color = args.color
  obj.label = args.label
  obj.enabled = args.enabled
  obj.children = args.children
    
  return obj
end


function BindingGroup:toJson()
  local json = require("JsonObject"):new()
  local array = require("JsonArray"):new()

  json:put("label",     self.label                        )
  json:put("color",     utils.colorToJsonValue(self.color) )
  json:put("enabled",   self.enabled                      )
  json:put("children",  array                             )
  json:put("className", self:className()                  )

  for i, child in ipairs(self.children) do
    array:put( child:toJson() )
  end

  return json
end


return BindingGroup