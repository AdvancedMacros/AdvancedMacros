import requests
import json
import infoUtil
import os
import sys

HOST = "https://minecraft.curseforge.com"

CF_TOKEN = os.getenv("CURSE_FORGE_API_TOKEN")
if not CF_TOKEN:
    raise Exception("CURSE_FORGE_API_TOKEN not found")

def getVersions():
    response = requests.get(
        f"{HOST}/api/game/versions", 
        headers={"X-Api-Token": CF_TOKEN}
    )
    if response.status_code == 200:
        return json.loads(response.text)
    raise Exception( f"{response.status_code} {response.text}" )

def getVersionTypes():
    response = requests.get(
        f"{HOST}/api/game/version-types", 
        headers={"X-Api-Token": CF_TOKEN
    })
    if response.status_code == 200:
        types = json.loads(response.text)
        lookup = {}
        for t in types:
            lookup[t["id"]] = t
        return lookup
    raise Exception( f"{response.status_code} {response.text}" )

def chooseVersionTags( mcVersion, platform ):
    branch = os.getenv("github.event.pull_request.base.ref", os.getenv("GITHUB_REF"))
    if branch.startswith("refs/heads/"):
        branch = branch[len("refs/heads/"):]

    branchPattern = branch.replace("x",r"\d+").replace(".", r"\.")
    if not infoUtil.checkRegexMatch(mcVersion, branchPattern):
        raise Exception(f"Target branch {branch} doesn't match current version defined in AM {mcVersion} (using pattern `{branchPattern}`)")

    versions = getVersions()
    types = getVersionTypes()

    tags = []
    tagNames = []
    for v in versions:
        name = v["name"]
        id = v["id"]
        tinfo = types.get( v["gameVersionTypeID"] )
        typeName = tinfo["name"] if tinfo else "?"
        tagName = f"{name} ({typeName})"

        if branch.startswith("1.12.") or \
           branch.startswith("1.14.") or \
           branch.startswith("1.15."):
            if name == "Forge" and platform.lower() != "fabric":
                tags.append( id )
                tagNames.append(tagName)
                continue
        else:
            if name == "NeoForge" and platform.lower() != "fabric":
                tags.append( id )
                tagNames.append(tagName)
                continue

            if platform.lower() != "forge" and platform == name:
                tags.append( id )
                tagNames.append(tagName)
                continue

        if name.startswith("Java "):
            if int( name[5:] ) >= 16:
                tags.append( id )
                tagNames.append(tagName)
           
        if name == mcVersion:
            tags.append( id ) #includes 1, 'addons' and version type, idk which are needed
            tagNames.append(tagName)

    print("Tags:")
    print(tagNames)
    return tags
    

def upload_file(project_id, metadata, fileName, filePath):
    url = f"{HOST}/api/projects/{project_id}/upload-file"

    metadataJson = json.dumps(metadata)
    data = {
        'metadata': metadataJson,
        fileName : open(filePath, 'rb')
    }

    response = requests.post(
        url, 
        files=data,
        headers = {
            "X-Api-Token": CF_TOKEN
        }
    )

    if response.status_code == 200:
        print("File uploaded successfully.")
        print(response.text)
    else:
        print("Failed to upload file.")

def chooseJar( libsDir ):
    for opt in os.listdir( libsDir ):
        if opt.endswith("1.0.0.jar"):
            return libsDir + os.sep + opt 
    raise Exception("no jar selected")

if __name__ == "__main__":
    platform = sys.argv[1]
    libsDir = sys.argv[2]
    filePath = chooseJar( libsDir )


    projectID = "advanced-macros"
    
    modInfo = infoUtil.getAMInfo()

    metadata = {
        "changelog": infoUtil.getChangeLog( modInfo["VERSION"] ), # Can be HTML or markdown if changelogType is set.
        "changelogType": "text", # Optional: defaults to text
        "displayName": f"Advanced Macros {modInfo["VERSION"]} for {platform} Minecraft {modInfo["GAME_VERSION"]}", # Optional: A friendly display name used on the site if provided.
        #parentFileID: 42, # Optional: The parent file of this file.
        "gameVersions": chooseVersionTags( modInfo["GAME_VERSION"], platform ), # A list of supported game versions, see the Game Versions API for details. Not supported if parentFileID is provided.
        "releaseType": modInfo["releaseType"], # One of "alpha", "beta", "release".
        # relations:
        # {
        #     projects: 
        #     [{
        #         slug: "mantle", # Slug of related plugin.
        #         type: ["embeddedLibrary", "incompatible", "optionalDependency", "requiredDependency", "tool"] # Choose one
        #     }]
        # } # Optional: An array of project relations by slug and type of dependency for inclusion in your project.
    }
    
    jarName = f"advanced-macros-{modInfo["VERSION"]}-for-{platform}-minecraft-{modInfo["GAME_VERSION"]}.jar"

    if os.getenv("ENABLE_AUTO_UPLOAD").lower() == "true":
        upload_file(projectID, metadata, jarName, filePath)
    else:
        print(" === Mock upload === ")
        print("Project ID: " + projectID)
        print("File Path: " + filePath)
        print("Exists: " + str(os.path.exists(filePath)))
        print("Jar as: " + jarName)
        print("Platform: " + platform)
        print("MetaData:")
        print(metadata)

