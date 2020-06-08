package Assignment1.UDPRatio;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class MBFragmentationClient {
    static final int KB = 1024;
    static final int MB = KB * 1024;
    static final String HOST = "129.3.20.62";
    static final int PORTNUM = 2711;
    public static void main(String[] args) {
        ArrayList<Long> fourAvg = new ArrayList<>();
        ArrayList<Long> twoAvg = new ArrayList<>();
        ArrayList<Long> oneAvg = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            byte b[] = new byte[4 * KB];
            fourAvg.add(getTime(b));
            b = new byte[2 * KB];
            twoAvg.add(getTime(b));
            b = new byte[1 * KB];
            oneAvg.add(getTime(b));
            System.out.println((i + 1) + "/10");
        }
        System.out.println("4KB packages: " + average(fourAvg));
        System.out.println("2KB packages: " + average(twoAvg));
        System.out.println("1KB packages: " + average(oneAvg));
    }

    public static Long getTime(byte[] b) {
        DatagramSocket socket;
        try {
            for (int i = 0; i < b.length; i++) {
                b[i] = (byte) i;
            }
            socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(HOST);
            long timeOut = System.nanoTime();
            int numMessages = MB / b.length;
            System.out.println("numMessages = " + numMessages);
            DatagramPacket packet = new DatagramPacket(ByteBuffer.allocate(4).putInt(numMessages).array(),
                    4, address, PORTNUM);
            socket.send(packet);
            int s = 0;
            for(;;) {
                socket.setSoTimeout(220);
                for (int i = s; i < numMessages; i++) {
                    packet = new DatagramPacket(ByteBuffer.allocate(4).putInt(b.length).array(),
                            4, address, PORTNUM);
                    socket.send(packet);
                    packet = new DatagramPacket(b, b.length, address, PORTNUM);
                    socket.send(packet);
                    packet = new DatagramPacket(ByteBuffer.allocate(4).putInt(i + 1).array(),
                            4, address, PORTNUM);
                    socket.send(packet);
                    for(;;) {
                        try {
                            byte rec[] = new byte[4];
                            packet = new DatagramPacket(rec, rec.length);
                            socket.receive(packet);
                            i = ByteBuffer.wrap(packet.getData()).getInt();
                            break;
                        } catch (SocketTimeoutException e) {
                            System.out.println("Timeout on message " + (i + 1));
                        }
                    }
//                    if((i + 1) % 64 == 0) {
//                        try {
//                            byte check[] = new byte[4];
//                            packet = new DatagramPacket(check, check.length);
//                            socket.receive(packet);
//                            i = ByteBuffer.wrap(check).getInt();
//                            System.out.println("Timeout on message " + i);
//                            i--;
//                            if (i < -1) {
//                                i = -1;
//                            }
//                            System.out.println("(i = " + (i + 1) + " and numMessages = " + numMessages + ")");
//                        } catch (SocketTimeoutException e) {
//                        }
//                    }
                }
                try {
                    byte ack[] = new byte[4];
                    packet = new DatagramPacket(ack, ack.length);
                    socket.receive(packet);
                    byte a = ByteBuffer.wrap(packet.getData()).get();
                    if (a == 'y') {
                        break;
                    } else {
                        System.out.println("a = " + a);
                        s = (int) a;
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Timeout");
                }
            }
            long timeElap = System.nanoTime() - timeOut;
            socket.close();
            return timeElap;
        } catch (IOException e) {
            System.err.println("IO Exception: ");
            e.printStackTrace();
            return null;
        }
    }

    private static double average(ArrayList<Long> vals) {
        int l = vals.size();
        double t = 0;
        for (Long val : vals) {
            if(val != null) {
                t += (val / 1000000);
            }
        }
        t /= l;
        return t;
    }
}