package eu.byncing.sokky.channel;

import eu.byncing.sokky.codec.ISokkyCodec;
import eu.byncing.sokky.codec.steam.IByteArrayStream;
import eu.byncing.sokky.result.ISokkyResult;
import eu.byncing.sokky.result.SokkyResult;
import eu.byncing.sokky.utils.ByteBuf;
import eu.byncing.sokky.utils.ObjectQueue;
import eu.byncing.sokky.utils.SokkyUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SokkyChannel implements ISokkyChannel {

    private final SokkyResult<ISokkyChannel> result = new SokkyResult<>();

    private ISokkyChannel.Initializer initializer;

    private final SokkyPipeline pipeline;

    private final Socket socket;

    private boolean connected;

    private final BlockingQueue<ObjectQueue> queue = new LinkedBlockingQueue<>();

    public SokkyChannel(ISokkyChannel.Initializer initializer, Socket socket) {
        this.pipeline = new SokkyPipeline();
        this.socket = socket;
        this.connected = true;

        this.initializer = initializer;
        this.initializer.init(this);

        this.pipeline.sync(Handler.class, "connected", this);

        packetQueue();
    }

    public SokkyChannel() {
        this.pipeline = new SokkyPipeline();
        this.socket = new Socket();
    }

    private void packetQueue() {
        new Thread(() -> {
            while (connected) {
                try {
                    while (!queue.isEmpty()) {
                        ObjectQueue objectQueue = queue.poll();

                        objectQueue.getResult().invoke(() -> {
                            try {
                                if (objectQueue.isSuccess()) return;
                                if (!connected) return;
                                OutputStream output = socket.getOutputStream();

                                ByteBuf buffer = new ByteBuf();
                                boolean invoke = pipeline.sync(ISokkyCodec.class, "encode", SokkyChannel.this, objectQueue.getPacket(), buffer);
                                if (!invoke) {
                                    objectQueue.getResult().failure(new RuntimeException("Object could not be encoded because your encoder does not exist."));
                                    return;
                                }

                                byte[] bytes = buffer.toArray();
                                if (bytes.length < 1) {
                                    objectQueue.getResult().failure(new RuntimeException("Package has no data to send."));
                                    return;
                                }

                                output.write(bytes);
                                output.flush();

                                objectQueue.success();

                                objectQueue.getResult().complete(SokkyChannel.this);
                            } catch (Exception exception) {
                                objectQueue.getResult().failure(exception);
                            }
                        });

                        objectQueue.getResult().sync();
                    }

                    SokkyUtil.blocking(1);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }

    public ISokkyResult<ISokkyChannel> connect(SocketAddress address) {
        result.invoke(() -> {
            try {
                socket.connect(address);
                connected = true;

                result.complete(SokkyChannel.this);
                initializer.init(this);

                pipeline.sync(Handler.class, "connected", this);

                packetQueue();
                run();
            } catch (IOException e) {
                result.failure(e);
            }
        });
        return result;
    }

    public void init(ISokkyChannel.Initializer initializer) {
        this.initializer = initializer;
    }

    public void run() {
        while (connected) {
            try {
                byte[] bytes = pipeline.get(IByteArrayStream.class).readBytes(socket);
                if (bytes == null) break;
                ISokkyCodec<?> decoder = pipeline.get(ISokkyCodec.class);
                if (decoder != null) {
                    Object decode = decoder.decode(this, new ByteBuf(bytes));
                    if (decode != null) pipeline.sync(Handler.class, "received", this, decode);
                }
            } catch (Exception exception) {
                pipeline.sync(Handler.class, "exception", this, exception);
                break;
            }
        }
        close().sync();
        pipeline.sync(Handler.class, "disconnected", this);
    }

    @Override
    public ISokkyResult<ISokkyChannel> writeObject(Object object) {
        SokkyResult<ISokkyChannel> result = new SokkyResult<>();
        queue.add(new ObjectQueue(result, object));
        return result;
    }

    @Override
    public ISokkyResult<ISokkyChannel> close() {
        result.invoke(() -> {
            try {
                if (!connected) return;
                connected = false;
                socket.close();
                result.complete(SokkyChannel.this);
            } catch (IOException e) {
                result.failure(e);
                pipeline.sync(Handler.class, "exception", this, e);
            }
        });
        return result;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    public SokkyPipeline getPipeline() {
        return pipeline;
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return socket.getRemoteSocketAddress();
    }

    @Override
    public InetAddress getInetAddress() {
        return socket.getInetAddress();
    }
}