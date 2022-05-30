package eu.byncing.sokky.utils;

import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ByteBuf {

    private byte[] bytes;

    private int write, read;

    public ByteBuf(byte[] bytes) {
        this.bytes = bytes;
        this.write = bytes.length;
    }

    public ByteBuf() {
        this(new byte[0]);
    }

    public void writeByte(byte value) {
        byte[] result = new byte[bytes.length + 1];

        result[write++] = value;

        for (int i = 0; i < result.length; i++) {
            if (i != write && i < bytes.length) result[i] = bytes[i];
        }

        this.bytes = result;
    }

    public byte readByte() {
        if (read > bytes.length - 1) return 0;
        return bytes[read++];
    }

    public void writeArray(byte[] bytes) {
        writeInt(bytes.length);
        for (byte aByte : bytes) writeByte(aByte);
    }

    public byte[] readArray() {
        int length = readInt();
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) bytes[i] = readByte();
        return bytes;
    }

    public void writeBytes(byte[] bytes) {
        for (byte aByte : bytes) writeByte(aByte);
    }

    public byte[] readBytes(int length) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) bytes[i] = readByte();
        return bytes;
    }

    public void writeInt(int value) {
        do {
            int part = value & 0x7F;
            value >>>= 7;
            if (value != 0) part |= 0x80;
            writeByte((byte) part);
        } while (value != 0);
    }

    public int readInt() {
        int value = 0, bytes = 0;
        byte b;
        do {
            b = readByte();
            value |= (b & 0x7F) << (bytes++ * 7);
            if (bytes > 5) return value;
        } while ((b & 0x80) == 0x80);
        return value;
    }

    public void writeShort(short value) {
        writeByte((byte) ((value & 0xFF00) >> 8));
        writeByte((byte) (value & 0x00FF));
    }

    @Deprecated
    public short readShort() {
        return java.nio.ByteBuffer.wrap(new byte[]{readByte(), readByte()}).order(ByteOrder.BIG_ENDIAN).getShort();
    }

    public void writeLong(long value) {
        while (true) {
            if ((value & ~((long) 0x7F)) == 0) {
                writeByte((byte) value);
                return;
            }
            writeByte((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
    }

    public long readLong() {
        long value = 0;
        int position = 0;
        while (true) {
            byte aByte = readByte();
            value |= (long) (aByte & 0x7F) << position;
            if ((aByte & 0x80) == 0) break;
            position += 7;
            if (position >= 64) throw new RuntimeException("Long is too big");
        }
        return value;
    }

    public void writeNioLong(long value) {
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(0, value);
        for (byte b : buffer.array()) writeByte(b);
    }

    public long readNioLong() {
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(Long.BYTES);
        for (int i = 0; i < Long.BYTES; i++) buffer.put(toArray()[i]);
        buffer.flip();
        return buffer.getLong();
    }

    public void writeString(String value) {
        if (value.length() < Short.MAX_VALUE) {
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            writeInt(bytes.length);
            for (byte aByte : bytes) writeInt(aByte);
        }
    }

    public String readString() {
        int length = readInt();
        if (length < 1) return "";
        byte[] bytes = new byte[length];
        for (int i = 0; i < bytes.length; i++) bytes[i] = (byte) readInt();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public void writeChar(char value) {
        writeInt(value);
    }

    public char readChar() {
        return (char) readInt();
    }

    public void writeBoolean(boolean value) {
        writeInt(value ? 1 : 0);
    }

    public boolean readBoolean() {
        int i = readInt();
        return i == 1;
    }

    public void writeUUID(UUID value) {
        writeString(value.toString());
    }

    public UUID readUUID() {
        String string = readString();
        if (string.isEmpty()) return null;
        return UUID.fromString(string);
    }

    public ByteBuf flip() {
        byte[] bytes = new byte[this.bytes.length];
        int i = 0;
        for (int i1 = this.bytes.length; i1 > 0; i1--) {
            bytes[i] = this.bytes[i1 - 1];
            i++;
        }
        return new ByteBuf(bytes);
    }

    public byte[] toArray() {
        return bytes;
    }

    public int readable() {
        return this.bytes.length - this.read;
    }

    public boolean isReadable() {
        return read < bytes.length;
    }
}