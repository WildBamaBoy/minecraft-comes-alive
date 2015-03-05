package mca.api.exception;

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
