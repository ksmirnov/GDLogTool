package com.griddynamics.logtool.filetailer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

public class FileTailer {
    private Map<String, Thread> activeWatchingFiles = new HashMap<String, Thread>();

    public static void main(String[] args) {
        String filepath = "/home/slivotov/workspace/testForFileTailer";
        FileTailer ft = new FileTailer();
        ft.addFileForWatching(filepath,"localhost",4445);

    }

    public void addFileForWatching(String filepath,String host,int port) {
        File fileForWatching = new File(filepath);
        if (fileForWatching.exists()) {
            WatchingFile watchingFile = new WatchingFile(fileForWatching,host,port);
            Thread t = new Thread(watchingFile);
            activeWatchingFiles.put(filepath, t);
            t.start();
        }
    }

    public void removeFileWatching(String filepath) {
        Thread t = activeWatchingFiles.remove(filepath);
        t.stop();
    }

    public Set<String> getFilesWatching() {
        return activeWatchingFiles.keySet();
    }
}

class WatchingFile implements Runnable {
    private File fileForWatching;
    private String host;
    private int port;

    public WatchingFile(File fileForWatching,String host, int port){
        this.fileForWatching = fileForWatching;
        this.host = host;
        this.port = port;
    }

    public void run() {
        UDPSendler sendler = new UDPSendler(host,port);
        long prevModifyTime = fileForWatching.lastModified();
        FileInputStream fis = null;
        FileChannel channel = null;
        long prevChanelSize = 0;
        try {
            fis = new FileInputStream(fileForWatching);
            channel = fis.getChannel();
            prevChanelSize = channel.size();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            if (fileForWatching.lastModified() > prevModifyTime) {
                prevModifyTime = fileForWatching.lastModified();
                try {
                    long chanelSize = channel.size();
                    if ((chanelSize - prevChanelSize) > 0) {
                        MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, prevChanelSize, chanelSize - prevChanelSize);
                        prevChanelSize = chanelSize;
                        StringBuilder newText = new StringBuilder("");
                        while (buffer.hasRemaining()) {
                            newText.append((char) buffer.get());
                        }
                        sendler.sendText(newText.toString());
                    } else {
                        prevChanelSize = chanelSize;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

class UDPSendler {
    private static int port = 4445;
    private InetAddress IPAddress;
    private String notSendedText = "";

    UDPSendler(String host, int port){
         this.port = port;
        try {
            this.IPAddress = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) throws IOException, UnknownHostException {
        DatagramSocket clientSocket = new DatagramSocket();
        byte[] sendData = msg.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
        clientSocket.send(sendPacket);
        clientSocket.close();
    }

    public void sendText(String text) {
        if (notSendedText.length() != 0) {
            text = notSendedText + text;
            notSendedText = "";
        }
        if(text.lastIndexOf("\n") == -1){
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

