/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Client - basic network client implementation
  Part of the Processing project - http://processing.org

  Copyright (c) 2004-2007 Ben Fry and Casey Reas
  The previous version of this code was developed by Hernando Barragan

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
 */

/**
 * Ripped out tons of ****.
 */
package mud.net;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Client implements Runnable {

    public boolean tn;

	final private Thread thread;
	final private Socket socket;
	final private InputStream input;
	final private OutputStream output;
	final private ConcurrentLinkedQueue<String> queuedLines = new ConcurrentLinkedQueue<String>();

    private final int BUF_SIZE = 4096;

    private boolean running;

	public Client(String host, int port) throws IOException {
        this(new Socket(host, port));
	}

	public Client(Socket socket) throws IOException {
		this.socket = socket;

		input = socket.getInputStream();
		output = socket.getOutputStream();

        running = true;
		thread = new Thread(this);
		thread.start();
	}

	public void stopRunning() {
        running = false;
	}

	public boolean isRunning() {
		return running;
	}

	public void run() {
        try {
            final byte buf[] = new byte[BUF_SIZE];
            while (running) {
                final int numRead = input.read(buf, 0, BUF_SIZE);
                if (numRead > 0) {
                    final String input = new String(buf, 0, numRead);
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
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception e) {}
            }
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
