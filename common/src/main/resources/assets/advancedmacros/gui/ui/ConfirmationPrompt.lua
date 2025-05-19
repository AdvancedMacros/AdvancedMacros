local utils = advancedMacros.utils

local EventChannel = require"ui/EventChannel"
local Card = require"ui/Card"
local Flow = require"ui/layout/Flow"

local ConfirmationPrompt = newClass"ui/ConfirmationPrompt"

ConfirmationPrompt.static = {
  responses = {
    OK = "ok",
    YES = "yes",
    NO = "no",
    CANCEL = "cancel",
  },
  icons = {
    INFO = "resource:info_64.png",
    QUESTION = "resource:question_64.png",
    WARNING = "resource:warning_64.png",
    ERROR = "resource:error_64.png",
  }
}

function ConfirmationPrompt:new( ... )
  local obj = ConfirmationPrompt._new( self, ... )
  
  local args = utils.kwargs({
    { parentGui = {"nil", "table"} },
    { title = "string", nil, "header" },
    { prompt = {"string", "class:ui/Element", "table"}, nil, "msg", "message" }, --table is gui element, must have setPos, getWidth, getHeight
    { icon = {"nil","string","table"}, nil, "badge" },
    { okButton = {"boolean", "function"}, false, "ok" },
    { yesButton = {"boolean", "function"}, false, "yes" },
    { noButton = {"boolean", "function"}, false, "no" },
    { cancelButton = {"boolean", "function"}, false, "cancel" },
    { callback = {"nil", "function"}, nil, "onResponse", "onResult", "onOK"}
  }, ...)
  
  obj.screen = gui.new()
  obj.screen.setParentGui( args.parentGui )
  local screenWidth, screenHeight = obj.screen.getSize()

  obj.elements = {}
  obj.events = {
    response = EventChannel:new{},
    resize = EventChannel:new{},
  }

  if args.callback then
    obj.events.response:addListener( args.callback )
  end

  obj.elements.card = Card:new{
    screen = obj.screen,
    x = 0,
    y = 0,
    width = 100, --temp
    height = 100, --temp
    color = { .1, .1, .1, 1 },
    closeable = false,
    enableable = false,
    collapsable = false,
    showDivider = false,
    enableContext = false,
  }

  --primary layout container
  obj.elements.vbox = Flow:new{
    screen = obj.screen,
    parent = obj.elements.card.group,
    x = 6,
    y = 6,
    width = 0, --adjusted by flow
    height = 0,  --adjusted by flow
    vGap = 6,
    hAlign = "center",
    vbox = true,
  }

  --Title setup
  obj.elements.titleFlow = Flow:new{
    screen = obj.screen,
    width = 0, --adjusted by flow in hbox mode
    height = 0, --adjusted by flow
    hAlign = "left",
    vAlign = "center",
    hGap = 4,
    hbox = true, --single row, expands
  }

  if args.icon then
    obj.elements.icon = obj.screen.newImage( args.icon, 0, 0, 14, 14 )
    obj.elements.titleFlow:add( obj.elements.icon )
  end

  obj.elements.title = obj.screen.newText( args.title, 0, 0, 14 ) --x, y set by flow
  obj.elements.titleFlow:add( obj.elements.title )
  obj.elements.vbox:add( obj.elements.titleFlow )

  --Message setup
  local prompt
  if type( args.prompt ) == "string" then
    prompt = obj.screen.newText( args.prompt, 0, 0, 10 )
  else
    prompt = args.prompt
  end

  obj.elements.prompt = prompt
  obj.elements.vbox:add( prompt )

  --Buttons setup
  do
    obj.elements.buttonHbox = Flow:new{
      screen = obj.screen,
      width = 0,
      height = 0,
      vAlign = "center",
      hGap = 4,
      hbox = true,
    }

    obj.elements.buttons = {}
    local buttons   = obj.elements.buttons
    local responses = ConfirmationPrompt.static.responses
    local w, h = 14 * 2, 14
    
    if args.cancelButton then
      local button = obj.screen.newImage( "resource:cancel.png", 0, 0, w, h )
      button.response = responses.CANCEL
      buttons.cancel = button
      if type(args.cancelButton) == "function" then button.action = args.cancelButton end
      obj.elements.buttonHbox:add( button )
    end

    if args.noButton then
      local button = obj.screen.newImage( "resource:no.png", 0, 0, w, h )
      button.response = responses.NO
      buttons.no = button
      if type(args.noButton) == "function" then button.action = args.noButton end
      obj.elements.buttonHbox:add( button )
    end

    if args.yesButton then
      local button = obj.screen.newImage( "resource:yes.png", 0, 0, w, h )
      button.response = responses.YES
      buttons.yes = button
      if type(args.yesButton) == "function" then button.action = args.yesButton end
      obj.elements.buttonHbox:add( button )
    end

    if args.okButton then
      local button = obj.screen.newImage( "resource:ok.png", 0, 0, w, h )
      button.response = responses.OK
      buttons.ok = button
      if type(args.okButton) == "function" then button.action = args.okButton end
      obj.elements.buttonHbox:add( button )
    end

    local makeOnClick = function( button ) 
      return function() 
        if button.action then
          button.action() 
        end
        obj:onResponse( button.resp ) 
      end
    end
    for k,b in pairs( buttons ) do
      b.setOnMouseClick( makeOnClick(b) )
      b.setHoverTint( 0x44000000 )
    end

    obj.elements.vbox:add( obj.elements.buttonHbox )
  end

  obj:resizeCard()

  return obj
end

function ConfirmationPrompt:resizeCard()
  local w, h = self.elements.vbox:getSize()
  self.elements.card:setSize( w + 12, h + 12 )
  self.events.resize:notify( self, self.elements.card:getSize() )
end

function ConfirmationPrompt:onResponse( response )
  self:close()
  self.events.response:notify( self, response )
end

function ConfirmationPrompt:open()
  local sw, sh = self.screen.getSize()
  local w, h = self.elements.card:getSize()
  self.elements.card:setPos( sw/2-w/2, sh/2-h/2 )
  self.screen.open()
end

function ConfirmationPrompt:close()
  if self.screen.getParentGui() then
    self.screen.getParentGui().open()
  else
    self.screen.close()
  end
end

return ConfirmationPrompt