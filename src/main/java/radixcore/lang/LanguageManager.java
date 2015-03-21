package radixcore.lang;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.minecraft.util.StringTranslate;
import radixcore.core.RadixCore;
import radixcore.util.RadixExcept;
import radixcore.util.RadixMath;
import cpw.mods.fml.common.FMLCommonHandler;

public class LanguageManager 
{
	private String modId;
	private AbstractLanguageParser parser;
	private Map<String, String> translationsMap;

	public LanguageManager(String providedModId)
	{
		this.modId = providedModId.toLowerCase();
		this.translationsMap = new HashMap<String, String>();
		
		Properties properties = new Properties();
		
		if (FMLCommonHandler.instance().getEffectiveSide().isClient())
		{
			String languageId = getLanguageIDFromOptions();
			
			try
			{
				properties.load(StringTranslate.class.getResourceAsStream("/assets/" + modId + "/lang/" + languageId + ".lang"));
			}

			catch (Exception e)
			{
				RadixCore.getLogger().error("Error loading language " + languageId + " for " + modId + ". Attempting to default to English.");
				
				try
				{
					properties.load(StringTranslate.class.getResourceAsStream("/assets/" + modId + "/lang/" + "en_US.lang"));
				}
				
				catch (Exception e2)
				{
					RadixExcept.logFatalCatch(e2, "Error loading language " + languageId + " for mod " + modId + ".");
				}
			}
		}
		
		else
		{
			try
			{
				properties.load(StringTranslate.class.getResourceAsStream("/assets/" + modId + "/lang/" + "en_US.lang"));
			}
			
			catch (Exception e)
			{
				RadixExcept.logFatalCatch(e, "Error loading language server-side. Loading cannot continue.");
			}
		}
		
		for (final Map.Entry<Object, Object> entrySet : properties.entrySet())
		{
			translationsMap.put(entrySet.getKey().toString(), entrySet.getValue().toString());
		}
	}

	public LanguageManager(String modId, AbstractLanguageParser parser)
	{
		this(modId);
		this.parser = parser;
	}
	
	public String getLanguageIDFromOptions()
	{
		BufferedReader reader = null;
		String languageID = "";

		try
		{
			reader = new BufferedReader(new FileReader(RadixCore.getRunningDirectory() + "/options.txt"));

			String line = null;

			while (line != null)
			{
				line = reader.readLine();

				if (line.contains("lang:"))
				{
					break;
				}
			}

			if (!line.isEmpty())
			{
				reader.close();
				languageID = line.substring(5);
			}
		}

		catch (final FileNotFoundException e)
		{
			RadixCore.getLogger().error("Could not find options.txt file. Defaulting to English.");
			languageID = "en_US";
		}

		catch (final IOException e)
		{
			RadixCore.getLogger().error("Error reading from Minecraft options.txt file. Defaulting to English.", e);
			languageID = "en_US";
		}

		catch (final NullPointerException e)
		{
			RadixCore.getLogger().error("NullPointerException while trying to read options.txt. Defaulting to English.");
			languageID = "en_US";
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
				RadixCore.getLogger().error("[" + modId + "] No mapping found for requested phrase ID: " + id);
				Throwable trace = new Throwable();
				RadixExcept.logErrorCatch(trace, "Stacktrace for non-fatal error.");
				return id;
			}
		}
	}
	
	/**
	 * @return	The number of phrases containing the provided string in their ID.
	 */
	public int getNumberOfPhrasesMatchingID(String id)
	{
		List<String> containingKeys = new ArrayList<String>();
		
		for (String key : translationsMap.keySet())
		{
			if (key.contains(id))
			{
				containingKeys.add(key);
			}
		}
		
		return containingKeys.size();
	}
}
