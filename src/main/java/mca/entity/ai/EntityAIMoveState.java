package mca.entity.ai;

import java.util.Optional;

import mca.api.objects.Player;
import mca.core.Constants;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumMoveState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigate;

public class EntityAIMoveState extends EntityAIBase {
	private final EntityVillagerMCA villager;

	public EntityAIMoveState(EntityVillagerMCA entityIn) {
		this.villager = entityIn;
		this.setMutexBits(1);
	}

	public boolean shouldExecute() {
		return !villager.playerToFollowUUID.equals(Constants.ZERO_UUID) || EnumMoveState.byId(villager.get(EntityVillagerMCA.MOVE_STATE)) == EnumMoveState.STAY;
	}

	public void updateTask() {
		PathNavigate nav = villager.getNavigator();
		Optional<Player> playerToFollow = villager.world.getPlayerEntityByUUID(villager.playerToFollowUUID);
		playerToFollow.ifPresent(p ->{
			switch (EnumMoveState.byId(villager.get(EntityVillagerMCA.MOVE_STATE))) {
			case FOLLOW:
				double distance = playerToFollow != null ? villager.getDistance(p.getPlayer()) : -1.0D;
				if (distance >= 3.0D && distance <= 10.0D) {
					nav.setPath(nav.getPathToEntityLiving(p.getPlayer()), villager.isRiding() ? 1.7D : 0.8D);
				} else if (distance > 10.0D) {
					villager.attemptTeleport(p.getPosX(), p.getPosY(), p.getPosZ());
				} else { // close enough to avoid crowding the player
					nav.clearPath();
				}
				break;
			case STAY:
				nav.clearPath();
				break;
			}		
		});
		
		if (!playerToFollow.isPresent()) {
			villager.set(EntityVillagerMCA.MOVE_STATE, EnumMoveState.MOVE.getId());
		}
	}
}