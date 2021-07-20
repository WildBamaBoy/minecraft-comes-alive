package mca.entity.ai;

import java.util.function.Supplier;

import mca.MCA;
import mca.entity.ai.brain.sensor.ExplodingCreeperSensor;
import mca.mixin.MixinSensorType;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.util.Identifier;

public interface ActivityMCA {
    Activity CHORE = activity("chore");

    SensorType<ExplodingCreeperSensor> EXPLODING_CREEPER = sensor("exploding_creeper", ExplodingCreeperSensor::new);

    static void bootstrap() { }

    static Activity activity(String name) {
        return Activity.register(new Identifier(MCA.MOD_ID, name).toString());
    }


    static <T extends Sensor<?>> SensorType<T> sensor(String name, Supplier<T> factory) {
        return MixinSensorType.register(new Identifier(MCA.MOD_ID, name).toString(), factory);
    }
}
