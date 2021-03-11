//package cobalt.minecraft.network.datasync;
//
//import cobalt.core.Cobalt;
//import cobalt.minecraft.nbt.CNBT;
//import com.minecraftcomesalive.mca.entity.EntityVillagerMCA;
//import lombok.Getter;
//import net.minecraft.entity.Entity;
//import net.minecraft.item.ItemStack;
//import net.minecraft.nbt.CompoundNBT;
//import net.minecraft.network.PacketBuffer;
//import net.minecraft.network.datasync.DataParameter;
//import net.minecraft.network.datasync.DataSerializers;
//import net.minecraft.network.datasync.EntityDataManager;
//import net.minecraft.network.datasync.IDataSerializer;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.text.ITextComponent;
//import net.minecraft.util.text.StringTextComponent;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//
//public class CDataManager {
//    @Getter private EntityDataManager mcManager;
//
//    public static final IDataSerializer<CNBT> CNBT_SERIALIZER = new IDataSerializer<CNBT>() {
//        public void write(PacketBuffer buf, CNBT value) {
//            buf.writeCompoundTag(value.getMcCompound());
//        }
//        public CNBT read(PacketBuffer buf) {
//            return CNBT.fromMC(buf.readCompoundTag());
//        }
//        public CNBT copyValue(CNBT value) {
//            return value.copy();
//        }
//    };
//
//    static {
//        DataSerializers.registerSerializer(CNBT_SERIALIZER);
//    }
//
//    private HashMap<String, EntityDataManager.DataEntry<?>> registeredParams;
//
//    private CDataManager(EntityDataManager vanillaManager) {
//        this.mcManager = vanillaManager;
//        this.registeredParams = new HashMap<>();
//    }
//
//    public static CDataManager fromMC(EntityDataManager vanillaManager) {
//        return new CDataManager(vanillaManager);
//    }
//
//    public static CDataManager createNew(Entity entity) {
//        return fromMC(new EntityDataManager(entity));
//    }
//
//    public static <T> CDataParameter<T> createKey(Class <? extends Entity> clazz, IDataSerializer<T> serializer) {
//        return CDataParameter.fromMC(EntityDataManager.createKey(clazz, serializer));
//    }
//
//    public <T> void register(CDataParameter<T> key, T value, String name) {
//        mcManager.register(key.getMcParameter(), value);
//        registeredParams.put(name,  new EntityDataManager.DataEntry<T>(key.getMcParameter(), value));
//    }
//
//    public <T> T get(CDataParameter<T> key) {
//        return mcManager.get(key.getMcParameter());
//    }
//
//    public <T> void set(CDataParameter<T> key, T value) {
//        mcManager.set(key.getMcParameter(), value);
//    }
//
//    public <T> void setDirty(DataParameter<T> key) {
//        // NO-OP, vanilla set() now marks dirty.
//        // mcManager.setDirty(key);
//    }
//
//    public <T> void saveAll(CNBT nbt) {
//        for (Map.Entry<String, EntityDataManager.DataEntry<?>> entry : registeredParams.entrySet()) {
//            DataParameter<?> param = entry.getValue().getKey();
//            IDataSerializer<?> serializer = param.getSerializer();
//            String paramName = entry.getKey();
//            T paramValue = (T) mcManager.get(param);
//
//            if (serializer == DataSerializers.VARINT) {
//                nbt.setInteger(paramName, (Integer)paramValue);
//            } else if (serializer == DataSerializers.FLOAT) {
//                nbt.setFloat(paramName, (Float)paramValue);
//            } else if (serializer == DataSerializers.STRING) {
//                nbt.setString(paramName, (String)paramValue);
//            } else if (serializer == DataSerializers.TEXT_COMPONENT) {
//                nbt.setString(paramName, ((ITextComponent)paramValue).getString());
//            } else if  (serializer == DataSerializers.ITEMSTACK) {
//                nbt.setTag(paramName, CNBT.fromMC(((ItemStack)paramValue).write(new CompoundNBT())));
//            } else if (serializer == DataSerializers.BOOLEAN) {
//                nbt.setBoolean(paramName, (Boolean)paramValue);
//            } else if (serializer == DataSerializers.BLOCK_POS) {
//                BlockPos pos = (BlockPos)paramValue;
//                nbt.setInteger(paramName + "X", pos.getX());
//                nbt.setInteger(paramName + "Y", pos.getY());
//                nbt.setInteger(paramName + "Z", pos.getZ());
//            } else if (serializer == DataSerializers.OPTIONAL_UNIQUE_ID) {
//                // Odd mixing of java util's optionals and guava's optionals. Try to handle both.
//                try {
//                    com.google.common.base.Optional uuid = (com.google.common.base.Optional)paramValue;
//                    nbt.setUUID(paramName, (UUID)uuid.or(new UUID(0,0)));
//                } catch (ClassCastException e) {
//                    java.util.Optional uuid = (java.util.Optional)paramValue;
//                    nbt.setUUID(paramName, (UUID)uuid.orElse(new UUID(0,0)));
//                }
//            } else if (serializer == DataSerializers.COMPOUND_NBT) {
//                nbt.setTag(paramName, CNBT.fromMC((CompoundNBT)paramValue));
//            } else if (serializer == DataSerializers.BYTE) {
//                nbt.setInteger(paramName, (Byte)paramValue);
//            } else {
//                Cobalt.getLog().error(String.format("Unrecognized value type `%s` on attempt to save param `%s`", paramValue.getClass().getName(), paramName));
//            }
//        }
//    }
//
//    public <T> void loadAll(CNBT nbt) {
//        for (Map.Entry<String, EntityDataManager.DataEntry<?>> entry : registeredParams.entrySet()) {
//            DataParameter<?> param = entry.getValue().getKey();
//            IDataSerializer<?> serializer = param.getSerializer();
//            String paramName = entry.getKey();
//
//            if (serializer == DataSerializers.VARINT) {
//                mcManager.set((DataParameter<T>)param, (T)Integer.valueOf(nbt.getInteger(paramName)));
//            } else if (serializer == DataSerializers.FLOAT) {
//                mcManager.set((DataParameter<T>)param, (T)Float.valueOf(nbt.getFloat(paramName)));
//            } else if (serializer == DataSerializers.STRING) {
//                mcManager.set((DataParameter<T>)param, (T)nbt.getString(paramName));
//            } else if (serializer == DataSerializers.TEXT_COMPONENT) {
//                mcManager.set((DataParameter<T>)param, (T)new StringTextComponent(nbt.getString(paramName)));
//            } else if  (serializer == DataSerializers.ITEMSTACK) {
//                mcManager.set((DataParameter<T>)param, (T)nbt.getCompoundTag(paramName)); //FIXME can't create itemstack instances properly, this will fail.
//            } else if (serializer == DataSerializers.BOOLEAN) {
//                mcManager.set((DataParameter<T>)param, (T)Boolean.valueOf(nbt.getBoolean(paramName)));
//            } else if (serializer == DataSerializers.BLOCK_POS) {
//                BlockPos pos = new BlockPos(nbt.getInteger(paramName + "X"), nbt.getInteger(paramName + "Y"), nbt.getInteger(paramName + "Z"));
//                mcManager.set((DataParameter<T>)param, (T)pos);
//            } else if (serializer == DataSerializers.OPTIONAL_UNIQUE_ID) {
//                mcManager.set((DataParameter<T>)param, (T)com.google.common.base.Optional.of(nbt.getUUID(paramName)));
//            } else if (serializer == DataSerializers.COMPOUND_NBT) {
//                mcManager.set((DataParameter<T>)param, (T)nbt.getCompoundTag(paramName));
//            } else if (serializer == DataSerializers.BYTE) {
//                mcManager.set((DataParameter<T>)param, (T)Integer.valueOf(nbt.getInteger(paramName)));
//            } else {
//                Cobalt.getLog().error(String.format("Unrecognized serializer on attempt to read data param `%s`: %s", paramName, serializer.getClass().getName()));
//            }
//        }
//    }
//}
