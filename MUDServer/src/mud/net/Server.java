package mud.net;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

import java.io.InputStream;
import java.net.*;
import java.util.*;

import mud.MUDServerI;
import mud.protocols.Telnet;
import mud.utils.Message;

public class Server implements Runnable {

    final private Vector<Client> clients = new Vector<Client>();
    final private MUDServerI parent;

    private ServerSocket server;
    private Thread thread;
    private boolean running;

    public Server(final MUDServerI p, final int port) {
        parent = p;
        try {
            server = new ServerSocket(port);
            thread = new Thread(this);
            thread.start();
            
            running = true;

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

	public void stopRunning() {
        running = false;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean hasClients() {
		return !clients.isEmpty();
	}
 	
 	public List<Client> getClients() {
		return new ArrayList<Client>(clients);
 	}
 	
 	public Client[] getClients_alt() {
 		return clients.toArray(new Client[0]);
 	}

    public void disconnect(final Client client) {
        client.stopRunning();
        clients.remove(client);
    }

    public void run() {
        try {
            running = true;
            
            while (running) {
                Socket socket = server.accept();             
                
                Client client = new Client(socket);
                
                /*if( client.usingTelnet() ) {
                	int c = 5;

                	while( c > 0 ) {
                		if( client.received_telnet_msgs.size() > 0 ) {
                			final List<Byte[]> msgs = client.received_telnet_msgs;

                			for(final Byte[] ba : msgs) {
                				byte[] ba2 = new byte[ba.length];

                				int index = 0;

                				for(Byte b : ba) {
                					ba2[index] = b;
                					index++;
                				}

                				String msg = Telnet.translate(ba2);

                				String[] msga = msg.split(" ");

                				System.out.println( "] Received: " + msg );

                				byte[] response = new byte[3];

                				// if asked WILL some unknown option, respond DONT
                				if( msga[1].equals("WILL") && msga[2].equals("null") ) {
                					response[0] = Telnet.IAC;
                					response[1] = Telnet.DONT;
                					response[2] = ba2[2];

                					client.write(response);
                					System.out.println( "] Sent: " + Telnet.translate(response) );
                				}

                				// if asked DO some unknown option, respond WONT
                				else if( msga[1].equals("DO") && msga[2].equals("null") ) {
                					response[0] = Telnet.IAC;
                					response[1] = Telnet.WONT;
                					response[2] = ba2[2];

                					client.write(response);
                					System.out.println( "] Sent: " + Telnet.translate(response) );
                				}
                			}
                		}
                		else {
                			System.out.println("No Telnet Messages");
                		}
                	}
                }*/

                clients.add(client);
                
                System.out.println("Accepted client socket.");
                
                parent.clientConnected(client);
            }
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            for (final Client c : clients) {
                c.stopRunning();
            }
        }
    }

    public void write(final String data) {
        writeToAllClients(data.getBytes());
    }

    public void write(final char data) {
        writeToAllClients(new byte[]{ (byte) data });
    }

    public void writeToAllClients(final byte data[]) {
        for (final Client c : new ArrayList<Client>(clients)) {
            if (c.isRunning()) {
                c.write(data);
            }
            else {
                clients.remove(c);
            }
        }
    }

    public void sendMessage(final Client aclient, final Message msg) {
        for (final Client client : clients) {
            if (client.equals(aclient)) {
                client.writeln(msg.getSender().getName() + " says, \"" + msg.getMessage() + "\" to you. (tell)");
            }
        }
    }
}