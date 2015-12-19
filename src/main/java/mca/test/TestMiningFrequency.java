package mca.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import mca.core.MCA;
import radixcore.util.RadixLogic;

/**
 * This test will output results from the mining chore simulated over different periods of time.
 */
public class TestMiningFrequency 
{
	private Map<Integer, DummyMiningEntry> miningEntries;
	
	@Before
	public void init()
	{
		MCA.initializeForTesting();
		miningEntries = new HashMap<Integer, DummyMiningEntry>();
		
		miningEntries.put(1, new DummyMiningEntry("COAL", 0.45F));
		miningEntries.put(2, new DummyMiningEntry("IRON", 0.4F));
		miningEntries.put(3, new DummyMiningEntry("LAPIS", 0.3F));
		miningEntries.put(4, new DummyMiningEntry("GOLD", 0.05F));
		miningEntries.put(5, new DummyMiningEntry("DIAMOND", 0.04F));
		miningEntries.put(6, new DummyMiningEntry("EMERALD", 0.03F));
		miningEntries.put(7, new DummyMiningEntry("QUARTZ", 0.02F));
		miningEntries.put(8, new DummyMiningEntry("ROSE GOLD", 0.15F));
	}
	
	@Test
	public void test()
	{
		System.out.println("Results over 1 minute:");
		simulateResults(60);
		
		System.out.println("Results over 1 hour:");
		simulateResults(3600);
		
		System.out.println("Results over 1 day:");
		simulateResults(86400);
	}
	
	private void simulateResults(int seconds)
	{
		List<DummyStack> results = new ArrayList<DummyStack>();
		
		for (int i = 0; i < seconds; i++)
		{
			DummyStack result = getHarvestStackTest();
			
			if (result != null)
			{
				results.add(result);
			}
		}
		
		printResults(results);
	}
	
	private void printResults(List<DummyStack> results)
	{
		Map<String, Integer> map = new HashMap<String, Integer>();
		
		for (DummyStack stack : results)
		{
			if (map.containsKey(stack.getStackName()))
			{
				map.put(stack.getStackName(), map.get(stack.getStackName()).intValue() + 1);
			}
			
			else
			{
				map.put(stack.getStackName(), 1);
			}
		}
		
		for (Map.Entry<String, Integer> entry : map.entrySet())
		{
			System.out.println(entry.getKey() + ": " + entry.getValue());
		}
	}
	
	private DummyStack getHarvestStackTest()
	{
		boolean doHarvest = RadixLogic.getBooleanWithProbability(25);

		if (doHarvest)
		{
			DummyStack addStack = null;
			boolean getSpecialOre = RadixLogic.getBooleanWithProbability(3);

			if (getSpecialOre)
			{
				float totalWeight = 0.0F;
				int index = -1;
				
				//Sum up the total weight of all entries.
				for (DummyMiningEntry entry : miningEntries.values())
				{
					totalWeight += entry.percentileWeight();
				}
				
				//Apply randomness.
				float random = (float) (Math.random() * totalWeight);
				
				// Subtract the weight of each item until we are at or less than zero. That entry
				// is the one we add.
				for (Map.Entry<Integer, DummyMiningEntry> entry : miningEntries.entrySet())
				{
					random -= entry.getValue().percentileWeight();
					
					if (random <= 0.0F)
					{
						index = entry.getKey();
						break;
					}
				}
				
				addStack = new DummyStack(miningEntries.get(index).getName(), 1);
			}

			else
			{
				addStack = new DummyStack("COBBLE", 1);
			}
			
			return addStack;
		}
		
		return null;
	}
}
