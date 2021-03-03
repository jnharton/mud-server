package mud.utils;

import java.util.BitSet;
import java.util.Hashtable;
import java.util.Map;

public class ClientData {
	final BitSet protocol_status = new BitSet(8);
	private boolean telnet = true;
	
	public Map<String, Object> data;
	
	public String state = "";
	
	public Map<String, String> challenge_response;
	
	public ClientData() {
		this.data = new Hashtable<String, Object>();
	}
}