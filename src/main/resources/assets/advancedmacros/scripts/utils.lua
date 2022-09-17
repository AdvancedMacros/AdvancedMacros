local utils = {}
advancedMacros.utils = advancedMacros.utils or utils
--type, value, funcName, arg
function utils.assertType( t, v, f, a, level )
  f = f or "?"
  a = a or "?"
  level = (level or 1) -1
  if type(t)=="table" then
    local exp = "{"
    for i,ty in pairs(t) do
      if type(v)==ty then return end
      exp = exp.. (#exp>0 and "," or "") .. ty
    end
    exp = exp.."}"
    error(("'%s' arg %s expected %s, got '%s'"):format(
      f, a, exp, type(v)
    ),3 + level)
  elseif type(v)~=t then
      error(("'%s' arg %s expected %s, got '%s'"):format(
        f, a, t, type(v)
      ),3 + level)
  end
end

function utils.keys( tbl )
  if type(tbl) ~= "table" then error("utils.keys expected table, got "..type(tbl),2) end
  local out = {}
  for a in pairs( tbl ) do
    out[#out+1] = a
  end
  return out  
end
function utils.values( tbl )
  if type(tbl) ~= "table" then error("utils.values expected table, got "..type(tbl),2) end
  local out = {}
  for a,b in pairs( tbl ) do
    out[#out+1] = b
  end
  return out
end
function utils.asSet( ... )
  local tmp = {}
  for _, tbl in ipairs{...}  do
    for a,b in ipairs(tbl) do
      tmp[b] = true
    end
  end
  return utils.keys(tmp)
end
function utils.inverse( tbl )
  local out = {}
  for a,b in pairs( tbl ) do
    out[b] = a
  end
  return out
end
--like javascript's reduce function
function utils.reduce( tbl, f, i )
  local k = next(tbl)
  if not k then return i end
  if not i then
    i = tbl[k]
    k = next(tbl, k)
  end
  while k do
    i = f(i, tbl[k])
    k = next(tbl, k)
  end
  return i
end

function utils.clone( x, deep )
  local tx = type(x)
  if tx=="table" then
    local g = {}
    for a,b in pairs(x) do
      local tb=type(b)
      if tb=="table" then
        if deep then
          g[a] = t.clone(b, deep)
        else
          g[a] = b
        end
      elseif tb~="function" then
        g[a] = b
      end
    end
    return g
  else
    return x
  end
end
--for combining tables with numbered index
function utils.append( a, b, inPlace )
  out = inPlace and a or {}
  if not inPlace then
    for i,j in ipairs(a) do
      table.insert(out, j)
    end
  end
  for i,j in ipairs(b) do
    table.insert(out, j)
  end
  return out
end

-- copy a table while applying a mapping function to change it's values
-- optionally also changing the keys
function utils.map( tbl, map, mapsKeys )
  local out = {}
  for a,b in pairs(tbl) do
    local c,d = map(a,b)
    if mapsKeys then
      if c==nil then error("nil key for index ["..tostring(a).."]",2) end
      out[c] = d
    else
      out[a] = c
    end
  end
  return out
end

function utils.pickRandom( options, useKeys )
  if not options then error("missing options...",2) end
  if useKeys then
    local k = utils.keys( options )
    return options[ k[ math.random(#k) ] ]
  else
    if #options == 0 then
      return nil
    end
    return options[ math.random( #options ) ]
  end
end

--present in math as math.map from morefunc
-- function utils.mathMap(x,a,b,c,d)
--   return (x-a)/(b-a) * (d-c) + c
-- end

function utils.startsWith( a, b, caseSens )
  if type(b)~="string" then
    error("Expected string b", 2)
  end
  if type(a)=="table" then
    local out = {}
    for i,j in ipairs(a) do
      if utils.startsWith(j,b) then
        table.insert(out, j)
      end
    end
    return out
  end
  return caseSense and a:sub(1,#b)==b or
    a:lower():sub(1,#b)==b:lower()
end

--check if a list of strings contains some text somewhere in any of them
--returns all matches, used for autocompletion
function utils.hasText( a, b )
  if type(a)=="table" then
    local out = {}
    for i,j in ipairs(a) do
      if utils.hasText(j,b) then
        table.insert(out,j)
      end
    end
    return out
  end
  local val = a:find(b,1,true)
  return not not val
end

--search a table for x
function utils.inTable( x,tbl )
  utils.assertType("table", tbl, "utils.inTable",2)
  for a,b in pairs(tbl) do
    if b==x then return true end
  end
  return false
end

--format time in seconds to something pretty
function utils.fTime( t, format )
  t = math.floor(t)
  local s=t%60
  t=t/60
  local m=t%60
  t=t/60
  local h=t%24
  t=math.floor(t/24)
  local out = {}
  if t>0 then
    out[1]=t.." days"
  end
  out[#out+1] = (format or "%02d:%02d:%02d"):format(h,m,s)
  return table.concat(out," ")
end

--------------------------------------------------------------------------------------------------------------------------------------------------------

function utils.printf(fstr,...)
  print(fstr:format(...))
end

function utils.split(text, delims)
  utils.assertType("string",text,"split",1)
  utils.assertType("string",delims,"split",2)
  local out = {}
  
  for a in text:gmatch("[^"..delims.."]+") do
    table.insert(out,a)
  end
  return out
end

function utils.charArray( str )
  local out = {}
  for i=1,#str do
    out[i] = str:sub(i,i)
  end
  return out
end

--lowercase, was used with autocompletion in mind for 
--case insensitive matches of multiple words
local assertType = utils.assertType
function utils.splitWords( text, lower )
  assertType( "string", text, "splitWords", 1)
  
  local out = {}
  for a in text:gmatch"([^ ]+)" do
    out[#out+1] = lower and a:lower() or a
  end
  return out
end

function utils.capitalizeWords( str )
  str = utils.splitWords( str, true )
  for i,s in ipairs( str ) do
    str[i] = str[i]:sub(1,1):upper() .. str[i]:sub(2)
  end
  return table.concat( str, " " )
end

--for use with blit on cc, but copied over anyway
--lets you impose one string on another
function utils.maskStr( base, mask, start )
  start = start or 1
  return base:sub(1,start-1) .. mask .. base:sub( start + #mask )
end

function utils.matchesOption( name, option )
  assertType("string",  name,"matchesOption",1)
  assertType("string",option,"matchesOption",2)
  local inWords = manage.splitWords(name, true)
  local opWords = manage.splitWords(option, true)
  for _,iWord in pairs( inWords ) do
    local found = false
    for _,oWord in pairs( opWords ) do
      if oWord:find( iWord ) then
        found = true
        break
      end
    end
    if not found then return false end
  end
  return true
end
--available item names
function utils.autoComplete( text, allOpts )
  assertType( "string", text, "autoComplete", 1 )
  
  local autos = {}
  for _,opt in pairs( allOpts ) do
    if utils.matchesOption( text, opt ) then
      autos[#autos+1] = opt 
    end
  end
end

function utils.reduce( tbl, f, i )
  local k = next(tbl)
  if not k then return i end
  if not i then
    i = tbl[k]
    k = next(tbl, k)
  end
  while k do
    i = f(i, tbl[k])
    k = next(tbl, k)
  end
  return i
end

function utils.clone( x, deep )
  local tx = type(x)
  if tx=="table" then
    local g = {}
    for a,b in pairs(x) do
      local tb=type(b)
      if tb=="table" then
        if deep then
          g[a] = utils.clone(b, deep)
        else
          g[a] = b
        end
      elseif tb~="function" then
        g[a] = b
      end
    end
    return g
  else
    return x
  end
end

function utils.writeFile( str, file, append )
  local f = filesystem.open( file or "debugOutput.txt", (append == true) and "a" or "w" )
  if type(str) == "table" then 
    str = utils.serializeOrdered( str )
  end
  f.writeLine( str )
  f.close()
end

function utils.multiGet( tbl, keys, unpack )
  local out = {}
  for i,k in ipairs(keys) do
    if unpack then
      table.insert( out, tbl[k] )
    else
      out[k] = tbl[k]
    end
  end
  if unpack then
    return table.unpack( out )
  end
  return out
end

local function argSplit( arg )
  local argTypes, default = {},nil
  local names = {false}
  local requiredTypes = {}
  for k,v in pairs( arg ) do
    if k==1 then
      default = v
    elseif type(k) == "string" then
      names[1] = k
      argTypes = v
    elseif type(k) == "number" then
      if type(v) == "table" then
        if not v[1] then
          error("formatting error",3)
        end
        requiredTypes[v[1]] = v[2]
        table.insert(names, v[1])
      else
        table.insert(names, v)
      end
    end
  end
  return names, argTypes, default, requiredTypes
end

--typical usage
--function foo( ... )
--  local args = utils.kwargs("foo", {
--    {x="number",0,"aliasOfX"},
--    {y={"string","boolean"},'ok'}
--  }, ...)
--  ...
--end
-- -- args"x" will tell you if an alias was used
--
--foo{x=100}
--foo{aliasOfX=100}
--foo{}
--foo{100,200}
--foo{y='h'}
--foo{100, x=3, aliasOfX=4} --very error

function utils.kwargs( argInfo, ... )
  local fName = debug.getinfo(2,"n").name
  local params = {...}
  if select("#",...) > 1 or type(params[1]) ~= "table" then error(("Incorrect usage, expected %s{...}, got %d params"):format(fName, select("#",...)), 3) end
  local params = params[1]

  for k in pairs(params) do
    if not utils.inTable(type(k), {"string","number"}) then
      error("only string or number keys are valid for kwargs",3)
    end
  end

  local used = {}
  local out = {}
  local metaInfo = {}
  local aliasLookup = {}

  local asserts = argInfo.asserts or {}

  for i, arg in ipairs(argInfo) do
    local argNames, argTypes, default, requiredTypes = argSplit( arg )
    
    if not argNames[1] then
      error(("Arg %d needs a name/type def"):format(i),2)
    end

    local requiredType
    table.insert( argNames, i )
    for i,argName in ipairs( argNames ) do
      if argName and used[argName] then
        error(("The variable '%s' is refered to more than once in the function definition"):format(argName),2)
        end
        used[argName] = true
      end
      
    do --check against mutliple possible matches ie foo{100, arg=101} where arg is arg 1
      if #utils.keys(utils.multiGet( params, argNames )) > 1 then
        error(("Duplicate argument for index #%d and key '%s'"):format(i, argNames[1]),3)
      end
    end
    local v
    local usedName = argNames[1]
    for _, name in ipairs( argNames ) do
      v = params[name]
      --print( tostring(name)..": "..tostring(v))
      usedName = name
      if v~=nil then break end
    end
    if type(argTypes) == "string" then
      argTypes = {argTypes}
    end
    if type(argTypes) ~= "table" then
      error(("arg types for '%s' need to be a string or list of strings"):format(argNames[1]),2)
    end
    if v == nil and not utils.inTable("nil", argTypes) then
      v = default
    end

    --type checks
    if requiredTypes[usedName] ~= nil then   -- has override
      if type(requiredTypes[usedName]) == "string" then -- force table
        requiredTypes[usedName] = {requiredTypes[usedName]}
      elseif type(requiredTypes[usedName])=="table" then --override must be table
        error(("Alias [%s] of [%s](%d) has a type restriction as type '%s', expected nil, string, or table of strings"):format(usedName, argNames[1],i, type(requiredTypes)),2)
      end
      if  not utils.inTable("*", argTypes) and not utils.typeMatches(v, requiredTypes[usedName]) then
      -- if not utils.inTable(type(v), requiredTypes[usedName]) then
        error(("When using arg %d (%s) as [%s], the type is restricted to '%s', got '%s'"):format(i, argNames[1], usedName, utils.serializeOrdered(requiredTypes[usedName]), type(v)),3)
      end
    elseif not utils.inTable("*", argTypes) and not utils.typeMatches( v, argTypes ) then
      error(("arg [%s](%s) was expected to be of type(s) %s, got %s"):format(usedName,utils.serializeOrdered(argNames):sub(2,-2), utils.serializeOrdered(argTypes), type(v)), 3 )
    end
    out[ argNames[1] ] = v
    out[ usedName ] = v
    metaInfo[argNames[1]] = usedName
    aliasLookup[usedName] = argNames[1]
  end

  metaInfo.__call = function( t, k )
    return metaInfo[k]
  end
  -- metaInfo.__index = function( t, k )
  --   if rawget(t, k) then return rawget(t, k) end
  --   return rawget(t, aliasLookup[k])
  -- end
  setmetatable(out, metaInfo)
  return out
end

--type(x) == "number" or type(x) =="string" or type(x) =="table"
--vs
--utils.typeMatches(x, {"number","string","table"})
--also supports checking if a table is a class when the type starts with "class:"
function utils.typeMatches( value, options )
  local typeName = type(value)
  for _, opt in ipairs(options) do
    if typeName == opt then return true end
    if opt:sub(1,6) == "class:" then
      local cName = opt:sub(7)
      local ok, cl = pcall( require,  cName )
      if ok and isClass( cl ) then
        if value:isA( cl ) then
          return true
        end
      end
    end
  end
  return false
end

--table serialization, optional sortFunc, visited for recursion ignore
function utils.serializeOrdered( tbl, sortFunc, visited )
  if type(tbl)~="table" then return type(tbl)=="string" and ('"'..tostring(tbl)..'"') or tostring(tbl) end
  visited = visited or {}
  if visited[tbl] then
    return tostring(tbl)
  end
  visited[tbl] = true
  local out = { "{" }
  local keys = utils.keys(tbl)
  table.sort( keys, sortFunc or function( a,b )
    if type(a)~=type(b) then
      return type(a)<type(b)
    end
    return a<b
  end ) --sortFunc is optional
  for i,v in ipairs( tbl ) do
    if #out > 1 then table.insert( out, ', ' ) end
    table.insert( out, utils.serializeOrdered(v) )
  end
  for i,k in ipairs( keys ) do
    if type(k)~="number" then
      local v = tbl[k]
      local tv = type(v)
      if #out > 1 then table.insert( out, ', ' ) end
      table.insert( out, k )
      table.insert( out, ' = ' )
      table.insert( out,  utils.serializeOrdered(v, sortFunc))
    end
  end
  table.insert(out,"}")
  return table.concat(out)
end

--returns value from `try` function or `catch` function if try error'd
--catch receives the stack trace from xpcall as an arg
--finally is allways called last if present
function utils.tryCatch( ... )
  local args = utils.kwargs({
    { try="function" },
    { catch={"nil","function"}, function() end },
    { finally={"nil","function"} },
    { args={"nil","table"}}
  },...)
  local r2
  local r = {
    xpcall( args.try, function(...) 
      r2 = {args.catch(...)} 
    end )
  }
  local ret = {}
  if r[1] then
    ret = {select(2,table.unpack(r))}
  end
  if args.finally then
    finally()
  end
  return table.unpack( ret or r2)
end

--[option] = weight, weight
function utils.weightedRandom( options )
  if type(options) ~= "table" then
    error("expected options table, got "..type(options),2)
  end
  local totalWeight = 0
  local keys = utils.keys( options )
  if #keys == 0 then return nil end
  if #keys == 1 then return keys[1] end

  for i=1, #keys do
    totalWeight = totalWeight + options[keys[i]]
  end
  local r = math.random()
  for option, weight in pairs( options ) do
    if r < weight then
      return option
    end
    r = r + weight
  end
  --safety case
  return keys[#keys]
end

function utils.inRect( testX, testY, boxX, boxY, boxWidth, boxHeight )
  utils.assertType("number", testX, "utils.inRect",1)
  utils.assertType("number", testY, "utils.inRect",2)
  utils.assertType("number", boxX, "utils.inRect",3)
  utils.assertType("number", boxY, "utils.inRect",4)
  utils.assertType("number", boxWidth, "utils.inRect",5)
  utils.assertType("number", boxHeight, "utils.inRect",6)
  return 
    ( boxX <= testX ) and
    ( boxY <= testY ) and
    ( testX < boxX + boxWidth ) and
    ( testY < boxY + boxHeight )
end