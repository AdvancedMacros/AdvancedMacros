local utils = advancedMacros.utils

local ViewCell = require"ui/layout/ViewCell"
local FileListCell = newClass("ui/cells/FileListCell", ViewCell)

FileListCell.static = {
  
}

function FileListCell:new( ... )
  local obj = FileListCell._new( self, ... )
  
  local pixelScale = utils.resScale( obj.screen )
  obj.thumbnail = image.new( obj.height * pixelScale, obj.height * pixelScale )

  obj.elements.fileListCell = {
    thumbnail = obj.screen.newImage( thumnail, 0, 0, obj.height, obj.height ),
    text = obj.screen.newText("", obj.height + 2, 0, obj.height),
    bounds = obj.screen.newRectangle(0, 0, obj.width, obj.height),
  }

  obj.elements.fileListCell.bounds.setColor( 0 ) --clear
  obj.elements.fileListCell.bounds.setHoverTint(0x44FFFFFF)
  obj.group.setScissor( obj.width, obj.height )

  obj.elements.fileListCell.thumbnail.setParent( obj.group )
  obj.elements.fileListCell.text.setParent( obj.group )
  obj.elements.fileListCell.bounds.setParent( obj.group )
  obj.selected = false

  obj.elements.fileListCell.bounds.setOnMouseClick(function(x,y,b) 
    if b == utils.LMB then
      obj.events.mouseClicked:notify(x,y,b, obj.model) 
    else
      obj:openContextMenu()
    end
    return true
  end)

  return obj
end

--@override
function FileListCell:onModelChange()
  local name = self.model.name
  local path = self.model.path
  --local selected = self.model.selected
  local ext = self.model.ext

  if self.model.isDir then
    self.elements.fileListCell.thumbnail.setImage"resource:folder_64.png"
  elseif self.model.isImage then
    local tmp = image.load(path)
    local g = self.thumbnail.graphics
    g.clearRect( 1, 1, self.thumbnail.getSize() )
    g.drawImage( tmp, 1, 1, self.thumbnail.getSize() ) --TODO keep aspect ratio and center
    self.thumbnail.update()
    self.elements.fileListCell.thumbnail.setImage( self.thumbnail )
  elseif ext == "lua" then
    self.elements.fileListCell.thumbnail.setImage"resource:lua_64.png"
  else
    self.elements.fileListCell.thumbnail.setImage"resource:file_icon_64.png"
  end

  --self.elements.fileListCell.bounds.setColor( selected and ViewCell.static.SELECTED_COLOR or 0 )

  self.elements.fileListCell.text.setText( name )
end

function FileListCell:setWidth( width, ... )
  self.elements.fileListCell.bounds.setWidth( width )
  self.group.setScissor( self.width, self.height )
  FileListCell:super().setWidth( self, width, ... )
end

function FileListCell:getContextMenuLayout()
  local layout = {
    {
      label = "New...",
    },{
      label = "Show in System Explorer",
    },{
      label = "Copy Path",
    },{
      label = "Rename...",
    },{
      label = "Delete...",
    },{
      label = "Cut",
    },{
      label = "Copy",
    }
  }

  if self.model.ext == "lua" then
    table.insert( layout, 1, {
      label = "Run",
    })
  end

  if FileListCell.static.clipboard then
    table.insert( layout, {
      label = "Paste"
    })
  end
  return layout
end

function FileListCell:openContextMenu()
  local contextLayout = self:getContextMenuLayout()
  local contextMenu = require("ui/ContextMenu"):new{
    parent = self.screen,
    layout = contextLayout,
  }:open( self.screen.getMousePos() )
end

return FileListCell