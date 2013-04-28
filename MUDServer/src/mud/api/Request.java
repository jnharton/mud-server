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

import mud.api.APIKey;
import mud.api.RequestType;
import mud.net.Client;

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