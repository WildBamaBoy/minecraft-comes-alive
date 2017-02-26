package mca.util;

import mca.actions.ActionStoryProgression;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumBabyState;
import mca.enums.EnumGender;
import mca.enums.EnumMarriageState;
import mca.enums.EnumProfession;
import mca.enums.EnumProgressionStep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import radixcore.constant.Font.Color;
import radixcore.constant.Font.Format;
import radixcore.constant.Time;

public class IngameTester 
{
	private IngameTester(){}
	
	public static void run(EntityPlayer player)
	{
		EntityVillagerMCA adam = new EntityVillagerMCA(player.world);
		EntityVillagerMCA eve = new EntityVillagerMCA(player.world);
		adam.setPosition(player.posX + 2, player.posY, player.posZ);
		eve.setPosition(player.posX + 1, player.posY, player.posZ);
		player.world.spawnEntity(adam);
		player.world.spawnEntity(eve);
		
		for (int i = 0; i < 5000; i++)
		{
			adam.attributes.assignRandomGender();
			adam.attributes.assignRandomName();
			adam.attributes.assignRandomPersonality();
			adam.attributes.assignRandomScale();
			adam.attributes.assignRandomSkin();
			
			try
			{
				assertTrue(!adam.attributes.getName().isEmpty());
				assertTrue(adam.attributes.getGender() != EnumGender.UNASSIGNED);
				assertTrue(adam.attributes.getProfessionEnum() != EnumProfession.Unassigned);
				assertTrue(!adam.attributes.getClothesTexture().isEmpty());
				assertTrue(!adam.attributes.getHeadTexture().isEmpty());
			}
			
			catch (AssertionError e)
			{
				e.printStackTrace();
				failTest("Villager creation", player);
				adam.setDead();
				eve.setDead();
				return;
			}
		}
		
		adam.attributes.setGender(EnumGender.MALE);
		eve.attributes.setGender(EnumGender.FEMALE);
		adam.attributes.setName("Adam");
		eve.attributes.setName("Eve");
		adam.attributes.assignRandomSkin();
		eve.attributes.assignRandomPersonality();
		eve.attributes.assignRandomProfession();
		eve.attributes.assignRandomSkin();
		
		passTest("Villager creation", player);
		
		try
		{
			adam.startMarriage(Either.<EntityVillagerMCA, EntityPlayer>withL(eve));
			
			assertTrue(adam.attributes.getMarriageState() == EnumMarriageState.MARRIED_TO_VILLAGER);
			assertTrue(eve.attributes.getMarriageState() == EnumMarriageState.MARRIED_TO_VILLAGER);
			assertTrue(adam.attributes.getSpouseGender() == eve.attributes.getGender());
			assertTrue(adam.attributes.getSpouseName().equals(eve.attributes.getName()));
			assertTrue(adam.attributes.getSpouseUUID() == eve.getPersistentID());
			assertTrue(eve.attributes.getSpouseGender() == adam.attributes.getGender());
			assertTrue(eve.attributes.getSpouseName().equals(adam.attributes.getName()));
			assertTrue(eve.attributes.getSpouseUUID() == adam.getPersistentID());
			assertTrue(adam.getBehavior(ActionStoryProgression.class).getIsDominant());
			assertTrue(!eve.getBehavior(ActionStoryProgression.class).getIsDominant());
			assertTrue(adam.getBehaviors().getAction(ActionStoryProgression.class).getProgressionStep() == EnumProgressionStep.TRY_FOR_BABY);
			assertTrue(eve.getBehaviors().getAction(ActionStoryProgression.class).getProgressionStep() == EnumProgressionStep.TRY_FOR_BABY);
			
			//Place us at the end of the story progression threshold
			adam.attributes.setTicksAlive(MCA.getConfig().storyProgressionThreshold * Time.MINUTE);
			eve.attributes.setTicksAlive(MCA.getConfig().storyProgressionThreshold * Time.MINUTE);
			
			boolean success = false;
			ActionStoryProgression story = adam.getBehavior(ActionStoryProgression.class);
			
			for (int i = 0; i < 50000; i++)
			{
				adam.getBehaviors().onUpdate();
				
				if (story.getProgressionStep() == EnumProgressionStep.HAD_BABY)
				{
					success = true;
					break;
				}
			}
			
			assertTrue(success);
			
			story = eve.getBehavior(ActionStoryProgression.class);
			assertTrue(story.getProgressionStep() == EnumProgressionStep.HAD_BABY);
			assertTrue(eve.attributes.getBabyState() != EnumBabyState.NONE);
			
			adam.endMarriage();
			eve.endMarriage();
			assertTrue(story.getProgressionStep() == EnumProgressionStep.HAD_BABY);
			assertTrue(eve.attributes.getBabyState() != EnumBabyState.NONE);
			assertTrue(adam.getBehavior(ActionStoryProgression.class).getProgressionStep() == EnumProgressionStep.SEARCH_FOR_PARTNER);
			
			adam.startMarriage(Either.<EntityVillagerMCA, EntityPlayer>withL(eve));
		}
		
		catch (AssertionError e)
		{
			e.printStackTrace();
			failTest("Villager marriage and story simulation", player);
			return;
		}
		
		passTest("Villager marriage and story simulation", player);
		
		//Actions
		//Story progression
		//Villager marriage
		//Player marriage to villager
		//Ending marriage
	}
	
	private static void addMessage(String message, EntityPlayer player)
	{
		player.sendMessage(new TextComponentString(Color.GOLD + "[" + Color.DARKRED + "MCA" + Color.GOLD + "] " + Format.RESET + message));
	}
	
	private static void passTest(String testName, EntityPlayer player)
	{
		addMessage("- " + testName + ": " + Color.GREEN + "[PASS]", player);
	}
	
	private static void failTest(String testName, EntityPlayer player)
	{
		addMessage("- " + testName + ": " + Color.RED + "[FAIL]", player);
	}
	
	private static void assertTrue(boolean expression) throws AssertionError
	{
		if (!expression)
		{
			throw new AssertionError();
		}
	}
	
	private static void assertFalse(boolean expression) throws AssertionError
	{
		if (expression)
		{
			throw new AssertionError();
		}
	}
}
