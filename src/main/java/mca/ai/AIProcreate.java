package mca.ai;

import java.util.Random;

import mca.core.MCA;
import mca.core.minecraft.ModAchievements;
import mca.core.minecraft.ModItems;
import mca.data.NBTPlayerData;
import mca.data.WatcherIDsHuman;
import mca.entity.EntityHuman;
import mca.packets.PacketOpenBabyNameGUI;
import mca.util.Utilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.Achievement;
import net.minecraft.util.EnumParticleTypes;
import radixcore.constant.Time;
import radixcore.data.WatchedBoolean;

public class AIProcreate extends AbstractAI
{
	private WatchedBoolean isProcreating;
	
	private boolean hasHadTwins;
	private int procreateTicks;
	
	public AIProcreate(EntityHuman owner) 
	{
		super(owner);
		
		isProcreating = new WatchedBoolean(false, WatcherIDsHuman.IS_PROCREATING, owner.getDataWatcherEx());
	}

	@Override
	public void onUpdateCommon() 
	{
	}

	@Override
	public void onUpdateClient() 
	{
		if (isProcreating.getBoolean())
		{
			owner.rotationYawHead += 40;
			Utilities.spawnParticlesAroundEntityC(EnumParticleTypes.HEART, owner, 2);
		}
	}

	@Override
	public void onUpdateServer() 
	{
		if (isProcreating.getBoolean())
		{
			procreateTicks++;
			
			if (procreateTicks >= Time.SECOND * 3)
			{
				isProcreating.setValue(false);
				procreateTicks = 0;
				owner.playSound(SoundEvents.entity_chicken_egg, 1.0F, 1.0F);

				final EntityPlayer playerSpouse = owner.getPlayerSpouse();
				
				if (playerSpouse != null)
				{
					NBTPlayerData data = MCA.getPlayerData(playerSpouse);
					data.setShouldHaveBaby(true);
					
					boolean isMale = new Random().nextBoolean();
					ItemStack stack = new ItemStack(isMale ? ModItems.babyBoy : ModItems.babyGirl);
					
					boolean isPlayerInventoryFull = playerSpouse.inventory.getFirstEmptyStack() == -1;
					
					if (isPlayerInventoryFull)
					{
						owner.getVillagerInventory().addItemStackToInventory(stack);
					}
					
					else
					{
						playerSpouse.inventory.addItemStackToInventory(stack);
					}
					
					Achievement achievement = isMale ? ModAchievements.babyBoy : ModAchievements.babyGirl;
					playerSpouse.addStat(achievement);
					
					MCA.getPacketHandler().sendPacketToPlayer(new PacketOpenBabyNameGUI(isMale), (EntityPlayerMP) playerSpouse);
				}
			}
		}
	}

	@Override
	public void reset() 
	{
		
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setBoolean("isProcreating", isProcreating.getBoolean());
		nbt.setBoolean("hasHadTwins", hasHadTwins);
		nbt.setInteger("procreateTicks", procreateTicks);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		isProcreating.setValue(nbt.getBoolean("isProcreating"));
		hasHadTwins = nbt.getBoolean("hasHadTwins");
		procreateTicks = nbt.getInteger("procreateTicks");
	}

	public void setIsProcreating(boolean value)
	{
		this.isProcreating.setValue(true);
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
