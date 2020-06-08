package Assignment2;

import Assignment2.tftp.TFTPServer;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

public class ProxyServer {

    private static DatagramSocket socket;
    private static InetAddress address;
    private static int PORTNUM = 12711;
    private static TFTPServer tftpServer;

    public static void main(String[] args) {
        try {
            socket = new DatagramSocket(2711);
            File remoteRead = new File("Saved");
            if (!remoteRead.exists()) {
                remoteRead.mkdir();
            }
            File remoteWrite = new File("Saved");
            byte[] s = new byte[4];

            for (; ; ) {
                DatagramPacket packet = new DatagramPacket(s, s.length);
                for (int i = 0; i < 1; i++) {
                    try {
                        socket.receive(packet);
                    } catch (SocketTimeoutException e) {
                        i--;
                    }
                }
                int b = ByteBuffer.wrap(packet.getData()).getInt();
                boolean window;
                window = b == 1;
                tftpServer = new TFTPServer(remoteRead, remoteWrite, 22711, TFTPServer.ServerMode.GET_ONLY,
                        null, null, window);
                int timeout = 500;
                socket.setSoTimeout(timeout);
                tftpServer.setSocketTimeout(timeout);
                socket.receive(packet);
                int size = ByteBuffer.wrap(s).getInt();
                byte[] u = new byte[size];
                packet = new DatagramPacket(u, u.length);
                socket.receive(packet);
                address = packet.getAddress();
                String url = new String(u);
                String checkURL = url.replaceAll("[:/]", "_");
                File dir = new File("Saved/" + checkURL);
                if(!dir.exists()) {
                    dir.mkdir();
                    Website website = new Website(url);
                    Elements jpgs = website.getDoc().getElementsByTag("img");
                    packet = new DatagramPacket(ByteBuffer.allocate(4).putInt(jpgs.size() + 1).array(), 4,
                            address, PORTNUM);
                    socket.send(packet);
                    System.out.println("Sending and caching " + (jpgs.size() + 1) + " file(s).");
                    for (Element jpg : jpgs) {
                        String src = jpg.absUrl("src");
                        System.out.println(src + " (Image)");
                        getImage(src, dir.getPath());
                    }
                    System.out.println(url + " (Website)");
                    savePage(url, dir.getPath());
                } else {
                    File[] files = dir.listFiles();
                    packet = new DatagramPacket(ByteBuffer.allocate(4).putInt(files.length).array(), 4,
                            address, PORTNUM);
                    socket.send(packet);
                    System.out.println("Sending contents of " + url + " (Folder)");
                    sendPages(files);
                }
                System.out.println("Done!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void getImage(String src, String path) throws IOException {
        if(src.isEmpty()) src = "empty";
        int indexName = src.lastIndexOf("/");
        if (indexName == src.length()) {
            src = src.substring(1, indexName);
        }
        indexName = src.lastIndexOf("/");
        String fileName = path + "/" + src.substring(indexName + 1, src.length());
        try {
            URL url = new URL(src.replace(" ", "%20"));
            InputStream in = url.openStream();
            OutputStream out = new BufferedOutputStream(new FileOutputStream(fileName));
            for (int b; (b = in.read()) != -1; ) {
                out.write(b);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            OutputStream out = new BufferedOutputStream(new FileOutputStream(fileName));
            out.write(0);
            out.close();
        }
        byte[] s = new byte[4];
        DatagramPacket packet = new DatagramPacket(s, s.length);
        for (int i = 0; i < 1; i++) {
            try {
                socket.receive(packet);
            } catch (SocketTimeoutException e) {
                i--;
            }
        }
        packet = new DatagramPacket(ByteBuffer.allocate(4).putInt(fileName.length()).array(), 4, address,
                PORTNUM);
        socket.send(packet);
        packet = new DatagramPacket(fileName.getBytes(), fileName.length(), address, PORTNUM);
        socket.send(packet);
        packet = new DatagramPacket(s, s.length);
        for (int i = 0; i < 1; i++) {
            try {
                socket.receive(packet);
            } catch (SocketTimeoutException e) {
                i--;
            }
        }
    }

    public static void savePage(String url, String path) throws IOException {
        String fileName = path + "/" + url.replaceAll("[:/]", "_") + ".html";
        try {
            URL u = new URL(url);
            InputStream in = u.openStream();
            OutputStream out = new BufferedOutputStream(new FileOutputStream(fileName));
            for (int b; (b = in.read()) != -1; ) {
                out.write(b);
            }
            out.close();
            in.close();
        } catch (MalformedURLException e) {
            OutputStream out = new BufferedOutputStream(new FileOutputStream(fileName));
            out.write(0);
            out.close();
        }
        byte[] s = new byte[4];
        DatagramPacket packet = new DatagramPacket(s, s.length);
        for (int i = 0; i < 1; i++) {
            try {
                socket.receive(packet);
            } catch (SocketTimeoutException e) {
                i--;
            }
        }
        packet = new DatagramPacket(ByteBuffer.allocate(4).putInt(fileName.length()).array(), 4, address,
                PORTNUM);
        socket.send(packet);
        packet = new DatagramPacket(fileName.getBytes(), fileName.length(), address, PORTNUM);
        socket.send(packet);
        packet = new DatagramPacket(s, s.length);
        for (int i = 0; i < 1; i++) {
            try {
                socket.receive(packet);
            } catch (SocketTimeoutException e) {
                i--;
            }
        }
    }

    public static void sendPages(File[] files) throws IOException {
        for(int i = 0; i < files.length; i++) {
            System.out.println("Sending file " + (i + 1) + "/" + (files.length));
            byte[] s = new byte[4];
            DatagramPacket packet = new DatagramPacket(s, s.length);
            for (int j = 0; j < 1; j++) {
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException e) {
                    j--;
                }
            }
            packet = new DatagramPacket(ByteBuffer.allocate(4).putInt(files[i].getPath().length()).array(), 4,
                    address, PORTNUM);
            socket.send(packet);
            packet = new DatagramPacket(files[i].getPath().getBytes(), files[i].getPath().length(), address, PORTNUM);
            socket.send(packet);
            packet = new DatagramPacket(s, s.length);
            for (int j = 0; j < 1; j++) {
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException e) {
                    j--;
                }
            }
        }
    }
}
