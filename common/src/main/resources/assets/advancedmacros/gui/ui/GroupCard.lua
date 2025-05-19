local utils = advancedMacros.utils
local misc = require"misc"
local Flow = require"ui/layout/Flow"

local Card = require"ui/Card"
local GroupCard = newClass("ui/GroupCard", Card)

function GroupCard:new( ... )
  local obj = GroupCard._new( self, ... )
  
  local args = utils.kwargs({
  }, ...)
  
  obj.elements.newBindingButton = obj.screen.newImage( "resource:greennewbinding.png", 6,  6, 14 * 3, 14 )
  obj.elements.newGroupButton = obj.screen.newImage( "resource:newgroup.png", 6 + 4 + 14 * 3, 6, 14 * 3, 14 )

  obj.elements.newBindingButton.setParent( obj.body )
  obj.elements.newGroupButton.setParent( obj.body )

  obj.elements.newBindingButton.setHoverTint(0x40000000)
  obj.elements.newGroupButton.setHoverTint(0x40000000)

  obj.flow = Flow:new{
    screen = obj.screen,
    parent = obj.body,
    x = 6, y = obj.elements.newBindingButton:getY() + obj.elements.newBindingButton:getHeight() + 6,
    width = obj.width - 12,
    height = 1,
    hGap = 4, vGap = 4,
    hAlign = "center"
  }

  obj.flow.events.resize:addListener(function() obj:onFlowResize() end)

  obj.elements.newBindingButton.setOnMouseClick(function() obj:addBinding() end)
  obj.elements.newGroupButton.setOnMouseClick(function() obj:addGroup() end)

  obj:onFlowResize()
  if self == GroupCard then
    obj:_postConstruct()
  end

  return obj
end

function GroupCard:bindCardCloseAction(card, parent)
  card.events.close:addListener(function()
    parent:remove(card)
    card:remove()
  end)
end

function GroupCard:addBinding( binding )
  local Binding = require"model/Binding"
  local BindingCard = require"ui/BindingCard"
  local color = misc.randomColor()
  binding = binding or Binding:new{
    color = color
  }
  local card = BindingCard:new{
    screen = self.screen,
    label = "Test Binding",
    x = 138,
    y = 5,
    width = 128,
    height = 64,
    radius = 12,
    borderThickness = 1,
    labelHeight = 14,
    enabled = true,
    parentEnabled = self:isEnabled(),
    color = binding.color,
    binding = binding
  }
  self:addChildEnableChangeListener( card )
  self.flow:add( card )
  self:bindCardCloseAction( card, self.flow )
end

function GroupCard:addGroup( model )
  local groupCard = GroupCard:new{
    screen = self.screen,
    label = "Test Group",
    x = 0,
    y = 0,
    width = self.width - 12,
    height = 120,
    radius = self.radius,
    borderThickness = self.borderThickness,
    labelHeight = self.labelHeight,
    color = { math.random(), math.random(), math.random(), 1 },
    enabled = true,
    parentEnabled = self:isEnabled(),
  }
  if model then
    groupCard:applyModel( model )
  end
  groupCard:onFlowResize()
  groupCard.events.resize:addListener(function() self:onFlowResize() end)
  self:addChildEnableChangeListener( groupCard )
  self.flow:add( groupCard )
  self:bindCardCloseAction(groupCard, self.flow)
end

function GroupCard:addChildEnableChangeListener( card )
  self.events.enableChanged:addListener(function( parent, enabled ) 
    card:setParentEnabled( enabled )
  end)
end

function GroupCard:onFlowResize()
  local flowY = self.flow:getY()
  local flowHeight = self.flow:getHeight()
  local y = self:getY()
  self.height = flowY - y + flowHeight + 6
  self:resizeCard()
end

function GroupCard:getContextMenuLayout()
  local menu = GroupCard:super().getContextMenuLayout( self )

  return menu
end

function GroupCard:applyModel( bindingGroup )
  self:setLabel( bindingGroup.label ) --from ui/Card
  self:setColor( bindingGroup.color ) --from ui/Card
  self:setEnabled( bindingGroup.enabled ) --from ui/Card
  local Binding = require"model/Binding"
  local BindingGroup = require"model/BindingGroup"
  for i, item in ipairs( bindingGroup.children ) do
    if item.className == "model/Binding" then
      self:addBinding( Binding:new( item ) ) -- use as kwargs
    elseif item.className == "model/BindingGroup" then
      self:addGroup( BindingGroup:new(item) )
    end
  end
end

function GroupCard:listBindings(bindings)
  local bindings = bindings or {}
  local BindingCard = require"ui/BindingCard"
  
  for i, child in ipairs( self.flow.children ) do
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

function GroupCard:exportModel()
  local BindingCard = require"ui/BindingCard"
  local Model = require"model/BindingGroup"
  local children = {}

  for i, child in ipairs( self.flow.children ) do
    if isClass( child ) then
      if child:isA( GroupCard ) or child:isA( BindingCard ) then
        table.insert( children, child:exportModel() )
      end
    end
  end

  return Model:new{
    label = self:getLabel(),
    color = self:getColor(),
    enabled = self:isEnabled(),
    children = children
  }
end

return GroupCard