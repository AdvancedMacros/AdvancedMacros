local utils = advancedMacros.utils
local Element = require"ui/Element"
local ContextMenu = require"ui/ContextMenu"

local EventSelect = newClass("ui/EventSelect", Element)
EventSelect.static = {
  menu = {
    {
      icon = "whitesearch.png", 
      label = "Search..."
    },
    {
      label = "Player", 
      menu = {
        {
          label = "Action",
          menu = {
            { label = "ArrowFired"     },
            { label = "AttackEntity"   },
            { label = "BlockInteract"  },
            { label = "BreakItem"      },
            { label = "EntityInteract" },
            { label = "HotbarChanged"  },
            { label = "ItemCrafted"    },
            { label = "ItemPickup"     },
            { label = "ItemTossed"     },
            { label = "JoinWorld"      }, --also in Player/status
            { label = "LeaveWorld"     }, --also in Player/status
            { label = "Respawn"        },
            { label = "UseBed"         },
            { label = "UseItem"        },
          }
        },{
          label = "Status",
          menu = {
            { label = "AirChanged"       },
            { label = "ArmourDurability" },
            { label = "AttackReady"      },
            { label = "Death"            },
            { label = "DimensionChanged" },
            { label = "HealthChanged"    },
            { label = "HungerChanged"    },
            { label = "SaturationChanged"},
            { label = "ItemDurability"   },
            { label = "PlayerIgnited"    },
            { label = "PotionStatus"     },
            { label = "JoinWorld"        },
            { label = "LeaveWorld"       },
            { label = "WakeUp"           },
            { label = "XP"               },
          },
        }
      }
    },{
      label = "GUI",
      menu = {
        { label = "Actionbar"      },
        { label = "Chat"           },
        { label = "ChatFilter"     },
        { label = "ChatSendFilter" },
        { label = "ContainerOpen"  },
        { label = "GuiClosed"      },
        { label = "GuiOpened"      },
      }
    },{
      label = "World",
      menu = {
        { label = "PlayerJoin"  },
        { label = "PlayerLeave" },
        { label = "Sound"       },
        { label = "Title"       },
        { label = "Weather"     },
        { label = "WorldSaved"  },
      },
    },{
      label = "Other",
      menu = {
        { label = "Anything"      },
        { label = "ProfileLoaded" },
        { label = "Startup"       },
      }
    }
  }
}

function EventSelect:new( ... )
  local obj = EventSelect._new( self, ... )
   
  local args = utils.kwargs({  
    { current = "string", "", "text" },
    { frameThickness = "number", 1, "frame" },
  }, ...)

  obj.elements.eventSelect = {}
  obj.elements.eventSelect.background = obj.screen.newRectangle( 1, 1, obj.width-2, obj.height-2 )
  obj.elements.eventSelect.frame = obj.screen.newBox( 0, 0, obj.width, obj.height, args.frameThickness )
  obj.elements.eventSelect.text = obj.screen.newText( "", 2, 0, obj.height - args.frameThickness*2 - 2 )
  obj.elements.eventSelect.background.setParent( obj.group )
  obj.elements.eventSelect.frame.setParent( obj.group )
  obj.elements.eventSelect.text.setParent( obj.group )

  obj.elements.eventSelect.frame.setColor( 0xFFFFFFFF )
  obj.elements.eventSelect.background.setHoverTint( 0x55FFFFFF )


  obj:buildMenus()

  obj.elements.eventSelect.background.setOnMouseClick(function(x,y,button)
    obj.menu:open( x, y )
  end)

  obj:setText( args.current )

  if self == EventSelect then
    obj:_postConstruct()
  end

  return obj
end

function EventSelect:setText( text )
  text = text or ""
  if #text == 0 then
    self.text = false
    self.elements.eventSelect.text.setText( "&7<click to set>" )
  else
    self.text = text
    self.elements.eventSelect.text.setText( text )
  end
  local bgX, bgY = self.elements.eventSelect.background.getPos()
  local bgW, bgH = self.elements.eventSelect.background.getSize()
  local textW, textH = self.elements.eventSelect.text.getSize()
  self.elements.eventSelect.text.setPos(
    bgX + bgW / 2 - textW / 2,
    bgY + bgH / 2 - textH / 2
  )
end

function EventSelect:getText()
  return self.text
end

function EventSelect:buildMenus()
  self.menu = ContextMenu:new{
    parent = self.screen,
    layout = EventSelect.static.menu,
    onSelect = function( option )
      if option:getLabel() == "Search..." then --TODO/FIXME search doesn't close context menu
        toast("TODO", "Search events")
        return
      end
      if self.onChange then
        self:onChange( option:getLabel() )
      end
      self:setText( option:getLabel() )
    end
  }
end


return EventSelect