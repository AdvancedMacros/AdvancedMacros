if(getSettings()==nil or getSettings().bindingsMenu ==nil)then return end
local color = getSettings().bindingsMenu.profileSelect.colors.text
color = color or {1, 1, 1, 1} --rgba --{a=255, r=255,g=255, b=255}
getSettings().bindingsMenu.profileSelect.colors.text = color

local settings = getSettings()

local function convertColorMode( tab )
  if tab.r and tab.g and tab.b and tab.a then
    tab[1] = tab.r / 255
    tab[2] = tab.g / 255
    tab[3] = tab.b / 255
    tab[4] = tab.a / 255
    tab.r, tab.g, tab.b, tab.a = nil, nil, nil, nil
    return
  end
  for a,b in pairs( tab ) do
    if type(b)=="table" then
      convertColorMode( b )
    end
  end
end

convertColorMode(settings)

getSettings().save()