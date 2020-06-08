package Assignment1.UDPRTT;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class EchoClient {
    final static String HOST = "localhost";
    final static int PORTNUM = 2711;

    public static void main(String[] args) {
        ArrayList<Long> oneAvg = new ArrayList<>();
        ArrayList<Long> sixtyFourAvg = new ArrayList<>();
        ArrayList<Long> kbAvg = new ArrayList<>();
        for(int i = 0; i < 500; i++) {
            byte b[] = new byte[1];
            oneAvg.add(getTime(b));
            b = new byte[64];
            sixtyFourAvg.add(getTime(b));
            b = new byte[1024];
            kbAvg.add(getTime(b));
            System.out.println(i + "/500");
        }
        System.out.println("1 byte: "+ (average(oneAvg) / 1000000) + " ms");
        System.out.println("64 bytes: " + (average(sixtyFourAvg) / 1000000) + " ms");
        System.out.println("1024 bytes: " + (average(kbAvg) / 1000000) + " ms");
    }

    public static long getTime(byte[] msg) {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(HOST);
            long timeOut = System.nanoTime();
            DatagramPacket packet = new DatagramPacket(ByteBuffer.allocate(4).putInt(msg.length).array(), 4, address, PORTNUM);
            socket.send(packet);
            packet = new DatagramPacket(msg, msg.length, address, PORTNUM);
            socket.send(packet);
            packet = new DatagramPacket(msg, msg.length);
            socket.receive(packet);
            long timeElap = System.nanoTime() - timeOut;
            socket.close();
            return timeElap;
        } catch (IOException e) {
            System.err.println("IO Exception: ");
            e.printStackTrace();
            return 0;
        }
    }

    private static double average(ArrayList<Long> vals) {
        int l = vals.size();
        double t = 0;
        for (Long val : vals) {
            if (val > 0) {
                t += val;
            } else {
                l--;
            }
        }
        t /= l;
        return t;
    }
}