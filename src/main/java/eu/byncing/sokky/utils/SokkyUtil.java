package eu.byncing.sokky.utils;

import java.io.DataInputStream;
import java.io.IOException;

public class SokkyUtil {

    public static void blocking(long duration) throws InterruptedException {
        Thread.sleep(duration);
    }

    public static int readVarInt(DataInputStream input) throws IOException {
        return readVarInt(input, 5);
    }

    public static int readVarInt(DataInputStream input, int maxBytes) throws IOException {
        int out = 0, bytes = 0;
        byte in;
        do {
            in = input.readByte();
            out |= (in & 0x7F) << (bytes++ * 7);
            if (bytes > maxBytes) throw new RuntimeException("VarInt too big");
        } while ((in & 0x80) == 0x80);
        return out;
    }

    public static int readVarInt(ByteBuf buf) {
        return readVarInt(buf, 5);
    }

    public static int readVarInt(ByteBuf buf, int maxBytes) {
        int out = 0, bytes = 0;
        byte in;
        do {
            in = buf.readByte();
            out |= (in & 0x7F) << (bytes++ * 7);
            if (bytes > maxBytes) throw new RuntimeException("VarInt too big");
        } while ((in & 0x80) == 0x80);
        return out;
    }
}