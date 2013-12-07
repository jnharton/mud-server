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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import mud.protocols.Telnet;
import mud.utils.Utils;

public class Client implements Runnable {
	private final Socket socket;
	private final InputStream input;
	private final OutputStream output;

	private boolean telnet = true;
	private boolean tn_neg_seq = false;

	private final int BUF_SIZE = 4096;

	private final ConcurrentLinkedQueue<String> queuedLines = new ConcurrentLinkedQueue<String>();

	private boolean running;

	private final StringBuffer sb = new StringBuffer(80);
	private List<Byte> buffer = new ArrayList<Byte>();

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
		try {
			while( running ) {
				/*int readValue = 0;

				if( !tn_neg_seq ) {
					// this makes concessions to telnet clients and mud clients by capturing all data sent simultaneously
					while( input.available() > 0 ) {
						readValue = input.read();

						if( (byte) readValue == Telnet.IAC ) {
							buffer.clear();
							buffer.add( (byte) readValue );
							tn_neg_seq = true;
							break;
						}

						final Character ch = (char) readValue;

						if (ch == '\012' || ch == '\015') { // newline (\n) or carriage-return (\r)
							this.queuedLines.add( sb.toString() ); // convert stringbuffer to string
							sb.delete(0, sb.length());             // clear stringbuffer
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

						System.out.println("current telnet input: " + Utils.stringToArrayList( sb.toString(), "" )); // tell us the whole string
					}
				}
				else {
					while( input.available() > 0 ) {
						readValue = input.read();

						buffer.add( (byte) readValue );
						
						System.out.println("Read: " + readValue);
					}
				}*/

				// ---------------------------------------------------------------------------------
				
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
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			stopRunning();

		}
		finally {
			try { socket.close(); }
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isRunning() {
		return running;
	}

	public void stopRunning() {
		running = false;
	}

	public String ip() {
		return socket.getInetAddress().getHostAddress();
	}

	public String getInput() {
		return queuedLines.poll();
	}

	public void writeln(final String data) {
		write((data + "\r\n").getBytes());
	}

	public void write(final String data) {
		write(data.getBytes());
	}

	public void write(final char data) {
		try {
			output.write(data);
			output.flush();

		} catch (Exception e) {
			e.printStackTrace();
			stopRunning();
		}
	}

	public void write(final byte data[]) {
		try {
			output.write(data);
			output.flush();

		} catch (Exception e) {
			e.printStackTrace();
			stopRunning();
		}
	}

}
