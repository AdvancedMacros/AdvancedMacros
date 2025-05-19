local utils = advancedMacros.utils

local CycleButton = require"ui/CycleButton"
local EventSelect = require"ui/EventSelect"
local KeyPicker   = require"ui/KeyPicker"
local FileSelect  = require"ui/FileSelect"
local Binding     = require"model/Binding"

local Card = require"ui/Card"
local BindingCard = newClass("res:BindingCard", Card)

function BindingCard:new( ... )
  local obj = BindingCard._new( self, ... )

  local args = utils.kwargs({
    { binding = "class:model/Binding", nil, "bind" }
  }, ...)

  -- obj.binding = args.binding
  
  obj.elements.bindingCard = {
    triggerMode = CycleButton:new{
      screen = obj.screen,
      parent = obj.body,
      x = 6, 
      y = obj.labelHeight + 6,
      width = 14,
      height = 14,
      frame = 1,
      backgroundColor = 0x77000000,
      optionMode = "images",
      labels = {
        Binding.static.triggerModes.KEY_DOWN,
        Binding.static.triggerModes.KEY_UP,
        Binding.static.triggerModes.KEY_ALL,
        Binding.static.triggerModes.EVENT,
      },
      options = {
        "resource:keydownevent.png",
        "resource:keyupevent.png",
        "resource:allkeyevent.png",
        "resource:gameevent.png",
      },
    },
    keyValue = KeyPicker:new{
      screen = obj.screen,
      parent = obj.body,
      x = 6 + 14 + 6,
      y = obj.labelHeight + 6,
      width = obj.width - 24 - 8,
      height = 14,
      frameThickness = 1,
      screen = obj.screen
    },
    eventValue = EventSelect:new{
      screen = obj.screen,
      parent = obj.body,
      x = 6 + 14 + 6, 
      y = obj.labelHeight + 6,
      width = obj.width - 24 - 8,
      height = 14,
    },
    scriptMode = CycleButton:new{
      screen = obj.screen,
      parent = obj.body,
      x = 6,
      y = obj.labelHeight + 6 + 14 + 4,
      width = 14, 
      height = 14,
      frame = 1,
      backgroundColor = 0x77000000,
      optionMode = "images",
      labels = {
        Binding.static.scriptModes.FILE,
        Binding.static.scriptModes.LUA,
      },
      options = {
        "resource:file_icon_64.png",
        "resource:lua_64.png"
      }
    },
    fileSelect = FileSelect:new{
      screen = obj.screen,
      parent = obj.body,
      x = 6 + 14 + 6,
      y = obj.labelHeight + 6 + 14 + 4,
      width = obj.width - 24 - 8,
      height = 14,
    }
  }

  obj.elements.bindingCard.triggerMode.events.change:addListener(function(button, option, index, label) obj:onTriggerModeChange(option, index, label) end)
  obj.elements.bindingCard.scriptMode.events.change:addListener(function(button, option, index, label ) obj:onScriptModeChange(option, index, label) end)

  obj.elements.bindingCard.eventValue:setVisible( args.binding.triggerMode == Binding.static.triggerModes.EVENT )
  obj.elements.bindingCard.keyValue:setVisible( args.binding.triggerMode ~= Binding.static.triggerModes.EVENT )

  obj:applyModel( args.binding )

  if self == BindingCard then
    obj:_postConstruct()
  end
  
  return obj
end

function BindingCard:onTriggerModeChange(option, index, label)
  local modes = Binding.static.triggerModes
  self.elements.bindingCard.eventValue:setVisible( label == modes.EVENT )
  self.elements.bindingCard.keyValue:setVisible( label ~= modes.EVENT )
end

function BindingCard:onScriptModeChange(option, index, label)
  local modes = Binding.static.scriptModes
  self.elements.bindingCard.fileSelect:setVisible( label == modes.FILE )
  -- self.binding:setScript
end

function BindingCard:getContextMenuLayout()
  local layout = BindingCard:super().getContextMenuLayout( self )
  return layout
end

function BindingCard:getTriggerMode()
  return self.elements.bindingCard.triggerMode:getSelectionLabel()
end

function BindingCard:getTriggerValue()
  local triggerMode = self:getTriggerMode()
  if triggerMode == Binding.static.triggerModes.EVENT then
    return self.elements.bindingCard.eventValue:getText()
  else
    return self.elements.bindingCard.keyValue:getText()
  end
end

function BindingCard:getScriptMode()
  return self.elements.bindingCard.scriptMode:getSelectionLabel()
end

function BindingCard:getScriptValue()
  local scriptMode = self:getScriptMode()
  if scriptMode == Binding.static.scriptModes.FILE then
    return self.elements.bindingCard.fileSelect:getFile()
  else
    toast("TODO","BindingCard:scriptValue")
  end
end

function BindingCard:setTriggerMode( modeName )
  self.elements.bindingCard.triggerMode:setSelectionByLabel( modeName )
end

function BindingCard:setTriggerName( name )
  local triggerMode = self:getTriggerMode()
  if triggerMode == Binding.static.triggerModes.EVENT then
    self.elements.bindingCard.eventValue:setText( name )
  else
    self.elements.bindingCard.keyValue:setText( name )
  end
end

function BindingCard:setScriptMode( modeName )
  self.elements.bindingCard.scriptMode:setSelectionByLabel( modeName )
end

function BindingCard:setScriptValue( scriptValue )
  local scriptMode = self:getScriptMode()
  if scriptMode == Binding.static.scriptModes.FILE then
    self.elements.bindingCard.fileSelect:setFile( scriptValue )
  else
    toast("TODO","BindingCard:scriptValue")
  end
end

function BindingCard:applyModel( binding )
  if binding.triggerMode then
    self:setTriggerMode( binding.triggerMode )
  end
  if binding.triggerName then
    self:setTriggerName( binding.triggerName )
  end
  if binding.scriptMode then
    self:setScriptMode( binding.scriptMode )
  end
  if binding.scriptValue then
    self:setScriptValue( binding.scriptValue )
  end
  if binding.label then
    self:setLabel( binding.label ) --inherited from ui/Card
  end
  if binding.color then
    self:setColor( binding.color ) --inherited from ui/Card
  end
  if binding.enabled then
    self:setEnabled( binding.enabled ) --inherited from ui/Card
  end
end

--for export/saving
function BindingCard:exportModel()
  return Binding:new{
    enabled     = self:isEnabled(),
    triggerMode = self:getTriggerMode(),
    triggerName = self:getTriggerValue(),
    scriptMode  = self:getScriptMode(),
    scriptValue = self:getScriptValue(),
    label       = self:getLabel(),
    color       = self:getColor(),
  }
end

return BindingCard