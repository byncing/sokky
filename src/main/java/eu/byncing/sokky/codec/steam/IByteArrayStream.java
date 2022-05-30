package eu.byncing.sokky.codec.steam;

import java.io.IOException;
import java.net.Socket;

public interface IByteArrayStream {

    byte[] readBytes(Socket socket) throws IOException;
}