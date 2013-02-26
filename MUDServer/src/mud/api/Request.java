package mud.api;
import mud.api.APIKey;
import mud.api.RequestType;
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

public class Request {
	private RequestType requestType;
	private APIKey apiKey;
	private String param;

	private Client client;

	boolean processed = false;
	String response = "";

	public Request(String[] requestData, Client tClient) {
		requestType = RequestType.getType(requestData[0]);
		apiKey = new APIKey(requestData[1]);
		param = requestData[2];
		client = tClient;
	}
	
	public RequestType getType() {
		return this.requestType;
	}
	
	public APIKey getAPIKey() {
		return this.apiKey;
	}
	
	public String getParam() {
		return this.param;
	}
	
	public Client getClient() {
		return this.client;
	}
}