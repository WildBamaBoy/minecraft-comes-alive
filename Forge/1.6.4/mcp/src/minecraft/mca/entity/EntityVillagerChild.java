/*******************************************************************************
 * EntityVillagerChild.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mca.entity;

import mca.api.VillagerEntryMCA;
import mca.api.VillagerRegistryMCA;
import mca.core.MCA;
import mca.core.util.object.PlayerMemory;
import mca.enums.EnumRelation;
import mca.item.ItemVillagerEditor;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * Defines a villager child and how it behaves.
 */
public class EntityVillagerChild extends EntityChild
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
	 * @param 	gender			The entity's desired gender.
	 * @param 	professionID	The entity's desired profession.
	 */
	public EntityVillagerChild(World world, String gender, int professionID)
	{
		super(world);

		this.name = getRandomName(gender);
		this.profession = professionID;
		this.gender = gender;
		
		if (profession == 4) //Butcher
		{
			//There are no female skins for butchers. Always make them Male.
			this.gender = "Male";
			this.name = getRandomName(gender);
		}

		this.setTexture();
	}

	@Override
	public void addAI() 
	{
		this.getNavigator().setBreakDoors(true);
		this.getNavigator().setAvoidsWater(true);
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAIAvoidEntity(this, EntityZombie.class, 8.0F, 0.6F, 0.35F));
		this.tasks.addTask(2, new EntityAIMoveIndoors(this));
		this.tasks.addTask(3, new EntityAIRestrictOpenDoor(this));
		this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
		this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 0.6F));
		this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
		this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityVillagerAdult.class, 5.0F, 0.02F));
		this.tasks.addTask(9, new EntityAIWander(this, 0.6F));
		this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityLivingBase.class, 8.0F));
	}

	@Override
	public String getCharacterType(int playerId) 
	{
		if (familyTree.getRelationOf(playerId) == EnumRelation.Grandparent || familyTree.getRelationOf(playerId) == EnumRelation.Greatgrandparent)
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
					MCA.instance.log(e);
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
		ItemStack itemStack = player.inventory.getCurrentItem();

		//Players get added to the playerMemory map when they interact with an entity.
		if (!playerMemoryMap.containsKey(player.username))
		{
			playerMemoryMap.put(player.username, new PlayerMemory(player.username));
		}
		
		PlayerMemory memory = playerMemoryMap.get(player.username);
		
		if (itemStack != null)
		{
			if (itemStack.getItem() instanceof ItemVillagerEditor)
			{
				player.openGui(MCA.instance, MCA.instance.guiVillagerEditorID, worldObj, (int)posX, (int)posY, (int)posZ);
				return true;
			}
		}
		
		if (!memory.isInGiftMode)
		{			
			player.openGui(MCA.instance, MCA.instance.guiInteractionVillagerChildID, worldObj, (int)posX, (int)posY, (int)posZ);
		}

		else if (itemStack != null)
		{
			memory.isInGiftMode = false;
			playerMemoryMap.put(player.username, memory);
			
			if (worldObj.isRemote)
			{
				doGift(itemStack, player);
			}
		}

		return false;
	}
}
