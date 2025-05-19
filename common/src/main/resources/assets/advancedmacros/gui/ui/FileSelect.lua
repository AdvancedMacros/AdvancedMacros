local utils = advancedMacros.utils
local EventChannel = require"ui/EventChannel"
local File = require"File"

local Element = require"ui/Element"
local FileSelect = newClass("ui/FileSelect", Element)

function FileSelect:new( ... )
  local obj = FileSelect._new( self, ... )
  
  local args = utils.kwargs({
    { frameThickness = "number", 1, "frame" },
  }, ...)
  
  obj.frameThickness = args.frameThickness

  obj.events.fileChanged = EventChannel:new{}

  obj.elements.fileSelect = {}
  obj.elements.fileSelect.bg = obj.screen.newRectangle(args.frameThickness, args.frameThickness, obj.width-args.frameThickness*2, obj.height-args.frameThickness*2)
  obj.elements.fileSelect.frame = obj.screen.newBox(0, 0, obj.width, obj.height)
  obj.elements.fileSelect.text = obj.screen.newText("", args.frameThickness, args.frameThickness+1, obj.height - args.frameThickness*2-2)

  obj.elements.fileSelect.frame.setColor( 0xFFFFFFFF )
  obj.elements.fileSelect.bg.setHoverTint( 0x44FFFFFF )

  obj.elements.fileSelect.bg.setParent( obj.group )
  obj.elements.fileSelect.frame.setParent( obj.group )
  obj.elements.fileSelect.text.setParent( obj.group )
  
  obj.elements.fileSelect.bg.setOnMouseClick(function() obj:openPrompt() end)


  obj:setFile(nil, false)

  if self == FileSelect then
    obj:_postConstruct()
  end

  return obj
end

function FileSelect:openPrompt()
  local FileBrowser = require"ui/FileBrowser"
  FileBrowser:new({
    filters = {
      FileBrowser.static.filters.lua,
      FileBrowser.static.filters.noExt,
    },
    onSelect = function( browser, model )
      local workspace = browser.activeWorkspace
      self.screen.open() --exit file browser
      self:setFile(
        File:new{
          path = model.name,
          workspaceName = workspace.text,
          workspacePath = workspace.path,
      })
    end
  }):open()
end


function FileSelect:setFile(file, _fireEvent)
  local fs = self.elements.fileSelect
  local text = fs.text
  if file == nil then
    self.file = file
    text.setText"&7<click to set>"
  elseif isClass(file) and file:isA(File) then
    self.file = file
    text.setText( file:getName() )
  else
    error("Unexpected arg "..type(file),2)
  end
  
  text.setX( fs.bg.getX() + fs.bg.getWidth()/2 - text.getWidth()/2 )
  
  if _fireEvent ~= false then
    self.events.fileChanged:notify( self, self.file )
  end
end

function FileSelect:getFile()
  return self.file
end


return FileSelect