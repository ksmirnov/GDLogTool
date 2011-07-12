package com.griddynamics.logtool;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.junit.*;
import static org.junit.Assert.*;

public class LogEventDecoderTest {

    private static class SerializableObject implements Serializable {}
    
    
    @Test
    public void testDecode() throws Exception {
        SerializableObject testObject = new SerializableObject();
        Object output;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(10);
        ObjectOutputStream oos = null;
        ChannelBuffer buffer;
        byte[] bArray;
        LogEventDecoder decoderInstance = new LogEventDecoder();
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(testObject);
        } finally {
            oos.flush();
            oos.close();
        }
        if(baos.size() > 0) {
            bArray = baos.toByteArray();
        } else {
            return;
        }
        buffer = ChannelBuffers.buffer(baos.size() * 2);
        for(int i = 0; i < bArray.length / 2; i ++) {
            buffer.writeByte(bArray[i]);
        }
        // 1st call: half of an object with all required flags
        decoderInstance.decode(null, null, buffer);
        for(int i = bArray.length / 2; i < bArray.length; i ++) {
            buffer.writeByte(bArray[i]);
        }
        // 2nd call: rest of an object
        output = decoderInstance.decode(null, null, buffer);
        assertTrue("Object (with flags) hasn't returned", output != null);
        for(int i = 4; i < bArray.length / 2; i ++) {
            buffer.writeByte(bArray[i]);
        }
        // 3rd call: half of an object without required flags
        decoderInstance.decode(null, null, buffer);
        for(int i = bArray.length / 2; i < bArray.length; i ++) {
            buffer.writeByte(bArray[i]);
        }
        // 4th call: rest of an object
        output = decoderInstance.decode(null, null, buffer);
        assertTrue("Object (without flags) hasn't returned", output != null);
    }

}
