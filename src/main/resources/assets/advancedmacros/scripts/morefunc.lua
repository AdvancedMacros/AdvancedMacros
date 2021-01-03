local AdvancedMacros = luajava.bindClass "com.theincgi.advancedMacros.AdvancedMacros"
function getMinecraft() return AdvancedMacros:getMinecraft() end
function _env() return AdvancedMacros:_env() end

--sign( num )
--returns -1, 0 or 1
function math.sign(num)
  if num==0 then
    return 0
  elseif num>0 then
    return 1
  else
    return -1
  end
end


--map(x, iMin, iMax, oMin, oMax)
--maps x from input range to output range
function math.map(x, iMin, iMax, oMin, oMax)
  return (x - iMin) * (oMax - oMin) / (iMax - iMin) + oMin
end


--attempts to pick an item from your inventory
--if it exists it is picked from hotbar or moved to the prefered slot
--slot number is returned or false if none found
function pickItem(id, optHotbarSlot)
  local inv = openInventory()
  local map = inv.mapping[inv.getType()]
  local optHotbarSlot = optHotbarSlot or getHotbar() -- 1
  --scan hotbar first
  for i, j in pairs(map.hotbar) do
    local item = inv.getSlot( j )
    if item and item.id == id then
      setHotbar( i )
      return j
    end
  end
  local scanOrder = {
          "main", "contents", "output"
  }
  for _,iType in pairs(scanOrder)do
    --scan player inventory
    if map[scanOrder] then
      for i, j in pairs(map[scanOrder])do
        local item = inv.getSlot(j)
        if item and item.id == id then
          local p = map.hotbar[optHotbarSlot]
          inv.click( j ) --pickup
          waitTick()
          inv.click( p ) --place/swap
          waitTick()
          if inv.getHeld() then --place if swaped
            inv.click( j )
            waitTick()
          end
          --TODO close inventory maybe
        return optHotbarSlot
        end
      end
    end
  end
  return false
end



local function className()
  return "UNNAMED_CLASS"
end
--inherit( baseClass )
--class inheritance function
--baseClass may be nil for creating a class with no parent
--constructor can be overriden by saving it to a local variable then
--calling it durring the call to the 'new' function
--this will instantiate all parent classes for this object
function advancedMacros.inherit( baseClass )
  local class = {}
  local classMeta = { __index = class }

  function class:new( ... )
    assert(self, "self can not be nil")
    local obj = self.__instance and self or {__instance = true}
    
    setmetatable( obj, getmetatable(obj) or classMeta )
    
    --log("Class &a"..self.className().." &fcalling &e&Nsuper",tostring(baseClass and baseClass.className() or nil))
    
    if baseClass then
      baseClass.new(obj, ... )
    end
    obj.__instance = nil --cleanup so :new works normal when done
    return obj
  end

  if baseClass then
    setmetatable( class, { __index = baseClass })
  end

  function class:class()
    return class
  end
  
  class.className = className --generic
  
  function class:super()
    return baseClass
  end
  function class:isA( someClass )
    assert(self, "Self can not be nil")
    assert(type(someClass)=="table", "Argument provided is not a class")
    local current = class
    while current do
      if current == someClass then
        return true
      end
      current = current:super()
    end
    return false
  end

  return class
end

function luajava.findMethodByDescription( sClass, name, desc )
  for n, f in pairs( luajava.getDeclaredMethods(sClass) ) do
    if n==name then
      for i, m in pairs( luajava.splitOverloaded( f ) ) do
        if luajava.describeMethod( m ) == desc then
          return m
        end
      end
      return false
    end
  end
  return false
end
