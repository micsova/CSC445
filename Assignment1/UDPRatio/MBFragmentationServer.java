package Assignment1.UDPRatio;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

public class MBFragmentationServer extends Thread {
    static final int PORT = 2711;

    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket(PORT);
            socket.setSoTimeout(200);

            for(;;) {
                byte[] s = new byte[4];
                DatagramPacket packet = new DatagramPacket(s, s.length);
                for (int i = 0; i < 1; i++) {
                    try {
                        socket.receive(packet);
                    } catch (SocketTimeoutException e) {
                        i--;
                    }
                }
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                int size;
                int numMessages = ByteBuffer.wrap(packet.getData()).getInt();
                System.out.println("numMessages = " + numMessages);
                for(int i = 0; i < numMessages; i++) {
                    try {
                        packet = new DatagramPacket(s, s.length);
                        socket.receive(packet);
                        size = ByteBuffer.wrap(packet.getData()).getInt();
                        byte[] receive = new byte[size];
                        packet = new DatagramPacket(receive, receive.length);
                        socket.receive(packet);
                        packet = new DatagramPacket(s, s.length);
                        socket.receive(packet);
                        System.out.println("i = " + i);
                        packet = new DatagramPacket(ByteBuffer.allocate(4).putInt(i).array(), 4, address, port);
                        socket.send(packet);
                    } catch (SocketTimeoutException e) {
                        System.out.println("Timeout on message " + (i + 1));
                        packet = new DatagramPacket(ByteBuffer.allocate(4).putInt(i).array(), 4, address, port);
                        socket.send(packet);
                        if(i >= 0) {
                            i--;
                        }
                    }
                }
                System.out.println("Completed transfer of " + numMessages + " messages");
                packet = new DatagramPacket(ByteBuffer.allocate(4).put((byte) 'y').array(), 4, address, port);
                socket.send(packet);
            }
        } catch (IOException e) {
            System.err.println("IO Exception: ");
            e.printStackTrace();
        }
    }
}