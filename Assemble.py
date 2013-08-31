#!/usr/bin/env python

#-------------------------------------------------------------------------------
# Assemble.py
# Prepares Minecraft Comes Alive to be distributed by packaging the mod
# archive and source archives.
#
# This file, both in source code form or as a compiled binary, is free and
# released into the public domain.
#-------------------------------------------------------------------------------

import os
import sys
import shutil
import zipfile
import subprocess
import traceback
import time

languagesMap = {'af': 'Afrikaans',
                'ar': 'Arabic',
                'bg': 'Bulgarian',
                'ca': 'Catalan',
                'cs': 'Czech',
                'cy': 'Welsh',
                'da': 'Danish',
                'de': 'German',
                'el': 'Greek',
                'en-AU': 'English',
                'en-CA': 'English',
                'en-GB': 'English',
                'en-PT': 'Pirate',
                'en-US': 'English',
                'en': 'English',
                'eo': 'Esperanto',
                'es-AR': 'Argentina Spanish',
                'es-ES': 'Spanish',
                'es-MX': 'Mexico Spanish',
                'es-UY': 'Uruguay Spanish',
                'es-VE': 'Venezuela Spanish',
                'et': 'Estonian',
                'eu': 'Basque',
                'fi': 'Finnish',
                'fr': 'French',
                'fr-CA': 'Canadian French',
                'ga-IE': 'Irish',
                'gl': 'Galician',
                'he': 'Hebrew',
                'hi': 'Hindi',
                'hr': 'Croatian',
                'hu': 'Hungarian',
                'id': 'Bahasa Indonesia',
                'is': 'Icelandic',
                'it': 'Italian',
                'ja': 'Japanese',
                'ka': 'Georgian',
                'ko': 'Korean',
                'kw': 'Cornish',
                'lt': 'Lithuanian',
                'lv': 'Latvian',
                'ms': 'Malay',
                'mt': 'Maltese',
                'nl': 'Dutch',
                'nn-NO': 'Nynorsk',
                'no': 'Norwegian',
                'pl': 'Polish',
                'pt-BR': 'Brazilian Portuguese',
                'pt-PT': 'Portuguese',
                'qya-AA': 'Quenya',
                'ro': 'Romanian',
                'ru': 'Russian',
                'sk': 'Slovak',
                'sl': 'Slovenian',
                'sr': 'Serbian',
                'sv-SE': 'Swedish',
                'th': 'Thai',
                'tlh-AA': 'Klingon',
                'tr': 'Turkish',
                'uk': 'Ukrainian',
                'vi': 'Vietnamese',
                'zh-CN': 'Chinese Simplified',
                'zh-TW': 'Chinese Traditional'}

releaseVersion = ""
baseFolder     = os.getcwd() + "/"
projectFolder  = baseFolder + "Forge/mcp/"
buildFolder    = baseFolder + "Build/"
languageFolder = baseFolder + "Crowdin/"
reobfFolder    = projectFolder + "/reobf/minecraft/mca/"
sourceFolder   = projectFolder + "/modsrc/minecraft/mca/"
assetsFolder   = projectFolder + "/src/minecraft/assets/"
    
def main():
    print "---------------------------------------------"
    print "Minecraft Comes Alive Packaging Script"
    print "---------------------------------------------"

    #Get release version
    releaseVersion = raw_input("Enter version: ")

    #Check whether to run MCP again or not.
    choice = raw_input("Run MCP? [Y/N]: ")
    
    if (choice == "Y" or choice == "y"):
        os.chdir(projectFolder)
        insertBlank()
        recompile()
        reobfuscate()

    #Clear the build folder
    if (os.path.exists(buildFolder)):
        print "Removing build folder..."
        shutil.rmtree(buildFolder)
        print "Waiting to continue..."
        time.sleep(1)

    os.mkdir(buildFolder)
    
    insertBlank()
    print "---------------------------------------"
    print "Packaging release archive..."
    print "---------------------------------------"
    insertBlank()
    
    #Build language.
    convertLanguageFiles()
    
    #Zip up the mod archive.
    modArchive = zipfile.ZipFile(buildFolder + "/MCA " + releaseVersion + ".zip", "w", zipfile.ZIP_DEFLATED)
    modFiles = os.listdir(buildFolder)

    modArchive.write(projectFolder + "src/minecraft/mca.png", "mca.png")
    modArchive.write(projectFolder + "src/minecraft/mcmod.info", "mcmod.info")
    modArchive.write(baseFolder + "LICENSE", "_LICENSE.txt")

    languageFiles = os.listdir(baseFolder + "/Crowdin/Build/")

    print "Zipping language files..."
    for fileName in languageFiles:
        fullPath = os.path.join(baseFolder + "/Crowdin/Build/", fileName)
        modArchive.write(fullPath, "assets/mca/language/" + os.path.basename(fullPath))

    print "Zipping assets..."
    for root, dirs, files in os.walk(assetsFolder):
        for fileName in files:
            fullPath = os.path.join(root, fileName)

            if "language" not in fullPath:
                modArchive.write(fullPath, fullPath.replace(assetsFolder, "assets/"))

    print "Zipping compiled classes..."
    for root, dirs, files in os.walk(reobfFolder):
        for fileName in files:
            fullPath = os.path.join(root, fileName)
            modArchive.write(fullPath, fullPath.replace(reobfFolder, "mca/"))

    modArchive.close()

    #Zip up the source.
    linesOfCode = 0
    
    print "Zipping up source archive..."
    getSource()
    sourceArchive = zipfile.ZipFile(buildFolder + "/MCA " + releaseVersion + " - Source.zip", "w", zipfile.ZIP_DEFLATED)
    sourceFiles = os.listdir(sourceFolder)

    for root, dirs, files in os.walk(sourceFolder):
        for fileName in files:
            containsCorrectHeader = True
            fullPath = os.path.join(root, fileName)
            archiveName = fullPath.replace(sourceFolder, "mca/")
            sourceArchive.write(fullPath, archiveName)

            with open(fullPath) as f:
                lines = f.readlines()

                for line in lines:
                    linesOfCode += 1

                    if fileName in lines:
                        containsCorrectHeader = True

            if not containsCorrectHeader:
                print "WARNING: Malformed header on " + fileName + "."

    print str(linesOfCode) + " lines."
    insertBlank()
    sourceArchive.close()

    print "--------------------------"
    print "Packaging complete."
    print "--------------------------"
    os.system("pause")

def recompile():
    os.chdir(projectFolder)
    subprocess.call("recompile.bat")
    insertBlank()

def reobfuscate():
    os.chdir(projectFolder)
    subprocess.call("reobfuscate.bat")
    insertBlank()

def getSource():
    os.chdir(projectFolder)
    subprocess.call("getchangedsrc.bat")
    insertBlank()

def convertLanguageFiles():
    for folder in os.listdir(languageFolder + "/Source"):
        if os.path.isdir(languageFolder + "/Source/" + folder):
            shutil.copyfile(languageFolder + "/Source/" + folder + "/" + "English.properties",
                            languageFolder + "/Build/" + languagesMap[folder] + ".properties")
            
def insertBlank():
    print ""
    
if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        print "!!!!!!!!!!!!Unexpected exception!!!!!!!!!!!!"
        print e
        traceback.print_exc()
        os.system("pause")
