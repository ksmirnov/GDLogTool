package com.griddynamics.logtool.agents;

import java.io.IOException;
import java.net.*;
import java.util.StringTokenizer;

public class UDPSendler {
    private static int port = 4445;
    private InetAddress IPAddress;
    private String notSendedText = "";
    private DatagramSocket clientSocket;

    public UDPSendler(String host, int port) {
        this.port = port;
        try {
            this.IPAddress = InetAddress.getByName(host);
            clientSocket = new DatagramSocket();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) throws IOException, UnknownHostException {
        byte[] sendData = msg.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
        clientSocket.send(sendPacket);
    }

    public void sendText(String text) {
        if (notSendedText.length() != 0) {
            text = notSendedText + text;
            notSendedText = "";
        }
        if (text.lastIndexOf("\n") == -1) {
            notSendedText = text;
        } else {
            if (text.lastIndexOf("\n") != text.length() - 1) {
                notSendedText = text.substring(text.lastIndexOf("\n") + 1, text.length());
                text = text.substring(0, text.lastIndexOf("\n"));
            }
            StringTokenizer st = new StringTokenizer(text, "\n");
            while (st.hasMoreElements()) {
                try {
                    sendMsg(st.nextToken());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}