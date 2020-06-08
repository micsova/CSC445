package Assignment1.TransferRates;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class SpeedsClient {
    static final int KB = 1024;
    static final String HOST = "129.3.20.62";
    static final int PORTNUM = 2711;

    public static void main(String[] args) {
        ArrayList<Long> oneAvgTo = new ArrayList<>();
        ArrayList<Long> sixteenAvgTo = new ArrayList<>();
        ArrayList<Long> sixtyFourAvgTo = new ArrayList<>();
        ArrayList<Long> twoFiftySixAvgTo = new ArrayList<>();
        ArrayList<Long> mbAvgTo = new ArrayList<>();
        ArrayList<Long> oneAvgFrom = new ArrayList<>();
        ArrayList<Long> sixteenAvgFrom = new ArrayList<>();
        ArrayList<Long> sixtyFourAvgFrom = new ArrayList<>();
        ArrayList<Long> twoFiftySixAvgFrom = new ArrayList<>();
        ArrayList<Long> mbAvgFrom = new ArrayList<>();
        long times[];

        try {
            Socket echoSocket = new Socket(HOST, PORTNUM);
            OutputStream out = echoSocket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(out);
            InputStream in = echoSocket.getInputStream();
            DataInputStream dis = new DataInputStream(in);

            for (int i = 0; i < 50; i++) {
                byte b[] = new byte[1 * KB];
                times = getTime(b, dos, dis);
                oneAvgTo.add(times[0]);
                oneAvgFrom.add(times[1]);
                b = new byte[16 * KB];
                times = getTime(b, dos, dis);
                sixteenAvgTo.add(times[0]);
                sixteenAvgFrom.add(times[1]);
                b = new byte[64 * KB];
                times = getTime(b, dos, dis);
                sixtyFourAvgTo.add(times[0]);
                sixtyFourAvgFrom.add(times[1]);
                b = new byte[256 * KB];
                times = getTime(b, dos, dis);
                twoFiftySixAvgTo.add(times[0]);
                twoFiftySixAvgFrom.add(times[1]);
                b = new byte[1024 * KB];
                times = getTime(b, dos, dis);
                mbAvgTo.add(times[0]);
                mbAvgFrom.add(times[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Print out upload/download speeds in MB/s
        System.out.println("1KB Upload: " + convertToMbps(1, averageMs(oneAvgTo)) + " MB/s\t1KB Download: " +
                convertToMbps(1, averageMs(oneAvgFrom)) + " MB/s");
        System.out.println("16KB Upload: " + convertToMbps(16, averageMs(sixteenAvgTo)) + " MB/s\t16KB Download: " +
                convertToMbps(16, averageMs(sixteenAvgFrom)) + " MB/s");
        System.out.println("64KB Upload: " + convertToMbps(64, averageMs(sixtyFourAvgTo)) +
                " MB/s\t64KB Download: " + convertToMbps(64, averageMs(sixtyFourAvgFrom)) + " MB/s");
        System.out.println("256KB Upload: " + convertToMbps(256, averageMs(twoFiftySixAvgTo)) +
                " MB/s\t256KB Download: " + convertToMbps(256, averageMs(twoFiftySixAvgFrom)) + " MB/s");
        System.out.println("1MB Upload: " + convertToMbps(1024, averageMs(mbAvgTo)) + " MB/s\t1MB Download: " +
                convertToMbps(1024, averageMs(mbAvgFrom)) + " MB/s");
    }

    private static long[] getTime(byte[] b, DataOutputStream dos, DataInputStream dis) {
        try {
            for (int i = 0; i < b.length; i++) {
                b[i] = (byte) i;
            }
            long uploadTime;
            long downloadTime;
            dos.writeInt(b.length);
            dos.write(b);
            long timeStart = System.nanoTime();
            uploadTime = dis.readLong();
            dis.readFully(b);
            downloadTime = System.nanoTime() - timeStart;
            System.out.println((b.length / 1024) + "KB --- " + "UT: " + uploadTime + " DT: " + downloadTime);
            return new long[]{uploadTime, downloadTime};
        } catch (IOException ex) {
            System.err.println("IO failure.");
            ex.printStackTrace();
            return new long[]{0, 0};
        }
    }

    private static double averageMs(ArrayList<Long> vals) {
        int l = vals.size();
        double t = 0;
        for (Long val : vals) {
            if(val != 0) {
                t += (val / 1000000);
            }
        }
        t /= l;
        System.out.println("t: " + t + " l: " + l);
        return t;
    }

    private static double convertToMbps(int kbs, double time) {
        double kbpms = kbs / time;
        double kbps = kbpms * 1000;
        double mbps = kbps / 1024;
        return mbps;
    }
}