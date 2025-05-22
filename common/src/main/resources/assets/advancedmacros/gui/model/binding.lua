local utils = advancedMacros.utils

local File = require"File"

local Binding = newClass"model/Binding"

Binding.static = {
  triggerModes = {
    KEY_DOWN = "key down",
    KEY_UP   = "key up",
    KEY_ALL  = "key all",
    EVENT    = "event",
   },
   scriptModes = {
    FILE = "file",
    LUA = "lua"
   }
}

function Binding:new( ... )
  local obj = Binding._new( self )
  local args = utils.kwargs({
    { enabled = "boolean", true },
    { triggerMode = "string", Binding.static.triggerModes.KEY_DOWN },
    { triggerName = {"string", "nil", "boolean"}, nil, "triggerValue" },
    { scriptMode = "string", "file" }, --"file" / "snippet"
    { scriptValue = {"string", "nil", "boolean", "table"} },
    { label = "string", "Binding" },
    { color = {"nil", "number","table"} },
  },...)
  
  
  obj.enabled = args.enabled
  obj.label = args.label
  
  obj:setTriggerMode(args.triggerMode)
  obj.triggerName = args.triggerName
  
  obj:setScriptMode( args.scriptMode )
  if type(args.scriptValue) == "table" and not isClass(args.scriptValue) then
    obj.scriptValue = File:new(args.scriptValue)
  else
    obj.scriptValue = args.scriptValue
  end

  obj.color = args.color

  return obj
end

function Binding:setTriggerMode( mode )
  assert(utils.inTable(mode, Binding.static.triggerModes), "Invalid mode `"..tostring(mode).."`")
  self.triggerMode = mode
  self.triggerName = nil
end

function Binding:setScriptMode( mode )
  assert(utils.inTable(mode, Binding.static.scriptModes), "Invalid mode `"..tostring(mode).."`")
  self.scriptMode = mode
  self.scriptValue = nil
end

function Binding:toJson()
  local json = require("JsonObject"):new()
  
  --names match kwargs for Binding:new(...)

  json:put("enabled",     self.enabled                      )
  json:put("triggerMode", self.triggerMode                  )
  json:put("triggerName", self.triggerName                  )
  json:put("scriptMode",  self.scriptMode                   )
  if isClass(self.scriptValue) then --file
    json:put("scriptValue", self.scriptValue:toJson()       )
  else
    json:put("scriptValue", self.scriptValue                )
  end
  json:put("label",       self.label                        )
  json:put("color",       utils.colorToJsonValue(self.color) )
  json:put("className",   self:className()                  )
  return json
end

return Binding