package Assignment3;

import java.net.BindException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.Random;

public abstract class Client {
    final int SERVER_PORT = 2711;
    final String HOST_NAME = "localhost";
    InetAddress address;
    DatagramSocket socket;
    double bid = 0;
    int port;
    DecimalFormat df = new DecimalFormat("#0.00");

    public void findGoodPort() {
        DatagramSocket testSocket;
        boolean goodport = false;
        Random rand = new Random();
        port = rand.nextInt(65535) + 1;
        while (!goodport) {
            try {
                testSocket = new DatagramSocket(port);
                testSocket.close();
                try {
                    testSocket = new DatagramSocket(port + 1);
                    goodport = true;
                    testSocket.close();
                } catch (BindException e) {
                    System.out.println("badPort");
                    port = rand.nextInt(65535) + 1;
                }
            } catch (BindException e) {
                System.out.println("badPort");
                port = rand.nextInt(65535) + 1;
            } catch (SocketException e) {}
        }
    }

    public abstract void main();
}
