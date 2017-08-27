package mca.actions;

import java.util.Random;

import mca.core.MCA;
import mca.core.minecraft.ItemsMCA;
import mca.data.NBTPlayerData;
import mca.entity.EntityVillagerMCA;
import mca.packets.PacketOpenBabyNameGUI;
import mca.util.Utilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import radixcore.constant.Time;

public class ActionProcreate extends AbstractAction
{
	private static final DataParameter<Boolean> IS_PROCREATING = EntityDataManager.<Boolean>createKey(EntityVillagerMCA.class, DataSerializers.BOOLEAN);
	
	private boolean hasHadTwins;
	private int procreateTicks;
	
	public ActionProcreate(EntityVillagerMCA actor) 
	{
		super(actor);
		setIsProcreating(false);
	}

	@Override
	public void onUpdateClient() 
	{
		if (getIsProcreating())
		{
			actor.rotationYawHead += 40;
			Utilities.spawnParticlesAroundEntityC(EnumParticleTypes.HEART, actor, 2);
		}
	}

	@Override
	public void onUpdateServer() 
	{
		if (getIsProcreating())
		{
			procreateTicks++;
			
			if (procreateTicks >= Time.SECOND * 3)
			{
				setIsProcreating(false);
				procreateTicks = 0;
				actor.playSound(SoundEvents.ENTITY_CHICKEN_EGG, 1.0F, 1.0F);

				final EntityPlayer playerSpouse = actor.attributes.getPlayerSpouseInstance();
				
				if (playerSpouse != null)
				{
					NBTPlayerData data = MCA.getPlayerData(playerSpouse);
					data.setOwnsBaby(true);
					
					boolean isMale = new Random().nextBoolean();
					ItemStack stack = new ItemStack(isMale ? ItemsMCA.BABY_BOY : ItemsMCA.BABY_GIRL);
					
					boolean isPlayerInventoryFull = playerSpouse.inventory.getFirstEmptyStack() == -1;
					
					if (isPlayerInventoryFull)
					{
						actor.attributes.getInventory().addItem(stack);
					}
					
					else
					{
						playerSpouse.inventory.addItemStackToInventory(stack);
					}
					
					//Achievement achievement = isMale ? AchievementsMCA.babyBoy : AchievementsMCA.babyGirl;
					//playerSpouse.addStat(achievement);

					MCA.getPacketHandler().sendPacketToPlayer(new PacketOpenBabyNameGUI(isMale), (EntityPlayerMP) playerSpouse);
				}
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setBoolean("isProcreating", getIsProcreating());
		nbt.setBoolean("hasHadTwins", hasHadTwins);
		nbt.setInteger("procreateTicks", procreateTicks);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		setIsProcreating(nbt.getBoolean("isProcreating"));
		hasHadTwins = nbt.getBoolean("hasHadTwins");
		procreateTicks = nbt.getInteger("procreateTicks");
	}

	public void setIsProcreating(boolean value)
	{
		actor.getDataManager().set(IS_PROCREATING, value);
	}
	
	public boolean getIsProcreating()
	{
		return actor.getDataManager().get(IS_PROCREATING);
	}
	
	public boolean getHasHadTwins()
	{
		return hasHadTwins;
	}
	
	public void setHasHadTwins(boolean value)
	{
		hasHadTwins = value;
	}
	
	protected void registerDataParameters()
	{
		actor.getDataManager().register(IS_PROCREATING, false);
	}
}
