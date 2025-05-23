local utils = advancedMacros.utils

--managages multiple event listeners
local EventChannel = newClass"ui/EventChannel"

function EventChannel:new( ... )
  local obj = EventChannel._new( self, ... )
  
  local args = utils.kwargs({
    {consumable = "boolean", false},
  }, ...)

  obj.consumable = args.consumable
  obj.listeners = {}
  obj.owners = setmetatable({}, {__mode="v"})
  
  return obj
end

function EventChannel:addListener( listener, owner )
  table.insert( self.listeners, listener )
  self.owners[listener] = owner or true
end

function EventChannel:removeListener( listener )
  table.remove( self.listeners, listener )
end

function EventChannel:cleanup()
  local i = 1
  repeat
    local listener = self.listeners[ i ]
    if self.owners[ listener ] then
      i = i+1
    else
      table.remove( self.listeners, listener )
    end
  until not self.listeners[ i ]
end

function EventChannel:notify( ... )
  self:cleanup()
  for i, listener in ipairs( self.listeners ) do
    local ok, result = pcall( listener, ... )
    if not ok then 
      log(result)
    end
    if self.consumable and result ~= nil then
      return result
    end
  end
end

return EventChannel