local File = package.preload["File"]
local Json = package.preload["Json"]
local utils = advancedMacros.utils
local JsonArray = package.preload["JsonArray"]

local Workspace = newClass("Workspace", File)

---creates a workspace with the given values                     <br>
---kwargs:                                                       <br>
--- { workspaceName = {"string"}, "name"},                       <br>
--- { workspacePath = {"nil", "string", "class:File"} ,"path"},
function Workspace:new( ... )
  local args = utils.kwargs({
    { workspaceName = {"string"}, "name"},
    { workspacePath = {"string", "class:File"} ,"path"},
    { permissions   = {"nil", "table"}}
  },...)

  local obj = Workspace._new( self, {
    workspaceName = args.workspaceName,
    workspacePath = args.workspacePath,
    path = "."
  })
  
  obj.permissions = args.permissions or {}

  return obj
end

---constructor, throws error if file not found
---@param name File|string Exact file to use or name of file without extension
---@return Workspace
function Workspace:load( name )
  local file
  if isClass(name) and name:isA(File) then
    file = name
  else
    file = File.static.workspaceDir:navigate(name..".json")
  end
  if not file:exists() then
    error(("Workspace with name '%s' doesn't exist"):format(name),2)
  end

  local data = Json:new(file:readAll()):toTable()
  local obj = self:new(data)

  return obj
end

---constructor, loads the file associated with the current workspace
---@return Workspace
function Workspace:current()
  local name = thread.current().getWorkspace().workspaceName
  return self:load( File.static.workspaceDir:navigate(name..".json") )
end

------------------------------------------------------------------------------
-- instance functions                                                       --
------------------------------------------------------------------------------

---produces a this workspace in Json form for saving to file
---@Override
---@return Json
function Workspace:toJson()
  local json = Workspace:super().toJson( self )
  local perms = JsonArray:new()
  json:put("permissions", perms)
  return json
end

---Saves this workspace to it's associated file in the workspace directory
function Workspace:save()
  local file = self:getConfigFile()
  file:write( self:toJson():toString() )
end

---@return File config file found in the workspace directory
function Workspace:getConfigFile()
  return File.static.workspaceDir:navigate(self.workspaceName..".json")
end


--examples:
--workspace.permissions includes
--"fileio.read:MyWorkspace" allows reading in `My Workspace`
--"fileio.read:Other Workspace:*" allows reading in the folder `Other Workspace`, but not subfolders
--"fileio.read:Other Workspace:potato/**" allows reading in the `potato` folder `Other Workspace`, including subfolders
--"fileio.write:Other workspace" allows writing in `Other Workspace`
--"luajava:net.minecraft.**" allows luajava binding of any class in the `net.minecraft` package
--"luajava:awt." allows luajava binding of any class in the `net.minecraft` package
--"luajava" full access
--"movement"
--
function Workspace:hasPermission(permission)
  --call to advancedMacros
end

package.preload["Workspace"] = Workspace

return Workspace