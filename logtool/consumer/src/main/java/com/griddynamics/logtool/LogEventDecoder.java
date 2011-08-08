package com.griddynamics.logtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

import java.io.*;

import static java.io.ObjectStreamConstants.STREAM_MAGIC;
import static java.io.ObjectStreamConstants.STREAM_VERSION;

public class LogEventDecoder extends FrameDecoder {
    private static final Logger logger = LoggerFactory.getLogger(LogEventDecoder.class);

    private static final int HEADER = (STREAM_MAGIC<<16) + STREAM_VERSION;

    private ObjectInputStream ois;
    private MutableBufferInputStream bufferStream;

    
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
        Object out = null;
        if(bufferStream == null) {
            bufferStream = new MutableBufferInputStream(buffer);
            ois = new ObjectInputStream(bufferStream);
            bufferStream.mark(0);
        } else {
            bufferStream.updateBuffer(buffer);
        }
        bufferStream.reset();
        try {
            out = ois.readObject();
            bufferStream.setMark(0);
            bufferStream.setReaderIndex(bufferStream.getWriterIndex());
        } catch (EOFException e) {
            logger.trace("Unable to read object, end of stream reached");
            bufferStream.setReaderIndex(0);
        } catch (StreamCorruptedException e) {
            if(buffer.readableBytes() >= 4) {
                if(buffer.readInt() == HEADER) {
                    logger.trace("Header has sent repeatedly");
                    bufferStream.setReaderIndex(0);
                    bufferStream.setMark(4);
                } else {
                    throw e;
                }
            } else {
                bufferStream.setReaderIndex(0);
            }
        } finally {
            ois.close();
        }
        return out;
    }


}
