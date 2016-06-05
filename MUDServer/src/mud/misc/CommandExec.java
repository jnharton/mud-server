package mud.misc;

/*
  Copyright (c) 2012 Jeremy N. Harton
  
  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
  documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
  rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
  persons to whom the Software is furnished to do so, subject to the following conditions:
  
  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
  Software.
  
  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
  SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

import java.util.concurrent.ConcurrentLinkedQueue;

import mud.MUDServer;
import mud.misc.CMD.Status;
import mud.net.Client;
import mud.objects.Player;

/**
 * A class which implements the Runnable interface as a means of
 * being itself a thread. This "thread" is used to execute commands sent by the user
 * independent of those commands being sent so that slow command execution doesn't in
 * any way slow the actual command input;
 * 
 * @author Jeremy
 *
 */
public class CommandExec implements Runnable {
	private MUDServer parent;
	private ConcurrentLinkedQueue<CMD> cmdQueue;
	private int sleepTime = 500; // 250ms, 0.25s
	
	private CMD newCmd;
	
	private boolean running = false;

	public CommandExec(MUDServer parent, ConcurrentLinkedQueue<CMD> cmdQueue) {
		this.parent = parent;
		this.cmdQueue = cmdQueue;
	}

	@Override
	public void run() {
		this.running = true;
		
		while ( this.running ) {
			if ( !this.cmdQueue.isEmpty() ) {
				try {
					newCmd = this.cmdQueue.poll();
					
					// should it be set to WAITING before it's put in the queue in the first place?
					newCmd.setStatus(CMD.Status.WAITING);
					
					// parse out stored data
					final String command = newCmd.getCmdString();
					final Client client = newCmd.getClient();
					
					final Player player = parent.getPlayer(client);
					
					// verify that client is still connected
					if( client.isRunning() ) {
						newCmd.setStatus(CMD.Status.ACTIVE); // mark command as being processed
						
						// try to capture errors
						try {
							// handle command permissions
							if ( parent.checkAccess(player, newCmd.getPermissions()) )
							{
								// interpret command
								parent.cmd(command, client);
							}
							else {
								// TODO write "error" to player?
								System.out.println("Insufficient Access Permissions");
							}
						}
						catch(final NullPointerException npe) {
							System.out.println("--- Stack Trace ---");
							npe.printStackTrace();
						}
						
						newCmd.setStatus(CMD.Status.FINISHED);
						
						// clear the processed command
						newCmd = null;
						
						if ( parent.loginCheck( client ) ) {
							parent.prompt(client); // buggy, especially when you're not logged on yet
						}
						
						// if the queue isn't empty, print out a list of the unresolved commands
						/*if ( !this.cmdQueue.isEmpty() ) {
							System.out.println("Queue");
							System.out.println("------------------------------");
							
							for (final CMD c : this.cmdQueue) {
								System.out.println( c.getCmdString().trim() );
							}
						}*/
						
						// sleep between executing commands
						Thread.sleep(sleepTime);
					}
					else continue;
				}
				catch (final InterruptedException ie) {
					// if interrupted when we are processing a command
					System.out.println("Command Execution: Interrupted!");
					
					System.out.println("--- Stack Trace ---");
					ie.printStackTrace();
					
					if (newCmd != null) {
						if (newCmd.getStatus() == Status.WAITING) {
							// if the command is still waiting (i.e. we never got to try to execute it)
							this.cmdQueue.add(newCmd);
						}
						else {
							parent.notify(parent.getPlayer(newCmd.getClient()), "Error: Failed Command Execution!");
						}
						
						newCmd = null; // null our reference to it
					}
				}
				catch(final NullPointerException npe) {
					System.out.println("--- Stack Trace ---");
					npe.printStackTrace();
				}
			}
		}
	}
	
	public int getCommandDelay() {
		return this.sleepTime;
	}
	
	public void setCommandDelay(int ms) {
		this.sleepTime = ms;
	}
}