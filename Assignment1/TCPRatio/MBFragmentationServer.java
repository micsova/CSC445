package Assignment1.TCPRatio;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class MBFragmentationServer {
    static final int PORT = 12711;

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            serverSocket.setSoTimeout(200);
            Socket client;

            for (;;) {
                try {
                    client = serverSocket.accept();
                    System.out.println("Working on Port " + PORT);
                    break;
                } catch (SocketTimeoutException e) {
                }
            }

            OutputStream out = client.getOutputStream();
            DataOutputStream dos = new DataOutputStream(out);
            InputStream in = client.getInputStream();
            DataInputStream dis = new DataInputStream(in);
            int size = 0;

            for(;;) {
                try {
                    int numMessages = dis.readInt();
                    for (int i = 0; i < numMessages; i++) {
                        size = dis.readInt();
                        byte b[] = new byte[size];
                        dis.readFully(b);
                    }
                } catch (SocketTimeoutException e) {
                    System.err.println("Timout");
                } catch (EOFException e) {
                    System.out.println("Done on Port " + PORT + "!");
                    break;
                }
                dos.writeChar('y');
                System.out.println("Received " + (size / 1024) + " byte packages");
            }
            dis.close();
            out.close();
            in.close();
            client.close();
            serverSocket.close();
        }
        catch (IOException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }
}