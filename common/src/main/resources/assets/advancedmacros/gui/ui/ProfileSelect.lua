local utils = advancedMacros.utils

local misc = require"misc"
local File = require"File"
local EventChannel = require"ui/EventChannel"
local StringListCell = require"ui/cells/StringListCell"
local ComboBox = require"ui/ComboBox"
local ContextMenu = require"ui/ContextMenu"

local Element = require"ui/Element"
-- log(utils.keys(Element))
assert(Element, "ELEMENT!!!!!!1")
local ProfileSelect = newClass("ui/ProfileSelect", Element)

function ProfileSelect:new( ... )
  local obj = ProfileSelect._new( self, ... )
  
  local args = utils.kwargs({
  }, ...)
  
  obj.elements.comboBox = ComboBox:new{
    screen = obj.screen,
    x = 0,
    y = 0, 
    width = obj.width,
    height = obj.height,
    cellClass = StringListCell,
    cellArgs = {
      hoverTint = 0x44FFFFFF,
    },
    viewTransformer = function( model )
      local modified = utils.clone( model )
      modified.backgroundColor = nil
      return modified
    end
  }

  obj.events.profileChanged = EventChannel:new{}
  obj.elements.comboBox.events.optionSelected:addListener(function(combo, model, previousModel) 
    obj.events.profileChanged:notify(obj, model.text, previousModel and previousModel.text) 
  end)

  obj.elements.comboBox:setParent( obj.group )

  obj.elements.comboBox.events.optionUnselected:addListener(function(combo, model) if model == nil then return end model.backgroundColor = nil end)
  obj.elements.comboBox.events.optionSelected:addListener(function(combo, model) if model == nil then return end model.backgroundColor = 0x440088FF end)
  obj.elements.comboBox.events.rmb:addListener(function(box, x, y) obj:openContext() end)

  obj:loadProfiles()
  obj:buildContextMenu()

  if self == ProfileSelect then
    obj:_postConstruct()
  end

  return obj
end

function ProfileSelect:loadProfiles()
  local options = {}
  local profiles = {}
  local files = misc.profileDir:list".json$"
  -- local settings = getSettings()
  -- local profiles = {} --settings.profilesV2 or {}

  profiles.DEFAULT = profiles.DEFAULT or {}
  -- if not settings.profilesV2 then
  --   settings.profilesV2 = profiles
  --   settings.save()
  -- end
  -- local names = utils.keys( profiles )
  local names = utils.map(files, function(k, v) 
    return v:getName(true) --without extension
  end)

  local currentName = self:getProfile()
  local current
  local defaultProfile
  table.sort( names )

  for _, name in pairs( names ) do
    if type(name) == "string" then
      local opt = {
        text = name,
      }
      if name == "DEFAULT" then
        defaultProfile = opt
        opt.prefix = "&e"
      end
      table.insert( options, opt )
      if name == currentName then
        current = opt
      end
    elseif type(name) == "table" then
      table.insert( options, name )
      if name.text == currentName then
        current = opt
      end
    end
  end
  self.elements.comboBox:setOptions( options )
  -- log(current or defaultProfile )
  self.elements.comboBox:setSelection( current or defaultProfile )
end

function ProfileSelect:setProfile( name )
  if self:getProfile() == name then
    return
  end
  local combo = self.elements.comboBox
  local previous = self:getProfile()
  if previous ~= name then
    for i, opt in ipairs( combo:getOptions() ) do
      if opt.text == name then
        combo:setSelection( opt )
        self.events.profileChanged:notify( self, self:getProfile(), previous )
        return
      end
    end
  end
  error("No profile with name '"..tostring(name).."'")
end

function ProfileSelect:getProfile()
  local selection = self.elements.comboBox:getSelection()
  return selection and selection.text or "DEFAULT"
end

function ProfileSelect:promptNewProfile()
  local TextPrompt = require"ui/TextPrompt"
  TextPrompt:new{
    parentGui = self.screen,
    title = "New Profile",
    prompt = "Enter a name for the profile.",
    callback = function( name ) self:newProfile( name, true ) end
  }:open()
end

--_fromUI -> true: err on duplicate name
--_fromUI -> false: prompt on duplicate name
function ProfileSelect:newProfile( name, _fromUI )
  name = name:trim()
  --check if exists
  local file = misc.profileDir:navigate(name..".json")
  if file:exists() then
    if not _fromUI then
      error('Profile "'..name..'" already exists', 2)
    end

    local CProm = require"ui/ConfirmationPrompt"
    -- local profile = self:getProfile()
    CProm:new{
      parentGui = self.screen,
      icon = CProm.static.icons.QUESTION,
      title = "&eAlready exists",
      msg = 'A profile with the name "'..name..'" already exists.\nTry again?',
      yes = true,
      cancel = true,
      onResponse = function(cprom, response)
        if response == CProm.static.responses.YES then
          self:promptNewProfile()
        end
      end
    }:open()
    return
  end

  file:write("[]") --empty list
  
  self:loadProfiles()
  self:setProfile( name )
end

function ProfileSelect:clearProfile()
  local file = misc.profileDir:navigate(self:getProfile()..".json")
  file:write"[]"
  self.events.profileChanged:notify( self, name )
end

function ProfileSelect:deleteProfile()
  local name = self:getProfile()
  if name == "DEFAULT" then
    self:clearProfile()
    return
  end
  local settings = getSettings()
  settings.profilesV2[ name ] = nil
  settings.save()
  self:loadProfiles()
  self:setProfile"DEFAULT"
end

function ProfileSelect:getContextMenuLayout()
  return {
    {
      label = "&aNew...",
      icon  = "resource:whiteplus.png",
      onClick = function() self:promptNewProfile() end
    },{
      label = "&eClear...",
      icon = "resource:remove.png",
      onClick = function()
        local CProm = require"ui/ConfirmationPrompt"
        local profile = self:getProfile()
        CProm:new{
          parentGui = self.screen,
          icon = CProm.static.icons.WARNING,
          title = "&cClear Profile",
          msg = 'Remove all bindings and groups from\nthe profile "'..profile..'"?',
          yes = true,
          cancel = true,
          onResponse = function(cprom, response)
            if response == CProm.static.responses.YES then
              self:clearProfile()
            end
          end
        }:open()
      end
    },{
      label = "&cDelete...",
      icon  = "resource:trashcan2.png",
      onClick = function()
        local CProm = require"ui/ConfirmationPrompt"
        local profile = self:getProfile()
        local cprom
        if profile == "DEFAULT" then
          cprom = CProm:new{
            parentGui = self.screen,
            icon = CProm.static.icons.WARNING,
            title = "&cClear all?",
            msg = "&eDEFAULT&f profile can't be deleted.\nWould you like to &cremove all bindings&f instead?",
            yes = true,
            no = true,
          }
        else
          cprom = CProm:new{
            parentGui = self.screen,
            icon = CProm.static.icons.WARNING,
            title = "&cConfirm delete",
            msg = "Delete this profile?\n"..self:getProfile(),
            yes = true,
            no = true,
          }
        end
        cprom.events.response:addListener(function(cprom, response)
          if response == CProm.static.responses.YES then
            if profile == "DEFAULT" then
              self:clearProfile()
            else
              self:deleteProfile()
            end
          end
        end)
        cprom:open()
      end
    }
  }
end

function ProfileSelect:buildContextMenu()
  self.contextLayout = self:getContextMenuLayout()
  self.contextMenu = ContextMenu:new{
    parent = self.screen,
    layout = self.contextLayout,
  }
end

function ProfileSelect:openContext()
  if self.contextMenu then
    local x, y = self.screen.getMousePos()
    self.contextMenu:open(x, y)
  end
end

return ProfileSelect