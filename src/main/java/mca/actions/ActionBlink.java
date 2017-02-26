package mca.actions;

import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import radixcore.constant.Time;
import radixcore.modules.RadixMath;

public class ActionBlink extends AbstractAction
{
	public boolean holdingBlink;
	public int timeSinceLastBlink;
	public int timeHeldBlink;
	public int nextBlink;

	public ActionBlink(EntityVillagerMCA actor) 
	{
		super(actor);
	}

	@Override
	public void onUpdateServer() 
	{
		if (MCA.getConfig().allowBlinking && !actor.getBehavior(ActionSleep.class).getIsSleeping() && actor.getHealth() > 0.0F)
		{
			timeSinceLastBlink++;

			if (holdingBlink)
			{
				timeHeldBlink++;

				if (timeHeldBlink >= 2)
				{
					timeHeldBlink = 0;
					holdingBlink = false;
					timeSinceLastBlink = 0;
					nextBlink = RadixMath.getNumberInRange(Time.SECOND * 2, Time.SECOND * 8);
					actor.getBehavior(ActionSleep.class).transitionSkinState(false);
				}
			}

			else if (timeSinceLastBlink >= nextBlink)
			{
				actor.getBehavior(ActionSleep.class).transitionSkinState(true);
				holdingBlink = true;
			}
		}
	}
}
