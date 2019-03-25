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
