package Assignment1.UDPRTT;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class EchoServer extends Thread {

    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket(2711);
            boolean running = true;
            byte[] s = new byte[4];

            while (running) {
                DatagramPacket packet = new DatagramPacket(s, s.length);
                socket.receive(packet);
                int size = ByteBuffer.wrap(packet.getData()).getInt();
                byte[] echo = new byte[size];
                packet = new DatagramPacket(echo, echo.length);
                socket.receive(packet);

                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                packet = new DatagramPacket(echo, echo.length, address, port);
                String received = new String(packet.getData(), 0, packet.getLength());

                if (received.equals("end")) {
                    running = false;
                    continue;
                }
                socket.send(packet);
            }
            socket.close();
        } catch (IOException e) {
            System.err.println("IO Exception: ");
            e.printStackTrace();
        }
    }
}