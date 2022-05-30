package eu.byncing.sokky;

import eu.byncing.sokky.channel.ISokkyChannel;
import eu.byncing.sokky.channel.SokkyChannel;
import eu.byncing.sokky.result.ISokkyResult;

import java.net.InetAddress;
import java.net.SocketAddress;

public class SokkyClient {

    private final SokkyChannel channel;

    public SokkyClient() {
        this.channel = new SokkyChannel();
    }

    public ISokkyResult<ISokkyChannel> connect(SocketAddress address) {
        return channel.connect(address);
    }

    public SokkyClient init(ISokkyChannel.Initializer initializer) {
        channel.init(initializer);
        return this;
    }

    public ISokkyResult<ISokkyChannel> close() {
        return channel.close();
    }

    public ISokkyChannel getChannel() {
        return channel;
    }

    public boolean isConnected() {
        return channel.isConnected();
    }

    public SocketAddress getRemoteAddress() {
        return channel.getRemoteAddress();
    }

    public InetAddress getInetAddress() {
        return channel.getInetAddress();
    }
}