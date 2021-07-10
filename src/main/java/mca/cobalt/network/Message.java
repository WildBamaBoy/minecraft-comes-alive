package mca.cobalt.network;

import lombok.SneakyThrows;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import java.io.*;

public abstract class Message implements Serializable {
    @SneakyThrows
    public static Message decode(PacketByteBuf b) {
        byte[] data = new byte[b.readableBytes()];
        b.readBytes(data);

        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(data));
        Object o = ois.readObject();
        ois.close();
        return (Message) o;
    }

    @SneakyThrows
    public void encode(PacketByteBuf b) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(this);
        oos.close();
        b.writeBytes(baos.toByteArray());
    }

    public abstract void receive(ServerPlayerEntity e);
}