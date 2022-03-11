package mca.network.client;

import mca.ClientProxy;
import mca.cobalt.network.Message;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class ShowToastRequest implements Message {
    private static final long serialVersionUID = 1055734972572313374L;

    private final String title;
    private final String message;

    public ShowToastRequest(String title, String message) {
        this.title = title;
        this.message = message;
    }

    public Text getTitle() {
        return new TranslatableText(title);
    }

    public Text getMessage() {
        return new TranslatableText(message);
    }

    @Override
    public void receive(PlayerEntity e) {
        ClientProxy.getNetworkHandler().handleToastMessage(this);
    }
}
