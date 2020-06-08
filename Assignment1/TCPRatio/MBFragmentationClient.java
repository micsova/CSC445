package Assignment1.TCPRatio;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class MBFragmentationClient {
    static final int KB = 1024;
    static final int MB = KB * 1024;
    static final String HOST = "129.3.20.62";
    static final int PORTNUM = 12711;
    public static void main(String[] args) {
        ArrayList<Long> fourAvg = new ArrayList<>();
        ArrayList<Long> twoAvg = new ArrayList<>();
        ArrayList<Long> oneAvg = new ArrayList<>();
        try {
            Socket echoSocket = new Socket(HOST, PORTNUM);
            OutputStream out = echoSocket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(out);
            InputStream in = echoSocket.getInputStream();
            DataInputStream dis = new DataInputStream(in);
            for (int i = 0; i < 50; i++) {
                byte b[] = new byte[4 * KB];
                fourAvg.add(getTime(b, dos, dis));
                b = new byte[2 * KB];
                twoAvg.add(getTime(b, dos, dis));
                b = new byte[1 * KB];
                oneAvg.add(getTime(b, dos, dis));
                System.out.println((i + 1) + "/50");
            }
            dos.close();
            in.close();
            out.close();
            echoSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("4KB packages: " + average(fourAvg));
        System.out.println("2KB packages: " + average(twoAvg));
        System.out.println("1KB packages: " + average(oneAvg));
    }

    private static Long getTime(byte[] b, DataOutputStream dos, DataInputStream in) {

        try {
            for (int i = 0; i < b.length; i++) {
                b[i] = (byte) i;
            }
            long timeOut;
            long timeElap;
            timeOut = System.nanoTime();
            //Make sure the total data sent is 1 MB
            int numMessages = MB / b.length;
            dos.writeInt(numMessages);
            for(int i = 0; i < numMessages; i++) {
                dos.writeInt(b.length);
                dos.write(b);
            }
            in.readChar();
            timeElap = System.nanoTime() - timeOut;
            return timeElap;
        } catch (IOException ex) {
            System.err.println("IO failure on " + (b.length / KB) + " byte package.");
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