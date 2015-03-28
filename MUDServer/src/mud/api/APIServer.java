package mud.api;

/*
 * Copyright (c) 2013 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import mud.net.Client;

public class APIServer implements Runnable {
	
	private ServerSocket serverSocket;
    private Thread thread;
    private boolean running;
    
    final private Vector<Client> clients = new Vector<Client>();
	
	final private MUDServerAPI parent;
	final private RequestProcessor rp;
	
	final public LinkedList<Request> requests = new LinkedList<Request>();
	final public LinkedList<Request> processed = new LinkedList<Request>();
	
	final private List<APIKey> apiKeys = new ArrayList<APIKey>();

	public APIServer(MUDServerAPI p, int port) {
		parent = p;
		rp = new RequestProcessor(this, parent);
		
		running = true;
		
		try {
			serverSocket = new ServerSocket(port);
			//thread = new Thread(this);
			//thread.start();
		}
		catch (IOException e) {
		    System.out.println("Could not listen on port: " + port);
		    System.exit(-1);
		}
		
		apiKeys.add( new APIKey("978419ff") );
		
		new Thread(rp).start();
	}
	
	@Override
	public void run() {
		
		while( running ) {
			if( !processed.isEmpty() ) { // send response if there are any
				Request request = processed.poll();
				System.out.println(request.response);
			}
			
			Socket clientSocket = null;
			
	        try {
	            clientSocket = serverSocket.accept();
	            
	            clients.add( new Client(clientSocket) );
	        }
	        catch (IOException e) {
	            System.err.println("Accept failed.");
	            System.exit(1);
	        }
		}
		
		rp.stop();
		System.exit(0);
	}
	
	public boolean validate(APIKey tKey) {
		return tKey.isValid() && apiKeys.contains(tKey); // neither of these currently returns true when expected
	}
	
	public static void main(String[] args) {		
		APIServer as = new APIServer(null, 4240);
		
		System.out.println(as.apiKeys);
		
		new Thread(as).start();
		
		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
		
		System.out.print("> ");
		
		try {
			while( br.ready() ) {
				String string = br.readLine();
				as.requests.add( new Request(new String[] { "request-data", "978419ff", string } , null) );
				System.out.print("> ");
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public void stop() {
		this.running = false;
	}
}