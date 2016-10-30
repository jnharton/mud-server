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

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import mud.protocols.Telnet;
import mud.utils.Utils;

public class Client implements Runnable {
	public static final int TELNET_COMMAND_LENGTH = 3;
	public static final int BUF_SIZE = 4096;
	
	private final Socket socket;
	private final InputStream input;
	private final OutputStream output;

	private boolean running = false;
	private boolean tn_neg_seq = false; // indicates if the bytes currently being received are part of a negotiation sequence
	private boolean debug = false;      // start out with debug disabled

	// temporary storage
	private final StringBuffer sb = new StringBuffer(80);
	private List<Byte> buffer = new LinkedList<Byte>();

	// received data
	public final List<Byte[]> received_telnet_msgs = new LinkedList<Byte[]>();
	private final ConcurrentLinkedQueue<String> queuedLines = new ConcurrentLinkedQueue<String>();
	
	// response
	private String response = "";
	private boolean response_expected = false;

	public Client(final String host, final int port) throws IOException, UnknownHostException {
		this(new Socket(host, port));
	}

	public Client(final Socket socket) throws IOException, SocketException {
		this.socket = socket;
		
		// is this actually necessary?
		this.socket.setOOBInline(true);

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

					// if we are in a TELNET NEGOTIATON SEQUENCE
					if( tn_neg_seq ) {
						if( bytes < TELNET_COMMAND_LENGTH ) {
							buffer.add( (byte) readValue );
							bytes++;

							debug("Read: " + readValue);
						}

						if( bytes == TELNET_COMMAND_LENGTH ) {
							/*  response section -- properly belongs in the server end
							 * 
							 *  would be wise probably just to send a nice, fixed size array to the server and let it deal with the bytes
							 */

							Byte[] ba = new Byte[buffer.size()];

							buffer.toArray(ba);

							received_telnet_msgs.add(ba);

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

							debug("TELNET Command");
							debug("Read: " + readValue);

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
							if( last_ch == '\015' ) sb.delete(0, sb.length());
							else                    received_line = true;
						}
						else if(ch == '\015') { // carriage-return (\r)
							if( last_ch == '\012' ) sb.delete(0, sb.length());
							else                    received_line = true;
						}
						else if (ch == '\010') { // backspace
							if( sb.length() != 0 ) sb.deleteCharAt( sb.length() - 1 );
						}
						else { // any other character
							debug("Read: " + ch + "(" + readValue + ")");

							sb.append(ch);

							debug("current telnet input: " + Utils.stringToList( sb.toString() )); // tell us the whole string
						}

						last_ch = ch;

						if( received_line ) {
							final String line = sb.toString().trim();

							if( !response_expected ) this.queuedLines.add(line);
							else                     this.response = line;

							sb.delete(0, sb.length());

							received_line = false;     // reset received line indicator
						}
					}

					// ---------------------------------------------------------------------------------
					
					/*
					
					// can't use BufferedReader here - we need access to the buffer for telnet commands, 
					// which won't be followed by a newline
					final byte buf[] = new byte[BUF_SIZE];
					
					int numRead = 0;
					int numParsed = 0;

					while (running) {
						final int lastNumRead = input.read(buf, numRead, BUF_SIZE - numRead);
						
						numRead += lastNumRead;
						
						for(int n = numParsed; n < numRead - 1; n++) {
							// test for telnet bytes?
							if( 1 == 0 ) {
								// if it's a telnet byte append it to the current telnet messagee
							}
							else {
								// if we read more than one character total and we found a carriage-return ('\r)
								// or a newline (\n) character, turn the input into a string and dump it into
								// queued lines
								
								if( buf[n] == '\r' || buf[n] == '\n' ) {
									final String input = sb.toString();
									
									queuedLines.add( sb.toString() );
									
									sb.delete(0, sb.length());
								}
								else {
									sb.append((char) buf[n]);
								}
								
								numParsed++;
							}
						}
						
						if( numParsed == BUF_SIZE ) {
							numParsed = 0;
							
							if( BUF_SIZE - numRead == 0 ) {
								numRead = 0;   // TODO be careful with this one
							}
						}
					}
					
					*/
					
					/*if (numRead > 1 && (buf[numRead - 1] == '\r' || buf[numRead - 1] == '\n')) {
						final String input = new String(buf, 0, numRead); // convert read characters into string

						numRead = 0; // clear count of read characters

						// handles possibility of multiple lines in input (mud client, probably)
						for (final String line : input.split("(\r\n|\n|\r)")) {
							if (line != null && !"".equals(line)) {
								queuedLines.add(line);
							}
						}
					}*/
				}
				
				try { Thread.sleep(10); }
				catch (InterruptedException e) { e.printStackTrace(); }
			}
		}
		catch (final SocketException se) {
			// if we catch a SocketException, we have likely stopped the client intentionally,
			// in which case no warning is really necessary, otherwise we want to print a stack
			// trace and stop the client.
			if( running ) {
				se.printStackTrace();
				stopRunning();
			}
		}
		catch (final IOException ioe) {
			ioe.printStackTrace();
			stopRunning();
		}
		finally {
			// do I really need to do this? stopRunning() should handle closing the socket
			try { socket.close(); }
			catch (final IOException ioe) { ioe.printStackTrace(); }
		}
	}

	public Socket getSocket() {
		return this.socket;
	}

	public String getIPAddress() {
		return socket.getInetAddress().getHostAddress();
	}

	public boolean isRunning() {
		return this.running;
	}

	public void stopRunning() {
		this.running = false;

		// clean up after ourselves
		try {
			if( input != null )  this.input.close();
			if( output != null ) this.output.close();
			if( socket != null ) this.socket.close();
		}
		catch(final IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public String getInput() {
		return this.queuedLines.poll();
	}

	public void write(final char ch) {
		try {
			output.write(ch);
		}
		catch (final IOException ioe) {
			ioe.printStackTrace();
			stopRunning();
		}
	}

	public void write(final byte b) {
		try {
			output.write(b);
		}
		catch (final IOException ioe) {
			ioe.printStackTrace();
			stopRunning();
		}
	}

	public void write(final byte data[]) {
		try {
			output.write(data);
			output.flush();
		}
		catch (final IOException ioe) {
			ioe.printStackTrace();
			stopRunning();
		}
	}

	public void write(final String data) {
		write( data.getBytes() );
	}

	public void writeln(final String data) {
		write( (data + "\r\n").getBytes() );
	}

	public void write(final List<String> data) {
		for(final String string : data) {
			writeln(string);
		}
	}

	public void setDebug(boolean state) {
		this.debug = state;
	}

	public void setResponseExpected(boolean re) {
		this.response_expected = re;
		this.response = null;
	}

	public String getResponse() {
		return this.response;
	}

	private void debug(final String message) {
		if( debug ) {
			System.out.println(message);
		}
	}
}