package radixcore.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Creates a list that can be cycled through in order using next() and previous(). Does not permit duplicates or null values.
 */
public final class NumberCycleList extends ArrayList<Integer>
{
	private int index;
	
	public static NumberCycleList fromIntegers(Integer... integers)
	{
		return fromList(Arrays.asList(integers));
	}
	
	public static NumberCycleList fromList(List<Integer> list)
	{
		NumberCycleList returnList = new NumberCycleList();
		
		for (Integer i : list)
		{
			if (!returnList.contains(i) && i != null)
			{
				returnList.add(i);
			}
		}
		
		Collections.sort(returnList);
		return returnList;
	}
	
	public int next()
	{
		index++;

		if (index > size() - 1)
		{
			index = 0;
		}
		
		return get(index);
	}
	
	public int previous()
	{
		index--;
		
		if (index < 0)
		{
			index = size() - 1;
		}
		
		return get(index);
	}
	
	public int get()
	{
		return get(index);
	}
}
