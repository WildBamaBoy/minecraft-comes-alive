/*******************************************************************************
 * EntityVillagerChild.java
 * Copyright (c) 2014 Radix-Shock Entertainment.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.entity;

import mca.core.Constants;
import mca.core.MCA;
import mca.core.util.Utility;
import mca.core.util.object.PlayerMemory;
import mca.enums.EnumRelation;
import mca.item.ItemVillagerEditor;
import mca.network.packets.PacketOpenGui;
import mca.network.packets.PacketSetFieldValue;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.radixshock.radixcore.logic.LogicHelper;

/**
 * Defines a villager child and how it behaves.
 */
public class EntityVillagerChild extends AbstractChild
{
	/**
	 * Constructor
	 * 
	 * @param 	world	The world the entity should be spawned in.
	 */
	public EntityVillagerChild(World world)
	{
		super(world);
	}

	/**
	 * Constructor
	 * 
	 * @param 	world			The world that the entity should be spawned in.
	 * @param 	isMale			Is the entity male?
	 * @param 	professionID	The entity's desired profession.
	 */
	public EntityVillagerChild(World world, boolean isMale, int professionID)
	{
		this(world);
		
		this.isMale = isMale;
		this.name = Utility.getRandomName(isMale);
		this.profession = professionID;
		
		if (profession == 4) //Butcher
		{
			//There are no female skins for butchers. Always make them Male.
			this.isMale = true;
			this.name = Utility.getRandomName(this.isMale);
		}

		this.setTexture();
	}

	@Override
	public void addAI() 
	{
		this.getNavigator().setBreakDoors(true);
		this.getNavigator().setAvoidsWater(false);
		this.getNavigator().setCanSwim(true);
		
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAIAvoidEntity(this, EntityZombie.class, 8.0F, Constants.SPEED_WALK, 0.35F));
		this.tasks.addTask(2, new EntityAIMoveIndoors(this));
		this.tasks.addTask(3, new EntityAIRestrictOpenDoor(this));
		this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
		this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, Constants.SPEED_WALK));
		this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
		this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityVillagerAdult.class, 5.0F, 0.02F));
		this.tasks.addTask(9, new EntityAIWander(this, Constants.SPEED_WALK));
		this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityLivingBase.class, 8.0F));
	}

	@Override
	public String getCharacterType(int playerId) 
	{
		final EnumRelation relation = familyTree.getRelationOf(playerId);
		
		if (relation == EnumRelation.Grandparent || relation == EnumRelation.Greatgrandparent || relation == EnumRelation.Grandfather ||
			relation == EnumRelation.Grandmother || relation == EnumRelation.Greatgrandfather || relation == EnumRelation.Greatgrandmother)
		{
			return "grandchild";
		}
		
		else
		{
			return "villagerchild";
		}
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		//Check for growing up into an adult.
		if (isReadyToGrow)
		{
			if (!worldObj.isRemote)
			{
				EntityVillagerAdult villager = new EntityVillagerAdult(worldObj, this);
				villager.setLocationAndAngles(posX, posY, posZ, rotationPitch, rotationYaw);
				
				try
				{
					worldObj.spawnEntityInWorld(villager);
				}
				
				//Rare entity is already tracked error.
				catch (IllegalStateException e)
				{
					MCA.getInstance().getLogger().log(e);
				}
			}

			setDeadWithoutNotification();
		}
		
		combatChore.useMelee = false;
		combatChore.useRange = false;
	}

	@Override
	public boolean interact(EntityPlayer player)
	{
		super.interact(player);
		
		if (!worldObj.isRemote)
		{
			final PlayerMemory memory = playerMemoryMap.get(player.getCommandSenderName());
			final ItemStack itemStack = player.inventory.getCurrentItem();

			if (itemStack != null) //Items here will always perform their functions regardless of the entity's state.
			{
				if (itemStack.getItem() instanceof ItemVillagerEditor)
				{
					MCA.packetHandler.sendPacketToPlayer(new PacketOpenGui(getEntityId(), Constants.ID_GUI_EDITOR), (EntityPlayerMP)player);
					return true;
				}
			}

			if (!memory.isInGiftMode || memory.isInGiftMode && itemStack == null) //When right clicked in gift mode without an item to give or when out of gift mode.
			{
				MCA.packetHandler.sendPacketToPlayer(new PacketOpenGui(getEntityId(), Constants.ID_GUI_VCHILD), (EntityPlayerMP)player);
			}

			else if (itemStack != null && memory.isInGiftMode) //When the player right clicks with an item and entity is in gift mode.
			{
				memory.isInGiftMode = false;
				playerMemoryMap.put(player.getCommandSenderName(), memory);
				
				if (itemStack.getItem() instanceof ItemAppleGold)
				{
					this.age += LogicHelper.getNumberInRange(30, 90);
					MCA.packetHandler.sendPacketToAllPlayers(new PacketSetFieldValue(getEntityId(), "age", age));
				}
				
				else
				{
					doGift(itemStack, player);
				}

				MCA.packetHandler.sendPacketToPlayer(new PacketSetFieldValue(getEntityId(), "playerMemoryMap", playerMemoryMap), (EntityPlayerMP)player);
			}
		}

		return super.interact(player);
	}
}
