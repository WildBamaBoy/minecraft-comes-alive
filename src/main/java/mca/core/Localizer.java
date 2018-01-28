package mca.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import mca.core.radix.LanguageParser;
import net.minecraft.client.Minecraft;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import radixcore.modules.RadixMath;
import radixcore.modules.lang.AbstractLanguageParser;

public class Localizer 
{
	private AbstractLanguageParser parser;
	private Map<String, String> translationsMap;

	public Localizer(FMLPreInitializationEvent event)
	{
		this.parser = new LanguageParser();
		this.translationsMap = new HashMap<String, String>();

		boolean loadedLanguage = false;

		for (StackTraceElement element : new Throwable().getStackTrace())
		{
			if (element.getClassName().equals("net.minecraft.server.dedicated.DedicatedServer"))
			{
				MCA.getLog().warn("MCA is running on a dedicated server and will default to using English as its language.");
				MCA.getLog().warn("This may cause issues with some phrases being translated while others are not.");
				MCA.getLog().warn("**** To change your server's language in MCA, change the `serverLanguageId` option in MCA's configuration. ****");
				loadLanguage(MCA.getConfig().serverLanguageId);
				loadedLanguage = true;
			}
		}

		if (!loadedLanguage)
		{
			loadLanguage(getGameLanguageID());
		}
	}

	@SideOnly(Side.CLIENT)
	public String getGameLanguageID()
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

	public String getString(String id)
	{
		return getString(id, (Object) null);
	}

	public String getString(String id, Object... arguments)
	{
		//Check if the exact provided key exists in the translations map.
		if (translationsMap.containsKey(id))
		{
			//Parse it if a parser was provided.
			if (parser != null)
			{
				return parser.parsePhrase(translationsMap.get(id), arguments);
			}

			else
			{
				return translationsMap.get(id);
			}
		}

		else
		{
			//Build a list of keys that at least contain part of the provided key name.
			List<String> containingKeys = new ArrayList<String>();

			for (String key : translationsMap.keySet())
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
					return parser.parsePhrase(translationsMap.get(key), arguments);
				}

				else
				{
					return translationsMap.get(key);
				}
			}

			else
			{
				MCA.getLog().error("No translation mapping found for requested phrase ID: " + id);
				return id;
			}
		}
	}

	public void onLanguageChange()
	{
		loadLanguage(getGameLanguageID());
	}
	
	private void loadLanguage(String languageId)
	{
		//Make sure our language ID is lower case
		languageId = languageId.toLowerCase();
		
		//Remove old translations.
		translationsMap.clear();

		//Handle all English locales.
		if (languageId.startsWith("en_") && !languageId.equals("en_us"))
		{
			loadLanguage("en_us");
			return;
		}

		//And Spanish locales.
		else if (languageId.startsWith("es_") && !languageId.equals("es_es"))
		{
			loadLanguage("es_es");
			return;
		}

		//All checks for locales have passed. Load the desired language.
		InputStream inStream = StringUtils.class.getResourceAsStream("/assets/mca/lang/" + languageId + ".lang");

		if (inStream == null) //When language is not found, default to English.
		{
			//Make sure we're not already English. Null stream on English is a problem.
			if (languageId.equals("en_us"))
			{
				throw new RuntimeException("Unable to load English language files. Loading cannot continue.");
			}

			else
			{
				MCA.getLog().error("Cannot load language " + languageId + ". Defaulting to English.");
				loadLanguage("en_us");
			}
		}

		else
		{
			try
			{
				List<String> lines = IOUtils.readLines(inStream, Charsets.UTF_8);
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
							throw new IOException("Empty phrase key on line " + lineNumber);
						}

						if (value.isEmpty())
						{
							MCA.getLog().warn("Empty phrase value on line " + lineNumber + ". Key value: " + key);
						}

						translationsMap.put(key, value);
					}
				}

				MCA.getLog().info("Loaded language " + languageId);
			}

			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
