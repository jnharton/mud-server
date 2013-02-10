package mud;

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

import mud.CMD.Status;
import mud.net.Client;
import mud.net.Server;

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

	private Server parent;
	private MUDServer parent1;
	private ConcurrentLinkedQueue<CMD> cmdQueue;
	private int sleepTime = 500; // 250ms, 0.25s
	
	private CMD newCmd;

	public CommandExec(Server parent, MUDServer parent1, ConcurrentLinkedQueue<CMD> cmdQueue) {
		this.parent = parent;
		this.parent1 = parent1;
		this.cmdQueue = cmdQueue;
	}

	@Override
	public void run() {
		while ( parent1.isRunning() ) {
			if (!this.cmdQueue.isEmpty()) {
				try {
					newCmd = this.cmdQueue.remove();
					
					newCmd.status = CMD.Status.ACTIVE; // mark command as being processed
					
					//System.out.println(newCmd.getCmdString() + ": " + newCmd.status); // tell us about it 
					
					// parse out stored data
					final String command = newCmd.getCmdString();
					final Client client = newCmd.getClient();
					
					// verify that client is still connected
					
					// try to capture errors
					try {
						// handle command permissions
						if ( parent1.checkAccess( client, newCmd.getPermissions() ) )
						{
							// interpret command
							parent1.cmd(command, client);
						}
						else {
							System.out.println("Insufficient Access Permissions");
						}
					}
					catch(Exception e) {
						e.printStackTrace();
					}
					
					newCmd.status = CMD.Status.FINISHED;
					
					//System.out.println(newCmd.getCmdString() + ": " + newCmd.status);
					
					// clear the processed command
					newCmd = null;
					
					if ( parent1.loginCheck( client ) ) {
						parent1.prompt(client); // buggy, especially when you're not logged on yet
					}
					
					// if the queue isn't empty, print out a list of the unresolved commands
					if (!this.cmdQueue.isEmpty()) {
						System.out.println("Queue");
						System.out.println("------------------------------");
						for (final CMD c : this.cmdQueue) {
							System.out.println(c.getCmdString().trim());
						}
					}
					
					// sleep between executing commands
					Thread.sleep(sleepTime);
				}
				catch (InterruptedException ie) {
					// if interrupted when we are beginning to process command
					System.out.println("Command Execution: Interrupted!");
					if (newCmd != null) {
						// if the command is still waiting
						if (newCmd.status == Status.WAITING) {
							this.cmdQueue.add(newCmd); // put it back into the queue
							newCmd = null;             // null our reference to it
						}
					}
					ie.printStackTrace();
				}
				catch(NullPointerException npe) {
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