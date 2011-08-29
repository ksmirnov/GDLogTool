package com.griddynamics.logtool.fixtures;

import fit.ActionFixture;
import java.net.*;

public class SendlerFixture extends ActionFixture {
    public void sendMessage(String data) throws Exception {
        DatagramSocket ds = new DatagramSocket();

        DatagramPacket sendPacket = new DatagramPacket(data.getBytes(), data.length(), InetAddress.getByName("localhost"), 4445);
        ds.send(sendPacket);
        ds.close();
    }
}
