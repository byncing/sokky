package eu.byncing.sokky.codec;

import eu.byncing.sokky.channel.ISokkyChannel;
import eu.byncing.sokky.utils.ByteBuf;

public interface ISokkyCodec<O> {

    void encode(ISokkyChannel channel, O object, ByteBuf buffer);

    Object decode(ISokkyChannel channel, ByteBuf buffer);
}