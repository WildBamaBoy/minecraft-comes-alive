package mca.core;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;

public final class Constants 
{
	public static final DataParameter<Integer> OVERWRITE_KEY = EntityDataManager.<Integer>createKey(EntityVillager.class, DataSerializers.VARINT);
	
	public static final int GUI_ID_NAMEBABY = 1;
	public static final int GUI_ID_SETUP = 2;
	public static final int GUI_ID_TOMBSTONE = 3;
	public static final int GUI_ID_PLAYERMENU = 4;
	public static final int GUI_ID_INVENTORY = 5;
	public static final int GUI_ID_EDITOR = 6;
	public static final int GUI_ID_INTERACT = 7;
	
	public static final float SPEED_SNEAK = 0.4F;
	public static final float SPEED_WALK = 0.6F;
	public static final float SPEED_RUN = 0.7F;
	public static final float SPEED_SPRINT = 0.8F;
	public static final float SPEED_HORSE_RUN = 1.4F;

	public static final float SCALE_M_ADULT = 0.9375F;
	public static final float SCALE_F_ADULT = 0.915F;
	public static final float SCALE_MAX = 1.1F;
	public static final float SCALE_MIN = 0.85F;

	
	private Constants()
	{
	}
}
