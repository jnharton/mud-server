package mud.net;

import java.io.*;
import java.net.*;
import java.util.*;

import org.junit.*;

import static org.junit.Assert.*;

public class ClientTest {

    private final static String SEND_TO_CLIENT = "hello there";
    private final static String SEND_FROM_CLIENT = "sending data from client to test";
    private final static int LIST_SIZE = 1024;

    @Test
    public void testClient() throws Exception {

        // make random strings to send to and from client
        final List<String> toSendToClient = new ArrayList<String>(LIST_SIZE);
        final List<String> toSendBackFromClient = new ArrayList<String>(LIST_SIZE);
        for (int i = 0; i < LIST_SIZE; i++) {
            toSendToClient.add(UUID.randomUUID().toString().replace("(\r|\n)+", ""));
            toSendBackFromClient.add(UUID.randomUUID().toString().replace("(\r|\n)+", ""));
        }

        // set up sockets
        final ServerSocket serverSock = new ServerSocket(7777);
        final Socket socket = new Socket("localhost", serverSock.getLocalPort());
        final Client client = new Client(serverSock.accept());
        System.out.println("Client connected.");

        // make daemon threads to read data back from client and from test as soon as available
        final List<String> sentToClient = new ArrayList<String>(LIST_SIZE);
        final Thread sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    final String line = client.getInput();
                    if (line != null) {
                        sentToClient.add(line);
                    }
                }
            }
        });
        sendThread.setDaemon(true);
        sendThread.start();

        final List<String> sentBackFromClient = new ArrayList<String>(LIST_SIZE);
        final Thread sendBackThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    while (true) {
                        sentBackFromClient.add(br.readLine());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        });
        sendBackThread.setDaemon(true);
        sendBackThread.start();

        System.out.println("Daemon threads started.");

        // thread to send data to client
        final Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final OutputStream myOut = socket.getOutputStream();
                    for (final String s : toSendToClient) {
                        myOut.write((s + "\r\n").getBytes());
                        myOut.flush();
                        
                        // test hangs unless i call println or print with "a", ".", " ", "\r", "\n", or "\r\n" here. :|
                        // print("") does not prevent it from hanging
                        // at least \r doesn't make the console text scroll by
                        // java version "1.7.0_07"
                        // Java(TM) SE Runtime Environment (build 1.7.0_07-b10)
                        // Java HotSpot(TM) 64-Bit Server VM (build 23.3-b01, mixed mode)
                        System.out.print("\r");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        });

        // thread to send data back from client
        final Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (final String s : toSendBackFromClient) {
                    client.writeln(s);
                }
            }
        });

        System.out.println("Starting writer threads.");

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("Writer threads joined.");

        Thread.sleep(300); // give reader threads a chance to receive all data

        for (int i = 0; i < LIST_SIZE; i++) {
            assertEquals(toSendToClient.get(i), sentToClient.get(i));
        }
        for (int i = 0; i < LIST_SIZE; i++) {
            assertEquals(toSendBackFromClient.get(i), sentBackFromClient.get(i));
        }
    }

}
