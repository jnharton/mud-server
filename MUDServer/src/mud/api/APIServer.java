package mud.api;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import mud.MUDServer;
import mud.net.Client;

/*
Copyright (c) 2012 Jeremy N. Harton

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

public class APIServer implements Runnable {
	
	private ServerSocket serverSocket;
    private Thread thread;
    private boolean running;
    
    final private Vector<Client> clients = new Vector<Client>();
	
	final private MUDServer parent;
	final private RequestProcessor rp;
	
	final public LinkedList<Request> requests = new LinkedList<Request>();
	final public LinkedList<Request> processed = new LinkedList<Request>();
	
	final private List<APIKey> apiKeys = new ArrayList<APIKey>();

	public APIServer(MUDServer p, int port) {
		parent = p;
		rp = new RequestProcessor(this, parent);
		
		try {
			serverSocket = new ServerSocket(port);
		}
		catch (IOException e) {
		    System.out.println("Could not listen on port: " + port);
		    System.exit(-1);
		}
		
		apiKeys.add( new APIKey("978419ff") );
		new Thread(rp).start();
		
		running = true;
	}
	
	@Override
	public void run() {
		while( running ) {
			if( !processed.isEmpty() ) { // send response if there are any
				Request request = processed.poll();
				//request.getClient().write(request.response);
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
	}
	
	public boolean validate(APIKey tKey) {
		//return tKey.isValid() && apiKeys.contains(tKey); // neither of these currently returns true when expected
		return true;
	}
	
	public static void main(String[] args) {
		//MUDServer ms = new MUDServer("localhost", 4201);
		//ms.init();
		
		APIServer as = new APIServer(null, 4240);
		
		System.out.println(as.apiKeys);
		
		as.requests.add( new Request(new String[] { "request-data", "978419ff", "test" } , null) );
		
		new Thread(as).start();
	}
}