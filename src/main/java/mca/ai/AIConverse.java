package mca.ai;

import mca.core.MCA;
import mca.data.WatcherIDsHuman;
import mca.entity.EntityHuman;
import net.minecraft.nbt.NBTTagCompound;
import radixcore.constant.Time;
import radixcore.data.WatchedBoolean;
import radixcore.data.WatchedInt;
import radixcore.util.RadixLogic;
import radixcore.util.RadixMath;

public class AIConverse extends AbstractAI 
{
	/* How often to attempt to start a conversation */
	private static final int START_INTERVAL = Time.SECOND * 30;

	/* How long until the conversation jumps to the next progress interval. */
	private static final int PROGRESS_INTERVAL = Time.SECOND * 4;

	private static final int NUMBER_OF_CONVERSATIONS = 2;
	
	private final WatchedInt conversationID;
	private final WatchedInt conversationProgress;
	private final WatchedInt conversationTarget;
	private final WatchedBoolean isConversationActive;

	private int timeUntilAdvance = START_INTERVAL;
	private int conversationSize = 0;
	
	public AIConverse(EntityHuman owner) 
	{
		super(owner);

		conversationID = new WatchedInt(0, WatcherIDsHuman.CONVERSATION_ID, owner.getDataWatcherEx());
		conversationProgress = new WatchedInt(0, WatcherIDsHuman.CONVERSATION_PROGRESS, owner.getDataWatcherEx());
		conversationTarget = new WatchedInt(0, WatcherIDsHuman.CONVERSATION_TARGET, owner.getDataWatcherEx());
		isConversationActive = new WatchedBoolean(false, WatcherIDsHuman.IS_CONVERSATION_ACTIVE, owner.getDataWatcherEx());
	}

	@Override
	public void onUpdateCommon() 
	{

	}

	@Override
	public void onUpdateClient() 
	{

	}

	@Override
	public void onUpdateServer() 
	{
		//TODO
//		if (owner.getAI(AISleep.class).getIsSleeping() && isConversationActive.getBoolean())
//		{
//			reset();
//		}
//		
//		if (conversationTarget.getInt() != 0 && !owner.getAI(AISleep.class).getIsSleeping())
//		{
//			if (timeUntilAdvance <= 0)
//			{
//				EntityHuman target = (EntityHuman) RadixLogic.getEntityByPermanentId(owner.worldObj, conversationTarget.getInt());
//
//				//Target may be null when checking distance.
//				if (target != null && RadixMath.getDistanceToEntity(owner, target) >= 4.0F)
//				{
//					target = null;
//				}
//
//				if (target != null)
//				{
//					owner.faceEntity(target, 1.0F, 1.0F);
//					conversationProgress.setValue(conversationProgress.getInt() + 2);
//
//					if (conversationProgress.getInt() > conversationSize)
//					{
//						reset();
//					}
//					
//					else
//					{
//						timeUntilAdvance = PROGRESS_INTERVAL;
//					}
//				}
//
//				else
//				{
//					reset();
//				}
//			}
//
//			else
//			{
//				timeUntilAdvance--;
//			}
//		}
//
//		else
//		{
//			if (timeUntilAdvance <= 0)
//			{
//				EntityHuman target = (EntityHuman) RadixLogic.getNearestEntityOfTypeWithinDistance(EntityHuman.class, owner, 4);
//				
//				if (target != null && !target.getAI(AIConverse.class).getConversationActive())
//				{
//					AIConverse conversation = target.getAI(AIConverse.class);
//					int conversationId = RadixMath.getNumberInRange(1, NUMBER_OF_CONVERSATIONS);
//					
//					isConversationActive.setValue(true);
//					conversationTarget.setValue(target.getPermanentId());
//					conversationID.setValue(conversationId);
//					conversationProgress.setValue(1);
//					conversationSize = MCA.getLanguageManager().getNumberOfPhrasesMatchingID("conversation" + conversationId);
//					
//					conversation.setConversationActive(true);
//					conversation.setConversationTarget(owner.getPermanentId());
//					conversation.setConversationID(conversationId);
//					conversation.setConversationProgress(0);
//					conversation.setConversationSize(conversationSize);
//					
//					timeUntilAdvance = PROGRESS_INTERVAL;
//					conversation.setConversationAdvanceTime(timeUntilAdvance / 2);
//				}
//
//				else
//				{
//					timeUntilAdvance = START_INTERVAL;
//				}
//			}
//
//			else
//			{
//				timeUntilAdvance--;
//			}
//		}
	}

	@Override
	public void reset() 
	{
		final EntityHuman target = (EntityHuman) RadixLogic.getEntityByPermanentId(owner.worldObj, conversationTarget.getInt());

		if (target != null)
		{
			AIConverse otherAI = target.getAI(AIConverse.class);
			otherAI.setConversationTarget(0); //To prevent infinite loop.
			otherAI.reset();
		}

		isConversationActive.setValue(false);
		conversationTarget.setValue(0);
		conversationProgress.setValue(0);
		conversationID.setValue(0);
		conversationSize = 0;
		
		timeUntilAdvance = START_INTERVAL;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setInteger("conversationID", conversationID.getInt());
		nbt.setInteger("conversationProgress", conversationProgress.getInt());
		nbt.setInteger("conversationTarget", conversationTarget.getInt());
		nbt.setBoolean("isConversationActive", isConversationActive.getBoolean());
		
		nbt.setInteger("timeUntilAdvance", timeUntilAdvance);
		nbt.setInteger("conversationSize", conversationSize);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		conversationID.setValue(nbt.getInteger("conversationID"));
		conversationProgress.setValue(nbt.getInteger("conversationProgress"));
		conversationTarget.setValue(nbt.getInteger("conversationTarget"));
		isConversationActive.setValue(nbt.getBoolean("isConversationActive"));
		
		timeUntilAdvance = nbt.getInteger("timeUntilAdvance");
		conversationSize = nbt.getInteger("conversationSize");
	}

	public void setConversationActive(boolean value)
	{
		isConversationActive.setValue(value);
	}

	public void setConversationID(int id) 
	{
		conversationID.setValue(id);
	}

	public void setConversationTarget(int id) 
	{
		conversationTarget.setValue(id);
	}

	public void setConversationProgress(int id) 
	{
		conversationProgress.setValue(id);
	}

	public void setConversationAdvanceTime(int value) 
	{
		timeUntilAdvance = value;
	}

	public void setConversationSize(int value)
	{
		conversationSize = value;
	}
	
	public boolean getConversationActive()
	{
		return isConversationActive.getBoolean();
	}

	public int getConversationID() 
	{
		return conversationID.getInt();
	}

	public int getConversationProgress() 
	{
		return conversationProgress.getInt();
	}
}
