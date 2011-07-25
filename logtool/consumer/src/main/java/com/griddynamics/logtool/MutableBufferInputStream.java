package com.griddynamics.logtool;

import org.jboss.netty.buffer.ChannelBuffer;

import java.io.IOException;
import java.io.InputStream;

public class MutableBufferInputStream extends InputStream {

    private ChannelBuffer buffer;
    private int mark = -1;


    MutableBufferInputStream(ChannelBuffer buffer) {
        this.buffer = buffer;
    }

    void updateBuffer(ChannelBuffer buffer) {
        if(this.buffer != buffer) {
            this.buffer = buffer;
        }
    }

    public int getReaderIndex() {
        return buffer.readerIndex();
    }

    public void setReaderIndex(int readerIndex) throws IndexOutOfBoundsException{
        buffer.readerIndex(readerIndex);
    }

    public int getWriterIndex() {
        return buffer.writerIndex();
    }

    public void setWriterIndex(int writerIndex) throws IndexOutOfBoundsException{
        buffer.writerIndex(writerIndex);
    }

    @Override
    public int read() throws IOException {
        if (!buffer.readable()) {
            return -1;
        }
        return buffer.readByte() & 0xff;
    }

    @Override
    public void mark(int readlimit) {
        mark = buffer.readerIndex();
    }

    public void setMark(int index) {
        mark = index;
    }

    @Override
    public void reset() throws IOException, IndexOutOfBoundsException {
        if(mark == -1) {
            throw new IOException("Buffer hasn't been market yet");
        } else {
            buffer.readerIndex(mark);
        }
    }
}
