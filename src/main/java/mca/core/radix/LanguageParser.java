package mca.core.radix;

import mca.core.MCA;
import mca.data.NBTPlayerData;
import mca.data.PlayerMemory;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumGender;
import mca.enums.EnumRelation;
import net.minecraft.entity.player.EntityPlayer;
import radixcore.modules.lang.AbstractLanguageParser;

public class LanguageParser extends AbstractLanguageParser
{
	@SuppressWarnings("null")
	@Override
	public String parsePhrase(String unparsedPhrase, Object[] arguments) 
	{
		int passes = 0;
		EntityVillagerMCA entitySpeaker = (EntityVillagerMCA) this.getArgumentOfType(arguments, EntityVillagerMCA.class, 1);
		EntityVillagerMCA entitySecondary = (EntityVillagerMCA) this.getArgumentOfType(arguments, EntityVillagerMCA.class, 2);
		EntityPlayer playerTarget = (EntityPlayer) this.getArgumentOfType(arguments, EntityPlayer.class);
		PlayerMemory memory = (entitySpeaker != null && playerTarget != null) ? entitySpeaker.attributes.getPlayerMemory(playerTarget) : null;
		
		//Allow at most 10 passes to avoid infinite loops.
		while (unparsedPhrase.contains("%"))
		{
			try
			{
				if (unparsedPhrase.contains("%Name%"))
				{
					unparsedPhrase = unparsedPhrase.replace("%Name%", entitySpeaker.attributes.getName());
				}
				
				else if (unparsedPhrase.contains("%Profession%"))
				{
					unparsedPhrase = unparsedPhrase.replace("%Profession%", entitySpeaker.attributes.getProfessionEnum().getUserFriendlyForm(entitySpeaker));
				}
				
				else if (unparsedPhrase.contains("%FatherName%"))
				{
					String parentName = entitySpeaker.attributes.getParentNames();
					unparsedPhrase = unparsedPhrase.replace("%FatherName%", parentName.subSequence(0, parentName.indexOf("|")));
				}
				
				else if (unparsedPhrase.contains("%MotherName%"))
				{
					String parentNames = entitySpeaker.attributes.getParentNames();
					unparsedPhrase = unparsedPhrase.replace("%MotherName%", parentNames.subSequence(parentNames.indexOf("|") + 1, parentNames.length()));
				}
				
				else if (unparsedPhrase.contains("%PlayerName%"))
				{
					try
					{
						NBTPlayerData data = MCA.getPlayerData(playerTarget);
						unparsedPhrase = unparsedPhrase.replace("%PlayerName%", data.getMcaName());
					}
					
					catch (Exception e)
					{
						unparsedPhrase = unparsedPhrase.replace("%PlayerName%", playerTarget.getName());
					}
				}
				
				else if (unparsedPhrase.contains("%ParentOpposite%"))
				{
					boolean isPlayerMale = MCA.getPlayerData(playerTarget).getGender() == EnumGender.MALE;
					
					if (isPlayerMale)
					{
						unparsedPhrase = unparsedPhrase.replace("%ParentOpposite%", MCA.getLocalizer().getString("parser.mom"));
					}
					
					else
					{
						unparsedPhrase = unparsedPhrase.replace("%ParentOpposite%", MCA.getLocalizer().getString("parser.dad"));						
					}
				}
				
				else if (unparsedPhrase.contains("%ParentTitle%"))
				{
					boolean isPlayerMale = MCA.getPlayerData(playerTarget).getGender() == EnumGender.MALE;
					
					if (!isPlayerMale)
					{
						unparsedPhrase = unparsedPhrase.replace("%ParentTitle%", MCA.getLocalizer().getString("parser.mom"));
					}
					
					else
					{
						unparsedPhrase = unparsedPhrase.replace("%ParentTitle%", MCA.getLocalizer().getString("parser.dad"));						
					}					
				}
				
				else if (unparsedPhrase.contains("%RelationToPlayer%"))
				{
					EnumRelation relation = memory.getRelation();
					unparsedPhrase = unparsedPhrase.replace("%RelationToPlayer%", MCA.getLocalizer().getString(relation.getPhraseId()));
				}
				
				else if (unparsedPhrase.contains("%a1%"))
				{
					unparsedPhrase = unparsedPhrase.replace("%a1%", arguments[0].toString());
				}
				
				else if (unparsedPhrase.contains("%a2%"))
				{
					unparsedPhrase = unparsedPhrase.replace("%a2%", arguments[1].toString());					
				}
			}

			catch (Exception e)
			{
				e.printStackTrace();
			}

			finally
			{
				passes++;
				
				if (passes >= 10)
				{
					Throwable t = new Throwable();
					t.printStackTrace();
					break;
				}
			}
		}
		return unparsedPhrase;
	}
}
