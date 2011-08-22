package com.griddynamics.logtool;

import org.jboss.netty.buffer.ChannelBuffers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.io.ObjectStreamConstants.STREAM_MAGIC;
import static java.io.ObjectStreamConstants.STREAM_VERSION;
import static java.io.ObjectStreamConstants.TC_RESET;

public class LogEventDecoder extends FrameDecoder {
    private static final Logger logger = LoggerFactory.getLogger(LogEventDecoder.class);

    private static final int HEADER = (STREAM_MAGIC<<16) + STREAM_VERSION;

    private ObjectInputStream ois;
    private MutableBufferInputStream bufferStream;
    ChannelBuffer initBuffer;

    public LogEventDecoder() {
        initBuffer = ChannelBuffers.buffer(4);
        initBuffer.writeInt(HEADER);
        bufferStream = new MutableBufferInputStream(initBuffer);
        try {
            ois = new ObjectInputStream(bufferStream);
        } catch(IOException e) {
            logger.error(e.getMessage(), e);
        }
        bufferStream.setMark(0);
    }
    
    /**
     * Decodes the received packets so far into an object.
     *
     * @param ctx     the context of this handler
     * @param channel the current channel
     * @param buffer  the cumulative buffer of received packets so far.
     * @return the object if all parts were received and decoded.
     *         {@code null} if there's not enough data in the buffer to decode an object.
     */
    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
        List out = new ArrayList();
        if(bufferStream.updateBuffer(buffer)) {
            logger.trace("Buffer changed");
            logger.trace("Buffer size: " + bufferStream.available());
            if(bufferStream.available() > 4) {
                if(bufferStream.readInt() == HEADER) {
                    bufferStream.mark(0);
                }
            } else {
                return null;
            }
        } else if(bufferStream.available() == 0) {
            return null;
        }
        bufferStream.reset();
        try {
            while(bufferStream.available() > 0) {
                Object entry = ois.readObject();
                out.add(entry);
                bufferStream.mark(0);
                switch(bufferStream.available()) {
                    case 1:
                        if(bufferStream.read() != TC_RESET) {
                            bufferStream.reset();
                            break;
                        }
                    case 0:
                        bufferStream.setMark(0);
                        break;
                    default:
                        if(bufferStream.read() == TC_RESET) {
                            bufferStream.mark(0);
                        } else {
                            bufferStream.reset();
                        }
                }
            }
        } catch (EOFException e) {
            logger.trace("End of stream reached, finishing decode...");
            logger.trace("Objects retrieved: " + out.size());
            bufferStream.reset();
        } catch (StreamCorruptedException e) {
            bufferStream.reset();
            if(bufferStream.available() > 4) {
                if(bufferStream.readInt() == HEADER) {
                    ois.close();
                    bufferStream.reset();
                    bufferStream.updateBuffer(buffer.slice());
                    try {
                        ois = new ObjectInputStream(bufferStream);
                    } catch (IOException ex) {
                        throw ex;
                    }
                    bufferStream.mark(0);
                } else if(e.getStackTrace()[0].getMethodName().equals("refill")) {
                    ois.close();
                    initBuffer.readerIndex(0);
                    bufferStream.updateBuffer(initBuffer);
                    try {
                        ois = new ObjectInputStream(bufferStream);
                    } catch (IOException ex) {
                        throw ex;
                    }
                    bufferStream.updateBuffer(buffer);
                    bufferStream.reset();
                } else {
                    throw e;
                }
            }
        } finally {
            ois.close();
            if(bufferStream.available() > 0 && out.size() == 0) {
                bufferStream.setReaderIndex(0);
            }
        }
        if(out.size() > 0) {
            return out;
        } else {
            return null;
        }
    }
}