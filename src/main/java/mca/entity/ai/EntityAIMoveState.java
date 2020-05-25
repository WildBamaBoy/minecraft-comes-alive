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
		EnumMoveState moveState = EnumMoveState.byId(villager.get(EntityVillagerMCA.MOVE_STATE));
		Optional<Player> playerToFollow = villager.world.getPlayerEntityByUUID(villager.playerToFollowUUID);
		
		// We're supposed to be following someone and we cannot find that player.
		if (moveState == EnumMoveState.FOLLOW && !playerToFollow.isPresent()) {
			villager.playerToFollowUUID = Constants.ZERO_UUID;
			villager.set(EntityVillagerMCA.MOVE_STATE, EnumMoveState.MOVE.getId());
		}
		
		// We're supposed to be following someone and we are able to find that player.
		else if (moveState == EnumMoveState.FOLLOW && playerToFollow.isPresent()) {
			Player player = playerToFollow.get();
			double distance = playerToFollow != null ? villager.getDistance(player.getPlayer()) : -1.0D;
			if (distance >= 3.0D && distance <= 10.0D) {
				nav.setPath(nav.getPathToEntityLiving(player.getPlayer()), villager.isRiding() ? 1.7D : 0.8D);
			} else if (distance > 10.0D) {
				villager.attemptTeleport(player.getPosX(), player.getPosY(), player.getPosZ());
			} else { // close enough to avoid crowding the player
				nav.clearPath();
			}
		}
		
		// Stay logic, just constantly clear the navigator path.
		else if (moveState == EnumMoveState.STAY) {
			nav.clearPath();
		}
	}
}