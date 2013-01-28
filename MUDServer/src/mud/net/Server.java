/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Server - basic network server implementation
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
import java.util.*;

public class Server implements Runnable {

    final private Vector<Client> clients = new Vector<Client>();

    private ServerSocket server;
    private Thread thread;
    private boolean running;

    public Server(final int port) {

        try {
            server = new ServerSocket(port);
            thread = new Thread(this);
            thread.start();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

	public void stopRunning() {
        running = false;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean hasClients() {
		return !clients.isEmpty();
	}

 	public Client[] getClients() {
		return (Client[]) clients.toArray();
	}

    public void disconnect(final Client client) {
        client.stopRunning();
        clients.remove(client);
    }

    public void run() {
        try {
            while (running) {
                Socket socket = server.accept();
                Client client = new Client(socket);
                clients.add(client);
            }
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            for (final Client c : clients) {
                c.stopRunning();
            }
        }
    }

    public void write(final String data) {
        writeToAllClients(data.getBytes());
    }

    public void write(final char data) {
        writeToAllClients(new byte[]{ (byte) data });
    }

    public void writeToAllClients(final byte data[]) {
        for (final Client c : new ArrayList<Client>(clients)) {
            if (c.isRunning())
                c.write(data);
            else
                clients.remove(c);
        }
    }

}
