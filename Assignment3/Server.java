package Assignment3;

import java.net.*;
import java.nio.ByteBuffer;


class Server {

    public static void main(String args[]) throws Exception {

        String[] names = new String[100];
        InetAddress[] addresses = new InetAddress[100];
        int[] ports = new int[100];
        int count = 0;

        double currentBid = 0;
        String currentBuyer = "No current buyer";
        DatagramSocket serverSocket = new DatagramSocket(2711);
        serverSocket.setSoTimeout(1000);
        byte[] receiveData = new byte[2000];
        boolean newBid;

        for (; ; ) {
            long bidStart = System.currentTimeMillis();
            newBid = false;
            System.out.println("New bid");
            for (int i = 0; i < count; i++) { //When a new bid starts, send info to "reset" the clients for the new bid
                DatagramPacket sendPacket = new DatagramPacket(ByteBuffer.allocate(8).putDouble(currentBid).array(),
                        8, addresses[i], ports[i]);
                serverSocket.send(sendPacket);
                sendPacket = new DatagramPacket(currentBuyer.getBytes(), currentBuyer.length(), addresses[i],
                        ports[i]);
                serverSocket.send(sendPacket);
                long timeLeft = (60 - (System.currentTimeMillis() - bidStart) / 1000);
                sendPacket = new DatagramPacket(ByteBuffer.allocate(8).putLong(timeLeft).array(), 8,
                        addresses[i], ports[i]);
                serverSocket.send(sendPacket);
            }
            for (; ; ) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                for (int i = 0; i < 1; i++) {
                    if (((System.currentTimeMillis() - bidStart) / 1000) >= 60) { //Every minute, start a new bid
                        currentBid = 0;
                        currentBuyer = "No current buyer";
                        newBid = true;
                        break;
                    }
                    try {
                        serverSocket.receive(receivePacket);
                    } catch (SocketTimeoutException e) {
                        i--;
                        for (int j = 0; j < count; j++) { //Every second, send an update with all info to all clients
                            DatagramPacket sendPacket = new DatagramPacket(ByteBuffer.allocate(8).putDouble(currentBid)
                                    .array(), 8, addresses[j], ports[j]);
                            serverSocket.send(sendPacket);
                            sendPacket = new DatagramPacket(currentBuyer.getBytes(), currentBuyer.length(),
                                    addresses[j], ports[j]);
                            serverSocket.send(sendPacket);
                            long timeLeft = (60 - (System.currentTimeMillis() - bidStart) / 1000);
                            sendPacket = new DatagramPacket(ByteBuffer.allocate(8).putLong(timeLeft).array(), 8,
                                    addresses[j], ports[j]);
                            serverSocket.send(sendPacket);
                        }
                    }
                }
                if(newBid) {
                    break;
                }

                double bid = ByteBuffer.wrap(receivePacket.getData()).getDouble();

                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();
                if (bid == -1) {
                    bidStart = System.currentTimeMillis();
                    addresses[count] = IPAddress;
                    ports[count] = port + 1;
                    byte[] name = new byte[1000];
                    receivePacket = new DatagramPacket(name, name.length);
                    serverSocket.receive(receivePacket);
                    names[count] = new String(name).trim();
                    System.out.println("\nNew Bidder");
                    System.out.println("IPAddress: " + addresses[count]);
                    System.out.println("Port: " + ports[count]);
                    System.out.println("Name: " + names[count]);
                    //Send to the new client an update with all the info
                    DatagramPacket sendPacket = new DatagramPacket(ByteBuffer.allocate(8).putDouble(currentBid).array(),
                            8, IPAddress, (port + 1));
                    serverSocket.send(sendPacket);
                    sendPacket = new DatagramPacket(currentBuyer.getBytes(), currentBuyer.length(), IPAddress,
                            (port + 1));
                    serverSocket.send(sendPacket);
                    long timeLeft = (60 - (System.currentTimeMillis() - bidStart) / 1000);
                    System.out.println(timeLeft);
                    sendPacket = new DatagramPacket(ByteBuffer.allocate(8).putLong(timeLeft).array(),
                            8, IPAddress, (port + 1));
                    serverSocket.send(sendPacket);
                    count++;
                } else {
                    System.out.println("\nBid: " + bid);
                    if (bid > currentBid) {
                        currentBid = bid;
                        for (int j = 0; j < count; j++) {
                            if (addresses[j].equals(IPAddress) &&
                                    ports[j] == (port + 1)) {
                                System.out.println("Got match");
                                currentBuyer = names[j];
                                break;
                            }
                        }

                        for (int j = 0; j < count; j++) { //Send to each client an update with the new bid
                            IPAddress = addresses[j];
                            port = ports[j];
                            DatagramPacket sendPacket = new DatagramPacket(ByteBuffer.allocate(8).putDouble(currentBid)
                                    .array(), 8, IPAddress, port);
                            System.out.println("Sending");
                            serverSocket.send(sendPacket);
                            sendPacket = new DatagramPacket(currentBuyer.getBytes(), currentBuyer.length(), IPAddress,
                                    port);
                            serverSocket.send(sendPacket);
                            long timeLeft = (60 - (System.currentTimeMillis() - bidStart) / 1000);
                            sendPacket = new DatagramPacket(ByteBuffer.allocate(8).putLong(timeLeft).array(), 8,
                                    IPAddress, port);
                            serverSocket.send(sendPacket);
                        }
                    } else {
                        System.out.println((System.currentTimeMillis() - bidStart) / 1000);
                    }
                }
            }
        }
    }
}
