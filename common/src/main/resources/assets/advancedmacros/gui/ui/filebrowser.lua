local utils = advancedMacros.utils
local File = require"File"
local FileListCell = require"ui/cells/FileListCell"
local StringListCell = require"ui/cells/StringListCell"
local ListView = require"ui/layout/ListView"
local PathBar = require"ui/PathBar"
local Workspace = require"Workspace"

local FileBrowser = newClass"ui/FileBrowser"
FileBrowser.static = {
  imageTypes = utils.map( image.getFormats().readers, function(k,v) return v:lower(), true end, true ),
  filters = {
    lua = "%.lua$",    --ends with .lua
    noExt = "^[^.]+$", --no dot
  }
}

function FileBrowser:new( ... )
  local obj = FileBrowser._new( self, ... )
  
  local args = utils.kwargs({
    { onSelect = {"nil","function"} },
    { file = "string", File.static.macrosDir:getPath(), "path"}, --TODO support File arg
    { workspace = {"nil","string"} },
    { filter = {"nil","string","table","function"}, nil, "filters" }, --lua pattern or patterns or function(name) return bool
  }, ...)

  obj.screen = gui.new()
  obj.onSelect = args.onSelect
  obj.filter = args.filter
  
  local sw, sh = obj.screen.getSize()

  obj.pathBar = PathBar:new{
    screen = obj.screen,
    x = 6,
    y = 6,
    width = sw - 12,
    height = 12,
  }

  obj.view = ListView:new{
    screen = obj.screen,
    x = 3 + sw * 1 / 3,
    y = 20,
    width = sw * 2 / 3 - 6,
    height = sh - 26,
    cellClass = FileListCell,
  }

  obj.workspaceLabel = obj.screen.newText("Workspace:", 6, obj.pathBar:getY() + obj.pathBar:getHeight() + 6, 12 )
  
  obj.workspaceList = ListView:new{
    screen = obj.screen,
    x = 1,
    y = obj.workspaceLabel.getY() + obj.workspaceLabel.getHeight(),
    width = sw / 3 - 6,
    height = sh - obj.workspaceLabel.getY() - obj.workspaceLabel.getHeight() - 6,
    cellClass = StringListCell,
    cellArgs = {
      hoverTint = 0x44FFFFFF,
    }
  }
  obj.newWorkspaceButton = obj.screen.newImage(
    "resource:whiteplus.png", 
    sw / 3 - 6 - 12,
    obj.workspaceLabel:getY(),
    12, 12
  )
  obj.newWorkspaceButton.setHoverTint( 0x44FFFFFF )

  obj:buildWorkspaceContextMenu()

  obj.view.events.cellClicked:addListener(function(x,y,b,model) obj:onFileClicked(x,y,b,model) end)
  obj.pathBar.events.pathChanged:addListener(function(path) obj:setPath( path ) end)
  obj.newWorkspaceButton.setOnMouseClick(function() obj:newWorkspace() end)
  -- obj.view:setData( {} )
  obj:setPath( args.file )

  
  
  obj.workspaceList.events.cellClicked:addListener(function(x,y,b,model) obj:onWorkspaceClicked(x,y,b,model) end)
  obj.activeWorkspace = obj.workspaceList.data[1]
  obj:updateWorkspaceList("AM Default")
  
  return obj
end

function FileBrowser:updateWorkspaceList(newSelection)
  local list = {}
  local workspaces = advancedMacros.listWorkspaces()
  workspaces["internal"] = nil --hide
  
  local keys = utils.keys(workspaces)
  table.sort(keys)
  for i, name in ipairs(keys) do
    local workspace = workspaces[name]
    list[i] = {
      text = name,
      path = workspace:getPath(),
      workspace = workspace
    }
  end

  for i, w in ipairs(list) do
    if not newSelection and w == self.activeWorkspace or newSelection == w.text then
      w.backgroundColor = 0x440077FF
      if newSelection then
        self.activeWorkspace = w
        self:setPath(self.activeWorkspace.path)
      end
      break
    end
  end

  self.workspaceList:setData(list)
end

function FileBrowser:newWorkspace(path)
  path = path or self.path
  local this = self

  local TextPrompt = require"ui/TextPrompt"
  
  TextPrompt:new{
    parentGui = self.screen,
    title     = "Name workspace",
    prompt    = "Add current directory as a workspace",
    callback  = function( name )
      
      if name == "AM Default" or name == "internal" then
        self:_confirmationTryRename("is reserved", self.newWorkspace, path)
        return
      end

      local workspace = advancedMacros.getWorkspace(name)
      if workspaace then
        self:_confirmationTryRename("is already in use", self.newWorkspace, path)
        return
      end --!exists
      workspace = advancedMacros.newWorkspace(name, path)
      
      this:updateWorkspaceList(name)
    end --callback
  }:open()
end

function FileBrowser:_confirmationTryRename(msg, callback, ...)
  local args = {...}
  local ConfirmationPrompt = require"ui/ConfirmationPrompt"
  local this = self
  local alert = ConfirmationPrompt:new{
    parentGui = self.screen,
    icon = ConfirmationPrompt.static.icons.WARNING,
    title = "Workspace already exists",
    msg = "The name\n&6"..name.."&f\n"..msg..". Choose a different name?",
    yes = function() 
      thread.new(function()
        sleep(100)
        callback(this,table.unpack(args))
      end).start()
    end,
    no = function() end
  }
  local t = thread.new(function()
    for i = 1, 10 do --wait for popup to close
      if self.screen:isOpen() then break end
      sleep(100)
    end
    alert:open()
  end)
  t:start()
end

function FileBrowser:onFileClicked( x, y, b, model )
  if b == utils.LMB then
    if filesystem.isDir( model.path ) then
      self:setPath( model.path )
    elseif self.onSelect then
      self.onSelect( self, model )
    elseif not model.isImage then
      toast("TODO","Open Editor")
    else
      toast("TODO","image/sound")
    end
  end
end

function FileBrowser:onWorkspaceClicked( x, y, b, model )
  if model ~= self.activeWorkspace then 
    local color = self.activeWorkspace.backgroundColor or 0x440077FF
    self.activeWorkspace.backgroundColor = nil
    model.backgroundColor = color
    self.activeWorkspace = model
    self:setPath( model.path )
    self.workspaceList:updateCells()
  end

  if b == utils.RMB and not model.workspace:isReserved() then
    local x, y = self.screen.getMousePos()
    self.workspaceContextMenu:open(x, y)
  end
end

function FileBrowser:getWorkspaceContextMenuLayout()
  return {
    {
      label = "Rename...",
      icon  = "resource:pencil_64.png",
      onClick = function() self:openRenamePrompt() end
    },
    {
      label = "Edit Permissions...",
      icon  = "resource:white_gear_64.png",
      onClick = function()
        self:openWorkspacePermissionsEditor()  
      end
    },
    {
      label = "Delete...",
      icon  = "resource:trashcan2.png",
      onClick = function() self:deleteWorkspace() end
    }
  }
end

function FileBrowser:openRenamePrompt()
  local TextPrompt = require"ui/TextPrompt"
  TextPrompt:new{
    parentGui = self.screen,
    title  = "Rename workspace",
    prompt = "Current name:\n"..self.activeWorkspace.text,
    callback = function(name)
      if name == "AM Default" or name == "internal" then
        self:_confirmationTryRename("is reserved", self.openRenamePrompt)
        return
      end
      
      if advancedMacros.getWorkspace(name) then
        self:_confirmationTryRename("is already in use", self.openRenamePrompt)
        return
      end

      self.activeWorkspace.workspace:rename(name)

      self:updateWorkspaceList(name)
    end
  }:open()
end

function FileBrowser:openWorkspacePermissionsEditor()
  require("ui/WorkspacePermissionEditor"):new{
    parentGui = self.screen,
    workspace = self.activeWorkspace.workspace
  }:open()
end

--deletes activeWorkspace after confirmation prompt
function FileBrowser:deleteWorkspace()
  local ConfirmationPrompt = require"ui/ConfirmationPrompt"
  ConfirmationPrompt:new{
    parentGui = self.screen,
    icon = ConfirmationPrompt.static.icons.WARNING,
    title = "Confirm delete",
    msg = "Are you sure you want to delete this workspace?\n&e"..self.activeWorkspace.text,
    yes = function()
      self.activeWorkspace.workspace:delete()
      self:updateWorkspaceList("AM Default")
    end,
    no = function() end
  }:open()
end

function FileBrowser:buildWorkspaceContextMenu( workspace )
  local ContextMenu = require"ui/ContextMenu"
  self.workspaceContextMenuLayout = self:getWorkspaceContextMenuLayout()
  self.workspaceContextMenu = ContextMenu:new{
    parent = self.screen,
    layout = self.workspaceContextMenuLayout,
    onSelect = function(contextItem)
      self:onWorkspaceContextMenuSelect(contextItem)
    end
  }
end

---incase of missing context menu logic
function FileBrowser:onWorkspaceContextMenuSelect(contextItem)
  toast("TODO", contextItem:getLabel())
end

function FileBrowser:back()
  --TODO navigate back without exiting
end

function FileBrowser:setPath( path )
  self.path = path
  self.pathBar:setPath( path )
  local fs = filesystem
  local files = fs.list( path )

  if self.filter then
    files = utils.filter(files, function(k,v)
      if fs.isDir(self.path.."/"..v) then return true end
      if type(self.filter) == "table" then
        for i, f in pairs( self.filter ) do
          if type( f ) == "function" then
            if f( v ) then return true end
          elseif type( f ) == "string" then
            if v:match( f ) then return true end
          end
        end
      elseif type(self.filter) == "function" then
        return self.filter( v )
      elseif type(self.filter) == "string" then
        return v:match(self.filter)
      end
    end)
  end

  table.sort(files, function( a, b )
    --return a < b
    local aIsDir = fs.isDir(self.path.."/"..a)
    local bIsDir = fs.isDir(self.path.."/"..b)
    if aIsDir and not bIsDir then
      return true
    end
    if not aIsDir and bIsDir then
      return false
    end
    return a:lower() < b:lower()
  end)

  files = utils.map(files, function(k, v)
    return {
      name = v,
      --selected = false,
      path = self.path.."/"..v,
      ext = (v:match("%.(.+)$") or ""):lower(),
      isImage = FileBrowser.static.imageTypes[(v:match("%.(.+)$") or ""):lower()],
      isDir = filesystem.isDir(self.path.."/"..v)
    }
  end)
  self.view:setData( files )
end

function FileBrowser:open()
  self.screen:open()
end

return FileBrowser