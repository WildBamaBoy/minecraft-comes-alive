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
import urllib

modVersion = ""
dirCWD     = os.getcwd() + "/"
dirCrowdin = dirCWD + "Crowdin/"

def main():
    print "---------------------------------------------"
    print "Minecraft Comes Alive Packaging Script"
    print "---------------------------------------------"

    #Get release version
    global modVersion
    modVersion = raw_input("Enter mod version: ")

    #Get minecraft version
    global minecraftVersion
    minecraftVersion = raw_input("Enter Minecraft version: ");

    global dirBuild
    dirBuild = dirCWD + "Build/" + minecraftVersion + "/"

    if "1.7" in minecraftVersion:
        build()

    log("Finished.")
    openBuildFolder()

def convertLanguageFiles():
    for folder in os.listdir(dirCrowdin + "/Source"):
        if os.path.isdir(dirCrowdin + "/Source/" + folder):
            shutil.copyfile(dirCrowdin + "/Source/" + folder + "/" + "English.properties", dirCrowdin + "/Build/" + languagesMap17[languagesMap16[folder]] + ".lang")

def log(message):
    print "> " + message

def logSub(message):
    print "   > " + message

def build():
    global dirGradleBase
    dirGradleBase = dirCWD + "Minecraft/" + minecraftVersion

    choice = raw_input("Run 'gradlew.bat build'? [Y/N]: ")
    print ""

    copyRadixSourceToMCA()
    updateLanguageFiles()

    if choice == "Y" or choice == "y":
        callGradleSubprocess()
    else:
        log("Gradle build SKIPPED.")

    deleteRadixSourceFromMCA()
    moveBuildToBuildFolder()
    cleanBuild()
    createSourceArchive()

def copyRadixSourceToMCA():
    log("Copying RadixCore source code to MCA directory...")

    dirRadixSrc = "D:/Programming/radix-core/Minecraft/1.7.2/src/main/java/com/"
    dirRadixDst = dirGradleBase + "/src/main/java/com/"

    if os.path.exists(dirRadixDst):
        shutil.rmtree(dirRadixDst)
        logSub("Removed existing RadixCore source code.")

    shutil.copytree(dirRadixSrc, dirGradleBase + "/src/main/java/com/")

def deleteRadixSourceFromMCA():
    log("Deleting RadixCore source code from MCA directory...")
    shutil.rmtree(dirGradleBase + "/src/main/java/com/")

def updateLanguageFiles():
    log("Updating language files...")
    convertLanguageFiles()

    dirAssetsMCA = dirGradleBase + "/src/main/resources/assets/mca/"
    shutil.rmtree(dirAssetsMCA + "/lang")
    shutil.copytree(dirCrowdin + "/Build/", dirAssetsMCA + "/lang")

def callGradleSubprocess():
    log("Building MCA...")

    os.chdir(dirGradleBase)
    print "--------------------- GRADLE BEGIN ---------------------"
    subprocess.call("gradlew.bat build")
    print "---------------------- GRADLE END ----------------------"

def moveBuildToBuildFolder():
    log("Moving build to build folder...")

    fileFinishedBuild = dirGradleBase + "/build/libs/MCA-RadixAssembled.jar"

    if os.path.exists(dirBuild):
        logSub("Removing existing build folder...")
        shutil.rmtree(dirBuild)
        logSub("Waiting to continue...")
        time.sleep(1)

    os.mkdir(dirBuild)
    shutil.copy(fileFinishedBuild, dirBuild + "/_in_MCA-" + modVersion + " MC-" + minecraftVersion + ".zip")

def cleanBuild():
    log("Cleaning MCA...")

    fileInArchive = dirBuild + "/_in_MCA-" + modVersion + " MC-" + minecraftVersion + ".zip"
    fileOutArchive = dirBuild + "/_out_MCA-" + modVersion + " MC-" + minecraftVersion + ".zip"

    zipInArchive = zipfile.ZipFile(fileInArchive, "r", zipfile.ZIP_DEFLATED)
    zipOutArchive = zipfile.ZipFile(fileOutArchive, "w", zipfile.ZIP_DEFLATED)

    for file in zipInArchive.filelist:
        if file.filename.startswith("com/") or file.filename.startswith("META-INF/"):
            logSub("Skipping: " + file.filename)
        else:
            zipOutArchive.writestr(file.filename, zipInArchive.read(file))

    zipInArchive.close()
    zipOutArchive.close()

    os.remove(fileInArchive)
    os.rename(fileOutArchive, str(fileOutArchive).replace("_out_", ""))

def openBuildFolder():
    explorerFriendlyName = str(dirBuild).replace("/", "\\")
    subprocess.Popen(r'explorer /root,"' + explorerFriendlyName + '"')

def createSourceArchive():
    linesOfCode = 0

    log("Building source archive...")

    sourceFolder = dirGradleBase + "/src/main/java/mca/"
    sourceArchive = zipfile.ZipFile(dirBuild + "/MCA-" + modVersion + " MC-" + minecraftVersion + " - Source.zip", "w", zipfile.ZIP_DEFLATED)
    sourceFiles = os.listdir(sourceFolder)

    for root, dirs, files in os.walk(sourceFolder):
        for fileName in files:
            containsCorrectHeader = False
            fullPath = os.path.join(root, fileName)
            archiveName = fullPath.replace(sourceFolder, "mca/")
            sourceArchive.write(fullPath, archiveName)

            with open(fullPath) as f:
                lines = f.readlines()

                for line in lines:
                    linesOfCode += 1

                    if fileName in line:
                        containsCorrectHeader = True
                    if "doSelfTest();" in line:
                        logSub("WARNING: Self tester will run!")

            if not containsCorrectHeader:
                logSub("WARNING: Malformed header on " + fileName + ".")

    logSub(str(linesOfCode) + " lines of code.")
    print ""
    sourceArchive.close()

languagesMap16 = {'af': 'Afrikaans',
                'ar': 'Arabic',
                'hy-AM': 'Armenian',
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
                'la-LA': 'Latin',
                'lb': 'Luxembourgish',
                'lt': 'Lithuanian',
                'lv': 'Latvian',
                'ms': 'Malay',
                'mt': 'Maltese',
                'nl': 'Dutch',
                'nn-NO': 'Nynorsk',
                'no': 'Norwegian',
                'oc': 'Occitan',
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

languagesMap17 = {'Afrikaans': 'af_ZA',
	'Arabic': 'ar_SA',
    'Armenian': 'hy_AM',
	'Bulgarian': 'bg_BG',
	'Catalan': 'ca_ES',
	'Czech': 'cs_CZ',
	'Welsh': 'cy_GB',
	'Danish': 'da_DK',
	'German': 'de_DE',
	'Greek': 'el_GR',
	'English': 'en_AU',
	'English': 'en_CA',
	'English': 'en_GB',
	'English': 'en_US',
	'Pirate': 'en_PT',
	'Esperanto': 'eo_UY',
	'Argentina Spanish': 'es_AR',
	'Spanish': 'es_ES',
	'Mexico Spanish': 'es_MX',
	'Uruguay Spanish': 'es_UY',
	'Venezuela Spanish': 'es_VE',
	'Estonian': 'et_EE',
	'Basque': 'eu_ES',
	'Finnish': 'fi_FI',
	'French': 'fr_FR',
	'Canadian French': 'fr_FA',
	'Irish': 'ga_IE',
	'Galician': 'gl_ES',
	'Hebrew': 'he_IL',
	'Hindi': 'hi_IN',
	'Croatian': 'hr_HR',
	'Hungarian': 'hu_HU',
	'Bahasa Indonesia': 'id_ID',
	'Icelandic': 'is_IS',
	'Italian': 'it_IT',
	'Japanese': 'ja_JP',
	'Georgian': 'ka_GE',
	'Korean': 'ko_KR',
	'Cornish': 'ko_KO',
    'Latin': 'la_LA',
    'Luxembourgish': 'lb_LB',
	'Lithuanian': 'lt_LT',
	'Latvian': 'lv_LV',
	'Malay': 'ms_MY',
	'Maltese': 'mt_MT',
	'Dutch': 'nl_NL',
	'Nynorsk': 'nn_NO',
	'Norwegian': 'nb_NO',
    'Occitan': 'oc_OC',
	'Polish': 'pl_PL',
	'Brazilian Portuguese': 'pt_BR',
	'Portuguese': 'pt_PT',
	'Quenya': 'qya_AA',
    'Romanian': 'ro_RO',
	'Russian': 'ru_RU',
	'Slovak': 'sk_SK',
	'Slovenian': 'sl_SI',
	'Serbian': 'sr_SP',
	'Swedish': 'sv_SE',
	'Thai': 'th_TH',
	'Klingon': 'tlh_AA',
	'Turkish': 'tr_TR',
	'Ukrainian': 'uk_UA',
	'Vietnamese': 'vi_VN',
	'Chinese Simplified': 'zh_CN',
	'Chinese Traditional': 'zh_TW'}

if __name__ == "__main__":
    try:
        os.system("color 0F")
        main()
    except Exception as e:
        print "--------------Unexpected exception--------------"
        print e
        traceback.print_exc()

    raw_input("Press any key to exit...")
