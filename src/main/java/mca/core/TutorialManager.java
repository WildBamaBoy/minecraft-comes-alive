package mca.core;

import mca.packets.PacketSetTutorialMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public final class TutorialManager 
{
	private static boolean firstMessageShown;
	private static boolean turnOffMessageShown;
	private static int timeSinceFirstMessage;
	
	private static TutorialMessage currentMessage;
	private static TutorialMessage prevMessage;
	
	private static int prevAnimationProgress = 0;
	private static int animationProgress = 0;
	private static int timeAnimationHeld = 0;
	private static int yMax = 24;

	/* 0 = message moving down, 1 = message holding, 2 = message retracting */
	private static int state = 0;

	private TutorialManager()
	{
	}

	public static void onUpdate()
	{
		if (MCA.getConfig().inTutorialMode)
		{
			if (firstMessageShown && !turnOffMessageShown)
			{
				timeSinceFirstMessage++;
				
				if (timeSinceFirstMessage >= 500)
				{
					setTutorialMessage(new TutorialMessage("You can turn off these messages at the Main Menu.", "Select Mods->Minecraft Comes Alive->Config->Tutorial mode->false."));
					turnOffMessageShown = true;
				}
			}
			
			if (currentMessage != null)
			{
				if (state == 0)
				{
					animationProgress++;

					if (animationProgress >= yMax)
					{
						state = 1;
					}
				}

				else if (state == 1)
				{
					timeAnimationHeld++;

					if (timeAnimationHeld >= 400)
					{
						state = 2;
					}
				}

				else if (state == 2)
				{
					animationProgress--;

					if (animationProgress <= 0)
					{
						currentMessage = null;
						return;
					}
				}

				currentMessage.draw(animationProgress);
			}
			
			if (prevMessage != null)
			{
				prevAnimationProgress--;

				if (prevAnimationProgress <= 0)
				{
					prevMessage = null;
					return;
				}
				
				prevMessage.draw(prevAnimationProgress);
			}
		}
	}

	public static void setTutorialMessage(TutorialMessage message)
	{
		firstMessageShown = true;
		
		if (currentMessage != null)
		{
			prevMessage = currentMessage;
			prevAnimationProgress = animationProgress;
		}
		
		animationProgress = 0;
		timeAnimationHeld = 0;
		state = 0;
		currentMessage = message;
	}

	public static void forceState(int newState)
	{
		state = newState;
	}

	public static TutorialMessage getCurrentMessage() 
	{
		return currentMessage;
	}
	
	public static void sendMessageToPlayer(EntityPlayer player, String line1, String line2)
	{
		MCA.getPacketHandler().sendPacketToPlayer(new PacketSetTutorialMessage(new TutorialMessage(line1, line2)), (EntityPlayerMP) player);
	}
}
