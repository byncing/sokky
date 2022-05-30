package eu.byncing.sokky.codec;

import eu.byncing.sokky.channel.ISokkyChannel;
import eu.byncing.sokky.utils.ByteBuf;

public class MessageCodec implements ISokkyCodec<String> {

    @Override
    public void encode(ISokkyChannel channel, String object, ByteBuf buffer) {
        buffer.writeString(object);
    }

    @Override
    public Object decode(ISokkyChannel channel, ByteBuf buffer) {
        return buffer.readString();
    }
}