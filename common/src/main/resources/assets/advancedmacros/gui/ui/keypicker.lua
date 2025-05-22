local utils = advancedMacros.utils
local Card = require"ui/Card"
local EventChannel = require"ui/EventChannel"

local Element = require"ui/Element"
local KeyPicker = newClass("ui/KeyPicker", Element)

function KeyPicker:new( ... )
  local obj = KeyPicker._new( self, ... )
  
  local args = utils.kwargs({
    {current = "string", ""},
    {frameThickness = "number", nil, "frame"},
  }, ...)

  obj.events.keyChanged = EventChannel:new{}

  obj.elements.keyPicker = {}
  obj.elements.keyPicker.background = obj.screen.newRectangle(1, 1, obj.width-args.frameThickness*2, obj.height - args.frameThickness*2)
  obj.elements.keyPicker.frame = obj.screen.newBox(0,0, obj.width, obj.height, args.frameThickness)
  obj.elements.keyPicker.text = obj.screen.newText("", 4, args.frameThickness, obj.height - args.frameThickness*2 - 2)
  

  obj.elements.keyPicker.background.setParent( obj.group )
  obj.elements.keyPicker.frame.setParent( obj.group )
  obj.elements.keyPicker.frame.setColor( 0xFFFFFFFF )
  obj.elements.keyPicker.text.setParent( obj.group )
  obj.elements.keyPicker.background.setHoverTint(0x44FFFFFF)

  obj:setText( args.current )

  obj.elements.keyPicker.background.setOnMouseClick(function() obj:openPrompt() end)

  -----------

  obj.popupScreen = gui.new()
  obj.popupScreen.setParentGui( obj.screen )

  local screenWidth, screenHeight = obj.screen.getSize()
  local cardWidth, cardHeight = screenWidth/3, screenHeight/3

  obj.card = Card:new{
    screen = obj.popupScreen,
    label = "Key Input",
    x = screenWidth/2 - cardWidth/2,
    y = screenHeight/2 - cardHeight/2,
    width = cardWidth,
    height = cardHeight,
    radius = 12,
    borderThickness = 1,
    labelHeight = 14,
    color = { .1, .1, .1, 1 },
    enableable = false,
    collapsable = false,  
  }
  
  local msg = "[Press any key or button]"
  obj.prompt = obj.popupScreen.newText(msg, 0, 0, 8)
  obj.prompt.setX( cardWidth/2 - obj.prompt.getWidth()/2 )
  obj.prompt.setY( obj.card:getBodyHeight()/2 - obj.prompt.getHeight())
  obj.prompt.setParent( obj.card.body )

  obj.card.events.close:addListener(function() obj.screen.open() end)
  obj.card.elements.cardHead.remove.setOnMouseEnter(function() obj:setPrompt"&cCancel" end)
  obj.card.elements.cardHead.remove.setOnMouseExit(function() obj:setPrompt(msg) end)

  obj.popupScreen.setOnKeyPressed(function(keyCode, scanCode, mods)
    if obj.card.elements.cardHead.remove.isHover() then
      return false
    end
    obj.screen:open() --close popup
    obj:setText( keyCode )
  end)

  if self == KeyPicker then
    obj:_postConstruct()
  end

  return obj
end

function KeyPicker:openPrompt()
  self.popupScreen:open()
end

function KeyPicker:setPrompt( msg )
  self.prompt.setText( msg )
  self.prompt.setX( self.card:getBodyX() + self.card.width/2 - self.prompt.getWidth()/2 )
  self.prompt.setY( self.card:getBodyY() + self.card:getBodyHeight()/2 - self.prompt.getHeight())
end

function KeyPicker:setText( text )
  assert(type(text)=="string", "text must be string")
  text = text or ""
  if #text == 0 then
    self.text = false
    self.elements.keyPicker.text.setText( "&7<click to set>" )
  else
    self.text = text
    self.elements.keyPicker.text.setText( text )
  end
  local bgX, bgY = self.elements.keyPicker.background.getPos()
  local bgW, bgH = self.elements.keyPicker.background.getSize()
  local textW, textH = self.elements.keyPicker.text.getSize()
  self.elements.keyPicker.text.setPos(
    bgX + bgW / 2 - textW / 2,
    bgY + bgH / 2 - textH / 2
  )
  self.events.keyChanged:notify( self, text )
end

function KeyPicker:getText()
  return self.text
end


return KeyPicker