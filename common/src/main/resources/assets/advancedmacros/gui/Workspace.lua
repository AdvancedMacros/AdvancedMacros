local File = require"File"
local Json = require"Json"
local utils = advancedMacros.utils
local JsonObject = require"JsonObject"
local JsonArray = require"JsonArray"

local Workspace = newClass("Workspace", File)

function Workspace:new( ... )
  local args = utils.kwargs({
    { workspaceName = {"nil", "string"}, "name"},
    { workspacePath = {"nil", "string", "class:File"} ,"path"},
  },...)

  local obj = Workspace._new( self, {
    workspaceName = args.workspaceName,
    workspacePath = args.workspacePath,
    path = "."
  })
  
  obj.permissions = {}

  return obj
end

--constructor <br>
--params: <br> 
--<code>name</code> - File or name of file without extension
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

--@Override
function Workspace:toJson()
  local json = Workspace:super().toJson( self )
  local perms = JsonArray:new()
  json:put("permissions", perms)
  return json
end

function Workspace:save()
  local file = self:getConfigFile()
  file:write( self:toJson():toString() )
end

function Workspace:getConfigFile()
  return File.static.workspaceDir:navigate(self.workspaceName..".json")
end

return Workspace