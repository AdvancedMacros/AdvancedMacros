local utils = advancedMacros.utils

local CycleButton = require"ui/CycleButton"
local StringListCell = require"ui/cells/StringListCell"
local ListView = require"ui/layout/ListView"

local ChangeLog = newClass"ChangeLog"

function ChangeLog:new( ... )
  local obj = ChangeLog._new( self, ... )
  
  obj.screen = gui.new()
  local width, height = obj.screen.getSize() 

  obj.showOnStartupLabel = obj.screen.newText("Show on startup? ", 6, 6, 12)
  obj.showOnStartup = CycleButton:new{
    screen = obj.screen,
    x = obj.showOnStartupLabel.getX() + obj.showOnStartupLabel.getWidth() + 4,
    y = 6,
    width = 12,
    height = 12,
    optionMode = "images",
    options = {
      "resource:greencheck.png",
      "resource:redx.png",
    },
    labels = {
      true, 
      false,
    },
    -- onChange = function(cycleButton, opt, index, label )
    --   local settings = getSettings()
    --   settings.changeLog = settings.changeLog or {}
    --   settings.changeLog.showOnStartup = label
    --   setttings.save()
    -- end
  }

  obj.showOnStartup:setSelectionByLabel( obj:isShowOnStartup() )

  obj.listView = ListView:new{
    screen = obj.screen,
    x = 6,
    y = 20,
    width = width - 12,
    height = height - 26,
    cellClass = StringListCell,
  }

  obj:readChangeLog()

  return obj
end

function ChangeLog:open()
  self.screen.open()
end

function ChangeLog:isShowOnStartup()
  local settings = getSettings()
  settings.changeLog = settings.changeLog or {}
  if settings.changeLog.showOnStartup == nil then
    settings.changeLog.showOnStartup = true
    settings.save()
  end
  return settings.changeLog.showOnStartup
end

function ChangeLog:readChangeLog()
  thread.new(function()
    local text = advancedMacros.getResource("gui/changelog.txt")
    local lines = utils.split(text, "\n")
    
    lines = utils.map(lines, function(k, v)
      return {text = v}
    end)
    
    self.listView:setData( lines )
  end).start()
end

return ChangeLog