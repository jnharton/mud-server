package mud.utils;

/**
 * LoginData
 * 
 * A data storage for the multiple pieces of input required from the user
 * to complete a login.
 * 
 * @author Jeremy
 *
 */
public class LoginData {
	public static final int LOGIN_START = 0;
	public static final int LOGIN_NAME = 1;
	public static final int LOGIN_PASSWORD = 2;
	public static final int LOGIN_COMPLETE = 3;
	
	public String state = "";
	
	public String username;
	public String password;
	
	public String temp = "";
	
	public LoginData(final String initialState) {
		this.state = initialState;
		
		this.username = "";
		this.password = "";
	}
}