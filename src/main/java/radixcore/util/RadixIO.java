package radixcore.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class RadixIO 
{
	public static void serializeToFile(File fileLocation, Object objectToSerialize)
	{
		try
		{
			if (!fileLocation.exists())
			{
				fileLocation.createNewFile();
			}

			final FileOutputStream fileOut = new FileOutputStream(fileLocation.getPath());
			final ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(objectToSerialize);
			objectOut.close();
		}

		catch (IOException e)
		{
			RadixExcept.logErrorCatch(e, "Failed to write object of type " + objectToSerialize.getClass().getSimpleName() + " to file!");
		}
	}

	public static Object deserializeFromFile(File fileLocation)
	{
		try
		{
			if (!fileLocation.exists())
			{
				fileLocation.createNewFile();
			}

			final FileInputStream fileIn = new FileInputStream(fileLocation.getPath());
			final ObjectInputStream objectIn = new ObjectInputStream(fileIn);
			final Object returnObject = objectIn.readObject();
			objectIn.close();
			
			return returnObject;
		}

		catch (IOException e)
		{
			RadixExcept.logErrorCatch(e, "Failed to read object from file located at " + fileLocation.getPath() + "!");
		} 
		
		catch (ClassNotFoundException e) 
		{
			RadixExcept.logFatalCatch(e, "ClassNotFound when reading object from file located at " + fileLocation.getPath() + "! Are all libraries present and up to date?");
		}
		
		return null;
	}

	private RadixIO()
	{
	}
}
