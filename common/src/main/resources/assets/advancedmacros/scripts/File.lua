local utils = advancedMacros.utils

local File = newClass"File"

local function lookupWorkspaceName( path )
  return utils.inverse( getSettings().workspaces )[ path ]
end

function File:new( ... )
  local obj = File._new( self, ... )
  
  local args = utils.kwargs({
    { workspaceName = {"nil", "string"}},
    { workspacePath = {"nil", "string", "class:File"} },
    { path = {"string", "table"}, nil, "file", "folder" },
  }, ...)

  local workspaceName, workspacePath = args.workspaceName, args.workspacePath
  if not args.workspaceName and not args.workspacePath then
    local ws = advancedMacros.getWorkspace()
    workspacePath = ws.path
    workspaceName = ws.name
    -- log("set path from no args: ", workspacePath)
  end

  obj.workspaceName = workspaceName
  if workspaceName and not workspacePath then
    workspacePath = getSettings().workspaces[ workspaceName ]:gsub(filesystem.separator,"/")
    if not obj.workspaceName then
      error("No workspace '"..workspaceName.."' defined in getSettings().workspaces")
    end
  elseif isClass( workspacePath ) and workspacePath:isA(File) then
    workspacePath = args.workspacePath:getPath()
  elseif not workspacePath then
    workspacePath = (args.workspacePath or advancedMacros.getWorkspace().path):gsub(filesystem.separator,"/"):match("(.+)[^/]?") --don't include ending slash if present
  end

  obj.workspacePath = workspacePath
  if not obj.workspaceName then 
    obj.workspaceName = lookupWorkspaceName( workspacePath )
  end

  local path = args.path
  if type( path ) == "string" then
    obj.path = path:match"^/?(.+)$"
  elseif isClass(path) then
    error("path arg can not be class, should be list of strings",2)
  else
    for k,v in ipairs(path) do
      if type(v) ~= "string" then
        error("expected string in table of arg path at index "..k..", got "..type(v), 2)
      end
      path[k] = v:gsub(filesystem.separator, "/"):match"^/?(.+)/?$"
    end
    obj.path = table.concat(path, "/")
  end
  
  --simplfy path if possible
  for folder, back in obj.path:gmatch"([^/]+)(/%.%./)" do -- something/../
    if folder ~= ".." then --don't collapse /../..
      obj.path = obj.path:gsub( folder..back, "", 1 )
    end
  end

  return obj
end

function File:getLocalPath()
  return self.path
end

function File:getPathParts()
  local parts = {}
  for part in self:getPath():gmatch"[^/]+" do
    table.insert(parts, part)
  end
  return parts
end

function File:getPath()
  local full = self.workspacePath.."/"..self.path
  for folder, back in full:gmatch"([^/]+)(/%.%./)" do -- something/../
    if folder ~= ".." then --don't collapse /../..
      full = full:gsub( folder..back, "", 1 )
    end
  end
  return full
end

function File:navigate( to )
  local file = self
  if file:isFile() then
    file = file:getParentDir()
  end

  if type(to) == "table" then
    table.insert(to, 1, file:getLocalPath())
  else
    to = {file:getLocalPath(), to}
  end

  return File:new{
    workspaceName = self.workspaceName,
    workspacePath = self.workspacePath,
    path = to,
  }
end

function File:getName( withoutExtension )
  local name = self.path:reverse():match"^[^/]+":reverse()
  if withoutExtension then
    local ext = self:getExtension()
    if #ext > 0 then
      name = name:sub(1, -#ext-2)
    end
  end
  return name
end

function File:getExtension()
  return (self.path:reverse():match("^([^./]+)%.") or ""):reverse():lower()
end

function File:open( mode )
  return filesystem.open( self:getPath(), mode )
end

function File:write( contents, append )
  utils.writeFile( contents, self:getPath(), append )
end

function File:run( ... )
  return run( self:getPath(), ... )
end

function File:readLines()
  local f = self:open"r"
  local lines = {}
  while true do
    local line = f.readLine()
    if not line then break end
    table.insert(lines, line)
  end
  f.close()
  return lines
end

function File:readAll()
  local f = self:open"r"
  local all = f.readAll()
  f.close()
  return all
end

function File:exists()
  return filesystem.exists( self:getPath() )
end

function File:copy( to )
  if isClass( to ) and to:isA( File ) then
    to = to:getPath()
  end
  return filesystem.copy( self:getPath(), to )
end

function File:delete()
  return filesystem.delete( self:getPath() )
end

function File:rename( to )
  if isClass( to ) and to:isA( File ) then
    to = to:getPath()
  end
  return filesystem.rename( to )
end

function File:mkDir()
  return filesystem.mkDir( self:getPath() )
end

function File:mkDirs()
  return filesystem.mkDirs( self:getPath() )
end

function File:isDir()
  return filesystem.isDir( self:getPath() )
end

function File:isFile()
  return self:exists() and not self:isDir()
end

--supported types only
function File:isImage()
  return advancedMarcros.utils.map( 
    image.getFormats().readers, 
    function(k,v) 
      return v:lower(), true 
    end, true )[ self:getExtension() ]
end

function File:listNames()
  local names = filesystem.list( self:getPath() )
  table.sort( names )
  return names
end

function File:list( filter )
  local files = self:listNames()
  local out = {}
  for i, name in ipairs( files ) do
    local f = File:new{
      workspaceName = self.workspaceName,
      workspacePath = self.workspacePath,
      path = {self.path, name},
    }
    if not filter 
    or (type(filter) == "function" and filter(f))
    or (type(filter) == "string" and name:match(filter)) then
      table.insert( out, f )
    end
  end

  return out
end

--if the workspace is exited the workspace is the same, but the path will contain /..
function File:getParentDir( workspaceExitAllowed )
  if pcall( filesystem.getParentDir( self:getPath() )) then
    return File:new{
      workspaceName = self.workspaceName,
      workspacePath = self.workspacePath,
      path = {self.path, ".."},
    }
  end
  return false
end

function File:getWorkspace()
  return {
    path = self:getWorkspacePath(),
    name = self:getWorkspaceName(),
  }
end

function File:getWorkspacePath()
  return self.workspacePath
end

function File:getWorkspaceName()
  return self.workspaceName
end

function File:withWorkspace( newWorkspace, name )
  if isClass(newWorkspace) and newWorkspace:isA(File) then
    newWorkspace = newWorkspace:getPath()
  end
  newWorkspace = newWorkspace:gsub(filesystem.separator, "/"):match("(.+)[^/]?") --don't include ending slash if present
  
  local oldParts = self:getPathParts() --full file path
  local newParts = {}                  --workspace only
  for part in newWorkspace:gmatch"[^/]+" do
    table.insert(newParts, part)
  end
  
  if oldParts[1] ~= newParts[1] and oldPath:match"[^:]+:/" then
    error("Can't backpath to a different drive on Windows", 2)
  end

  local backPathing = {}
  local matchFailed = false
  local i, j = 1, 1
  while true do
    if not matchFailed and oldParts[ i ] == newParts[ j ] then
      i = i + 1
      j = j + 1
    elseif newParts[ j ] then
      matchFailed = true

      table.insert(backPathing, "..")
      j = j + 1
    end
  end

  local newPath = backPathing
  while oldParts[i] do
    table.insert(newPath, oldParts[i])
    i = i+1
  end
  
  return File:new{
    workspaceName = name,
    workspacePath = newWorkspace,
    path = newPath
  }
end

function File:equals( otherFile )
  return self:getPath() == otherFile:getPath()
end

function File:toJson()
  local JsonObject = require"JsonObject"
  local out = JsonObject:new()
  out:put("workspaceName", self.workspaceName)
  out:put("workspacePath", self.workspacePath)
  out:put("path",          self.path)
  return out
end

File.static = {
  profileDir = File:new{
    --workspacePath default to macros address,
    path = "../profiles"
  },
  macrosDir = File:new{
    --workspacePath default to macros address,
    path = "../macros"
  },
  workspaceDir = File:new{
    --workspacePath default to macros address,
    path = "../workspaces"
  }
}

File.static.workspaceDir:mkDirs()
package.preload["File"] = File

return File