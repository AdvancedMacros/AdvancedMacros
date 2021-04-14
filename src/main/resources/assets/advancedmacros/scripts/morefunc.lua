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

--Checks if the _MOD_VERSION is before the version given by the first arg
--If a second arg is passed, it checks if the second is before the first
function advancedMacros.isBeforeVersion( sVersion, modVersion )
  modVersion = modVersion or _MOD_VERSION
  assert(type(sVersion)=="string","Arg 1 expected string, got "..type(older))
  assert(type(modVersion)=="string","Arg 2 expected string or nil, got "..type(newer))
    
  local t = {}
  for a,b in sVersion:gmatch"(%d+)(%a*)" do
    t[#t+1] = {a,b}
  end
  for a,b in modVersion:gmatch"(%d+)(%a*)" do
    t[#t+1] = {a,b}
  end
  local a = tonumber(t[1][1])
  a = a *    26 + (t[1][2] and t[1][2]:byte() or (('a'):byte()-1)) - ('a'):byte() + 1
  a = a * 10000 + tonumber(t[2][1])
  a = a *    26 + (t[2][2] and t[2][2]:byte() or (('a'):byte()-1)) - ('a'):byte() + 1
  a = a *  1000 + tonumber(t[3][1])
  a = a *    26 + (t[3][2] and t[3][2]:byte() or (('a'):byte()-1)) - ('a'):byte() + 1
  
  local b = tonumber(t[4][1])
  b = b *    26 + (t[4][2] and t[4][2]:byte() or (('a'):byte()-1)) - ('a'):byte() + 1
  b = b * 10000 + tonumber(t[5][1])
  b = b *    26 + (t[5][2] and t[5][2]:byte() or (('a'):byte()-1)) - ('a'):byte() + 1
  b = b *  1000 + tonumber(t[6][1])
  b = b *    26 + (t[6][2] and t[6][2]:byte() or (('a'):byte()-1)) - ('a'):byte() + 1
  
  return a > b
end

--Checks if the _MOD_VERSION is at or equal to the first arg
--if a second arg is passed it checks if the second arg is newer than the first 
function advancedMacros.isAfterVersion( sVersion, modVersion )
  return not advancedMacros.isBeforeVersion( sVersion, modVersion )
end


--attempts to pick an item from your inventory
--if it exists it is picked from hotbar or moved to the prefered slot
--slot number is returned or false if none found
function pickItem(id, optHotbarSlot)
    local inv = openInventory()
    local map = inv.mapping[inv.getType()]
    local optHotbarSlot = optHotbarSlot or 1
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
      if map[iType] then
        for i, j in pairs(map[iType])do
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
--local utils = luajava.bindClass"com.theincgi.advancedMacros.misc.Utils"
--local x = luajava.findMethodByDescription( utils, "getDebugStacktrace", "()Lorg/luaj/vm2_v3_0_1/LuaValue;")
--x(utils)