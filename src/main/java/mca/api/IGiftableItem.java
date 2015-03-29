package mca.api;

/**
 * Defines an item as 'giftable' to an MCA villager.
 */
public interface IGiftableItem 
{
	/**
	 * Should return the gift value for one of your item. This will increase the player's
	 * relation ship by x points.
	 * 
	 * Hearts displayed above the villager's head represent one heart per 10 relationship points
	 * up to a maximum of 5 hearts. After 5 hearts, each heart will become golden at 60 points, 
	 * 70 points, etc.
	 */
	int getGiftValue();
}
