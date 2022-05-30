package eu.byncing.sokky.utils;

import eu.byncing.sokky.channel.ISokkyChannel;
import eu.byncing.sokky.result.SokkyResult;

public class ObjectQueue {

    private final SokkyResult<ISokkyChannel> result;

    private final Object packet;

    private boolean success;

    public ObjectQueue(SokkyResult<ISokkyChannel> result, Object packet) {
        this.result = result;
        this.packet = packet;
    }

    public SokkyResult<ISokkyChannel> getResult() {
        return result;
    }

    public Object getPacket() {
        return packet;
    }

    public boolean isSuccess() {
        return success;
    }

    public void success() {
        this.success = true;
    }
}