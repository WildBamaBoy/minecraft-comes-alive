package mca.network;

import mca.client.gui.GuiFamilyTree;
import mca.client.gui.GuiInteract;
import mca.cobalt.network.Message;
import mca.entity.data.FamilyTreeEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;
import java.util.UUID;

public class GetInteractDataResponse extends Message {
    private final Map<String, Boolean> constraints;
    String father;
    String mother;

    public GetInteractDataResponse(Map<String, Boolean> constraints, String father, String mother) {
        this.constraints = constraints;
        this.father = father;
        this.mother = mother;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void receive(ServerPlayerEntity player) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof GuiInteract) {
            GuiInteract gui = (GuiInteract) screen;
            gui.setConstraints(constraints);
            gui.setParents(father, mother);
        }
    }
}
