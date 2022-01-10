local args = {...}

local sandboxMeta = {__index=_G}
local globalMeta = {
  __index=_G,
  __newindex=function(t,k,v) _G[k] = v end
}
local R = {}
local globeIcon   = "resource:globe.png"
local sandboxIcon = "resource:sandbox.png"

local function isShift()
  if __GAME_VERSION == "1.12.2" then
    return isKeyDown"LSHIFT" or isKeyDown"RSHIFT"
  else
    local state = HID.getState"keyboard"
    return state.LEFT_SHIFT or state.RIGHT_SHIFT
  end
end

local function wrapTask(f, ...)
  local args = {...}
  function tf()
    local mutex = newMutex"guiTask"
    mutex.lock()
    f(table.unpack(args))
    mutex.unlock()
  end
  runThread(tf, ...)
  return true
end
--lexicographic order
local function lexPairs( t )
  local keys = {}
  for a in pairs(t) do keys[#keys+1] = a end
  table.sort(keys)
  local i = 0
  return function()
    i = i+1
    return keys[i], t[keys[i]]
  end
end

function R.setup()
  REPL = gui.new()
  local wid, hei = REPL.getSize()
  REPL.elements = {}
  REPL.history = {'echo("&cH&6e&al&dl&5o &cW&6o&ar&bl&1d&d!")'}
  REPL.varPath = {"_ENV"}
  
  REPL.elements.outputScroll = REPL.newScrollBar(wid*2/3-7, 5,    7, hei-25   )
  REPL.elements.varScroll    = REPL.newScrollBar(wid-9    , 5+12, 7, hei-14-12)
  REPL.elements.varHScroll   = REPL.newScrollBar(wid*2/3+1  , hei-8,wid/3-10, 7, "h")
  REPL.elements.modeIcon     = REPL.newImage(sandboxIcon, wid*2/3-15, hei-17, 12,12)
  REPL.elements.modeIcon.mode= "sandbox"
  REPL.elements.varHScroll.setScrollSpeed(8)
  REPL.elements.outputLines = {}
  REPL.elements.outputGroup = REPL.newGroup()
  REPL.elements.varGroup = REPL.newGroup()
  REPL.elements.varGroup.setPos(wid*2/3+5, 0)
  REPL.elements.varLines = {}
  REPL.elements.varPath  = REPL.newText("_ENV:" ,wid*2/3+5,5)
  
  
  REPL.elements.outputScroll.setMaxItems(1)
  REPL.elements.outputScroll.setVisibleItems( math.ceil( ((hei-20)/12) ) )
  REPL.elements.varScroll.setVisibleItems( math.ceil( ((hei-20)/12) ) )
  REPL.elements.varHScroll.setVisibleItems( wid/3 )
  
  REPL.elements.input = REPL.newMinecraftTextField(5, hei-15, wid*2/3-10-22, 12)
  REPL.elements.input.enableBackground(false)
  REPL.elements.input.setFocused(true)
  REPL.elements.input.setMaxLength(1000)
  REPL.setOnKeyPressed( function(...)
    if REPL.elements.input.isFocused() then return end
    REPL.elements.input.setFocused(true)
    local char = ''
    if __GAME_VERSION == "1.12.2" then
      local charTyped, keycode = ...
      char = charTyped
    elseif __GAME_VERSION=="1.14.4" or __GAME_VERSION=="1.15.2" then
      local keyName, scanCode, modifiers = ...
      char = keyName
    else
      toast("REPL","Unsupported version")
    end
    if string.byte( char ) < string.byte' ' then return end --ignore control chars
    REPL.elements.input.setText( REPL.elements.input.getText()..char )
  end)
  if __GAME_VERSION == "1.12.2" then
    REPL.elements.input.setOnKeyPressed(
      function(char, keyCode)
        --log(keyCode)
        if keyCode==28 then --ENTER
          local expr = REPL.elements.input.getText()
          REPL.elements.input.setText""
          REPL.evaluate(expr)
          REPL.historyN = 0
        elseif keyCode==200 then --UP
          REPL.historyN = math.min((REPL.historyN or 0) + 1, #REPL.history)
          if REPL.historyN>0 then
            REPL.elements.input.setText(REPL.history[REPL.historyN])
          end
        elseif keyCode==208 then --DOWN
          REPL.historyN = math.max(0, (REPL.historyN or 0) - 1)
          if REPL.historyN>0 then
            REPL.elements.input.setText(REPL.history[REPL.historyN])
          else
            REPL.elements.input.setText""
          end
        end
      end)
  elseif __GAME_VERSION=="1.14.4" or __GAME_VERSION=="1.15.2" then
    REPL.elements.input.setOnKeyPressed( function(keyName, keyCode, mods)
      if keyName=="ENTER" then
        local expr = REPL.elements.input.getText()
        REPL.elements.input.setText""
        REPL.evaluate(expr)
        REPL.historyN = 0
      elseif keyCode==328 then --up
        REPL.historyN = math.min((REPL.historyN or 0) + 1, #REPL.history)
        if REPL.historyN>0 then
          REPL.elements.input.setText(REPL.history[REPL.historyN])
        end
      elseif keyCode==336 then --down
        REPL.historyN = math.max(0, (REPL.historyN or 0) - 1)
        if REPL.historyN>0 then
          REPL.elements.input.setText(REPL.history[REPL.historyN])
        else
          REPL.elements.input.setText""
        end
      end
      return true
    end)
    --REPL.elements.input.setOnCharTyped( function(char, mods)
    --  toast("X",char)
    --end)
  else
    toast("REPL","Unsupported version")
  end
  
  function REPL.updateVarList()
    local mutex = newMutex"REPL.updateVarList"
    mutex.lock()
    local t = REPL.varPath[1]=="_ENV" and REPL._env or _G
    for i=2, #REPL.varPath do
      local z = t[REPL.varPath[i]]
      if type(z)=="table" then
        t = z
      else
        REPL.varPath = {table.unpack(REPL.varPath, 1, i-1)}
      end
    end
    local path = table.concat( REPL.varPath, "&7->&f" )
    REPL.elements.varPath.setText( path:sub(math.max(1,#path-25)) )
    local lines = {}
    for k,v in lexPairs(t) do
      local typeColor = ({tab="&a",num="&b",boo="&e",fun="&d",str="&6"})[type(v):sub(1,3)] or ("&7["..type(v).."]&f")
      local first = true
      local vs = tostring(v)
      for w in vs:gmatch"[^\n]+" do
        lines[#lines+1] = {
          str = (first and (k.." &7= ") or "  ") ..typeColor..tostring(w),
          name = k,
          value = v
        }
        first = false
      end
    end
    local varsWidth = 0
    local wid, hei = REPL.getSize()
    for i = 1, 500 do
      if lines[i] then
        local k,v,s = lines[i].name, lines[i].value, lines[i].str
        if not REPL.elements.varLines[i] then
          REPL.elements.varLines[i] = REPL.newText("", wid*2/3+5, 15+(#REPL.elements.varLines*12)+REPL.elements.varGroup.getY() )
          REPL.elements.varLines[i].setParent( REPL.elements.varGroup )
          REPL.elements.varLines[i].setOnMouseClick(function(x,y,button)
            if button==0 then --LMB
              if type(REPL.elements.varLines[i].varValue)=="table" then
                table.insert(REPL.varPath, REPL.elements.varLines[i].varName)
                --log(REPL.varPath)
                REPL.updateVarList()
              else
                --toast(REPL.elements.varLines[i].varName, type(REPL.elements.varLines[i].varValue))
              end
            else
              local p = {table.unpack(REPL.varPath,2)}
              p[#p+1] = REPL.elements.varLines[i].varName
              local msg = ""
              for a,b in pairs(p) do
                msg = msg..((type(b)=="string") and ("."..b) or ("["..b.."]"))
              end
              REPL.elements.input.setText( 
                REPL.elements.input.getText()..msg:sub(2)..(type(REPL.elements.varLines[i].varValue)=="function" and "(" or "")
              )
            end
          end)
        end
        REPL.elements.varLines[i].setText(s)
        REPL.elements.varLines[i].varName = k
        REPL.elements.varLines[i].varValue = v
        REPL.elements.varLines[i].unremove()
        varsWidth = math.max(varsWidth, REPL.elements.varLines[i].getWidth())
      elseif REPL.elements.varLines[i] then
        REPL.elements.varLines[i].remove()
      else
        break
      end
      REPL.elements.varHScroll.setMaxItems(varsWidth + 5)
    end
    REPL.elements.varScroll.setMaxItems(#lines + 1)
    REPL.elements.varScroll.setScrollPos(math.min(REPL.elements.varScroll.getScrollPos(), REPL.elements.varScroll.getMaxItems()))
    REPL.elements.varHScroll.setScrollPos(math.min(REPL.elements.varHScroll.getScrollPos(), REPL.elements.varHScroll.getMaxItems()))
    REPL.updateVarScrollOffset()
    mutex.unlock()
  end
  
  function REPL.updateOutputScrollOffset()
    REPL.elements.outputGroup.setPos(0, -REPL.elements.outputScroll.getScrollPos()*12)
  end
  function REPL.updateVarScrollOffset()
    local wid = REPL.getSize()
    REPL.elements.varGroup.setPos(wid*2/3+5-REPL.elements.varHScroll.getScrollPos(), -REPL.elements.varScroll.getScrollPos()*12)
  end
  
  REPL.setOnScroll(function(delta)
    local mx, my = REPL.getMousePos()
    local wid, hei = REPL.getSize()
    if mx < wid*2/3 then
      REPL.elements.outputScroll.scroll(delta)
      REPL.updateOutputScrollOffset()
    else
      if isShift() then
        REPL.elements.varHScroll.scroll(delta)
      else
        REPL.elements.varScroll.scroll(delta)
      end
      REPL.updateVarScrollOffset()
    end
  end)
  
  REPL.elements.outputScroll.setOnMouseDrag(   REPL.updateOutputScrollOffset)
  REPL.elements.outputScroll.setOnMouseRelease( REPL.updateOutputScrollOffset)
  REPL.elements.varScroll.setOnMouseDrag( REPL.updateVarScrollOffset)
  REPL.elements.varScroll.setOnMouseRelease( REPL.updateVarScrollOffset)
  REPL.elements.varHScroll.setOnMouseDrag( REPL.updateVarScrollOffset)
  REPL.elements.varHScroll.setOnMouseRelease( REPL.updateVarScrollOffset)
  REPL.elements.varPath.setOnMouseClick( function(x,y,b)
    if #REPL.varPath == 1 then
      REPL.varPath[1] = (REPL.varPath[1]=="_ENV") and "_G" or "_ENV"
    else
      REPL.varPath[#REPL.varPath] = nil
    end
    --REPL.updateVarList()
  end)
  
  
  function REPL.echo(...)
    local args = {...}
    if #args == 0 then return end
    args = table.concat(args, " ")
    
    local m = args:gmatch("[^\n]+")
    args = m()
    if not args then return end
    local text = REPL.newText()
    text.setText(args)
    if #REPL.elements.outputLines > 500 then
      table.remove(REPL.elements.outputLines, 1).remove()
      for i = 1, #REPL.elements.outputLines do
        local l = REPL.elements.outputLines[i]
        l.setPos(0, l.getY()-12)
      end
    end
    text.setPos(0, 12*#REPL.elements.outputLines + REPL.elements.outputGroup.getY())
    text.setParent( REPL.elements.outputGroup )
    table.insert(REPL.elements.outputLines, text)
    
    REPL.elements.outputScroll.setMaxItems( #REPL.elements.outputLines )
    REPL.elements.outputScroll.setScrollPos( #REPL.elements.outputLines )
    REPL.updateOutputScrollOffset()
    
    while args do
      args = m()
      if args then
        REPL.echo(args)
      end
    end
  end
  function REPL.clear()
    while #REPL.elements.outputLines > 0 do
      table.remove(REPL.elements.outputLines, 1).remove()
    end
  end
  REPL._env = {
    echo = REPL.echo,
    clear = REPL.clear
  }
  setmetatable(REPL._env, sandboxMeta)
  
  function REPL.evaluate(expr)
    local mutex = newMutex"REPL.evaluate"
    if REPL.activeEval and (
       REPL.activeEval.getStatus() and(
       REPL.activeEval.getStatus() == "STOPPED" or --pcall should stop this from being needed
       REPL.activeEval.getStatus() == "CRASH" or 
       REPL.activeEval.getStatus() == "DONE")) then
      REPL.activeEval = nil
    end
    if not mutex.tryLock() or REPL.activeEval then
      toast("REPL","Evaluation is busy")
      REPL.elements.input.setText(expr)
      return
    end
    REPL.echo( (REPL.elements.modeIcon.mode=="global" and "&7[&1G&7] &f" or "&7[&aS&7]&f ").. expr:gsub("([^&])&([abcdef1234567890BIOSUFNTR])","%1&&%2") )
    local f, err = load("return "..expr,"sandbox","bt", REPL._env)
    local err2
    if err then
      f, err2 = load(expr, "sandbox", "bt", REPL._env)
    end
    table.insert(REPL.history, 1, expr)
    REPL.history[101] = nil
    
    function wrap()
      local result = {pcall(f)}
      if not result[1] then
        REPL.echo("&c"..result[2]:gsub("\n","\n&c"))
      else
        for i,r in pairs(result) do
          if i~=1 then
            REPL.echo(("&7 [%2d]&0-> &f%s"):format(i-1,tostring(r)))
          end
        end
      end
      REPL.activeEval = nil
    end
    if not f then
      REPL.echo("&6"..(err2 or err):gsub("\n","\n&6"))
    else
      REPL.activeEval = runThread(wrap)
    end
    
    --REPL.updateVarList()
    mutex.unlock()
  end
  
  function REPL.reset()
    REPL.close()
    R.setup()
    REPL.open()
  end
  
  function REPL.toggleMode()
    local mode = REPL.elements.modeIcon.mode=="sandbox" and "global" or "sandbox"
    REPL.elements.modeIcon.setImage( mode=="sandbox" and sandboxIcon or globeIcon )
    REPL.elements.modeIcon.mode = mode
    setmetatable(REPL._env, mode=="sandbox" and sandboxMeta or globalMeta)
    toast("REPL","Mode: "..mode)
  end
  
  local function timerFunc()
    --toast("REPL","Timer start")
    while true do
      local mutex = newMutex"REPL.varTimer"
      mutex.lock()
      local check = REPL.timer and REPL.timer.getID() == thread.current().getID()
      mutex.unlock()
      if not check then break end
      REPL.updateVarList()
      sleep(250)
    end
    --toast("REPL","Timer end")
  end
  REPL.timer = false
  function REPL.startVarTimer()
    local mutex = newMutex"REPL.varTimer"
    mutex.lock()
    if not REPL.timer then
      REPL.timer = thread.new(timerFunc)
      REPL.timer.start()
    end
    mutex.unlock()
  end
  function REPL.stopVarTimer()
    local mutex = newMutex"REPL.varTimer"
    mutex.lock()
    REPL.timer = false
    mutex.unlock()
  end
  function REPL.help()
    REPL.echo("&aTips:")
    REPL.echo("  &b1&f) Typing will auto-focus the text box.")
    REPL.echo("  &b2&f) Clicking the sandbox icon in the bottom left\n     will change if the expression should\n     modify the global enviroment.")
    REPL.echo("  &b3&f) Pressing UP or DOWN will cycle through previous commands.")
    REPL.echo("  &b4&f) Clicking on the word &e_ENV&f or &e_G&f on the right pannel will switch between\n     global and sandbox variables.")
    REPL.echo("  &b5&f) LMB on a table in the variable pannel will show the table's contents.")
    REPL.echo("  &b6&f) RMB on any variable in the right pannel will paste it's full name into the text box.")
    REPL.echo("  &b7&f) &aREPL.reset&f() can be used to reset this gui.")
    REPL.echo("  &b8&f) &dCTRL&f + &dC&f can be used to stop an active expression.")
    REPL.echo("  &b8&f) &aecho&f(&7...&f) can be used to output text to this console.")
    REPL.echo("  &b9&f) &7[&1G&7]&f and &7[&aS&7]&f indicate if an expression was run with global or local scope.")
    REPL.echo(" &b10&f) The local environment also index's the global enviroment so functions can be accessed.")
    REPL.echo(" &b11&f) Holding shift while scrolling will scroll sideways.")
    REPL.echo(" &b12&f) You can go back to a previous table in the var pannel by clicking the text at the top.")
    REPL.echo(" &b13&f) You can also get this help menu with &aREPL.help&f().")
    REPL.echo(" &b14&f) You can clear this console with &aclear&f().")
    REPL.echo(" &b15&f) Up to &6500&f lines will be shown in each pannel.")
    REPL.echo(" &b16&f) This gui is written in a &elua&f script.")
  end
  REPL._env.help = REPL.help
  function REPL.onResize()
    local wid, hei = REPL.getSize()
    REPL.elements.outputScroll.setPos(wid*2/3-7, 5)
    REPL.elements.outputScroll.setSize(7, hei-25)
    
    REPL.elements.varScroll.setPos(wid-9, 5+12)
    REPL.elements.varScroll.setSize(7, hei-14-12)
    
    REPL.elements.varHScroll.setPos(wid*2/3+1, hei-8)
    REPL.elements.varHScroll.setSize(wid/3-10, 7)
    
    REPL.elements.modeIcon.setPos(wid*2/3-15, hei-17)
    
    REPL.elements.input.setPos(5, hei-15)
    REPL.elements.input.setSize(wid*2/3-10-22, 12)
    
    REPL.elements.varPath.setPos(wid*2/3+5, 5)
    
    local mutex = newMutex"REPL.updateVarList"
    mutex.lock()
    REPL.elements.varGroup.setPos(wid*2/3+5, REPL.elements.varGroup.getY())
    mutex.unlock()
  end
  REPL.elements.modeIcon.setOnMouseClick( REPL.toggleMode )
  REPL.setOnOpen( REPL.startVarTimer )
  REPL.setOnClose( REPL.stopVarTimer )
  REPL.setOnResize( REPL.onResize )
  REPL.setName"REPL"
  REPL.echo("&7REPL Version 1.2\n&7Type &bhelp()&7 for tips!")
end

--
-- Main code
--
if not REPL or args[1]=="manual" then
  R.setup()
  --REPL.updateVarList()
end
REPL.open()
