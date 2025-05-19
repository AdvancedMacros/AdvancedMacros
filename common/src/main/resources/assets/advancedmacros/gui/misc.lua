local utils = advancedMacros.utils
local File = require"File"

local misc = {}

misc.LMB = 0
misc.MMB = 2
misc.RMB = 1

-- local MACROS_FOLDER = filesystem.getMacrosAddress():sub(1,-#("macros")-1)
misc.profileDir = File:new{
  --workspacePath default to macros address,
  path = "../profiles"
}
misc.macrosDir = File:new{
  --workspacePath default to macros address,
  path = "../macros"
}
misc.workspaceDir = File:new{
  --workspacePath default to macros address,
  path = "../workspaces"
}

misc.workspaceDir:mkDirs()

function misc.colorToJsonValue( color )
  if type(color) == "number" then
    return color
  elseif type(color) == "table" then
    if color.r then
      local json = require"JsonObject":new()
      json:put("r", color.r)
      json:put("g", color.g)
      json:put("b", color.b)
      json:put("a", color.a)
      return json
    else
      local array = require"JsonArray":new()
      for i = 1, 4 do
        array:put( color[i] )
      end
      return array
    end
  end
end

function misc.lookupWorkspaceName( path )
  return utils.inverse( getSettings().workspaces )[ path ]
end

function misc.randomColor()
  return { utils.hsvToRgb( math.random() * 360, 1, .7, 1 ) }
end

function misc.resScale( screen )
  local sw,sh,pw,ph = screen.getSize()
  return math.max( (pw/sw), (ph/sh) )
end

function misc.trimColor( color )
  local h,s,v,a = utils.rgbToHsv( table.unpack( color ))
  s = s * .5
  v = 1 - ( (1-v) * .5 )
  return { utils.hsvToRgb( h,s,v,a ) }
end

function misc.darkenColor( color )
  local h,s,v,a = utils.rgbToHsv( table.unpack( color ))
  v = v * .6
  return { utils.hsvToRgb( h,s,v,a ) }
end

function misc.listProfiles()
end

return misc