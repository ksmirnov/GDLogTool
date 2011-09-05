package com.griddynamics.logtool.fixtures;

import fitlibrary.DoFixture;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SshFixture extends DoFixture {
    private String hostToReadFrom;
    private String userToReadFrom;
    private String pathToFile;
    private String hostToSend;
    private int portToSend;
    private final SSHClient ssh = new SSHClient();
    Process tailerProcess;

    public void setHostToReadFrom(String hostToReadFrom) {
        this.hostToReadFrom = hostToReadFrom;
    }

    public void setUserToReadFrom(String userToReadFrom) {
        this.userToReadFrom = userToReadFrom;
    }

    public void setPathToFile(String pathToFile) {
        this.pathToFile = pathToFile;
    }

    public void setHostToSend(String hostToSend) {
        this.hostToSend = hostToSend;
    }

    public void setPortToSend(int portToSend) {
        this.portToSend = portToSend;
    }





    public void createFileBySsh() throws IOException {
        if (!ssh.isConnected()) {
            sshConnect();
        }
        execSshCommand("touch " + pathToFile);
        System.out.println("File successfully created");
    }

    public void startSshTailerWatching() {
        try {
            tailerProcess = Runtime.getRuntime().exec("java -cp target/lib/bcprov-jdk16-1.46.jar:" +
                    "target/lib/sshj-0.5.0.jar:target/lib/slf4j-api-1.6.1.jar:" +
                    "target/lib/udp-sendler-1.0.0-SNAPSHOT.jar:target/lib/ssh-tailer-1.0.0-SNAPSHOT.jar " +
                    "com.griddynamics.logtool.agents.SshTailer " + hostToReadFrom + " " +
            pathToFile + " " + userToReadFrom + " " + hostToSend + " " + portToSend);
            System.out.println("Successfully start ssh-tailer");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeMessageToFile(String msg) throws IOException, InterruptedException {
        if (!ssh.isConnected()) {
            sshConnect();
        }
        Thread.sleep(200);
        execSshCommand("echo '" + msg + "' >> " + pathToFile);
        System.out.println("Successfully written");
    }

    public void stopSshTailerWatching() throws InterruptedException {
            System.out.println("Trying to kill process with :" + "ps aux | grep ssh-tailer-1.0.0-SNAPSHOT.jar.*" + pathToFile);
            tailerProcess.destroy();
    }

    public void deleteFileBySsh() throws IOException, InterruptedException {
        Thread.sleep(1000);
        execSshCommand("rm " + pathToFile);
        sshDisconnect();
        System.out.println("Successfully deleted");

    }

    private void sshConnect() {
        try {
            ssh.loadKnownHosts();
            ssh.connect(hostToReadFrom);
            ssh.authPublickey(userToReadFrom);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void execSshCommand(String command) throws IOException {
        Session session = null;
        try {
            session = ssh.startSession();
            session.exec(command);
            session.close();
        } finally {
            if (session != null) session.close();
        }
    }

    private void sshDisconnect() throws IOException {
        ssh.disconnect();
    }

    public static void main (String[] args) {
        SshFixture sf = new SshFixture();
        sf.setHostToReadFrom("localhost");
        sf.setUserToReadFrom("slivotov");
        sf.setPathToFile("/home/slivotov/blakie.txt");
        sf.setHostToSend("localhost");
        sf.setPortToSend(4445);
        try {
            sf.createFileBySsh();
            sf.startSshTailerWatching();
            sf.writeMessageToFile("Aga klassno poluchilos! ");
            sf.writeMessageToFile("Eto tebe ne siski myat'! ");
            sf.writeMessageToFile("Eto SPARTA!!");
            sf.deleteFileBySsh();
            sf.stopSshTailerWatching();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void deleteLog(String searchQuery) throws Exception {
                LogtoolRequester requester = new LogtoolRequester(hostToSend, portToSend);
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "doSolrSearch");
        params.put("subaction", "solrsearch");
        params.put("query", searchQuery);
        String response = requester.get(params);
        response = response.substring(response.indexOf("["), response.length());
        JSONArray array=(JSONArray) JSONValue.parse(response);
        JSONObject obj = (JSONObject) array.get(0);
        System.out.println(PathConstructor.reversePath(obj.get("path").toString()));
//        LogtoolRequester lr = new LogtoolRequester(host, port);
        params.clear();
        params.put("action", "deleteLog");
        params.put("path", PathConstructor.reversePath(obj.get("path").toString()));
        requester.get(params);
    }

}
