package mca.ai;

import java.util.Random;

import mca.core.MCA;
import mca.core.minecraft.AchievementsMCA;
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
import net.minecraft.stats.Achievement;
import net.minecraft.util.EnumParticleTypes;
import radixcore.constant.Time;

public class AIProcreate extends AbstractAI
{
	private static final DataParameter<Boolean> IS_PROCREATING = EntityDataManager.<Boolean>createKey(EntityVillagerMCA.class, DataSerializers.BOOLEAN);
	
	private boolean hasHadTwins;
	private int procreateTicks;
	
	public AIProcreate(EntityVillagerMCA owner) 
	{
		super(owner);
		setIsProcreating(false);
	}

	@Override
	public void onUpdateClient() 
	{
		if (getIsProcreating())
		{
			owner.rotationYawHead += 40;
			Utilities.spawnParticlesAroundEntityC(EnumParticleTypes.HEART, owner, 2);
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
				owner.playSound(SoundEvents.ENTITY_CHICKEN_EGG, 1.0F, 1.0F);

				final EntityPlayer playerSpouse = owner.getPlayerSpouseInstance();
				
				if (playerSpouse != null)
				{
					NBTPlayerData data = MCA.getPlayerData(playerSpouse);
					data.setOwnsBaby(true);
					
					boolean isMale = new Random().nextBoolean();
					ItemStack stack = new ItemStack(isMale ? ItemsMCA.babyBoy : ItemsMCA.babyGirl);
					
					boolean isPlayerInventoryFull = playerSpouse.inventory.getFirstEmptyStack() == -1;
					
					if (isPlayerInventoryFull)
					{
						owner.getVillagerInventory().addItem(stack);
					}
					
					else
					{
						playerSpouse.inventory.addItemStackToInventory(stack);
					}
					
					Achievement achievement = isMale ? AchievementsMCA.babyBoy : AchievementsMCA.babyGirl;
					playerSpouse.addStat(achievement);
					
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
		owner.getDataManager().set(IS_PROCREATING, value);
	}
	
	public boolean getIsProcreating()
	{
		return owner.getDataManager().get(IS_PROCREATING);
	}
	
	public boolean getHasHadTwins()
	{
		return hasHadTwins;
	}
	
	public void setHasHadTwins(boolean value)
	{
		hasHadTwins = value;
	}
}
