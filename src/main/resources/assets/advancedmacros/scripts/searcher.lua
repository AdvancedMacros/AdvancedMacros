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

function loader(name)
  local name = "/"..filesystem.resolve(name, 2)
  local nameL = name..".lua"
  
  if filesystem.exists( nameL ) then
    return function() return run( nameL ) end
  end
  if filesystem.exists( name ) then
    return function() return run( name ) end
  end
  
  --log( filesystem.resolve(name, 3))
  return nil
end
package.searchers[#package.searchers+1] = loader

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
