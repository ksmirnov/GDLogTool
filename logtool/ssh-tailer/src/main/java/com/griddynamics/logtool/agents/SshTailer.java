package com.griddynamics.logtool.agents;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SshTailer {
    Map<String, Thread> activeWatchingHosts = new HashMap<String, Thread>();

    public static void main(String[] args) {
        Properties props = new Properties();
        try {
            InputStream in = SshTailer.class.getResourceAsStream("/sshtailerConfig.properties");
            props.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SshTailer sshTailer = new SshTailer();
        String hostToSend = props.getProperty("hostToSend");
        int portToSend = Integer.parseInt(props.getProperty("portToSend"));
        sshTailer.addHostForWatching(props.getProperty("hostToReadFrom"), props.getProperty("filesToReadFrom"),
                hostToSend, portToSend);
        int i = 1;
        while (props.getProperty("hostToReadFrom" + i) != null) {
            sshTailer.addHostForWatching(props.getProperty("hostToReadFrom" + i), props.getProperty("filesToReadFrom" + i),
                    hostToSend, portToSend);
            i++;
        }
    }

    public void addHostForWatching(String hostToReadFrom, String filesToRead, String hostToSend, int portToSend) {
        WatchingSsh watchingSsh = new WatchingSsh(hostToReadFrom, filesToRead, hostToSend, portToSend);
        Thread t = new Thread(watchingSsh);
        activeWatchingHosts.put(hostToReadFrom, t);
        t.start();
    }

    public void removeHostWatching(String filepath) {
        Thread t = activeWatchingHosts.remove(filepath);
        t.stop();
    }

    public Set<String> getFilesWatching() {
        return activeWatchingHosts.keySet();
    }
}


class WatchingSsh implements Runnable {
    UDPSendler sendler;
    String host;
    String files;
    boolean singleFile;
    String header;
    List<Pattern> patternList = new ArrayList<Pattern>();
    Pattern headerPatter = Pattern.compile("==> (.+) <==");

    public WatchingSsh(String hostToReadFrom, String filesToRead, String hostToSend, int portToSend) {
        sendler = new UDPSendler(hostToSend, portToSend);
        this.host = hostToReadFrom;
        this.files = filesToRead;
        if (filesToRead.indexOf("*") == -1 && filesToRead.indexOf(" ") == -1) {
            singleFile = true;
        } else {
            StringTokenizer stTok = new StringTokenizer(filesToRead, " ");
            while (stTok.hasMoreElements()) {
                String bufSt = stTok.nextToken();
                if (bufSt.indexOf('*') != -1) {
                    patternList.add(createPattern(bufSt));
                }
            }
            singleFile = false;
        }
    }

    private Pattern createPattern(String st) {
        StringBuilder sb = new StringBuilder(st);
        int starInd = sb.indexOf("*");
        sb.insert(starInd, ".");
        if (sb.indexOf("/", starInd) != -1) {
            sb.insert(sb.indexOf("/", starInd), ")");
        } else {
            sb.append(")");
        }
        sb.insert(sb.lastIndexOf("/", starInd) + 1, "(");
        return Pattern.compile(sb.toString());
    }

    private String convertToConsumerFormat(String mes) {
        if (singleFile) {
            String fileName = files.substring(files.lastIndexOf("/") + 1, files.length());
            return "| SshTailer_" + host + " | " + fileName + " | " + mes;
        } else {
            return "| SshTailer_" + host + " | " + header + " | " + mes;
        }

    }

    private boolean checkStringForHeader(String mes) {
        if (singleFile) {
            return false;
        }
        Matcher m = headerPatter.matcher(mes);
        if (m.matches()) {
            String s = m.group(1);
            boolean match = false;
            for (Pattern p : patternList) {
                m = p.matcher(s);
                if (m.matches()) {
                    header = m.group(1);
                    match = true;
                    break;
                }
            }
            if (!match) {
                header = s.substring(s.lastIndexOf("/") + 1, s.length());
            }
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        final SSHClient ssh = new SSHClient();
        try {
            ssh.loadKnownHosts();
            ssh.connect(host);
            try {
                ssh.authPublickey(System.getProperty("user.name"));
                final Session session = ssh.startSession();
                try {
                    final Command cmd = session.exec("tail -F " + files);
                    InputStream is = cmd.getInputStream();
                    int r = is.read();
                    StringBuilder sb = new StringBuilder("");
                    while (r != -1) {
                        if ((char) r != '\n') {
                            sb.append((char) r);
                        } else if (sb.toString().length() > 1) {
                            if (!checkStringForHeader(sb.toString())) {
                                sendler.sendMsg(convertToConsumerFormat(sb.toString()));
                            }
                            sb.delete(0, sb.length());
                        }
                        r = is.read();
                    }
                    cmd.join(5, TimeUnit.SECONDS);
                    System.out.println("\n** exit status: " + cmd.getExitStatus());
                } finally {
                    session.close();
                }
            } finally {
                ssh.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
