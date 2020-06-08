package Assignment1.TCPRTT;

import java.net.*;
import java.io.*;

public class EchoServer {
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

            for(;;) {
                try {
                    int size = dis.readInt();
                    byte b[] = new byte[size];
                    dis.readFully(b);
                    dos.write(b);
                } catch (SocketTimeoutException e) {
                    System.err.println("Timeout");
                } catch (EOFException e) {
                    System.out.println("Done on Port " + PORT + "!");
                    break;
                }
            }
            dos.close();
            dis.close();
            out.close();
            in.close();
            client.close();
            serverSocket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }
}