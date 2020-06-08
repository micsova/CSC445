package Assignment2;

import Assignment2.tftp.TFTP;
import Assignment2.tftp.TFTPClient;
import Assignment2.tftp.TFTPPacket;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * When requesting a URL, the server will check if it has that page cached.
 *
 * It will then either send just the cached HTML file or it will access
 * the website and send the HTML file along with every image on the page
 */
public class Client {
    static final String USAGE =
            "Usage: [options] hostname url\n\n" +
                    "hostname   - The name of the remote host\n" +
                    "url - The URL of the website you are requesting\n\n" +
                    "options:\n" +
                    "\t-t timeout in seconds (default 60s)\n" +
                    "\t-v Verbose (trace packets)\n" +
                    "\t-i version of IP (4 or 6) to use (default is 4)\n" +
                    "\t-d pretend to drop 1% of packets\n" +
                    "\t-w use TCP-style sliding windows instead of sequential ACKs\n";
    private static String hostName;
    private static int PORTNUM = 2711;
    private static int TFTPPORTNUM = 22711;
    private static String url;
    private static boolean drops;
    private static boolean window;
    private static InetAddress address;
    private static final int TRANSFER_MODE = TFTP.BINARY_MODE;
    private static DatagramPacket ACK;
    private static DatagramSocket socket;
    private static ArrayList<Integer> totalBytesRead = new ArrayList<>();
    private static long timeOut;
    private static long timeElap;

    public static void main(String[] args) {
        url = args[args.length - 1];
        hostName = args[args.length - 2];
        boolean closed = false;
        int argc;
        String arg;
        final TFTPClient tftp;
        int timeout = 60000;
        boolean verbose = false;
        for (argc = 0; argc < args.length; argc++) {
            arg = args[argc];
            if (arg.startsWith("-")) {
                switch (arg) {
                    case "-t":
                        timeout = 1000 * Integer.parseInt(args[++argc]);
                        break;
                    case "-v":
                        verbose = true;
                        break;
                    case "-d":
                        drops = true;
                        break;
                    case "-w":
                        window = true;
                        break;
                    default:
                        System.err.println("Error: unrecognized option. (" + arg + ")");
                        System.err.print(USAGE);
                        System.exit(1);
                }
            } else {
                break;
            }
        }
        try {
            address = InetAddress.getByName(hostName);
        } catch (UnknownHostException e) {
            System.err.println("Error: Invalid hostname.");
            System.exit(1);
        }
        if(address instanceof Inet4Address) {
            System.out.println("IPv4");
        } else if (address instanceof Inet6Address) {
            System.out.println("IPv6");
        }
        sendURL(url);

        byte[] s = new byte[4];
        DatagramPacket packet;
        try {
            packet = new DatagramPacket(s, s.length);
            socket.setSoTimeout(timeout);
            for (int i = 0; i < 1; i++) {
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException e) {
                    i--;
                }
            }
        } catch (IOException e) {
            System.err.println("Error: Didn't receive number of elements.");
            e.printStackTrace();
            System.exit(1);
        }

        int numElements = ByteBuffer.wrap(s).getInt();

        if (args.length - argc != 2) {
            System.err.println("Error: invalid number of arguments");
            System.err.println(USAGE);
            System.exit(1);
        }

        hostName = args[argc];

        if (verbose) {
            tftp = new TFTPClient(socket) {
                @Override
                protected void trace(String direction, TFTPPacket packet) {
                    System.out.println(direction + " " + packet);
                }
            };
        } else {
            tftp = new TFTPClient(socket);
        }

        tftp.setDefaultTimeout(timeout);

        System.out.println("Initiating transfer.");
        for (int i = 0; i < numElements; i++) {
            System.out.println("Receiving file " + (i + 1) + "/" + numElements);
            closed = receive(tftp);
        }
        timeElap = System.nanoTime() - timeOut;

        if (!closed) {
            System.out.println("Failed");
            System.exit(1);
        }
        average();
        System.out.println("Transfer complete.");
    }

    private static boolean receive(TFTPClient tftp) {
        byte[] s = new byte[4];
        boolean closed;
        FileOutputStream fos = null;
        byte[] f = null;
        DatagramPacket packet;
        try {
            for (int i = 0; i < 1; i++) {
                socket.send(ACK);
                packet = new DatagramPacket(s, s.length);
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException e) {
                    i--;
                }
                if(ByteBuffer.wrap(s).getInt() > 1000) {
                    i--;
                }
            }
            f = new byte[ByteBuffer.wrap(s).getInt()];
            packet = new DatagramPacket(f, f.length);
            socket.receive(packet);
        } catch (IOException e) {
            System.err.println("Error: Could not receive file name");
            e.printStackTrace();
            System.exit(1);
        }

        String fileName = new String(f);
        File dir = new File ("Received");
        if(!dir.exists()) {
            dir.mkdir();
        }
        fileName = fileName.substring(fileName.indexOf("/") + 1, fileName.length());
        dir = new File("Received/" + fileName.substring(0, fileName.indexOf("/")));
        if(!dir.exists()) {
            dir.mkdir();
        }
        String localFile = fileName.substring(fileName.lastIndexOf("/"), fileName.length());
        File file = new File(dir.getPath() + "/" + localFile);

        try {
            fos = new FileOutputStream(file);
        } catch (IOException e) {
            tftp.close();
            System.err.println("Error: Could not open local file for writing.");
            e.printStackTrace();
            System.exit(1);
        }

        tftp.open();

        int bytesRead = 0;

        try {
            bytesRead = tftp.receiveFile(fileName, TRANSFER_MODE, fos, address, TFTPPORTNUM, drops, window);
        } catch (UnknownHostException e) {
            System.err.println("Error: Could not resolve hostname.");
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Error: I/O Exception occurred while receiving file.");
            e.printStackTrace();
            System.exit(1);
        } finally {
            closed = close(fos);
        }
        try {
            socket.send(ACK);
        } catch (IOException e) {}
        if(bytesRead != 0) {
            totalBytesRead.add(bytesRead);
        }
        return closed;
    }

    private static boolean close(Closeable fos) {
        boolean closed;
        try {
            if (fos != null) {
                fos.close();
            }
            closed = true;
        } catch (IOException e) {
            closed = false;
            System.err.println("Error: Error closing file");
            System.err.println(e.getMessage());
        }
        return closed;
    }

    private static void sendURL(String url) {
        try {
            address = InetAddress.getByName(hostName);
            ACK = new DatagramPacket(ByteBuffer.allocate(4).putInt(1).array(), 4, address, PORTNUM);
            socket = new DatagramSocket(12711);
            byte[] bytes = url.getBytes();
            int w = window ? 1 : 0;
            DatagramPacket packet = new DatagramPacket(ByteBuffer.allocate(4).putInt(w).array(), 4, address,
                    PORTNUM);
            timeOut = System.nanoTime();
            socket.send(packet);
            packet = new DatagramPacket(ByteBuffer.allocate(4).putInt(bytes.length).array(), 4,
                    address, PORTNUM);
            socket.send(packet);
            packet = new DatagramPacket(bytes, bytes.length, address, PORTNUM);
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("Error: Error sending URL to server.");
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void average() {
        int totalBytes = 0;
        for(int i : totalBytesRead) {
            totalBytes += i;
        }
        double micros = (timeElap/1000);
        double millis = (micros/1000);
        double seconds = (millis/1000);
        System.out.println("Total time = " + seconds + " seconds");
        double totalBits = totalBytes * 8;
        double kb = totalBits/1000;
        double kbps = kb/seconds;
        if(kbps < 1000) {
            System.out.println("Total bits = " + kb + " kilobits");
            System.out.println("Average throughput over " + totalBytesRead.size() + " files = " + kbps + " kbps.");
        } else {
            double mb = kb / 1000;
            double mbps = mb/seconds;
            System.out.println("Total bits = " + mb + " megabits");
            System.out.println("Average throughput over " + totalBytesRead.size() + " files = " + mbps + " mbps.");
        }
    }
}
