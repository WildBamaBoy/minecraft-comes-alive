package radixcore.lang;

public abstract class AbstractLanguageParser
{
	public abstract String parsePhrase(String id, Object[] arguments);
	
	public final Object getArgumentOfType(Object[] arguments, Class argumentType)
	{
		return getArgumentOfType(arguments, argumentType, 1);
	}
	
	public final Object getArgumentOfType(Object[] arguments, Class argumentType, int number)
	{
		int counter = 1;
		
		for (Object obj : arguments)
		{
			//!= null accounts for no arguments.
			if (obj != null && argumentType.isAssignableFrom(obj.getClass()))
			{
				if (counter == number)
				{
					return obj;
				}
				
				else
				{
					counter++;
				}
			}
		}
		
		return null;
	}
}
