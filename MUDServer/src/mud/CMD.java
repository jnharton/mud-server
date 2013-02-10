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

import mud.net.Client;
import mud.objects.Player;

/**
 * Class to hold a command and it's state of processing. Used
 * with user input and the CommandExec class
 * 
 * @author Jeremy
 *
 */
public class CMD {
	Status status;
	private String cmdString;
	private Player player;
	
	public enum Status {
		WAITING("waiting"),
		ACTIVE("active"),
		FINISHED("finished");
		
		private String text;
		
		private Status(String sText) {
			this.text = sText;
		}
		
		public String toString() {
			return this.text;
		}
	};

	private int perm = 0;

	public CMD(final String string, final Player player) {
		this.status = Status.WAITING;
		this.cmdString = string;
		this.player = player;
	}

	public CMD(final String string, final Player player, final int permission) {
		this(string, player);
		this.perm = permission;
	}

	public String getCmdString() {
		return this.cmdString;
	}

	public Client getClient() {
		return this.player.getClient();
	}

	public Player getPlayer() {
		return this.player;
	}

	public int getPermissions() {
		return this.perm;
	}
}