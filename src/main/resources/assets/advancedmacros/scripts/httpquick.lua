local statusHTTP = function(r, logjson, length)
    assert(type(r)=="table", "expected httpQuick r")
    length = logjson and length or 1024

    log("&7> From &8"..r.url)
    log("&7> Got "..r.type.." ["..r.code.."]")
    log("&7> Bytes "..r.length.." Time "..r.time.."ms "..r.headers.http)
    if r.json and logjson and r.length < length then
        log(r.json)
    end
end

local saveHTTP = function(response, file)
    assert(type(response)=="table", "expected httpQuick response")
    local slash = file and file:byte(1) or false; -- log(slash)
    if slash ~= 47 and slash ~= 92 and slash ~= 126 then
        local ext = response.url:match("/([^/]+)/?$")
        ext = ext.."."..response.type:match("/(.+)")
        file = file or os.date("--%m-%d_%H.%M.%S_")..ext
        filesystem.mkDir("~/downloads")
        file = "~/downloads/"..file
    end

    print("Saved file: "..file)
    local file = filesystem.open(file, "w")
    file.write( response.response )
    file.close()
end

local parseJSON = function(response)
    if response.type:match "application/json" then
        pcall(function()
            local json = require 'json'
            response.json = json.parse(response.response)
            return response.json
        end)
    else
        response.json = false
        return false
    end
end

local buildHTTP = function(req)
    req = req or {}
    assert(not req.url, "can't override url by req")
    req.requestProperties = req.requestProperties or {}

    local prop = req.requestProperties
    local agent = "AdvancedMacros/".._MOD_VERSION

    if not prop['user-agent'] then
        prop['user-agent'] = agent
    end

    if req.followRedirects == nil then
        req.followRedirects = true
    end

    if req.out then
        req.doOutput = true
        prop.requestMethod = 'POST'
        prop['Content-type'] = req.type or 'text/plain'
    end

    return req
end

local httpQuick = function(url, req)
    assert(type(url)=="string","url missing")

    local request = buildHTTP(req)
    local response = {}
    request.url = url

    local time = os.millis()
    local connection = httpRequest(request)
    if request.doOutput then
        local out = connection.output()
        out.write(req.out)
        out.flush()
        out.close()
    end

    response.url = request.url:gsub("%?.+$","")
    response.lenght = connection.getContentLength()
    response.code   = connection.getResponseCode() 
    response.type  = connection.getContentType()

    local input = connection.err() or connection.input()
    response.headers = connection.getHeaderFields()
    response.response = input.getContent()
    response.time = os.millis() - time
    connection.disconnect()

    if response.lenght == -1 then 
        response.lenght = #response.response
    end

    response.status = statusHTTP
    response.parse = parseJSON
    response.save = saveHTTP
    return response
end

package.loaded.httpQuick = httpQuick
return httpQuick
