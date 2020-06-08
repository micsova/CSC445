package Assignment1.TCPRTT;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class EchoClient {
    static final String HOST = "129.3.20.62";
    static final int echoServicePortNumber = 2711;
    public static void main(String[] args) {
        ArrayList<Long> oneAvg = new ArrayList<>();
        ArrayList<Long> sixtyFourAvg = new ArrayList<>();
        ArrayList<Long> kbAvg = new ArrayList<>();
        try {
            Socket echoSocket = new Socket(HOST, echoServicePortNumber);
            echoSocket.setSoTimeout(300);
            OutputStream out = echoSocket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(out);
            InputStream in = echoSocket.getInputStream();
            DataInputStream dis = new DataInputStream(in);
            for (int i = 0; i < 200; i++) {
                byte b[] = new byte[1];
                oneAvg.add(getTime(b, dos, dis));
                b = new byte[64];
                sixtyFourAvg.add(getTime(b, dos, dis));
                b = new byte[1024];
                kbAvg.add(getTime(b, dos, dis));
                System.out.println((i + 1) + "/200");
            }
            dos.close();
            dis.close();
            out.close();
            in.close();
            echoSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("1 byte: " + average(oneAvg));
        System.out.println("64 bytes: " + average(sixtyFourAvg));
        System.out.println("1024 bytes: " + average(kbAvg));
    }

    private static long getTime(byte[] b, DataOutputStream dos, DataInputStream dis) {

        try {
            for (int i = 0; i < b.length; i++) {
                b[i] = (byte) i;
            }
            long timeOut;
            long timeElap;
            timeOut = System.nanoTime();
            dos.writeInt(b.length);
            dos.write(b);
            dis.readFully(b);
            timeElap = System.nanoTime() - timeOut;
            return timeElap;
        } catch (SocketTimeoutException ex) {
            System.err.println("Timeout on " + b.length + " byte size package");
            return 0;
        } catch (IOException e) {
            System.err.println("IO Failure");
            System.err.println(e);
            return 0;
        }
    }

    private static double average(ArrayList<Long> vals) {
        int l = vals.size();
        double t = 0;
        for (Long val : vals) {
            if(val != 0) {
                t += (val / 1000000);
            }
        }
        t /= l;
        return t;
    }
}