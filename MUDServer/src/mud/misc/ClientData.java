package mud.misc;

import java.util.BitSet;

public class ClientData {
	final BitSet protocol_status = new BitSet(8);
	
	private boolean telnet = true;
	
	public ClientData() {		
	}
}