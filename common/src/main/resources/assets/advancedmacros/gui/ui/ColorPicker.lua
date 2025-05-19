local utils = advancedMacros.utils
local misc = require"misc"
local Card = require"ui/Card"
local ExampleCard = require"ui/ExampleCard"

local ColorPicker = newClass"ui/ColorPicker"

function ColorPicker:new( ... )
  local obj = ColorPicker._new( self, ... )
  
  local args = utils.kwargs({
    { parentGui = "table", nil, "screen" },
    { onSelect = "function" },
    { color = "table", {1,1,1,1}, "defaultColor" }
  }, ...)

  obj.parentGui = args.parentGui
  obj.screen = gui.new()
  obj.screen.setParentGui( obj.parentGui )
  obj.onSelect = args.onSelect
  obj.color =  {obj:limitColor( table.unpack(args.color) )}
  

  local sw, sh = obj.screen.getSize()
  local cardWidth, cardHeight = sh / 2 , sh / 3
  local exampleWidth = cardWidth / 3
  local exampleHeight = exampleWidth / 2
  
  obj.elements = {
    card = Card:new{
      screen = obj.screen,
      x = sw / 2 - cardWidth / 2,
      y = sh / 2 - cardHeight / 2,
      width = cardWidth,
      height = cardHeight,
      color = {.1, .1, .1, 1},
      collapsable = false,
      enableable = false,
      label = "Pick Color",
      enableContext = false,
    },
  }
  local imgSize = math.min(obj.elements.card:getBodyHeight() - 12, cardWidth * 2 / 3 - 12)
  obj.scale = misc.resScale( obj.screen )

  obj.wheelImage = image.new(imgSize * obj.scale, imgSize * obj.scale)
  thread.new(function()
    obj:drawWheel()
  end).start()


  obj.elements.example = ExampleCard:new{
    screen = obj.screen,
    x = 12 + imgSize,
    y = 6,
    width = exampleWidth,
    height = exampleHeight,
    color = obj.color,
    closeable = false,
    label = "Example",
    useColorCache = false,
  }

  obj.elements.textGroup = obj.screen.newGroup(12 + imgSize, 12 + exampleHeight )
  obj.elements.todo = obj.screen.newText("&7TODO:\n&7text edit\n<--- Click to set", 0, 0, 8)
  
  obj.elements.wheel = obj.screen.newImage(obj.wheelImage, 6, 6, imgSize, imgSize)
  obj.elements.ok = obj.screen.newImage("resource:ok.png", cardWidth - (cardWidth/3 - 14), obj.elements.card:getBodyHeight() - 6 - 14, 28, 14)
  
  obj.elements.ok.setHoverTint( 0x44000000 )
  
  obj.elements.wheel.setOnMouseClick(function(...) obj:onClickWheel(...) end)
  obj.elements.wheel.setOnMouseDrag(function(...) obj:onClickWheel(...) end)
  obj.elements.ok.setOnMouseClick(function() obj.onSelect(obj, obj.color) end)
  obj.elements.card.elements.cardHead.remove.setOnMouseClick(function() obj:close() end)
  
  obj.elements.wheel.setParent( obj.elements.card.body )
  obj.elements.example:setParent( obj.elements.card.body )
  obj.elements.ok.setParent( obj.elements.card.body )
  obj.elements.todo.setParent(obj.elements.textGroup)
  obj.elements.textGroup.setParent(obj.elements.card.body)
  
  return obj
end

function ColorPicker:open()
  self.screen.open()
end

function ColorPicker:close()
  self.parentGui.open()
end

function ColorPicker:limitColor(r,g,b,a)
  if not r then return r,g,b,a end
  local t = 1 - (math.max( 2.7,  r*r + g*g + b*b ) - 2.7)
  return r*t, g*t, b*t, a
end

function ColorPicker:drawWheel()
  local w,h = self.wheelImage.getSize()
  for y = 1, h do
    for x = 1, w do
      local r,g,b,a = self:getColorAtPixel(x,y,1)
      if not r then r,g,b,a = 0,0,0,0 end
      self.wheelImage.setPixel(x,y, r,g,b,a)
    end
  end
  runOnMC( self.wheelImage.update )
end

function ColorPicker:onClickWheel(x,y,button)
  local wx, wy = self.elements.wheel.getPos()
  local px, py = (x-wx)*self.scale, (y-wy)*self.scale
  local r,g,b,a = self:limitColor( self:getColorAtPixel( px, py, 1 ) )
  if r then
    self.elements.example:setColor{r,g,b,a}
    self.elements.example:updateTiles()
    self.color = {r,g,b,a}
  end
end

function ColorPicker:getColorAtPixel(x, y, value)
  value = value or 1
  local w,h = self.wheelImage.getSize()
  local cx, cy = w/2 + 1, h/2 + 1
  local dx, dy = x - cx, y - cy
  local d = math.sqrt( dx*dx + dy*dy ) / (w/2)
  if d > 1 then
    return false
  end
  local a = (math.deg(math.atan2( dy, dx )) + 360) % 360
  return utils.hsvToRgb( a, d, value, 1 )
end

return ColorPicker