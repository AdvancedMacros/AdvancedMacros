if(#package.path <= #("?.lua"))then
  local paths = {
    -- filesystem.getMacrosAddress().."\\libs\\?.lua",
    -- filesystem.getMacrosAddress().."\\libs\\?\\init.lua",
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

---path to workspace + / + module name
---@param name string name of module
---@param workspace Workspace the workspace
local function getGlobalModuleName(name, workspace)
  return workspace:navigate(name):getPath()
end

---unloads all instances of `module` across all workspaces & removes from package.globalLoaded
---@param module string name of the module
---@param workspace nil|string|Workspace name or instance of workspace, if nil then defaults to current workspace
function package.unload(module, workspace)
  if workspace == nil then
    workspace = getCurrentWorkspace()
  elseif type(workspace)=="string" then
    workspace = getWorkspaceByName(workspace)
  elseif instanceOf(workspace, package.preload["Workspace"]) then
    --no action needed
  else
    error("invalid arg 2 [workspace], expected nil|string|class:Workspace, got "..type(workspace))
  end

  local globalName = getGlobalModuleName(module, workspace)
  local loaded = package.globalLoaded[globalName]
  package.globalLoaded[globalName] = nil
  for path, cache in pairs(workspaceLoads) do
    for k, v in pairs(cache) do
      if v == loaded then
        cache[k] = nil
        break
      end
    end
  end
end

local function getCurrentWorkspace()
  return advancedMacros.getCurrentWorkspace()
end

---Get Workspace by name, throws error if workspace file doesn't exist
---Workspace is cached in workspaceCache
---@param name string
---@return Workspace|nil
local function getWorkspaceByName(name)
  local cached = workspaceCache[name]
  if cached then return cached end
  local Workspace = package.preload["Workspace"]
  local workspace = advancedMacros.getWorkspace(name)
  -- if name == "internal" then
  --   workspace = Workspace:new{
  --     workspaceName = "internal",
  --     workspacePath = "resource:"
  --   }
  -- else 
  --   workspace = Workspace:load(name)
  -- end
  workspaceCache[name] = workspace --TODO remove caching feature?
  return workspace
end



---wrapper function to pull from `package.globalLoaded`
---makes it so if nested workspaces refer to the same file it's only loaded once
---TODO setting/workspace permission?
---@param path string value from `getGlobalModuleName`
---@param loader function loader function for the module
---@return function wrappedLoader loader that stashes it's returned value in package.globalLoaded
local function cacheGlobalModule(path, loader)
  return function()
    local value = package.globalLoaded[path]
    if not value then
      value = loader()
      package.globalLoaded[path] = value
    end
    return value
  end
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

  local globalName = getGlobalModuleName(name, workspace)
  local gLoaded = package.globalLoaded[globalName]
  if gLoaded then return gLoaded end

  if workspace.workspaceName == "internal" then
    local src =  advancedMacros.getResource("scripts/"..name:lower()..".lua")
              or advancedMacros.getResource("gui/"..name:lower()..".lua")

    if src then
      return cacheGlobalModule(globalName, load(src, "resource:"..name, "t", _G))
    end
    return ([[resource "%s" doesn't exist]]):format(name)
  end

  local attempts = {}
  --search package.path options
  for pattern in package.path:gsub("\\","/"):gmatch"[^;]+" do
    local file = workspace:navigate( pattern:gsub("?", name) )
    if file:exists() then
      return cacheGlobalModule(globalName, load(file:readAll(), workspace.workspaceName.."::"..name, "bt", _G )) --TODO sandboxing options / access permissions
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
