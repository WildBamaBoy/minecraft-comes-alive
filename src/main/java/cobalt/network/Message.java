package cobalt.network;

import io.netty.buffer.ByteBuf;
import lombok.SneakyThrows;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;

import java.io.*;
import java.util.Base64;

public abstract class Message implements Serializable {
    @SneakyThrows
    public void encode(PacketBuffer b) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(this);
        oos.close();
        b.writeBytes(baos.toByteArray());
    }

    @SneakyThrows
    public static Message decode(PacketBuffer b) {
        byte[] data = new byte[b.readableBytes()];
        b.readBytes(data);

        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(data));
        Object o = ois.readObject();
        ois.close();
        return (Message) o;
    }

    public abstract void receive(ServerPlayerEntity e);
}