package mud.utils;

public class loginData {
	public int state;
	public String input;
	
	public String username;
	public String password;
	
	public loginData(int tState) {
		this.state = tState;
		
		this.username = "";
		this.password = "";
	}
}