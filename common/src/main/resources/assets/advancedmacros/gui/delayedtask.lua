local utils = advancedMacros.utils
local DelayedTask = newClass"DelayedTask"

DelayedTask.static = {
  _UUID = 0,
  genUUID = function()
    DelayedTask.static.mutex.lock()
    DelayedTask.static._UUID = DelayedTask.static._UUID + 1
    local uuid = DelayedTask.static._UUID
    DelayedTask.static.mutex.unlock()
    return uuid
  end,
  mutex = newMutex"AM_DelayedTask"
}

function DelayedTask:new( ... )
  local obj = DelayedTask._new( self, ... )
  
  local args = utils.kwargs({
    { task = "function", nil, "action", "func", "runnable" },
    { delay = "number", nil, "millis", "seconds", "minutes", "hours", "days", "ticks" }, --default is millis
  }, ...)
  
  obj.status = "new"
  obj.task = args.task
  obj.delay     = args.delay
  obj.delayUnit = args"delay"
  obj.thread = thread.new( function() obj:_threadTask() end )
  obj.uuid = DelayedTask.static.genUUID()
  obj.mutex = newMutex( "AM_DelayedTask_"..obj.uuid )

  return obj
end

function DelayedTask:_threadTask()
  self.status = "waiting"
  self.startTime = os.clock()
  if self.delayUnit == "tick" then
    for i = 1, self.delay do
      waitTick()
      if self.canceled then return end
    end
    
  else
    local millis
    if self.delayUnit == "millis" or self.delayUnit == "delay" then
      millis = self.delay
    elseif self.delayUnit == "seconds" then
      millis = self.delay * 1000
    elseif self.delayUnit == "minutes" then
      millis = self.delay * 60 * 1000
    elseif self.delayUnit == "hours" then
      millis = self.delay * 60 * 60 * 1000
    elseif self.delayUnit == "days" then
      millis = self.delay * 24 * 60 * 60 * 1000
    else
      error("missing case for time unit '"..self.delayUnit.."'")
    end

    while (os.clock() - self.startTime) * 1000 < millis do
      local remaining = math.min( 1000, millis - (os.clock() - self.startTime) )
      if remaining > 0 then
        sleep( remaining ) --check every second if canceled (but don't go over remaining time)
      end
      if self.canceled then return end
    end
  end

  --final status check then call
  self.mutex.lock() --------------------------------
  if self.canceled then              -- Mutex locked
    self.mutex.unlock()              --
    return                           --
  end                                --
  local a, b = pcall( self.task )    --
  self.status = "done"               --
  self.mutex.unlock() ------------------------------

  if not a then error(b) end
end

function DelayedTask:resetTimer()
  self.startTime = os.clock()
end

function DelayedTask:start()
  self.mutex.lock()
  if self.canceled then
    self.mutex.unlock()
    return
  end
  self.thread.start()
  self.mutex.unlock()
  return self
end

function DelayedTask:cancel()
  self.mutex.lock()
  if self.status == "done" then
    self.mutex.unlock()
    return false
  end
  self.canceled = true
  self.status = "canceled"
  self.mutex.unlock()
  return true
end

return DelayedTask