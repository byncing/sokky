package eu.byncing.sokky.channel;

import eu.byncing.sokky.result.ISokkyResult;

import java.net.InetAddress;
import java.net.SocketAddress;

public interface ISokkyChannel {

    ISokkyResult<ISokkyChannel> writeObject(Object object);

    ISokkyResult<ISokkyChannel> close();

    boolean isConnected();

    Pipeline getPipeline();

    SocketAddress getRemoteAddress();

    InetAddress getInetAddress();

    interface Pipeline {

        void add(Object... objects);

        void remove(Class<?>... classes);

        boolean sync(Class<?> clazz, String name, Object... objects);

        void async(Class<?> clazz, String name, Object... objects);

        <T> T get(Class<T> clazz);
    }

    interface Handler<O> {

        default void connected(ISokkyChannel channel) {
        }

        default void disconnected(ISokkyChannel channel) {
        }

        default void received(ISokkyChannel channel, O object) {
        }

        default void exception(ISokkyChannel channel, Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    interface Initializer {

        void init(ISokkyChannel channel);
    }
}