package mud.protocols;

import java.util.HashMap;

import mud.net.Client;
import mud.utils.Utils;

/**
 * stuff to help implement telnet specification (see IETF RFC 854)
 * 
 * NOTE: for custom options,
 * MCCP  == MCCP1     == COMPRESS
 * MCCP2 == COMPRESS2
 * 
 * @author Jeremy
 */
public class Telnet  {
	
	// Telnet
	// Declaration                              // NAME              CODE MEANING
	public static final byte SE   = (byte) 240; // SE                240  End of subnegotiation parameters.
	public static final byte NOP  = (byte) 241; // NOP               241  No operation.
	public static final byte DM   = (byte) 242; // Data Mark         242
	
	public static final byte BRK  = (byte) 243; // Break             243  NVT character BRK.
	public static final byte IP   = (byte) 244; // Interrupt Process 244  The function IP.
	public static final byte AO   = (byte) 245; // Abort output      245  The function AO.
	public static final byte AYT  = (byte) 246; // Are You There     246  The function AYT.
	public static final byte EC   = (byte) 247; // Erase Character   247  The function EC.
	public static final byte EL   = (byte) 248; // Erase Line        248  The function EL.
	public static final byte GA   = (byte) 249; // Go Ahead          249  The GA signal.
	public static final byte SB   = (byte) 250; // SB                250
	
	public static final byte WILL = (byte) 251; // server/client will do X
	public static final byte WONT = (byte) 252; // server/client won't do X 
	public static final byte DO   = (byte) 253; // server/client do X (imperative)
	public static final byte DONT = (byte) 254; // server/client don't do X (imperative)
	public static final byte IAC  = (byte) 255; // Is A Command (Indicates a command)
	
	//
	public static final byte TERMINAL_TYPE = (byte) 24;
	
	// custom options for other protocols (general)
	public static final byte LINEMODE = (byte) 34;
	
	// custom options for other protocols (MUD specific)
	public static final byte MCCP = (byte) 85;   // MUD Client Compression Protocol 1 (zlib compressed stream)
	public static final byte MCCP1 = MCCP;       // MCCP alias
	public static final byte COMPRESS = MCCP;    // MCCP alias
	public static final byte MCCP2 = (byte) 86;  // MUD Client Compression Protocol 2 (zlib compressed stream)
	public static final byte COMPRESS2 = MCCP2;  // MCCP2 alias
	public static final byte MSP = (byte) 90;    // MUD Sound Protocol
	public static final byte MXP = (byte) 91;    // MUD eXtension Protocol
	public static final byte ZMP = (byte) 93;    // Zenith MUD Protocol
	public static final byte c102 = (byte) 102;  // Aardwolf Telnet Client Protocol
	public static final byte ATCP = (byte) 200;  // Achaea Telnet Client Protocol
	public static final byte ATCP2 = (byte) 201; // Generic Mud Communication Protocol (also known as ATCP2)
	public static final byte GCMP = (byte) 201;  // Generic Mud Communication Protocol (also known as ATCP2) 
	
	public static final HashMap<String, Byte> map = new HashMap<String, Byte>(1, 0.75f) {
		{
			put("SE", SE);       put("NOP", NOP);             put("DM", DM);     put("BRK", BRK);
			put("IP", IP);       put("AO", AO);               put("AYT", AYT);   put("EC", EC);
			put("EL", EL);       put("GA", GA);               put("SB", SB);     put("WILL", WILL);
			put("WONT", WONT);   put("DO", DO);               put("DONT", DONT); put("IAC", IAC);
			
			put("TERMINAL-TYPE", TERMINAL_TYPE);
			put("LINEMODE", LINEMODE);
			
			put("MCCP", MCCP);   put("MCCP1", MCCP1);         put("COMPRESS", COMPRESS);
			put("MCCP2", MCCP2); put("COMPRESS2", COMPRESS2);
			put("MSP", MSP);
			put("MXP", MXP);
		}
	};
	
	public static final HashMap<Byte, String> map1 = new HashMap<Byte, String>(1, 0.75f) {
		{
			put(SE, "SE");       put(NOP, "NOP");             put(DM, "DM");     put(BRK, "BRK");
			put(IP, "IP");       put(AO, "AO");               put(AYT, "AYT");   put(EC, "EC");
			put(EL, "EL");       put(GA, "GA");               put(SB, "SB");     put(WILL, "WILL");
			put(WONT, "WONT");   put(DO, "DO");               put(DONT, "DONT"); put(IAC, "IAC");
			
			put(TERMINAL_TYPE, "TERMINAL-TYPE");
			put(LINEMODE, "LINEMODE");
			
			put(MCCP, "MCCP");   put(MCCP1, "MCCP1");         put(COMPRESS, "COMPRESS");
			put(MCCP2, "MCCP2"); put(COMPRESS2, "COMPRESS2");
			put(MXP, "MXP");
		}
	};
	
	static public void send(String input, Client client) {
		byte[] ba = translate(input);
		client.write(ba);
	}
	
	/**
	 * telnetPhraseToByteArray?
	 * 
	 * Translate a string of keywords that are the equivalents of a
	 * byte into a byte array
	 * 
	 * @param input - a telnet "phrase" consisting of keywords that map to bytes
	 * @return an array of bytes
	 */
	static public byte[] translate(String input) {
		String[] work = input.split(" "); // split the phrase into it's constituent parts
		
		byte[] ba = new byte[work.length]; // make a new byte array that can hold the phrase's byte equivalents
		
		System.out.println("Translate from String to Byte (sending)");
		
		for (int s = 0; s < work.length; s++) {
			System.out.println(work[s] + " " + map.get(work[s]));
			ba[s] = map.get(work[s]);
		}
		
		return ba;
	}
	
	/**
	 * Translate a byte array into a string of keywords equivalent to their
	 * respective bytes.
	 * 
	 * @param input - a byte array containing keywords
	 * @return a string containing string representations of those keywords
	 */
	static public String translate(byte[] input) {
		String[] output = new String[input.length]; // make a new string array to store the parts of the phrase in
		
		System.out.println("Translate from Byte to String (receiving)");
		
		for (int b = 0; b < input.length; b++) {
			System.out.println(input[b] + " " + map1.get(input[b]));
			output[b] = map1.get(input[b]);
		}
		
		return Utils.join(output, " ");
	}
}