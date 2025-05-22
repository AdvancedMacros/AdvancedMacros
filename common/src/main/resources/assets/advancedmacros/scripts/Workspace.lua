local utils = advancedMacros.utils

local Workspace = newClass("Workspace")

--controls class wrapper for Workspace
function Workspace:new( controls )
  local obj = Workspace._new( self )

  obj.controls = controls
  for k, v in pairs(controls) do
    obj[k] = function(self, ...)
      return v(...)
    end
  end

  return obj
end

function Workspace:toFile()
  local File = package.preload["File"]
  return File:new{
    workspaceName = self:getName(),
    workspacePath = self:getPath(),
    path = "."
  }
end

package.preload["Workspace"] = Workspace

return Workspace