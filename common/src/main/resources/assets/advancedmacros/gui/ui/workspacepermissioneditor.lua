local utils = advancedMacros.utils

local Workspace = require"Workspace"

local WorkspacePermissionEditor = newClass"ui/WorkspacePermissionEditor"

function WorkspacePermissionEditor:new( ... )
  local obj = WorkspacePermissionEditor._new( self, ... )
  
  local args = utils.kwargs({
    {back = {"table", "nil"}, nil, "parentGui"},
    {workspace = {"string", "class:Workspace"}, nil}
  }, ...)

  self.screen = gui.new()
  self.back = args.back

  if type(args.workspace) == "string" then
    self.workspace = Workspace:load(args.workspace)
  else
    self.workspace = args.workspace
  end

  -------------------------------
  
  self.elements = {}

  self.elements.todo = self.screen.newText("TODO\n&6Under construction...", 5, 20)

  if self.back then
    self.elements.navigateBack = self.screen.newImage("resource:whiteback.png", 5, 5, 12, 12)
    self.elements.navigateBack.setHoverTint( 0x40000000 )
    self.elements.navigateBack.setOnMouseClick(function()
      self:navigateBack()
    end)
  end
  
  --[[
  [back] [Workspace name]

  ]]

  return obj
end

function WorkspacePermissionEditor:navigateBack()
  if self.back then
    self.back.open()
  end
end

function WorkspacePermissionEditor:open()
  self.screen.open()
end

return WorkspacePermissionEditor