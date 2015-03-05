package mca.api.exception;

/**
 * Exception that is thrown when a mapping for data concerning a particular chore is not found.
 * Used to stop an active chore if a mod that adds something to the chore is removed.
 */
public final class MappingNotFoundException extends Exception
{
	public MappingNotFoundException()
	{
		super();
	}

	public MappingNotFoundException(String message) 
	{ 
		super(message); 
	}

	public MappingNotFoundException(String message, Throwable cause) 
	{ 
		super(message, cause); 
	}

	public MappingNotFoundException(Throwable cause) 
	{ 
		super(cause); 
	}
}
