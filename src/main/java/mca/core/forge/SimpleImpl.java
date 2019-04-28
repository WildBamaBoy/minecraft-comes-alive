package mca.core.forge;

import io.netty.buffer.ByteBuf;
import mca.client.network.ClientMessageQueue;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.inventory.InventoryMCA;
import mca.items.ItemBaby;
import mca.server.ServerMessageHandler;
import net.minecraft.client.Minecraft;
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

import javax.annotation.Nullable;
import java.util.UUID;

public class SimpleImpl {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("mca");

    public static void registerMessages() {
        INSTANCE.registerMessage(ButtonClickNotifyMessageHandler.class, ButtonClickNotifyMessage.class, 0, Side.SERVER);
        INSTANCE.registerMessage(SayMessageHandler.class, SayMessage.class, 1, Side.CLIENT);
        INSTANCE.registerMessage(BabyNameMessageHandler.class, BabyNameMessage.class, 2, Side.SERVER);
        INSTANCE.registerMessage(CareerIdMessageHandler.class, CareerIdMessage.class, 3, Side.CLIENT);
        INSTANCE.registerMessage(CareerIdRequestMessageHandler.class, CareerIdRequestMessage.class, 4, Side.SERVER);
        INSTANCE.registerMessage(OpenGuiMessageHandler.class, OpenGuiMessage.class, 5, Side.CLIENT);
        INSTANCE.registerMessage(InventoryRequestMessageHandler.class, InventoryRequestMessage.class, 6, Side.SERVER);
        INSTANCE.registerMessage(InventoryMessageHandler.class, InventoryMessage.class, 7, Side.CLIENT);
    }

    @SideOnly(Side.CLIENT)
    private static EntityPlayer getPlayerClient() {
        return Minecraft.getMinecraft().player;
    }

    public static class ButtonClickNotifyMessage implements IMessage {
        private String buttonId;
        private UUID targetUUID;
        public ButtonClickNotifyMessage() {
        }

        public ButtonClickNotifyMessage(String buttonId, @Nullable UUID targetUUID) {
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

    public static class ButtonClickNotifyMessageHandler implements IMessageHandler<ButtonClickNotifyMessage, IMessage> {
        @Override
        public IMessage onMessage(ButtonClickNotifyMessage message, MessageContext ctx) {
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

    public static class SayMessage implements IMessage {
        private String phraseId;
        private int speakingEntityId;
        public SayMessage() {
        }

        public SayMessage(String phraseId, int speakingEntityId) {
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

    public static class SayMessageHandler implements IMessageHandler<SayMessage, IMessage> {
        @Override
        public IMessage onMessage(SayMessage message, MessageContext ctx) {
            EntityPlayer player = getPlayerClient();
            EntityVillagerMCA villager = (EntityVillagerMCA) player.getEntityWorld().getEntityByID(message.speakingEntityId);

            if (villager != null) {
                villager.say(player, message.phraseId);
            }

            return null;
        }
    }

    public static class BabyNameMessage implements IMessage {
        private String babyName;

        public BabyNameMessage() {
        }

        public BabyNameMessage(String babyName) {
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

    public static class BabyNameMessageHandler implements IMessageHandler<BabyNameMessage, IMessage> {
        @Override
        public IMessage onMessage(BabyNameMessage message, MessageContext ctx) {
            EntityPlayerMP player = (EntityPlayerMP) ctx.getServerHandler().player;
            ItemStack stack = player.inventory.getStackInSlot(player.inventory.currentItem);

            if (stack.getItem() instanceof ItemBaby) {
                stack.getTagCompound().setString("name", message.babyName);
            }

            return null;
        }
    }

    public static class CareerIdMessage implements IMessage {
        private int careerId;
        private UUID entityUUID;
        public CareerIdMessage() {
        }

        public CareerIdMessage(int careerId, UUID entityUUID) {
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

    public static class CareerIdMessageHandler implements IMessageHandler<CareerIdMessage, IMessage> {
        @Override
        public IMessage onMessage(CareerIdMessage message, MessageContext ctx) {
            //must be thrown in the queue and processed on the main thread since we must loop through the loaded entity list
            //it could change while looping and cause a ConcurrentModificationException.
            ClientMessageQueue.add(message);
            return null;
        }
    }

    public static class OpenGuiMessage implements IMessage {
        private int guiId;

        public OpenGuiMessage() {
        }

        public OpenGuiMessage(int guiId) {
            this.guiId = guiId;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(this.guiId);
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.guiId = buf.readInt();
        }
    }

    public static class OpenGuiMessageHandler implements IMessageHandler<OpenGuiMessage, IMessage> {
        @Override
        public IMessage onMessage(OpenGuiMessage message, MessageContext ctx) {
            EntityPlayer player = getPlayerClient();
            player.openGui(MCA.getInstance(), message.guiId, player.world, 0, 0, 0);
            return null;
        }
    }

    public static class CareerIdRequestMessage implements IMessage {
        private UUID entityUUID;

        public CareerIdRequestMessage() {
        }

        public CareerIdRequestMessage(UUID entityUUID) {
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

    public static class CareerIdRequestMessageHandler implements IMessageHandler<CareerIdRequestMessage, IMessage> {
        @Override
        public IMessage onMessage(CareerIdRequestMessage message, MessageContext ctx) {
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

            return new CareerIdMessage(careerId, message.entityUUID);
        }
    }

    public static class InventoryRequestMessage implements IMessage {
        private UUID entityUUID;

        public InventoryRequestMessage() {
        }

        public InventoryRequestMessage(UUID entityUUID) {
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

    public static class InventoryRequestMessageHandler implements IMessageHandler<InventoryRequestMessage, IMessage> {
        @Override
        public IMessage onMessage(InventoryRequestMessage message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            EntityVillagerMCA villager = (EntityVillagerMCA) player.getServerWorld().getEntityFromUuid(message.entityUUID);
            return new InventoryMessage(villager.getUniqueID(), villager.inventory);
        }
    }

    public static class InventoryMessage implements IMessage {
        private UUID entityUUID;
        private NBTTagCompound inventoryNBT;
        public InventoryMessage() {
        }

        public InventoryMessage(UUID entityUUID, InventoryMCA inventory) {
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

    public static class InventoryMessageHandler implements IMessageHandler<InventoryMessage, IMessage> {
        @Override
        public IMessage onMessage(InventoryMessage message, MessageContext ctx) {
            ClientMessageQueue.add(message);
            return null;
        }
    }
}