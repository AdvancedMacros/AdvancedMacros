local utils = advancedMacros.utils

local Card = require"ui/Card"
local TextPrompt = newClass"ui/TextPrompt"

function TextPrompt:new( ... )
  local obj = TextPrompt._new( self, ... )
  
  local args = utils.kwargs({
    {screen = "table", nil, "parentGui"},
    {title = "string","Input"},
    {prompt = "string"},
    {maxLength = {"nil","number"}},
    {defaultText = "string", "", "default"},
    {callback = "function", nil, "onResult", "onOK"},
  }, ...)


  obj.screen = gui.new()
  obj.screen.setParentGui( args.screen )
  obj.callback = args.callback

  local screenWidth, screenHeight = obj.screen.getSize()
  local promptHeight = 128
  local labelHeight = 14


  obj.prompt = obj.screen.newText( args.prompt, 6, 6, 8 )
  local promptWidth = obj.prompt.getWidth() + 12
  promptWidth = math.max(promptWidth, screenWidth / 3)

  obj.card = Card:new{
    screen = obj.screen,
    label = args.title,
    x = screenWidth/2 - promptWidth/2,
    y = screenHeight/2 - promptWidth/2,
    width = promptWidth,
    height = promptHeight,
    radius = 12,
    borderThickness = 1,
    labelHeight = labelHeight,
    color = { .1, .1, .1, 1 },
    enableable = false,
    collapsable = false,
  }

  obj.card.events.close:addListener(function(card) obj:onClose() end)

  obj.input = obj.screen.newMinecraftTextField( obj.prompt.getX(), obj.prompt.getY() + obj.prompt.getHeight() + 6, obj.card:getWidth()-12, 12, args.defaultText )
  obj.okButton = obj.screen.newImage( "resource:ok.png", promptWidth - 24-6, obj.input.getY() + 6 + obj.input.getHeight(), 24, 12)
  obj.card:setHeight( obj.card.labelHeight + obj.okButton.getY() + obj.okButton.getHeight() + 6 )

  obj.prompt.setParent( obj.card.body )
  obj.input.setParent( obj.card.body )
  obj.okButton.setParent( obj.card.body )
  obj.okButton.setZ(1)
  obj.okButton.setHoverTint(0x44000000)

  obj.okButton.setOnMouseClick(function() obj:onOK() end)
  obj.input.setOnKeyPressed(function(keyCode, scanCode, mods) if keyCode == "ENTER" then obj:onOK() end end)

  
  return obj
end

function TextPrompt:open()
  self.input.setFocused(true)
  self.input.setCursorPos(0)
  self.input.setSelectionSize( #self.input.getText() )
  self.screen.open()
end

function TextPrompt:setTitle( title )
  self.title.setText( title )
end

function TextPrompt:getTitle()
  return self.title.getText()
end

function TextPrompt:getPrompt()
  return self.prompt.getText()
end

function TextPrompt:setPrompt( msg )
  self.prompt.setText( msg )
end

function TextPrompt:setInputText( text )
  self.input.setText( text )
end

function TextPrompt:getInputText()
  return self.input.getText()
end

function TextPrompt:onClose()
  self.screen.getParentGui().open()
end

function TextPrompt:onOK()
  self.screen.getParentGui().open()
  self.callback( self:getInputText() )
end

return TextPrompt