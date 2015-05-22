package mca.api.enums;

/**
 * The gift category determines at which relationship level
 * that a particular gift will have the chance of being given
 * to a player.
 */
public enum EnumGiftCategory 
{
	/** Relationship less than zero (no or black hearts) */
	BAD,
	/** Relationship from 0 - 25 (no or 3 red hearts)*/
	GOOD,
	/** Relationship from 26 - 50 (3 red hearts - 5 red hearts)*/
	BETTER,
	/** Relationship from 51 - inf (5 red hearts - any gold hearts)*/
	BEST;
}
