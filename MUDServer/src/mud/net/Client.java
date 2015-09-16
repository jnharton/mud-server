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

import java.io.*;
import java.net.*;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import mud.protocols.Telnet;
import mud.utils.Utils;

public class Client implements Runnable {
	public static final int CHAR_MODE = 0;
	public static final int LINE_MODE = 1;
	public static final int TELNET_COMMAND_LENGTH = 3;
	public static final int BUF_SIZE = 4096;
	
	private final static int MCCP1 = 0;
	private final static int MCCP2 = 1;
	private final static int MSP = 2;
	private final static int MXP = 3;
	
	private final Socket socket;
	private final InputStream input;
	private final OutputStream output;
	
	private boolean running;

	private boolean telnet = true;
	private boolean tn_neg_seq = false; // indicates if the bytes currently being recieved are part of a negotiation sequence

	private boolean console = false; // indicates to the server that the client is using the admin console (default: false)
	
	private boolean response_expected = false;
	
	final BitSet protocol_status = new BitSet(8);
	
	private int input_mode = CHAR_MODE;
	
	// temporary storage
	private final StringBuffer sb = new StringBuffer(80);
	private List<Byte> buffer = new LinkedList<Byte>();
	
	// received data
	public final List<Byte[]> received_telnet_msgs = new LinkedList<Byte[]>();
	private final ConcurrentLinkedQueue<String> queuedLines = new ConcurrentLinkedQueue<String>();
	private String response = "";

	public Client(final String host, final int port) throws IOException {
		this(new Socket(host, port));
	}

	public Client(final Socket socket) throws IOException {
		this.socket = socket;

		this.input = socket.getInputStream();
		this.output = socket.getOutputStream();

		this.running = true;

		new Thread(this).start();
	}

	public void run() {
		// TODO refine code
		int readValue;
		int bytes;
		int last_ch = ' ';

		boolean received_line = false;

		try {
			while( running ) {
				readValue = 0;
				bytes = 0;

				while( input.available() > 0 ) {

					// read in a value
					readValue = input.read();

					// if we are TELNET NEGOTIATON SEQUENCE
					if( tn_neg_seq ) {
						if( bytes < TELNET_COMMAND_LENGTH ) {
							buffer.add( (byte) readValue );
							bytes++;

							System.out.println("Read: " + readValue);
						}

						if( bytes == TELNET_COMMAND_LENGTH ) {
							/*  response section -- properly belongs in the server end
							 * 
							 *  would be wise probably just to send a nice, fixed size array to the server and let it deal with the bytes
							 */

							Byte[] ba = new Byte[buffer.size()];

							buffer.toArray(ba);
							
							received_telnet_msgs.add( ba );
							
							//ba = null;
							
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

								write(response);
								System.out.println( "] Sent: " + Telnet.translate(response) );
							}

							// if asked DO some unknown option, respond WONT
							else if( msga[1].equals("DO") && msga[2].equals("null") ) {
								response[0] = Telnet.IAC;
								response[1] = Telnet.WONT;
								response[2] = ba2[2];

								write(response);
								System.out.println( "] Sent: " + Telnet.translate(response) );
							}

							/* end response section */

							buffer.clear();
							bytes = 0;
							tn_neg_seq = false;
						}
					}
					else {
						// if we see a TELNET COMMAND BYTE (take notice and enter TELNET NEGOTIATION SEQUENCE)
						if( (byte) readValue == Telnet.IAC ) {
							buffer.clear();

							System.out.println("TELNET Command");
							System.out.println("Read: " + readValue);

							buffer.add( (byte) readValue );
							bytes++;

							tn_neg_seq = true;

							continue;
						}

						final Character ch = (char) readValue;

						// LF+CR, LFCR, CR+LF, CRLF
						// the above combinations are detected and the cr after the nl or the nl after the cr
						// are ignored because it introduces an extra line into the command

						if (ch == '\012') { // newline (\n)
							if( last_ch == '\015') sb.delete(0, sb.length()); // clear stringbuffer
							else                   received_line = true;
						}
						else if(ch == '\015') { // carriage-return (\r)
							if( last_ch == '\012') sb.delete(0, sb.length()); // clear stringbuffer
							else                   received_line = true;
						}
						else if (ch == '\010') { // backspace
							if( !(sb.length() == 0) ) {
								sb.deleteCharAt( sb.length() - 1 );
							}
						}
						else { // any other character
							System.out.println("Read: " + ch + "(" + readValue + ")");
							sb.append(ch);
						}

						last_ch = ch;

						if( received_line ) {
							if( !response_expected ) {
								this.queuedLines.add( sb.toString().trim() ); // convert stringbuffer to string
							}
							else {
								this.response = sb.toString().trim();
								//System.out.println("RESPONSE: " + response);
							}

							sb.delete(0, sb.length());                    // clear stringbuffer
							received_line = false;
						}

						System.out.println("current telnet input: " + Utils.stringToArrayList( sb.toString(), "" )); // tell us the whole string
					}

					// ---------------------------------------------------------------------------------

					/*

					// can't use BufferedReader here - we need access to the buffer for telnet commands, 
					// which won't be followed by a newline
					final byte buf[] = new byte[BUF_SIZE];
					int numRead = 0;

					while (running) {
						final int lastNumRead = input.read(buf, numRead, BUF_SIZE - numRead);
						numRead += lastNumRead;

						// if we read more than one character total and we found a carriage-return ('\r)
						// or a newline (\n) character, turn the input into a string and dump it into
						// queued lines
						if (numRead > 1 && (buf[numRead - 1] == '\r' || buf[numRead - 1] == '\n')) {
							final String input = new String(buf, 0, numRead); // convert read characters into string

							numRead = 0; // clear count of read characters

							// handles possibility of multiple lines in input (mud client, probably)
							for (final String line : input.split("(\r\n|\n|\r)")) {
								if (line != null && !"".equals(line)) {
									queuedLines.add(line);
								}
							}
						}
					}

					 */
				}
			}
		}
		catch (SocketException se) {
			// if we catch a SocketException, we have likely stopped the client intentionally,
			// in which case no warning is really necessary, otherwise we want to print a stack
			// trace and stop the client.
			if( running ) {
				se.printStackTrace();
				stopRunning();
			}
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
			stopRunning();
		}
		finally {
			try { socket.close(); }
			catch (IOException ioe) { ioe.printStackTrace(); }
		}
	}
	
	public Socket getSocket() {
		return this.socket;
	}

	public boolean isRunning() {
		return running;
	}

	public void stopRunning() {
		running = false;
		
		// clean up after ourselves
		try {
			if( input != null )  input.close();
			if( output != null ) output.close();
			if( socket != null ) socket.close();
		}
		catch(IOException ioe) {ioe.printStackTrace(); }
	}

	public String getIPAddress() {
		return socket.getInetAddress().getHostAddress();
	}

	public String getInput() {
		return queuedLines.poll();
	}
	
	public InputStream getInputStream() {
		return this.input;
	}
	
	public OutputStream getOutputStream() {
		return this.output;
	}

	public void write(final char ch) {
		try {
			output.write(ch);
			//output.flush();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
			stopRunning();
		}
	}
	
	public void write(final byte b) {
		try {
			output.write(b);
			//output.flush();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
			stopRunning();
		}
	}

	public void write(final byte data[]) {
		try {
			output.write(data);
			output.flush();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
			stopRunning();
		}
	}
	
	public void write(final String data) {
		write(data.getBytes());
	}
	
	public void writeln(final String data) {
		write((data + "\r\n").getBytes());
	}
	
	public void write(final List<String> data) {
		for(final String string : data) {
			writeln(string);
		}
	}
	
	public void setConsole(boolean console) {
		this.console = console;
	}
	
	public boolean isConsole() {
		return this.console;
	}
	
	public boolean usingTelnet() {
		return this.telnet;
	}
	
	public void setResponseExpected(boolean re) {
		this.response_expected = re;
		this.response = null;
	}
	
	public String getResponse() {
		return this.response;
	}
	
	/*
	// special commands?
	final String cmd = sb.toString().trim();
	
	if( cmd.startsWith("/") ) {
		if( cmd.substring(1).equalsIgnoreCase("char-mode") ) {
			this.input_mode = CHAR_MODE;
		}
		else if( cmd.substring(1).equalsIgnoreCase("line-mode") ) {
			this.input_mode = LINE_MODE;
		}
	}
	else {
		this.queuedLines.add( sb.toString().trim() ); // convert stringbuffer to string
	}
	*/
}