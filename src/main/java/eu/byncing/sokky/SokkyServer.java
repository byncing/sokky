package eu.byncing.sokky;

import eu.byncing.sokky.channel.ISokkyChannel;
import eu.byncing.sokky.channel.SokkyChannel;
import eu.byncing.sokky.result.ISokkyResult;
import eu.byncing.sokky.result.SokkyResult;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SokkyServer {

    private boolean connected;

    private final SokkyResult<SokkyServer> result = new SokkyResult<>();

    private ISokkyChannel.Initializer initializer;

    private ServerSocket socket;

    private final Collection<ISokkyChannel> channels = new ConcurrentLinkedQueue<>();

    public SokkyServer() {
        try {
            this.socket = new ServerSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ISokkyResult<SokkyServer> bind(SocketAddress address) {
        result.invoke(() -> {
            try {
                if (socket == null) {
                    result.failure(new NullPointerException("Socket was not initialized."));
                    return;
                }
                socket.bind(address);
                connected = true;
                result.complete(this);
                while (connected) {
                    try {
                        Socket accept = socket.accept();
                        SokkyChannel channel = new SokkyChannel(initializer, accept);
                        channels.add(channel);

                        new Thread(() -> {
                            channel.run();
                            channels.remove(channel);
                        }).start();

                    } catch (IOException exception) {
                        connected = false;
                        result.failure(exception);
                        break;
                    }
                }
            } catch (IOException e) {
                result.failure(e);
            }
        });
        return result;
    }

    public SokkyServer init(ISokkyChannel.Initializer initializer) {
        this.initializer = initializer;
        return this;
    }

    public ISokkyResult<SokkyServer> close() {
        result.invoke(() -> {
            try {
                if (socket == null) {
                    result.failure(new NullPointerException("Socket was not initialized."));
                    return;
                }
                if (!connected) return;
                connected = false;
                socket.close();
                for (ISokkyChannel channel : channels) channel.close();
                result.complete(SokkyServer.this);
            } catch (IOException e) {
                result.failure(e);
            }
        });
        return result;
    }

    public boolean isConnected() {
        return connected;
    }

    public Collection<ISokkyChannel> getChannels() {
        return channels;
    }

    public int getPort() {
        return socket.getLocalPort();
    }

    public SocketAddress getLocalAddress() {
        return socket.getLocalSocketAddress();
    }

    public InetAddress getInetAddress() {
        return socket.getInetAddress();
    }
}