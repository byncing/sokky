package eu.byncing.sokky.codec.steam;

import eu.byncing.sokky.utils.SokkyUtil;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class LengthSteam implements IByteArrayStream {

    @Override
    public byte[] readBytes(Socket socket) throws IOException {
        DataInputStream input = new DataInputStream(socket.getInputStream());

        int length = SokkyUtil.readVarInt(input);
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) bytes[i] = (byte) input.read();

        return bytes;
    }
}