package mca.network.client;

import mca.client.gui.GuiBlueprint;
import mca.client.gui.GuiFamilyTree;
import mca.client.gui.GuiInteract;
import mca.client.gui.GuiNameBaby;
import mca.client.gui.GuiStaffOfLife;
import mca.client.gui.GuiWhistle;
import mca.entity.VillagerLike;
import mca.item.BabyItem;
import mca.server.world.data.Village;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class ClientInteractionManagerImpl implements ClientInteractionManager {

    private final MinecraftClient client = MinecraftClient.getInstance();

    @Override
    public void handleGuiRequest(OpenGuiRequest message, PlayerEntity e) {
        switch (message.gui) {
        case WHISTLE:
            client.openScreen(new GuiWhistle());
            break;
        case STAFF_OF_LIFE:
            client.openScreen(new GuiStaffOfLife());
            break;
        case BLUEPRINT:
            client.openScreen(new GuiBlueprint());
            break;
        case INTERACT:
            client.openScreen(new GuiInteract((VillagerLike<?>)client.world.getEntityById(message.villager)));
            break;
        case BABY_NAME:
            if (client.player != null) {
                ItemStack item = client.player.getStackInHand(Hand.MAIN_HAND);
                if (item.getItem() instanceof BabyItem) {
                    client.openScreen(new GuiNameBaby(client.player, item));
                }
            }
            break;
    }
    }

    @Override
    public void handleFamilyTreeResponse(GetFamilyTreeResponse message) {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen instanceof GuiFamilyTree) {
            GuiFamilyTree gui = (GuiFamilyTree) screen;
            gui.setFamilyData(message.uuid, message.family);
        }
    }

    @Override
    public void handleInteractDataResponse(GetInteractDataResponse message) {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen instanceof GuiInteract) {
            GuiInteract gui = (GuiInteract) screen;
            gui.setConstraints(message.constraints);
            gui.setParents(message.father, message.mother);
        }
    }

    @Override
    public void handleVillageDataResponse(GetVillageResponse message) {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen instanceof GuiBlueprint) {
            GuiBlueprint gui = (GuiBlueprint) screen;
            Village village = new Village();
            village.load(message.getData());

            gui.setVillage(village);
            gui.setReputation(message.reputation);
        }
    }

    @Override
    public void handleVillagerDataResponse(GetVillagerResponse message) {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen instanceof GuiWhistle) {
            GuiWhistle gui = (GuiWhistle) screen;
            gui.setVillagerData(message.getData());
        }
    }

    @Deprecated
    @Override
    public void handleSavedVillagersResponse(SavedVillagersResponse message) {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen instanceof GuiStaffOfLife) {
            GuiStaffOfLife gui = (GuiStaffOfLife) screen;
            gui.setVillagerData(message.getData());
        }
    }
}
