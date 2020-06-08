/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Assignment3;

/**
 * @author Chairman
 */


import java.io.IOException;
import java.util.*;
import java.net.*;
import java.nio.ByteBuffer;

public class ClientText extends Client {

    double newBid;
    String buyerName = "No current buyer";
    String name;

    private class MyTask implements Runnable {

        public void run() {
            try {
                DatagramSocket clientSocket = new DatagramSocket(port + 1);
                for(;;) {
                    byte[] bidPacket = new byte[8];
                    byte[] namePacket = new byte[1000];
                    byte[] timePacket = new byte[8];
                    DatagramPacket receivePacket = new DatagramPacket(bidPacket, bidPacket.length);
                    for (int i = 0; i < 1; i++) {
                        try {
                            clientSocket.receive(receivePacket);
                        } catch (SocketTimeoutException e) {
                            i--;
                        }
                    }
                    newBid = ByteBuffer.wrap(bidPacket).getDouble();

                    //Uncomment this to add receiving of buyer address and time left in auction
                    //CLIENT TEXT SHOULDN'T DO ANYTHING WITH TIME OTHER THAN RECEIVE TO CLEAR THE PACKET

                    receivePacket = new DatagramPacket(namePacket, namePacket.length);
                    clientSocket.receive(receivePacket);
                    buyerName = new String(namePacket);
                    buyerName = buyerName.trim();
                    receivePacket = new DatagramPacket(timePacket, timePacket.length);
                    clientSocket.receive(receivePacket);
                    if (newBid != bid) {
                        bid = newBid;
                        System.out.println("\n\n***New Max Bid***\n");
                        System.out.print("\rMax bid: $" + df.format(bid) + "\tBuyer address: " + buyerName);
                        System.out.print("\tBid: ");
                    }
                }
            } catch (IOException e) {}
        }
    }

    public void main() {
        Scanner kb = new Scanner(System.in);
        System.out.print("Please enter your name: ");
        name = kb.nextLine();
        for (; ; ) {
            try {
                if (socket == null) {
                    findGoodPort();
                    socket = new DatagramSocket(port);
                    address = InetAddress.getByName("localhost");
                    Thread t = new Thread(new MyTask());
                    t.start();
                    contactServer(name);
                }
                bid(kb.nextLine());
                System.out.print("\nMax bid: $" + df.format(bid) + "\tBuyer address: " + buyerName);
                System.out.print("\tBid: $");
            } catch (IOException e) {}
        }
    }

    public void contactServer(String name) {
        bid("-1");
        byte[] n = name.getBytes();
        DatagramPacket packet = new DatagramPacket(n, n.length, address, SERVER_PORT);
        try {
            socket.send(packet);
        } catch (IOException e) {}
        System.out.print("\nMax bid: $" + df.format(bid) + "\tBuyer address: " + buyerName);
        System.out.print("\tBid: $");
    }

    public void bid(String input) {
        try {
            double bidAmount;
            String[] parts = input.split("\\.");
            if (parts.length > 1) {
                if (parts[1].length() > 2) {
                    System.out.print("\n***Amount can only have up to two decimal places.***\n");
                    return;
                }
            }
            try {
                bidAmount = Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.print("\n***Invalid amount.***\n");
                return;
            }

            //long startTime = System.nanoTime();
            DatagramPacket sendPacket = new DatagramPacket(ByteBuffer.allocate(8).putDouble(bidAmount).array(),
                    8, address, SERVER_PORT);
            socket.send(sendPacket);
            if(!input.equals("-1")) {
                System.out.println("\n***" + name + " sent $" + input + " as bid***");
            }
        } catch (IOException e) {}
    }
}
