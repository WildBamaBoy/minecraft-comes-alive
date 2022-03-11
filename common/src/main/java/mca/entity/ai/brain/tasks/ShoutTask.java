package mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import java.util.function.Predicate;
import mca.entity.VillagerEntityMCA;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;

public class ShoutTask extends Task<VillagerEntityMCA> {
    private final String phrase;
    private final int interval;
    private final Predicate<VillagerEntityMCA> condition;

    private long lastShout;

    public ShoutTask(String phrase, int interval, Predicate<VillagerEntityMCA> condition) {
        super(ImmutableMap.of());
        this.phrase = phrase;
        this.interval = interval;
        this.condition = condition;
    }

    protected boolean shouldRun(ServerWorld world, VillagerEntityMCA entity) {
        return entity.world.getTime() - lastShout > interval && condition.test(entity);
    }

    protected void run(ServerWorld world, VillagerEntityMCA entity, long time) {
        entity.sendChatToAllAround(phrase);
        lastShout = entity.world.getTime();
    }
}
