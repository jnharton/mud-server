package mud;

import mud.net.Client;

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
	private Client client;
	
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

	public CMD(String string, Client client) {
		this.status = Status.WAITING;
		this.cmdString = string;
		this.client = client;
	}

	public CMD(String string, Client client, int permission) {
		this(string, client);
		this.perm = permission;
	}

	public String getCmdString() {
		return this.cmdString;
	}

	public Client getClient() {
		return this.client;
	}

	public int getPermissions() {
		return this.perm;
	}
}