--[[
TODO:
Add workspace
RMB Workspace for permission settings
sandbox scripts for permissions
lock workspace of running scripts
scroll slide path in file browser
moveable bindings
direct string src binding
LMB workspace to navigate to folder
REPL load without keybind first
Import existing bindings
search events
open text editor
open in external editor?
sandbox toggle on binding/group?
 -> inspect sandbox?
Open editor from file browser
chat filter events (consumable)
mutex groups for binding?
temp bindings
color wheel (card color picker) enter hex code for color
scroll in changelog
autosave
custom event names
require should use modules from same workspace

update changelog
]]

local path = filesystem.resolve("", 1):gsub("\\","/").."/?.lua"
if not package.path:find( path, 1, true ) then
  package.path = package.path .. ";" .. path
  log("&7&NAdded local dir to package.path", path)
end

local toClear = {} -- {misc = true, DelayedTask = true, ChangeLog = true, Json = true, JsonObject = true, JsonArray = true}
for i, x in pairs( package.loaded ) do
  if i:gsub("\\","/"):find("modDev/1.20.x/gui/newBindingsMenu/",1,true) then
    toClear[ i ] = true
  end
end
for k in pairs( toClear ) do
  log("&7&BUnloaded &7"..k)
  package.loaded[ k ] = nil
end

-- run("ResourceSearcher.lua")
--preload
local Json = require"Json"
local File = require"File"
require"JsonArray"
require"JsonObject"
require"DelayedTask"
require"ui/EventChannel"
require"ui/Element"
require"ui/FramedRectangle"
local ScrollView = require"ui/layout/ScrollView"
require"ui/CycleButton"
require"ui/EnableToggle"
require"ui/ContextMenuItem"
require"ui/ContextMenu"
require"ui/EventSelect"
local Flow = require"ui/layout/Flow"
require"ui/layout/ViewCell"
require"ui/cells/FileListCell"
require"ui/cells/StringListCell"
require"ui/layout/ListView"
require"ui/ComboBox"
local ProfileSelect = require"ui/ProfileSelect"
require"ui/PathBar"
require"ui/FileSelect"
local Card = require"ui/Card"
local ConfirmationPrompt = require"ui/ConfirmationPrompt"
require"ui/ExampleCard"
require"ui/ColorPicker"
local TextPrompt = require"ui/TextPrompt"
local Binding = require"model/Binding"
local BindingGroup = require"model/BindingGroup"
require"ui/KeyPicker"
local BindingCard = require"ui/BindingCard"
local GroupCard = require"ui/GroupCard"
local FileBrowser = require"ui/FileBrowser"


local utils = advancedMacros.utils
local BindingsMenu = newClass"BindingsMenu"

function BindingsMenu:new( ... )
  local obj = BindingsMenu._new( self, ... )
  
  obj.screen = gui.new()
  local width, height = obj.screen.getSize()

  --header
  obj.profileText = obj.screen.newText("Profile: ", 6, 6, 14)
  obj.profileText.setOnMouseClick( function() obj:save() end )
  obj.profileSelect = ProfileSelect:new{
    screen = obj.screen,
    x = obj.profileText.getWidth() + 6, 
    y = 6,
    width = width - obj.profileText.getWidth() - 14,
    height = 14,
  }
  obj.toolbarFlow = Flow:new{
    screen = obj.screen,
    x = 6,
    y = obj.profileText.getY() + obj.profileText.getHeight() + 4,
    width = width - 12,
    height = 0,
    hgap = 4,
    vgap = 4,
    hAlign = "right",
  }
  obj.runningButton       = obj.screen.newImage("resource:running.png", 0, 0, 14 * 3, 14)
  obj.scriptBrowserButton = obj.screen.newImage("resource:orange_scripts.png",   0, 0, 14 * 3, 14)
  obj.replButton          = obj.screen.newImage("resource:repl.png",             0, 0, 14 * 2, 14)
  obj.changeLogButton     = obj.screen.newImage("resource:indigo_changelog.png", 0, 0, 14 * 3, 14)

  obj.toolbarFlow:add( obj.runningButton )
  obj.toolbarFlow:add( obj.scriptBrowserButton )
  obj.toolbarFlow:add( obj.replButton )
  obj.toolbarFlow:add( obj.changeLogButton )

  obj.runningButton.setHoverTint( 0x44000000 )
  obj.scriptBrowserButton.setHoverTint( 0x44000000 )
  obj.replButton.setHoverTint( 0x44000000 )
  obj.changeLogButton.setHoverTint( 0x44000000 )


  obj.profileSelect.events.profileChanged:addListener( function(profileSelect, current, previous) obj:loadProfile( current, previous ) end )
  obj.runningButton.setOnMouseClick( function(x, y, b) obj:openRunning() end )
  obj.scriptBrowserButton.setOnMouseClick( function(x, y, b) obj:openScriptBrowser(x, y, b) end )
  obj.replButton.setOnMouseClick( function(x, y, b) obj:openREPL(x, y, b) end )
  obj.changeLogButton.setOnMouseClick( function(x, y, b) obj:openChangeLog() end )

  --bindings
  obj.bindingsGroup = obj.screen.newGroup( 0, obj.toolbarFlow:getY() + 4 )
  obj.newBindingButton = obj.screen.newImage( "resource:greennewbinding.png", 6,  0, 14 * 3, 14 )
  obj.newGroupButton   = obj.screen.newImage( "resource:newgroup.png",        6 + 4 + 14 * 3,  0, 14 * 3, 14 )
  obj.bindingsScrollView = ScrollView:new{
    screen = obj.screen,
    x = 6,
    y = 14,
    width = width - 12,
    height = height - obj.bindingsGroup.getY() - 14 - 14 - 10,
  }
  obj.bindingsFlow = Flow:new{
    screen = obj.screen,
    x = 0,
    y = 6,
    width = obj.bindingsScrollView:getViewportWidth(),
    height = 0,
    hgap = 6,
    vgap = 6,
    debug = true
  }

  obj.bindingsScrollView:add( obj.bindingsFlow )

  obj.newBindingButton.setParent( obj.bindingsGroup )
  obj.newGroupButton.setParent( obj.bindingsGroup )
  obj.bindingsScrollView:setParent( obj.bindingsGroup )

  obj.newBindingButton.setHoverTint( 0x44000000 )
  obj.newGroupButton.setHoverTint( 0x44000000 )

  obj.newBindingButton.setOnMouseClick(function(x,y,b) obj:newBinding() end)
  obj.newGroupButton.setOnMouseClick(function(x,y,b) obj:newGroup() end)

  --footer
  obj.footerGroup = obj.screen.newGroup(0, height - 12 - 6)
  obj.tip = obj.screen.newText("&a&BTip:&f Context menu available &7(RMB)", 8, 0, 12)
  obj.tip.setParent( obj.footerGroup )

  obj.screen.setOnResize(function(w, h) obj:onResize(w, h) end)

  obj:loadProfile("DEFAULT")

  return obj
end

function BindingsMenu:onResize(w, h)
  self.profileSelect:setWidth(w - self.profileText.getWidth() - 14)
  self.toolbarFlow:setWidth(w - 12)
  self.bindingsScrollView:setWidth(w - 12)
  self.bindingsScrollView:setHeight(h - self.bindingsGroup.getY() - 14 - 14 - 10)

  for i, item in ipairs(self.bindingsFlow.children) do
    if instanceOf(item, GroupCard) then
      item:setWidth(w - 24)
    end
  end

  self.bindingsFlow:setWidth(self.bindingsScrollView:getViewportWidth())
  self.footerGroup.setY(h - 12 - 6)
end

function BindingsMenu:open()
  self.screen.open()
end

function BindingsMenu:openScriptBrowser( x, y, b )
  self.fileBrowser = self.fileBrowser or FileBrowser:new({})
  self.fileBrowser:open()
end

function BindingsMenu:openREPL( x, y, b )
  --TODO run REPL if REPL is not defined yet
  if b == utils.RMB then
    --context menu
    --reset
  else
    REPL.open()
  end
end

function BindingsMenu:openChangeLog( x, y, b )
  local ChangeLog = require"internal::ChangeLog"
  ChangeLog:new({}):open()
end

function BindingsMenu:openRunning()
  toast("TODO", "open gui")
end

function BindingsMenu:getProfileName()
  return self.profileSelect:getProfile()
end

function BindingsMenu:clearProfile()
  for i, child in ipairs(utils.values(self.bindingsFlow.children)) do --uses duplicate table so it's not modified during removal
    self.bindingsFlow:remove( child )
    child:remove()
  end
end

function BindingsMenu:loadProfile( name, previous )
  previous = previous or "DEFAULT"
  utils.tryCatch{
    try = function()
      local profileJson = File.static.profilesDir:navigate( name..".json" )
      if not profileJson:exists() and name ~= "DEFAULT" then
        error(("File not found: %s"):format(name, profileJson:getPath()), 2)
      end
      
      self:clearProfile()

      local profile = Json:new( profileJson:readAll() ):toTable()

      for i, item in ipairs( profile ) do
        --log("LOAD: "..item.className)
        if item.className == "model/Binding" then
          --log( item )
          self:newBinding( Binding:new( item ) ) -- use as kwargs
        elseif item.className == "model/BindingGroup" then
          self:newGroup( BindingGroup:new( item ) )
        end
      end
      -- self.profileSelect:setProfile( name )
    end,

    catch = function( err )
      log( "&c&BError loading profile '&f"..name.."&c&B':\n&c"..err )
      -- log(debug.traceback())
      local alert = ConfirmationPrompt:new{
        parentGui = self.screen,
        icon = ConfirmationPrompt.static.icons.ERROR,
        title = "Error",
        msg = "Could not load the profile '"..name.."'\n&bClear the profile and continue?\n&aYES&f - Continue without loading\n&cNO&f - Go back to '"..previous.."'",
        yes = function() self.profileSelect:setProfile( name ) self:clearProfile() end,
        no = function() self:loadProfile(previous) end,
      }
      local t = thread.new(function()
        for i = 1, 10 do --wait for popup to close
          if self.screen:isOpen() then break end
          sleep(100)
        end
        alert:open()
      end)
      t:start()
    end
  }
end

function BindingsMenu:save()
  local JsonArray = require"JsonArray"
  local array = JsonArray:new()

  for i, e in ipairs( self.bindingsFlow.children ) do
    if isClass( e ) then
      if e:isA( BindingCard ) or e:isA( GroupCard ) then
        array:put( e:exportModel():toJson() )
        -- table.insert( bindings, e:exportModel() )
      end
    end
  end

  thread.new(function()
    local file = File.static.profilesDir:navigate( self:getProfileName()..".json" )
    -- file:write(utils.serializeOrdered(bindings, nil, 2))
    file:write( array:toString() )
  end).start()
end

function BindingsMenu:bindCardCloseAction(card, parent)
  card.events.close:addListener(function()
    -- narrate("Remove element")
    parent:remove(card)
    card:remove()
  end)
end

function BindingsMenu:newBinding( binding )
  binding = binding or Binding:new{
    color = utils.randomColor()
  }
  local card = BindingCard:new{
    screen = self.screen,
    label = "Test Binding",
    x = 138,
    y = 5,
    width = 128,
    height = 64,
    color = binding.color,
    binding = binding
  }
  self.bindingsFlow:add( card )
  self:bindCardCloseAction(card, self.bindingsFlow)
end

function BindingsMenu:newGroup( group )
  local width = self.bindingsScrollView:getViewportWidth()
  local groupCard = GroupCard:new{
    screen = self.screen,
    label = "Test Group",
    x = 0,
    y = 0,
    width = width - 12,
    height = 120,
    color = utils.randomColor(),
  }
  
  if group then
    groupCard:applyModel( group )
  end
  
  self.bindingsFlow:add( groupCard )
  self:bindCardCloseAction( groupCard, self.bindingsFlow )
end

function BindingsMenu:listBindings(bindings)
  local bindings = bindings or {}
  local BindingCard = require"internal::ui/BindingCard"
  local GroupCard   = require"internal::ui/GroupCard"
  
  for i, child in ipairs( self.bindingsFlow.children ) do
    if isClass(child) then
      if child:isA( BindingCard ) then
        table.insert(bindings, child)
      elseif child:isA( GroupCard ) then
        child:listBindings(bindings)
      end
    end
  end
  
  return bindings
end


---@return `true if event consumed`, `<filter results>`
function BindingsMenu:_triggerBinding(binding, eventType, value, ...)
  local args = {...}
  local isConsumable = eventType == "event" and value:match"Filter$"
  local action
  local srcMode = binding:getScriptMode()
  local workspace = advancedMacros.getWorkspace"AM Default" --TODO sandbox direct access of this function away from other workspaces
  
  if srcMode == Binding.static.scriptModes.FILE then
    local file = binding:getScriptValue()
    if file:exists() then
      if not file.workspaceName then
        log(("&6Couldn't trigger binding with name '&f%s&6' because the File is missing a workspace name"):format(binding:getLabel()))
        return
      end
      workspace = advancedMacros.getWorkspace(file.workspaceName)
      if not workspace then
        log(("&6Couldn't trigger binding with name '&f%s&6' because it has no valid workspace set"):format(binding:getLabel()))
        return
      end
      action = function(...)
        return run( file:getPath(), ... )
      end
    else
      log(("&6Couldn't trigger binding with name '&f%s&6' because the &U&NFile&6 doesn't exist"):format(binding:getLabel()), file:getPath())
      return false
    end

  elseif srcMode == Binding.static.scriptModes.LUA then
    local src = binding:gstScriptValue()
    action = load(src, binding:getLabel(), "t", _G)
  end

  if action and not isConsumable then
    local t = thread.new(action)
    t.setLabel(binding:getLabel())
    t.setWorkspace(workspace)
    -- t.setWorkspace(file)
    t.start()
    return false
  elseif action then
    local results = {action(eventType, value, ...)}
    if #results == 0 then
      return true, results
    end
  end
end

function BindingsMenu:listMatchingBindings(...)
  local args = utils.kwargs({
    {eventType = "string"},
    {value     = "string"},
    {eventArgs = "table", {}},
    {includeAnything = "boolean", true},
  },...)
  local bindings = self:listBindings()
  local eventType = args.eventType
  local value     = args.value

  local bindingQueue = {}

  for i, binding in ipairs( bindings ) do
    local trigMode = binding:getTriggerMode()
    local trigVal = binding:getTriggerValue()
    local keyCheck = true
    if trigMode == "key up" then
      trigMode = "key"
      keyCheck = args.eventArgs[1] == "up"
    elseif trigMode == "key down" then
      trigMode = "key"
      keyCheck = args.eventArgs[1] == "down"
    elseif trigMode == "key all" then
      trigMode = "key"
    end

    -- log{
    --   label = binding:getLabel(), 
    --   args = {
    --     argEventType = args.eventType, 
    --     argsValue = args.value,
    --     argsIncludeAnything = args.includeAnything, 
    --   },
    --   locals = {
    --     trigMode = trigMode, 
    --     eventType = eventType, 
    --     keyCheck = keyCheck, 
    --     enable = binding:isEnabled()
    --   },
    --   ["..."] = {...}
    -- }

    if trigMode == eventType 
    and keyCheck
    and (trigVal == value or (args.includeAnything and trigVal == "Anything"))
    and binding:isEnabled() then
      table.insert(bindingQueue, binding)
    end
  end

  return bindingQueue
end

function BindingsMenu:trigger(...)
  local bindingQueue = self:listMatchingBindings(...)

  local args = utils.kwargs({
    {eventType = "string"},
    {value     = "string"},
    {eventArgs = "table", {}},
    {callback  = {"nil", "function"}} --for filter results
  },...)

  local eventArgs = args.eventArgs

  local t = thread.new(function()
    for i, binding in ipairs(bindingQueue) do
      log("&7Triggering binding with label &B"..binding:getLabel())
      local done, newArgs = self:_triggerBinding(binding, args.eventType, args.value, table.unpack(eventArgs))
      if done then
        break
      end
      eventArgs = newArgs or eventArgs
    end
    if args.callback then
      args.callback(table.unpack(eventArgs))
    end
  end)
  t.setLabel("event-dispatch:"..args.eventType..":"..args.value)
  t.setWorkspace(advancedMacros.getWorkspace"AM Default") --filter events use this workspace
  t.start()
end

-- local menu = BindingsMenu:new{}
-- menu:open()
-- MENU = menu --TODO remove debug

return BindingsMenu





-- local card = Card:new{
--   screen = menu.screen,
--   label = "Test Card",
--   x = 5,
--   y = 5,
--   width = 128,
--   height = 64,
--   radius = 14,
--   borderThickness = 1,
--   color = { 0, .7, .9, 1 }
-- }

-- local binding = Binding:new{
--   triggerMode = "key",
--   triggerName = "BACKSPACE",
--   scriptMode  = "file",
--   scriptValue = "example.lua"
-- }

-- local bindingCard = BindingCard:new{
--   screen = menu.screen,
--   label = "Test Binding",
--   x = 138,
--   y = 5,
--   width = 128,
--   height = 64,
--   radius = 12,
--   borderThickness = 1,
--   labelHeight = 14,
--   color = { .3, .4, .8, 1 },
--   binding = binding
-- }

-- local groupCard = GroupCard:new{
--   screen = menu.screen,
--   label = "Test Group",
--   x = 3,
--   y = 80,
--   width = 320,
--   height = 128,
--   radius = 12,
--   borderThickness = 1,
--   labelHeight = 14,
--   color = { .3, .4, .8, 1 },
-- }



-- card:setPos(5,5)
-- log(card.group)

-- local r = menu.screen.newRectangle( 300, 5, 32, 32 )
-- r.setOnMouseClick(function() FileBrowser:new({

-- }):open() end)
-- r.setColor(0xbb22FF99)

-- menu.screen.open()
-- local ColorPicker = require"ui/ColorPicker"
-- local colorPicker = ColorPicker:new{
--   parentGui = menu.screen
-- }
-- colorPicker.screen.open()
-- textPrompt = TextPrompt:new{
--   screen = menu.screen,
--   prompt = "Enter new name:",
-- }

-- textPrompt:open()