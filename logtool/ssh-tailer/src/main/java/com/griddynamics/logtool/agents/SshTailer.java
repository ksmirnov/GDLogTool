package com.griddynamics.logtool.agents;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SshTailer {
    Map<String, Thread> activeWatchingHosts = new HashMap<String, Thread>();

    public static void main(String[] args) {
        if(args.length % 5 != 0 ){
            System.out.println("Incorrect properties");
        } else {
            SshTailer sshTailer = new SshTailer();
            for (int i = 0; i < args.length/5; i++) {
                String hostToReadFrom = args[0 + 5*i];
                String filesToRead = args[1 + 5*i];
                String userToReadFrom = args[2 + 5*i];
                String hostToSend= args[3 + 5*i];
                int portToSend = Integer.parseInt(args[4 + 5*i]);
                sshTailer.addHostForWatching(hostToReadFrom, filesToRead, userToReadFrom, hostToSend , portToSend);
            }
        }
    }

    public void addHostForWatching(String hostToReadFrom, String filesToRead, String userToReadFrom, String hostToSend, int portToSend) {
        WatchingSsh watchingSsh = new WatchingSsh(hostToReadFrom, filesToRead, userToReadFrom, hostToSend, portToSend);
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
    String user;
    boolean singleFile;
    String header;
    List<Pattern> patternList = new ArrayList<Pattern>();
    Pattern headerPatter = Pattern.compile("==> (.+) <==");

    public WatchingSsh(String hostToReadFrom, String filesToRead,String userToReadFrom, String hostToSend, int portToSend) {
        sendler = new UDPSendler(hostToSend, portToSend);
        this.host = hostToReadFrom;
        this.user = userToReadFrom;
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
                ssh.authPublickey(user);
                final Session session = ssh.startSession();
                try {
                    final Command cmd = session.exec("tail -F " + files);
                    InputStream is = cmd.getInputStream();
                    int r = is.read();
                    StringBuilder sb = new StringBuilder("");
                    while (r != -1) {
                        if ((char) r != '\n') {
                            sb.append((char) r);
                        } else if (sb.toString().length() > 0) {
                            if (!checkStringForHeader(sb.toString())) {
                                System.out.println("Successfully send by tailer: " + convertToConsumerFormat(sb.toString()));
                                sendler.sendMsg(convertToConsumerFormat(sb.toString()));
                            }
                            sb.delete(0, sb.length());
                        }
                        r = is.read();
                    }
                    cmd.join(5, TimeUnit.SECONDS);
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
