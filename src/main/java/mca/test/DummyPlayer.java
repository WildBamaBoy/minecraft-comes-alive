package mca.test;

import java.lang.reflect.Field;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import mca.core.MCA;
import mca.data.PlayerData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import sun.misc.Unsafe;

/**
 * A simple fake player that is used during unit testing.
 */
public class DummyPlayer extends EntityPlayer
{
	private static UUID steveUUID = new UUID(0L, 0L);
	private static UUID alexUUID = new UUID(1L, 1L);
	private boolean isSteve;
	
	private DummyPlayer(DummyWorld world)
	{
		//This is never run.
		super(world, new GameProfile(steveUUID, "Steve"));
	}

	public static DummyPlayer getDummyPlayer(boolean isSteve)
	{
		// We're not concerned with the default data assigned to an EntityPlayer. We mainly need the instance, 
		// and can override methods to return most control data we need to work with.
		
		// In that case, use Unsafe to create an instance of our DummyPlayer. While EntityPlayer is being constructed, 
		// the test will fail with a NPE due to the early initialization of the CraftingManager, which tries to 
		// add recipes for blocks and items that do not yet exist.
		try
		{
			DummyPlayer player = (DummyPlayer) getUnsafe().allocateInstance(DummyPlayer.class);

			// We will manually assign control data to prevent crashes during testing.
			player.setWorld(new DummyWorld());
			player.isSteve = isSteve;
						
			if (isSteve)
			{
				MCA.stevePlayerData = new PlayerData(player);
				MCA.stevePlayerData.initializeNewData(player);
			}
			
			else
			{
				MCA.alexPlayerData = new PlayerData(player);
				MCA.alexPlayerData.initializeNewData(player);
			}
			
			return player;
		}
		
		catch (Exception e)
		{
			return null;
		}
	}
	
	public boolean getIsSteve()
	{
		return isSteve;
	}
	
	@Override
	public UUID getUniqueID() 
	{
		return isSteve ? steveUUID : alexUUID;
	}

	@Override
	public String getName() 
	{
		return isSteve ? "Steve" : "Alex";
	}

	@Override
	public void addChatMessage(ITextComponent chatComponent) 
	{
		return; //Not used, required by implementation.
	}

	@Override
	public boolean canCommandSenderUseCommand(int opLevel, String command) 
	{
		return false; //Not used, required by implementation.
	}
	
	private static Unsafe getUnsafe()
	{
		//This skips over the SecurityException thrown when accessing Unsafe.
		try
		{
			Field unsafe = Unsafe.class.getDeclaredField("theUnsafe");
			unsafe.setAccessible(true);
			return (Unsafe) unsafe.get(null);
		}
		
		catch (Exception e) { }
		
		return null;
	}

	@Override
	public boolean isSpectator() 
	{
		return false;
	}

	@Override
	public boolean isCreative() 
	{
		return false;
	}
}
