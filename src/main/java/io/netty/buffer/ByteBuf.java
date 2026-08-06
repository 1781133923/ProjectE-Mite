package io.netty.buffer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Minimal ByteBuf stand-in for MITE (1.6.4 has no Netty).
 */
public class ByteBuf {
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final DataOutputStream writer = new DataOutputStream(this.output);
    private DataInputStream reader;
    private moddedmite.rustedironcore.network.PacketByteBuf delegate;

    /**
     * Switches this buffer into read-only delegation mode (used on the receive path
     * where RustedIronCore hands us its own PacketByteBuf).
     */
    public void setDelegate(moddedmite.rustedironcore.network.PacketByteBuf delegate) {
        this.delegate = delegate;
    }

    public ByteBuf writeInt(int value) {
        if (this.delegate != null) {
            throw new UnsupportedOperationException("Cannot write to a delegated buffer");
        }
        try {
            this.writer.writeInt(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    public int readInt() {
        if (this.delegate != null) {
            return this.delegate.readInt();
        }
        try {
            return this.reader().readInt();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public ByteBuf writeBoolean(boolean value) {
        if (this.delegate != null) {
            throw new UnsupportedOperationException("Cannot write to a delegated buffer");
        }
        try {
            this.writer.writeBoolean(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    public boolean readBoolean() {
        if (this.delegate != null) {
            return this.delegate.readBoolean();
        }
        try {
            return this.reader().readBoolean();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public ByteBuf writeDouble(double value) {
        if (this.delegate != null) {
            throw new UnsupportedOperationException("Cannot write to a delegated buffer");
        }
        try {
            this.writer.writeDouble(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    public double readDouble() {
        if (this.delegate != null) {
            return this.delegate.readDouble();
        }
        try {
            return this.reader().readDouble();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public ByteBuf writeFloat(float value) {
        if (this.delegate != null) {
            throw new UnsupportedOperationException("Cannot write to a delegated buffer");
        }
        try {
            this.writer.writeFloat(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    public float readFloat() {
        if (this.delegate != null) {
            return this.delegate.readFloat();
        }
        try {
            return this.reader().readFloat();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public ByteBuf writeShort(int value) {
        if (this.delegate != null) {
            throw new UnsupportedOperationException("Cannot write to a delegated buffer");
        }
        try {
            this.writer.writeShort(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    public short readShort() {
        if (this.delegate != null) {
            return this.delegate.readShort();
        }
        try {
            return this.reader().readShort();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public int readUnsignedShort() {
        if (this.delegate != null) {
            return this.delegate.readShort() & 0xFFFF;
        }
        try {
            return this.reader().readUnsignedShort();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public ByteBuf writeByte(int value) {
        if (this.delegate != null) {
            throw new UnsupportedOperationException("Cannot write to a delegated buffer");
        }
        try {
            this.writer.writeByte(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    public byte readByte() {
        if (this.delegate != null) {
            return this.delegate.readByte();
        }
        try {
            return this.reader().readByte();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public int readUnsignedByte() {
        if (this.delegate != null) {
            return this.delegate.readByte() & 0xFF;
        }
        try {
            return this.reader().readUnsignedByte();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public ByteBuf writeLong(long value) {
        try {
            this.writer.writeLong(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    public long readLong() {
        if (this.delegate != null) {
            // RIC's PacketByteBuf has no readLong; compose from two ints.
            return ((long) this.delegate.readInt() << 32) | (this.delegate.readInt() & 0xFFFFFFFFL);
        }
        try {
            return this.reader().readLong();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public ByteBuf writeBytes(byte[] src) {
        try {
            this.writer.write(src);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    public ByteBuf readBytes(byte[] dst) {
        if (this.delegate != null) {
            for (int i = 0; i < dst.length; i++) {
                dst[i] = this.delegate.readByte();
            }
            return this;
        }
        try {
            this.reader().readFully(dst);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    public ByteBuf writeString(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        this.writeShort(bytes.length);
        return this.writeBytes(bytes);
    }

    public String readString() {
        short length = this.readShort();
        byte[] bytes = new byte[length];
        this.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public byte[] array() {
        return this.output.toByteArray();
    }

    public int readableBytes() {
        try {
            return this.array().length - (this.reader == null ? 0 : this.reader.available());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private DataInputStream reader() {
        if (this.reader == null) {
            this.reader = new DataInputStream(new ByteArrayInputStream(this.array()));
        }
        return this.reader;
    }
}
