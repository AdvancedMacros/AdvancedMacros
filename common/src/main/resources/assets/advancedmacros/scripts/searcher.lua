if(#package.path <= #("?.lua"))then
  local paths = {
    filesystem.getMacrosAddress().."\\libs\\?.lua",
    filesystem.getMacrosAddress().."\\libs\\?\\init.lua",
   -- "C:\\Program Files (x86)\\Lua\\5.1\\clibs\\?.dll"
  }
  package.path = ".\\?.lua;"..package.path
  for a,b in pairs(paths) do
    package.path = package.path .. ";" .. b
  end
end

--{name: Workspace} loaded from file
local workspaceCache = {}

--{fullFilePath: module}
package.globalLoaded = {}

function package.invalidateWorkspaceCache()
  workspaceCache = {}
end

--unload all instances of `module` across all workspaces
function package.unload(module, workspace)
end

local function getCurrentWorkspace()
  local Workspace = package.preload["Workspace"]
  return Workspace:new(advancedMacros.getWorkspace())
end

---Get Workspace by name, throws error if workspace file doesn't exist
---Workspace is cached in workspaceCache
---@param name string
---@return Workspace|nil
local function getWorkspaceByName(name)
  local cached = workspaceCache[name]
  if cached then return cached end
  local Workspace = package.preload["Workspace"]
  local workspace = Workspace:load(name)
  workspaceCache[name] = workspace
  return workspace
end

---path to workspace + / + module name
local function getGlobalModuleName(name, workspaceName)
  local path
  if workspaceName then
    path = getWorkspaceByName(workspaceName).workspacePath
  else
    path = getCurrentWorkspace().workspacePath
  end
  return path.."/"..name
end

local workspaceLoads = {}
---@return table loaded unique table for the thread's workspace
local function getWorkspaceLoads()
  local workspacePath = getCurrentWorkspace().workspacePath
  if not workspaceLoads[workspacePath] then
    workspaceLoads[workspacePath] = {}
  end
  return workspaceLoads[workspacePath]
end

--0. loaded (with meta-events)
--1. preload
--2. lua file
--3. 

--add metatable to package.loaded so it's workspace local
--{path: {globalModuleName: module,...}, ...}
local loadedMeta = {
  __index = function(t, k)
    return getWorkspaceLoads()[k]
  end,
  __newindex = function (t, k, v)
    getWorkspaceLoads()[k] = v
  end,
  __pairs = function()
    return pairs(getWorkspaceLoads())
  end,
  __ipairs = function()
    return ipairs(getWorkspaceLoads())
  end,
}
setmetatable(package.loaded, loadedMeta)

-- ---Searcher for package.preload, if workspace is defined and ~= "internal" then this searcher is skipped
-- ---@param name string name of the module to load
-- ---@param workspace string|nil name of the workspace
-- ---@return function|string|nil loader function to load the module or string error or nil if unavailable
-- local function preloadSearcher(name, workspace)
--   local preload = package.preload[name]
--   if preload then
--     return function()
--       return preload
--     end
--   end
--   return ("no field package.preload['%s']"):format(name)
-- end

---Searcher for lua files
---files can be from resources or user 
---reserved workspace name "internal" will use getResource
---@param name string name of the module to load
---@param workspace string|nil name of the workspace which is ignored in preload searcher
---@return function|string|nil loader function to load the module or string error or nil if unavailable
local function luaSearcher(name, workspaceName)
  local workspace

  if not workspaceName then
    workspace = getCurrentWorkspace()
  else
    workspace = getWorkspaceByName(workspaceName)
  end

  if not workspace then
    return "no such workspace '"..tostring(workspaceName).."'"
  end

  if workspace.workspaceName == "internal" then
    local src =  advancedMacros.getResource("scripts/"..name:lower()..".lua")
              or advancedMacros.getResource("gui/"..name:lower()..".lua")

    if src then
      return load(src, "resource:"..name, "t", _G)
    end
    return ([[resource "%s" doesn't exist]]):format(name)
  end

  local attempts = {}
  --search package.path options
  for pattern in package.path:gsub("\\","/"):gmatch"[^;]+" do
    local file = workspace:navigate( pattern:gsub("?", name) )
    if file:exists() then
      return load(file:readAll(), workspace.workspaceName.."::"..name, "bt", _G ) --TODO sandboxing options / access permissions
    end
    table.insert(attempts, ("'%s' not found"):format(file:getPath()))
  end
  
  return table.concat(attempts, "\n")
end

--     .searchers[1] = preload, default behaviour
package.searchers[2] = luaSearcher
--     .searchers[3] = java searcher
--     .searchers[4] = jar lib searcher

----------


-- function loader(name)
--   local name = "/"..filesystem.resolve(name, 2)
--   local nameL = name..".lua"
  
--   if filesystem.exists( nameL ) then
--     return function() return run( nameL ) end
--   end
--   if filesystem.exists( name ) then
--     return function() return run( name ) end
--   end
  
--   --log( filesystem.resolve(name, 3))
--   return nil
-- end

-- package.searchers[#package.searchers+1] = loader

--if not package.cpath then
--  package.cpath = 
--    ".\?.dll;"..
--    ".\?51.dll;"..
--    "C:\Program Files (x86)\Lua\5.1\?.dll;"..
--    "C:\Program Files (x86)\Lua\5.1\?51.dll;"..
--    "C:\Program Files (x86)\Lua\5.1\clibs\?.dll;"..
--    "C:\Program Files (x86)\Lua\5.1\clibs\?51.dll;"..
--    "C:\Program Files (x86)\Lua\5.1\loadall.dll;"..
--    "C:\Program Files (x86)\Lua\5.1\clibs\loadall.dll"
--end
--log(package.path)
