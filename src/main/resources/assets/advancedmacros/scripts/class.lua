--inherit( className, baseClass )
--class inheritance function
--baseClass may be nil for creating a class with no parent
--constructor can be overriden by saving it to a local variable then
--calling it durring the call to the 'new' function
--this will instantiate all parent classes for this object
function newClass( className, baseClass )
  local class = {}
  local classMeta = { 
    __index = baseClass,
    __class = className 
  }
  setmetatable( class, classMeta )

  function class:new( ... )
    assert(self, "self can not be nil")
    local obj = self.__constructing and self or {__constructing = true}
    
    setmetatable( obj, getmetatable(obj) or {
      __index = class,
      __instance = true, --kept after construct 
      __class = className,
    } )

    if baseClass then
      baseClass.new(obj, ... ) -- <-__instance used because of this
    end
    obj.__constructing = nil --cleanup so :new works normal when done
    return obj
  end

  function class:class()
    return class
  end
  
  function class:__enableMetaEvents()
    local meta = getmetatable( self )
    local this = self
    for a,b in ipairs{
      "index", --special handler
      "newindex",
      "mode",
      "call",
      "metatable",
      "tostring",
      "len",
      "pairs",
      "ipairs",
      "gc",
      "name",
      -- "__close", 5.4
      "unm",
      "add",
      "sub",
      "mul",
      "div",
      -- "idiv", 5.3
      "mod",
      "pow",
      "concat",
      -- "band", 5.3
      -- "bor",
      -- "bxor",
      -- "bnot",
      -- "shl",
      -- "shr",
      "eq",
      "lt",
      "le"
    } do
      local name = "__"..b
      if b == "index" then
        meta.__index = function( t, k )
          local values = { class[ k ] }
          if #values > 0 then return table.unpack( values ) end
          local indexer = this.__index
          if type( indexer ) == "function" then
            return indexer( t, k )
          else
            return indexer[ k ]
          end
        end
      else
        meta[ name ] = self[ name ]
      end
    end
  end

  class.className = function() return classMeta.__class end
  
  function class:super()
    return baseClass
  end

  function class:isA( someClass )
    if not self then error("Self can not be nil", 2) end
    if not isClass(someClass) then error("Argument provided is not a class",2) end
    local current = class
    while current do
      if current == someClass then
        return true
      end
      current = current:super()
    end
    return false
  end

  function class:isInstance()
    return getmetatable( self ).__instance or false
  end

  return class
end

function isClass( x )
  local t = type(x)
  if t == "table" then
    local meta = getmetatable( x )
    if meta then
      if meta.__class then
        return true
      end
    end
  end
  return false
end


--Bar = require.....
-- local Foo = advancedMacros.class( "Foo", Bar )
--
-- local _new = Foo.new
-- function Foo:new()
--   local obj = _new( self )
--
--   return obj
-- end
--
-- return Foo