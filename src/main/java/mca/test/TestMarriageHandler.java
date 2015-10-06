package mca.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import mca.ai.AIProgressStory;
import mca.core.MCA;
import mca.data.PlayerData;
import mca.data.PlayerMemory;
import mca.entity.EntityHuman;
import mca.enums.EnumDialogueType;
import mca.enums.EnumProgressionStep;
import mca.util.MarriageHandler;
import radixcore.core.RadixCore;

/**
 * This test will ensure that marriages can be started and ended properly.
 */
public class TestMarriageHandler 
{
	EntityHuman adam;
	EntityHuman eve;
	DummyPlayer steve;
	DummyPlayer alex;
	PlayerData steveData;
	PlayerData alexData;
	
	@Before
	public void init()
	{
		RadixCore.isTesting = true;
		MCA.initializeForTesting();
		
		adam = new EntityHuman(null);
		eve = new EntityHuman(null);
		steve = DummyPlayer.getDummyPlayer(true);
		alex = DummyPlayer.getDummyPlayer(false);
		
		steveData = MCA.stevePlayerData;
		alexData = MCA.alexPlayerData;
		
		// 'Spawn' our entities.
		steve.worldObj.spawnEntityInWorld(adam);
		steve.worldObj.spawnEntityInWorld(eve);
		steve.worldObj.spawnEntityInWorld(steve);
		steve.worldObj.spawnEntityInWorld(alex);
	}

	@Test
	public void test() throws InstantiationException 
	{
		PlayerMemory eveMemory = eve.getPlayerMemory(steve);
		AIProgressStory eveStoryProgression = eve.getAI(AIProgressStory.class);
		
		//Preliminary testing for default values and proper working methods.
		Assert.assertTrue(adam.getSpouseId() == 0);
		Assert.assertFalse(adam.getIsMarried());
		Assert.assertFalse(adam.getIsEngaged());
		Assert.assertTrue(steveData.getSpousePermanentId() == 0);
		Assert.assertFalse(steveData.getIsMarried());
		Assert.assertFalse(steveData.getIsEngaged());
		
		//******************************
		// player -> human engagement
		//******************************
		MarriageHandler.startEngagement(steve, eve);
		
		Assert.assertTrue(steveData.getIsEngaged());
		Assert.assertTrue(eve.getIsEngaged());
		Assert.assertFalse(steveData.getIsMarried());
		Assert.assertFalse(eve.getIsMarried());
		Assert.assertEquals(eve.getPermanentId(), steveData.getSpousePermanentId());
		Assert.assertEquals(steveData.getPermanentId(), eve.getSpouseId());
		Assert.assertEquals(eve.getName(), steveData.getSpouseName());
		Assert.assertEquals(steve.getCommandSenderName(), eve.getSpouseName());
		
		Assert.assertEquals(eveMemory.getDialogueType(), EnumDialogueType.SPOUSE);
		Assert.assertEquals(eveStoryProgression.getProgressionStep(), EnumProgressionStep.FINISHED);
		
		resetAllAndAssert();
		
		Assert.assertEquals(eveMemory.getDialogueType(), EnumDialogueType.ADULT);
		Assert.assertEquals(eveStoryProgression.getProgressionStep(), EnumProgressionStep.SEARCH_FOR_PARTNER);
		
		//*****************************
		// player -> human marriage
		//*****************************
		MarriageHandler.startMarriage(steve, eve);

		Assert.assertFalse(steveData.getIsEngaged());
		Assert.assertFalse(eve.getIsEngaged());
		Assert.assertTrue(steveData.getIsMarried());
		Assert.assertTrue(eve.getIsMarried());
		Assert.assertEquals(eve.getPermanentId(), steveData.getSpousePermanentId());
		Assert.assertEquals(steveData.getPermanentId(), eve.getSpouseId());
		Assert.assertEquals(eve.getName(), steveData.getSpouseName());
		Assert.assertEquals(steve.getCommandSenderName(), eve.getSpouseName());
		Assert.assertEquals(eveMemory.getDialogueType(), EnumDialogueType.SPOUSE);
		Assert.assertEquals(eveStoryProgression.getProgressionStep(), EnumProgressionStep.FINISHED);
		
		resetAllAndAssert();
		
		//****************************
		// human -> human marriage
		//****************************
		MarriageHandler.startMarriage(adam, eve);
		
		Assert.assertFalse(adam.getIsEngaged());
		Assert.assertFalse(eve.getIsEngaged());
		Assert.assertTrue(adam.getIsMarried());
		Assert.assertTrue(eve.getIsMarried());
		Assert.assertEquals(eve.getPermanentId(), adam.getSpouseId());
		Assert.assertEquals(adam.getPermanentId(), eve.getSpouseId());
		Assert.assertEquals(eve.getName(), adam.getSpouseName());
		Assert.assertEquals(adam.getName(), eve.getSpouseName());
		
		resetAllAndAssert();
		
		//***************************
		// player -> player marriage
		//***************************
		MarriageHandler.startMarriage(steve, alex);
		
		Assert.assertFalse(steveData.getIsEngaged());
		Assert.assertFalse(alexData.getIsEngaged());
		Assert.assertTrue(steveData.getIsMarried());
		Assert.assertTrue(alexData.getIsMarried());
		Assert.assertEquals(alexData.getPermanentId(), steveData.getSpousePermanentId());
		Assert.assertEquals(steveData.getPermanentId(), alexData.getSpousePermanentId());
		Assert.assertEquals(alex.getCommandSenderName(), steveData.getSpouseName());
		Assert.assertEquals(steve.getCommandSenderName(), alexData.getSpouseName());
		
		resetAllAndAssert();
	}
	
	private void resetAllAndAssert()
	{
		MarriageHandler.endMarriage(steve, alex);
		MarriageHandler.endMarriage(adam, eve);
		MarriageHandler.endMarriage(steve, eve);
		
		Assert.assertFalse(steveData.getIsEngaged());
		Assert.assertFalse(steveData.getIsMarried());
		Assert.assertEquals(0, steveData.getSpousePermanentId());
		Assert.assertEquals("none", steveData.getSpouseName());
		
		Assert.assertFalse(alexData.getIsEngaged());
		Assert.assertFalse(alexData.getIsMarried());
		Assert.assertEquals(0, alexData.getSpousePermanentId());
		Assert.assertEquals("none", alexData.getSpouseName());
		
		Assert.assertFalse(adam.getIsEngaged());
		Assert.assertFalse(adam.getIsMarried());
		Assert.assertEquals(0, adam.getSpouseId());
		Assert.assertEquals("none", adam.getSpouseName());
		
		Assert.assertFalse(eve.getIsEngaged());
		Assert.assertFalse(eve.getIsMarried());
		Assert.assertEquals(0, eve.getSpouseId());
		Assert.assertEquals("none", eve.getSpouseName());
	}
}
