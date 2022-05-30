# sokky
a java network library specialized for sockets

### Get started
````gradle
repositories {
    maven {url('https://repo.byncing.eu/')}
}

dependencies {
    implementation('eu.byncing:sokky:2.0.0-SNAPSHOT')
}
````

### TestServer
````java
package eu.byncing.sokky;

import eu.byncing.sokky.channel.ISokkyChannel;
import eu.byncing.sokky.codec.BufferCodec;
import eu.byncing.sokky.codec.steam.LengthSteam;
import eu.byncing.sokky.utils.ByteBuf;

import java.net.InetSocketAddress;
import java.util.Arrays;

public class TestServer {

    public static void main(String[] args) {
        SokkyServer server = new SokkyServer();
        server.init(channel -> channel.getPipeline().add(new ISokkyChannel.Handler<ByteBuf>() {
            @Override
            public void connected(ISokkyChannel channel) {
                System.out.println(channel.getRemoteAddress() + " has connected");
            }

            @Override
            public void received(ISokkyChannel channel, ByteBuf buf) {
                System.out.println(channel.getRemoteAddress() + " received: " + Arrays.toString(buf.toArray()));
                int id = buf.readInt();
                if (id == 0xFe) {
                    System.out.println(channel.getRemoteAddress() + " - name: " + buf.readString());
                    System.out.println(channel.getRemoteAddress() + " - age: " + buf.readInt());
                }
            }

            @Override
            public void disconnected(ISokkyChannel channel) {
                System.out.println(channel.getRemoteAddress() + " has disconnected");
            }
        }, new BufferCodec(), new LengthSteam())).bind(new InetSocketAddress(3000)).sync();
    }
}
````

### TestClient
````java
package eu.byncing.sokky;

import eu.byncing.sokky.channel.ISokkyChannel;
import eu.byncing.sokky.codec.BufferCodec;
import eu.byncing.sokky.codec.steam.LengthSteam;
import eu.byncing.sokky.utils.ByteBuf;

import java.net.InetSocketAddress;

public class TestClient {

    public static void main(String[] args) {
        SokkyClient client = new SokkyClient();
        client.init(channel -> channel.getPipeline().add(new ISokkyChannel.Handler<ByteBuf>() {
            @Override
            public void connected(ISokkyChannel channel) {
                System.out.println(channel.getRemoteAddress() + " has connected");

                ByteBuf buf = new ByteBuf();
                buf.writeInt(0xFe);
                buf.writeString("byncing");
                buf.writeInt(19);

                channel.writeObject(buf);
            }

            @Override
            public void disconnected(ISokkyChannel channel) {
                System.out.println(channel.getRemoteAddress() + " has disconnected");
            }
        }, new BufferCodec(), new LengthSteam())).connect(new InetSocketAddress("127.0.0.1", 3000)).sync();
    }
}
````