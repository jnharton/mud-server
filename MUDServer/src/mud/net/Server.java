package mud.net;

import java.io.IOException;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

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
        this.parent = p;
        
        try {
        	this.server = new ServerSocket(port);
        	this.thread = new Thread(this);
            
        	this.thread.start();
            
        	this.running = true;
        }
        catch (final IOException ioe) {
        	System.out.println("could not open server socket");
        	System.out.println("--- Stack Trace ---");
            ioe.printStackTrace();
            
            // TODO is a full exit appropriate here
            System.exit(-1);
        }
    }

	public void stopRunning() {
		this.running = false;
	}

	public boolean isRunning() {
		return this.running;
	}

	public boolean hasClients() {
		return !this.clients.isEmpty();
	}
 	
 	public List<Client> getClients() {
		return new ArrayList<Client>(this.clients);
 	}

    public void disconnect(final Client client) {
        client.stopRunning();
        this.clients.remove(client);
    }

    public void run() {
        try {
        	this.running = true;
            
            while (this.running) {
                Socket socket = this.server.accept();             
                
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
                
                this.clients.add(client);
                
                System.out.println("Accepted client socket.");
                
                parent.clientConnected(client);
            }
        }
        catch (final Exception e) {
        	System.out.println("--- Stack Trace ---");
            e.printStackTrace();
        }
        finally {
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
            if (c.isRunning())  c.write(data);
            else                clients.remove(c); // TODO is this a concurrent modification problem?
        }
    }
    
    /*
     * TODO fix/remove this kludge which was sort of intended for a say command anyway
     */
    public void sendMessage(final Message msg, final Client client) {
    	final String sender = msg.getSender().getName();
    	final String message = msg.getMessage();
    	
    	client.writeln(sender + " says, \"" + message + "\" to you. (tell)");
    }
}