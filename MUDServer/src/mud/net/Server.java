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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import mud.interfaces.MUDServerI;

public class Server implements Runnable {
	final private MUDServerI parent;

	private ServerSocket server;
	private Integer port;

	final private Vector<Client> clients;
	final public Map<String, Thread> threads;

	private boolean running;

	public Server(final MUDServerI p, final int port) {
		this.parent = p;
		this.port = port;

		this.clients = new Vector<Client>();
		this.threads = new Hashtable<String, Thread>();

		this.running = false;
	}

	public boolean init() {
		boolean success = true;

		try {
			this.server = new ServerSocket(port);
		}
		catch (final IOException ioe) {
			success = false;

			System.out.println("could not open server socket");
			System.out.println("--- Stack Trace ---");
			ioe.printStackTrace();
		}

		return success;
	}

	public void run() {
		try {
			if( init() ) {
				this.running = true;
			}

			Socket socket = null;

			while (this.running) {
				socket = this.server.accept();

				System.out.println("Accepted client socket.");

				final Client client = new Client(socket);
				
				this.clients.add(client);
				
				//new Thread(client).start();
				startThread(client, "client" + clients.size() + "_" + socket.getInetAddress().getHostAddress());

				parent.clientConnected(client);
			}
		}
		catch (final SocketException se) {
			System.out.println("--- Stack Trace ---");
			se.printStackTrace();
		}
		catch (final IOException ioe) {
			System.out.println("--- Stack Trace ---");
			ioe.printStackTrace();
		}
		finally {
			this.running = false;

			for (final Client c : clients) c.stopRunning();
		}
	}

	public void stopRunning() {
		this.running = false;
	}

	public boolean isRunning() {
		return this.running;
	}

	public List<Client> getClients() {
		return new ArrayList<Client>(this.clients);
	}

	public void disconnect(final Client client) {
		client.stopRunning();
		this.clients.remove(client);
		//this.threads.remove
	}

	public void write(final String data) {
		writeToAllClients(data.getBytes());
	}

	public void write(final char data) {
		writeToAllClients(new byte[]{ (byte) data });
	}

	public void writeToAllClients(final byte data[]) {
		final List<Client> dead = new LinkedList<Client>();

		// used to be a list of clients
		for (final Client c : this.clients) {
			if (c.isRunning())  c.write(data);
			else                dead.add(c);
		}

		this.clients.removeAll( dead );
	}
	
	private final void startThread(final Runnable r, final String threadName) {
		Thread thread = new Thread(r, threadName);
		
		thread.start();
		
		this.threads.put(thread.getName(), thread);
	}
}