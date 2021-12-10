package mca.network.client;

import mca.cobalt.network.Message;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.TranslatableText;

public class ShowToastRequest implements Message {
    private static final long serialVersionUID = 1055734972572313374L;

    private final SystemToast.Type type;
    private final String title;
    private final String description;

    public ShowToastRequest(SystemToast.Type type, String title, String description) {
        this.type = type;
        this.title = title;
        this.description = description;
    }

    @Override
    public void receive(PlayerEntity e) {
        ToastManager manager = MinecraftClient.getInstance().getToastManager();
        SystemToast.add(manager, type, new TranslatableText(title), new TranslatableText(description));
    }
}
