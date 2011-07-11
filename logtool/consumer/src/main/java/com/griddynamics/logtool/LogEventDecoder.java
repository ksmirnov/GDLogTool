package com.griddynamics.logtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamConstants;

public class LogEventDecoder extends FrameDecoder {

    private static final Logger logger = LoggerFactory.getLogger(LogEventDecoder.class);

    private final ByteArrayOutputStream cache = new ByteArrayOutputStream();

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
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) {
        Object out = null;
        ByteArrayInputStream inputStream;
        if(cache.size() == 0) {
            buffer.markReaderIndex();
            short s0 = ObjectStreamConstants.STREAM_MAGIC;
            short s1 = ObjectStreamConstants.STREAM_VERSION;
            if(buffer.readable()) {
                if(buffer.readShort() != s0) {
                    for(byte b : getBytes(s0)) {
                        cache.write(b);
                    }
                }
                if(buffer.readShort() != s1) {
                    for(byte b : getBytes(s1)) {
                        cache.write(b);
                    }
                }
            }
            buffer.resetReaderIndex();
        }
        while(buffer.readable()) {
            cache.write(buffer.readByte());
        }
        inputStream = new ByteArrayInputStream(cache.toByteArray());
        try {
            out = new ObjectInputStream(inputStream).readObject();
            cache.reset();
        } catch (Exception e) {
            logger.trace(e.getMessage(), e);
        }
        return out;
    }

    /**
     * Turns short value to reversed byte array
     *
     * @param s value of short type
     * @return  reversed byte array
     */
    private static byte[] getBytes(short s) {
        return new byte[]{(byte)((s & 0xFF00)>>8), (byte)(s & 0x00FF)};
    }
}
