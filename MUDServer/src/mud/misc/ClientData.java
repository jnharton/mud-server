package mud.misc;

public class ClientData {
	public String loginstate = "";
	public String name = "";
	public String pass = "";
	/*public String data = "";
	
	public String lock_holder = "";
	public boolean lock = false;*/
	
	public ClientData() {		
	}
	
	public ClientData(final String loginState) {
		this.loginstate = loginState;
	}
}