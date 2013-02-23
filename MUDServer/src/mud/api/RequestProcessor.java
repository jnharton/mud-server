package mud.api;

import java.util.LinkedList;

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

public class RequestProcessor implements Runnable {
	
	private boolean running;
	
	APIServer as;
	
	protected RequestProcessor(APIServer parent) {
		as = parent;
	}
	
	@Override
	public void run() {
		running = true;
		
		while( running ) {
			Request request = as.requests.poll();

			if(request != null) {
				System.out.println("Processing Request...");
				processRequest(request);
				System.out.println("Done");
				
				as.processed.add(request);
				System.out.println("Added processed request to processed queue!");
			}
		}
	}
	
	private void processRequest(Request request) {			
		if( as.validate(request.apiKey) ) {
			System.out.println("API Key is valid!");
			request.response = "APIServer> Test Phase -- No Data";
		}
		else {
			System.out.println("API Key is invalid!");
			request.response = "APIServer> Invalid API Key!";
		}
		
		request.processed = true;
	}
}