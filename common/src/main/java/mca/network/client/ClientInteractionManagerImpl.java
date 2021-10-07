package mca.network.client;

import java.util.Optional;

import mca.client.gui.*;
import mca.client.gui.BlueprintScreen;
import mca.entity.VillagerLike;
import mca.entity.ZombieVillagerEntityMCA;
import mca.item.BabyItem;
import mca.item.ExtendedWrittenBookItem;
import mca.server.world.data.BabyTracker;
import mca.server.world.data.Village;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;

public class ClientInteractionManagerImpl implements ClientInteractionManager {

    private final MinecraftClient client = MinecraftClient.getInstance();

    @Override
    public void handleGuiRequest(OpenGuiRequest message, PlayerEntity e) {
        switch (message.gui) {
            case WHISTLE:
                client.openScreen(new WhistleScreen());
                break;
            case BOOK:
                if (client.player != null) {
                    ItemStack item = client.player.getStackInHand(Hand.MAIN_HAND);
                    if (item.getItem() instanceof ExtendedWrittenBookItem) {
                        ExtendedWrittenBookItem bookItem = (ExtendedWrittenBookItem)item.getItem();
                        ExtendedBookScreen book = new ExtendedBookScreen(bookItem.getBook());
                        client.openScreen(book);
                    }
                }
                break;
            case BLUEPRINT:
                client.openScreen(new BlueprintScreen());
                break;
            case INTERACT:
                VillagerLike<?> villager = (VillagerLike<?>)client.world.getEntityById(message.villager);
                if (villager instanceof ZombieVillagerEntityMCA) {
                    String t = new String(new char[((ZombieVillagerEntityMCA)villager).getRandom().nextInt(8) + 2]).replace("\0", ". ");
                    villager.sendChatMessage(new LiteralText(t), client.player);
                } else {
                    client.openScreen(new InteractScreen(villager));
                }
                break;
            case VILLAGER_EDITOR:
                Entity entity = client.world.getEntityById(message.villager);
                client.openScreen(new VillagerEditorScreen(entity.getUuid()));
                break;
            case BABY_NAME:
                if (client.player != null) {
                    ItemStack item = client.player.getStackInHand(Hand.MAIN_HAND);
                    if (item.getItem() instanceof BabyItem) {
                        client.openScreen(new NameBabyScreen(client.player, item));
                    }
                }
                break;
        }
    }

    @Override
    public void handleFamilyTreeResponse(GetFamilyTreeResponse message) {
        Screen screen = client.currentScreen;
        if (screen instanceof FamilyTreeScreen) {
            FamilyTreeScreen gui = (FamilyTreeScreen)screen;
            gui.setFamilyData(message.uuid, message.family);
        }
    }

    @Override
    public void handleInteractDataResponse(GetInteractDataResponse message) {
        Screen screen = client.currentScreen;
        if (screen instanceof InteractScreen) {
            InteractScreen gui = (InteractScreen)screen;
            gui.setConstraints(message.constraints);
            gui.setParents(message.father, message.mother);
        }
    }

    @Override
    public void handleVillageDataResponse(GetVillageResponse message) {
        Screen screen = client.currentScreen;
        if (screen instanceof BlueprintScreen) {
            BlueprintScreen gui = (BlueprintScreen)screen;
            Village village = new Village();
            village.load(message.getData());

            gui.setVillage(village);
            gui.setRank(message.rank, message.reputation, message.ids);
        }
    }

    @Override
    public void handleVillageDataFailedResponse(GetVillageFailedResponse message) {
        Screen screen = client.currentScreen;
        if (screen instanceof BlueprintScreen) {
            BlueprintScreen gui = (BlueprintScreen)screen;
            gui.setVillage(null);
        }
    }

    @Override
    public void handleVillagerDataResponse(GetVillagerResponse message) {
        Screen screen = client.currentScreen;
        if (screen instanceof WhistleScreen) {
            WhistleScreen gui = (WhistleScreen)screen;
            gui.setVillagerData(message.getData());
        } else  if (screen instanceof VillagerEditorScreen) {
            VillagerEditorScreen gui = (VillagerEditorScreen)screen;
            gui.setVillagerData(message.getData());
        }
    }

    @Override
    public void handleDialogueResponse(InteractionDialogueResponse message) {
        Screen screen = client.currentScreen;
        if (screen instanceof InteractScreen) {
            InteractScreen gui = (InteractScreen)screen;
            gui.setDialogue(message.question, message.answers, message.silent);
        }
    }

    @Override
    public void handleChildData(GetChildDataResponse message) {
        BabyItem.CLIENT_STATE_CACHE.put(message.id, Optional.ofNullable(message.getData()).map(BabyTracker.ChildSaveState::new));
    }
}
