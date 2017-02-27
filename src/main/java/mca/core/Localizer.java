package mca.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import mca.core.radix.LanguageParser;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import radixcore.modules.RadixMath;
import radixcore.modules.lang.AbstractLanguageParser;

@SideOnly(Side.CLIENT)
public class Localizer 
{
	private File langDir;
	private Map<String, String> localizerMap;
	private AbstractLanguageParser parser;
	private String remoteSHA1;

	public Localizer(FMLPreInitializationEvent event)
	{
		langDir = new File(event.getModConfigurationDirectory(), "/MCA/lang");
		parser = new LanguageParser();
		localizerMap = new HashMap<String, String>();
		remoteSHA1 = "";

		if (!langDir.exists())
		{
			langDir.mkdirs();
		}

		onLanguageChange();
	}

	/**
	 * @return	The number of phrases containing the provided string in their ID.
	 */
	public int getNumberOfPhrasesMatchingID(String id)
	{
		List<String> containingKeys = new ArrayList<String>();

		for (String key : localizerMap.keySet())
		{
			if (key.contains(id))
			{
				containingKeys.add(key);
			}
		}

		return containingKeys.size();
	}

	public String getString(String id)
	{
		return getString(id, (Object) null);
	}

	public String getString(String id, Object... arguments)
	{
		//Check if the exact provided key exists in the translations map.
		if (localizerMap.containsKey(id))
		{
			//Parse it if a parser was provided.
			if (parser != null)
			{
				return parser.parsePhrase(localizerMap.get(id), arguments);
			}

			else
			{
				return localizerMap.get(id);
			}
		}

		else
		{
			//Build a list of keys that at least contain part of the provided key name.
			List<String> containingKeys = new ArrayList<String>();

			for (String key : localizerMap.keySet())
			{
				if (key.contains(id))
				{
					containingKeys.add(key);
				}
			}

			//Return a random potentially valid key if some were found.
			if (containingKeys.size() > 0)
			{
				String key = containingKeys.get(RadixMath.getNumberInRange(0, containingKeys.size() - 1));

				if (parser != null)
				{
					return parser.parsePhrase(localizerMap.get(key), arguments);
				}

				else
				{
					return localizerMap.get(key);
				}
			}

			else
			{
				MCA.getLog().error("[MCA] No mapping found for requested phrase ID: " + id);
				return id;
			}
		}
	}

	public void onLanguageChange()
	{
		String langId = getGameLanguageID();
		boolean languageSupported = isLanguageSupported(langId);

		if (!languageSupported)
		{
			langId = "en_us";
		}

		if (needsToUpdate(langId))
		{
			updateLanguageFiles(langId);
		}
		
		loadLanguage(langId);
	}

	private void loadLanguage(String langId)
	{
		localizerMap.clear();

		File langFile = new File(langDir, langId + ".lang");

		try
		{
			List<String> lines = IOUtils.readLines(new FileInputStream(langFile), Charsets.UTF_8);
			int lineNumber = 0;

			for (String line : lines)
			{
				lineNumber++;

				if (!line.startsWith("#") && !line.isEmpty())
				{
					String[] split = line.split("\\=");
					String key = split[0];
					String value = split.length == 2 ? split[1].replace("\\", "") : "";

					if (key.isEmpty())
					{
						MCA.getLog().warn("Empty phrase key on line " + lineNumber);
						continue;
					}

					if (value.isEmpty())
					{
						MCA.getLog().warn("Empty phrase value on line " + lineNumber + ". Key value: " + key);
						continue;
					}

					localizerMap.put(key, value);
				}
			}
		}

		catch (Exception e)
		{
			throw new RuntimeException("Failed to load external language file.", e);
		}
	}

	/**
	 * Attempts to download the relevant language file from the server.
	 * If it succeeds, the file will be stored in `langFileCache`.
	 */
	private boolean isLanguageSupported(String langId)
	{
		try
		{
			String supported = IOUtils.toString(new URL("http://files.radix-shock.com/MCA/lang/supported-languages")).toLowerCase();

			if (!supported.contains(langId))
			{
				MCA.getLog().error("Your selected language is not supported by MCA.");
				MCA.getLog().error("MCA will default to English.");
				return false;
			}

			else
			{
				return true;
			}
		}

		catch (Exception e)
		{
			MCA.getLog().error("Failed to fetch list of supported languages!");
			MCA.getLog().error("MCA will default to English.");
			return false;
		}
	}

	private String getRemoteLangFileName(String langId)
	{
		return langId.substring(0, 2) + "_" + langId.substring(3).toUpperCase() + ".lang";
	}

	private void updateLanguageFiles(String langId)
	{
		String languageFileName = langId.substring(0, 2) + "_" + langId.substring(3).toUpperCase() + ".lang";
		File langFile = new File(langDir, languageFileName.toLowerCase());
		File langSHA1 = new File(langDir, languageFileName.toLowerCase() + ".sha1");

		try
		{
			if (langFile.exists()) langFile.delete();
			if (langSHA1.exists()) langSHA1.delete();

			MCA.getLog().info("Updating localization file for " + langId + " from remote");

			String remoteLang = IOUtils.toString(new URL("http://files.radix-shock.com/MCA/lang/" + languageFileName));
			if (remoteSHA1.isEmpty()) remoteSHA1 = IOUtils.toString(new URL("http://files.radix-shock.com/MCA/lang/" + languageFileName + ".sha1"));

			FileUtils.writeStringToFile(langFile, remoteLang);
			FileUtils.writeStringToFile(langSHA1, remoteSHA1);

			MCA.getLog().info("Localization updated successfully.");
		}

		catch (IOException e)
		{
			MCA.getLog().error("Failed to fetch localization file from remote server. Defaulting to built-in English localization file.");
			MCA.getLog().error("Updated localizations will be downloaded if the server becomes available later.");
			InputStream in = this.getClass().getResourceAsStream("/assets/mca/lang/en_us.lang");

			try
			{
				List<String> lines = IOUtils.readLines(in);
				FileUtils.writeLines(langFile, lines);
			}

			catch (Exception e1)
			{
				throw new RuntimeException("Failed to load language from remote, and failed to substitute with built-in English localizations.");
			}
		}
	}

	/*
	 * Compares local SHA1 hash to remote SHA1 and returns true if they do not match.
	 * Saves remote SHA1 hash to cache.
	 */
	private boolean needsToUpdate(String languageId)
	{
		try
		{
			String remoteLangFile = getRemoteLangFileName(languageId);
			File localLangFile = new File(langDir, remoteLangFile.toLowerCase());
			File localLangSHA1 = new File(langDir, remoteLangFile.toLowerCase() + ".sha1");

			//Check to see if we have a local SHA1 at all before querying for the SHA1 hash.
			if (!localLangSHA1.exists() || !localLangFile.exists()) { return true; }
			String localSHA1 = IOUtils.toString(localLangSHA1.toURI());

			//If we do have a local SHA1, query the server and see if they match. Cache in our remote SHA1 variable.
			remoteSHA1 = IOUtils.toString(new URL("http://files.radix-shock.com/MCA/lang/" + remoteLangFile + ".sha1"));
			return !localSHA1.equals(remoteSHA1);
		}

		catch (Exception e)
		{
			MCA.getLog().error("Failed to fetch data for language: " + languageId);
			return true;
		}
	}

	private String getGameLanguageID()
	{
		String languageID = "en_us";

		try
		{
			languageID = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
		}

		catch (final Exception e)
		{
			MCA.getLog().error("Unable to get current language code. Defaulting to English.");
		}

		return languageID;
	}
}
