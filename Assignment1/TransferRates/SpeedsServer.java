package Assignment1.TransferRates;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class SpeedsServer {
    static final int PORT = 2711;

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

            for (;;) {
                try {
                    long timeStart = System.nanoTime();
                    int size = dis.readInt();
                    byte b[] = new byte[size];
                    dis.readFully(b);
                    long uploadTime = System.nanoTime() - timeStart;
                    dos.writeLong(uploadTime);
                    dos.write(b);
                } catch (SocketTimeoutException e) {
                    System.err.println("Timeout");
                } catch (EOFException e) {
                    System.out.println("Done on Port " + PORT + "!");
                    break;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }
}