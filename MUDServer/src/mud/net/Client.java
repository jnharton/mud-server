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
import java.util.concurrent.ConcurrentLinkedQueue;

public class Client implements Runnable {

    public boolean tn = true;

	final private Socket socket;
	final private InputStream input;
	final private OutputStream output;
	final private ConcurrentLinkedQueue<String> queuedLines = new ConcurrentLinkedQueue<String>();

    private final int BUF_SIZE = 4096;

    private boolean running;

	public Client(final String host, final int port) throws IOException {
        this(new Socket(host, port));
	}

	public Client(final Socket socket) throws IOException {
		this.socket = socket;

		input = socket.getInputStream();
		output = socket.getOutputStream();

        running = true;
		new Thread(this).start();
	}

	public void stopRunning() {
        running = false;
	}

	public boolean isRunning() {
		return running;
	}

	public void run() {
        try {
            // can't use BufferedReader here - we need access to the buffer for telnet commands, 
            // which won't be followed by a newline
            final byte buf[] = new byte[BUF_SIZE];
            int numRead = 0;
            
            while (running) {
                final int lastNumRead = input.read(buf, numRead, BUF_SIZE - numRead);
                numRead += lastNumRead;
                if (numRead > 1 && (buf[numRead - 1] == '\r' || buf[numRead - 1] == '\n')) {
                    final String input = new String(buf, 0, numRead);
                    numRead = 0;
                    for (final String line : input.split("(\r\n|\n|\r)")) {
                        if (line != null && !"".equals(line)) {
                            queuedLines.add(line);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            stopRunning();

        } finally {
            try {
                socket.close();
            } catch (Exception e) {}
        }
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
