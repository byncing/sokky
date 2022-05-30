package eu.byncing.sokky.codec;

import eu.byncing.sokky.channel.ISokkyChannel;
import eu.byncing.sokky.utils.ByteBuf;

public class BufferCodec implements ISokkyCodec<ByteBuf> {

    @Override
    public void encode(ISokkyChannel channel, ByteBuf object, ByteBuf buffer) {
        int length = object.toArray().length;

        buffer.writeInt(length);
        for (byte b : object.toArray()) buffer.writeByte(b);
    }

    @Override
    public Object decode(ISokkyChannel channel, ByteBuf buffer) {
        return buffer;
    }
}