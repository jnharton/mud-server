package mud.net;

import java.io.*;
import java.net.*;
import java.util.*;

import mud.net.Client;
import mud.net.Server;
import mud.MUDServerI;

import org.junit.*;

import static org.junit.Assert.*;

public class ServerTest {

    final static private String SERVER_OUTPUT = "hello there";

    @Test
    public void testServer() throws Exception {
    
        final int[] connections = new int[]{ 0 };

        final MUDServerI mudServer = new MUDServerI() {
            public void clientConnected(final Client someClient) {
                connections[0] += 1;
            }
        };
        final Server server = new Server(mudServer, 7777);
        assertEquals(0, server.getClients().size());
        final Socket socket = new Socket("localhost", 7777);
        sleep(100);
        assertEquals(1, server.getClients().size());
        assertEquals(1, connections[0]);

        server.writeToAllClients((SERVER_OUTPUT + "\r\n").getBytes());
        final BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        assertEquals(SERVER_OUTPUT, br.readLine());

        final Client onlyClient = server.getClients().get(0);
        server.disconnect(onlyClient);
        sleep(100);
        assertEquals(0, server.getClients().size());
        assertEquals(false, onlyClient.isRunning());
    }
    
    private void sleep(final int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
