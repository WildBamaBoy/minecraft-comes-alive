package mca.core.forge;

import io.netty.buffer.ByteBuf;
import mca.client.gui.GuiStaffOfLife;
import mca.client.network.ClientMessageQueue;
import mca.core.Constants;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.PlayerHistory;
import mca.entity.data.SavedVillagers;
import mca.entity.inventory.InventoryMCA;
import mca.items.ItemBaby;
import mca.server.ServerMessageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.util.control.Exception;
import sun.plugin2.message.Message;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NetMCA {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("mca");

    public static void registerMessages() {
        INSTANCE.registerMessage(ButtonActionHandler.class, ButtonAction.class, 0, Side.SERVER);
        INSTANCE.registerMessage(SayHandler.class, Say.class, 1, Side.CLIENT);
        INSTANCE.registerMessage(BabyNameHandler.class, BabyName.class, 2, Side.SERVER);
        INSTANCE.registerMessage(CareerResponseHandler.class, CareerResponse.class, 3, Side.CLIENT);
        INSTANCE.registerMessage(CareerRequestHandler.class, CareerRequest.class, 4, Side.SERVER);
        INSTANCE.registerMessage(InventoryRequestHandler.class, InventoryRequest.class, 5, Side.SERVER);
        INSTANCE.registerMessage(InventoryResponseHandler.class, InventoryResponse.class, 6, Side.CLIENT);
        INSTANCE.registerMessage(SavedVillagersRequestHandler.class, SavedVillagersRequest.class, 7, Side.SERVER);
        INSTANCE.registerMessage(SavedVillagersResponseHandler.class, SavedVillagersResponse.class, 8, Side.CLIENT);
        INSTANCE.registerMessage(ReviveVillagerHandler.class, ReviveVillager.class, 9, Side.SERVER);
    }

    @SideOnly(Side.CLIENT)
    private static EntityPlayer getPlayerClient() {
        return Minecraft.getMinecraft().player;
    }

    public static class ButtonAction implements IMessage {
        private String buttonId;
        private UUID targetUUID;

        public ButtonAction() {
        }

        public ButtonAction(String buttonId, @Nullable UUID targetUUID) {
            this.buttonId = buttonId;
            this.targetUUID = targetUUID;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeBoolean(targetUUID != null);
            ByteBufUtils.writeUTF8String(buf, this.buttonId);

            if (targetUUID != null) {
                ByteBufUtils.writeUTF8String(buf, this.targetUUID.toString());
            }
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            boolean hasTarget = buf.readBoolean();
            this.buttonId = ByteBufUtils.readUTF8String(buf);

            if (hasTarget) {
                this.targetUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
            }
        }

        public String getButtonId() {
            return this.buttonId;
        }

        public UUID getTargetUUID() {
            return this.targetUUID;
        }

        public boolean targetsServer() {
            return getTargetUUID() == null;
        }
    }

    public static class ButtonActionHandler implements IMessageHandler<ButtonAction, IMessage> {
        @Override
        public IMessage onMessage(ButtonAction message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;

            //The message can target a particular villager, or the server itself.
            if (!message.targetsServer()) {
                EntityVillagerMCA villager = (EntityVillagerMCA) player.getServerWorld().getEntityFromUuid(message.targetUUID);
                String buttonId = message.buttonId;

                if (villager != null) {
                    player.getServerWorld().addScheduledTask(() -> villager.handleButtonClick(player, buttonId));
                }
            } else {
                ServerMessageHandler.handleMessage(player, message);
            }
            return null;
        }
    }

    public static class Say implements IMessage {
        private String phraseId;
        private int speakingEntityId;

        public Say() {
        }

        public Say(String phraseId, int speakingEntityId) {
            this.phraseId = phraseId;
            this.speakingEntityId = speakingEntityId;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, this.phraseId);
            buf.writeInt(this.speakingEntityId);
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.phraseId = ByteBufUtils.readUTF8String(buf);
            this.speakingEntityId = buf.readInt();
        }
    }

    public static class SayHandler implements IMessageHandler<Say, IMessage> {
        @Override
        public IMessage onMessage(Say message, MessageContext ctx) {
            EntityPlayer player = getPlayerClient();
            EntityVillagerMCA villager = (EntityVillagerMCA) player.getEntityWorld().getEntityByID(message.speakingEntityId);

            if (villager != null) {
                villager.say(player, message.phraseId);
            }

            return null;
        }
    }

    public static class BabyName implements IMessage {
        private String babyName;

        public BabyName() {
        }

        public BabyName(String babyName) {
            this.babyName = babyName;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, this.babyName);
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.babyName = ByteBufUtils.readUTF8String(buf);
        }
    }

    public static class BabyNameHandler implements IMessageHandler<BabyName, IMessage> {
        @Override
        public IMessage onMessage(BabyName message, MessageContext ctx) {
            EntityPlayerMP player = (EntityPlayerMP) ctx.getServerHandler().player;
            ItemStack stack = player.inventory.getStackInSlot(player.inventory.currentItem);

            if (stack.getItem() instanceof ItemBaby) {
                stack.getTagCompound().setString("name", message.babyName);
            }

            return null;
        }
    }

    public static class CareerResponse implements IMessage {
        private int careerId;
        private UUID entityUUID;

        public CareerResponse() {
        }

        public CareerResponse(int careerId, UUID entityUUID) {
            this.careerId = careerId;
            this.entityUUID = entityUUID;
        }

        public int getCareerId() {
            return careerId;
        }

        public UUID getEntityUUID() {
            return entityUUID;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeVarInt(buf, careerId, 4);
            ByteBufUtils.writeUTF8String(buf, entityUUID.toString());
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.careerId = ByteBufUtils.readVarInt(buf, 4);
            this.entityUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        }
    }

    public static class CareerResponseHandler implements IMessageHandler<CareerResponse, IMessage> {
        @Override
        public IMessage onMessage(CareerResponse message, MessageContext ctx) {
            //must be thrown in the queue and processed on the main thread since we must loop through the loaded entity list
            //it could change while looping and cause a ConcurrentModificationException.
            ClientMessageQueue.add(message);
            return null;
        }
    }

    public static class CareerRequest implements IMessage {
        private UUID entityUUID;

        public CareerRequest() {
        }

        public CareerRequest(UUID entityUUID) {
            this.entityUUID = entityUUID;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, entityUUID.toString());
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.entityUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        }
    }

    public static class CareerRequestHandler implements IMessageHandler<CareerRequest, IMessage> {
        @Override
        public IMessage onMessage(CareerRequest message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            int careerId = -255;

            try {
                EntityVillagerMCA villager = (EntityVillagerMCA) player.getServerWorld().getEntityFromUuid(message.entityUUID);

                if (villager != null) {
                    careerId = ObfuscationReflectionHelper.getPrivateValue(EntityVillager.class, villager, "careerId");
                }
            } catch (ClassCastException e) {
                MCA.getLog().error("UUID provided in career request does not match an MCA villager!: " + message.entityUUID.toString());
                return null;
            } catch (NullPointerException e) {
                MCA.getLog().error("UUID provided in career request does not match a loaded MCA villager!: " + message.entityUUID.toString());
                return null;
            }

            if (careerId == -255) {
                MCA.getLog().error("Career ID wasn't assigned for UUID: " + message.entityUUID);
                return null;
            }

            return new CareerResponse(careerId, message.entityUUID);
        }
    }

    public static class InventoryRequest implements IMessage {
        private UUID entityUUID;

        public InventoryRequest() {
        }

        public InventoryRequest(UUID entityUUID) {
            this.entityUUID = entityUUID;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, entityUUID.toString());
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.entityUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        }
    }

    public static class InventoryRequestHandler implements IMessageHandler<InventoryRequest, IMessage> {
        @Override
        public IMessage onMessage(InventoryRequest message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            EntityVillagerMCA villager = (EntityVillagerMCA) player.getServerWorld().getEntityFromUuid(message.entityUUID);
            if (villager != null) {
                return new InventoryResponse(villager.getUniqueID(), villager.inventory);
            }
            return null;
        }
    }

    public static class InventoryResponse implements IMessage {
        private UUID entityUUID;
        private NBTTagCompound inventoryNBT;

        public InventoryResponse() {
        }

        public InventoryResponse(UUID entityUUID, InventoryMCA inventory) {
            this.inventoryNBT = new NBTTagCompound();
            this.entityUUID = entityUUID;
            this.inventoryNBT.setTag("inventory", inventory.writeInventoryToNBT());
        }

        @Override
        public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, entityUUID.toString());
            ByteBufUtils.writeTag(buf, inventoryNBT);
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.entityUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
            this.inventoryNBT = ByteBufUtils.readTag(buf);
        }

        public UUID getEntityUUID() {
            return entityUUID;
        }

        public NBTTagCompound getInventoryNBT() {
            return inventoryNBT;
        }
    }

    public static class InventoryResponseHandler implements IMessageHandler<InventoryResponse, IMessage> {
        @Override
        public IMessage onMessage(InventoryResponse message, MessageContext ctx) {
            ClientMessageQueue.add(message);
            return null;
        }
    }

    public static class SavedVillagersRequest implements IMessage {
        public SavedVillagersRequest() {}

        @Override
        public void fromBytes(ByteBuf buf) {}

        @Override
        public void toBytes(ByteBuf buf) {}
    }

    public static class SavedVillagersRequestHandler implements IMessageHandler<SavedVillagersRequest, IMessage> {
        @Override
        public IMessage onMessage(SavedVillagersRequest message, MessageContext ctx) {
            return new SavedVillagersResponse(ctx.getServerHandler().player);
        }
    }

    public static class SavedVillagersResponse implements IMessage {
        private Map<String, NBTTagCompound> villagers = new HashMap<>();

        public SavedVillagersResponse() { }

        public SavedVillagersResponse(EntityPlayer player) {
            villagers = SavedVillagers.get(player.world).getMap();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(villagers.size());
            villagers.forEach((k,v) -> {
                ByteBufUtils.writeUTF8String(buf, k);
                ByteBufUtils.writeTag(buf, v);
            });
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            int size = buf.readInt();
            for (int i = 0; i < size; i++) {
                String k = ByteBufUtils.readUTF8String(buf);
                NBTTagCompound v = ByteBufUtils.readTag(buf);
                villagers.put(k, v);
            }
        }
    }

    public static class SavedVillagersResponseHandler implements IMessageHandler<SavedVillagersResponse, IMessage> {
        @Override
        public IMessage onMessage(SavedVillagersResponse message, MessageContext ctx) {
            GuiScreen screen = Minecraft.getMinecraft().currentScreen;
            if (screen instanceof GuiStaffOfLife) {
                ((GuiStaffOfLife) screen).setVillagerData(message.villagers);
            }
            return null;
        }
    }

    public static class ReviveVillager implements IMessage {
        private UUID target;
        public ReviveVillager() {}

        public ReviveVillager(UUID uuid) {
            this.target = uuid;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, target.toString());
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            target = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        }
    }

    public static class ReviveVillagerHandler implements IMessageHandler<ReviveVillager, IMessage> {
        @Override
        public IMessage onMessage(ReviveVillager message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().player;
            SavedVillagers villagers = SavedVillagers.get(player.world);
            NBTTagCompound nbt = SavedVillagers.get(player.world).loadByUUID(message.target);
            if (nbt != null) {
                EntityVillagerMCA villager = new EntityVillagerMCA(player.world);
                villager.setPosition(player.posX, player.posY, player.posZ);
                player.world.spawnEntity(villager);

                villager.readEntityFromNBT(nbt);
                villager.reset();

                villagers.remove(message.target);
                player.inventory.mainInventory.get(player.inventory.currentItem).damageItem(1, player);
            }

            return null;
        }
    }
}