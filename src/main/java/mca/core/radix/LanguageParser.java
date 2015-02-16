package mca.core.radix;

import mca.data.PlayerMemory;
import mca.entity.EntityHuman;
import net.minecraft.entity.player.EntityPlayer;
import radixcore.helpers.ExceptHelper;
import radixcore.lang.AbstractLanguageParser;

public class LanguageParser extends AbstractLanguageParser
{
	@SuppressWarnings("null")
	@Override
	public String parsePhrase(String unparsedPhrase, Object[] arguments) 
	{
		int passes = 0;
		EntityHuman entitySpeaker = (EntityHuman) this.getArgumentOfType(arguments, EntityHuman.class, 1);
		EntityHuman entitySecondary = (EntityHuman) this.getArgumentOfType(arguments, EntityHuman.class, 2);
		EntityPlayer playerTarget = (EntityPlayer) this.getArgumentOfType(arguments, EntityPlayer.class);
		PlayerMemory memory = (entitySpeaker != null && playerTarget != null) ? entitySpeaker.getPlayerMemory(playerTarget) : null;
		
		//Allow at most 10 passes to avoid infinite loops.
		while (unparsedPhrase.contains("%"))
		{
			try
			{
				if (unparsedPhrase.contains("%Name%"))
				{
					unparsedPhrase = unparsedPhrase.replace("%Name%", entitySpeaker.getName());
				}
				
				else if (unparsedPhrase.contains("%Profession%"))
				{
					unparsedPhrase = unparsedPhrase.replace("%Profession%", entitySpeaker.getProfessionEnum().getUserFriendlyForm());
				}
				
				else if (unparsedPhrase.contains("%FatherName%"))
				{
					String parentName = entitySpeaker.getParentNames();
					unparsedPhrase = unparsedPhrase.replace("%FatherName%", parentName.subSequence(0, parentName.indexOf("|")));
				}
				
				else if (unparsedPhrase.contains("%MotherName%"))
				{
					String parentNames = entitySpeaker.getParentNames();
					unparsedPhrase = unparsedPhrase.replace("%MotherName%", parentNames.subSequence(parentNames.indexOf("|") + 1, parentNames.length()));
				}
				
				else if (unparsedPhrase.contains("%PlayerName%"))
				{
					unparsedPhrase = unparsedPhrase.replace("%PlayerName%", memory.getPlayerName());					
				}
			}

			catch (Exception e)
			{
				ExceptHelper.logErrorCatch(e, "Exception while parsing phrase.");
			}

			finally
			{
				passes++;
				
				if (passes >= 10)
				{
					Throwable t = new Throwable();
					ExceptHelper.logErrorCatch(t, "Too many passes through parser! Some text isn't displaying correctly.");
					break;
				}
			}
		}

		return unparsedPhrase;
	}
}
